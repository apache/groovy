package org.codehaus.groovy.runtime;

/** 
 * Test withWriter with inner loop closing the stream
 * in advance.
 * Some methods e.g. transformChar() close the streams.
 * If used inside a withWriter(), it must not lead to 
 * problems
 * 
 * @author Joachim Baumann</a>
 * @version $Revision$
 */

class WithResourceStreamClosedTest extends GroovyTestCase {

	void testWithWriterStreamClosed() {

	    def outer = new StringWriter()
	    def reader = new StringReader("Hallo Welt")

	    outer.withWriter { writer ->
	        reader.transformChar(writer) { it }
	      
	    }
		assert outer.toString() == "Hallo Welt"
	}
	void testWithOutputStreamClosed() {
	    def os = new ByteArrayOutputStream()
	    os.withStream { out ->
	        os.close()     
	    }
	}

}
