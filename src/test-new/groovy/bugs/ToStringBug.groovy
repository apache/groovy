/**
 * @version $Revision$
 */
class ToStringBug extends GroovyTestCase {
     
    void testBug() {
    	println "Starting test"
    	
    	value = toString()
    	assert value != null
    	
    	println value
    	println "Found value ${value}"
    }
    
    String toString() {
    	return super.toString() + "[hey]"
    }
}