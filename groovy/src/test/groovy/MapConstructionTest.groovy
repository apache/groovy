package groovy

/** 
 * Tests creating Maps in Groovy
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class MapConstructionTest extends GroovyTestCase {

    void testMap() {
        def m = [ 1 : 'abc', 2 : 'def', 3 : 'xyz' ]

        println(m)

        def mtoo = [ 1 : [ "innerKey" : "innerValue" ], 2 : m ]

        println(mtoo)

        assertMap(m)
    }

    def testMapAsParameter() {
        assertMap([ 1 : 'abc', 2 : 'def', 3 : 'xyz' ])
    }

    def testMapViaHashMap() {
        def m = new HashMap()
        m.put(1, 'abc')
        m.put(2, 'def')
        m.put(3, 'xyz')
        assertMap(m)
    }

    void assertMap(m) {
        assert m instanceof Map
        // do not test the final type, i.e. assumiong m is a HashMap

        def result = 0
        def text = ""
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
