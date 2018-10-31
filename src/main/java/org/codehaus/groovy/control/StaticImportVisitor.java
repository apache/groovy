/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.control;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.AnnotationConstantExpression;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.EmptyExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.NamedArgumentListExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.syntax.Types;

import java.util.List;
import java.util.Map;

import static org.apache.groovy.ast.tools.ClassNodeUtils.getPropNameForAccessor;
import static org.apache.groovy.ast.tools.ClassNodeUtils.hasPossibleStaticMethod;
import static org.apache.groovy.ast.tools.ClassNodeUtils.hasPossibleStaticProperty;
import static org.apache.groovy.ast.tools.ClassNodeUtils.hasStaticProperty;
import static org.apache.groovy.ast.tools.ClassNodeUtils.isInnerClass;
import static org.apache.groovy.ast.tools.ClassNodeUtils.isValidAccessorName;
import static org.codehaus.groovy.runtime.MetaClassHelper.capitalize;
import static org.apache.groovy.ast.tools.ClassNodeUtils.getField;
import static org.apache.groovy.ast.tools.ExpressionUtils.transformInlineConstants;

/**
 * Visitor to resolve constants and method calls from static Imports
 */
public class StaticImportVisitor extends ClassCodeExpressionTransformer {
    private ClassNode currentClass;
    private MethodNode currentMethod;
    private SourceUnit source;
    private boolean inSpecialConstructorCall;
    private boolean inClosure;
    private boolean inPropertyExpression;
    private Expression foundConstant;
    private Expression foundArgs;
    private boolean inAnnotation;
    private boolean inLeftExpression;

    public void visitClass(ClassNode node, SourceUnit source) {
        this.currentClass = node;
        this.source = source;
        super.visitClass(node);
    }

    @Override
    protected void visitConstructorOrMethod(MethodNode node, boolean isConstructor) {
        this.currentMethod = node;
        super.visitConstructorOrMethod(node, isConstructor);
        this.currentMethod = null;
    }

    @Override
    public void visitAnnotations(AnnotatedNode node) {
        boolean oldInAnnotation = inAnnotation;
        inAnnotation = true;
        super.visitAnnotations(node);
        inAnnotation = oldInAnnotation;
    }

    public Expression transform(Expression exp) {
        if (exp == null) return null;
        if (exp.getClass() == VariableExpression.class) {
            return transformVariableExpression((VariableExpression) exp);
        }
        if (exp.getClass() == BinaryExpression.class) {
            return transformBinaryExpression((BinaryExpression) exp);
        }
        if (exp.getClass() == PropertyExpression.class) {
            return transformPropertyExpression((PropertyExpression) exp);
        }
        if (exp.getClass() == MethodCallExpression.class) {
            return transformMethodCallExpression((MethodCallExpression) exp);
        }
        if (exp.getClass() == ClosureExpression.class) {
            return transformClosureExpression((ClosureExpression) exp);
        }
        if (exp.getClass() == ConstructorCallExpression.class) {
            return transformConstructorCallExpression((ConstructorCallExpression) exp);
        }
        if (exp.getClass() == ArgumentListExpression.class) {
            Expression result = exp.transformExpression(this);
            if (inPropertyExpression) {
                foundArgs = result;
            }
            return result;
        }
        if (exp instanceof ConstantExpression) {
            Expression result = exp.transformExpression(this);
            if (inPropertyExpression) {
                foundConstant = result;
            }
            if (inAnnotation && exp instanceof AnnotationConstantExpression) {
                ConstantExpression ce = (ConstantExpression) result;
                if (ce.getValue() instanceof AnnotationNode) {
                    // replicate a little bit of AnnotationVisitor here
                    // because we can't wait until later to do this
                    AnnotationNode an = (AnnotationNode) ce.getValue();
                    Map<String, Expression> attributes = an.getMembers();
                    for (Map.Entry<String, Expression> entry : attributes.entrySet()) {
                        Expression attrExpr = transform(entry.getValue());
                        entry.setValue(attrExpr);
                    }

                }
            }
            return result;
        }
        return exp.transformExpression(this);
    }

    // if you have a Bar class with a static foo property, and this:
    //   import static Bar.foo as baz
    // then this constructor (not normal usage of statics):
    //   new Bar(baz:1)
    // will become:
    //   new Bar(foo:1)

