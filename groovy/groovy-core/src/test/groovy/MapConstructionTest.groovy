package groovy;

import org.codehaus.groovy.GroovyTestCase;

/** 
 * Tests creating Maps in Groovy
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class MapConstructionTest extends GroovyTestCase {

    void testMap() {
        m = [ 1 : 'abc', 2 : 'def', 3 : 'xyz' ];

        //System.err.println( m );

        mtoo = [ 1 : [ "innerKey" : "innerValue" ], 2 : m ];

        System.err.println( mtoo );

        assertMap(m);
    }

    /** @todo parser
    testMapAsParameter() {
        assertMap([ 1 : 'abc', 2 : 'def', 3 : 'xyz' ]);
    }

    testMapViaHashMap() {
        m = new HashMap();
        m.put(1, 'abc');
        m.put(2, 'def');
        m.put(3, 'xyz');
        assertMap(m);
    }


    */
    
    assertMap(m) {
        //assert m instanceof Map;
        /** @todo parser
        assertEquals( "java.util.HashMap", m.getClass().getName() );

        result = 0;
        text = "";
        for e in m {
            result += e.key;
            text += e.value;
        }
        assertEquals(result, 6);
        assertEquals(text, "abcdefxyz");
*/
	    
        assertEquals(m.size(), 3);
        // assertEquals(s[2], 'def');
    }
}
