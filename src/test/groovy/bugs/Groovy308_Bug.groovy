package groovy.bugs

import java.io.*

/**
 * @version $Revision$
 */
class Groovy308_Bug extends GroovyTestCase {
    
    void testBug() {
    	def out = new StringWriter()
    	out << "hello " << "world!"
    	
    	def value = out.toString()
    	assert value == "hello world!"
    	
    	out = new ByteArrayOutputStream()
    	out << "hello " << "world!"

		value = new String(out.toByteArray())
		assert value == "hello world!"
    	    	
    	System.out << "hello" << " world!"
    }
}

