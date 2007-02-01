package groovy

/** 
 * Tests Closures in Groovy
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class ClosureReturnTest extends GroovyTestCase {

    void testReturnValues() {
        def block = {x-> return x > 5}

        def value = block.call(10)
        assert value

        value = block.call(3)
        assert value == false
    }

    void testReturnValueUsingFunction() {
        def block = {x-> return someFunction(x) }
        
        def value = block.call(10)
        assert value

        value = block.call(3)
        assert value == false
    }
    
    def someFunction(x) {
        return x > 5
    }
}
