package groovy.bugs

class ArrayMethodCallBug extends TestSupport {

    void testMethodCallingWithArrayBug() {
        def array = getMockArguments()
        
        dummyMethod(array)
    }
    
    protected void dummyMethod(array) {
    }
}