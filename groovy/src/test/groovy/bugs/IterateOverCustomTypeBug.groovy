package groovy.bugs

/**
 * @version $Revision: 1.3 $
 */
class IterateOverCustomTypeBug extends TestSupport {
    
    void testBug() {
        def object = this
        
        def answer = []
        for (i in object) {
            answer << i
        }
        assert answer == ['a', 'b', 'c']
        
        answer = []
        object.each { answer << it }
        assert answer == ['a', 'b', 'c']
    }
}