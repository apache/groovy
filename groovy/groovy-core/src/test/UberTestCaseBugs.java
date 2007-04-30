/**
 * Collects all Bug-related tests.
 *
 * @author <a href="mailto:jeremy.rayner@bigfoot.com">Jeremy Rayner</a>
 * @version $Revision$
 */

import groovy.util.AllTestSuite;
import junit.framework.Test;
import junit.framework.TestCase;

public class UberTestCaseBugs extends TestCase {
    public static Test suite() {
        return AllTestSuite.suite("./src/test", "groovy/**/*Bug.groovy");
    }

// no tests inside (should we have an AbstractGroovyTestCase???)
//        groovy.bugs.TestSupport.class

//  The following classes appear in target/test-classes but do not extend junit.framework.TestCase
//        groovy.bugs.Cheese.class
//        groovy.bugs.MyRange.class
//        groovy.bugs.Scholastic.class
//        groovy.bugs.SimpleModel.class

}
