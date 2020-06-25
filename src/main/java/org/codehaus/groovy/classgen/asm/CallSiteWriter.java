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
package org.codehaus.groovy.classgen.asm;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.InterfaceHelperClassNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.runtime.callsite.CallSite;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.objectweb.asm.Opcodes.AALOAD;
import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.IFNONNULL;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.RETURN;

/**
 * This class represents non public API used by AsmClassGenerator. Don't
 * use this class in your code
 */
public class CallSiteWriter {
    private static final int SIG_ARRAY_LENGTH = 255;
    private static String [] sig = new String [SIG_ARRAY_LENGTH];
    private static String getCreateArraySignature(int numberOfArguments) {
        if (numberOfArguments >= SIG_ARRAY_LENGTH) {
            throw new IllegalArgumentException(String.format(
                      "The max number of supported arguments is %s, but found %s",
                        SIG_ARRAY_LENGTH, numberOfArguments));
        }
        if (sig[numberOfArguments] == null) {
            StringBuilder sb = new StringBuilder("(");
            for (int i = 0; i != numberOfArguments; ++i) {
                sb.append("Ljava/lang/Object;");
            }
            sb.append(")[Ljava/lang/Object;");
            sig[numberOfArguments] = sb.toString();
        }
        return sig[numberOfArguments];
    }
    private static final int
        MOD_PRIVSS = ACC_PRIVATE+ACC_STATIC+ACC_SYNTHETIC,
        MOD_PUBSS  = ACC_PUBLIC+ACC_STATIC+ACC_SYNTHETIC;
    private static final ClassNode CALLSITE_ARRAY_NODE = ClassHelper.make(CallSite[].class);
    private static final String
        GET_CALLSITE_METHOD     = "$getCallSiteArray",
        CALLSITE_CLASS          = "org/codehaus/groovy/runtime/callsite/CallSite",
        CALLSITE_DESC           = "[Lorg/codehaus/groovy/runtime/callsite/CallSite;",
        GET_CALLSITE_DESC       = "()"+CALLSITE_DESC,
        CALLSITE_ARRAY_CLASS    = "org/codehaus/groovy/runtime/callsite/CallSiteArray",
        GET_CALLSITEARRAY_DESC  = "()Lorg/codehaus/groovy/runtime/callsite/CallSiteArray;",
        CALLSITE_FIELD          = "$callSiteArray",
        REF_CLASS               = "java/lang/ref/SoftReference",
        REF_DESC                = "L"+REF_CLASS+";",
        METHOD_OO_DESC          = "(Ljava/lang/Object;)Ljava/lang/Object;",
        CREATE_CSA_METHOD       = "$createCallSiteArray";
    public static final String CONSTRUCTOR = "<$constructor$>";
    private final List<String> callSites = new ArrayList<String>(32);
    private int callSiteArrayVarIndex = -1;
    private final WriterController controller;

    public CallSiteWriter(WriterController wc) {
        this.controller = wc;
        ClassNode node = controller.getClassNode();
        if(node instanceof InterfaceHelperClassNode) {
            InterfaceHelperClassNode ihcn = (InterfaceHelperClassNode) node;
            callSites.addAll(ihcn.getCallSites());
        }
    }
    
    public void makeSiteEntry() {
        if (controller.isNotClinit()) {
            controller.getMethodVisitor().visitMethodInsn(INVOKESTATIC, controller.getInternalClassName(), GET_CALLSITE_METHOD, GET_CALLSITE_DESC, false);
            controller.getOperandStack().push(CALLSITE_ARRAY_NODE);
            callSiteArrayVarIndex = controller.getCompileStack().defineTemporaryVariable("$local$callSiteArray", CALLSITE_ARRAY_NODE, true);
        }
    }
    
    public void generateCallSiteArray() {
        if (!controller.getClassNode().isInterface()) {
            controller.getClassVisitor().visitField(MOD_PRIVSS, CALLSITE_FIELD, REF_DESC, null, null);
            generateCreateCallSiteArray();
            generateGetCallSiteArray();
        }
    }

