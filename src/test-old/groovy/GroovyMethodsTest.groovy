import java.io.InputStreamReader

/** 
 * Tests the various new Groovy methods
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author Guillaume Laforge
 * @version $Revision$
 */
class GroovyMethodsTest extends GroovyTestCase {
	void testCollect() {
		assert [2, 4, 6].collect { it * 2} == [4, 8, 12]
		
		answer = [2, 4, 6].collect(new Vector()) { it * 2}
		
		assert answer[0] == 4
		assert answer[1] == 8
		assert answer[2] == 12
	}

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
        answer = list.grep(~".*a.*")

        assert answer == ["James", "Guillaume", "Sam"]

        answer = list.grep(~"B.b")

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

    void testReverseEach() {
        l = ["cheese", "loves", "Guillaume"]
        expected = ["Guillaume", "loves", "cheese"]
        
        answer = []
        l.reverseEach{ answer << it }

        assert answer == expected
    }
    
    void testGrep() {
	    	list = ["Guillaume", "loves", "cheese"]
	    	
	    	answer = list.grep(~".*ee.*")
	    	assert answer == ["cheese"]
	    	
	    	list = [123, "abc", 4.56]
	    	answer = list.grep(String)
	    	assert answer == ["abc"]
	    	
	    	list = [4, 2, 7, 3, 6, 2]
	    	answer = list.grep(2..3)
	    	assert answer == [2, 3, 2]
	}
    
    void testMapGetWithDefault() {
    		map = [:]
    		
    		assert map.foo == null
    		
    		map.get("foo", []).add(123)
    		
    		assert map.foo == [123]
    		
    		map.get("bar", [:]).get("xyz", [:]).cheese = 123
    		
    		assert map.bar.xyz.cheese == 123
    		assert map.size() == 2
    	}
    		
    void DISABLE_testExecuteCommandLineProcessUsingAString() {
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
	    	new InputStreamReader(process.in).eachLine { line |
	    		println line
	    		count++
	    	}
	    	println ""
	    	
	    	process.waitFor()
	    	value = process.exitValue()
	    	println "Exit value of command line is ${value}"
	    	
	    	assert count > 1
    }
    
    void DISABLED_testExecuteCommandLineProcessAndUseWaitForOrKill() {
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
    	
    	process.waitForOrKill(1000)
    	value = process.exitValue()
    	println "Exit value of command line is ${value}"
    	
    	
    	process = cmd.execute()
    	
    	process.waitForOrKill(1)
    	value = process.exitValue()
    	println "Exit value of command line is ${value}"
    }
    
    void testDisplaySystemProperties() {
    	println "System properties are..."
    	properties = System.properties
    	keys = properties.keySet().sort()
    	for (k in keys) { 
    		println "${k} = ${properties[k]}"
    	}
    }

    void testMax() {
        assert [-5, -3, -1, 0, 2, 4].max{ it * it } == -5
    }

    void testMin() {
        assert [-5, -3, -1, 0, 2, 4].min{ it * it } == 0
    }
    
    void testSort() {
    	assert [-5, -3, -1, 0, 2, 4].sort { it*it } == [0, -1, 2, -3, 4, -5]
    }
}
