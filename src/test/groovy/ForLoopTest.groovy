import groovy.bugs.TestSupport

import java.util.Vector

class ForLoopTest extends GroovyTestCase {

	property x
	
    void testRange() {
        x = 0

        for ( i in 0..9 ) {
            x = x + i
        }

        assert x == 45
    }

    void testList() {
        x = 0
		
        for ( i in [0, 1, 2, 3, 4] ) {
            x = x + i
        }

        assert x == 10
    }

    void testArray() {
        array = (0..4).toArray()
        
        println "Class: ${array.class} for array ${array}"
        
        x = 0
        
        for ( i in array ) {
            x = x + i
        }

        assert x == 10
	}
    
    void testIntArray() {
        array = TestSupport.getIntArray()
        
        println "Class: ${array.class} for array ${array}"
        
        x = 0
        
        for ( i in array ) {
            x = x + i
        }

        assert x == 15
    }
    
    void testString() {
        text = "abc"
        
        list = []
        for (c in text) {
            list.add(c)
        }
        
        assert list == ["a", "b", "c"]
    }
    
    void testVector() {
        vector = new Vector()
        vector.addAll( [1, 2, 3] )
        
        answer = []
        for (i in vector.elements()) {
            answer << i
        }
        assert answer == [1, 2, 3]
    }
}