    private void generateGetCallSiteArray() {
        int visibility = (controller.getClassNode() instanceof InterfaceHelperClassNode) ? MOD_PUBSS : MOD_PRIVSS;
        MethodVisitor mv = controller.getClassVisitor().visitMethod(visibility, GET_CALLSITE_METHOD, GET_CALLSITE_DESC, null, null);
        controller.setMethodVisitor(mv);
        mv.visitCode();
        mv.visitFieldInsn(GETSTATIC, controller.getInternalClassName(), "$callSiteArray", "Ljava/lang/ref/SoftReference;");
        Label l0 = new Label();
        mv.visitJumpInsn(IFNULL, l0);
        mv.visitFieldInsn(GETSTATIC, controller.getInternalClassName(), "$callSiteArray", "Ljava/lang/ref/SoftReference;");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/ref/SoftReference", "get", "()Ljava/lang/Object;", false);
        mv.visitTypeInsn(CHECKCAST, "org/codehaus/groovy/runtime/callsite/CallSiteArray");
        mv.visitInsn(DUP);
        mv.visitVarInsn(ASTORE, 0);
        Label l1 = new Label();
        mv.visitJumpInsn(IFNONNULL, l1);
        mv.visitLabel(l0);
        mv.visitMethodInsn(INVOKESTATIC, controller.getInternalClassName(), "$createCallSiteArray", "()Lorg/codehaus/groovy/runtime/callsite/CallSiteArray;", false);
        mv.visitVarInsn(ASTORE, 0);
        mv.visitTypeInsn(NEW, "java/lang/ref/SoftReference");
        mv.visitInsn(DUP);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/ref/SoftReference", "<init>", "(Ljava/lang/Object;)V", false);
        mv.visitFieldInsn(PUTSTATIC, controller.getInternalClassName(), "$callSiteArray", "Ljava/lang/ref/SoftReference;");
        mv.visitLabel(l1);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "org/codehaus/groovy/runtime/callsite/CallSiteArray", "array", "[Lorg/codehaus/groovy/runtime/callsite/CallSite;");
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
        
    private void generateCreateCallSiteArray() { 
        List<String> callSiteInitMethods = new LinkedList<String>(); 
        int index = 0; 
        int methodIndex = 0; 
        final int size = callSites.size(); 
        final int maxArrayInit = 5000; 
        // create array initialization methods
        while (index < size) { 
            methodIndex++; 
            String methodName = "$createCallSiteArray_" + methodIndex; 
            callSiteInitMethods.add(methodName); 
            MethodVisitor mv = controller.getClassVisitor().visitMethod(MOD_PRIVSS, methodName, "([Ljava/lang/String;)V", null, null);
            controller.setMethodVisitor(mv);
            mv.visitCode(); 
            int methodLimit = size; 
            // check if the next block is over the max allowed
            if ((methodLimit - index) > maxArrayInit) { 
                methodLimit = index + maxArrayInit; 
            } 
            for (; index < methodLimit; index++) { 
                mv.visitVarInsn(ALOAD, 0); 
                mv.visitLdcInsn(index); 
                mv.visitLdcInsn(callSites.get(index)); 
                mv.visitInsn(AASTORE); 
            } 
            mv.visitInsn(RETURN); 
            mv.visitMaxs(2,1); 
            mv.visitEnd(); 
        }
        // create base createCallSiteArray method
        MethodVisitor mv = controller.getClassVisitor().visitMethod(MOD_PRIVSS, CREATE_CSA_METHOD, GET_CALLSITEARRAY_DESC, null, null);
        controller.setMethodVisitor(mv);
        mv.visitCode(); 
        mv.visitLdcInsn(size); 
        mv.visitTypeInsn(ANEWARRAY, "java/lang/String"); 
        mv.visitVarInsn(ASTORE, 0); 
        for (String methodName : callSiteInitMethods) { 
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESTATIC, controller.getInternalClassName(), methodName, "([Ljava/lang/String;)V", false);
        } 

        mv.visitTypeInsn(NEW, CALLSITE_ARRAY_CLASS); 
        mv.visitInsn(DUP); 
        controller.getAcg().visitClassExpression(new ClassExpression(controller.getClassNode())); 

        mv.visitVarInsn(ALOAD, 0);

