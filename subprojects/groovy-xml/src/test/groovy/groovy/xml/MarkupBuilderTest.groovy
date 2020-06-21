/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy.xml

/**
 * Tests for MarkupBuilder. The tests directly in this file are specific
 * to MarkupBuilder. Functionality in common with StreamingMarkupBuilder
 * is tested in the BuilderTestSupport parent class.
 */
class MarkupBuilderTest extends BuilderTestSupport {
    private StringWriter writer
    private MarkupBuilder xml

    protected void setUp() {
        writer = new StringWriter()
        xml = new MarkupBuilder(writer)
    }

    private assertExpectedXmlDefault(expectedXml) {
        checkXml expectedXml, writer
    }

    /**
     * It is not recommended practice to use the value attribute
     * when also using nested content as there is no way to specify
     * the ordering of such mixed content. The default behaviour is
     * to include the value as the first node in the resulting xml.
     *
     * StreamingMarkupBuilder excludes this behavior and requires
     * yield or yieldUnescaped exclusively. MarkupBuilder also supports
     * the yield approach but retains this style for backwards compatibility.
     */
    void testSmallTreeWithTextAndAttributes() {
        def m = {
            root1('hello1', a: 5, b: 7) {
                elem1('hello2', c: 4) {
                    elem2('hello3', d: 4)
                }
                elem1('hello2', c: 4) {
                    elem2('hello3')
                    elem2('hello3', d: 4)
                }
                elem1('hello2', c: 4) {
                    elem2('hello3', d: 4)
                    elem2('hello3')
                }
                elem1('hello2', c: 4) {
                    elem2(d: 4)
                    elem2('hello3', d: 4)
                }
                elem1('hello2', c: 4) {
                    elem2('hello3', d: 4)
                    elem2(d: 4)
                }
                elem1('hello2') {
                    elem2('hello3', d: 4)
                    elem2(d: 4)
                }
            }
        }
        assertExpectedXml m, '''\
<root1 a='5' b='7'>hello1<elem1 c='4'>hello2<elem2 d='4'>hello3</elem2>
</elem1>
<elem1 c='4'>hello2<elem2>hello3</elem2>
<elem2 d='4'>hello3</elem2>
</elem1>
<elem1 c='4'>hello2<elem2 d='4'>hello3</elem2>
<elem2>hello3</elem2>
</elem1>
<elem1 c='4'>hello2<elem2 d='4' />
<elem2 d='4'>hello3</elem2>
</elem1>
<elem1 c='4'>hello2<elem2 d='4'>hello3</elem2>
<elem2 d='4' />
</elem1>
<elem1>hello2<elem2 d='4'>hello3</elem2>
<elem2 d='4' />
</elem1>
</root1>'''
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
        assertEquals expectedXml, fixEOLs(writer.toString())
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
  <null />
  <nullAttribute t1='' />
  <emptyWithAttributes attr1='set' />
  <emptyAttribute t1='' />
  <parent key='value'>
    <label for='usernameId'>Username: </label>
    <input name='test' id='1' />
  </parent>
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
            'null'(null)
            nullAttribute(t1:null)
            emptyWithAttributes(attr1:'set')
            emptyAttribute(t1:'')
            parent(key:'value'){
                label(for:'usernameId', 'Username: ')
                input(name:'test', id:1)
            }
        }

        assertEquals expectedXml, fixEOLs(writer.toString())
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

        assertEquals expectedXml, fixEOLs(writer.toString())
    }

    void testCallingMethod() {
       // this test is to ensure compatibility only
       xml.p {
         def aValue = myMethod([:]).value
         em(aValue)
      }

      assertExpectedXmlDefault '<p><em>call to outside</em></p>'
    }

    void testOmitAttributeSettingsKeepBothDefaultCase() {
        xml.element(att1:null, att2:'')
        assertExpectedXmlDefault "<element att1='' att2='' />"
    }

    void testOmitAttributeSettingsOmitNullKeepEmpty() {
        xml.omitNullAttributes = true
        xml.element(att1:null, att2:'')
        assertExpectedXmlDefault "<element att2='' />"
    }

    void testOmitAttributeSettingsKeepNullOmitEmpty() {
        xml.omitEmptyAttributes = true
        xml.element(att1:null, att2:'')
        assertExpectedXmlDefault "<element att1='' />"
    }

    void testOmitAttributeSettingsOmitBoth() {
        xml.omitEmptyAttributes = true
        xml.omitNullAttributes = true
        xml.element(att1:null, att2:'')
        assertExpectedXmlDefault "<element />"
    }

    void testDelegateOnlyToSkipInternalClosureMethods() {
        def items = {
          pizza(price: 8.5)
          curry(price: 8)
          burger(price: 7.5)
        }
        items.resolveStrategy = Closure.DELEGATE_ONLY
        xml.menu(items)
        assertExpectedXmlDefault '''
            <menu>
              <pizza price='8.5' />
              <curry price='8' />
              <burger price='7.5' />
            </menu>
        '''
    }

    void testMkpYield() {
        xml.mkp.yieldUnescaped("<?xml version='1.0' encoding='UTF-8'?>\n")
        xml.element {
            mkp.yield("<>")
            xml.inner("foo") { mkp.yield('bar') }
            mkp.yieldUnescaped("\n  <inside>wow</inside>")
        }
        String expectedXml = '''\
<?xml version='1.0' encoding='UTF-8'?>
<element>&lt;&gt;
  <inner>foobar</inner>
  <inside>wow</inside>
</element>'''
        assertEquals expectedXml, fixEOLs(writer.toString())
    }

    void testWithIndentPrinter() {
        xml = new MarkupBuilder(new IndentPrinter(new PrintWriter(writer), "", false))
        xml.element(att1:'attr') { subelement('foo') }
        assert writer.toString() == "<element att1='attr'><subelement>foo</subelement></element>"
    }

    void testEscapingOfAttributes() {
        def writer = new StringWriter()
        def builder = new groovy.xml.MarkupBuilder(writer)
        builder.a(href: "http://www.example.com?foo=1&bar=2")
        assert writer.toString().contains('http://www.example.com?foo=1&amp;bar=2')
    }

    void testNoEscapingOfAttributes() {
        def writer = new StringWriter()
        def builder = new groovy.xml.MarkupBuilder(writer)
        builder.escapeAttributes = false
        builder.a(href: "http://www.example.com?foo=1&bar=2")
        assert writer.toString().contains('http://www.example.com?foo=1&bar=2')
    }

    private myMethod(x) {
      x.value='call to outside'
      return x
    }

    protected assertExpectedXml(Closure markup, String expectedXml) {
        assertExpectedXml markup, null, expectedXml
    }

    protected assertExpectedXml(Closure markup, Closure configureBuilder, String expectedXml) {
        def writer = new StringWriter()
        def builder = new MarkupBuilder(writer)
        if (configureBuilder) configureBuilder(builder)
        markup.delegate = builder
        markup()
        checkXml(expectedXml, writer)
    }

}