    private Expression transformMapEntryExpression(MapEntryExpression me, ClassNode constructorCallType) {
        Expression key = me.getKeyExpression();
        Expression value = me.getValueExpression();
        ModuleNode module = currentClass.getModule();
        if (module != null && key instanceof ConstantExpression) {
            Map<String, ImportNode> importNodes = module.getStaticImports();
            if (importNodes.containsKey(key.getText())) {
                ImportNode importNode = importNodes.get(key.getText());
                if (importNode.getType().equals(constructorCallType)) {
                    String newKey = importNode.getFieldName();
                    return new MapEntryExpression(new ConstantExpression(newKey), value.transformExpression(this));
                }
            }
        }
        return me;
    }

    protected Expression transformBinaryExpression(BinaryExpression be) {
        int type = be.getOperation().getType();
        boolean oldInLeftExpression;
        Expression right = transform(be.getRightExpression());
        be.setRightExpression(right);
        Expression left;
        if (type == Types.EQUAL && be.getLeftExpression() instanceof VariableExpression) {
            oldInLeftExpression = inLeftExpression;
            inLeftExpression = true;
            left = transform(be.getLeftExpression());
            inLeftExpression = oldInLeftExpression;
            if (left instanceof StaticMethodCallExpression) {
                StaticMethodCallExpression smce = (StaticMethodCallExpression) left;
                StaticMethodCallExpression result = new StaticMethodCallExpression(smce.getOwnerType(), smce.getMethod(), right);
                setSourcePosition(result, be);
                return result;
            }
        } else {
            left = transform(be.getLeftExpression());
        }
        be.setLeftExpression(left);
        return be;
    }

    protected Expression transformVariableExpression(VariableExpression ve) {
        Variable v = ve.getAccessedVariable();
        if (v instanceof DynamicVariable) {
            Expression result = findStaticFieldOrPropAccessorImportFromModule(v.getName());
            if (result != null) {
                setSourcePosition(result, ve);
                if (inAnnotation) {
                    result = transformInlineConstants(result);
                }
                return result;
            }
        } else if (v instanceof FieldNode) {
            if (inSpecialConstructorCall) { // GROOVY-8819
                FieldNode fn = (FieldNode) v;
                ClassNode declaringClass = fn.getDeclaringClass();
                if (fn.isStatic() && currentClass.isDerivedFrom(declaringClass)) {
                    Expression result = new PropertyExpression(new ClassExpression(declaringClass), v.getName());
                    result.setSourcePosition(ve);

                    return result;
                }
            }
        }
        return ve;
    }

    /**
     * Set the source position of toSet including its property expression if it has one.
     *
     * @param toSet resulting node
     * @param origNode original node
     */
    private static void setSourcePosition(Expression toSet, Expression origNode) {
        toSet.setSourcePosition(origNode);
        if (toSet instanceof PropertyExpression) {
            ((PropertyExpression) toSet).getProperty().setSourcePosition(origNode);
        }
    }

