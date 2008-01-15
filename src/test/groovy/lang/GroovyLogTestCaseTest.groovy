package groovy.lang

import java.util.logging.Level
import java.util.logging.Logger

/**
Showing usage of the GroovyLogTestCase
@author Dierk Koenig
**/

class GroovyLogTestCaseTest extends GroovyLogTestCase {

    static final LOG = Logger.getLogger('groovy.lang.GroovyLogTestCaseTest')

    void loggedMethod() {
        LOG.finer 'some log entry'
    }

    void testStringLog(){
        def result = stringLog(Level.FINER, 'groovy.lang.GroovyLogTestCaseTest') {
            loggedMethod()
        }
        assertTrue result, result.contains('some log entry')
    }

    void testCombinedUsageForMetaClass(){
/*
        def result = withLevel(Level.FINER, 'groovy.lang.MetaClass') {
            stringLog(Level.FINER, 'methodCalls'){
                'hi'.toString()
            }
        }
        assertTrue result, result.contains('java.lang.String toString()')
*/
    }
}