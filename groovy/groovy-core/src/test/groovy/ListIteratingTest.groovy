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
    
    def testIteratingWithList() {
        s = new ArrayList()
        s.add(1)
        s.add(2)
        s.add(3)
        s.add(4)
        assertSequence(s)
    }

    protected void assertSequence(s) {
        result = 0
        for ( i in s ) {
            result = result + i
        }
	    
        assert(result == 10)
        assert(s.size() == 4)
        
        assert(s[2] == 3)
        /** @todo parser
        result = 0
        for ( i in s[1:2] ) {
            result += i
        }
        assert(result == 2+3)
        */
    }
}