    protected Expression transformMethodCallExpression(MethodCallExpression mce) {
        Expression args = transform(mce.getArguments());
        Expression method = transform(mce.getMethod());
        Expression object = transform(mce.getObjectExpression());
        boolean isExplicitThisOrSuper = false;
        boolean isExplicitSuper = false;
        if (object instanceof VariableExpression) {
            VariableExpression ve = (VariableExpression) object;
            isExplicitThisOrSuper = !mce.isImplicitThis() && (ve.isThisExpression() || ve.isSuperExpression());
            isExplicitSuper = ve.isSuperExpression();
        }

        if (mce.isImplicitThis() || isExplicitThisOrSuper) {
            if (mce.isImplicitThis()) {
                if (null == currentClass.tryFindPossibleMethod(mce.getMethodAsString(), args)) {

                    Expression ret = findStaticMethodImportFromModule(method, args);
                    if (ret != null) {
                        setSourcePosition(ret, mce);
                        return ret;
                    }
                    if (method instanceof ConstantExpression && !inLeftExpression) {
                        // could be a closure field
                        String methodName = (String) ((ConstantExpression) method).getValue();
                        ret = findStaticFieldOrPropAccessorImportFromModule(methodName);
                        if (ret != null) {
                            ret = new MethodCallExpression(ret, "call", args);
                            setSourcePosition(ret, mce);
                            return ret;
                        }
                    }
                }
            } else if (currentMethod!=null && currentMethod.isStatic() && isExplicitSuper) {
                MethodCallExpression ret = new MethodCallExpression(new ClassExpression(currentClass.getSuperClass()), method, args);
                setSourcePosition(ret, mce);
                return ret;
            }

            if (method instanceof ConstantExpression) {
                ConstantExpression ce = (ConstantExpression) method;
                Object value = ce.getValue();
                if (value instanceof String) {
                    boolean foundInstanceMethod = false;
                    String methodName = (String) value;
                    boolean inInnerClass = isInnerClass(currentClass);
                    if (currentMethod != null && !currentMethod.isStatic()) {
                        if (currentClass.hasPossibleMethod(methodName, args)) {
                            foundInstanceMethod = true;
                        }
                    }
                    boolean lookForPossibleStaticMethod = !methodName.equals("call");
                    lookForPossibleStaticMethod &= !foundInstanceMethod;
                    lookForPossibleStaticMethod |= inSpecialConstructorCall;
                    lookForPossibleStaticMethod &= !inInnerClass;
                    if (!inClosure && lookForPossibleStaticMethod &&
                            (hasPossibleStaticMethod(currentClass, methodName, args, true))
                            || hasPossibleStaticProperty(currentClass, methodName)) {
                        StaticMethodCallExpression smce = new StaticMethodCallExpression(currentClass, methodName, args);
                        setSourcePosition(smce, mce);
                        return smce;
                    }
                    if (!inClosure && inInnerClass && inSpecialConstructorCall && mce.isImplicitThis() && !foundInstanceMethod) {
                        if (currentClass.getOuterClass().hasPossibleMethod(methodName, args)) {
                            object = new PropertyExpression(new ClassExpression(currentClass.getOuterClass()), new ConstantExpression("this"));
                        } else if (hasPossibleStaticMethod(currentClass.getOuterClass(), methodName, args, true)
                                || hasPossibleStaticProperty(currentClass.getOuterClass(), methodName)) {
                            StaticMethodCallExpression smce = new StaticMethodCallExpression(currentClass.getOuterClass(), methodName, args);
                            setSourcePosition(smce, mce);
                            return smce;
                        }
                    }

                    if (mce.isImplicitThis() && lookForPossibleStaticMethod && hasPossibleStaticMethod(currentClass, methodName, args, true)) {
                        StaticMethodCallExpression result = new StaticMethodCallExpression(currentClass, methodName, args);
                        result.setSourcePosition(mce);
                        return result;
                    }
                }
            }
        }

        MethodCallExpression result = new MethodCallExpression(object, method, args);
        result.setSafe(mce.isSafe());
        result.setImplicitThis(mce.isImplicitThis());
        result.setSpreadSafe(mce.isSpreadSafe());
        result.setMethodTarget(mce.getMethodTarget());
        // GROOVY-6757
        result.setGenericsTypes(mce.getGenericsTypes());
        setSourcePosition(result, mce);
        return result;
    }

    protected Expression transformConstructorCallExpression(ConstructorCallExpression cce) {
        inSpecialConstructorCall = cce.isSpecialCall();
        Expression expression = cce.getArguments();
        if (expression instanceof TupleExpression) {
            TupleExpression tuple = (TupleExpression) expression;
            if (tuple.getExpressions().size() == 1) {
                expression = tuple.getExpression(0);
                if (expression instanceof NamedArgumentListExpression) {
                    NamedArgumentListExpression namedArgs = (NamedArgumentListExpression) expression;
                    List<MapEntryExpression> entryExpressions = namedArgs.getMapEntryExpressions();
                    for (int i = 0; i < entryExpressions.size(); i++) {
                        entryExpressions.set(i, (MapEntryExpression) transformMapEntryExpression(entryExpressions.get(i), cce.getType()));
                    }
                }
            }
        }
        Expression ret = cce.transformExpression(this);
        inSpecialConstructorCall = false;
        return ret;
    }

    protected Expression transformClosureExpression(ClosureExpression ce) {
        boolean oldInClosure = inClosure;
        inClosure = true;
        if (ce.getParameters() != null) {
            for (Parameter p : ce.getParameters()) {
                if (p.hasInitialExpression()) {
                    p.setInitialExpression(transform(p.getInitialExpression()));
                }
            }
        }
        Statement code = ce.getCode();
        if (code != null) code.visit(this);
        inClosure = oldInClosure;
        return ce;
    }

