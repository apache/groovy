/**
 * Collects all Bug-related tests.
 *
 * @author <a href="mailto:jeremy.rayner@bigfoot.com">Jeremy Rayner</a>
 * @version $Revision$
 */

import groovy.util.AllTestSuite;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class UberTestCaseBSF extends TestCase {
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(org.codehaus.groovy.bsf.BSFTest.class);
        suite.addTestSuite(org.codehaus.groovy.bsf.CacheBSFTest.class);
        return suite;
    }

// no tests inside (should we have an AbstractGroovyTestCase???)
//        groovy.bugs.TestSupport.class

//  The following classes appear in target/test-classes but do not extend junit.framework.TestCase
//        groovy.bugs.Cheese.class
//        groovy.bugs.MyRange.class
//        groovy.bugs.Scholastic.class
//        groovy.bugs.SimpleModel.class

}

