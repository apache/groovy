package groovy.bugs

import groovy.xml.StreamingMarkupBuilder

class Groovy_2473Bug extends GroovyTestCase {
	void testBug() {
      def w = new StringWriter()
      def b = new StreamingMarkupBuilder()
    
      w << b.bind() {
        mkp.xmlDeclaration()
        a("\u0083")
      }
  
      assertEquals('<?xml version="1.0"?>\n<a>&#x83;</a>', w.toString())
      
      b.encoding = "UTF-8"
      
      w = new StringWriter()
      
      w << b.bind() {
        mkp.xmlDeclaration()
        a("\u0083")
      }
  
      assertEquals('<?xml version="1.0" encoding="UTF-8"?>\n<a>\u0083</a>', w.toString())
    }
}