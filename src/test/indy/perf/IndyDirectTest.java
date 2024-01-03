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
public class IndyDirectTest {
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

    @SuppressWarnings("unused")
    public static CallSite bootstrap(MethodHandles.Lookup caller, String name, MethodType type) {
        MethodHandle handle;
        try {
            handle = caller.findStatic(IndyDirectTest.class, name, type);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return new MutableCallSite(handle);
    }

    @Test
    public void t1_indyDirectCall1() throws ReflectiveOperationException, IOException {
        execute(null);
    }

    @Test
    public void t2_indyDirectCall2() throws ReflectiveOperationException, IOException {
        execute("t2_indyDirectCall2");
    }

    public void execute(String name) throws ReflectiveOperationException, IOException {
        Class<? extends Runnable> c = writeIndyCall(getBSM(IndyDirectTest.class).handle, "foo", "(I)I", (mv -> {
            mv.visitInsn(Opcodes.ICONST_1);
        }));
        Runnable r = c.getDeclaredConstructor().newInstance();
        perf(r, name);
    }

    public static void main(String[] args) {
        executeTests(IndyDirectTest.class);
    }

}
