import java.util.HashMap
import java.util.Map

/** 
 * Tests creating Maps in Groovy
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class MapConstructionTest extends GroovyTestCase {

    void testMap() {
        m = [ 1 : 'abc', 2 : 'def', 3 : 'xyz' ]

        println(m)

        mtoo = [ 1 : [ "innerKey" : "innerValue" ], 2 : m ]

        println(mtoo)

        assertMap(m)
    }

    testMapAsParameter() {
        assertMap([ 1 : 'abc', 2 : 'def', 3 : 'xyz' ])
    }

    testMapViaHashMap() {
        m = new HashMap()
        m.put(1, 'abc')
        m.put(2, 'def')
        m.put(3, 'xyz')
        assertMap(m)
    }

    assertMap(m) {
        assert m instanceof Map
        assert m.getClass().getName() == "java.util.HashMap"

        result = 0
        text = ""
        for ( e in m ) {
            result = result + e.key
            text = text + e.value
        }
        assert result == 6
        assert text == "abcdefxyz"
	    
        assert m.size() == 3

        assert m[2] == 'def'
    }
}
