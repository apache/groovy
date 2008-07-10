package groovy.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

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

    static {
        try {
            final Class jframe = Class.forName("javax.swing.JFrame");
            final Constructor constructor = jframe.getConstructor(new Class[]{String.class});
            constructor.newInstance(new String[]{"testing"});
            headless = false;
        } catch (java.awt.HeadlessException e) {
            headless = true;
        } catch (UnsatisfiedLinkError e) {
            headless = true;
        } catch (ClassNotFoundException e) {
            headless = true;
        } catch (NoClassDefFoundError e) {
            headless = true;
        } catch (IllegalAccessException e) {
            headless = true;
        } catch (InstantiationException e) {
            headless = true;
        } catch (NoSuchMethodException e) {
            headless = true;
        } catch (InvocationTargetException e) {
            headless = true;
        }
    }

}
