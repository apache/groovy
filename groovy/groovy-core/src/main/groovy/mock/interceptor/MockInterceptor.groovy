package groovy.mock.interceptor

/**
    Intercepting calls to the collaborating object and notify the expectation object.
    @author Dierk Koenig
*/

class MockInterceptor implements PropertyAccessInterceptor {

    def expectation = null

    Object beforeInvoke(Object object, String methodName, Object[] arguments) {
        if (!expectation) throw new IllegalStateException("Property 'expectation' must be set before use.")
        return expectation.match(methodName).call(arguments)
    }

    Object beforeGet(Object object, String property) {
        if (!expectation) throw new IllegalStateException("Property 'expectation' must be set before use.")
        String name = "get${property[0].toUpperCase()}${property[1..-1]}"
        return expectation.match(name).call()                    
    }

    void beforeSet(Object object, String property, Object newValue) {
        if (!expectation) throw new IllegalStateException("Property 'expectation' must be set before use.")
        String name = "set${property[0].toUpperCase()}${property[1..-1]}"
        expectation.match(name).call(newValue)
    }

    Object afterInvoke(Object object, String methodName, Object[] arguments, Object result) {
        return null // never used
    }

    boolean doInvoke() {
        return false // future versions may allow collaborator method calls depending on state
    }
}