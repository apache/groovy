/**
 * @version $Revision$
 */
class ClosureTypedVariableBug extends GroovyTestCase {
    
    void testBug2() {
   		
        Integer count = 0
        closure = { count = it }
        closure(1)
        assert count == 1
    }
    
    void testBug() {
   		count = makeClosure(0)
        assert count == 1
    }

/** @todo can't turn a parameter into a reference

    makeClosure(Number count) {
    	closure = { count = it }
    	closure(1)
    	return count
    }
*/

    makeClosure(Number c) {
    	count = c
    	closure = { count = it }
    	closure(1)
    	return count
    }
}