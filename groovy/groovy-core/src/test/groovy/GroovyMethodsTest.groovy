import java.io.InputStreamReader

/** 
 * Tests the various new Groovy methods
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class GroovyMethodsTest extends GroovyTestCase {

    void testJoin() {
        assert [2, 4, 6].join("-") == "2-4-6"
        assert ["edam", "cheddar", "brie"].join(", ") == 'edam, cheddar, brie'
        
        println( ["abc", 5, 2.34].join(", ") )
    }
    
    void testTimes() {
        count = 0
        5.times { i | count = count + i }
        assert count == 10
        
        count = 0
        temp = 5
        temp.times { i | count = count + i }
        
        assert count == 10
    }
    
    void testArraySubscript() {
        list = [1, 2, 3, 4]
        array = list.toArray()
        
        value = array[2]
        
        assert value == 3
        
        array[0] = 9
        
       	assert array[0] == 9
    }
    
    void testToCharacterMethod() {
    	s = 'c'
    	x = s.toCharacter()
    	
    	assert x instanceof Character
    }

    void testListGrep() {
        list = ["James", "Bob", "Guillaume", "Sam"]
        answer = list.grep(".*a.*")

        assert answer == ["James", "Guillaume", "Sam"]

        answer = list.grep("B.b")

        assert answer == ["Bob"]
    }

    void testCollectionToList() {
        c = [1, 2, 3, 4, 5] // but it's a list
        l = c.toList()

        assert l.containsAll(c)
        assert c.size() == l.size()
    }

    void testJoinString() {
        arr = new String[] {"a", "b", "c", "d"}
        joined = arr.join(", ")

        assert joined == "a, b, c, d"
    }

    
    void testExecuteCommandLineProcessUsingAString() {
    	/** @todo why does this not work
    	javaHome = System.getProperty('java.home', '')
    	cmd = "${javaHome}/bin/java -version"
    	*/
    	
    	cmd = "ls -l"
    	if (System.getProperty('os.name', '').contains('Win')) {
    		cmd = "dir"
    	}
    	
    	println "executing command: ${cmd}"
    	
    	process = cmd.execute()
    	//process = "ls -l".execute()
    	
    	// lets have an easier way to do this!
    	count = 0
    	
    	println "Read the following lines..."

    	/** @todo we should simplify the following line!!! */
    	new InputStreamReader(process.inputStream).eachLine { line |
    		println line
    		count++
    	}
    	println ""
    	
    	process.waitFor()
    	value = process.exitValue()
    	println "Exit value of command line is ${value}"
    	
    	assert count > 1
    }
    
    void testDisplaySystemProperties() {
    	println "System properties are..."
    	properties = System.properties
    	keys = properties.keySet().sort()
    	for (k in keys) { 
    		println "${k} = ${properties[k]}"
    	}
    }
}
