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
package org.codehaus.groovy.transform.trait;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.syntax.Token;

import java.lang.reflect.Modifier;
import java.util.Collection;

import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.castX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.isInstanceOfX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ternaryX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

/**
 * This expression transformer is used internally by the {@link org.codehaus.groovy.transform.trait.TraitASTTransformation
 * trait} AST transformation to change the receiver of a message on "this" into a static method call on the trait helper
 * class.
 * <p>
 * In a nutshell, code like the following method definition in a trait: <code>void foo() { this.bar() }</code> is
 * transformed into: <code>void foo() { TraitHelper$bar(this) }</code>
 *
 * @since 2.3.0
 */
class TraitReceiverTransformer extends ClassCodeExpressionTransformer {

    private final VariableExpression weaved;
    private final SourceUnit unit;
    private final ClassNode traitClass;
    private final ClassNode traitHelperClass;
    private final ClassNode fieldHelper;
    private final Collection<String> knownFields;

    private boolean inClosure;

    public TraitReceiverTransformer(VariableExpression thisObject, SourceUnit unit, final ClassNode traitClass,
                                    final ClassNode traitHelperClass, ClassNode fieldHelper, Collection<String> knownFields) {
        this.weaved = thisObject;
        this.unit = unit;
        this.traitClass = traitClass;
        this.traitHelperClass = traitHelperClass;
        this.fieldHelper = fieldHelper;
        this.knownFields = knownFields;
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return unit;
    }

    @Override
    public Expression transform(final Expression exp) {
        ClassNode weavedType = weaved.getOriginType();
        if (exp instanceof BinaryExpression) {
            return transformBinaryExpression((BinaryExpression) exp, weavedType);
        } else if (exp instanceof StaticMethodCallExpression) {
            StaticMethodCallExpression call = (StaticMethodCallExpression) exp;
            ClassNode ownerType = call.getOwnerType();
            if (traitClass.equals(ownerType)) {
                MethodCallExpression result = new MethodCallExpression(
                        new VariableExpression(weaved),
                        call.getMethod(),
                        transform(call.getArguments())
                );
                result.setSafe(false);
                result.setImplicitThis(false);
                result.setSpreadSafe(false);
                result.setSourcePosition(call);
                return result;
            }
        } else if (exp instanceof MethodCallExpression) {
            MethodCallExpression call = (MethodCallExpression) exp;
            Expression obj = call.getObjectExpression();
            if (call.isImplicitThis() || "this".equals(obj.getText())) {
                return transformMethodCallOnThis(call);
            } else if ("super".equals(obj.getText())) {
                return transformSuperMethodCall(call);
            }
        } else if (exp instanceof FieldExpression) {
            return transformFieldExpression((FieldExpression) exp);
        } else if (exp instanceof VariableExpression) {
            VariableExpression vexp = (VariableExpression) exp;
            Variable accessedVariable = vexp.getAccessedVariable();
            if (accessedVariable instanceof FieldNode || accessedVariable instanceof PropertyNode) {
                if (knownFields.contains(accessedVariable.getName())) {
                    boolean isStatic = Modifier.isStatic(accessedVariable.getModifiers());
                    Expression receiver = createFieldHelperReceiver();
                    if (isStatic) {
                        receiver = asClass(receiver);
                    }
                    FieldNode fn = accessedVariable instanceof FieldNode ? (FieldNode) accessedVariable : ((PropertyNode) accessedVariable).getField();
                    MethodCallExpression mce = new MethodCallExpression(receiver, Traits.helperGetterName(fn), ArgumentListExpression.EMPTY_ARGUMENTS);
                    mce.setImplicitThis(false);
                    mce.setSourcePosition(exp);
                    markDynamicCall(mce, fn, isStatic);
                    return mce;
                } else {
                    PropertyExpression propertyExpression = new PropertyExpression(
                            new VariableExpression(weaved), accessedVariable.getName());
                    propertyExpression.getProperty().setSourcePosition(exp);
                    return propertyExpression;
                }
            } else if (accessedVariable instanceof DynamicVariable && !inClosure) { // GROOVY-9386
                PropertyExpression propertyExpression = new PropertyExpression(
                        new VariableExpression(weaved), accessedVariable.getName());
                propertyExpression.getProperty().setSourcePosition(exp);
                return propertyExpression;
            }
            if (vexp.isThisExpression()) {
                VariableExpression res = new VariableExpression(weaved);
                res.setSourcePosition(exp);
                return res;
            }
            if (vexp.isSuperExpression()) {
                throwSuperError(vexp);
            }
        } else if (exp instanceof PropertyExpression) {
            PropertyExpression pexp = (PropertyExpression) exp;
            Expression object = pexp.getObjectExpression();
            if (pexp.isImplicitThis() || "this".equals(object.getText())) {
                String propName = pexp.getPropertyAsString();
                if (knownFields.contains(propName)) {
                    return createFieldHelperCall(exp, weavedType, propName);
                }
            }
        } else if (exp instanceof ClosureExpression) {
            MethodCallExpression mce = new MethodCallExpression(
                    exp,
                    "rehydrate",
                    new ArgumentListExpression(
                            new VariableExpression(weaved),
                            new VariableExpression(weaved),
                            new VariableExpression(weaved)
                    )
            );
            mce.setImplicitThis(false);
            mce.setSourcePosition(exp);
            boolean oldInClosure = inClosure;
            inClosure = true;
            ((ClosureExpression) exp).getCode().visit(this);
            inClosure = oldInClosure;
            // The rewrite we do is causing some troubles with type checking, which will
            // not be able to perform closure parameter type inference
            // so we store the replacement, which will be done *after* type checking.
            exp.putNodeMetaData(TraitASTTransformation.POST_TYPECHECKING_REPLACEMENT, mce);
            return exp;
        }

        // todo: unary expressions (field++, field+=, ...)
        return super.transform(exp);
    }

