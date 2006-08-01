package groovy.xml;

import groovy.util.GroovyTestCase

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
class MarkupBuilderTest extends GroovyTestCase {
 
    StringWriter writer = new StringWriter()
    MarkupBuilder chars = new MarkupBuilder(writer)
    XmlParser parser = new XmlParser()

    void testBuilder() {
        String expectedXml = 
"""<chars>
  <ampersand a='&amp;'>&amp;</ampersand>
  <quote attr='"'>"</quote>
  <apostrophe attr='&apos;'>'</apostrophe>
  <lessthan attr='value'>chars: &amp; &lt; &gt; '</lessthan>
  <element attr='value 1 &amp; 2'>chars: &amp; &lt; &gt; " in middle</element>
  <greaterthan>&gt;</greaterthan>
  <emptyElement />
</chars>"""

        // Generate the markup.
        chars.chars {
            ampersand(a: "&", "&")
            quote(attr: "\"", "\"")
            apostrophe(attr: "'", "'")
            lessthan(attr: "value", "chars: & < > '") 
            element(attr: "value 1 & 2", "chars: & < > \" in middle")
            greaterthan(">")
            emptyElement()
        }

        // Compare the MarkupBuilder generated XML with the 'expectedXml'
        // string.
        def outputValue = writer.toString()
        if (expectedXml.indexOf("\r\n") >= 0)  expectedXml = expectedXml.replaceAll("\r\n", "\n");
        if (outputValue.indexOf("\r\n") >= 0)  outputValue = outputValue.replaceAll("\r\n", "\n");
        assertEquals(expectedXml, outputValue)
    }

    /**
     * Tests that MarkupBuilder escapes element content correctly, even
     * when the content contains line-endings.
     */
    void testEscapingMultiLineContent() {
        def expectedXml = 
"""<element>This is multi-line content with characters, such as &lt;, that
require escaping. The other characters consist of:

    * &gt; - greater than
    * &amp; - ampersand
</element>"""

        // Generate the markup.
        chars.element("""This is multi-line content with characters, such as <, that
require escaping. The other characters consist of:

    * > - greater than
    * & - ampersand
""")

        // Compare the generated markup with the 'expectedXml' string.
        def outputValue = writer.toString()
        if (expectedXml.indexOf("\r\n") >= 0)  expectedXml = expectedXml.replaceAll("\r\n", "\n");
        if (outputValue.indexOf("\r\n") >= 0)  outputValue = outputValue.replaceAll("\r\n", "\n");
        assertEquals(expectedXml, outputValue)
    }
}
