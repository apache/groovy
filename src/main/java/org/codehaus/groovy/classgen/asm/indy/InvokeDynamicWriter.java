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

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.EmptyExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.tools.WideningCategories;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.classgen.asm.BytecodeHelper;
import org.codehaus.groovy.classgen.asm.CompileStack;
import org.codehaus.groovy.classgen.asm.InvocationWriter;
import org.codehaus.groovy.classgen.asm.MethodCallerMultiAdapter;
import org.codehaus.groovy.classgen.asm.OperandStack;
import org.codehaus.groovy.classgen.asm.WriterController;
import org.codehaus.groovy.runtime.wrappers.Wrapper;
import org.codehaus.groovy.vmplugin.v7.IndyInterface;
import org.objectweb.asm.Handle;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;

import static org.codehaus.groovy.classgen.asm.BytecodeHelper.getTypeDescription;
import static org.codehaus.groovy.vmplugin.v7.IndyInterface.CALL_TYPES.CAST;
import static org.codehaus.groovy.vmplugin.v7.IndyInterface.CALL_TYPES.GET;
import static org.codehaus.groovy.vmplugin.v7.IndyInterface.CALL_TYPES.INIT;
import static org.codehaus.groovy.vmplugin.v7.IndyInterface.CALL_TYPES.METHOD;
import static org.codehaus.groovy.vmplugin.v7.IndyInterface.GROOVY_OBJECT;
import static org.codehaus.groovy.vmplugin.v7.IndyInterface.IMPLICIT_THIS;
import static org.codehaus.groovy.vmplugin.v7.IndyInterface.SAFE_NAVIGATION;
import static org.codehaus.groovy.vmplugin.v7.IndyInterface.SPREAD_CALL;
import static org.codehaus.groovy.vmplugin.v7.IndyInterface.THIS_CALL;
import static org.objectweb.asm.Opcodes.H_INVOKESTATIC;

/**
 * This Writer is used to generate the call invocation byte codes
 * for usage by invokedynamic.
 */
public class InvokeDynamicWriter extends InvocationWriter {
    
    
    private static final String INDY_INTERFACE_NAME = IndyInterface.class.getName().replace('.', '/');
    private static final String BSM_METHOD_TYPE_DESCRIPTOR = 
        MethodType.methodType(
                CallSite.class, Lookup.class, String.class, MethodType.class,
                String.class, int.class
        ).toMethodDescriptorString();
    private static final Handle BSM = 
        new Handle(
                H_INVOKESTATIC,
                INDY_INTERFACE_NAME,
                "bootstrap",
                BSM_METHOD_TYPE_DESCRIPTOR);

    private final WriterController controller;

    public InvokeDynamicWriter(WriterController wc) {
        super(wc);
        this.controller = wc;
    }

    @Override
    protected boolean makeCachedCall(Expression origin, ClassExpression sender,
            Expression receiver, Expression message, Expression arguments,
            MethodCallerMultiAdapter adapter, boolean safe, boolean spreadSafe,
            boolean implicitThis, boolean containsSpreadExpression
    ) {
        // fixed number of arguments && name is a real String and no GString
        if ((adapter == null || adapter == invokeMethod || adapter == invokeMethodOnCurrent || adapter == invokeStaticMethod) && !spreadSafe) {
            String methodName = getMethodName(message);
            if (methodName != null) {
                makeIndyCall(adapter, receiver, implicitThis, safe, methodName, arguments);
                return true;
            }
        }
        return false;
    }
    
    private String prepareIndyCall(Expression receiver, boolean implicitThis) {
        CompileStack compileStack = controller.getCompileStack();
        OperandStack operandStack = controller.getOperandStack();

        compileStack.pushLHS(false);
        
        // load normal receiver as first argument
        compileStack.pushImplicitThis(implicitThis);
        receiver.visit(controller.getAcg());
        compileStack.popImplicitThis();
        return "("+getTypeDescription(operandStack.getTopOperand());
    }
    
    private void finishIndyCall(Handle bsmHandle, String methodName, String sig, int numberOfArguments, Object... bsmArgs) {
        CompileStack compileStack = controller.getCompileStack();
        OperandStack operandStack = controller.getOperandStack();

        controller.getMethodVisitor().visitInvokeDynamicInsn(methodName, sig, bsmHandle, bsmArgs);

        operandStack.replace(ClassHelper.OBJECT_TYPE, numberOfArguments);
        compileStack.popLHS();
    }
    
