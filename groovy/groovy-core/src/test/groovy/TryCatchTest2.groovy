class TryCatchTest2 extends GroovyTestCase {

    property exceptionCalled
    property finallyCalled
	
    void testFailingMethod() {
        try {
            failingMethod()
        }
        catch (AssertionError e) {
            onException(e)
        }
        finally {
            onFinally()
        }
        afterTryCatch()
    }
        
        /*
        assert exceptionCalled : "should have invoked the catch clause"        
        assert finallyCalled : "should have invoked the finally clause"
        println("After try/catch")
        */
        /*
    void testWorkingMethod() {
        try {
            workingMethod()
        }
        catch (AssertionError e) {
            onException(e)
        }
        finally {
            onFinally()
        }
        assert exceptionCalled == false : "should not invoked the catch clause"        
        assert finallyCalled : "should have invoked the finally clause"
        println("After try/catch")
    }
    */


    void failingMethod() {
        assert false : "Failing on purpose"
	}

    void workingMethod() {
    }

    void onException(e) {
	    assert e != null
	    exceptionCalled = true
	}
	
    void onFinally() {
        finallyCalled = true
	}

    void afterTryCatch() {
        assert exceptionCalled : "should have invoked the catch clause"        
        assert finallyCalled : "should have invoked the finally clause"
        println("After try/catch")
    }
}