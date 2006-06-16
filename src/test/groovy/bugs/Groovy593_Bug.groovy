package groovy.bugs

import java.io.StringWriter
import groovy.xml.MarkupBuilder

/** 
 * Tests that special XML chars are entitized by MarkupBuilder.
 *
 * @author <a href="mailto:scottstirling@rcn.com">Scott Stirling</a>
 *
 * @version $Revision: 1.4 $
 *
 *   Fix the cr lf handling of multiline stringon both of linux and Windows XP.
 *   This test should success on Windows XP.
 *
 *   @author Pilho Kim
 */
class Groovy593_Bug extends GroovyTestCase {
    
    StringWriter writer = new StringWriter()
    MarkupBuilder chars = new MarkupBuilder(writer)
    XmlParser parser = new XmlParser()
    String expectedXML = 
"""<chars>
  <ampersand a='&amp;'>&amp;</ampersand>
  <quote attr='"'>"</quote>
  <apostrophe attr='&apos;'>'</apostrophe>
  <lessthan attr='value'>chars: &amp; &lt; &gt; '</lessthan>
  <element attr='value 1 &amp; 2'>chars: &amp; &lt; &gt; " in middle</element>
  <greaterthan>&gt;</greaterthan>
</chars>"""

    void testBug() {
        // XML characters to test with
        chars.chars {
            ampersand(a: "&", "&")
            quote(attr: "\"", "\"")
            apostrophe(attr: "'", "'")
            lessthan(attr: "value", "chars: & < > '") 
            element(attr: "value 1 & 2", "chars: & < > \" in middle")
            greaterthan(">")
        }
        //DEBUG
        //println writer

        // Test MarkupBuilder state with expectedXML
  	    // Handling the cr lf characters, depending on operating system. 
        def outputValue = writer.toString()
        if (expectedXML.indexOf("\r\n") >= 0)  expectedXML = expectedXML.replaceAll("\r\n", "\n");
        if (outputValue.indexOf("\r\n") >= 0)  outputValue = outputValue.replaceAll("\r\n", "\n");
        assertEquals(expectedXML.replaceAll("\r\n", "\n"), outputValue)
        
        // parser will throw a SAXParseException if XML is not valid
        parser.parseText(writer.toString())
    }
    
}

