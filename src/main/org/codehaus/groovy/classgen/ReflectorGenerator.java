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

import java.util.List;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Constants;
import org.objectweb.asm.Label;

/**
 * Code generates a Reflector 
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class ReflectorGenerator implements Constants {

    private List methods;
    private ClassWriter cw;
    private CodeVisitor cv;
    private BytecodeHelper helper = new BytecodeHelper(null);

    public ReflectorGenerator(List methods) {
        this.methods = methods;
    }

    public byte[] generate(String className) {
        cw = new ClassWriter(false);
        String fileName = className;
        int idx = className.lastIndexOf('.');
        if (idx > 0) {
            fileName = className.substring(idx + 1);
        }
        fileName += ".java";

        cw.visit(
            ACC_PUBLIC + ACC_SUPER,
            helper.getClassInternalName(className),
            "org/codehaus/groovy/runtime/Reflector",
            null,
            fileName);

        cv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        cv.visitVarInsn(ALOAD, 0);
        cv.visitMethodInsn(INVOKESPECIAL, "org/codehaus/groovy/runtime/Reflector", "<init>", "()V");
        cv.visitInsn(RETURN);
        cv.visitMaxs(1, 1);
        cw.visitEnd();
        return cw.toByteArray();
    }

    protected void generateInvokeMethod() {
        int minMethodIndex = 1;
        int methodCount = methods.size();
        int maxMethodIndex = methodCount;
        cv =
            cw.visitMethod(
                ACC_PUBLIC,
                "invoke",
                "(Lgroovy/lang/MetaMethod;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;",
                null,
                null);
        helper = new BytecodeHelper(cv);

        cv.visitVarInsn(ALOAD, minMethodIndex);
        cv.visitMethodInsn(INVOKEVIRTUAL, "groovy/lang/MetaMethod", "getMethodIndex", "()I");
        Label defaultLabel = new Label();
        Label[] labels = new Label[methodCount];
        for (int i = 0; i < methodCount; i++) {
            labels[i] = new Label();
        }

        cv.visitTableSwitchInsn(minMethodIndex, maxMethodIndex, defaultLabel, labels);

        for (int i = 0; i < methodCount; i++) {
            labels[i] = new Label();

            cv.visitLabel(labels[i]);
            MetaMethod method = (MetaMethod) methods.get(i);
            method.setMethodIndex(i);
            invokeMethod(method);
            cv.visitInsn(ARETURN);
        }

        cv.visitLabel(defaultLabel);
        cv.visitVarInsn(ALOAD, 0);
        cv.visitVarInsn(ALOAD, 1);
        cv.visitVarInsn(ALOAD, 2);
        cv.visitVarInsn(ALOAD, 3);
        cv.visitMethodInsn(
            INVOKEVIRTUAL,
            "org/codehaus/groovy/classgen/DummyReflector",
            "noSuchMethod",
            "(Lgroovy/lang/MetaMethod;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");
        cv.visitInsn(ARETURN);
        cv.visitMaxs(4, 4);
        cw.visitEnd();
    }

    protected void invokeMethod(MetaMethod method) {
        /** simple
        cv.visitVarInsn(ALOAD, 2);
        cv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;");
        */
        String type = helper.getClassInternalName(method.getDeclaringClass().getName());
        String descriptor = helper.getMethodDescriptor(method.getReturnType().getName(), method.getParameterTypes());

        if (method.isStatic()) {
            loadParameters(method);
            cv.visitMethodInsn(INVOKESTATIC, type, method.getName(), descriptor);
        }
        else {
            cv.visitVarInsn(ALOAD, 2);
            loadParameters(method);
            cv.visitMethodInsn(INVOKEVIRTUAL, type, method.getName(), descriptor);
        }

        helper.toObject(method.getReturnType());
    }

    protected void loadParameters(MetaMethod method) {
        Class[] parameters = method.getParameterTypes();
        int size = parameters.length;
        for (int i = 0; i < size; i++) {
            cv.visitVarInsn(ALOAD, 3);
            helper.pushConstant(i);
            cv.visitInsn(AALOAD);
        }
    }
}
