package groovy;

import java.util.ArrayList;

/** 
 * Tests iterating using Groovy
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class SequenceIteratingTest extends Test {

    testIteratingWithTuples() {
        s = 1, 2, 3, 4;
        assertSequence(s);
    }

    testIteratingWithTuplesAsParameter() {
        assertSequence(1, 2, 3, 4);
    }

    testIteratingWithSequences() {
        s = {1, 2, 3, 4 };
        assertSequence(s);
    }
    
    testIteratingWithSequencesAsParameter() {
        assertSequence({1, 2, 3, 4 });
    }
    
    testIteratingWithList() {
        s = ArrayList();
        s.add(1);
        s.add(2);
        s.add(3);
        s.add(4);
        assertSequence(s);
    }
    
    
    protected assertSequence(s) {
        result = 0;
        for i in s {
            result += i;
        }
	    
        assert(result == 10);
        assert(s[2] == 3);
        assert(s.size() == 4);
        
        result = 0;
        for i in s[1:2] {
            result += i;
        }
        assert(result == 2+3);
    }
}