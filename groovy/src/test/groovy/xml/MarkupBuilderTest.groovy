/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.xml

import org.custommonkey.xmlunit.*

/**
 * Tests that special XML chars are entitized by MarkupBuilder.
 *
 * @version $Revision: 1.4 $
 *
 *   @author Scott Stirling
 *   @author Pilho Kim
 *   @author Paul King
 */
class MarkupBuilderTest extends GroovyTestCase {
    private StringWriter writer
    private MarkupBuilder xml

    protected void setUp() {
        writer = new StringWriter()
        xml = new MarkupBuilder(writer)
        XMLUnit.setIgnoreWhitespace(true)
    }

    private assertExpectedXml(expectedXml) {
        def xmlDiff = new Diff(expectedXml, writer.toString())
        assert xmlDiff.similar(), xmlDiff.toString()
    }

    void testSmallTree() {
        xml.root1(a:5, b:7) {
            elem1('hello1')
            elem2('hello2')
            elem3(x:7)
        }
        assertExpectedXml('''\
<root1 a='5' b='7'>
  <elem1>hello1</elem1>
  <elem2>hello2</elem2>
  <elem3 x='7' />
</root1>''')
    }

    void testTree() {
        xml.root2(a:5, b:7) {
            elem1('hello1')
            elem2('hello2')
            nestedElem(x:'abc', y:'def') {
                child(z:'def', nulltest:null)
                child2()
            }
            nestedElem2(z:'zzz') {
                child(z:'def')
                child2("hello")
            }
        }
        assertExpectedXml('''\
<root2 a='5' b='7'>
  <elem1>hello1</elem1>
  <elem2>hello2</elem2>
  <nestedElem x='abc' y='def'>
    <child z='def' />
    <child2 />
  </nestedElem>
  <nestedElem2 z='zzz'>
    <child z='def' />
    <child2>hello</child2>
  </nestedElem2>
</root2>''')
    }

    void testContentAndDataInMarkup() {
        xml.a(href:"http://groovy.codehaus.org", "groovy")
        assertExpectedXml("<a href='http://groovy.codehaus.org'>groovy</a>")
    }

    void testMarkupWithColonsAndNamespaces() {
        def expectedXml = '''\
<ns1:customer-description>
  <last-name>Laforge</last-name>
  <first-name>
    <first>Guillaume</first>
    <initial-letters>A.J.</initial-letters>
  </first-name>
</ns1:customer-description>'''
        xml."ns1:customer-description"{
            "last-name"("Laforge")
            "first-name"{
                first("Guillaume")
                "initial-letters"("A.J.")
            }
        }
        assertEquals(expectedXml, fixEOLs(writer.toString()))
    }

    /**
     * Main test method. Checks that well-formed XML is generated
     * and that the appropriate characters are escaped with the
     * correct entities.
     */
    void testBuilder() {
        String expectedXml = '''\
<chars>
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

        assertEquals(expectedXml, fixEOLs(writer.toString()))
    }

    /**
     * Tests the builder with double quotes for attribute values.
     */
    void testBuilderWithDoubleQuotes() {
        String expectedXml = '''\
<chars>
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

        assertEquals(expectedXml, fixEOLs(writer.toString()))
    }

    /**
     * Tests that MarkupBuilder escapes element content correctly, even
     * when the content contains line-endings.
     */
    void testEscapingMultiLineContent() {

        // Generate the markup.
        xml.element('''This is multi-line content with characters, such as <, that
require escaping. The other characters consist of:

    * > - greater than
    * & - ampersand
''')

        assertExpectedXml('''\
<element>This is multi-line content with characters, such as &lt;, that
require escaping. The other characters consist of:

    * &gt; - greater than
    * &amp; - ampersand
</element>''')
    }

    /**
     * Checks against a regression bug whereby some empty elements were
     * not closed.
     */
    void testMarkupForClosingTags() {

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

        assertExpectedXml('''\
<ELEM1>
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
</ELEM1>''')
    }

    void testMixedMarkup() {
        xml.p {
            em('Usually')
            mkp.yield ' Hearts & Diamonds '
            b('beats')
            mkp.yieldUnescaped ' Spades &amp; Clubs'
        }

        assertExpectedXml('''\
<p><em>Usually</em> Hearts &amp; Diamonds <b>beats</b> Spades &amp; Clubs </p>''')
    }
    
    void testCallingMethod() {
       // this test is to ensure compatiblity only
       xml.p { 
         def aValue = myMethod([:]).value
         em(aValue)
      }
      
      assertExpectedXml('<p><em>call to outside</em></p>')
    }
    
    private myMethod(x) {
      x.value='call to outside'
      return x
    }

}
