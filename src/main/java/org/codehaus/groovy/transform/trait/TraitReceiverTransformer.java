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
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TernaryExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;

import java.util.Collection;
import java.util.List;

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
            if (accessedVariable instanceof FieldNode) {
                FieldNode fn = (FieldNode) accessedVariable;
                Expression receiver = createFieldHelperReceiver();
                MethodCallExpression mce;
                boolean isStatic = fn.isStatic();
                if (isStatic) {
                    receiver = createStaticReceiver(receiver);
                }
                mce = new MethodCallExpression(
                        receiver,
                        Traits.helperGetterName(fn),
                        ArgumentListExpression.EMPTY_ARGUMENTS
                );
                mce.setSourcePosition(exp);
                mce.setImplicitThis(false);
                markDynamicCall(mce, fn, isStatic);
                return mce;
            } else if (accessedVariable instanceof PropertyNode) {
                String propName = accessedVariable.getName();
                if (knownFields.contains(propName)) {
                    return createFieldHelperCall(exp, weavedType, propName);
                } else {
                    PropertyExpression propertyExpression = new PropertyExpression(
                            new VariableExpression(weaved), accessedVariable.getName());
                    propertyExpression.getProperty().setSourcePosition(exp);
                    return propertyExpression;
                }
            } else if (accessedVariable instanceof DynamicVariable) {
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
        mce.setSourcePosition(exp instanceof PropertyExpression ? ((PropertyExpression) exp).getProperty() : exp);
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

    private TernaryExpression createStaticReceiver(final Expression receiver) {
        return new TernaryExpression(
                new BooleanExpression(new BinaryExpression(
                        receiver,
                        Token.newSymbol(Types.KEYWORD_INSTANCEOF, -1, -1),
                        new ClassExpression(ClassHelper.CLASS_Type)
                )),
                receiver,
                new MethodCallExpression(createFieldHelperReceiver(), "getClass", ArgumentListExpression.EMPTY_ARGUMENTS)
        );
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
        MethodCallExpression transformed = new MethodCallExpression(
                weaved,
                Traits.getSuperTraitMethodName(traitClass, method),
                superCallArgs
        );
        transformed.setSourcePosition(call);
        transformed.setSafe(call.isSafe());
        transformed.setSpreadSafe(call.isSpreadSafe());
        transformed.setImplicitThis(false);
        return transformed;
    }

    private Expression transformMethodCallOnThis(final MethodCallExpression call) {
        Expression method = call.getMethod();
        Expression arguments = call.getArguments();
        if (method instanceof ConstantExpression) {
            String methodName = method.getText();
            List<MethodNode> methods = traitClass.getMethods(methodName);
            for (MethodNode methodNode : methods) {
                if (methodName.equals(methodNode.getName()) && methodNode.isPrivate()) {
                    if (inClosure) {
                        return transformPrivateMethodCallOnThisInClosure(call, arguments, methodName);
                    }
                    return transformPrivateMethodCallOnThis(call, arguments, methodName);
                }
            }
        }

        if (inClosure) {
            return transformMethodCallOnThisInClosure(call);
        }

        return transformMethodCallOnThisFallBack(call, method, arguments);

    }

    private Expression transformMethodCallOnThisFallBack(final MethodCallExpression call,
                                                         final Expression method, final Expression arguments) {
        MethodCallExpression transformed = new MethodCallExpression(
                weaved,
                method,
                transform(arguments)
        );
        transformed.setSourcePosition(call);
        transformed.setSafe(call.isSafe());
        transformed.setSpreadSafe(call.isSpreadSafe());
        transformed.setImplicitThis(false);
        return transformed;
    }

    private Expression transformMethodCallOnThisInClosure(final MethodCallExpression call) {
        MethodCallExpression transformed = new MethodCallExpression(
                (Expression) call.getReceiver(),
                call.getMethod(),
                transform(call.getArguments())
        );
        transformed.setSourcePosition(call);
        transformed.setSafe(call.isSafe());
        transformed.setSpreadSafe(call.isSpreadSafe());
        transformed.setImplicitThis(call.isImplicitThis());
        return transformed;
    }

    private Expression transformPrivateMethodCallOnThis(final MethodCallExpression call,
                                                        final Expression arguments, final String methodName) {
        ArgumentListExpression newArgs = createArgumentList(arguments);
        MethodCallExpression transformed = new MethodCallExpression(
                new VariableExpression("this"),
                methodName,
                newArgs
        );
        transformed.setSourcePosition(call);
        transformed.setSafe(call.isSafe());
        transformed.setSpreadSafe(call.isSpreadSafe());
        transformed.setImplicitThis(true);
        return transformed;
    }

    private Expression transformPrivateMethodCallOnThisInClosure(final MethodCallExpression call,
                                                                 final Expression arguments, final String methodName) {
        ArgumentListExpression newArgs = createArgumentList(arguments);
        MethodCallExpression transformed = new MethodCallExpression(
                new ClassExpression(traitHelperClass),
                methodName,
                newArgs
        );
        transformed.setSourcePosition(call);
        transformed.setSafe(call.isSafe());
        transformed.setSpreadSafe(call.isSpreadSafe());
        transformed.setImplicitThis(true);
        return transformed;
    }

    private Expression createFieldHelperReceiver() {
        return ClassHelper.CLASS_Type.equals(weaved.getOriginType()) ? weaved : new CastExpression(fieldHelper, weaved);
    }

    private ArgumentListExpression createArgumentList(final Expression origCallArgs) {
        ArgumentListExpression newArgs = new ArgumentListExpression();
        newArgs.addExpression(new VariableExpression(weaved));
        if (origCallArgs instanceof TupleExpression) {
            List<Expression> expressions = ((TupleExpression) origCallArgs).getExpressions();
            for (Expression expression : expressions) {
                newArgs.addExpression(transform(expression));
            }
        } else {
            newArgs.addExpression(origCallArgs);
        }
        return newArgs;
    }
}
