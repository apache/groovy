package groovy;



class TryCatchTest extends GroovyTestCase {

    property exceptionCalled;
    property finallyCalled;
	
    void testTryCatch() {
        try {
            failingMethod();
        }
        catch (AssertionError e) {
            onException(e);
        }
        finally {
            onFinally();
        }
        afterTryCatch();
    }

    void failingMethod() {
        assert false : "Failing on purpose";
	}
	
    void onException(e) {
	    assert e != null;
	    exceptionCalled = true;
	}
	
    void onFinally() {
        finallyCalled = true;
	}

    void afterTryCatch() {
        assert exceptionCalled : "should have invoked the catch clause";        
        assert finallyCalled : "should have invoked the finally clause";
        //System.out.println("After try/catch");
    }
}