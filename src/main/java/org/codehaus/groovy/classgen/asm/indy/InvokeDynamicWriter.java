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
package org.codehaus.groovy.classgen.asm.indy;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.EmptyExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.tools.WideningCategories;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.classgen.asm.CompileStack;
import org.codehaus.groovy.classgen.asm.InvocationWriter;
import org.codehaus.groovy.classgen.asm.MethodCallerMultiAdapter;
import org.codehaus.groovy.classgen.asm.OperandStack;
import org.codehaus.groovy.classgen.asm.WriterController;
import org.codehaus.groovy.runtime.wrappers.Wrapper;
import org.codehaus.groovy.vmplugin.v8.IndyInterface;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import static org.apache.groovy.ast.tools.ExpressionUtils.isSuperExpression;
import static org.apache.groovy.ast.tools.ExpressionUtils.isThisExpression;
import static org.codehaus.groovy.ast.ClassHelper.OBJECT_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.boolean_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.getWrapper;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveBoolean;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveType;
import static org.codehaus.groovy.ast.ClassHelper.isWrapperBoolean;
import static org.codehaus.groovy.ast.tools.GeneralUtils.bytecodeX;
import static org.codehaus.groovy.classgen.asm.BytecodeHelper.doCast;
import static org.codehaus.groovy.classgen.asm.BytecodeHelper.getTypeDescription;
import static org.codehaus.groovy.vmplugin.v8.IndyInterface.CallType.CAST;
import static org.codehaus.groovy.vmplugin.v8.IndyInterface.CallType.GET;
import static org.codehaus.groovy.vmplugin.v8.IndyInterface.CallType.INIT;
import static org.codehaus.groovy.vmplugin.v8.IndyInterface.CallType.INTERFACE;
import static org.codehaus.groovy.vmplugin.v8.IndyInterface.CallType.METHOD;
import static org.codehaus.groovy.vmplugin.v8.IndyInterface.GROOVY_OBJECT;
import static org.codehaus.groovy.vmplugin.v8.IndyInterface.IMPLICIT_THIS;
import static org.codehaus.groovy.vmplugin.v8.IndyInterface.SAFE_NAVIGATION;
import static org.codehaus.groovy.vmplugin.v8.IndyInterface.SPREAD_CALL;
import static org.codehaus.groovy.vmplugin.v8.IndyInterface.THIS_CALL;
import static org.objectweb.asm.Opcodes.H_INVOKESTATIC;
import static org.objectweb.asm.Opcodes.IFNULL;

/**
 * This Writer is used to generate the call invocation byte codes
 * for usage by invokedynamic.
 */
public class InvokeDynamicWriter extends InvocationWriter {

    private static final String BSM_DESCRIPTOR = MethodType.methodType(
            CallSite.class, Lookup.class, String.class, MethodType.class, String.class, int.class
    ).toMethodDescriptorString();

    private static final Handle BSM = new Handle(H_INVOKESTATIC,
        IndyInterface.class.getName().replace('.', '/'), "bootstrap", BSM_DESCRIPTOR, false
    );

    private static final Object[] CAST_ARGS = {"()", 0};

    //--------------------------------------------------------------------------

    public InvokeDynamicWriter(final WriterController controller) {
        super(controller);
    }

    @Override
    protected boolean makeCachedCall(final Expression origin, final ClassExpression sender,
            final Expression receiver, final Expression message, final Expression arguments,
            final MethodCallerMultiAdapter adapter, final boolean safe, final boolean spreadSafe,
            final boolean implicitThis, final boolean containsSpreadExpression) {
        // fixed number of arguments && name is a real String and no GString
        if (!spreadSafe && (adapter == null || adapter == invokeMethod || adapter == invokeMethodOnCurrent || adapter == invokeStaticMethod)) {
            String methodName = getMethodName(message);
            if (methodName != null) {
                makeIndyCall(adapter, receiver, implicitThis, safe, methodName, arguments);
                return true;
            }
        }
        return false;
    }

    private String prepareIndyCall(final Expression receiver, final boolean implicitThis) {
        CompileStack compileStack = controller.getCompileStack();
        OperandStack operandStack = controller.getOperandStack();

        compileStack.pushLHS(false);

        // load normal receiver as first argument
        compileStack.pushImplicitThis(implicitThis);
        // GROOVY-7785: use iterative approach to avoid stack overflow for chained method calls
        visitReceiverOfMethodCall(receiver);
        compileStack.popImplicitThis();

        return "(" + getTypeDescription(operandStack.getTopOperand());
    }

