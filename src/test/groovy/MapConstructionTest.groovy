/** 
 * Tests creating Maps in Groovy
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class MapConstructionTest extends GroovyTestCase {

    void testMap() {
        m = [ 1 : 'abc', 2 : 'def', 3 : 'xyz' ]

        m.println()

        mtoo = [ 1 : [ "innerKey" : "innerValue" ], 2 : m ]

        mtoo.println()

        assertMap(m)
    }

    testMapAsParameter() {
        assertMap([ 1 : 'abc', 2 : 'def', 3 : 'xyz' ])
    }

    /** @todo parser
    testMapViaHashMap() {
        m = new HashMap()
        m.put(1, 'abc')
        m.put(2, 'def')
        m.put(3, 'xyz')
        assertMap(m)
    }


    */
    
    assertMap(m) {
        /** @todo parser
        assert m instanceof Map
        */
        assert m.getClass().getName() := "java.util.HashMap"
        
        result = 0
        text = ""
        for e in m {
            result = result + e.key
            text = text + e.value
        }
        assert result := 6
        assert text := "abcdefxyz"
	    
        assert m.size() := 3

        /** @todo parser
        assert s[2] := 'def'
         */
    }
}
