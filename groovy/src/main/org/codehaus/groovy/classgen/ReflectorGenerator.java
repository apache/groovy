/*
 * Copyright 2003-2007 the original author or authors.
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
package org.codehaus.groovy.classgen;

import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.CachedMethod;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.Iterator;
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

    private static List m_names = new ArrayList();

    private static String get_m_name (int i) {
      while (i >= m_names.size()) {
        m_names.add("m" + m_names.size());
      }

      return (String) m_names.get(i);
    }

    public ReflectorGenerator(List methods) {
        this.methods = new ArrayList(methods.size());
        for (Iterator it = methods.iterator(); it.hasNext(); ) {
            CachedMethod method = (CachedMethod) it.next();
            if (method.canBeCalledByReflector())
              this.methods.add(method);
        }
    }

    public void generate(ClassVisitor cv, String className) {
        this.cv = cv;

        classInternalName = BytecodeHelper.getClassInternalName(className);
        cv.visit(ClassGenerator.asmJDKVersion, ACC_PUBLIC + ACC_SUPER, classInternalName, null, "org/codehaus/groovy/runtime/Reflector", null);

        cv.visitField(ACC_PUBLIC + ACC_STATIC, "accessor", "Ljava/lang/Object;", null, null);

        MethodVisitor mvInit = cv.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mvInit.visitVarInsn(ALOAD, 0);
        mvInit.visitMethodInsn(INVOKESPECIAL, "org/codehaus/groovy/runtime/Reflector", "<init>", "()V");
        mvInit.visitInsn(RETURN);
        mvInit.visitMaxs(1, 1);

        MethodVisitor mvClinit = cv.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
        mvClinit.visitTypeInsn(NEW, classInternalName);
        mvClinit.visitInsn(DUP);
        mvClinit.visitMethodInsn(INVOKESPECIAL, classInternalName, "<init>", "()V");
        mvClinit.visitFieldInsn(PUTSTATIC, classInternalName, "accessor", "Ljava/lang/Object;");
        mvClinit.visitInsn(RETURN);
        mvClinit.visitMaxs(1, 1);

        generateInvokeMethod();

        cv.visitEnd();
    }

    protected void generateInvokeMethod() {
        int methodCount = methods.size();

        MethodVisitor mv = cv.visitMethod(
                ACC_PUBLIC,
                "invoke",
                "(Lorg/codehaus/groovy/reflection/CachedMethod;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;",
                null,
                null);

        // load parameters for the helper method call
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ALOAD, 3);

        // get method number for switch
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/codehaus/groovy/reflection/CachedMethod", "getMethodIndex", "()I");

        // init meta methods with number
        Label defaultLabel = new Label();
        Label[] labels = new Label[methodCount];
        int[] indices = new int[methodCount];
        for (int i = 0; i < methodCount; i++) {
            labels[i] = new Label();
            CachedMethod method = (CachedMethod) methods.get(i);
            method.setMethodIndex(indices[i] = i+1);
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
                    get_m_name(i),
                    "(Lorg/codehaus/groovy/reflection/CachedMethod;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");
            mv.visitInsn(ARETURN);
        }

        // call helper for error
        mv.visitLabel(defaultLabel);
        mv.visitMethodInsn(
                INVOKEVIRTUAL,
                classInternalName,
                "noSuchMethod",
                "(Lorg/codehaus/groovy/reflection/CachedMethod;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");
        mv.visitInsn(ARETURN);
        // end method
        mv.visitMaxs(4, 4);
        mv.visitEnd();

        // create helper methods m*
        for (int i = 0; i < methodCount; i++) {
            mv = cv.visitMethod(
                    ACC_PRIVATE,
                    get_m_name(i),
                    "(Lorg/codehaus/groovy/reflection/CachedMethod;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;",
                    null,
                    null);
            helper = new BytecodeHelper(mv);

            CachedMethod method = (CachedMethod) methods.get(i);
            invokeMethod(method, mv);
            if (method.getReturnType() == void.class) {
                mv.visitInsn(ACONST_NULL);
            }
            mv.visitInsn(ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }

    protected void invokeMethod(CachedMethod method, MethodVisitor mv) {
        // compute class to make the call on
        Class callClass = method.getDeclaringClass().getTheClass();
        boolean useInterface = callClass.isInterface();
//        if (callClass == null) {
//            callClass = method.getCallClass();
//        } else {
//            useInterface = true;
//        }
        // get bytecode information
        String type = BytecodeHelper.getClassInternalName(callClass.getName());
        String descriptor = BytecodeHelper.getMethodDescriptor(method.getReturnType(), method.getNativeParameterTypes());

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

    protected void loadParameters(CachedMethod method, int argumentIndex, MethodVisitor mv) {
        CachedClass[] parameters = method.getParameterTypes();
        int size = parameters.length;
        for (int i = 0; i < size; i++) {
            // unpack argument from Object[]
            mv.visitVarInsn(ALOAD, argumentIndex);
            helper.pushConstant(i);
            mv.visitInsn(AALOAD);

            // cast argument to parameter class, inclusive unboxing
            // for methods with primitive types
            Class type = parameters[i].getTheClass();
            if (type.isPrimitive()) {
                helper.unbox(type);
            } else {
                helper.doCast(type);
            }
        }
    }
}
