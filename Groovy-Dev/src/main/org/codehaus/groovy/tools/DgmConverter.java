package org.codehaus.groovy.tools;

import org.codehaus.groovy.classgen.BytecodeHelper;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.CachedMethod;
import org.codehaus.groovy.reflection.ReflectionCache;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.FileOutputStream;
import java.io.IOException;

public class DgmConverter implements Opcodes{
    private static BytecodeHelper helper;

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        final CachedClass dgm = ReflectionCache.getCachedClass(DefaultGroovyMethods.class);
        final CachedMethod[] cachedMethods = dgm.getMethods();
        for (int i = 0, cur = 0; i < cachedMethods.length; i++) {
            CachedMethod method = cachedMethods[i];
            if (!method.isStatic() || !method.isPublic())
              continue;

            if (method.getParameterTypes().length == 0)
              continue;

            ClassWriter cw = new ClassWriter(true);
            final String className = "org/codehaus/groovy/runtime/dgm$" + cur;
            cw.visit(V1_3,ACC_PUBLIC, className,null,"org/codehaus/groovy/reflection/GeneratedMetaMethod", null);

            MethodVisitor mv;
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "org/codehaus/groovy/reflection/GeneratedMetaMethod", "<init>", "()V");
            mv.visitInsn(RETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();

            final Class returnType = method.getReturnType();
            final String methodDescriptor = BytecodeHelper.getMethodDescriptor(returnType, method.getNativeParameterTypes());

            mv = cw.visitMethod(ACC_PUBLIC, "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", null, null);
            helper = new BytecodeHelper(mv);
            mv.visitCode();
            mv.visitVarInsn(ALOAD,1);
            helper.doCast(method.getParameterTypes()[0].getTheClass());
            loadParameters(method,2,mv);
            mv.visitMethodInsn(INVOKESTATIC, "org/codehaus/groovy/runtime/DefaultGroovyMethods", method.getName(), methodDescriptor);
            helper.box(returnType);
            if (method.getReturnType() == void.class) {
                mv.visitInsn(ACONST_NULL);
            }
            mv.visitInsn(ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();

            mv = cw.visitMethod(ACC_PUBLIC + ACC_FINAL, "doMethodInvoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", null, null);
            helper = new BytecodeHelper(mv);
            mv.visitCode();
            if (method.getParamsCount() == 2 && method.getParameterTypes()[0].isNumber && method.getParameterTypes()[1].isNumber) {
                mv.visitVarInsn(ALOAD,1);
                helper.doCast(method.getParameterTypes()[0].getTheClass());

                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKEVIRTUAL, className, "getParameterTypes", "()[Lorg/codehaus/groovy/reflection/CachedClass;");
                mv.visitInsn(ICONST_0);
                mv.visitInsn(AALOAD);
                mv.visitVarInsn(ALOAD, 2);
                mv.visitInsn(ICONST_0);
                mv.visitInsn(AALOAD);
                mv.visitMethodInsn(INVOKEVIRTUAL, "org/codehaus/groovy/reflection/CachedClass", "coerceArgument", "(Ljava/lang/Object;)Ljava/lang/Object;");

                // cast argument to parameter class, inclusive unboxing
                // for methods with primitive types
                Class type = method.getParameterTypes()[1].getTheClass();
                if (type.isPrimitive()) {
                    helper.unbox(type);
                } else {
                    helper.doCast(type);
                }
            }
            else {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, 2);
                mv.visitMethodInsn(INVOKEVIRTUAL, className, "coerceArgumentsToClasses", "([Ljava/lang/Object;)[Ljava/lang/Object;");
                mv.visitVarInsn(ASTORE, 2);
                mv.visitVarInsn(ALOAD,1);
                helper.doCast(method.getParameterTypes()[0].getTheClass());
                loadParameters(method,2,mv);
            }
            mv.visitMethodInsn(INVOKESTATIC, "org/codehaus/groovy/runtime/DefaultGroovyMethods", method.getName(), methodDescriptor);
            helper.box(returnType);
            if (method.getReturnType() == void.class) {
                mv.visitInsn(ACONST_NULL);
            }
            mv.visitInsn(ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();

            if (method.getParamsCount() == 2 && method.getParameterTypes()[0].isNumber && method.getParameterTypes()[1].isNumber) {
                // 1 param meta method
                mv = cw.visitMethod(ACC_PUBLIC, "isValidMethod", "([Ljava/lang/Class;)Z", null, null);
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 1);
                Label l0 = new Label();
                mv.visitJumpInsn(IFNULL, l0);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKEVIRTUAL, className, "getParameterTypes", "()[Lorg/codehaus/groovy/reflection/CachedClass;");
                mv.visitInsn(ICONST_0);
                mv.visitInsn(AALOAD);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitInsn(ICONST_0);
                mv.visitInsn(AALOAD);
                mv.visitMethodInsn(INVOKEVIRTUAL, "org/codehaus/groovy/reflection/CachedClass", "isAssignableFrom", "(Ljava/lang/Class;)Z");
                Label l1 = new Label();
                mv.visitJumpInsn(IFEQ, l1);
                mv.visitLabel(l0);
                mv.visitInsn(ICONST_1);
                Label l2 = new Label();
                mv.visitJumpInsn(GOTO, l2);
                mv.visitLabel(l1);
                mv.visitInsn(ICONST_0);
                mv.visitLabel(l2);
                mv.visitInsn(IRETURN);
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }

            mv = cw.visitMethod(ACC_PUBLIC, "$markerMethod$" + method.getName(), methodDescriptor, null, null);
            mv.visitCode();
            if (returnType == Void.TYPE)
              mv.visitInsn(RETURN);
            else {
              if (returnType.isPrimitive()) {
                  if (returnType == float.class) {
                      mv.visitInsn(FCONST_0);
                      mv.visitInsn(FRETURN);
                  }
                  else
                  if (returnType == double.class) {
                      mv.visitInsn(DCONST_0);
                      mv.visitInsn(DRETURN);
                  }
                  else
                  if (returnType == long.class) {
                      mv.visitInsn(LCONST_0);
                      mv.visitInsn(LRETURN);
                  }
                  else
                  if (returnType == float.class) {
                      mv.visitInsn(FCONST_0);
                      mv.visitInsn(FRETURN);
                  }
                  else {
                      mv.visitInsn(ICONST_0);
                      mv.visitInsn(IRETURN);
                  }
              }
              else {
                  mv.visitInsn(ACONST_NULL);
                  mv.visitInsn(ARETURN);
              }
            }
            mv.visitMaxs(0, 0);
            mv.visitEnd();

            cw.visitEnd();

            final byte[] bytes = cw.toByteArray();
            final FileOutputStream fileOutputStream = new FileOutputStream("target/classes/" + className + ".class");
            fileOutputStream.write(bytes);
            fileOutputStream.flush();
            fileOutputStream.close();

            cur++;
        }
    }

    protected static void loadParameters(CachedMethod method, int argumentIndex, MethodVisitor mv) {
        CachedClass[] parameters = method.getParameterTypes();
        int size = parameters.length-1;
        for (int i = 0; i < size; i++) {
            // unpack argument from Object[]
            mv.visitVarInsn(ALOAD, argumentIndex);
            helper.pushConstant(i);
            mv.visitInsn(AALOAD);

            // cast argument to parameter class, inclusive unboxing
            // for methods with primitive types
            Class type = parameters[i+1].getTheClass();
            if (type.isPrimitive()) {
                helper.unbox(type);
            } else {
                helper.doCast(type);
            }
        }
    }
}
