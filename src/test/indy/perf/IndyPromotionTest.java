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
public class IndyPromotionTest {
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

    private static MethodHandle INVOKER = getHandle(IndyPromotionTest.class, "invoker");
    private static MethodHandle BS2 = getHandle(IndyPromotionTest.class, "bootstrapStage2");

    @SuppressWarnings("unused")
    public static CallSite bootstrap(MethodHandles.Lookup caller, String name, MethodType type) {
        MutableCallSite callsite = new MutableCallSite(type);
        callsite.setTarget(INVOKER.bindTo(callsite));
        return callsite;
    }

    @SuppressWarnings("unused")
    public static int invoker(MutableCallSite callsite, int i) {
        try {
            Method m = IndyPromotionTest.class.getMethod("foo", int.class);
            callsite.setTarget(BS2.bindTo(callsite));
            return (int) m.invoke(null, i);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unused")
    public static int bootstrapStage2(MutableCallSite callsite, int i) {
        MethodHandle handle;
        try {
            handle = LOOKUP.findStatic(IndyDirectTest.class, "foo", callsite.type());
            callsite.setTarget(handle);
            return (int) handle.invokeExact(i);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void t1_indyPromotion() throws ReflectiveOperationException, IOException {
        execute(null);
    }

    @Test
    public void t2_indyPromotion() throws ReflectiveOperationException, IOException {
        execute("t2_indyPromotion");
    }

    public void execute(String name) throws ReflectiveOperationException, IOException {
        Class<? extends Runnable> c = writeIndyCall(getBSM(IndyPromotionTest.class).handle, "foo", "(I)I", (mv -> {
            mv.visitInsn(Opcodes.ICONST_1);
        }));
        Runnable r = c.getDeclaredConstructor().newInstance();
        perf(r, name);
    }

    public static void main(String[] args) {
        executeTests(IndyPromotionTest.class);
    }

}
