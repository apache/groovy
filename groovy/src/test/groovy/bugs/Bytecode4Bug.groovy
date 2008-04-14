package groovy.bugs

/**
 * @version $Revision$
 */
class Bytecode4Bug extends GroovyTestCase {

    def count = 0
     
    void testInject() {
        def x = [1, 2, 3].inject(0) { c, s -> c += s }
        assert x == 6
    }
     
    void testUsingProperty() {
        count = 0
        getCollection().each { count += it }       
        assert count == 10
    }
    
    void testUsingIncrementingProperty() {
        count = 0
        getCollection().each { count++ }       
        assert count == 4
    }
    
    def getCollection() {
        [1, 2, 3, 4]
    }
}