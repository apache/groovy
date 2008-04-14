/**
 * All TCK testcases written in Groovy or Java.
 *
 * @author <a href="mailto:jeremy.rayner@bigfoot.com">Jeremy Rayner</a>
 * @author Dierk Koenig
 * @version $Revision$
 */

import groovy.util.AllTestSuite;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class UberTestCaseTCK_VM5 extends TestCase {
    public static Test suite() {
        TestSuite suite = (TestSuite) AllTestSuite.suite("src/test/gls", "**/vm5/*Test.groovy");
        return suite;
    }
}
