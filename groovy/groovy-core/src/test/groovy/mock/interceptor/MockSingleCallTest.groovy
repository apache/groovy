package groovy.mock.interceptor

import junit.framework.AssertionFailedError

/*
    Testing Groovy Mock support for single calls to the Collaborator with
    no, one, multiple, or arbitrary arguments, exceptions and failures.
    @author Dierk Koenig
*/

class MockSingleCallTest extends GroovyTestCase {

    MockFor mocker

    void setUp() {
        mocker = new MockFor(Collaborator.class)
    }

    void testSingleCallNoArgs() {
        mocker.demand.one { 1 }
        mocker.use {
            assertEquals 1, new Caller().collaborateOne()
        }
    }
    void testSingleCallOneArg() {
        mocker.demand.one { arg -> return arg }
        mocker.use {
            assertEquals 2, new Caller().collaborateOne(2)
        }
    }
    void testSingleCallTwoArgs() {
        mocker.demand.one { one, two -> return one + two }
        mocker.use {
            assertEquals 2, new Caller().collaborateOne(1, 1)
        }
    }
    void testNoSingleCallTwoArgsWhenNoArgDemanded() {
        mocker.demand.one { 2 }
        mocker.use {
            shouldFail {
                assertEquals 2, new Caller().collaborateOne(1, 1)
            }
        }
    }
    void testSingleCallTwoArgsWhenArbitraryArgsDemanded() {
        mocker.demand.one { Object[] arg ->  2 }
        mocker.use {
            assertEquals 2, new Caller().collaborateOne(1, 1)
        }
    }
    void testSingleCallTwoArgsWhenDefaultArgsDemanded() {
        mocker.demand.one { one=null, two=null ->  2 }
        mocker.use {
            assertEquals 2, new Caller().collaborateOne(1, 1)
        }
    }
    void testVerifyFailsIfOneDemandedButNoneExcecuted() {
        mocker.demand.one { 1 }
        shouldFail(AssertionFailedError.class) {
            mocker.use {
                // no call
            }
        }
    }
    void testSingleCallExceptionDemanded() {
        mocker.demand.one { throw new IllegalArgumentException() }
        mocker.use {
            //shouldFail(IllegalArgumentException.class) {
            shouldFail { // todo: should fail with IllegalArgumentException instead of GroovyRuntimeException
                new Caller().collaborateOne()
            }
        }
    }
    void testSingleCallFailDemanded() {
        mocker.demand.one { fail 'just kidding' }
        mocker.use {
            shouldFail() { new Caller().collaborateOne() }
        }
    }
    void testJavaCall() {
        mocker = new MockFor(String.class)
        mocker.demand.toString { 'groovy' }
        mocker.use {
            assertEquals 'groovy', new Caller().collaborateJava()
        }
    }

}



