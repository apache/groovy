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

import org.custommonkey.xmlunit.XMLUnit
import org.custommonkey.xmlunit.Diff

/**
 * Common test cases for StreamingMarkupBuilder and MarkupBuilder.
 */
abstract class BuilderTestSupport extends GroovyTestCase {

    protected abstract assertExpectedXml(Closure markup, String expectedXml)

    protected abstract assertExpectedXml(Closure markup, Closure configureBuilder, String expectedXml)

    protected checkXml(String expectedXml, StringWriter writer) {
        XMLUnit.ignoreWhitespace = true
        def xmlDiff = new Diff(expectedXml, writer.toString())
        assert xmlDiff.similar(), xmlDiff.toString() + "\n" + writer.toString()
    }

    void testHref() {
        def m = {
            a(href: "http://groovy.codehaus.org", "groovy")
        }
        assertExpectedXml m, "<a href='http://groovy.codehaus.org'>groovy</a>"
    }

    void testHrefDoubleQuotes() {
        def m = {
            a(href: "http://groovy.codehaus.org", "groovy")
        }
        assertExpectedXml m, { it.useDoubleQuotes = true }, '<a href="http://groovy.codehaus.org">groovy</a>'
    }

    void testNestedQuotesSingle() {
        def m = {
            root {
                one( foo:"bar('baz')", 'foo("baz")' )
                two( foo:'bar("baz")', "foo('baz')" )
            }
        }
        assertExpectedXml m, '<root><one foo=\'bar(&apos;baz&apos;)\'>foo("baz")</one><two foo=\'bar("baz")\'>foo(\'baz\')</two></root>'
    }

    void testNestedQuotesDouble() {
        def m = {
            root {
                one( foo:"bar('baz')", 'foo("baz")' )
                two( foo:'bar("baz")', "foo('baz')" )
            }
        }
        assertExpectedXml m, { it.useDoubleQuotes = true }, '<root><one foo="bar(\'baz\')">foo("baz")</one><two foo="bar(&quot;baz&quot;)">foo(\'baz\')</two></root>'
    }

    void testExpandEmptyElements() {
        def m = {
            html {
                head()
                body {
                    textarea(left: 40)
                    textarea('')
                }
            }
        }
        assertExpectedXml m, { it.useDoubleQuotes = true }, '''<html><head/><body><textarea left='40'/><textarea></textarea></body></html>'''
    }

    void testTree() {
        def m = {
            root2(a: 5, b: 7) {
                elem1('hello1')
                elem2('hello2')
                nestedElem(x: 'abc', y: 'def') {
                    child(z: 'def')
                    child2()
                }

                nestedElem2(z: 'zzz') {
                    child(z: 'def')
                    child2("hello")
                }
            }
        }

        assertExpectedXml m, '''\
<root2 a='5' b='7'>
  <elem1>hello1</elem1>
  <elem2>hello2</elem2>
  <nestedElem x='abc' y='def'>
    <child z='def'/>
    <child2/>
  </nestedElem>
  <nestedElem2 z='zzz'>
    <child z='def'/>
    <child2>hello</child2>
  </nestedElem2>
</root2>'''
    }

    /**
     * Checks against a regression bug whereby some empty elements were not closed.
     */
    void testMarkupForClosingTags() {
        def list = ['first', 'second', 'third']

        def m = {
            ELEM1 {
                list.each {r ->
                    ELEM2(id: r, type: '2') {
                        ELEM3A(id: r)
                        ELEM3B(type: '3', 'text')
                    }
                }
            }
        }

        assertExpectedXml m, '''\
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
</ELEM1>'''
    }

    void testMixedMarkup() {
        def m = {
// uncomment if you want entities like &nbsp; (and add to expected too)
//            mkp.yieldUnescaped '''<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
//                                  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">'''
            html {
                body {
                    p {
                        mkp.yield 'The '
                        i('quick')
                        mkp.yieldUnescaped ' brown fox jumped over the <b>lazy</b> dog &amp; sleepy cat'
                    }
                }
            }
        }

        assertExpectedXml m, '''\
<html>
  <body>
    <p>The <i>quick</i> brown fox jumped over the <b>lazy</b> dog &amp; sleepy cat</p>
  </body>
</html>'''
    }

    void testMixedMarkupWithEntityExpansion() {
        def m = {
            p {
                em('Usually')
                mkp.yield ' Hearts & Diamonds '
                b('beats')
                mkp.yieldUnescaped ' Spades &amp; Clubs'
            }
        }
        assertExpectedXml m, '''\
<p><em>Usually</em> Hearts &amp; Diamonds <b>beats</b> Spades &amp; Clubs </p>'''
    }

    void testMixedMarkupWithEmptyNodes() {
        def m = {
            p {
                mkp.yield 'Red: Hearts & Diamonds'
                br()
                mkp.yieldUnescaped 'Black: Spades &amp; Clubs'
            }
        }
        assertExpectedXml m, '''\
<p>Red: Hearts &amp; Diamonds\n  <br/>Black: Spades &amp; Clubs \n</p>'''
    }

    /**
     * Tests that the Builder escapes element content correctly, even
     * when the content contains line-endings.
     */
    void testEscapingMultiLineContent() {
        def m = {
            element('''This is multi-line content with characters, such as <, that
require escaping. The other characters consist of:

    * > - greater than
    * & - ampersand
''')
        }

        assertExpectedXml m, '''\
<element>This is multi-line content with characters, such as &lt;, that
require escaping. The other characters consist of:

    * &gt; - greater than
    * &amp; - ampersand
</element>'''
    }

    void testNewlineCarriageReturnTabExpansion() {
        def m = {
            root {
                a( b:'''one\n\r\ttwo''' ) {
                  c( 'one\n\r\ttwo' )
                }
            }
        }
        assertExpectedXml m, """<root><a b='one&#10;&#13;&#09;two'><c>one\n\r\ttwo</c></a></root>"""
    }

    void testObjectOperationsInMarkup() {
        def m = {
            root {
                (1..3).each {
                    item()
                }
            }
        }
        assertExpectedXml m, "<root><item/><item/><item/></root>"
    }

    /**
     * Fix for GROOVY-3786
     *
     * yield and yieldUnescaped should call toString() if a non-String object is passed as argument
     */
    void testYieldObjectToStringRepresentation() {
        def m = {
            table {
                td(id: 999) { mkp.yield 999 }
                td(id: 99) { mkp.yieldUnescaped 99 }
            }
        }
        assertExpectedXml m, "<table><td id='999'>999</td><td id='99'>99</td></table>"
    }

    /**
     * test special builder mkp functionality
     */
    void testSmallTreeWithMkpFunctionality() {
        def m = {
            mkp.xmlDeclaration(version: '1.0')
            mkp.pi("xml-stylesheet": [href: "mystyle.css", type: "text/css"])
            root1(a: 5, b: 7) {
                elem1('hello1')
                elem2('hello2')
                mkp.comment('hello3')
                comment('hello4', [:])
                comment(value: 'hello5')
                comment {}
                elem3(x: 7)
            }
        }
        assertExpectedXml m, '''\
<?xml version="1.0"?>
<?xml-stylesheet href="mystyle.css" type="text/css"?>
<root1 a='5' b='7'>
  <elem1>hello1</elem1>
  <elem2>hello2</elem2>
  <!-- hello3 -->
  <comment>hello4</comment>
  <comment value='hello5'/>
  <comment/>
  <elem3 x='7'/>
</root1>'''
    }

}