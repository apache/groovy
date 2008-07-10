package groovy.security;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.Script;
import groovy.util.GroovyTestCase;
import junit.framework.TestCase;
import junit.framework.TestFailure;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import junit.textui.ResultPrinter;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.io.File;
import java.io.PrintStream;
import java.io.IOException;
import java.security.*;
import java.util.Enumeration;

/**
 * @author Steve Goetze
 */
public class SecurityTestSupport extends GroovyTestCase {
    private static final String POLICY_FILE = "security/groovy.policy";
    private static int counter = 0;
    private static boolean securityDisabled;
    private static boolean securityAvailable;
    private static boolean securityChecked = false;

    static {
        if (System.getProperty("groovy.security.disabled") != null) {
            securityAvailable = false;
            securityDisabled = true;
        } else {
            securityDisabled = false;
            if (new File(POLICY_FILE).exists()) {
                securityAvailable = true;
                System.setProperty("java.security.policy", "=security/groovy.policy");
            } else {
                securityAvailable = false;
            }
        }
    }

    public static boolean isSecurityAvailable() {
        return securityAvailable;
    }

    public static boolean isSecurityDisabled() {
        return securityDisabled;
    }

    public static void resetSecurityPolicy(String policyFileURL) {
        System.setProperty("java.security.policy", policyFileURL);
        Policy.getPolicy().refresh();
    }

    protected class SecurityTestResultPrinter extends ResultPrinter {

        public SecurityTestResultPrinter(PrintStream stream) {
            super(stream);
        }

        public void print(TestResult result) {
            getWriter().println("Security testing on a groovy test failed:");
            printErrors(result);
            printFailures(result);
            printFooter(result);
        }
    }

