/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy.security;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.Script;
import groovy.test.GroovyTestCase;
import junit.framework.TestCase;
import junit.framework.TestFailure;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import junit.textui.ResultPrinter;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.Permission;
import java.security.Policy;
import java.security.PrivilegedAction;
import java.util.Enumeration;

public abstract class SecurityTestSupport extends GroovyTestCase {
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
                resetSecurityPolicy("=" + POLICY_FILE);
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

    protected GroovyClassLoader loader =
            AccessController.doPrivileged(
                    (PrivilegedAction<GroovyClassLoader>) () -> new GroovyClassLoader(SecurityTestSupport.class.getClassLoader())
            );

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
        AccessController.doPrivileged((PrivilegedAction) () -> {
            Thread.currentThread().setContextClassLoader(loader);
            return null;
        });
    }

    protected void tearDown() {
        AccessController.doPrivileged((PrivilegedAction) () -> {
            System.setSecurityManager(securityManager);
            Thread.currentThread().setContextClassLoader(currentClassLoader);
            return null;
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
                    break;
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
    protected void assertExecute(final File file, final Permission missingPermission) {
        if (!isSecurityAvailable()) {
            return;
        }
        // Use our privileged access in order to prevent checks lower in the call stack.  Otherwise we would have
        // to grant access to IDE unit test runners and unit test libs.  We only care about testing the call stack
        // higher upstream from this point of execution.
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            GroovyCodeSource gcs = null;
            try {
                gcs = new GroovyCodeSource(file);
            } catch (IOException fnfe) {
                fail(fnfe.toString());
            }
            parseAndExecute(gcs, missingPermission);
            return null;
        });
    }

    /*
      * Execute the script represented by scriptStr using the supplied codebase.  If missingPermission
      * is non-null, then this invocation expects an AccessControlException with missingPermission
      * as the reason.  If missingPermission is null, the script is expected to execute successfully.
      */
    protected void assertExecute(final String scriptStr, String codeBase, final Permission missingPermission) {
        if (!isSecurityAvailable()) {
            return;
        }
        final String effectiveCodeBase = (codeBase != null) ? codeBase : "/groovy/security/test";
        // Use our privileged access in order to prevent checks lower in the call stack.  Otherwise we would have
        // to grant access to IDE unit test runners and unit test libs.  We only care about testing the call stack
        // higher upstream from this point of execution.
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            parseAndExecute(new GroovyCodeSource(scriptStr, generateClassName(), effectiveCodeBase), missingPermission);
            return null;
        });
    }
}
