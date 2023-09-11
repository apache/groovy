package indy.perf;

import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.lang.invoke.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;

import static indy.perf.IndyPerfUtil.*;

@FixMethodOrder( MethodSorters.NAME_ASCENDING )
public class RuntimeCallsiteGenTest {
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

    @Test
    public void t1_callSiteGen() throws ReflectiveOperationException, IOException {
        execute(null);
    }

    @Test
    public void t2_callSiteGen() throws ReflectiveOperationException, IOException {
        execute("t2_runtimeCallSiteGen");
    }

    public static class DefaultCallSite implements CallSiteGen {
        private CallSiteGen[] array;
        private int index;
        public DefaultCallSite(CallSiteGen[] array, int index) {
            this.array = array;
            this.index = index;
            array[index] = this;
        }
        @Override
        public Object call(Object[] args) {
            Class<CallSiteGen> clazz = IndyPerfUtil.writeCallSiteImpl(CallSiteGen.class, RuntimeCallsiteGenTest.class);
            CallSiteGen target = null;
            try {
                target = clazz.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new AssertionError(e);
            }
            array[index] = target;
            return target.call(args);
        }
    }

    public interface CallSiteGen {
        Object call(Object[] args);
    }

    public CallSiteGen[] CSA = new CallSiteGen[1];

    public void execute(String name) throws ReflectiveOperationException, IOException {
        CSA[0] = new DefaultCallSite(CSA, 0);
        Runnable r = new Runnable() {
            @Override
            public void run() {
                CSA[0].call(new Object[]{1});
            }
        };
        perf(r, name);
    }

    public static void main(String[] args) {
        executeTests(RuntimeCallsiteGenTest.class);
    }

}
