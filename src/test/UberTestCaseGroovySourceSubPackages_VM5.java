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

public class UberTestCaseGroovySourceSubPackages_VM5 extends TestCase {
    public static Test suite() {
        return AllTestSuite.suite("src/test", "groovy/**/vm5/*Test.groovy");
    }

}