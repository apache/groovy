package groovy.security;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import java.io.File;

/**
 * Test case for running a single groovy script parsed from a .groovy file.
 */
public class RunOneGroovyScript extends SecurityTestSupport {

    protected static String file;

    public static void main(String[] args) {
        if (args.length > 0) {
            file = args[0];
        }
        TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(RunOneGroovyScript.class);
    }

    public void testScript() {
        String fileName = System.getProperty("script", file);
        if (fileName == null) {
            throw new RuntimeException("No filename given in the 'script' system property so cannot run a Groovy script");
        }
        assertExecute(new File(fileName), null);
    }
}
