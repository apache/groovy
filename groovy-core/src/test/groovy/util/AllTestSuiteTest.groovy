package groovy.util

import java.util.logging.Level
import junit.framework.Test

/**
    Testing groovy.util.AllTestSuite.
    The suite() method must properly collect Test files under the given dir and pattern,
    add found files to the log,
    produce a proper TestSuite,
    and wrap Scripts into TestCases.
    @author Dierk Koenig
*/
class AllTestSuiteTest extends GroovyLogTestCase {

    def suite

    void setUp() {
        suite = null
    }

    void testSuiteForThisFileOnly() {
        def result = stringLog(Level.FINEST, 'groovy.util.AllTestSuite') {
            withProps('src/test/groovy/util','AllTestSuiteTest.groovy') {
                suite = AllTestSuite.suite()
            }
        }
        assertTrue result, result.contains('AllTestSuiteTest.groovy')
        assertEquals 1+1, result.count("\n")   // only one entry in the log
        assert suite, 'Resulting suite should not be null'
        assertEquals 2, suite.countTestCases() // the 2 test methods in this file
    }

    void testAddingScriptsThatDoNotInheritFromTestCase() {
        withProps('src/test/groovy/util','suite/*.groovy') {
            suite = AllTestSuite.suite()
        }
        assert suite
        assertEquals 1, suite.countTestCases()
        suite.testAt(0) // call the contained Script to makes sure it is testable
    }

    /** store old System property values for not overriding them accidentally */
    void withProps(dir, pattern, yield) {
        String olddir = System.properties.'groovy.test.dir'
        String oldpat = System.properties.'groovy.test.pattern'
        System.properties.'groovy.test.dir' = dir
        System.properties.'groovy.test.pattern' = pattern
        yield()
        if (olddir) System.properties.'groovy.test.dir' = olddir
        if (oldpat) System.properties.'groovy.test.pattern' = oldpat
    }
}