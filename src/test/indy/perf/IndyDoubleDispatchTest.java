package indy.perf;

import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.MethodSorters;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.lang.invoke.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;

import static indy.perf.IndyPerfUtil.*;

@FixMethodOrder( MethodSorters.NAME_ASCENDING )
public class IndyDoubleDispatchTest {
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

    private static MethodHandle SELECTOR = getHandle(IndyDoubleDispatchTest.class, "selector");

    @SuppressWarnings("unused")
    public static CallSite bootstrap(MethodHandles.Lookup caller, String name, MethodType type) {
        MutableCallSite callsite = new MutableCallSite(type);
        MethodHandle handle = MethodHandles.insertArguments(SELECTOR, 0, caller, callsite, name);
        callsite.setTarget(handle);
        return callsite;
    }

    @SuppressWarnings("unused")
    public static int selector(MethodHandles.Lookup caller, MutableCallSite callsite, String name, int i) {
        MethodHandle handle;
        try {
            handle = caller.findStatic(IndyDoubleDispatchTest.class, name, callsite.type());
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        callsite.setTarget(handle);
        try {
            return (int) handle.invokeExact(i);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void t1_indyDoubleDispatch() throws ReflectiveOperationException, IOException {
        execute(null);
    }

    @Test
    public void t2_indyDoubleDispatch() throws ReflectiveOperationException, IOException {
        execute("t2_indyDoubleDispatch");
    }

    public void execute(String name) throws ReflectiveOperationException, IOException {
        Class<? extends Runnable> c = writeIndyCall(getBSM(IndyDoubleDispatchTest.class).handle, "foo", "(I)I", (mv -> {
            mv.visitInsn(Opcodes.ICONST_1);
        }));
        Runnable r = c.getDeclaredConstructor().newInstance();
        perf(r, name);
    }
    public static void main(String[] args) {
        executeTests(IndyDoubleDispatchTest.class);
    }
}
