package groovy

class ClosureReturnWithoutReturnStatementTest extends GroovyTestCase {

    void testReturnValues() {
        def block = {x-> x > 5}

        def value = block.call(10)
        assert value

        value = block.call(3)
        assert value == false
    }

    void testReturnValueUsingFunction() {
        def block = {x-> someFunction(x) }
        
        def value = block.call(10)
        assert value

        value = block.call(3)
        assert value == false
    }
    
    def someFunction(x) {
        x > 5
    }
}
