/**
 * @version $Revision$
 */
class Bytecode5Bug extends GroovyTestCase {

    void testUsingLocalVar() {
        c = 0
        getCollection().each { c += it }       
        assert c == 10
    }
    
    def getCollection() {
        [1, 2, 3, 4]
    }
}