class ClosureReturnWithoutReturnStatementTest extends GroovyTestCase {

    void testReturnValues() {
        block = {x:: x > 5}
        
        value = block.call(10)
        assert value
	    
        value = block.call(3)
        assert value == false
    }
	
    void testReturnValueUsingFunction() {
        block = {x:: someFunction(x) }
        
        value = block.call(10)
        assert value

        value = block.call(3)
        assert value == false
    }
    
    def someFunction(x) {
        x > 5
    }
}
