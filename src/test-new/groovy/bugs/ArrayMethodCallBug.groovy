package groovy.bugs

class ArrayMethodCallBug extends TestSupport {

    void testMethodCallingWithArrayBug() {
        array = getMockArguments()
        
        dummyMethod(array)
    }
    
    protected void dummyMethod(array) {
    }
}