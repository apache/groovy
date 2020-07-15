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
package org.codehaus.groovy.classgen.asm.sc;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.LambdaExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.MethodReferenceExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.tools.WideningCategories;
import org.codehaus.groovy.classgen.asm.BinaryExpressionMultiTypeDispatcher;
import org.codehaus.groovy.classgen.asm.BinaryExpressionWriter;
import org.codehaus.groovy.classgen.asm.BytecodeHelper;
import org.codehaus.groovy.classgen.asm.CompileStack;
import org.codehaus.groovy.classgen.asm.OperandStack;
import org.codehaus.groovy.classgen.asm.VariableSlotLoader;
import org.codehaus.groovy.classgen.asm.WriterController;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.TokenUtil;
import org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys;
import org.codehaus.groovy.transform.sc.StaticCompilationVisitor;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static org.apache.groovy.ast.tools.ExpressionUtils.isThisExpression;
import static org.codehaus.groovy.ast.ClassHelper.CLOSURE_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.char_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.double_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.float_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.long_TYPE;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.binX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callThisX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.castX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.declX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getSetterName;
import static org.codehaus.groovy.ast.tools.GeneralUtils.nullX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
import static org.codehaus.groovy.transform.sc.StaticCompilationVisitor.ARRAYLIST_ADD_METHOD;
import static org.codehaus.groovy.transform.sc.StaticCompilationVisitor.ARRAYLIST_CLASSNODE;
import static org.codehaus.groovy.transform.sc.StaticCompilationVisitor.ARRAYLIST_CONSTRUCTOR;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.DIRECT_METHOD_CALL_TARGET;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.INFERRED_FUNCTIONAL_INTERFACE_TYPE;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.INFERRED_TYPE;

/**
 * A specialized version of the multi type binary expression dispatcher which is aware of static compilation.
 * It is able to generate optimized bytecode for some operations using JVM instructions when available.
 */
public class StaticTypesBinaryExpressionMultiTypeDispatcher extends BinaryExpressionMultiTypeDispatcher implements Opcodes {

    private static final MethodNode CLOSURE_GETTHISOBJECT_METHOD = CLOSURE_TYPE.getMethod("getThisObject", Parameter.EMPTY_ARRAY);

    private final AtomicInteger labelCounter = new AtomicInteger();

    public StaticTypesBinaryExpressionMultiTypeDispatcher(final WriterController wc) {
        super(wc);
    }

    @Override
    protected void writePostOrPrefixMethod(final int op, final String method, final Expression expression, final Expression orig) {
        MethodNode mn = orig.getNodeMetaData(DIRECT_METHOD_CALL_TARGET);
        if (mn != null) {
            controller.getOperandStack().pop();
            MethodCallExpression call = callX(expression, method);
            call.setMethodTarget(mn);
            call.visit(controller.getAcg());
            return;
        }

        ClassNode top = controller.getOperandStack().getTopOperand();
        if (ClassHelper.isPrimitiveType(top) && (ClassHelper.isNumberType(top) || char_TYPE.equals(top))) {
            MethodVisitor mv = controller.getMethodVisitor();
            visitInsnByType(top, mv, ICONST_1, LCONST_1, FCONST_1, DCONST_1);
            if ("next".equals(method)) {
                visitInsnByType(top, mv, IADD, LADD, FADD, DADD);
            } else {
                visitInsnByType(top, mv, ISUB, LSUB, FSUB, DSUB);
            }
            return;
        }

        super.writePostOrPrefixMethod(op, method, expression, orig);
    }

    private static void visitInsnByType(final ClassNode top, final MethodVisitor mv, final int iInsn, final int lInsn, final int fInsn, final int dInsn) {
        if (WideningCategories.isIntCategory(top) || char_TYPE.equals(top)) {
            mv.visitInsn(iInsn);
        } else if (long_TYPE.equals(top)) {
            mv.visitInsn(lInsn);
        } else if (float_TYPE.equals(top)) {
            mv.visitInsn(fInsn);
        } else if (double_TYPE.equals(top)) {
            mv.visitInsn(dInsn);
        }
    }

