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
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;

import java.util.Collection;

import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.binX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.castX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.isInstanceOfX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX;
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

    public TraitReceiverTransformer(final VariableExpression thisObject, final SourceUnit unit, final ClassNode traitClass,
                                    final ClassNode traitHelperClass, final ClassNode fieldHelper, final Collection<String> knownFields) {
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
        } else if (exp instanceof MethodCallExpression) {
            MethodCallExpression mce = (MethodCallExpression) exp;
            String obj = mce.getObjectExpression().getText();
            if ("super".equals(obj)) {
                return transformSuperMethodCall(mce); // super.m(x) --> $self.Ttrait$super$m(x)
            } else if ("this".equals(obj)) {
                return transformMethodCallOnThis(mce); // this.m(x) --> $self.m(x) or this.m($self, x)
            }
        } else if (exp instanceof FieldExpression) {
            FieldNode fn = ((FieldExpression) exp).getField();
            return transformFieldReference(exp, fn, fn.isStatic());
        } else if (exp instanceof VariableExpression) {
            VariableExpression vexp = (VariableExpression) exp;
            Variable accessedVariable = vexp.getAccessedVariable();
            if (accessedVariable instanceof FieldNode || accessedVariable instanceof PropertyNode) {
                if (knownFields.contains(vexp.getName())) {
                    boolean isStatic = accessedVariable.isStatic();
                    return transformFieldReference(exp, accessedVariable instanceof FieldNode
                            ? (FieldNode) accessedVariable : ((PropertyNode) accessedVariable).getField(), isStatic);
                } else {
                    PropertyExpression propertyExpression = propX(varX(weaved), vexp.getName());
                    propertyExpression.getProperty().setSourcePosition(exp);
                    return propertyExpression;
                }
            } else if (accessedVariable instanceof DynamicVariable && !inClosure) { // GROOVY-9386
                PropertyExpression propertyExpression = propX(varX(weaved), vexp.getName());
                propertyExpression.getProperty().setSourcePosition(exp);
                return propertyExpression;
            }
            if (vexp.isThisExpression()) {
                VariableExpression variableExpression = varX(weaved);
                variableExpression.setSourcePosition(exp);
                return variableExpression;
            }
            if (vexp.isSuperExpression()) {
                throwSuperError(vexp);
            }
        } else if (exp instanceof PropertyExpression) {
            PropertyExpression pexp = (PropertyExpression) exp;
            String obj = pexp.getObjectExpression().getText();
            if (pexp.isImplicitThis() || "this".equals(obj)) {
                String propName = pexp.getPropertyAsString();
                if (knownFields.contains(propName)) {
                    FieldNode fn = new FieldNode(propName, 0, ClassHelper.OBJECT_TYPE, weavedType, null);
                    return transformFieldReference(exp, fn, false);
                }
            }
        } else if (exp instanceof ClosureExpression) {
            MethodCallExpression mce = callX(exp, "rehydrate", args(
                    varX(weaved),
                    varX(weaved),
                    varX(weaved)
            ));
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

        // TODO: unary expressions (field++, field+=, ...)
        return super.transform(exp);
    }

    private Expression transformBinaryExpression(final BinaryExpression exp, final ClassNode weavedType) {
        Expression leftExpression = exp.getLeftExpression();
        Expression rightExpression = exp.getRightExpression();
        Token operation = exp.getOperation();
        if (operation.getType() == Types.ASSIGN) {
            String leftFieldName = null;
            if (leftExpression instanceof VariableExpression && ((VariableExpression) leftExpression).getAccessedVariable() instanceof FieldNode) {
                leftFieldName = ((VariableExpression) leftExpression).getAccessedVariable().getName();
            } else if (leftExpression instanceof FieldExpression) {
                leftFieldName = ((FieldExpression) leftExpression).getFieldName();
            } else if (leftExpression instanceof PropertyExpression
                    && (((PropertyExpression) leftExpression).isImplicitThis() || "this".equals(((PropertyExpression) leftExpression).getObjectExpression().getText()))) {
                leftFieldName = ((PropertyExpression) leftExpression).getPropertyAsString();
            }
            if (leftFieldName != null) {
                FieldNode staticField = tryGetFieldNode(weavedType, leftFieldName);
                if (fieldHelper == null || staticField == null && !fieldHelper.hasPossibleMethod(Traits.helperSetterName(new FieldNode(leftFieldName, 0, ClassHelper.OBJECT_TYPE, weavedType, null)), rightExpression)) {
                    return binX(propX(varX(weaved), leftFieldName), operation, transform(rightExpression)); // GROOVY-7342, GROOVY-7456, GROOVY-9739, GROOVY-10143, et al.
                }
                FieldNode fn = weavedType.getDeclaredField(leftFieldName);
                if (fn == null) {
                    fn = new FieldNode(leftFieldName, 0, ClassHelper.OBJECT_TYPE, weavedType, null);
                }
                Expression receiver = createFieldHelperReceiver();
                boolean isStatic = staticField != null && staticField.isStatic();
                if (fn.isStatic()) { // DO NOT USE isStatic variable here!
                    receiver = propX(receiver, "class");
                }
                String method = Traits.helperSetterName(fn);
                MethodCallExpression mce = callX(
                        receiver,
                        method,
                        super.transform(rightExpression)
                );
                mce.setImplicitThis(false);
                mce.setSourcePosition(exp);
                markDynamicCall(mce, staticField, isStatic);
                return mce;
            }
        }

        Expression leftTransform = transform(leftExpression);
        Expression rightTransform = transform(rightExpression);
        Expression ret = exp instanceof DeclarationExpression ?
                new DeclarationExpression(leftTransform, operation, rightTransform) : binX(leftTransform, operation, rightTransform);
        ret.setSourcePosition(exp);
        ret.copyNodeMetaData(exp);
        return ret;
    }

    private Expression transformFieldReference(final Expression exp, final FieldNode fn, final boolean isStatic) {
        Expression receiver = createFieldHelperReceiver();
        if (isStatic) {
            receiver = asClass(receiver);
        }

        MethodCallExpression mce = callX(receiver, Traits.helperGetterName(fn));
        mce.setImplicitThis(false);
        mce.setSourcePosition(exp);
        markDynamicCall(mce, fn, isStatic);
        return mce;
    }

    private static void markDynamicCall(final MethodCallExpression mce, final FieldNode fn, final boolean isStatic) {
        if (isStatic) {
            mce.putNodeMetaData(TraitASTTransformation.DO_DYNAMIC, fn.getOriginType());
        }
    }

    private static FieldNode tryGetFieldNode(final ClassNode weavedType, final String fieldName) {
        FieldNode fn = weavedType.getDeclaredField(fieldName);
        if (fn == null && ClassHelper.isClassType(weavedType)) {
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
        newCall.setSourcePosition(call);
        newCall.setSafe(call.isSafe());
        newCall.setSpreadSafe(call.isSpreadSafe());
        newCall.setImplicitThis(false);
        return newCall;
    }

    private Expression transformMethodCallOnThis(final MethodCallExpression call) {
        Expression method    = call.getMethod();
        Expression arguments = call.getArguments();
        Expression thisExpr  = call.getObjectExpression();

        if (method instanceof ConstantExpression) {
            // GROOVY-7213, GROOVY-7214, GROOVY-8282, GROOVY-8859, GROOVY-10106, GROOVY-10312
            MethodNode methodNode = findConcreteMethod(traitClass, call.getMethodAsString());
            if (methodNode != null) {
                // this.m(x) --> (this or T$Trait$Helper).m($self or $static$self or (Class)$self.getClass(), x)
                Expression selfClassOrObject = methodNode.isStatic() && !ClassHelper.isClassType(weaved.getOriginType()) ? castX(ClassHelper.CLASS_Type.getPlainNodeReference(), callX(weaved, "getClass")) : weaved;
                MethodCallExpression newCall = callX(!inClosure ? thisExpr : classX(traitHelperClass), method, createArgumentList(selfClassOrObject, arguments));
                newCall.setGenericsTypes(call.getGenericsTypes());
                newCall.setSpreadSafe(call.isSpreadSafe());
                newCall.setSourcePosition(call);
                return newCall;
            }
        }

        // this.m(x) --> ($self or $static$self).m(x)
        MethodCallExpression newCall = callX(inClosure ? thisExpr : weaved, method, transform(arguments));
        newCall.setGenericsTypes(call.getGenericsTypes()); // GROOVY-11302: this.<T>m(x)
        newCall.setImplicitThis(inClosure ? call.isImplicitThis() : false);
        newCall.setSafe(inClosure ? call.isSafe() : false);
        newCall.setSpreadSafe(call.isSpreadSafe());
        newCall.setSourcePosition(call);
        return newCall;
    }

    private static MethodNode findConcreteMethod(final ClassNode traitClass, final String methodName) {
        for (MethodNode methodNode : traitClass.getDeclaredMethods(methodName)) {
            if (methodNode.isPrivate() || methodNode.isStatic()) {
                return methodNode;
            }
        }

        // GROOVY-8272, GROOVY-10312: public static method from super trait
        var traits = Traits.findTraits(traitClass); traits.remove(traitClass);

        for (ClassNode superTrait : traits) {
            for (MethodNode methodNode : Traits.findHelper(superTrait).getDeclaredMethods(methodName)) {
                if (methodNode.isPublic() && methodNode.isStatic()
                        // exclude public method with body as it's included in trait interface
                        && ClassHelper.isClassType(methodNode.getParameters()[0].getType())) {
                    return methodNode;
                }
            }
        }

        return null;
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

    private Expression createFieldHelperReceiver() {
        return ClassHelper.isClassType(weaved.getOriginType()) ? weaved : castX(fieldHelper, weaved);
    }

    private Expression asClass(final Expression e) {
        ClassNode rawClass = ClassHelper.CLASS_Type.getPlainNodeReference();
        return ternaryX(isInstanceOfX(e, rawClass), e, callX(e, "getClass"));
    }
}