    private void makeIndyCall(MethodCallerMultiAdapter adapter, Expression receiver, boolean implicitThis, boolean safe, String methodName, Expression arguments) {
        OperandStack operandStack = controller.getOperandStack();
        
        StringBuilder sig = new StringBuilder(prepareIndyCall(receiver, implicitThis));
        
        // load arguments
        int numberOfArguments = 1;
        ArgumentListExpression ae = makeArgumentList(arguments);
        boolean containsSpreadExpression = AsmClassGenerator.containsSpreadExpression(arguments);
        if (containsSpreadExpression) {
            controller.getAcg().despreadList(ae.getExpressions(), true);
            sig.append(getTypeDescription(Object[].class));
        } else {
            for (Expression arg : ae.getExpressions()) {
                arg.visit(controller.getAcg());
                if (arg instanceof CastExpression) {
                    operandStack.box();
                    controller.getAcg().loadWrapper(arg);
                    sig.append(getTypeDescription(Wrapper.class));
                } else {
                    sig.append(getTypeDescription(operandStack.getTopOperand()));
                }
                numberOfArguments++;
            }
        }

        sig.append(")Ljava/lang/Object;");
        String callSiteName = METHOD.getCallSiteName();
        if (adapter==null) callSiteName = INIT.getCallSiteName();
        int flags = getMethodCallFlags(adapter, safe, containsSpreadExpression);
        finishIndyCall(BSM, callSiteName, sig.toString(), numberOfArguments, methodName, flags);
    }
    
    private static int getMethodCallFlags(MethodCallerMultiAdapter adapter, boolean safe, boolean spread) {
        int ret = 0;
        if (safe)                           ret |= SAFE_NAVIGATION;
        if (adapter==invokeMethodOnCurrent) ret |= THIS_CALL;
        if (spread)                         ret |= SPREAD_CALL;
        return ret;
    }

    @Override
    public void makeSingleArgumentCall(Expression receiver, String message, Expression arguments) {
        makeIndyCall(invokeMethod, receiver, false, false, message, arguments);
    }

    private static int getPropertyFlags(boolean safe, boolean implicitThis, boolean groovyObject) {
        int flags = 0;
        if (implicitThis)   flags |= IMPLICIT_THIS;
        if (groovyObject)   flags |= GROOVY_OBJECT;
        if (safe)           flags |= SAFE_NAVIGATION;
        return flags;
    }

    protected void writeGetProperty(Expression receiver, String propertyName, boolean safe, boolean implicitThis, boolean groovyObject) {
        String sig = prepareIndyCall(receiver, implicitThis);
        sig += ")Ljava/lang/Object;";
        int flags = getPropertyFlags(safe,implicitThis,groovyObject);
        finishIndyCall(BSM, GET.getCallSiteName(), sig, 1, propertyName, flags);
    }
    
    @Override
    protected void writeNormalConstructorCall(ConstructorCallExpression call) {
        makeCall(call, new ClassExpression(call.getType()), new ConstantExpression("<init>"), call.getArguments(), null, false, false, false);
    }
    
    @Override
    public void coerce(ClassNode from, ClassNode target) {
        ClassNode wrapper = ClassHelper.getWrapper(target);
        makeIndyCall(invokeMethod, EmptyExpression.INSTANCE, false, false, "asType", new ClassExpression(wrapper));
        if (ClassHelper.boolean_TYPE.equals(target) || ClassHelper.Boolean_TYPE.equals(target)) {
            writeIndyCast(ClassHelper.OBJECT_TYPE,target);
        } else {
            BytecodeHelper.doCast(controller.getMethodVisitor(), wrapper);
            controller.getOperandStack().replace(wrapper);
            controller.getOperandStack().doGroovyCast(target);
        }
    }

    @Override
    public void castToNonPrimitiveIfNecessary(ClassNode sourceType, ClassNode targetType) {
        ClassNode boxedType = ClassHelper.getWrapper(sourceType);
        if (WideningCategories.implementsInterfaceOrSubclassOf(boxedType, targetType)) {
            controller.getOperandStack().box();
            return;
        }
        writeIndyCast(sourceType, targetType);
    }

    private void writeIndyCast(ClassNode sourceType, ClassNode targetType) {

        String sig = "(" +
                getTypeDescription(sourceType) +
                ')' +
                getTypeDescription(targetType);
        controller.getMethodVisitor().visitInvokeDynamicInsn(
                //TODO: maybe use a different bootstrap method since no arguments are needed here
                CAST.getCallSiteName(), sig, BSM, "()", 0);
        controller.getOperandStack().replace(targetType);
    }

    @Override
    public void castNonPrimitiveToBool(ClassNode sourceType) {
        writeIndyCast(sourceType, ClassHelper.boolean_TYPE);
    }

}
