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
import org.codehaus.groovy.ast.ClassHelper;
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

import static java.lang.reflect.Modifier.isPrivate;
import static java.lang.reflect.Modifier.isProtected;
import static java.lang.reflect.Modifier.isPublic;
import static org.apache.groovy.ast.tools.ClassNodeUtils.getField;
import static org.apache.groovy.ast.tools.ClassNodeUtils.getPropNameForAccessor;
import static org.apache.groovy.ast.tools.ClassNodeUtils.hasPossibleStaticMethod;
import static org.apache.groovy.ast.tools.ClassNodeUtils.hasPossibleStaticProperty;
import static org.apache.groovy.ast.tools.ClassNodeUtils.hasStaticProperty;
import static org.apache.groovy.ast.tools.ClassNodeUtils.isInnerClass;
import static org.apache.groovy.ast.tools.ClassNodeUtils.isValidAccessorName;
import static org.apache.groovy.ast.tools.ClassNodeUtils.samePackageName;
import static org.apache.groovy.ast.tools.ExpressionUtils.isSuperExpression;
import static org.apache.groovy.ast.tools.ExpressionUtils.transformInlineConstants;
import static org.apache.groovy.util.BeanUtils.capitalize;
import static org.codehaus.groovy.ast.tools.ClosureUtils.getParametersSafe;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getGetterName;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getSetterName;
import static org.codehaus.groovy.transform.trait.Traits.isTrait;

/**
 * Visitor to resolve constants and method calls from static imports.
 */
public class StaticImportVisitor extends ClassCodeExpressionTransformer {

    private SourceUnit sourceUnit;
    private ClassNode  currentClass;
    private MethodNode currentMethod;

    private Expression foundArgs;
    private Expression foundConstant;

    private boolean inClosure;
    private boolean inAnnotation;
    private boolean inLeftExpression;
    private boolean inPropertyExpression;
    private boolean inSpecialConstructorCall;

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