    /**
     * Visits receiver expression, using iterative approach for method call chains.
     * GROOVY-7785: Flattens deep recursive AST structures to avoid stack overflow.
     */
    private void visitReceiverOfMethodCall(final Expression receiver) {
        // Collect chain of simple method calls that can use indy optimization
        Deque<MethodCallExpression> chain = new ArrayDeque<>();
        Expression current = receiver;
        for (; current instanceof MethodCallExpression mce
                && !mce.isSpreadSafe() && !mce.isImplicitThis()
                && !isSuperExpression(mce.getObjectExpression())
                && !isThisExpression(mce.getObjectExpression()); current = mce.getObjectExpression()) {
            String name = getMethodName(mce.getMethod());
            if (name == null || "call".equals(name)) break; // dynamic name or functional interface call
            chain.push(mce);
        }

        AsmClassGenerator acg = controller.getAcg();
        current.visit(acg);

        while (!chain.isEmpty()) {
            MethodCallExpression call = chain.pop();
            acg.onLineNumber(call, "visitMethodCallExpression: \"" + call.getMethod() + "\":");
            finishIndyCallForChain(call);
            controller.getAssertionWriter().record(call.getMethod());
        }
    }

    /**
     * Completes an indy call for a chained method with receiver already on stack.
     */
    private void finishIndyCallForChain(final MethodCallExpression call) {
        OperandStack operandStack = controller.getOperandStack();
        AsmClassGenerator acg = controller.getAcg();
        Expression arguments = call.getArguments();
        boolean safe = call.isSafe();

        StringBuilder sig = new StringBuilder("(").append(getTypeDescription(operandStack.getTopOperand()));
        Label end = null;
        if (safe && !isPrimitiveType(operandStack.getTopOperand())) {
            operandStack.dup();
            end = operandStack.jump(IFNULL);
        }

        int nArgs = 1;
        List<Expression> args = makeArgumentList(arguments).getExpressions();
        boolean spread = AsmClassGenerator.containsSpreadExpression(arguments);
        if (spread) {
            acg.despreadList(args, true);
            sig.append(getTypeDescription(Object[].class));
        } else {
            for (Expression arg : args) {
                arg.visit(acg);
                if (arg instanceof CastExpression) {
                    operandStack.box();
                    acg.loadWrapper(arg);
                    sig.append(getTypeDescription(Wrapper.class));
                } else {
                    sig.append(getTypeDescription(operandStack.getTopOperand()));
                }
                nArgs++;
            }
        }
        sig.append(")Ljava/lang/Object;");

        int flags = safe ? SAFE_NAVIGATION : 0;
        if (spread) flags |= SPREAD_CALL;
        controller.getMethodVisitor().visitInvokeDynamicInsn(METHOD.getCallSiteName(), sig.toString(), BSM, getMethodName(call.getMethod()), flags);
        operandStack.replace(OBJECT_TYPE, nArgs);
        if (end != null) controller.getMethodVisitor().visitLabel(end);
    }

    private void finishIndyCall(final Handle bsmHandle, final String methodName, final String sig, final int numberOfArguments, final Object... bsmArgs) {
        CompileStack compileStack = controller.getCompileStack();
        OperandStack operandStack = controller.getOperandStack();

        controller.getMethodVisitor().visitInvokeDynamicInsn(methodName, sig, bsmHandle, bsmArgs);

        operandStack.replace(OBJECT_TYPE, numberOfArguments);
        compileStack.popLHS();
    }

    private void makeIndyCall(final MethodCallerMultiAdapter adapter, final Expression origReceiver, final boolean implicitThis, final boolean safe, final String methodName, final Expression arguments) {
        OperandStack operandStack = controller.getOperandStack();

        Expression receiver = correctReceiverForInterfaceCall(origReceiver);
        StringBuilder sig = new StringBuilder(prepareIndyCall(receiver, implicitThis));

        Label end = null;
        if (safe && !isPrimitiveType(operandStack.getTopOperand())) {
            operandStack.dup();
            end = operandStack.jump(IFNULL);
        }

        // load arguments
        int numberOfArguments = 1;
        List<Expression> args = makeArgumentList(arguments).getExpressions();
        boolean containsSpreadExpression = AsmClassGenerator.containsSpreadExpression(arguments);
        AsmClassGenerator acg = controller.getAcg();
        if (containsSpreadExpression) {
            acg.despreadList(args, true);
            sig.append(getTypeDescription(Object[].class));
        } else {
            for (Expression arg : args) {
                arg.visit(acg);
                if (arg instanceof CastExpression) {
                    operandStack.box();
                    acg.loadWrapper(arg);
                    sig.append(getTypeDescription(Wrapper.class));
                } else {
                    sig.append(getTypeDescription(operandStack.getTopOperand()));
                }
                numberOfArguments += 1;
            }
        }

        sig.append(")Ljava/lang/Object;");

        String callSiteName = METHOD.getCallSiteName();
        if (adapter == null) callSiteName = INIT.getCallSiteName();
        if (receiver != origReceiver) callSiteName = INTERFACE.getCallSiteName();

        int flags = getMethodCallFlags(adapter, false, containsSpreadExpression);

        finishIndyCall(BSM, callSiteName, sig.toString(), numberOfArguments, methodName, flags);

        if (end != null) controller.getMethodVisitor().visitLabel(end);
    }

