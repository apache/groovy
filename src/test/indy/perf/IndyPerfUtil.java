package indy.perf;

import org.junit.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.*;
import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Consumer;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

public class IndyPerfUtil {
    private static final int WARM_UP_ITERATIONS = 100;
    private static final int TEST_ITERATIONS = 10;
    private static final int HOT_LOOP_ITERATIONS = 100_000;
    public static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static boolean WRITE_FILE = true;

    private static long getTotal(long[] times, int length) {
        long t_diff = 0;
        for (int i=0; i<length; i+=2) {
            t_diff += times[i+1] - times[i];
        }
        return t_diff;
    }

    public static void perf(Runnable r, String name) {
        System.out.println("------------------------------------");
        long[] warmupTimes = new long[WARM_UP_ITERATIONS*2];
        long time0 = System.nanoTime();
        r.run();
        long time1 = System.nanoTime();
        warmupTimes[0] = time0;
        warmupTimes[1] = time1;
        print("init time", time0, time1, 1, 0);

        for (int i = 0; i< WARM_UP_ITERATIONS-1; i++) {
            long w1 = System.nanoTime();
            r.run();
            long w2 = System.nanoTime();
            int index = (i+1)*2;
            warmupTimes[index] = w1;
            warmupTimes[index+1] = w2;
            print("warmup time per iteration", w1, w2, 1, getTotal(warmupTimes, index+2));
        }
        System.out.println("------------------------------------");
        long t_diff = getTotal(warmupTimes, warmupTimes.length);
        System.out.println("total warmup time: " + t_diff);

        if (name != null && WRITE_FILE) {
            try(FileOutputStream fo = new FileOutputStream("perf.data", true)) {
                PrintWriter pw = new PrintWriter(fo);
                pw.println();
                pw.println(name);
                long tdiff = 0;
                for (int i=0; i<warmupTimes.length; i+=2) {
                    long diff = warmupTimes[i+1]-warmupTimes[i];
                    tdiff += diff;
                    pw.print(diff);
                    pw.print('\t');
                    pw.println(tdiff);
                }
                pw.println();
                pw.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


        System.out.println("------------------------------------");
        for (int j = 0; j< TEST_ITERATIONS; j++) {
            long timeStart = System.nanoTime();
            for (int i = 0; i < HOT_LOOP_ITERATIONS; i++) {
                r.run();
            }
            long timeEnd = System.nanoTime();
            print("avg time in iteration " + j, timeStart, timeEnd, HOT_LOOP_ITERATIONS, 0);
        }
        System.out.println("------------------------------------");
    }

    public static Class<? extends Runnable> writeIndyCall(
        Handle bootstrap, String message, String messageSignature, Consumer<MethodVisitor> argWriter
    ) throws IOException {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        cw.visit(
            Opcodes.V17, Opcodes.ACC_SUPER | Opcodes.ACC_PUBLIC,
            "indy/Runner",
            null, "java/lang/Object", new String[]{Runnable.class.getName().replace('.', '/')}
        );

        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "run", "()V", null, new String[0]);
        argWriter.accept(mv);
        mv.visitInvokeDynamicInsn(message, messageSignature, bootstrap);
        mv.visitInsn(Opcodes.POP);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitIntInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        cw.visitSource("Runner.dyn", null);
        cw.visitEnd();
        byte[] bytes = cw.toByteArray();
        try(FileOutputStream fos = new FileOutputStream("Runner.class")){
            fos.write(bytes);
            fos.flush();
        }
        return makeClass(bytes);
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> makeClass(byte[] code) {
        //return (Class<? extends Runnable>) LOOKUP.defineClass(code);
        var cl = new ClassLoader(IndyPerfUtil.class.getClassLoader()) {
            public Class<? extends Runnable> makeClass() {
                return (Class<? extends Runnable>) defineClass(null, code, 0, code.length);
            }
        };
        return (Class<T>) cl.makeClass();
    }

    private static void print(String text, long t1, long t2, int runs, long totalTime) {
        System.out.print(text + " with runs " + runs +": (ns) " + (t2-t1)/runs);
        if (totalTime>0) {
            System.out.print(" / total "+totalTime+" (ns)");
        }
        System.out.println();
    }

    public static BSM getBSM(Class<?> clazz) {
        Optional<Method> method = Arrays.stream(clazz.getMethods()).
            filter(m -> m.getName().equals("bootstrap")).
            filter(m -> Modifier.isStatic(m.getModifiers())).
            findFirst();
        if (method.isEmpty()) {
            throw new AssertionError("cannot find static bootstrap method");
        }
        MethodType type = MethodType.methodType(
            CallSite.class, MethodHandles.Lookup.class, String.class, MethodType.class
        );

        Handle handle = new Handle(Opcodes.H_INVOKESTATIC,
            clazz.getName().replace('.', '/'), "bootstrap", type.toMethodDescriptorString(), false
        );
        return new BSM(handle, method.get());
    }

    public static MethodHandle getHandle(Class<?> clazz, String name) {
        Optional<Method> method = Arrays.stream(clazz.getMethods()).
            filter(m -> m.getName().equals(name)).
            findFirst();
        try {
            return LOOKUP.unreflect(method.orElseThrow());
        } catch (IllegalAccessException iae){
            throw new AssertionError(iae);
        }
    }

    public static <T> Class<T> writeCallSiteImpl(Class<?> callsiteGen, Class<?> fooOwner) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        cw.visit(
            Opcodes.V17, Opcodes.ACC_SUPER | Opcodes.ACC_PUBLIC,
            "callsite/Runner",
            null, "java/lang/Object", new String[]{callsiteGen.getName().replace('.', '/')}
        );

        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "call", "([Ljava/lang/Object;)Ljava/lang/Object;", null, new String[0]);
        mv.visitIntInsn(Opcodes.ALOAD, 1);
        mv.visitInsn(Opcodes.ICONST_0);
        mv.visitInsn(Opcodes.AALOAD);
        mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Integer");
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, fooOwner.getName().replace('.', '/'), "foo", "(I)I", false);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitIntInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        cw.visitSource("CallsiteImpl.dyn", null);
        cw.visitEnd();
        byte[] bytes = cw.toByteArray();
        return makeClass(bytes);
    }

    public static void executeTests(Class<?> clazz) {
        try {
            final Object instance = clazz.getDeclaredConstructor().newInstance();

            Arrays.stream(clazz.getMethods()).
                filter(m -> m.getAnnotation(Test.class) != null).
                sorted(Comparator.comparing(Method::getName)).
                forEach(m -> {
                    try {
                        m.invoke(instance);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                });
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    public static class BSM {
        Handle handle;
        Method method;
        public BSM(Handle handle, Method method) {
            this.handle = handle;
            this.method = method;
        }
    }
}