        mv.visitMethodInsn(INVOKESPECIAL, CALLSITE_ARRAY_CLASS, "<init>", "(Ljava/lang/Class;[Ljava/lang/String;)V", false);
        mv.visitInsn(ARETURN); 
        mv.visitMaxs(0,0); 
        mv.visitEnd(); 
    } 
    
    private int allocateIndex(String name) {
        callSites.add(name);
        return callSites.size()-1;
    }

    private void invokeSafe(boolean safe, String unsafeMethod, String safeMethod) {
        String method = unsafeMethod;
        if (safe) method = safeMethod;
        controller.getMethodVisitor().visitMethodInsn(INVOKEINTERFACE, CALLSITE_CLASS, method, METHOD_OO_DESC, true);
        controller.getOperandStack().replace(ClassHelper.OBJECT_TYPE);
    }

    public void prepareCallSite(String message) {
        MethodVisitor mv = controller.getMethodVisitor();
        if (controller.isNotClinit()) {
            mv.visitVarInsn(ALOAD, callSiteArrayVarIndex);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, controller.getClassName(), GET_CALLSITE_METHOD, GET_CALLSITE_DESC, false);
        }
        final int index = allocateIndex(message);
        mv.visitLdcInsn(index);
        mv.visitInsn(AALOAD);
    }
    
    private void prepareSiteAndReceiver(Expression receiver, String methodName, boolean implicitThis) {
        prepareSiteAndReceiver(receiver, methodName, implicitThis, false);
    }
    
    protected void prepareSiteAndReceiver(Expression receiver, String methodName, boolean implicitThis, boolean lhs) {
        //site
        prepareCallSite(methodName);

        // receiver
        CompileStack compileStack = controller.getCompileStack();
        compileStack.pushImplicitThis(implicitThis);
        compileStack.pushLHS(lhs);
        receiver.visit(controller.getAcg());
        controller.getOperandStack().box();
        compileStack.popLHS();
        compileStack.popImplicitThis();
    }
    
    protected void visitBoxedArgument(Expression exp) {
        exp.visit(controller.getAcg());
        if (!(exp instanceof TupleExpression)) {
            // we are not in a tuple, so boxing might be missing for
            // this single argument call
            controller.getOperandStack().box();
        }
    }

    public final void makeSingleArgumentCall(Expression receiver, String message, Expression arguments) {
        makeSingleArgumentCall(receiver, message, arguments, false);
    }

    public void makeSingleArgumentCall(Expression receiver, String message, Expression arguments, boolean safe) {
        OperandStack operandStack = controller.getOperandStack();
        int m1 = operandStack.getStackLength();
        //slow Path
        prepareSiteAndReceiver(receiver, message, false, controller.getCompileStack().isLHS());
        visitBoxedArgument(arguments);
        int m2 = operandStack.getStackLength();
        controller.getMethodVisitor().visitMethodInsn(INVOKEINTERFACE, "org/codehaus/groovy/runtime/callsite/CallSite", safe ? "callSafe" : "call", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true);
        operandStack.replace(ClassHelper.OBJECT_TYPE, m2-m1);
    }
    
    public void makeGroovyObjectGetPropertySite(Expression receiver, String methodName, boolean safe, boolean implicitThis) {
        prepareSiteAndReceiver(receiver, methodName, implicitThis);
        invokeSafe(safe, "callGroovyObjectGetProperty", "callGroovyObjectGetPropertySafe");
    }
    
    public void makeGetPropertySite(Expression receiver, String methodName, boolean safe, boolean implicitThis) {
        prepareSiteAndReceiver(receiver, methodName, implicitThis);
        invokeSafe(safe, "callGetProperty", "callGetPropertySafe");
    }
    
    public void makeCallSite(Expression receiver, String message, Expression arguments, boolean safe, boolean implicitThis, boolean callCurrent, boolean callStatic) {
        prepareSiteAndReceiver(receiver, message, implicitThis);

        CompileStack compileStack = controller.getCompileStack();
        compileStack.pushImplicitThis(implicitThis);
        compileStack.pushLHS(false);
        boolean constructor = message.equals(CONSTRUCTOR);
        OperandStack operandStack = controller.getOperandStack();
        
        // arguments
        boolean containsSpreadExpression = AsmClassGenerator.containsSpreadExpression(arguments);
        int numberOfArguments = containsSpreadExpression ? -1 : AsmClassGenerator.argumentSize(arguments);
        int operandsToReplace = 1;
        if (numberOfArguments > MethodCallerMultiAdapter.MAX_ARGS || containsSpreadExpression) {
            ArgumentListExpression ae;
            if (arguments instanceof ArgumentListExpression) {
                ae = (ArgumentListExpression) arguments;
            } else if (arguments instanceof TupleExpression) {
                TupleExpression te = (TupleExpression) arguments;
                ae = new ArgumentListExpression(te.getExpressions());
            } else {
                ae = new ArgumentListExpression();
                ae.addExpression(arguments);
            }
            controller.getCompileStack().pushImplicitThis(false);
            if (containsSpreadExpression) {
                numberOfArguments = -1;
                controller.getAcg().despreadList(ae.getExpressions(), true);
            } else {
                numberOfArguments = ae.getExpressions().size();
                for (int i = 0; i < numberOfArguments; i++) {
                    Expression argument = ae.getExpression(i);
                    argument.visit(controller.getAcg());
                    operandStack.box();
                    if (argument instanceof CastExpression) controller.getAcg().loadWrapper(argument);
                }
                operandsToReplace += numberOfArguments;
            }
            controller.getCompileStack().popImplicitThis();
        }
        controller.getCompileStack().popLHS();
        controller.getCompileStack().popImplicitThis();
        
        MethodVisitor mv = controller.getMethodVisitor();
        
        if (numberOfArguments > 4) {
            final String createArraySignature = getCreateArraySignature(numberOfArguments);
            mv.visitMethodInsn(INVOKESTATIC, "org/codehaus/groovy/runtime/ArrayUtil", "createArray", createArraySignature, false);
            //TODO: use pre-generated Object[]
            operandStack.replace(ClassHelper.OBJECT_TYPE.makeArray(),numberOfArguments);
            operandsToReplace = operandsToReplace-numberOfArguments+1;
        }

        final String desc = getDescForParamNum(numberOfArguments);
        if (callStatic) {
            mv.visitMethodInsn(INVOKEINTERFACE, CALLSITE_CLASS, "callStatic", "(Ljava/lang/Class;" + desc, true);
        } else if (constructor) {
            mv.visitMethodInsn(INVOKEINTERFACE, CALLSITE_CLASS, "callConstructor", "(Ljava/lang/Object;" + desc, true);
        } else if (callCurrent) {
            mv.visitMethodInsn(INVOKEINTERFACE, CALLSITE_CLASS, "callCurrent", "(Lgroovy/lang/GroovyObject;" + desc, true);
        } else if (safe) {
            mv.visitMethodInsn(INVOKEINTERFACE, CALLSITE_CLASS, "callSafe", "(Ljava/lang/Object;" + desc, true);
        } else {
            mv.visitMethodInsn(INVOKEINTERFACE, CALLSITE_CLASS, "call", "(Ljava/lang/Object;" + desc, true);
        }
        operandStack.replace(ClassHelper.OBJECT_TYPE,operandsToReplace);
    }
    
    private static String getDescForParamNum(int numberOfArguments) {
        switch (numberOfArguments) {
            case 0:
              return ")Ljava/lang/Object;";
            case 1:
              return "Ljava/lang/Object;)Ljava/lang/Object;";
            case 2:
                return "Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;";
            case 3:
                return "Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;";
            case 4:
                return "Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;";
            default:
                return "[Ljava/lang/Object;)Ljava/lang/Object;";
        }
    }

    public List<String> getCallSites() {
        return callSites;
    }

    public void makeCallSiteArrayInitializer() {
        final String classInternalName = BytecodeHelper.getClassInternalName(controller.getClassNode());
        MethodVisitor mv = controller.getMethodVisitor();
        mv.visitInsn(ACONST_NULL);
        mv.visitFieldInsn(PUTSTATIC, classInternalName, "$callSiteArray", "Ljava/lang/ref/SoftReference;");
    }

    public boolean hasCallSiteUse() {
        return callSiteArrayVarIndex>=0;
    }

    public void fallbackAttributeOrPropertySite(PropertyExpression expression, Expression objectExpression, String name, MethodCallerMultiAdapter adapter) {
        if (controller.getCompileStack().isLHS()) controller.getOperandStack().box();
        controller.getInvocationWriter().makeCall(
                expression,
                objectExpression, // receiver
                new CastExpression(ClassHelper.STRING_TYPE, expression.getProperty()), // messageName
                MethodCallExpression.NO_ARGUMENTS, adapter,
                expression.isSafe(), expression.isSpreadSafe(), expression.isImplicitThis()
        );
    }
}