    private Expression correctReceiverForInterfaceCall(final Expression receiver) {
        if (receiver instanceof PropertyExpression pexp) {
            if (pexp.getObjectExpression() instanceof ClassExpression
                     && pexp.getProperty() instanceof ConstantExpression) {
                String name = pexp.getProperty().getText();
                ClassNode type = pexp.getObjectExpression().getType();
                if ((name.equals("this") || name.equals("super")) && type.isInterface()) {
                    return bytecodeX(type, mv -> mv.visitIntInsn(Opcodes.ALOAD, 0)); // load this with type
                }
            }
        }
        return receiver;
    }

    private static int getMethodCallFlags(final MethodCallerMultiAdapter adapter, final boolean safe, final boolean spread) {
        int flags = 0;
        if (safe)                           flags |= SAFE_NAVIGATION;
        if (spread)                         flags |= SPREAD_CALL;
        if (adapter==invokeMethodOnCurrent) flags |= THIS_CALL;

        return flags;
    }

    @Override
    public void makeSingleArgumentCall(final Expression receiver, final String message, final Expression arguments, final boolean safe) {
        makeIndyCall(invokeMethod, receiver, false, safe, message, arguments);
    }

    protected void writeGetProperty(final Expression receiver, final String propertyName, final boolean safe, final boolean implicitThis, final boolean groovyObject) {
        var descriptor = prepareIndyCall(receiver, implicitThis) + ")Ljava/lang/Object;";
        int flags = 0;
        if (safe)         flags |= SAFE_NAVIGATION;
        if (groovyObject) flags |= GROOVY_OBJECT;
        if (implicitThis) flags |= IMPLICIT_THIS;
        else if (isThisExpression(receiver)) flags |= THIS_CALL; // GROOVY-6335
        finishIndyCall(BSM, GET.getCallSiteName(), descriptor, 1, propertyName, flags);
    }

    @Override
    protected void writeNormalConstructorCall(final ConstructorCallExpression call) {
        makeCall(call, new ClassExpression(call.getType()), new ConstantExpression("<init>"), call.getArguments(), null, false, false, false);
    }

    @Override
    public void coerce(final ClassNode from, final ClassNode target) {
        ClassNode wrapper = getWrapper(target);
        makeIndyCall(invokeMethod, EmptyExpression.INSTANCE, false, false, "asType", new ClassExpression(wrapper));
        if (isPrimitiveBoolean(target) || isWrapperBoolean(target)) {
            writeIndyCast(OBJECT_TYPE, target);
        } else {
            doCast(controller.getMethodVisitor(),wrapper);
            controller.getOperandStack().replace(wrapper);
            controller.getOperandStack().doGroovyCast(target);
        }
    }

    @Override
    public void castToNonPrimitiveIfNecessary(final ClassNode sourceType, final ClassNode targetType) {
        if (WideningCategories.implementsInterfaceOrSubclassOf(getWrapper(sourceType), targetType)) {
            controller.getOperandStack().box();
        } else {
            writeIndyCast(sourceType, targetType);
        }
    }

    @Override
    public void castNonPrimitiveToBool(final ClassNode sourceType) {
        writeIndyCast(sourceType, boolean_TYPE);
    }

    private void writeIndyCast(final ClassNode sourceType, final ClassNode targetType) {
        String descriptor = "(" + getTypeDescription(sourceType) + ')' + getTypeDescription(targetType);
        controller.getMethodVisitor().visitInvokeDynamicInsn(CAST.getCallSiteName(), descriptor, BSM, CAST_ARGS);
        controller.getOperandStack().replace(targetType); // cast converts top operand from source to target type
    }
}
