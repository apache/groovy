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
package groovy.xml.dom

import groovy.test.GroovyTestCase
import groovy.xml.DOMBuilder
import groovy.xml.StreamingDOMBuilder

class DOMTest extends GroovyTestCase {
    def benchmark = false

    void testDOMParser() {
        def xml = new StringReader("<html><head><title class='mytitle'>Test</title></head><body><p class='mystyle'>This is a test.</p></body></html>")
        def doc = DOMBuilder.parse(xml)
        def html = doc.documentElement
        if (!benchmark) assertCorrect html
    }

    void testDOMBuilder() {
        def html = DOMBuilder.newInstance().
                html {
                    head {
                        title(class: "mytitle", "Test")
                    }
                    body {
                        p(class: "mystyle", "This is a test.")
                    }
                }
        if (!benchmark) assertCorrect html
    }

    void testDOMBuilderWithNullValue() {
        def html = DOMBuilder.newInstance().
                html {
                    head {
                        title(testAttr: null, "Test")
                    }
                }
        use(DOMCategory) {
            assert html.head.title[0].'@testAttr' == ''
        }
    }

    void testStreamingDOMBuilder() {
        def builder = new StreamingDOMBuilder()
        def make = builder.bind {
            html {
                head {
                    title(class: "mytitle", "Test")
                }
                body {
                    p(class: "mystyle", "This is a test.")
                }
            }
        }
        if (!benchmark) assertCorrect make().documentElement
    }

    private def assertCorrect(html) {
        use(DOMCategory) {
            assert html.head.title.text() == 'Test', "Expected 'Test' but was: ${html.head.title.text()}"
            assert html.body.p[0].text() == 'This is a test.'
            assert html.find { it.tagName == 'body' }.tagName == 'body'
            assert html.getElementsByTagName('*').findAll { it.'@class' != '' }.size() == 2
        }
        // should fail outside category
        shouldFail(MissingPropertyException) { html.head }
    }

    static void main(args) {
        // Relative results:
        // When:      05 May 2004  14 Oct 2006  4 Mar 2007
        // Notes:                  Xerces 2.4   Xerces 2.8
        // Parser:    1.0          1.0          1.0
        // Builder:   1.05         2.90         1.20
        // Streaming: 0.77         0.20         0.20
        def x = args.size() == 0 ? 1000 : Integer.parseInt(args[0])
        def mydomtest = new DOMTest()
        def standard = 0
        mydomtest.benchmark = true
        [{ mydomtest.testDOMParser() },
                { mydomtest.testDOMBuilder() },
                { mydomtest.testStreamingDOMBuilder() }].eachWithIndex { testMethod, index ->
            // Run the method once to fill any caches and to load classes
            testMethod()
            def start = System.currentTimeMillis()
            def lastIndex
            for (i in 1..x) {
                testMethod()
                lastIndex = i
            }
            def elapsed = System.currentTimeMillis() - start
            def result = lastIndex * 1000 / elapsed

            standard = (standard == 0 ? result : standard)
            def factor = result / standard
            def relative = 1 / factor
            println "${index}: ${factor}x relative=${relative} (${result} trees/s)"
        }
    }
}
