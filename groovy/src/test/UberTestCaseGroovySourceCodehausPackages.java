/**
 * Collecting all Groovy unit tests that are written in Groovy, not in root, 
 * and not Bug-related.
 *
 * @author Joachim Baumann
 * @version $Revision: 6546 $
 */

import groovy.util.AllTestSuite;
import junit.framework.Test;
import junit.framework.TestCase;

/**
 * Collect all groovy tests 
 */
public class UberTestCaseGroovySourceCodehausPackages extends TestCase {
    /**
     * Add all groovy tests from the codehaus subdirs
     * @return testsuite
     */
    public static Test suite() {
        return AllTestSuite.suite("src/test", "org/codehaus/**/*Test.groovy", "org/codehaus/**/vm5/*Test.groovy");
    }
}
