/**
 * @version $Revision$
 */
class ClosureVariableBug extends GroovyTestCase {
    
    void testBug() {
        count = 0
        closure = { assert count == it }
        closure(0)
        
        count = 1
        closure(1)
    }
}