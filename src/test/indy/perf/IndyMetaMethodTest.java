package indy.perf;

import groovy.lang.GroovyObject;
import groovy.lang.GroovySystem;
import groovy.lang.MetaClass;
import groovy.lang.MetaClassRegistry;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.classgen.asm.BytecodeHelper;
import org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.MethodSorters;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;

import static indy.perf.IndyPerfUtil.*;
//125467
//630155 - 740342
@FixMethodOrder( MethodSorters.NAME_ASCENDING )
public class IndyMetaMethodTest {
    @Rule
    public final TestRule watchman = new TestWatcher() {
        @Override
        protected void starting(Description description) {
            System.out.println(description + "\n\n\tstarted.");
        }

        @Override
        protected void finished(Description description) {
            System.out.println(description + "\tdone.");
        }
    };

    public static int foo(int i) {
        return i;
    }

    private final static MethodHandle MC_INVOKE, GET_MC_CALL, BASE_HANLDE_CALL, BASE_HANLDE;
    static {
        //Object invokeMethod(Class sender, Object receiver, String methodName, Object[] arguments, boolean isCallToSuper, boolean fromInsideClass);
        try {
            MC_INVOKE = LOOKUP.findVirtual(MetaClass.class, "invokeStaticMethod", MethodType.methodType(Object.class, Object.class, String.class, Object[].class));
            GET_MC_CALL = LOOKUP.findStatic(IndyMetaMethodTest.class, "getMetaClass", MethodType.methodType(MetaClass.class, MyCallSite.class, Object.class));

            MethodType type = MethodType.methodType(int.class, Class.class, int.class);
            MethodHandle invoke = MC_INVOKE.asCollector(3, Object[].class, type.parameterCount()-1);
            BASE_HANLDE = invoke;

            MethodHandle invokeWithGetMc = MethodHandles.dropArguments(invoke, 1, MyCallSite.class);
            invokeWithGetMc = MethodHandles.foldArguments(invokeWithGetMc, GET_MC_CALL);
            BASE_HANLDE_CALL = invokeWithGetMc;
        } catch (Exception e) {
            throw new GroovyBugError(e);
        }
    }

    final static MetaClass MC;
    static {
        var mc = GroovySystem.getMetaClassRegistry().getMetaClass(IndyMetaMethodTest.class);
        mc.initialize();
        MC = mc;
    }

    private static class MyCallSite extends MutableCallSite {
        public final MethodHandle tail;
        public final String name;
        private MyCallSite(MethodType type, String name, MethodHandle tail) {
            super(type);
            this.tail = tail;
            this.name = name;
        }

    }

    public static MetaClass getMetaClass(MyCallSite callsite, Object receiver) {
        //MetaClass mc = GroovySystem.getMetaClassRegistry().getMetaClass((Class) receiver);
        /*MethodHandle handle = MethodHandles.insertArguments(callsite.tail, 0, mc);
        handle = MethodHandles.insertArguments(handle, 1, callsite.name);
        handle = handle.asType(callsite.getTarget().type());
        callsite.setTarget(handle);*/
        //return mc;
        return MC;
    }


    @SuppressWarnings("unused")
    public static CallSite bootstrap(MethodHandles.Lookup caller, String name, MethodType type) {

/*
        MethodHandle invoke = MethodHandles.insertArguments(MC_INVOKE, 2, name);
        invoke = invoke.asCollector(2, Object[].class, type.parameterCount()-1);

        MethodHandle invokeWithGetMc = MethodHandles.dropArguments(invoke, 1, MyCallSite.class);
        invokeWithGetMc = MethodHandles.foldArguments(invokeWithGetMc, GET_MC_CALL);


        MyCallSite callsite = new MyCallSite(type, invoke);
        invokeWithGetMc = MethodHandles.insertArguments(invokeWithGetMc, 0, callsite);
        MethodHandle handle = invokeWithGetMc.asType(type);
        */

        MethodHandle handle;

        MyCallSite callsite = new MyCallSite(type, name, BASE_HANLDE);/*
        MethodHandle handle = MethodHandles.insertArguments(BASE_HANLDE_CALL, 0, callsite);
        handle = MethodHandles.insertArguments(handle, 1, name);
        handle = handle.asType(type);
*/
        try {
            handle = caller.findStatic(IndyMetaMethodTest.class, name, type.dropParameterTypes(0,1));
            handle = MethodHandles.dropArguments(handle, 0, Class.class);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        callsite.setTarget(handle);
        return callsite;
    }

    @Test
    public void t1_indyDirectMetaMethodCall() throws ReflectiveOperationException, IOException {
        execute(null);
    }

    @Test
    public void t2_indyDirectMetaMethodCall() throws ReflectiveOperationException, IOException {
        execute("t2_indyDirectMetaMethodCall2");
    }

    public void execute(String name) throws ReflectiveOperationException, IOException {
        Type thisClass = Type.getType(this.getClass());
        Class<? extends Runnable> c = writeIndyCall(getBSM(IndyMetaMethodTest.class).handle, "foo",
            "(Ljava/lang/Class;I)I",
            (mv -> {
                mv.visitLdcInsn(thisClass);
                mv.visitInsn(Opcodes.ICONST_1);
            }));
        Runnable r = c.getDeclaredConstructor().newInstance();
        GroovySystem.getMetaClassRegistry().getMetaClass(c).initialize();
        perf(r, name);
    }

    public static void main(String[] args) {
        executeTests(IndyMetaMethodTest.class);
    }

}
