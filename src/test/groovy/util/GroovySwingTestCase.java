package groovy.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class GroovySwingTestCase extends GroovyTestCase {
    private static boolean headless;

    public static boolean getHeadless() {
        return headless;
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
