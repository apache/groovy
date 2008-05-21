package org.codehaus.groovy.reflection;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.MethodVisitor;
import org.codehaus.groovy.classgen.BytecodeHelper;
import org.codehaus.groovy.ast.ClassHelper;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Constructor;

public class MethodHandleFactory implements Opcodes{

    public static MethodHandle unreflect (Method method) {
        if (SunClassLoader.sunVM != null || checkAccessable(method)) {
          return createCompiledMethodHandle (method, ClassInfo.getClassInfo(method.getDeclaringClass()).getArtifactClassLoader());
        }

        return new ReflectiveMethodHandle(method);
    }

    private static MethodHandle unreflect (Method method, ClassLoaderForClassArtifacts loader) {
        if (SunClassLoader.sunVM != null || checkAccessable(method))
          return createCompiledMethodHandle (method, loader);

        return new ReflectiveMethodHandle(method);
    }

    private static boolean checkAccessable(Method method) {
        if (!Modifier.isPublic(method.getDeclaringClass().getModifiers()))
          return false;

        if (!Modifier.isPublic(method.getModifiers()))
          return false;

        for (Class paramType : method.getParameterTypes())
            if (!Modifier.isPublic(paramType.getModifiers()))
              return false;

        return true;
    }

    public static void genLoadParameters(int argumentIndex, MethodVisitor mv, BytecodeHelper helper, Method method) {
        Class<?>[] parameters = method.getParameterTypes();
        int size = parameters.length;
        for (int i = 0; i < size; i++) {
            // unpack argument from Object[]
            mv.visitVarInsn(Opcodes.ALOAD, argumentIndex);
            helper.pushConstant(i);
            mv.visitInsn(Opcodes.AALOAD);

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

    public static void genLoadParametersDirect(int argumentIndex, MethodVisitor mv, BytecodeHelper helper, Method method) {
        Class<?>[] parameters = method.getParameterTypes();
        int size = parameters.length;
        for (int i = 0; i < size; i++) {
            mv.visitVarInsn(Opcodes.ALOAD, argumentIndex+i);

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

    public static void genLoadParametersPrimitiveDirect(int argumentIndex, MethodVisitor mv, BytecodeHelper helper, Method method) {
        Class<?>[] parameters = method.getParameterTypes();
        int size = parameters.length;
        int idx = 0;
        for (int i = 0; i < size; i++, idx++) {
            Class type = parameters[i];
            if (type == double.class) {
                mv.visitVarInsn(DLOAD, idx++);
            } else if (type == float.class) {
                mv.visitVarInsn(FLOAD, idx);
            } else if (type == long.class) {
                mv.visitVarInsn(LLOAD, idx++);
            } else if (
                   type == boolean.class
                || type == char.class
                || type == byte.class
                || type == int.class
                || type == short.class) {
                mv.visitVarInsn(ILOAD, idx);
            } else {
                mv.visitVarInsn(ALOAD, idx);
                helper.doCast(type);
            }
        }
    }

    private static class ReflectiveMethodHandle extends MethodHandle {
        private final Method method;

        public ReflectiveMethodHandle(Method method) {
            this.method = method;
            method.setAccessible(true);
        }

        public Object invoke (Object receiver, Object [] args) throws Throwable{
            return  method.invoke(receiver, args);
        }
    }

    private static MethodHandle createCompiledMethodHandle(Method method, ClassLoaderForClassArtifacts loader) {
        try {
          Constructor c = compileMethodHandle(method, loader);
          if (c != null)
            return (MethodHandle) c.newInstance();
        } catch (Throwable e) { //
        }
        return new ReflectiveMethodHandle(method);
    }

    private static Constructor compileMethodHandle(Method cachedMethod, ClassLoaderForClassArtifacts loader) {
        ClassWriter cw = new ClassWriter(true);
        final String name = loader.createClassName(cachedMethod);
        final byte[] bytes = genMethodHandle(cachedMethod, cw, name);
        return loader.defineClassAndGetConstructor(name, bytes);
    }

    private static byte[] genMethodHandle(Method method, ClassWriter cw, String name) {
        cw.visit(Opcodes.V1_4, Opcodes.ACC_PUBLIC, name.replace('.','/'), null, "org/codehaus/groovy/reflection/MethodHandle", null);

        genConstructor(cw, "org/codehaus/groovy/reflection/MethodHandle");

        genInvokeXxxWithArray(cw, method);
        genInvokeWithFixedParams(cw, method);
        genInvokeWithFixedPrimitiveParams(cw, method);

        cw.visitEnd();

        return cw.toByteArray();
    }

    private static void genConstructor(ClassWriter cw, final String superClass) {
        MethodVisitor mv;
        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, superClass, "<init>", "()V");
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    public static void genInvokeXxxWithArray(ClassWriter cw, Method method) {
        MethodVisitor mv;
        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", null, EXCEPTIONS);
        mv.visitCode();

        BytecodeHelper helper = new BytecodeHelper(mv);

        Class callClass = method.getDeclaringClass();
        boolean useInterface = callClass.isInterface();

        String type = BytecodeHelper.getClassInternalName(callClass.getName());
        String descriptor = BytecodeHelper.getMethodDescriptor(method.getReturnType(), method.getParameterTypes());

        // make call
        if (Modifier.isStatic(method.getModifiers())) {
            genLoadParameters(2, mv, helper, method);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, type, method.getName(), descriptor);
        } else {
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            helper.doCast(callClass);
            genLoadParameters(2, mv, helper, method);
            mv.visitMethodInsn((useInterface) ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL, type, method.getName(), descriptor);
        }

        helper.box(method.getReturnType());
        if (method.getReturnType() == void.class) {
            mv.visitInsn(Opcodes.ACONST_NULL);
        }

        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private static void genInvokeWithFixedParams(ClassWriter cw, Method method) {
        MethodVisitor mv;
        final int pc = method.getParameterTypes().length;
        if (pc <= 4)
        {
            StringBuilder pdescb = new StringBuilder();
            for (int i = 0; i != pc; ++i)
              pdescb.append("Ljava/lang/Object;");

            String pdesc = pdescb.toString();

            mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "invoke", "(Ljava/lang/Object;" + pdesc + ")Ljava/lang/Object;", null, EXCEPTIONS);
            mv.visitCode();

            BytecodeHelper helper = new BytecodeHelper(mv);

            Class callClass = method.getDeclaringClass();
            boolean useInterface = callClass.isInterface();

            String type = BytecodeHelper.getClassInternalName(callClass.getName());
            String descriptor = BytecodeHelper.getMethodDescriptor(method.getReturnType(), method.getParameterTypes());

            // make call
             if (Modifier.isStatic(method.getModifiers())) {
                MethodHandleFactory.genLoadParametersDirect(2, mv, helper, method);
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, type, method.getName(), descriptor);
            } else {
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                helper.doCast(callClass);
                MethodHandleFactory.genLoadParametersDirect(2, mv, helper, method);
                mv.visitMethodInsn((useInterface) ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL, type, method.getName(), descriptor);
            }

            helper.box(method.getReturnType());
            if (method.getReturnType() == void.class) {
                mv.visitInsn(Opcodes.ACONST_NULL);
            }

            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }

    private static void genInvokeWithFixedPrimitiveParams(ClassWriter cw, Method method) {
        MethodVisitor mv;
        final Class<?>[] pt = method.getParameterTypes();
        final int pc = pt.length;
        if (pc > 0 && pc <= 3)
        {
            StringBuilder pdescb = new StringBuilder();
            boolean hasPrimitive = false;
            for (int i = 0; i != pc; ++i)
              if (pt[i].isPrimitive()) {
                  hasPrimitive = true;
                  pdescb.append(BytecodeHelper.getTypeDescription(pt[i]));
              }
              else
                pdescb.append("Ljava/lang/Object;");

            if (!hasPrimitive)
              return;

            String pdesc = pdescb.toString();

            mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "invoke", "(Ljava/lang/Object;" + pdesc + ")Ljava/lang/Object;", null, EXCEPTIONS);
            mv.visitCode();

            BytecodeHelper helper = new BytecodeHelper(mv);

            Class callClass = method.getDeclaringClass();
            boolean useInterface = callClass.isInterface();

            String type = BytecodeHelper.getClassInternalName(callClass.getName());
            String descriptor = BytecodeHelper.getMethodDescriptor(method.getReturnType(), method.getParameterTypes());

            // make call
             if (Modifier.isStatic(method.getModifiers())) {
                MethodHandleFactory.genLoadParametersPrimitiveDirect(2, mv, helper, method);
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, type, method.getName(), descriptor);
            } else {
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                helper.doCast(callClass);
                MethodHandleFactory.genLoadParametersPrimitiveDirect(2, mv, helper, method);
                mv.visitMethodInsn((useInterface) ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL, type, method.getName(), descriptor);
            }

            helper.box(method.getReturnType());
            if (method.getReturnType() == void.class) {
                mv.visitInsn(Opcodes.ACONST_NULL);
            }

            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }

    private static final String[] EXCEPTIONS = new String[] { "java/lang/Throwable" };
}
