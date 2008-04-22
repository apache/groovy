/**
 * Collecting all Groovy unit tests that are written in Groovy, not in root, and not Bug-related.
 *
 * @author <a href="mailto:jeremy.rayner@bigfoot.com">Jeremy Rayner</a>
 * @author Dierk Koenig
 * @version $Revision$
 */

import groovy.util.AllTestSuite;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class UberTestCaseGroovySourceSubPackages extends TestCase {
    private static final String EXCLUDES = "groovy/**/vm5/*Test.groovy";
    private static final String BASE = "src/test";
    public static Test suite() {
        TestSuite suite = new TestSuite();
//        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/*Test.groovy", EXCLUDES));
        // temp hack to track down build issue
//        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/A*Test.groovy", EXCLUDES));
//        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/B*Test.groovy", EXCLUDES));
//        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/C*Test.groovy", EXCLUDES));
//        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/D*Test.groovy", EXCLUDES));
//        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/E*Test.groovy", EXCLUDES));
//        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/F*Test.groovy", EXCLUDES));
//        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/G*Test.groovy", EXCLUDES));
//        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/H*Test.groovy", EXCLUDES));
//        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/I*Test.groovy", EXCLUDES));
//        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/J*Test.groovy", EXCLUDES));
//        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/K*Test.groovy", EXCLUDES));
//        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/L*Test.groovy", EXCLUDES));
//        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/M*Test.groovy", EXCLUDES));
//        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/N*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/O*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/P*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/Q*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/R*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/S*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/T*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/U*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/V*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/W*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/X*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/Y*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/Z*Test.groovy", EXCLUDES));
        return suite;
    }
}
