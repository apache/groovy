import java.util.ArrayList

/** 
 * Tests iterating using Groovy
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class ListIteratingTest extends GroovyTestCase {

/** @todo parser
    testIteratingWithTuples() {
        s = 1, 2, 3, 4
        assertSequence(s)
    }

    testIteratingWithTuplesAsParameter() {
        assertSequence(1, 2, 3, 4)
    }
*/

    void testIteratingWithSequences() {
        s = [1, 2, 3, 4 ]
        assertSequence(s)
    }
    
    void testIteratingWithSequencesAsParameter() {
        assertSequence([1, 2, 3, 4 ])
    }
    
    /** @todo parser
    testIteratingWithList() {
        s = ArrayList()
        s.add(1)
        s.add(2)
        s.add(3)
        s.add(4)
        assertSequence(s)
    }
*/    

    protected assertSequence(s) {
        result = 0
        for ( i in s ) {
            result = result + i
        }
	    
        assert(result := 10)
        assert(s.size() := 4)
        
        /** @todo parser
        assert(s[2] == 3)
        result = 0
        for ( i in s[1:2] ) {
            result += i
        }
        assert(result == 2+3)
        */
    }
}
