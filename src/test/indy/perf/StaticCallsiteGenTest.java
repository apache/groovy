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

import static indy.perf.IndyPerfUtil.executeTests;
import static indy.perf.IndyPerfUtil.perf;

@FixMethodOrder( MethodSorters.NAME_ASCENDING )
public class StaticCallsiteGenTest {
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
    public void t1_staticCallSiteGen() throws ReflectiveOperationException, IOException {
        execute(null);
    }

    @Test
    public void t2_staticCallSiteGen() throws ReflectiveOperationException, IOException {
        execute("t2_statiCallSiteGen");
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
            CallSiteGen target = new CallSiteGen() {
                @Override
                public Object call(Object[] args) {
                    return StaticCallsiteGenTest.foo((Integer) args[0]);
                }
            };
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
        executeTests(StaticCallsiteGenTest.class);
    }
}
