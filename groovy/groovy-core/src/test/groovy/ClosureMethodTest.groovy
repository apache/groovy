/** 
 * Tests the various Closure methods in Groovy
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class ClosureMethodTest extends GroovyTestCase {

	property count

    void testListCollect() {
        list = [1, 2, 3, 4]
        answer = list.collect( {| item | return item * 2 } )

        assert answer.size() := 4
        
        expected = [2, 4, 6, 8]
        assert answer := expected
    }

    void testMapCollect() {
        map = [1:2, 2:4, 3:6, 4:8]
        answer = map.collect( {| e | return e.key + e.value } )
		
		// lest sort the results since maps are in hash code order
		answer = answer.sort()
		
        assert answer.size() := 4
        assert answer := [3, 6, 9, 12]
        assert answer.get(0) := 3
        assert answer.get(1) := 6
        assert answer.get(2) := 9
        assert answer.get(3) := 12
    }

    void testListFind() {
        list = ["a", "b", "c"]
        answer = list.find( {| item | return item := "b" })
        assert answer := "b"
        
        answer = list.find{| item | return item := "z" }
        assert answer == null
    }
    
    void testMapFind() {
        map = [1:2, 2:4, 3:6, 4:8]
        answer = map.find( {| entry | return entry.value := 6 })
        assert answer != null
        assert answer.key := 3
        assert answer.value := 6
        
        answer = map.find{| entry | return entry.value := 0 }
        assert answer == null
    }

    void testListSelect() {
        list = [20, 5, 40, 2]
        answer = list.select( {| item | return item < 10 } )

        assert answer.size() := 2
        assert answer := [5, 2]
    }
    
    void testMapSelect() {
        map = [1:2, 2:4, 3:6, 4:8]
        answer = map.select( {| entry | return entry.value > 5 })

        assert answer.size() := 2
        
        keys = answer.collect( {| entry | return entry.key })
        values = answer.collect {| entry | return entry.value }
        
        // maps are in hash order so lets sort the results            
        assert keys.sort() := [3, 4]
        assert values.sort() := [6, 8]
    }

    void testListEach() {
        count = 0

        list = [1, 2, 3, 4]
        list.each({| item | count = count + item })
		
        assert count := 10

        list.each{| item | count = count + item }
		
        assert count := 20
    }

    void testMapEach() {
        count = 0

        map = [1:2, 2:4, 3:6, 4:8]
        map.each({| e | count = count + e.value })

        assert count := 20
        
        /** @todo parser
        map.each({| e | count = count + e.value + e.key })
		
        assert count := 30
        */
    }
}
