package groovy.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException
import javax.swing.SwingUtilities;

public class GroovySwingTestCase extends GroovyTestCase {
    private static boolean headless;

    /**
     * A boolean indicating if we are running in headless mode.
     * Check this flag if you believe your test may make use of AWT/Swing
     * features, then simply return rather than running your test.
     *
     * @return true if running in headless mode
     */
    public static boolean isHeadless() {
        return headless;
    }

    /**
     * Alias for isHeadless().
     *
     * @return true if running in headless mode
     */
    public static boolean getHeadless() {
        return isHeadless();
    }

    public static void testInEDT(Closure test) {
        Throwable exception = null
        if (headless) {
            return
        }
        SwingUtilities.invokeAndWait {
            try {
                test()
            } catch (Throwable t) {
                exception = t
            }
        }
        if (exception != null) {
            throw exception;
        }
    }

    static {
        try {
            final Class jframe = Class.forName("javax.swing.JFrame");
            final Constructor constructor = jframe.getConstructor([String] as Class[]);
            constructor.newInstance(["testing"] as String[]);
            headless = false;
        } catch (Throwable t) {
            // any exception means headless
            headless = true;
        }
    }

}
