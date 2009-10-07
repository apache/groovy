/*
 * Copyright 2003-2009 the original author or authors.
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

/**
 * This test uses the concise syntax to test the building of 
 * textual markup (XML or HTML) using GroovyMarkup
 */
class StreamingMarkupBuilderTest extends BuilderTestSupport {

    protected assertExpectedXml(Closure markup, String expectedXml) {
        def builder = new StreamingMarkupBuilder()
        def writer = new StringWriter()
        writer << builder.bind(markup)
        checkXml(expectedXml, writer)
    }

    void testSmallTree() {
        def m = {
            mkp.pi("xml-stylesheet":[href:"mystyle.css", type:"text/css"])
            root1(a:5, b:7) {
                elem1('hello1')
                elem2('hello2')
                elem3(x:7)
            }
        }
        assertExpectedXml m, '''\
<?xml-stylesheet href="mystyle.css" type="text/css"?>
<root1 a='5' b='7'>
  <elem1>hello1</elem1>
  <elem2>hello2</elem2>
  <elem3 x='7'/>
</root1>'''
    }

    void testObjectOperationsInMarkup() {
        def doc = new StreamingMarkupBuilder().bind {
            root {
                (1..3).each {
                    item()
                }
            }
        }
        assert doc.toString() == "<root><item/><item/><item/></root>"
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

}