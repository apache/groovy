package groovy.bugs

class ArrayMethodCallBug extends TestSupport {

    void testMethodCallingWithArrayBug() {
        array = getMockArguments()
        
        /** @todo
        dummyMethod(array)
        */
    }
    
    protected dummyMethod(array) {
    }
}