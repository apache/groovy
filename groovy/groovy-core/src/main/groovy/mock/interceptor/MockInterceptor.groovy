package groovy.mock.interceptor

class MockInterceptor implements Interceptor {

    @Property expectation = null

    Object beforeInvoke(Object object, String methodName, Object[] arguments) {
        if (!expectation) throw new IllegalStateException("Property 'expectation' must be set before use.")
        return expectation.match(methodName).call(arguments)
    }

    Object afterInvoke(Object object, String methodName, Object[] arguments, Object result) {
        return null // never used
    }

    boolean doInvoke() {
        return false // future versions may allow collaborator method calls depending on state
    }
}