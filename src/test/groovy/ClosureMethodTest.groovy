import java.io.File

/** 
 * Tests the various Closure methods in Groovy
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class ClosureMethodTest extends GroovyTestCase {

    void testListCollect() {
        def list = [1, 2, 3, 4]
        def answer = list.collect( {item -> return item * 2 } )

        assert answer.size() == 4

        def expected = [2, 4, 6, 8]
        assert answer == expected
    }

    void testMapCollect() {
        def map = [1:2, 2:4, 3:6, 4:8]
        def answer = map.collect( {e-> return e.key + e.value } )

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
        def list = ["a", "b", "c"]
        def answer = list.find( {item-> return item == "b" })
        assert answer == "b"

        answer = list.find{item-> return item == "z" }
        assert answer == null
    }

    void testMapFind() {
        def map = [1:2, 2:4, 3:6, 4:8]
        def answer = map.find( {entry-> return entry.value == 6 })
        assert answer != null
        assert answer.key == 3
        assert answer.value == 6

        answer = map.find{entry-> return entry.value == 0 }
        assert answer == null
    }

    void testListFindAll() {
        def list = [20, 5, 40, 2]
        def answer = list.findAll( {item-> return item < 10 } )

        assert answer.size() == 2
        assert answer == [5, 2]
    }

    void testMapFindAll() {
        def map = [1:2, 2:4, 3:6, 4:8]
        def answer = map.findAll( {entry-> return entry.value > 5 })

        assert answer.size() == 2

        def keys = answer.collect( {entry-> return entry.key })
        def values = answer.collect {entry-> return entry.value }

        println("keys " + keys + " values " + values)

        // maps are in hash order so lets sort the results
        keys.sort()
        values.sort()

        assert keys == [3, 4]
        assert values == [6, 8]
    }

    void testListEach() {
        def count = 0

        def list = [1, 2, 3, 4]
        list.each({item-> count = count + item })

        assert count == 10

        list.each{item-> count = count + item }

        assert count == 20
    }

    void testMapEach() {
        def count = 0

        def map = [1:2, 2:4, 3:6, 4:8]
        map.each({e-> count = count + e.value })

        assert count == 20

        map.each({e-> count = count + e.value + e.key })

        assert count == 50
    }

    void testMapEachWith2Params() {
        def count = 0

        def map = [1:2, 2:4, 3:6, 4:8]
        map.each {key, value -> count = count + value }

        assert count == 20

        map.each {key, value -> count = count + value + key }

        assert count == 50
    }

    void testListEvery() {
        assert [1, 2, 3, 4].every {i-> return i < 5 }
        assert [1, 2, 7, 4].every {i-> return i < 5 } == false
    }

    void testListAny() {
        assert [1, 2, 3, 4].any {i-> return i < 5 }
        assert [1, 2, 3, 4].any {i-> return i > 3 }
        assert [1, 2, 3, 4].any {i-> return i > 5 } == false
    }

    void testJoin() {
        def value = [1, 2, 3].join('-')
        assert value == "1-2-3"
    }

    void testListReverse() {
        def value = [1, 2, 3, 4].reverse()
        assert value == [4, 3, 2, 1]
    }

    void testListInject() {
        def value = [1, 2, 3].inject('counting: ') { str, item -> str + item }
        assert value == "counting: 123"

        value = [1, 2, 3].inject(0) { c, item -> c + item }
        assert value == 6
    }

    void testDump() {
        def text = dump()
        println("Dumping object ${text}")
        assert text != null && text.startsWith("<")
    }

    void testInspect() {
        def text = [1, 2, 'three'].inspect()
        println("Inspecting ${text}")
        assert text == '[1, 2, "three"]'
    }

    void testEachLine() {
        def file = new File("src/test/groovy/Bar.groovy")
        if(file.exists() == false) {
            file = new File("Bar.groovy")
        }

        println("Contents of file: " + file)

        file.eachLine { line -> println(line) }

        println("")
    }

    void testForEachLine() {
        def file = new File("src/test/groovy/Bar.groovy")
        if(file.exists() == false) {
            file = new File("Bar.groovy")
        }

        println("For loop to display contents of file: " + file)

        for (line in file) { println(line) }

        println("")
    }

    void testReadLines() {
        def file = new File("src/test/groovy/Bar.groovy")
        if(file.exists() == false) {
            file = new File("Bar.groovy")
        }

        def lines = file.readLines()

        assert lines != null
        assert lines.size() > 0

        println("File has: " + lines.size() + " line(s)")
    }

    void testEachFile() {
        def file = new File("src/test/groovy")
        if(!file.exists()) {
            file = new File(".")
        }

        println("Closure loop to display contents of dir: " + file)

        file.eachFile { f -> println(f.getName()) }
        
        println("")
    }
    
    void testTokenize() {
        def text = "hello-there-how-are-you"
        
        def answer = []
        for (i in text.tokenize('-')) {
            answer.add(i)
        }
        assert answer == ['hello', 'there', 'how', 'are', 'you']
    }
    
    void testUpto() {
        def answer = []
        
        1.upto(5) { answer.add(it) }
        
        assert answer == [1, 2, 3, 4, 5]
    }
}