    protected Expression transformPropertyExpression(PropertyExpression pe) {
        if (currentMethod!=null && currentMethod.isStatic()
                && pe.getObjectExpression() instanceof VariableExpression
                && ((VariableExpression) pe.getObjectExpression()).isSuperExpression()) {
            PropertyExpression pexp = new PropertyExpression(
                    new ClassExpression(currentClass.getSuperClass()),
                    transform(pe.getProperty())
            );
            pexp.setSourcePosition(pe);
            return pexp;
        }
        boolean oldInPropertyExpression = inPropertyExpression;
        Expression oldFoundArgs = foundArgs;
        Expression oldFoundConstant = foundConstant;
        inPropertyExpression = true;
        foundArgs = null;
        foundConstant = null;
        Expression objectExpression = transform(pe.getObjectExpression());
        boolean candidate = false;
        if (objectExpression instanceof MethodCallExpression) {
            candidate = ((MethodCallExpression)objectExpression).isImplicitThis();
        }

        if (foundArgs != null && foundConstant != null && candidate) {
            Expression result = findStaticMethodImportFromModule(foundConstant, foundArgs);
            if (result != null) {
                objectExpression = result;
                objectExpression.setSourcePosition(pe);
            }
        }
        inPropertyExpression = oldInPropertyExpression;
        foundArgs = oldFoundArgs;
        foundConstant = oldFoundConstant;
        pe.setObjectExpression(objectExpression);
        return pe;
    }

    private Expression findStaticFieldOrPropAccessorImportFromModule(String name) {
        ModuleNode module = currentClass.getModule();
        if (module == null) return null;
        Map<String, ImportNode> importNodes = module.getStaticImports();
        Expression expression;
        String accessorName = getAccessorName(name);
        // look for one of these:
        //   import static MyClass.setProp [as setOtherProp]
        //   import static MyClass.getProp [as getOtherProp]
        // when resolving prop reference
        if (importNodes.containsKey(accessorName)) {
            expression = findStaticProperty(importNodes, accessorName);
            if (expression != null) return expression;
        }
        if (accessorName.startsWith("get")) {
            accessorName = "is" + accessorName.substring(3);
            if (importNodes.containsKey(accessorName)) {
                expression = findStaticProperty(importNodes, accessorName);
                if (expression != null) return expression;
            }
        }

        // look for one of these:
        //   import static MyClass.prop [as otherProp]
        // when resolving prop or field reference
        if (importNodes.containsKey(name)) {
            ImportNode importNode = importNodes.get(name);
            expression = findStaticPropertyAccessor(importNode.getType(), importNode.getFieldName());
            if (expression != null) return expression;
            expression = findStaticField(importNode.getType(), importNode.getFieldName());
            if (expression != null) return expression;
        }
        // look for one of these:
        //   import static MyClass.*
        // when resolving prop or field reference
        for (ImportNode importNode : module.getStaticStarImports().values()) {
            ClassNode node = importNode.getType();
            expression = findStaticPropertyAccessor(node, name);
            if (expression != null) return expression;
            expression = findStaticField(node, name);
            if (expression != null) return expression;
        }
        return null;
    }

    private Expression findStaticProperty(Map<String, ImportNode> importNodes, String accessorName) {
        Expression result = null;
        ImportNode importNode = importNodes.get(accessorName);
        ClassNode importClass = importNode.getType();
        String importMember = importNode.getFieldName();
        result = findStaticPropertyAccessorByFullName(importClass, importMember);
        if (result == null) {
            result = findStaticPropertyAccessor(importClass, getPropNameForAccessor(importMember));
        }
        return result;
    }

