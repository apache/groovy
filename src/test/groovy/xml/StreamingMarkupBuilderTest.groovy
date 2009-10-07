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
 * Tests for StreamingMarkupBuilder. The tests directly in this file
 * are specific to StreamingMarkupBuilder. Functionality in common with
 * MarkupBuilder is tested in the BuilderTestSupport parent class.
 *
 *   @author John Wilson
 *   @author Paul King
 */
class StreamingMarkupBuilderTest extends BuilderTestSupport {

    protected assertExpectedXml(Closure markup, String expectedXml) {
        def builder = new StreamingMarkupBuilder()
        def writer = new StringWriter()
        writer << builder.bind(markup)
        checkXml(expectedXml, writer)
    }

    /**
     * test some StreamingMarkupBuilder specific mkp functionality
     */
    void testSmallTree() {
        def m = {
            mkp.xmlDeclaration(version:'1.0')
            mkp.pi("xml-stylesheet":[href:"mystyle.css", type:"text/css"])
            root1(a:5, b:7) {
                elem1('hello1')
                elem2('hello2')
                mkp.comment('hello3')
                elem3(x:7)
            }
        }
        assertExpectedXml m, '''\
<?xml version="1.0"?>
<?xml-stylesheet href="mystyle.css" type="text/css"?>
<root1 a='5' b='7'>
  <elem1>hello1</elem1>
  <elem2>hello2</elem2>
  <!-- hello3 -->
  <elem3 x='7'/>
</root1>'''
    }

}