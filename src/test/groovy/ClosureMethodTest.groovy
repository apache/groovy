import java.io.File

/** 
 * Tests the various Closure methods in Groovy
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class ClosureMethodTest extends GroovyTestCase {

	property count

    void testListMap() {
        list = [1, 2, 3, 4]
        answer = list.map( {item| return item * 2 } )

        assert answer.size() == 4
        
        expected = [2, 4, 6, 8]
        assert answer == expected
    }

    void testMapMap() {
        map = [1:2, 2:4, 3:6, 4:8]
        answer = map.map( {e| return e.key + e.value } )
		
		// lest sort the results since maps are in hash code order
		answer = answer.sort()
		
        assert answer.size() == 4
        assert answer == [3, 6, 9, 12]
        assert answer.get(0) == 3
        assert answer.get(1) == 6
        assert answer.get(2) == 9
        assert answer.get(3) == 12
    }

    void testListFind() {
        list = ["a", "b", "c"]
        answer = list.find( {item| return item == "b" })
        assert answer == "b"
        
        answer = list.find{item| return item == "z" }
        assert answer == null
    }
    
    void testMapFind() {
        map = [1:2, 2:4, 3:6, 4:8]
        answer = map.find( {entry| return entry.value == 6 })
        assert answer != null
        assert answer.key == 3
        assert answer.value == 6
        
        answer = map.find{entry| return entry.value == 0 }
        assert answer == null
    }

    void testListFindAll() {
        list = [20, 5, 40, 2]
        answer = list.findAll( {item| return item < 10 } )

        assert answer.size() == 2
        assert answer == [5, 2]
    }
    
    void testMapFindAll() {
        map = [1:2, 2:4, 3:6, 4:8]
        answer = map.findAll( {entry| return entry.value > 5 })

        assert answer.size() == 2
        
        keys = answer.map( {entry| return entry.key })
        values = answer.map {entry| return entry.value }

		/** @todo parser        
        println("keys " + keys + " values " + values)
        println("keys " + keys)
        println("values " + values)
        "keys " + keys + " values " + values.println()
        text = "keys " + keys + " values " + values
        */
		
        // maps are in hash order so lets sort the results       
        keys.sort() 
        values.sort() 
        
        assert keys == [3, 4]
        assert values == [6, 8]
    }

    void testListEach() {
        count = 0

        list = [1, 2, 3, 4]
        list.each({item| count = count + item })
		
        assert count == 10

        list.each{item| count = count + item }
		
        assert count == 20
    }

    void testMapEach() {
        count = 0

        map = [1:2, 2:4, 3:6, 4:8]
        map.each({e| count = count + e.value })

        assert count == 20
        
        /** @todo parser
        map.each({e| count = count + e.value + e.key })
		
        assert count == 30
        */
    }
    
    void testListEvery() {
        assert [1, 2, 3, 4].every {i| return i < 5 }
        assert [1, 2, 7, 4].every {i| return i < 5 } == false
    }

    void testListAny() {
        assert [1, 2, 3, 4].any {i| return i < 5 }
        assert [1, 2, 3, 4].any {i| return i > 3 }
        assert [1, 2, 3, 4].any {i| return i > 5 } == false
    }
    
    void testJoin() {
        value = [1, 2, 3].join('-')
        assert value == "1-2-3"
    }
    
    void testListReverse() {
        value = [1, 2, 3, 4].reverse()
        assert value == [4, 3, 2, 1]
    }
    
    void testInspect() {
        text = inspect()
        println(text)
        assert text != null && text.startsWith("<")
    }

    void testEachLine() {
        file = new File("src/test/groovy/Bar.groovy")
        
        println("Contents of file: " + file)
        
        file.eachLine { line | println(line) }
        
        println("")
    }
    
    void testReadLines() {
        file = new File("src/test/groovy/Bar.groovy")

		lines = file.readLines()
		
		assert lines != null
		assert lines.size() > 0

		/** @todo parser		        
        println("File has: " + lines.size() + " lines")
        */
        println("File has number of lines: " + lines.size())
    }
    
    void testEachFile() {
        file = new File("src/test/groovy")
        
        println("Contents of dir: " + file)
        
        file.eachFile { f | println(f.getName()) }
        
        println("")
    }
}
