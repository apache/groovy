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
import java.util.function.Predicate;

import static org.apache.groovy.ast.tools.ClassNodeUtils.getField;
import static org.apache.groovy.ast.tools.ClassNodeUtils.getPropNameForAccessor;
import static org.apache.groovy.ast.tools.ClassNodeUtils.hasPossibleStaticMethod;
import static org.apache.groovy.ast.tools.ClassNodeUtils.hasPossibleStaticProperty;
import static org.apache.groovy.ast.tools.ClassNodeUtils.hasStaticProperty;
import static org.apache.groovy.ast.tools.ClassNodeUtils.isInnerClass;
import static org.apache.groovy.ast.tools.ClassNodeUtils.isValidAccessorName;
import static org.apache.groovy.ast.tools.ExpressionUtils.transformInlineConstants;
import static org.apache.groovy.util.BeanUtils.capitalize;
import static org.codehaus.groovy.ast.tools.ClosureUtils.getParametersSafe;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getSetterName;

/**
 * Visitor to resolve constants and method calls from static imports.
 */
public class StaticImportVisitor extends ClassCodeExpressionTransformer {
    private ClassNode currentClass;
    private MethodNode currentMethod;
    private SourceUnit sourceUnit;
    private boolean inSpecialConstructorCall;
    private boolean inClosure;
    private boolean inPropertyExpression;
    private Expression foundConstant;
    private Expression foundArgs;
    private boolean inAnnotation;
    private boolean inLeftExpression;

    /**
     * Use {@link #StaticImportVisitor(ClassNode,SourceUnit)}.
     */
    @Deprecated
    public StaticImportVisitor() {
        this(null, null);
    }

    public StaticImportVisitor(final ClassNode classNode, final SourceUnit sourceUnit) {
        this.currentClass = classNode;
        this.sourceUnit = sourceUnit;
    }