    @Override
    protected void evaluateBinaryExpressionWithAssignment(final String method, final BinaryExpression expression) {
        Expression leftExpression = expression.getLeftExpression();
        if (leftExpression instanceof PropertyExpression) {
            PropertyExpression pexp = (PropertyExpression) leftExpression;

            BinaryExpression expressionWithoutAssignment = binX(
                    leftExpression,
                    Token.newSymbol(
                            TokenUtil.removeAssignment(expression.getOperation().getType()),
                            expression.getOperation().getStartLine(),
                            expression.getOperation().getStartColumn()
                    ),
                    expression.getRightExpression()
            );
            expressionWithoutAssignment.copyNodeMetaData(expression);
            expressionWithoutAssignment.setSafe(expression.isSafe());
            expressionWithoutAssignment.setSourcePosition(expression);

            if (makeSetProperty(
                    pexp.getObjectExpression(),
                    pexp.getProperty(),
                    expressionWithoutAssignment,
                    pexp.isSafe(),
                    pexp.isSpreadSafe(),
                    pexp.isImplicitThis(),
                    pexp instanceof AttributeExpression)) {
                return;
            }
        }
        super.evaluateBinaryExpressionWithAssignment(method, expression);
    }

    @Override
    public void evaluateEqual(final BinaryExpression expression, final boolean defineVariable) {
        Expression leftExpression = expression.getLeftExpression();
        if (!defineVariable) {
            if (leftExpression instanceof PropertyExpression) {
                PropertyExpression pexp = (PropertyExpression) leftExpression;
                if (makeSetProperty(
                        pexp.getObjectExpression(),
                        pexp.getProperty(),
                        expression.getRightExpression(),
                        pexp.isSafe(),
                        pexp.isSpreadSafe(),
                        pexp.isImplicitThis(),
                        pexp instanceof AttributeExpression)) {
                    return;
                }
            }
        } else {
            Expression rightExpression = expression.getRightExpression();
            if (rightExpression instanceof LambdaExpression || rightExpression instanceof MethodReferenceExpression) {
                rightExpression.putNodeMetaData(INFERRED_FUNCTIONAL_INTERFACE_TYPE, leftExpression.getNodeMetaData(INFERRED_TYPE));
            }
        }
        // GROOVY-5620: spread-safe operator on LHS is not supported
        if (leftExpression instanceof PropertyExpression
                && ((PropertyExpression) leftExpression).isSpreadSafe()
                && StaticTypeCheckingSupport.isAssignment(expression.getOperation().getType())) {
            // rewrite it so that it can be statically compiled
            transformSpreadOnLHS(expression);
            return;
        }
        super.evaluateEqual(expression, defineVariable);
    }

    private void transformSpreadOnLHS(final BinaryExpression expression) {
        PropertyExpression spreadExpression = (PropertyExpression) expression.getLeftExpression();
        Expression receiver = spreadExpression.getObjectExpression();

        int counter = labelCounter.incrementAndGet();
        CompileStack compileStack = controller.getCompileStack();
        OperandStack operandStack = controller.getOperandStack();

        // create an empty arraylist
        VariableExpression result = varX(this.getClass().getSimpleName() + "$spreadresult" + counter, ARRAYLIST_CLASSNODE);
        ConstructorCallExpression newArrayList = ctorX(ARRAYLIST_CLASSNODE);
        newArrayList.setNodeMetaData(DIRECT_METHOD_CALL_TARGET, ARRAYLIST_CONSTRUCTOR);
        Expression decl = declX(result, newArrayList);
        decl.visit(controller.getAcg());
        // if (receiver != null)
        receiver.visit(controller.getAcg());
        Label ifnull = compileStack.createLocalLabel("ifnull_" + counter);
        MethodVisitor mv = controller.getMethodVisitor();
        mv.visitJumpInsn(IFNULL, ifnull);
        operandStack.remove(1); // receiver consumed by if()
        Label nonull = compileStack.createLocalLabel("nonull_" + counter);
        mv.visitLabel(nonull);
        ClassNode componentType = StaticTypeCheckingVisitor.inferLoopElementType(
                controller.getTypeChooser().resolveType(receiver, controller.getClassNode()));
        Parameter iterator = new Parameter(componentType, "for$it$" + counter);
        VariableExpression iteratorAsVar = varX(iterator);
        PropertyExpression pexp = spreadExpression instanceof AttributeExpression
            ? new AttributeExpression(iteratorAsVar, spreadExpression.getProperty(), true)
            : new PropertyExpression(iteratorAsVar, spreadExpression.getProperty(), true);
        pexp.setImplicitThis(spreadExpression.isImplicitThis());
        pexp.setSourcePosition(spreadExpression);
        BinaryExpression assignment = binX(pexp, expression.getOperation(), expression.getRightExpression());
        MethodCallExpression add = callX(result, "add", assignment);
        add.setMethodTarget(ARRAYLIST_ADD_METHOD);
        // for (e in receiver) { result.add(e?.method(arguments) }
        ForStatement stmt = new ForStatement(
                iterator,
                receiver,
                stmt(add)
        );
        stmt.visit(controller.getAcg());
        // else { empty list }
        mv.visitLabel(ifnull);
        // end of if/else
        // return result list
        result.visit(controller.getAcg());
    }

