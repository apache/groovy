package groovy.mock

import groovy.mock.GroovyMock
import junit.framework.AssertionFailedError

class MockTest extends GroovyTestCase {

    def mock

    void setUp() {
        mock = GroovyMock.newInstance()
    }

    void testASimpleExpectationCanBeSetAndMet() {
        // expectation
        mock.doSomething("hello")

        // execute
        mock.instance.doSomething("hello")

        // verify
        mock.verify()
    }

    void testASimpleExpectationCanBeSetAndFailed() {
        // expectation
        mock.doSomething("hello")

        // execute
        try {
            mock.instance.doSomething("goodbye")
            fail("expected exception")
        }
        catch (RuntimeException goodException) {
        }

    }

    void testASimpleExpectationCanBeSetButNeverCalledSoVerifyFails() {
        // expectation
        mock.doSomething("hello")

        // execute
        // don't call it

        // verify
        try {
            mock.verify()
            fail("should not have verified")
        }
        catch (AssertionFailedError goodException) {
        }
    }

    void testAnExpectationWithAClosureGivesErrorIFCalledAndClosureFails() {
        mock.doSomething( {arg | assert arg=="poo" } )

        // verify
        try {
            mock.instance.doSomething("hello")
            fail("Expected verify to fail");
        }
        catch (RuntimeException ex) {
            //expected
        }
    }

    /*
     * was GROOVY-76
     */
    void testAnExpectationwithAClosurePassesIfClosurePasses() {
        mock.doSomething {arg | assert arg=="hello" } 
        
        // execute
        mock.instance.doSomething("hello")

        //verify
        mock.verify()
    }

    void testAnExpectationWithAClosureGivesErrorIFNotCalled() {
        mock.doSomething( {arg | assert arg=="poo" } )
        // verify
        try {
            mock.verify()
            fail("Expected verify to fail");
        }
        catch (AssertionFailedError ex) {
            //expected
        }
    }

}


