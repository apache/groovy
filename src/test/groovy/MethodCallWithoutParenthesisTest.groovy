class MethodCallWithoutParenthesisTest extends GroovyTestCase {

    flag = false
    
    void testMethodCallWithOneParam() {
        flag = false
        
        methodWithOneParam "hello"
        
        assert flag
    }
    
    void testMethodCallWithOneParamUsingThis() {
        flag = false
        
        this.methodWithOneParam "hello"
        
        assert flag
    }
    
    void methodWithOneParam(text) {
        println("Called method with parameter ${text}")
        assert text == "hello"
        flag = true
    }
    
    void testMethodCallWithTwoParams() {
        value = methodWithTwoParams 5, 6
        
        assert value == 11
    }
    
    void testMethodCallWithTwoParamsUsingThis() {
        value = this.methodWithTwoParams 5, 6
        
        assert value == 11
    }
    
    methodWithTwoParams(a, b) {
        println("Called method with parameters ${a} and ${b}")
        a + b
    }
}