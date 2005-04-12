/**
 * @version $Revision$
 */
class Bytecode6Bug extends GroovyTestCase {

    void testPostFixReturn() {
        i = 1
        closure = { i++ }
        value = closure()
        
        assert value == 1
        assert i == 2
    }
    
    void testPreFixReturn() {
        i = 1
        closure = { return ++i }
        value = closure()
        
        assert value == 2
        assert i == 2
    }
}