    @Override
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
            if (foundArgs == null && inPropertyExpression) {
                foundArgs = result;
            }
            return result;
        }
        if (exp instanceof ConstantExpression) {
            Expression result = exp.transformExpression(this);
            if (foundConstant == null && inPropertyExpression) {
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
                result.copyNodeMetaData(smce);
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
            Expression result = findStaticFieldOrPropertyAccessorImportFromModule(v.getName());
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

        // GROOVY-10396: skip the instance method checks when the context is static with-respect-to current class
        boolean staticWrtCurrent = inSpecialConstructorCall || currentMethod != null && currentMethod.isStatic();

        if (mce.isImplicitThis()) {
            String name = mce.getMethodAsString();
            boolean thisOrSuperMethod = staticWrtCurrent ? hasPossibleStaticMethod(currentClass, name, args, true) : currentClass.tryFindPossibleMethod(name, args) != null;
            if (!thisOrSuperMethod && currentClass.getOuterClasses().stream().noneMatch(oc -> oc.tryFindPossibleMethod(name, args) != null)) {
                Expression result = findStaticMethodImportFromModule(method, args);
                if (result != null) {
                    setSourcePosition(result, mce);
                    return result;
                }
            }
        } else if (staticWrtCurrent && isSuperExpression(object)) {
            Expression result = new MethodCallExpression(new ClassExpression(currentClass.getSuperClass()), method, args);
            result.setSourcePosition(mce);
            return result;
        }

        if (method instanceof ConstantExpression && ((ConstantExpression) method).getValue() instanceof String
                && mce.isImplicitThis() && !isTrait(currentClass)) { // GROOVY-7191, GROOVY-8272, GROOVY-10312
            String name = mce.getMethodAsString();

            boolean foundInstanceMethod = !staticWrtCurrent && currentClass.hasPossibleMethod(name, args);

            Predicate<ClassNode> hasPossibleStaticMember = cn -> {
                if (hasPossibleStaticMethod(cn, name, args, true)) {
                    return true;
                }
                // GROOVY-9587: skip property check for non-empty call arguments
                if (args instanceof TupleExpression && ((TupleExpression) args).getExpressions().isEmpty()
                        && hasPossibleStaticProperty(cn, name)) {
                    return true;
                }
                return false;
            };

            if (isInnerClass(currentClass)) {
                if (inSpecialConstructorCall && !foundInstanceMethod) {
                    // check for reference to outer class method in this(...) or super(...)
                    if (currentClass.getOuterClass().hasPossibleMethod(name, args)) {
                        object = new PropertyExpression(new ClassExpression(currentClass.getOuterClass()), new ConstantExpression("this"));
                    } else if (hasPossibleStaticMember.test(currentClass.getOuterClass())) {
                        Expression result = new StaticMethodCallExpression(currentClass.getOuterClass(), name, args);
                        result.setSourcePosition(mce);
                        return result;
                    }
                }
            } else if (inSpecialConstructorCall || (!inClosure && !foundInstanceMethod && !name.equals("call"))) {
                // check for reference to static method in this(...) or super(...) or when call not resolved
                if (hasPossibleStaticMember.test(currentClass)) {
                    Expression result = new StaticMethodCallExpression(currentClass, name, args);
                    result.setSourcePosition(mce);
                    return result;
                }
            }
        }

        MethodCallExpression result = new MethodCallExpression(object, method, args);
        result.setGenericsTypes(mce.getGenericsTypes()); // GROOVY-6757
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
                    entryExpressions.replaceAll(me -> (MapEntryExpression) transformMapEntryExpression(me, cce.getType()));
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
        if (currentMethod != null && currentMethod.isStatic()
                && isSuperExpression(pe.getObjectExpression())) {
            PropertyExpression pexp = new PropertyExpression(
                    new ClassExpression(currentClass.getUnresolvedSuperClass()),
                    transform(pe.getProperty())
            );
            pexp.setSourcePosition(pe);
            return pexp;
        }

        boolean oldInPropertyExpression = inPropertyExpression;
        Expression oldFoundConstant = foundConstant;
        Expression oldFoundArgs = foundArgs;
        inPropertyExpression = true;
        foundConstant = null;
        foundArgs = null;
        Expression objectExpression = transform(pe.getObjectExpression());
        if (foundArgs != null && foundConstant != null
                && !foundConstant.getText().trim().isEmpty()
                && objectExpression instanceof MethodCallExpression
                && ((MethodCallExpression) objectExpression).isImplicitThis()) {
            Expression result = findStaticMethodImportFromModule(foundConstant, foundArgs);
            if (result != null) {
                objectExpression = result;
                objectExpression.setSourcePosition(pe);
            }
        }
        inPropertyExpression = oldInPropertyExpression;
        foundConstant = oldFoundConstant;
        foundArgs = oldFoundArgs;

        pe.setObjectExpression(objectExpression);
        return pe;
    }

    //--------------------------------------------------------------------------

    private Expression findStaticFieldOrPropertyAccessorImportFromModule(String name) {
        ModuleNode module = currentClass.getModule(); if (module == null) return null;
        Map<String, ImportNode> staticImports = module.getStaticImports();

        // look for one of these:
        //   import static MyClass.isProp [as isOtherProp]
        //   import static MyClass.getProp [as getOtherProp]
        //   import static MyClass.setProp [as setOtherProp]
        // when resolving property reference
        Expression expression;
        if (!inLeftExpression) {
            expression = findStaticProperty(staticImports, "is" + capitalize(name));
            if (expression != null) return expression;
        }
        expression = findStaticProperty(staticImports, getAccessorName(name));
        if (expression != null) return expression;

        // look for one of these:
        //   import static MyClass.prop [as otherProp]
        // when resolving property or field reference
        if (staticImports.containsKey(name)) { ImportNode importNode = staticImports.get(name);
            expression = findStaticPropertyOrField(importNode.getType(), importNode.getFieldName());
            if (expression != null) return expression;
        }

        // look for one of these:
        //   import static MyClass.*
        // when resolving property or field reference
        for (ImportNode importNode : module.getStaticStarImports().values()) {
            expression = findStaticPropertyOrField(importNode.getType(), name);
            if (expression != null) return expression;
        }

        return null;
    }

    private Expression findStaticMethodImportFromModule(Expression method, Expression args) {
        if (currentClass.getModule() == null) return null;
        if (!(method instanceof ConstantExpression)) return null;
        if (!(((ConstantExpression) method).getValue() instanceof String)) return null;

        Expression expression;
        String name = method.getText();
        Map<String, ImportNode> staticImports = currentClass.getModule().getStaticImports();
        // look for one of these:
        //   import static MyClass.field [as alias]
        //   import static MyClass.method [as alias]
        //   import static MyClass.property [as alias]
        // when resolving implicit-this call name(args)
        if (staticImports.containsKey(name)) {
            ImportNode importNode = staticImports.get(name);
            expression = findStaticMethod(importNode.getType(), importNode.getFieldName(), args);
            if (expression != null) return expression;
            if (!inLeftExpression) { // GROOVY-11056, et al.
                expression = findStaticPropertyOrField(importNode.getType(), importNode.getFieldName());
                if (expression != null) { // assume name refers to a callable static field/property
                    MethodCallExpression call = new MethodCallExpression(expression, "call", args);
                    call.setImplicitThis(false);
                    return call;
                }
            }
        }
        // look for one of these:
        //   import static MyClass.property [as alias]
        //   import static MyClass.isProperty [as alias]
        //   import static MyClass.getProperty [as alias]
        //   import static MyClass.setProperty [as alias]
        // when resolving isName(), getName() or setName(args)
        boolean accessor = isValidAccessorName(name);
        if (accessor) {
            ImportNode importNode = staticImports.get(name);
            if (importNode != null) {
                String propName = getPropNameForAccessor(importNode.getFieldName());
                expression = findStaticPropertyAccessorGivenArgs(importNode.getType(), propName, args);
                if (expression != null) { // expression may refer to getter or setter, so make new call
                    return newStaticMethodCallX(importNode.getType(), importNode.getFieldName(), args);
                }
            }
            importNode = staticImports.get(getPropNameForAccessor(name));
            if (importNode != null) {
                ClassNode importType = importNode.getType();
                String importMember = importNode.getFieldName();
                expression = findStaticMethod(importType, prefix(name) + capitalize(importMember), args);
                if (expression != null) return expression;
                expression = findStaticPropertyAccessorGivenArgs(importType, importMember, args);
                if (expression != null) {
                    return newStaticMethodCallX(importType, prefix(name) + capitalize(importMember), args);
                }
            }
        }

        Map<String, ImportNode> staticStarImports = currentClass.getModule().getStaticStarImports();
        if (currentClass.isEnum() && staticStarImports.containsKey(currentClass.getName())) {
            ImportNode importNode = staticStarImports.get(currentClass.getName());
            expression = findStaticMethod(importNode.getType(), name, args);
            return expression;
        }
        // look for one of these:
        //   import static MyClass.*
        // when resolving name(args), getName(), etc.
        for (ImportNode importNode : staticStarImports.values()) {
            ClassNode importType = importNode.getType();
            expression = findStaticMethod(importType, name, args);
            if (expression != null) return expression;
            if (!inLeftExpression) { // GROOVY-10329, et al.
                expression = findStaticPropertyOrField(importType, name);
                if (expression != null) { // assume name refers to a callable static field/property
                    MethodCallExpression call = new MethodCallExpression(expression, "call", args);
                    call.setImplicitThis(false);
                    return call;
                }
            }
            if (accessor) {
                String propName = getPropNameForAccessor(name);
                expression = findStaticPropertyAccessorGivenArgs(importType, propName, args);
                if (expression != null) { // expression may refer to getter or setter, so ...
                    return newStaticMethodCallX(importType, name, args);
                }
            }
        }
        return null;
    }

    private Expression findStaticPropertyAccessorByFullName(ClassNode staticImportType, String accessorName) {
        Expression argumentList = inLeftExpression ? new ArgumentListExpression(EmptyExpression.INSTANCE) : ArgumentListExpression.EMPTY_ARGUMENTS;
        Expression accessorExpr = findStaticMethod(staticImportType, accessorName, argumentList);
        if (accessorExpr != null && accessorName.startsWith("is")) { // GROOVY-9382, GROOVY-10133
            MethodNode method = staticImportType.getMethod(accessorName, Parameter.EMPTY_ARRAY);
            if (method == null || !ClassHelper.isPrimitiveBoolean(method.getReturnType())) {
                accessorExpr = null;
            }
        }
        return accessorExpr;
    }

    private Expression findStaticPropertyAccessorGivenArgs(ClassNode staticImportType, String propName, Expression args) {
        return findStaticPropertyAccessor(staticImportType, propName); // TODO: validate args?
    }

    private Expression findStaticPropertyAccessor(ClassNode staticImportType, String propName) {
        String accessorName = getAccessorName(propName);
        Expression accessor = null;
        if (!inLeftExpression) {
            accessor = findStaticPropertyAccessorByFullName(staticImportType, "is" + accessorName.substring(3));
        }
        if (accessor == null) {
            accessor = findStaticPropertyAccessorByFullName(staticImportType, accessorName);
        }
        if (accessor == null && hasStaticProperty(staticImportType, propName)) {
            if (inLeftExpression)
                accessor = newStaticMethodCallX(staticImportType, accessorName, ArgumentListExpression.EMPTY_ARGUMENTS); // <-- will be replaced
            else
                accessor = newStaticPropertyX(staticImportType, propName);
        }
        return accessor;
    }

    private Expression findStaticPropertyOrField(ClassNode staticImportType, String variableName) {
        Expression expression = findStaticPropertyAccessor(staticImportType, variableName);
        if (expression == null) {
            if (staticImportType.isPrimaryClassNode() || staticImportType.isResolved()) {
                FieldNode field = getField(staticImportType, variableName, FieldNode::isStatic);
                if (field != null && isMemberAccessible(field.getDeclaringClass(), field.getModifiers())) { // GROOVY-8145
                    expression = newStaticPropertyX(staticImportType, variableName);
                }
            }
        }
        return expression;
    }

    private Expression findStaticProperty(Map<String, ImportNode> staticImports, String accessorName) {
        Expression expression = null;
        ImportNode importNode = staticImports.get(accessorName);
        if (importNode != null) { ClassNode importType = importNode.getType();
            expression = findStaticPropertyAccessorByFullName(importType, importNode.getFieldName());
            if (expression == null) { // perhaps the property accessor will be generated
                String propertyName = getPropNameForAccessor(importNode.getFieldName());
                if (hasStaticProperty(importType, propertyName)) {
                    if (inLeftExpression) {
                        expression = newStaticMethodCallX(importType, importNode.getFieldName(),
                                ArgumentListExpression.EMPTY_ARGUMENTS); // <-- will be replaced
                    } else {
                        expression = newStaticPropertyX(importType, propertyName);
                    }
                }
            }
        }
        return expression;
    }

    private static Expression findStaticMethod(ClassNode staticImportType, String methodName, Expression args) {
        if (staticImportType.isPrimaryClassNode() || staticImportType.isResolved()) {
            if (staticImportType.hasPossibleStaticMethod(methodName, args)) {
                return newStaticMethodCallX(staticImportType, methodName, args);
            }
        }
        return null;
    }

    private static StaticMethodCallExpression newStaticMethodCallX(ClassNode type, String name, Expression args) {
        return new StaticMethodCallExpression(type.getPlainNodeReference(), name, args);
    }

    private static PropertyExpression newStaticPropertyX(ClassNode type, String name) {
        return new PropertyExpression(new ClassExpression(type.getPlainNodeReference()), name);
    }

    private boolean isMemberAccessible(ClassNode declaringClass, int modifiers) {
        if (isPublic(modifiers)
                || currentClass.equals(declaringClass)
                || currentClass.getOuterClasses().contains(declaringClass)) {
            return true;
        }
        if (isProtected(modifiers) && currentClass.isDerivedFrom(declaringClass)) {
            return true;
        }
        if (!isPrivate(modifiers) && samePackageName(currentClass, declaringClass)) {
            return true;
        }
        return false;
    }

    private String getAccessorName(String name) {
        return inLeftExpression ? getSetterName(name) : getGetterName(name);
    }

    private static String prefix(String name) {
        return name.startsWith("is") ? "is" : name.substring(0, 3);
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return sourceUnit;
    }
}
