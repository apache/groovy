/**
 * @version $Revision$
 */
class Bytecode4Bug extends GroovyTestCase {

    count = 0
     
    void testInject() {
        x = [1, 2, 3].inject(0) { c, s | c += s }
        assert x == 6
    }
     
    void testUsingProperty() {
        count = 0
        getCollection().each { count += it }       
        assert count == 10
    }
    
    /*
     void testUsingLocalVar() {
        c = 0
        getCollection().each { c += it }       
        assert c == 10
    }
     */
    
    /*
    void testPostFixReturn() {
        i = 1
        closure = { i++ }
        value = closure()
        
        assert value == 1
        assert i == 1
    }
    
    void testPreFixReturn() {
        i = 1
        closure = { ++i }
        value = closure()
        
        assert value == 2
        assert i == 1
    }
    */
    
    getCollection() {
        [1, 2, 3, 4]
    }
}