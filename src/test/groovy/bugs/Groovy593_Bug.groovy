package groovy.bugs

import java.io.StringWriter
import groovy.xml.MarkupBuilder

/** 
 * Tests that special XML chars are entitized by MarkupBuilder.
 *
 * @author <a href="mailto:scottstirling@rcn.com">Scott Stirling</a>
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
        assertEquals(expectedXML, writer.toString())

        // parser will throw a SAXParseException if XML is not valid
        parser.parseText(writer.toString())
    }
}

