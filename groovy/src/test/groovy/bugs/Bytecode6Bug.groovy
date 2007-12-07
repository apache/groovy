package groovy.bugs

/**
 * @version $Revision$
 */
class Bytecode6Bug extends GroovyTestCase {

    void testPostFixReturn() {
        def i = 1
        def closure = { i++ }
        def value = closure()
        
        assert value == 1
        assert i == 2
    }
    
    void testPreFixReturn() {
        def i = 1
        def closure = { return ++i }
        def value = closure()
        
        assert value == 2
        assert i == 2
    }
}