    private Expression createFieldHelperCall(Expression exp, ClassNode weavedType, String propName) {
        String method = Traits.helperGetterName(new FieldNode(propName, 0, ClassHelper.OBJECT_TYPE, weavedType, null));
        MethodCallExpression mce = new MethodCallExpression(
                createFieldHelperReceiver(),
                method,
                ArgumentListExpression.EMPTY_ARGUMENTS
        );
        mce.setSourcePosition(exp);
        mce.setImplicitThis(false);
        return mce;
    }

    private Expression transformFieldExpression(final FieldExpression exp) {
        FieldNode field = exp.getField();
        MethodCallExpression mce = new MethodCallExpression(
                createFieldHelperReceiver(),
                Traits.helperGetterName(field),
                ArgumentListExpression.EMPTY_ARGUMENTS
        );
        mce.setSourcePosition(exp);
        mce.setImplicitThis(false);
        markDynamicCall(mce, field, field.isStatic());
        return mce;
    }

    private Expression transformBinaryExpression(final BinaryExpression exp, final ClassNode weavedType) {
        Expression leftExpression = exp.getLeftExpression();
        Expression rightExpression = exp.getRightExpression();
        Token operation = exp.getOperation();
        if (operation.getText().equals("=")) {
            String leftFieldName = null;
            // it's an assignment
            if (leftExpression instanceof VariableExpression && ((VariableExpression) leftExpression).getAccessedVariable() instanceof FieldNode) {
                leftFieldName = ((VariableExpression) leftExpression).getAccessedVariable().getName();
            } else if (leftExpression instanceof FieldExpression) {
                leftFieldName = ((FieldExpression) leftExpression).getFieldName();
            } else if (leftExpression instanceof PropertyExpression
                    && (((PropertyExpression) leftExpression).isImplicitThis() || "this".equals(((PropertyExpression) leftExpression).getObjectExpression().getText()))) {
                leftFieldName = ((PropertyExpression) leftExpression).getPropertyAsString();
                FieldNode fn = tryGetFieldNode(weavedType, leftFieldName);
                if (fieldHelper == null || fn == null && !fieldHelper.hasPossibleMethod(Traits.helperSetterName(new FieldNode(leftFieldName, 0, ClassHelper.OBJECT_TYPE, weavedType, null)), rightExpression)) {
                    return createAssignmentToField(rightExpression, operation, leftFieldName);
                }
            }
            if (leftFieldName != null) {
                FieldNode fn = weavedType.getDeclaredField(leftFieldName);
                FieldNode staticField = tryGetFieldNode(weavedType, leftFieldName);
                if (fn == null) {
                    fn = new FieldNode(leftFieldName, 0, ClassHelper.OBJECT_TYPE, weavedType, null);
                }
                Expression receiver = createFieldHelperReceiver();
                boolean isStatic = staticField != null && staticField.isStatic();
                if (fn.isStatic()) { // DO NOT USE isStatic variable here!
                    receiver = new PropertyExpression(receiver, "class");
                }
                String method = Traits.helperSetterName(fn);
                MethodCallExpression mce = new MethodCallExpression(
                        receiver,
                        method,
                        new ArgumentListExpression(super.transform(rightExpression))
                );
                mce.setSourcePosition(exp);
                mce.setImplicitThis(false);
                markDynamicCall(mce, staticField, isStatic);
                return mce;
            }
        }
        Expression leftTransform = transform(leftExpression);
        Expression rightTransform = transform(rightExpression);
        Expression ret =
                exp instanceof DeclarationExpression ? new DeclarationExpression(
                        leftTransform, operation, rightTransform
                ) :
                        new BinaryExpression(leftTransform, operation, rightTransform);
        ret.setSourcePosition(exp);
        ret.copyNodeMetaData(exp);
        return ret;
    }

