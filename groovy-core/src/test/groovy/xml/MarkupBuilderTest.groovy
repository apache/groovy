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
    private StringWriter writer
    private MarkupBuilder xml

    protected void setUp() {
        writer = new StringWriter()
        xml = new MarkupBuilder(writer)
    }

    /**
     * Main test method. Checks that well-formed XML is generated
     * and that the appropriate characters are escaped with the
     * correct entities.
     */
    void testBuilder() {
        String expectedXml = '''<chars>
  <ampersand a='&amp;'>&amp;</ampersand>
  <quote attr='"'>"</quote>
  <apostrophe attr='&apos;'>'</apostrophe>
  <lessthan attr='value'>chars: &amp; &lt; &gt; '</lessthan>
  <element attr='value 1 &amp; 2'>chars: &amp; &lt; &gt; " in middle</element>
  <greaterthan>&gt;</greaterthan>
  <emptyElement />
</chars>'''

        // Generate the markup.
        xml.chars {
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
        assertEquals(expectedXml, fixEOLs(writer.toString()))
    }

    /**
     * Tests the builder with double quotes for attribute values.
     */
    void testBuilderWithDoubleQuotes() {
        String expectedXml = '''<chars>
  <ampersand a="&amp;">&amp;</ampersand>
  <quote attr="&quot;">"</quote>
  <apostrophe attr="'">'</apostrophe>
  <lessthan attr="value">chars: &amp; &lt; &gt; '</lessthan>
  <element attr="value 1 &amp; 2">chars: &amp; &lt; &gt; " in middle</element>
  <greaterthan>&gt;</greaterthan>
  <emptyElement />
</chars>'''

        // Generate the markup.
        xml.doubleQuotes = true
        xml.chars {
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
        assertEquals(expectedXml, fixEOLs(writer.toString()))
    }

    /**
     * Tests that MarkupBuilder escapes element content correctly, even
     * when the content contains line-endings.
     */
    void testEscapingMultiLineContent() {
        def expectedXml = 
'''<element>This is multi-line content with characters, such as &lt;, that
require escaping. The other characters consist of:

    * &gt; - greater than
    * &amp; - ampersand
</element>'''

        // Generate the markup.
        xml.element('''This is multi-line content with characters, such as <, that
require escaping. The other characters consist of:

    * > - greater than
    * & - ampersand
''')

        // Compare the generated markup with the 'expectedXml' string.
        assertEquals(expectedXml, fixEOLs(writer.toString()))
    }

    /**
     * Checks against a regression bug whereby some empty elements were
     * not closed.
     */
    void testMarkupForClosingTags() {
        def expectedXml =
'''<ELEM1>
  <ELEM2 type='2' id='first'>
    <ELEM3A id='first' />
    <ELEM3B type='3'>text</ELEM3B>
  </ELEM2>
  <ELEM2 type='2' id='second'>
    <ELEM3A id='second' />
    <ELEM3B type='3'>text</ELEM3B>
  </ELEM2>
  <ELEM2 type='2' id='third'>
    <ELEM3A id='third' />
    <ELEM3B type='3'>text</ELEM3B>
  </ELEM2>
</ELEM1>'''

        // Generate the XML.
        def list = ['first', 'second', 'third']

        xml.ELEM1() {
            list.each(){ r ->
                xml.ELEM2(id:r, type:'2') {
                    xml.ELEM3A(id:r)
                    xml.ELEM3B(type:'3', 'text')
                }
            }
        }

        // Check that the MarkupBuilder has generated the expected XML.
        assertEquals(expectedXml, fixEOLs(writer.toString()))
    }  
}
