package indy.perf;

import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.MethodSorters;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;

import static indy.perf.IndyPerfUtil.executeTests;
import static indy.perf.IndyPerfUtil.perf;

@FixMethodOrder( MethodSorters.NAME_ASCENDING )
public class SimpleReflectiveTest {
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
    public void reflectiveCall_t1() {
        execute_reflectiveCall(null);
    }

    @Test
    public void reflectiveCall_t2() {
        execute_reflectiveCall("reflectiveCall");
    }

    public void execute_reflectiveCall(String name) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    Method m = SimpleReflectiveTest.class.getMethod("foo", int.class);
                    m.invoke(null, 1);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        perf(r,name);
    }

    @Test
    public void reflectiveCallCached_t1() {
        execute_reflectiveCallCached(null);
    }

    @Test
    public void reflectiveCallCached_t2() {
        execute_reflectiveCallCached("reflectiveCallCached");
    }

    public void execute_reflectiveCallCached(String name) {
        Runnable r = new Runnable() {
            Method m = null;
            @Override
            public void run() {
                try {
                    if (m == null) {
                        m = SimpleReflectiveTest.class.getMethod("foo", int.class);
                    }
                    m.invoke(null, 1);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        perf(r, name);
    }

    public static void main(String[] args) {
        executeTests(SimpleReflectiveTest.class);
    }
}
