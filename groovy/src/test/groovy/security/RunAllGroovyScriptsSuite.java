/*
 * Created on Apr 7, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package groovy.security;

import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Run all the .groovy scripts found under the src/test tree with a security manager active.
 * Not currently part of the build because it adds about 4 minutes to the build process.
 *
 * @author Steve Goetze
 */
public class RunAllGroovyScriptsSuite extends SecurityTestSupport {

    /**
     * Find all Groovy script test cases in the source tree and execute them with a security policy in effect.
     * The complete filename of the groovy source file is used as the codebase so that the proper grants can be
     * made for each script.
     */
    protected void executeTests(File dir, TestResult result) throws Exception {
        File[] files = dir.listFiles();
        List traverseList = new ArrayList();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isDirectory()) {
                traverseList.add(file);
            } else {
                String name = file.getName();
                if (name.endsWith("Test.groovy") || name.endsWith("Bug.groovy")) {
                    //if (name.endsWith("IanMaceysBug.groovy")) {
                    Class clazz = parseClass(file);
                    if (TestCase.class.isAssignableFrom(clazz)) {
                        TestSuite suite = new TestSuite(clazz);
                        suite.run(result);
                    }
                }
            }
        }
        for (Iterator iter = traverseList.iterator(); iter.hasNext();) {
            executeTests((File) iter.next(), result);
        }
    }

    public void testGroovyScripts() throws Exception {
        if (!isSecurityAvailable()) {
            return;
        }
        TestResult result = new TestResult();
        executeTests(new File("src/test"), result);
        if (!result.wasSuccessful()) {
            new SecurityTestResultPrinter(System.out).print(result);
            fail("At least one groovy testcase did not run under the secure groovy environment.  Results in the testcase output");
        }
    }
}