    private boolean makeSetProperty(final Expression receiver, final Expression message, final Expression arguments, final boolean safe, final boolean spreadSafe, final boolean implicitThis, final boolean isAttribute) {
        ClassNode receiverType = controller.getTypeChooser().resolveType(receiver, controller.getClassNode());
        String property = message.getText();
        boolean isThisExpression = isThisExpression(receiver);
        if (isAttribute || (isThisExpression && receiverType.getDeclaredField(property) != null)) {
            ClassNode current = receiverType;
            FieldNode fn = null;
            while (fn == null && current != null) {
                fn = current.getDeclaredField(property);
                if (fn == null) {
                    current = current.getSuperClass();
                }
            }
            if (fn != null && receiverType != current && !fn.isPublic()) {
                // check that direct access is allowed
                if (!fn.isProtected()) {
                    return false;
                }
                if (!Objects.equals(receiverType.getPackageName(), current.getPackageName())) {
                    return false;
                }
                if (!fn.isStatic()) {
                    receiver.visit(controller.getAcg());
                }
                arguments.visit(controller.getAcg());
                OperandStack operandStack = controller.getOperandStack();
                operandStack.doGroovyCast(fn.getOriginType());
                MethodVisitor mv = controller.getMethodVisitor();
                mv.visitFieldInsn(fn.isStatic() ? PUTSTATIC : PUTFIELD,
                        BytecodeHelper.getClassInternalName(fn.getOwner()),
                        property,
                        BytecodeHelper.getTypeDescription(fn.getOriginType()));
                operandStack.remove(fn.isStatic() ? 1 : 2);
                return true;
            }
        }
        if (!isAttribute) {
            String setter = getSetterName(property);
            MethodNode setterMethod = receiverType.getSetterMethod(setter, false);
            ClassNode declaringClass = setterMethod != null ? setterMethod.getDeclaringClass() : null;
            if (isThisExpression && declaringClass != null && declaringClass.equals(controller.getClassNode())) {
                // this.x = ... shouldn't use a setter if in the same class
                setterMethod = null;
            } else if (setterMethod == null) {
                PropertyNode propertyNode = receiverType.getProperty(property);
                if (propertyNode != null) {
                    if (!Modifier.isFinal(propertyNode.getModifiers())) {
                        setterMethod = new MethodNode(
                                setter,
                                ACC_PUBLIC,
                                ClassHelper.VOID_TYPE,
                                new Parameter[]{new Parameter(propertyNode.getOriginType(), "value")},
                                ClassNode.EMPTY_ARRAY,
                                EmptyStatement.INSTANCE
                        );
                        setterMethod.setDeclaringClass(receiverType);
                    }
                }
            }
            if (setterMethod != null) {
                Expression call = StaticPropertyAccessHelper.transformToSetterCall(
                        receiver,
                        setterMethod,
                        arguments,
                        implicitThis,
                        safe,
                        spreadSafe,
                        true, // to be replaced with a proper test whether a return value should be used or not
                        message
                );
                call.visit(controller.getAcg());
                return true;
            }
            if (isThisExpression && !controller.isInGeneratedFunction()) {
                receiverType = controller.getClassNode();
            }
            if (makeSetPrivateFieldWithBridgeMethod(receiver, receiverType, property, arguments, safe, spreadSafe, implicitThis)) {
                return true;
            }
        }
        return false;
    }