    protected GroovyClassLoader loader = (GroovyClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
        public Object run() {
            return new GroovyClassLoader(SecurityTestSupport.class.getClassLoader());
        }
    });

    private SecurityManager securityManager;
    private ClassLoader currentClassLoader;

    public SecurityTestSupport() {
    }

    /*
      * Check SecuritySupport to see if security is properly configured.  If not, fail the first
      * test that runs.  All remaining tests will run, but not do any security checking.
      */
    private boolean checkSecurity() {
        if (!securityChecked) {
            securityChecked = true;
            if (!isSecurityAvailable()) {
                fail("Security is not available - skipping security tests.  Ensure that "
                        + POLICY_FILE + " is available from the current execution directory.");
            }
        }
        return isSecurityAvailable();
    }

    //Prepare for each security test.  First, check to see if groovy.lib can be determined via
    //a call to checkSecurity().  If not, fail() the first test.  Establish a security manager
    //and make the GroovyClassLoader the initiating class loader (ala GroovyShell) to compile AND
    //invoke the test scripts.  This handles cases where multiple .groovy scripts are involved in a
    //test case: a.groovy depends on b.groovy; a.groovy is parsed (and in the process the gcl
    //loads b.groovy via findClass).  Note that b.groovy is only available in the groovy class loader.
    //See
    protected void setUp() {
        if (checkSecurity()) {
            securityManager = System.getSecurityManager();
            if (securityManager == null) {
                System.setSecurityManager(new SecurityManager());
            }
        }
        currentClassLoader = Thread.currentThread().getContextClassLoader();
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                Thread.currentThread().setContextClassLoader(loader);
                return null;
            }
        });
    }

    protected void tearDown() {
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                System.setSecurityManager(securityManager);
                Thread.currentThread().setContextClassLoader(currentClassLoader);
                return null;
            }
        });
    }

    protected synchronized String generateClassName() {
        return "testSecurity" + (++counter);
    }

    /*
      * Execute the groovy script contained in file.  If missingPermission
      * is non-null, then this invocation expects an AccessControlException with missingPermission
      * as the reason.  If missingPermission is null, the script is expected to execute successfully.
      */
    protected Class parseClass(File file) {
        GroovyCodeSource gcs = null;
        try {
            gcs = new GroovyCodeSource(file);
        } catch (IOException fnfe) {
            fail(fnfe.toString());
        }
        return parseClass(gcs);
    }

    /*
	 * Parse the Groovy code contained in the GroovyCodeSource as a privileged operation (i.e. do not
	 * require the code source to have specific compile time permissions) and return the resulting class.
	 */
    protected Class parseClass(final GroovyCodeSource gcs) {
        Class clazz = null;
        try {
            clazz = loader.parseClass(gcs);
        } catch (Exception e) {
            fail(e.toString());
        }
        return clazz;
    }

    /*
      * Parse the script contained in the GroovyCodeSource as a privileged operation (i.e. do not
      * require the code source to have specific compile time permissions).  If the class produced is a
      * TestCase, run the test in a suite and evaluate against the missingPermission.
      * Otherwise, run the class as a groovy script and evaluate against the missingPermission.
      */
    private void parseAndExecute(final GroovyCodeSource gcs, Permission missingPermission) {
        Class clazz = null;
        try {
            clazz = loader.parseClass(gcs);
        } catch (Exception e) {
            fail(e.toString());
        }
        if (TestCase.class.isAssignableFrom(clazz)) {
            executeTest(clazz, missingPermission);
        } else {
            executeScript(clazz, missingPermission);
        }
    }

    protected void executeTest(Class test, Permission missingPermission) {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(test);
        TestResult result = new TestResult();
        suite.run(result);
        if (result.wasSuccessful()) {
            if (missingPermission == null) {
                return;
            } else {
                fail("Security test expected an AccessControlException on " + missingPermission + ", but did not receive one");
            }
        } else {
            if (missingPermission == null) {
                new SecurityTestResultPrinter(System.out).print(result);
                fail("Security test was expected to run successfully, but failed (results on System.out)");
            } else {
                //There may be more than 1 failure:  iterate to ensure that they all match the missingPermission.
                boolean otherFailure = false;
                for (Enumeration e = result.errors(); e.hasMoreElements();) {
                    TestFailure failure = (TestFailure) e.nextElement();
                    if (failure.thrownException() instanceof AccessControlException) {
                        AccessControlException ace = (AccessControlException) failure.thrownException();
                        if (missingPermission.implies(ace.getPermission())) {
                            continue;
                        }
                    }
                    otherFailure = true;
                }
                if (otherFailure) {
                    new SecurityTestResultPrinter(System.out).print(result);
                    fail("Security test expected an AccessControlException on " + missingPermission + ", but failed for other reasons (results on System.out)");
                }
            }
        }
    }

    protected void executeScript(Class scriptClass, Permission missingPermission) {
        try {
            Script script = InvokerHelper.createScript(scriptClass, new Binding());
            script.run();
            //InvokerHelper.runScript(scriptClass, null);
        } catch (AccessControlException ace) {
            if (missingPermission != null && missingPermission.implies(ace.getPermission())) {
                return;
            } else {
                fail(ace.toString());
            }
        }
        if (missingPermission != null) {
            fail("Should catch an AccessControlException");
        }
    }

    /*
      * Execute the groovy script contained in file.  If missingPermission
      * is non-null, then this invocation expects an AccessControlException with missingPermission
      * as the reason.  If missingPermission is null, the script is expected to execute successfully.
      */
    protected void assertExecute(File file, Permission missingPermission) {
        if (!isSecurityAvailable()) {
            return;
        }
        GroovyCodeSource gcs = null;
        try {
            gcs = new GroovyCodeSource(file);
        } catch (IOException fnfe) {
            fail(fnfe.toString());
        }
        parseAndExecute(gcs, missingPermission);
    }

    /*
      * Execute the script represented by scriptStr using the supplied codebase.  If missingPermission
      * is non-null, then this invocation expects an AccessControlException with missingPermission
      * as the reason.  If missingPermission is null, the script is expected to execute successfully.
      */
    protected void assertExecute(String scriptStr, String codeBase, Permission missingPermission) {
        if (!isSecurityAvailable()) {
            return;
        }
        if (codeBase == null) {
            codeBase = "/groovy/security/test";
        }
        parseAndExecute(new GroovyCodeSource(scriptStr, generateClassName(), codeBase), missingPermission);
    }
}
