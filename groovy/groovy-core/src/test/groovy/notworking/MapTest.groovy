package groovy;

import java.util.HashMap;
import java.util.Map;

/** 
 * Tests using Maps in Groovy
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class MapTest extends Test {

    testMap() {
        m = { 1 : 'abc', 2 : 'def', 3 : 'xyz' };
        assertMap(m);
    }

    testMapAsParameter() {
        assertMap({ 1 : 'abc', 2 : 'def', 3 : 'xyz' });
    }

    testMapViaHashMap() {
        m = HashMap();
        m.put(1, 'abc');
        m.put(2, 'def');
        m.put(3, 'xyz');
        assertMap(m);
    }

	testMapMutation() {    
	    m = { 'abc' : 'def', 'def' : 134, 'xyz' : 'zzz' };
	    
        assertEquals(m['unknown'], null);
        assertEquals(m['def'], 134);
	    
	    m['def'] = 'cafebabe';
	    
        assertEquals(m['def'], 'cafebabe');
	    
	    assertEquals(m.size(), 3);
	    
	    m.remove('def');
	    
        assertEquals(m['def'], null);
        assertEquals(m.size(), 2);
	}

    
    protected assertMap(m) {
        assert(m instanceof Map);
        
        result = 0;
        text = "";
        for e in m {
            result += e.key;
            text += e.value;
        }
	    
        assertEquals(result, 6);
        assertEquals(text, "abcdefxyz");
        assertEquals(s.size(), 3);
        assertEquals(s[2], 'def');
    }
}