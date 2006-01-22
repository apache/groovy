/**
 * All TCK testcases written in Groovy or Java.
 *
 * @author <a href="mailto:jeremy.rayner@bigfoot.com">Jeremy Rayner</a>
 * @author Dierk Koenig
 * @version $Revision$
 */
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import groovy.util.AllTestSuite;

public class UberTestCaseTCK extends TestCase {
    public static Test suite() {
        TestSuite suite = (TestSuite) AllTestSuite.suite("./src/test/gls","Test.groovy");
        suite.addTest(AllGlsJavaTestsSuite.suite());
        return suite;
    }
}