    private boolean makeSetPrivateFieldWithBridgeMethod(final Expression receiver, final ClassNode receiverType, final String fieldName, final Expression arguments, final boolean safe, final boolean spreadSafe, final boolean implicitThis) {
        FieldNode field = receiverType.getField(fieldName);
        ClassNode outerClass = receiverType.getOuterClass();
        if (field == null && implicitThis && outerClass != null && !receiverType.isStaticClass()) {
            Expression pexp;
            if (controller.isInGeneratedFunction()) {
                MethodCallExpression mce = callThisX("getThisObject");
                mce.setImplicitThis(true);
                mce.setMethodTarget(CLOSURE_GETTHISOBJECT_METHOD);
                mce.putNodeMetaData(INFERRED_TYPE, controller.getOutermostClass());
                pexp = castX(controller.getOutermostClass(), mce);
            } else {
                pexp = propX(classX(outerClass), "this");
                ((PropertyExpression) pexp).setImplicitThis(true);
            }
            pexp.putNodeMetaData(INFERRED_TYPE, outerClass);
            pexp.setSourcePosition(receiver);
            return makeSetPrivateFieldWithBridgeMethod(pexp, outerClass, fieldName, arguments, safe, spreadSafe, true);
        }
        ClassNode classNode = controller.getClassNode();
        if (field != null && field.isPrivate() && !receiverType.equals(classNode)
                && (StaticInvocationWriter.isPrivateBridgeMethodsCallAllowed(receiverType, classNode)
                    || StaticInvocationWriter.isPrivateBridgeMethodsCallAllowed(classNode,receiverType))) {
            Map<String, MethodNode> mutators = receiverType.redirect().getNodeMetaData(StaticCompilationMetadataKeys.PRIVATE_FIELDS_MUTATORS);
            if (mutators != null) {
                MethodNode methodNode = mutators.get(fieldName);
                if (methodNode != null) {
                    MethodCallExpression call = callX(receiver, methodNode.getName(), args(field.isStatic() ? nullX() : receiver, arguments));
                    call.setImplicitThis(implicitThis);
                    call.setMethodTarget(methodNode);
                    call.setSafe(safe);
                    call.setSpreadSafe(spreadSafe);
                    call.visit(controller.getAcg());
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void assignToArray(final Expression parent, final Expression receiver, final Expression index, final Expression rhsValueLoader, final boolean safe) {
        ClassNode arrayType = controller.getTypeChooser().resolveType(receiver, controller.getClassNode());
        int operationType = getOperandType(arrayType.getComponentType());
        BinaryExpressionWriter bew = binExpWriter[operationType];

        if (bew.arraySet(true) && arrayType.isArray() && !safe) {
            super.assignToArray(parent, receiver, index, rhsValueLoader, safe);
        } else {
            /*
             * This code path is needed because ACG creates array access expressions
             */
            StaticTypeCheckingVisitor visitor = new StaticCompilationVisitor(controller.getSourceUnit(), controller.getClassNode());
            // GROOVY-6061
            if (rhsValueLoader instanceof VariableSlotLoader && parent instanceof BinaryExpression) {
                rhsValueLoader.putNodeMetaData(INFERRED_TYPE, controller.getTypeChooser().resolveType(parent, controller.getClassNode()));
            }
            // let's replace this assignment to a subscript operator with a method call
            // e.g. x[5] = 10
            // -> (x, [], 5), =, 10
            // -> methodCall(x, "putAt", [5, 10])
            MethodCallExpression call = callX(receiver, "putAt", args(index, rhsValueLoader));
            call.setSafe(safe);
            call.setSourcePosition(parent);
            visitor.visitMethodCallExpression(call);

            OperandStack operandStack = controller.getOperandStack();
            int height = operandStack.getStackLength();
            call.visit(controller.getAcg());
            operandStack.pop();
            operandStack.remove(operandStack.getStackLength() - height);

            // return value of assignment
            rhsValueLoader.visit(controller.getAcg());
        }
    }
}
