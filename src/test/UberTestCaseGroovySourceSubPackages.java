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
        // temp hack to track down bamboo build issue
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
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/Sa*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/Sb*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/Sc*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/Sd*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/Se*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/Sf*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/Sg*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/Sh*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/Si*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/Sj*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/Sk*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/Sl*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/Sm*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/Sn*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/So*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/Sp*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/Sq*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/Sr*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/Ss*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/St*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/Su*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/Sv*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/SwingBuilderBindingsTest.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/SwingBuilderTest.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/Sx*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/Sy*Test.groovy", EXCLUDES));
        suite.addTest(AllTestSuite.suite(BASE, "groovy/*/**/Sz*Test.groovy", EXCLUDES));
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
