package groovy.bugs

/**
 *  Verifies that DefaultGroovyMethods.transformLine(Reader, Writer, Closure)
 *  actually writes its output.
 */

class Groovy1081_Bug extends GroovyTestCase {
 
    void testShort() {
     	def reader = new StringReader('abc')
		def writer = new StringWriter()

		reader.transformLine(writer) { it }
		
		// Implementation was creating a BufferedWriter, but not flushing it
		assertTrue(writer.toString().startsWith('abc'))
    }
    
}
