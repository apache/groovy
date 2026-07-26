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
import static org.objectweb.asm.Opcodes.NOP;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.RETURN;

/**
 * Bytecode writer for classic (non-{@code invokedynamic}) call sites.
 * <p>
 * Non-public API used by {@code AsmClassGenerator}. Do not use this class
 * in application code. Groovy 6 defaults to invokedynamic
 * ({@link org.codehaus.groovy.classgen.asm.indy.IndyCallSiteWriter});
 * this writer is selected only when {@code indy} is disabled. The generated
 * bytecode references {@code org.codehaus.groovy.runtime.callsite.*}, which
 * lives in the optional {@code groovy-callsite} module — that module must be
 * on the classpath both to emit and to execute such classes
 * ({@code WriterController} fails fast if it is missing when {@code indy} is off).
 *
 * @see org.codehaus.groovy.classgen.asm.indy.IndyCallSiteWriter
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
            String sb = "(" + "Ljava/lang/Object;".repeat(numberOfArguments) + ")[Ljava/lang/Object;";
            sig[numberOfArguments] = sb;
        }
        return sig[numberOfArguments];
    }
    private static final int
        MOD_PRIVSS = ACC_PRIVATE+ACC_STATIC+ACC_SYNTHETIC,
        MOD_PUBSS  = ACC_PUBLIC+ACC_STATIC+ACC_SYNTHETIC;
    private static final ClassNode CALLSITE_ARRAY_TYPE = ClassHelper.make("org.codehaus.groovy.runtime.callsite.CallSite").makeArray();
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
    /**
     * Sentinel name for constructor calls.
     */
    public static final String CONSTRUCTOR = "<$constructor$>";
    private final List<String> callSites = new ArrayList<String>(32);
    private int callSiteArrayVarIndex = -1;
    private final WriterController controller;

    /**
     * Creates a call site writer with the given controller.
     *
     * @param wc the writer controller
     */
    public CallSiteWriter(WriterController wc) {
        this.controller = wc;
        ClassNode node = controller.getClassNode();
        if(node instanceof InterfaceHelperClassNode ihcn) {
            callSites.addAll(ihcn.getCallSites());
        }
    }

    /**
     * Generates bytecode to load the call site array into a local variable.
     */
    public void makeSiteEntry() {
        if (controller.isNotClinit()) {
            MethodVisitor mv = controller.getMethodVisitor();
            mv.visitInsn(NOP); // GROOVY-9076: need this for debugger to support step into
            // GROOVY-11982: for an interface, route to the helper inner class which
            // owns $getCallSiteArray (interfaces themselves never have it, and an
            // INVOKESTATIC Methodref against an interface owner throws ICCE)
            mv.visitMethodInsn(INVOKESTATIC, controller.getClassName(), GET_CALLSITE_METHOD, GET_CALLSITE_DESC, false);
            controller.getOperandStack().push(CALLSITE_ARRAY_TYPE);
            callSiteArrayVarIndex = controller.getCompileStack().defineTemporaryVariable("$local$callSiteArray", CALLSITE_ARRAY_TYPE, true);
        }
    }

    /**
     * Generates the call site array field and accessor methods.
     */
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
            mv.visitMaxs(0, 0);
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
        mv.visitMaxs(0, 0);
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

    /**
     * Prepares a call-site entry for the named method on the bytecode operand stack,
     * loading the call-site array and selecting the slot for {@code message}.
     *
     * @param message the method name for which to prepare the call site
     */
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

    /**
     * Generates bytecode to prepare the call site and receiver for a method call.
     *
     * @param receiver the receiver expression
     * @param methodName the method name
     * @param implicitThis whether the receiver is implicit 'this'
     * @param lhs whether this is on the left-hand side of an assignment
     */
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

    /**
     * Visits an argument expression and ensures it is boxed.
     *
     * @param exp the argument expression
     */
    protected void visitBoxedArgument(Expression exp) {
        exp.visit(controller.getAcg());
        if (!(exp instanceof TupleExpression)) {
            // we are not in a tuple, so boxing might be missing for
            // this single argument call
            controller.getOperandStack().box();
        }
    }

    /**
     * Generates a single-argument method call.
     *
     * @param receiver the receiver expression
     * @param message the method name
     * @param arguments the argument expression
     */
    public final void makeSingleArgumentCall(Expression receiver, String message, Expression arguments) {
        makeSingleArgumentCall(receiver, message, arguments, false);
    }

    /**
     * Generates a single-argument method call with optional safe navigation.
     *
     * @param receiver the receiver expression
     * @param message the method name
     * @param arguments the argument expression
     * @param safe whether to use safe navigation
     */
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

    /**
     * Generates a GroovyObject getProperty call.
     *
     * @param receiver the receiver expression
     * @param methodName the property name
     * @param safe whether to use safe navigation
     * @param implicitThis whether the receiver is implicit 'this'
     */
    public void makeGroovyObjectGetPropertySite(Expression receiver, String methodName, boolean safe, boolean implicitThis) {
        prepareSiteAndReceiver(receiver, methodName, implicitThis);
        invokeSafe(safe, "callGroovyObjectGetProperty", "callGroovyObjectGetPropertySafe");
    }

    /**
     * Generates a general getProperty call.
     *
     * @param receiver the receiver expression
     * @param methodName the property name
     * @param safe whether to use safe navigation
     * @param implicitThis whether the receiver is implicit 'this'
     */
    public void makeGetPropertySite(Expression receiver, String methodName, boolean safe, boolean implicitThis) {
        prepareSiteAndReceiver(receiver, methodName, implicitThis);
        invokeSafe(safe, "callGetProperty", "callGetPropertySafe");
    }

    /**
     * Generates a general method call through the call site infrastructure.
     *
     * @param receiver the receiver expression
     * @param message the method name
     * @param arguments the arguments expression
     * @param safe whether to use safe navigation
     * @param implicitThis whether the receiver is implicit 'this'
     * @param callCurrent whether to call on the current object
     * @param callStatic whether this is a static method call
     */
    public void makeCallSite(final Expression receiver, final String message, final Expression arguments,
            final boolean safe, final boolean implicitThis, final boolean callCurrent, final boolean callStatic) {
        prepareSiteAndReceiver(receiver, message, implicitThis);

        AsmClassGenerator acg = controller.getAcg();
        CompileStack cs = controller.getCompileStack();
        OperandStack os = controller.getOperandStack();
        MethodVisitor mv = controller.getMethodVisitor();

        cs.pushLHS(false);
        cs.pushImplicitThis(implicitThis);

        boolean containsSpreadExpression = AsmClassGenerator.containsSpreadExpression(arguments);
        int numberOfArguments = AsmClassGenerator.argumentSize(arguments), operandsToReplace = 1;
        if (numberOfArguments > MethodCallerMultiAdapter.MAX_ARGS || containsSpreadExpression) {
            ArgumentListExpression list = InvocationWriter.makeArgumentList(arguments);
            cs.pushImplicitThis(false);
            if (containsSpreadExpression) {
                numberOfArguments = -1;
                acg.despreadList(list.getExpressions(), true);
            } else {
                numberOfArguments = list.getExpressions().size();
                for (Expression argument : list) {
                    argument.visit(acg);
                    os.box();
                    if (argument instanceof CastExpression) {
                        acg.loadWrapper(argument);
                    }
                }
                operandsToReplace += numberOfArguments;
            }
            cs.popImplicitThis();
        }

        cs.popLHS();
        cs.popImplicitThis();

        String desc;
        switch (numberOfArguments) {
        case 0:
            desc = ")Ljava/lang/Object;"; break;
        case 1:
            desc = "Ljava/lang/Object;)Ljava/lang/Object;"; break;
        case 2:
            desc = "Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"; break;
        case 3:
            desc = "Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"; break;
        case 4:
            desc = "Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"; break;
        default:
            mv.visitMethodInsn(INVOKESTATIC, "org/codehaus/groovy/runtime/ArrayUtil", "createArray", getCreateArraySignature(numberOfArguments), false);
            os.replace(ClassHelper.OBJECT_TYPE.makeArray(), numberOfArguments);
            operandsToReplace = operandsToReplace - numberOfArguments + 1;
        case -1: // spread expression case produces Object[]
            desc = "[Ljava/lang/Object;)Ljava/lang/Object;";
        }

        if (callStatic) {
            mv.visitMethodInsn(INVOKEINTERFACE, CALLSITE_CLASS, "callStatic", "(Ljava/lang/Class;" + desc, true);
        } else if (message.equals(CONSTRUCTOR)) {
            mv.visitMethodInsn(INVOKEINTERFACE, CALLSITE_CLASS, "callConstructor", "(Ljava/lang/Object;" + desc, true);
        } else if (callCurrent) {
            mv.visitMethodInsn(INVOKEINTERFACE, CALLSITE_CLASS, "callCurrent", "(Lgroovy/lang/GroovyObject;" + desc, true);
        } else if (safe) {
            mv.visitMethodInsn(INVOKEINTERFACE, CALLSITE_CLASS, "callSafe", "(Ljava/lang/Object;" + desc, true);
        } else {
            mv.visitMethodInsn(INVOKEINTERFACE, CALLSITE_CLASS, "call", "(Ljava/lang/Object;" + desc, true);
        }

        os.replace(ClassHelper.OBJECT_TYPE, operandsToReplace);
    }

    /**
     * Returns the list of call site names.
     *
     * @return the call site names
     */
    public List<String> getCallSites() {
        return callSites;
    }

    /**
     * Generates bytecode to initialize the call site array field to null.
     */
    public void makeCallSiteArrayInitializer() {
        final String classInternalName = BytecodeHelper.getClassInternalName(controller.getClassNode());
        MethodVisitor mv = controller.getMethodVisitor();
        mv.visitInsn(ACONST_NULL);
        mv.visitFieldInsn(PUTSTATIC, classInternalName, "$callSiteArray", "Ljava/lang/ref/SoftReference;");
    }

    /**
     * Checks if any call sites have been used.
     *
     * @return true if call sites have been used
     */
    public boolean hasCallSiteUse() {
        return callSiteArrayVarIndex>=0;
    }

    /**
     * Generates a fallback call for attribute or property access.
     *
     * @param expression the property expression
     * @param objectExpression the object expression
     * @param name the property name
     * @param adapter the method caller adapter
     */
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

    /**
     * Emits a property-write site (GROOVY-12138). The assigned value is
     * already on the operand stack. The default implementation is the classic
     * {@code ScriptBytecodeAdapter} adapter call; the indy writer may override
     * this to emit an {@code invokedynamic} {@code setProperty} call site.
     *
     * @param expression the property expression being assigned
     * @param objectExpression the receiver expression
     * @param name the property name
     * @param adapter the classic set adapter used for the default path
     * @param groovyObject whether the receiver is statically known to be a {@code GroovyObject}
     */
    public void makeSetPropertySite(PropertyExpression expression, Expression objectExpression, String name, MethodCallerMultiAdapter adapter, boolean groovyObject) {
        fallbackAttributeOrPropertySite(expression, objectExpression, name, adapter);
    }
}
