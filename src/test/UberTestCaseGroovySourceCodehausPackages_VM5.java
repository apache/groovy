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
public class UberTestCaseGroovySourceCodehausPackages_VM5 extends TestCase {
    /**
     * Add all VM5 specific groovy tests from the codehaus subdirs
     * @return testsuite
     */
    public static Test suite() {
        return AllTestSuite.suite("src/test", "org/codehaus/**/vm5/*Test.groovy");
    }
}