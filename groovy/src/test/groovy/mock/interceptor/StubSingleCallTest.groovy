package groovy.mock.interceptor

/**
    Testing Groovy Stub support for single calls to the Collaborator
    @author Dierk Koenig
*/

class StubSingleCallTest extends GroovyTestCase {

    StubFor stub

    void setUp() {
    	stub = new StubFor(Collaborator.class)
    }

    void testFirstOptionalOmitted() {
    	stub.demand.one(0..1) { 1 }
    	stub.use {
            def caller = new Caller()
        }
    	stub.expect.verify()
        // Getting here means no exception, which is what we want to test.  (Fix for GROOVY-2309)
    }
}



