package groovy.bugs

/**
 * @version $Revision$
 */
class ToStringBug extends GroovyTestCase {
     
    void testBug() {
    	println "Starting test"
    	
    	def value = toString()
    	assert value != null
    	
    	println value
    	println "Found value ${value}"
    }
    
    String toString() {
    	return super.toString() + "[hey]"
    }
}