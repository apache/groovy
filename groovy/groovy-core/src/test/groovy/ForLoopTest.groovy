import groovy.bugs.TestSupport

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
}
