/*
 $Id$

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */
package org.codehaus.groovy.classgen;

import groovy.lang.MetaMethod;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;

/**
 * Code generates a Reflector
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class ReflectorGenerator implements Opcodes {

    private List methods;
    private ClassVisitor cv;
    private BytecodeHelper helper = new BytecodeHelper(null);
    private String classInternalName;

    public ReflectorGenerator(List methods) {
        this.methods = methods;
    }

    public void generate(ClassVisitor cv, String className) {
        this.cv = cv;

        classInternalName = BytecodeHelper.getClassInternalName(className);
        cv.visit(ClassGenerator.asmJDKVersion, ACC_PUBLIC + ACC_SUPER, classInternalName, (String) null, "org/codehaus/groovy/runtime/Reflector", null);

        MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "org/codehaus/groovy/runtime/Reflector", "<init>", "()V");
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);

        generateInvokeMethod();

        cv.visitEnd();
    }

    protected void generateInvokeMethod() {
        int methodCount = methods.size();

        MethodVisitor mv = cv.visitMethod(
                ACC_PUBLIC,
                "invoke",
                "(Lgroovy/lang/MetaMethod;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;",
                null,
                null);

        // load parameters for the helper method call
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ALOAD, 3);

        // get method number for switch
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, "groovy/lang/MetaMethod", "getMethodIndex", "()I");

        // init meta methods with number
        Label defaultLabel = new Label();
        Label[] labels = new Label[methodCount];
        int[] indices = new int[methodCount];
        for (int i = 0; i < methodCount; i++) {
            labels[i] = new Label();
            MetaMethod method = (MetaMethod) methods.get(i);
            method.setMethodIndex(i + 1);
            indices[i] = method.getMethodIndex();
        }

        // do switch
        mv.visitLookupSwitchInsn(defaultLabel, indices, labels);
        // create switch cases
        for (int i = 0; i < methodCount; i++) {
            // call helper for invocation
            mv.visitLabel(labels[i]);
            mv.visitMethodInsn(
                    INVOKESPECIAL,
                    classInternalName,
                    "m" + i,
                    "(Lgroovy/lang/MetaMethod;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");
            mv.visitInsn(ARETURN);
        }

        // call helper for error
        mv.visitLabel(defaultLabel);
        mv.visitMethodInsn(
                INVOKEVIRTUAL,
                classInternalName,
                "noSuchMethod",
                "(Lgroovy/lang/MetaMethod;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");
        mv.visitInsn(ARETURN);
        // end method
        mv.visitMaxs(4, 4);
        mv.visitEnd();

        // create helper methods m*
        for (int i = 0; i < methodCount; i++) {
            mv = cv.visitMethod(
                    ACC_PRIVATE,
                    "m" + i,
                    "(Lgroovy/lang/MetaMethod;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;",
                    null,
                    null);
            helper = new BytecodeHelper(mv);

            MetaMethod method = (MetaMethod) methods.get(i);
            invokeMethod(method, mv);
            if (method.getReturnType() == void.class) {
                mv.visitInsn(ACONST_NULL);
            }
            mv.visitInsn(ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }

    protected void invokeMethod(MetaMethod method, MethodVisitor mv) {
        // compute class to make the call on
        Class callClass = method.getInterfaceClass();
        boolean useInterface = false;
        if (callClass == null) {
            callClass = method.getCallClass();
        } else {
            useInterface = true;
        }
        // get bytecode information
        String type = BytecodeHelper.getClassInternalName(callClass.getName());
        String descriptor = BytecodeHelper.getMethodDescriptor(method.getReturnType(), method.getParameterTypes());

        // make call
        if (method.isStatic()) {
            loadParameters(method, 3, mv);
            mv.visitMethodInsn(INVOKESTATIC, type, method.getName(), descriptor);
        } else {
            mv.visitVarInsn(ALOAD, 2);
            helper.doCast(callClass);
            loadParameters(method, 3, mv);
            mv.visitMethodInsn((useInterface) ? INVOKEINTERFACE : INVOKEVIRTUAL, type, method.getName(), descriptor);
        }

        helper.box(method.getReturnType());
    }

    protected void loadParameters(MetaMethod method, int argumentIndex, MethodVisitor mv) {
        Class[] parameters = method.getParameterTypes();
        int size = parameters.length;
        for (int i = 0; i < size; i++) {
            // unpack argument from Object[]
            mv.visitVarInsn(ALOAD, argumentIndex);
            helper.pushConstant(i);
            mv.visitInsn(AALOAD);

            // cast argument to parameter class, inclusive unboxing
            // for methods with primitive types
            Class type = parameters[i];
            if (type.isPrimitive()) {
                helper.unbox(type);
            } else {
                helper.doCast(type);
            }
        }
    }
}
