import junit.framework.Test;
import junit.framework.TestSuite;
import gls.ch06.s05.JName1Test;

/**
 * All Java Unit tests in the 'gls' dir
 */

public class AllGlsJavaTestsSuite {

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(JName1Test.class);
        return suite;
    }
}
