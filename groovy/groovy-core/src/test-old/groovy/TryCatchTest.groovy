class TryCatchTest extends GroovyTestCase {

    property exceptionCalled
    property finallyCalled
	
    void testTryCatch() {
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
        assert exceptionCalled : "should have invoked the catch clause"        
        assert finallyCalled : "should have invoked the finally clause"
        println("After try/catch")
     }


     void testTryFinally() {
         Boolean touched = false;
         
         try {
         }
         finally {
             touched = true;
         }

         assert touched : "finally not called with empty try"
     }



     void testWorkingMethod() {
         /** @todo causes inconsistent stack height
          assert exceptionCalled == false : "should not invoked the catch clause"        
          */
         
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
    
    void failingMethod() {
        assert false : "Failing on purpose"
	}
	
    void workingMethod() {
        assert true : "Should never fail"
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
    
    protected void setUp() {
        exceptionCalled = false
        finallyCalled = false
    }
}
