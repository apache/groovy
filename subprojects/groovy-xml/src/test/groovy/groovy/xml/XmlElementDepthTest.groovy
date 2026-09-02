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

import org.junit.jupiter.api.Test
import org.xml.sax.SAXException

import static groovy.test.GroovyAssert.shouldFail
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * Secure processing does not bound element depth, and the SAX parse itself survives an arbitrarily
 * deep document because nesting is tracked on the heap. The damage lands on the first consumer to
 * walk the result recursively, which dies with a StackOverflowError -- an Error, so it escapes the
 * catch(Exception) an application would use for a malformed document. The parsers therefore bound
 * the depth themselves, at one check point ahead of every consumer.
 */
class XmlElementDepthTest {

    private static final int DEFAULT = FactorySupport.DEFAULT_MAX_ELEMENT_DEPTH
    private static final String LIMIT = 'jdk.xml.maxElementDepth'

    /** A document whose deepest element sits at {@code depth}, the root counting as depth 1. */
    private static String nested(int depth) {
        '<a>' * depth + '</a>' * depth
    }

    private static withLimit(Object value, Closure body) {
        String previous = System.getProperty(LIMIT)
        if (value == null) System.clearProperty(LIMIT) else System.setProperty(LIMIT, value.toString())
        try {
            body()
        } finally {
            if (previous == null) System.clearProperty(LIMIT) else System.setProperty(LIMIT, previous)
        }
    }

    // GROOVY-12331
    @Test
    void testDocumentAtTheDefaultBoundStillParses() {
        assertEquals('', new XmlSlurper().parseText(nested(DEFAULT)).text())
        assertEquals('a', new XmlParser().parseText(nested(DEFAULT)).name())
    }

    // GROOVY-12331
    @Test
    void testXmlSlurperRejectsADocumentPastTheBound() {
        def e = shouldFail(SAXException) { new XmlSlurper().parseText(nested(DEFAULT + 1)) }
        assertTrue(e.message.contains('maxElementDepth'), e.message)
    }

    // GROOVY-12331
    @Test
    void testXmlParserRejectsADocumentPastTheBound() {
        def e = shouldFail(SAXException) { new XmlParser().parseText(nested(DEFAULT + 1)) }
        assertTrue(e.message.contains('maxElementDepth'), e.message)
    }

    // GROOVY-12331
    @Test
    void testDomBuilderRejectsADocumentPastTheBound() {
        // the DocumentBuilderFactory route is bounded too, not just the SAX one
        shouldFail(SAXException) { DOMBuilder.parse(new StringReader(nested(DEFAULT + 1))) }
    }

    // GROOVY-12331
    @Test
    void testDeepDocumentFailsTheParseRatherThanTheConsumer() {
        // 350KB used to parse cleanly and then kill text()/toString()/XmlNodePrinter with a
        // StackOverflowError; the failure now arrives as an ordinary parse exception instead
        String deep = nested(50000)
        assertTrue(deep.length() > 300000)
        shouldFail(SAXException) { new XmlSlurper().parseText(deep).text() }
        shouldFail(SAXException) { new XmlParser().parseText(deep) }
    }

    // GROOVY-12331
    @Test
    void testBoundIsConfigurableByTheStandardJaxpProperty() {
        withLimit(10) {
            assertEquals('', new XmlSlurper().parseText(nested(10)).text())
            shouldFail(SAXException) { new XmlSlurper().parseText(nested(11)) }
            shouldFail(SAXException) { new XmlParser().parseText(nested(11)) }
        }
    }

    // GROOVY-12331
    @Test
    void testPropertyValueOfZeroRestoresUnlimitedDepth() {
        withLimit(0) {
            assertEquals('', new XmlSlurper().parseText(nested(DEFAULT + 500)).text())
        }
    }

    // GROOVY-12331
    @Test
    void testOrdinaryDocumentsAreUnaffected() {
        def slurped = new XmlSlurper().parseText('<root><child id="1">text</child></root>')
        assertEquals('text', slurped.child.text())
        assertEquals('1', slurped.child.@id.text())

        def parsed = new XmlParser().parseText('<root><child id="1">text</child></root>')
        assertEquals('text', parsed.child.text())
        assertEquals('1', parsed.child[0].@id)
    }
}
