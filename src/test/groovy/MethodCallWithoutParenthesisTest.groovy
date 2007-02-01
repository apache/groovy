package groovy

class MethodCallWithoutParenthesisTest extends GroovyTestCase {

    def flag

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
        methodWithTwoParams 5, 6

        // not allowed in New Groovy
        // value = methodWithTwoParams 5, 6
        def value = methodWithTwoParams(5, 6)

        assert value == 11
    }
    
    void testMethodCallWithTwoParamsUsingThis() {
        def value = this.methodWithTwoParams(5, 6)
        
        assert value == 11
    }

    def methodWithTwoParams(a, b) {
        println("Called method with parameters ${a} and ${b}")
        return a + b
    }
}