    private static void markDynamicCall(final MethodCallExpression mce, final FieldNode fn, final boolean isStatic) {
        if (isStatic) {
            mce.putNodeMetaData(TraitASTTransformation.DO_DYNAMIC, fn.getOriginType());
        }
    }

    private BinaryExpression createAssignmentToField(final Expression rightExpression,
                                                     final Token operation, final String fieldName) {
        return new BinaryExpression(
                new PropertyExpression(
                        new VariableExpression(weaved),
                        fieldName
                ),
                operation,
                transform(rightExpression));
    }

    private static FieldNode tryGetFieldNode(final ClassNode weavedType, final String fieldName) {
        FieldNode fn = weavedType.getDeclaredField(fieldName);
        if (fn == null && ClassHelper.CLASS_Type.equals(weavedType)) {
            GenericsType[] genericsTypes = weavedType.getGenericsTypes();
            if (genericsTypes != null && genericsTypes.length == 1) {
                // for static properties
                fn = genericsTypes[0].getType().getDeclaredField(fieldName);
            }
        }
        return fn;
    }

    private void throwSuperError(final ASTNode node) {
        unit.addError(new SyntaxException("Call to super is not allowed in a trait", node.getLineNumber(), node.getColumnNumber()));
    }

    private Expression transformSuperMethodCall(final MethodCallExpression call) {
        String method = call.getMethodAsString();
        if (method == null) {
            throwSuperError(call);
        }

        Expression arguments = transform(call.getArguments());
        ArgumentListExpression superCallArgs = new ArgumentListExpression();
        if (arguments instanceof ArgumentListExpression) {
            ArgumentListExpression list = (ArgumentListExpression) arguments;
            for (Expression expression : list) {
                superCallArgs.addExpression(expression);
            }
        } else {
            superCallArgs.addExpression(arguments);
        }
        MethodCallExpression newCall = callX(
                weaved,
                Traits.getSuperTraitMethodName(traitClass, method),
                superCallArgs
        );
        newCall.setImplicitThis(false);
        newCall.setSafe(call.isSafe());
        newCall.setSourcePosition(call);
        newCall.setSpreadSafe(call.isSpreadSafe());
        return newCall;
    }

    private Expression transformMethodCallOnThis(final MethodCallExpression call) {
        Expression method = call.getMethod();
        Expression arguments = call.getArguments();

        if (method instanceof ConstantExpression) {
            String methodName = call.getMethodAsString();
            for (MethodNode methodNode : traitClass.getMethods(methodName)) {
                if (methodName.equals(methodNode.getName()) && (methodNode.isStatic() || methodNode.isPrivate())) {
                    MethodCallExpression newCall;
                    if (!inClosure && methodNode.isStatic()) { // GROOVY-10312: $self or $static$self.staticMethod(...)
                        newCall = callX(varX(weaved), methodName, transform(arguments));
                        newCall.setImplicitThis(false);
                        newCall.setSafe(false);
                    } else {
                        ArgumentListExpression newArgs = createArgumentList(methodNode.isStatic() ? asClass(varX("this")) : weaved, arguments);
                        newCall = callX(inClosure ? classX(traitHelperClass) : call.getObjectExpression(), methodName, newArgs);
                        newCall.setImplicitThis(true);
                        newCall.setSafe(call.isSafe());
                    }
                    newCall.setSpreadSafe(call.isSpreadSafe());
                    newCall.setSourcePosition(call);
                    return newCall;
                }
            }
        }

        MethodCallExpression newCall = callX(inClosure ? call.getObjectExpression() : weaved, method, transform(arguments));
        newCall.setImplicitThis(inClosure ? call.isImplicitThis() : false);
        newCall.setSafe(call.isSafe());
        newCall.setSourcePosition(call);
        newCall.setSpreadSafe(call.isSpreadSafe());
        return newCall;
    }

    private ArgumentListExpression createArgumentList(final Expression self, final Expression arguments) {
        ArgumentListExpression newArgs = new ArgumentListExpression();
        newArgs.addExpression(self);
        if (arguments instanceof TupleExpression) {
            for (Expression argument : (TupleExpression) arguments) {
                newArgs.addExpression(transform(argument));
            }
        } else {
            newArgs.addExpression(transform(arguments));
        }
        return newArgs;
    }

    private static Expression asClass(final Expression e) {
        ClassNode rawClass = ClassHelper.CLASS_Type.getPlainNodeReference();
        return ternaryX(isInstanceOfX(e, rawClass), e, callX(e, "getClass"));
    }

    private Expression createFieldHelperReceiver() {
        return weaved.getOriginType().equals(ClassHelper.CLASS_Type) ? weaved : castX(fieldHelper, weaved);
    }
}
