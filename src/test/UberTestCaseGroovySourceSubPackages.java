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
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/A*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/B*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/C*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/D*Test.groovy", EXCLUDES));
        // TODO remove comment once ExpandoMetaClassTest is fixed
//        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/E*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/F*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/G*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/H*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/I*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/J*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/K*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/L*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/M*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/N*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/O*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/P*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/Q*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/R*Test.groovy", EXCLUDES));
        // TODO remove comment once bamboo headless issue is fixed
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/SA*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/SB*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/SC*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/SD*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/SE*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/SF*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/SG*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/SH*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/SI*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/SJ*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/SK*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/SL*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/SM*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/SN*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/SO*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/SP*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/SQ*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/SR*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/SS*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/ST*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/SU*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/SV*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/SW*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/SX*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/SY*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/SZ*Test.groovy", EXCLUDES));
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
