package groovy.bugs

import java.io.StringWriter
import groovy.xml.MarkupBuilder

/** 
 * Tests that special XML chars are entitized by MarkupBuilder.
 *
 * @author <a href="mailto:scottstirling@rcn.com">Scott Stirling</a>
 *
 * @version Revision 1.2 $
 *
 *     Fix the crlf handling, depending on operating system.
 *
 *     @author <a href="mailto:phkim@cluecom.co.kr">Pilho Kim</a>
 */
class Groovy593_Bug extends GroovyTestCase {
    
    StringWriter writer = new StringWriter()
    MarkupBuilder chars = new MarkupBuilder(writer)
    XmlParser parser = new XmlParser()
    String expectedXML = <<<EOF
<chars>
  <ampersand a='&amp;'>&amp;</ampersand>
  <quote>&quot;</quote>
  <lessthan attr='value'>chars: &amp; &lt; &gt; &quot;</lessthan>
  <element attr='value 1 &amp; 2'>chars: &amp; &lt; &gt; &quot; in middle</element>
  <greaterthan>&gt;</greaterthan>
</chars>
EOF

    void testBug() {
        // XML characters to test with
        chars.chars {
            ampersand(a: "&", "&")
            quote("\'")
            lessthan(attr: "value", "chars: & < > \'") 
            element(attr: "value 1 & 2", "chars: & < > \' in middle")
            greaterthan(">")
        }
        //DEBUG
        //println writer

        // Test MarkupBuilder state with expectedXML
	// Handling the cr lf characters, depending on operating system. 
        outputValue = writer.toString()
        if (expectedXML.indexOf("\r\n") >= 0 && outputValue.indexOf("\r\n") < 0) {
                assert expectedXML.replaceAll("\r\n", "\n").length() == outputValue.length()
                assertEquals(expectedXML.replaceAll("\r\n", "\n"), outputValue)
        }
        else if (expectedXML.indexOf("\r\n") < 0 && outputValue.indexOf("\r\n") >= 0) {
                assert expectedXML.length() == outputValue.replaceAll("\r\n", "\n").length()
                assertEquals(expectedXML, outputValue.replaceAll("\r\n", "\n"))
        }

        // parser will throw a SAXParseException if XML is not valid
        parser.parseText(writer.toString())
    }
}

