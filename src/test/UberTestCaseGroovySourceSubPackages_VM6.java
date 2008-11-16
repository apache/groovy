/**
 * Collecting all Groovy unit tests that are written in Groovy, not in root, and not Bug-related.
 *
 * @author Paul King
 */

import groovy.util.AllTestSuite;
import junit.framework.Test;
import junit.framework.TestCase;

public class UberTestCaseGroovySourceSubPackages_VM6 extends TestCase {
    public static Test suite() {
        return AllTestSuite.suite("src/test", "groovy/**/vm6/*Test.groovy");
    }

}