    private Expression findStaticMethodImportFromModule(Expression method, Expression args) {
        ModuleNode module = currentClass.getModule();
        if (module == null || !(method instanceof ConstantExpression)) return null;
        Map<String, ImportNode> importNodes = module.getStaticImports();
        ConstantExpression ce = (ConstantExpression) method;
        Expression expression;
        Object value = ce.getValue();
        // skip non-Strings, e.g. Integer
        if (!(value instanceof String)) return null;
        final String name = (String) value;
        // look for one of these:
        //   import static SomeClass.method [as otherName]
        // when resolving methodCall() or getProp() or setProp()
        if (importNodes.containsKey(name)) {
            ImportNode importNode = importNodes.get(name);
            expression = findStaticMethod(importNode.getType(), importNode.getFieldName(), args);
            if (expression != null) return expression;
            expression = findStaticPropertyAccessorGivenArgs(importNode.getType(), getPropNameForAccessor(importNode.getFieldName()), args);
            if (expression != null) {
                return new StaticMethodCallExpression(importNode.getType(), importNode.getFieldName(), args);
            }
        }
        // look for one of these:
        //   import static SomeClass.someProp [as otherName]
        // when resolving getProp() or setProp()
        if (isValidAccessorName(name)) {
            String propName = getPropNameForAccessor(name);
            if (importNodes.containsKey(propName)) {
                ImportNode importNode = importNodes.get(propName);
                ClassNode importClass = importNode.getType();
                String importMember = importNode.getFieldName();
                expression = findStaticMethod(importClass, prefix(name) + capitalize(importMember), args);
                if (expression != null) return expression;
                expression = findStaticPropertyAccessorGivenArgs(importClass, importMember, args);
                if (expression != null) {
                    return new StaticMethodCallExpression(importClass, prefix(name) + capitalize(importMember), args);
                }
            }
        }
        Map<String, ImportNode> starImports = module.getStaticStarImports();
        ClassNode starImportType;
        if (currentClass.isEnum() && starImports.containsKey(currentClass.getName())) {
            ImportNode importNode = starImports.get(currentClass.getName());
            starImportType = importNode == null ? null : importNode.getType();
            expression = findStaticMethod(starImportType, name, args);
            if (expression != null) return expression;
        } else {
            for (ImportNode importNode : starImports.values()) {
                starImportType = importNode == null ? null : importNode.getType();
                expression = findStaticMethod(starImportType, name, args);
                if (expression != null) return expression;
                expression = findStaticPropertyAccessorGivenArgs(starImportType, getPropNameForAccessor(name), args);
                if (expression != null) {
                    return new StaticMethodCallExpression(starImportType, name, args);
                }
            }
        }
        return null;
    }

    private static String prefix(String name) {
        return name.startsWith("is") ? "is" : name.substring(0, 3);
    }

    private String getAccessorName(String name) {
        return (inLeftExpression ? "set" : "get") + capitalize(name);
    }

    private Expression findStaticPropertyAccessorGivenArgs(ClassNode staticImportType, String propName, Expression args) {
        // TODO validate args?
        return findStaticPropertyAccessor(staticImportType, propName);
    }

    private Expression findStaticPropertyAccessor(ClassNode staticImportType, String propName) {
        String accessorName = getAccessorName(propName);
        Expression accessor = findStaticPropertyAccessorByFullName(staticImportType, accessorName);
        if (accessor == null && accessorName.startsWith("get")) {
            accessor = findStaticPropertyAccessorByFullName(staticImportType, "is" + accessorName.substring(3));
        }
        if (accessor == null && hasStaticProperty(staticImportType, propName)) {
            // args will be replaced
            if (inLeftExpression)
                accessor = new StaticMethodCallExpression(staticImportType, accessorName, ArgumentListExpression.EMPTY_ARGUMENTS);
            else
                accessor = new PropertyExpression(new ClassExpression(staticImportType), propName);
        }
        return accessor;
    }

    private Expression findStaticPropertyAccessorByFullName(ClassNode staticImportType, String accessorMethodName) {
        // anything will do as we only check size == 1
        ArgumentListExpression dummyArgs = new ArgumentListExpression();
        dummyArgs.addExpression(new EmptyExpression());
        return findStaticMethod(staticImportType, accessorMethodName, (inLeftExpression ? dummyArgs : ArgumentListExpression.EMPTY_ARGUMENTS));
    }

    private static Expression findStaticField(ClassNode staticImportType, String fieldName) {
        if (staticImportType.isPrimaryClassNode() || staticImportType.isResolved()) {
            FieldNode field = getField(staticImportType, fieldName);
            if (field != null && field.isStatic())
                return new PropertyExpression(new ClassExpression(staticImportType), fieldName);
        }
        return null;
    }

    private static Expression findStaticMethod(ClassNode staticImportType, String methodName, Expression args) {
        if (staticImportType.isPrimaryClassNode() || staticImportType.isResolved()) {
            if (staticImportType.hasPossibleStaticMethod(methodName, args)) {
                return new StaticMethodCallExpression(staticImportType, methodName, args);
            }
        }
        return null;
    }

    protected SourceUnit getSourceUnit() {
        return source;
    }
}