    /**
     * Call {@link #StaticImportVisitor(ClassNode,SourceUnit)} then {@link #visitClass(ClassNode)}.
     */
    @Deprecated
    public void visitClass(final ClassNode classNode, final SourceUnit sourceUnit) {
        this.currentClass = classNode;
        this.sourceUnit = sourceUnit;
        visitClass(classNode);
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
        Class<? extends Expression> clazz = exp.getClass();
        if (clazz == VariableExpression.class) {
            return transformVariableExpression((VariableExpression) exp);
        }
        if (clazz == BinaryExpression.class) {
            return transformBinaryExpression((BinaryExpression) exp);
        }
        if (clazz == PropertyExpression.class) {
            return transformPropertyExpression((PropertyExpression) exp);
        }
        if (clazz == MethodCallExpression.class) {
            return transformMethodCallExpression((MethodCallExpression) exp);
        }
        if (exp instanceof ClosureExpression) {
            return transformClosureExpression((ClosureExpression) exp);
        }
        if (clazz == ConstructorCallExpression.class) {
            return transformConstructorCallExpression((ConstructorCallExpression) exp);
        }
        if (clazz == ArgumentListExpression.class) {
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
                    setSourcePosition(result, ve);
                    return result;
                }
            }
        }
        return ve;
    }

    protected Expression transformMethodCallExpression(MethodCallExpression mce) {
        Expression object = transform(mce.getObjectExpression());
        Expression method = transform(mce.getMethod());
        Expression args = transform(mce.getArguments());

        if (mce.isImplicitThis()) {
            if (currentClass.tryFindPossibleMethod(mce.getMethodAsString(), args) == null) {
                Expression result = findStaticMethodImportFromModule(method, args);
                if (result != null) {
                    setSourcePosition(result, mce);
                    return result;
                }
                if (method instanceof ConstantExpression && !inLeftExpression) {
                    // could be a closure field
                    String methodName = (String) ((ConstantExpression) method).getValue();
                    result = findStaticFieldOrPropAccessorImportFromModule(methodName);
                    if (result != null) {
                        result = new MethodCallExpression(result, "call", args);
                        result.setSourcePosition(mce);
                        return result;
                    }
                }
            }
        } else if (currentMethod != null && currentMethod.isStatic() && (object instanceof VariableExpression && ((VariableExpression) object).isSuperExpression())) {
            Expression result = new MethodCallExpression(new ClassExpression(currentClass.getSuperClass()), method, args);
            result.setSourcePosition(mce);
            return result;
        }

        if (method instanceof ConstantExpression && ((ConstantExpression) method).getValue() instanceof String && (mce.isImplicitThis()
                || (object instanceof VariableExpression && (((VariableExpression) object).isThisExpression() || ((VariableExpression) object).isSuperExpression())))) {
            String methodName = (String) ((ConstantExpression) method).getValue();

            boolean foundInstanceMethod = (currentMethod != null && !currentMethod.isStatic() && currentClass.hasPossibleMethod(methodName, args));

            Predicate<ClassNode> hasPossibleStaticMember = cn -> {
                if (hasPossibleStaticMethod(cn, methodName, args, true)) {
                    return true;
                }
                // GROOVY-9587: don't check for property for non-empty call args
                if (args instanceof TupleExpression && ((TupleExpression) args).getExpressions().isEmpty()
                        && hasPossibleStaticProperty(cn, methodName)) {
                    return true;
                }
                return false;
            };

            if (mce.isImplicitThis()) {
                if (isInnerClass(currentClass)) {
                    if (inSpecialConstructorCall && !foundInstanceMethod) {
                        // check for reference to outer class method in this(...) or super(...)
                        if (currentClass.getOuterClass().hasPossibleMethod(methodName, args)) {
                            object = new PropertyExpression(new ClassExpression(currentClass.getOuterClass()), new ConstantExpression("this"));
                        } else if (hasPossibleStaticMember.test(currentClass.getOuterClass())) {
                            Expression result = new StaticMethodCallExpression(currentClass.getOuterClass(), methodName, args);
                            result.setSourcePosition(mce);
                            return result;
                        }
                    }
                } else if (inSpecialConstructorCall || (!inClosure && !foundInstanceMethod && !methodName.equals("call"))) {
                    // check for reference to static method in this(...) or super(...) or when call not resolved
                    if (hasPossibleStaticMember.test(currentClass)) {
                        Expression result = new StaticMethodCallExpression(currentClass, methodName, args);
                        result.setSourcePosition(mce);
                        return result;
                    }
                }
            }
        }

        MethodCallExpression result = new MethodCallExpression(object, method, args);
        result.setGenericsTypes(mce.getGenericsTypes());
        result.setMethodTarget(mce.getMethodTarget());
        result.setImplicitThis(mce.isImplicitThis());
        result.setSpreadSafe(mce.isSpreadSafe());
        result.setSafe(mce.isSafe());
        result.setSourcePosition(mce);
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
        for (Parameter p : getParametersSafe(ce)) {
            if (p.hasInitialExpression()) {
                p.setInitialExpression(transform(p.getInitialExpression()));
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
                return newStaticMethodCallX(importNode.getType(), importNode.getFieldName(), args);
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
                    return newStaticMethodCallX(importClass, prefix(name) + capitalize(importMember), args);
                }
            }
        }
        Map<String, ImportNode> starImports = module.getStaticStarImports();
        ClassNode starImportType;
        if (currentClass.isEnum() && starImports.containsKey(currentClass.getName())) {
            ImportNode importNode = starImports.get(currentClass.getName());
            starImportType = importNode == null ? null : importNode.getType();
            expression = findStaticMethod(starImportType, name, args);
            return expression;
        } else {
            for (ImportNode importNode : starImports.values()) {
                starImportType = importNode == null ? null : importNode.getType();
                expression = findStaticMethod(starImportType, name, args);
                if (expression != null) return expression;
                expression = findStaticPropertyAccessorGivenArgs(starImportType, getPropNameForAccessor(name), args);
                if (expression != null) {
                    return newStaticMethodCallX(starImportType, name, args);
                }
            }
        }
        return null;
    }

    private static String prefix(String name) {
        return name.startsWith("is") ? "is" : name.substring(0, 3);
    }

    private String getAccessorName(String name) {
        return inLeftExpression ? getSetterName(name) : "get" + capitalize(name);
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
                accessor = newStaticMethodCallX(staticImportType, accessorName, ArgumentListExpression.EMPTY_ARGUMENTS);
            else
                accessor = newStaticPropertyX(staticImportType, propName);
        }
        return accessor;
    }

    private Expression findStaticPropertyAccessorByFullName(ClassNode staticImportType, String accessorMethodName) {
        // anything will do as we only check size == 1
        ArgumentListExpression dummyArgs = new ArgumentListExpression();
        dummyArgs.addExpression(EmptyExpression.INSTANCE);
        return findStaticMethod(staticImportType, accessorMethodName, (inLeftExpression ? dummyArgs : ArgumentListExpression.EMPTY_ARGUMENTS));
    }

    private static Expression findStaticField(ClassNode staticImportType, String fieldName) {
        if (staticImportType.isPrimaryClassNode() || staticImportType.isResolved()) {
            FieldNode field = getField(staticImportType, fieldName);
            if (field != null && field.isStatic())
                return newStaticPropertyX(staticImportType, fieldName);
        }
        return null;
    }

    private static Expression findStaticMethod(ClassNode staticImportType, String methodName, Expression args) {
        if (staticImportType.isPrimaryClassNode() || staticImportType.isResolved()) {
            if (staticImportType.hasPossibleStaticMethod(methodName, args)) {
                return newStaticMethodCallX(staticImportType, methodName, args);
            }
        }
        return null;
    }

    private static PropertyExpression newStaticPropertyX(ClassNode type, String name) {
        return new PropertyExpression(new ClassExpression(type.getPlainNodeReference()), name);
    }

    private static StaticMethodCallExpression newStaticMethodCallX(ClassNode type, String name, Expression args) {
        return new StaticMethodCallExpression(type.getPlainNodeReference(), name, args);
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return sourceUnit;
    }
}
