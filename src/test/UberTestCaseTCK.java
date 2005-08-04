/**
 * to prevent a JVM startup-shutdown time per test, it should be more efficient to
 * collect the tests together into a suite.
 *
 * @author <a href="mailto:jeremy.rayner@bigfoot.com">Jeremy Rayner</a>
 * @version $Revision$
 */
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class UberTestCaseTCK extends TestCase {
    public static Test suite() {
        TestSuite suite = new TestSuite();

        suite.addTestSuite(gls.ch03.s01.Unicode1Test.class);
	// todo        suite.addTestSuite(gls.ch03.s01.Unicode2Test.class);

	suite.addTestSuite(gls.ch03.s02.Longest1Test.class);
	suite.addTestSuite(gls.ch03.s02.LexicalTranslation1Test.class);
        
        suite.addTestSuite(gls.ch03.s03.UnicodeEscapes1Test.class);
        // todo suite.addTestSuite(gls.ch03.s03.UnicodeEscapes2Test.class); 
        return suite;
    }
}

