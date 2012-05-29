/*
 * Copyright 2003-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.classgen.asm.indy;

import java.lang.invoke.*;
import java.lang.invoke.MethodHandles.Lookup;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.classgen.asm.CompileStack;
import org.codehaus.groovy.classgen.asm.InvocationWriter;
import org.codehaus.groovy.classgen.asm.MethodCallerMultiAdapter;
import org.codehaus.groovy.classgen.asm.OperandStack;
import org.codehaus.groovy.classgen.asm.WriterController;
import org.codehaus.groovy.runtime.wrappers.Wrapper;
import org.codehaus.groovy.vmplugin.v7.IndyInterface;
import org.objectweb.asm.Handle;

import static org.objectweb.asm.Opcodes.*;
import static org.codehaus.groovy.classgen.asm.BytecodeHelper.*;

/**
 * This Writer is used to generate the call invocation byte codes
 * for usage by invokedynamic.
 * 
 * @author <a href="mailto:blackdrag@gmx.org">Jochen "blackdrag" Theodorou</a>
 */
public class InvokeDynamicWriter extends InvocationWriter {
    
    
    private static final String INDY_INTERFACE_NAME = IndyInterface.class.getName().replace('.', '/');
    private static final String BSM_METHOD_TYPE_DESCRIPTOR = 
        MethodType.methodType(
                CallSite.class, Lookup.class, String.class, MethodType.class
        ).toMethodDescriptorString();
    
    private static final String PROPERTY_BSM_METHOD_TYPE_DESCRIPTOR = 
            MethodType.methodType(
                    CallSite.class, Lookup.class, String.class, MethodType.class, int.class
                    ).toMethodDescriptorString();    
    private static final Handle GET_PROPERTY_BSM = 
            new Handle(
                    H_INVOKESTATIC,
                    INDY_INTERFACE_NAME,
                    "bootstrapGetProperty",
                    PROPERTY_BSM_METHOD_TYPE_DESCRIPTOR);
    
    private static final Handle BSM = 
        new Handle(
                H_INVOKESTATIC,
                INDY_INTERFACE_NAME,
                "bootstrap",
                BSM_METHOD_TYPE_DESCRIPTOR);
    private static final Handle BSM_SAFE = 
        new Handle(
                H_INVOKESTATIC,
                INDY_INTERFACE_NAME,
                "bootstrapSafe",
                BSM_METHOD_TYPE_DESCRIPTOR);
    private static final Handle BSM_CURRENT = 
        new Handle(
                H_INVOKESTATIC,
                INDY_INTERFACE_NAME,
                "bootstrapCurrent",
                BSM_METHOD_TYPE_DESCRIPTOR);
    private static final Handle BSM_CURRENT_SAFE = 
        new Handle(
                H_INVOKESTATIC,
                INDY_INTERFACE_NAME,
                "bootstrapCurrentSafe",
                BSM_METHOD_TYPE_DESCRIPTOR);
    
    private WriterController controller;

    public InvokeDynamicWriter(WriterController wc) {
        super(wc);
        this.controller = wc;
    }

