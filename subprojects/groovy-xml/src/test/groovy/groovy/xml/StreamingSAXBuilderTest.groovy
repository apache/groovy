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

import groovy.test.GroovyTestCase
import org.custommonkey.xmlunit.Diff
import org.custommonkey.xmlunit.XMLUnit
import org.xml.sax.ContentHandler

import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult

class StreamingSAXBuilderTest extends GroovyTestCase {

    void testDefaultSerialization() {
        def handler = TransformerFactory.newInstance().newTransformerHandler()
        def outstream = new ByteArrayOutputStream()
        handler.setResult(new StreamResult(outstream))

        def doc = new StreamingSAXBuilder().bind {
            document {
                item(to: 'world', 'hello')
            }
        }
        def expected = '<document><item to="world">hello</item></document>'

        doc(handler)
        XMLUnit.setIgnoreWhitespace(true)
        def xmlDiff = new Diff(expected, outstream.toString())
        assert xmlDiff.similar(), xmlDiff.toString()
    }

    void testDefaultSerializationNamespaces() {
        def handler = TransformerFactory.newInstance().newTransformerHandler()
        def outstream = new ByteArrayOutputStream()
        handler.setResult(new StreamResult(outstream))

        def doc = new StreamingSAXBuilder().bind {
            mkp.declareNamespace("" : "uri:urn1")
            mkp.declareNamespace("x" : "uri:urn2")
            outer {
                'x:inner'('hello')
            }
        }
        def expected = '<outer xmlns="uri:urn1" xmlns:x="uri:urn2"><x:inner>hello</x:inner></outer>'

        doc(handler)
        XMLUnit.setIgnoreWhitespace(true)
        def xmlDiff = new Diff(expected, outstream.toString())
        assert xmlDiff.similar(), xmlDiff.toString()
    }

    void testCustomHandler() {
        def visited = []
        def handler = [
                startDocument: {->
                    visited << 'startDocument'
                },
                startElement: {uri, localName, qName, atts ->
                    visited << 'startElement'
                },
                characters: {chars, start, end -> /* ignore */ },
                endElement: {uri, localName, qName ->
                    visited << 'endElement'
                },
                endDocument: {->
                    visited << 'endDocument'
                },
        ] as ContentHandler

        def doc = new StreamingSAXBuilder().bind {
            document {
                item(to: 'world', 'hello')
            }
        }
        def expected = ["startDocument", "startElement", "startElement",
                "endElement", "endElement", "endDocument"]

        doc(handler)
        assert visited == expected
    }
}