    @Override
    public void makeCall(
            Expression origin,  Expression receiver,
            Expression message, Expression arguments,
            MethodCallerMultiAdapter adapter, 
            boolean safe, boolean spreadSafe, boolean implicitThis 
    ) {
        // direct method call
        boolean containsSpreadExpression = AsmClassGenerator.containsSpreadExpression(arguments);
        if (containsSpreadExpression) {
            super.makeCall(origin, receiver, message, arguments, adapter, safe, spreadSafe, implicitThis);
            return;
        }
        
        if (origin instanceof MethodCallExpression) {
            MethodCallExpression mce = (MethodCallExpression) origin;
            MethodNode target = mce.getMethodTarget();
            if (writeDirectMethodCall(target, implicitThis, receiver, makeArgumentList(arguments))) return;
        }

        // no direct method call, dynamic call site        
        ClassNode cn = controller.getClassNode();
        if (controller.isInClosure() && !implicitThis && AsmClassGenerator.isThisExpression(receiver)) cn=cn.getOuterClass();
        ClassExpression sender = new ClassExpression(cn);

        OperandStack operandStack = controller.getOperandStack();
        CompileStack compileStack = controller.getCompileStack();
        AsmClassGenerator acg = controller.getAcg();
        
        // fixed number of arguments && name is a real String and no GString
        if ((adapter == invokeMethod || adapter == invokeMethodOnCurrent || adapter == invokeStaticMethod) && !spreadSafe) {
            String methodName = getMethodName(message);
            
            if (methodName != null) {
                makeIndyCall(adapter, receiver, implicitThis, safe, methodName, arguments);
                return;
            }
        }

        // variable number of arguments wrapped in a Object[]
        
        
        // ensure VariableArguments are read, not stored
        compileStack.pushLHS(false);

        // sender only for call sites
        if (adapter == AsmClassGenerator.setProperty) {
            ConstantExpression.NULL.visit(acg);
        } else {
            sender.visit(acg);
        }
        
        // receiver
        compileStack.pushImplicitThis(implicitThis);
        receiver.visit(acg);
        operandStack.box();
        compileStack.popImplicitThis();
        
        
        int operandsToRemove = 2;
        // message
        if (message != null) {
            message.visit(acg);
            operandStack.box();
            operandsToRemove++;
        }

        // arguments
        int numberOfArguments = containsSpreadExpression ? -1 : AsmClassGenerator.argumentSize(arguments);
        if (numberOfArguments > MethodCallerMultiAdapter.MAX_ARGS || containsSpreadExpression) {
            ArgumentListExpression ae = makeArgumentList(arguments);
            if (containsSpreadExpression) {
                acg.despreadList(ae.getExpressions(), true);
            } else {
                ae.visit(acg);
            }
        } else if (numberOfArguments > 0) {
            operandsToRemove += numberOfArguments;
            TupleExpression te = (TupleExpression) arguments;
            for (int i = 0; i < numberOfArguments; i++) {
                Expression argument = te.getExpression(i);
                argument.visit(acg);
                operandStack.box();
                if (argument instanceof CastExpression) acg.loadWrapper(argument);
            }
        }

        adapter.call(controller.getMethodVisitor(), numberOfArguments, safe, spreadSafe);

        compileStack.popLHS();
        operandStack.replace(ClassHelper.OBJECT_TYPE,operandsToRemove);
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
        
        String sig = prepareIndyCall(receiver, implicitThis);
        
        // load arguments
        int numberOfArguments = 1;
        ArgumentListExpression ae = makeArgumentList(arguments);
        for (Expression arg : ae.getExpressions()) {
            arg.visit(controller.getAcg());
            if (arg instanceof CastExpression) {
                operandStack.box();
                controller.getAcg().loadWrapper(arg);
                sig += getTypeDescription(Wrapper.class);
            } else {
                sig += getTypeDescription(operandStack.getTopOperand());
            }
            numberOfArguments++;
        }
        sig += ")Ljava/lang/Object;";
        
        Handle bsmHandle = getBsmHandle(adapter, safe);
        finishIndyCall(bsmHandle, methodName, sig, numberOfArguments);
    }
    
    private Handle getBsmHandle(MethodCallerMultiAdapter adapter, boolean safe) {
        if (adapter==invokeMethodOnCurrent) {
            if (safe) return BSM_CURRENT_SAFE;
            return BSM_CURRENT;
        } else {
            if (safe) return BSM_SAFE;
            return BSM;
        }
    }

    @Override
    public void makeSingleArgumentCall(Expression receiver, String message, Expression arguments) {
        makeIndyCall(null, receiver, false, false, message, arguments);
    }

    private int getPropertyHandleIndex(boolean safe, boolean implicitThis, boolean groovyObject) {
        int index = 0;
        if (implicitThis)   index += 1;
        if (groovyObject)   index += 2;
        if (safe)           index += 4;
        return index;
    }

    protected void writeGetProperty(Expression receiver, String propertyName, boolean safe, boolean implicitThis, boolean groovyObject) {
        String sig = prepareIndyCall(receiver, implicitThis);
        sig += ")Ljava/lang/Object;";
        int index = getPropertyHandleIndex(safe,implicitThis,groovyObject);
        finishIndyCall(GET_PROPERTY_BSM, propertyName, sig, 1, index);
    }
}
