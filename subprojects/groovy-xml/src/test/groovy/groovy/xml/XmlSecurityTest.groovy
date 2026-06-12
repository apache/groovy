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

import groovy.xml.dom.DOMCategory
import org.junit.jupiter.api.Test
import org.xml.sax.InputSource
import org.xml.sax.SAXParseException

import javax.xml.transform.TransformerException
import javax.xml.transform.stream.StreamSource

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertThrows
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * Security smoke tests for Groovy's XML parsers and serializer covering
 * XXE, billion-laughs and external resource resolution. These tests
 * pin the secure-by-default contract documented in the XML user guide.
 */
class XmlSecurityTest {

    private static final String XXE_PAYLOAD = '''<?xml version="1.0"?>
<!DOCTYPE foo [
  <!ELEMENT foo ANY>
  <!ENTITY xxe SYSTEM "file:///nonexistent/should/not/be/read">
]>
<foo>&xxe;</foo>'''

    private static final String BILLION_LAUGHS = '''<?xml version="1.0"?>
<!DOCTYPE lolz [
  <!ENTITY lol "lol">
  <!ENTITY lol2 "&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;">
  <!ENTITY lol3 "&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;">
  <!ENTITY lol4 "&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;">
]>
<lolz>&lol4;</lolz>'''

    private static final String EXTERNAL_DTD = '''<?xml version="1.0"?>
<!DOCTYPE foo SYSTEM "http://example.invalid/external.dtd">
<foo/>'''

    // ---- XmlParser ----

    @Test
    void xmlParserRejectsDoctypeByDefault() {
        def parser = new XmlParser()
        assertThrows(SAXParseException, { parser.parseText(XXE_PAYLOAD) })
    }

    @Test
    void xmlParserRejectsBillionLaughsByDefault() {
        def parser = new XmlParser()
        assertThrows(SAXParseException, { parser.parseText(BILLION_LAUGHS) })
    }

    @Test
    void xmlParserAllowsDoctypeWhenOptedIn() {
        // Verifies the relax knob still works: with allowDocTypeDeclaration=true
        // the parser is willing to process a DOCTYPE. We feed an internal-only DTD
        // (no external entity reference) so that the test does not depend on the
        // network or filesystem state.
        def parser = new XmlParser(false, true, true)
        def root = parser.parseText('''<?xml version="1.0"?>
<!DOCTYPE foo [<!ELEMENT foo (#PCDATA)>]>
<foo>hello</foo>''')
        assertEquals('foo', root.name())
        assertEquals('hello', root.text())
    }

    // ---- XmlSlurper ----

    @Test
    void xmlSlurperRejectsDoctypeByDefault() {
        def slurper = new XmlSlurper()
        assertThrows(SAXParseException, { slurper.parseText(XXE_PAYLOAD) })
    }

    @Test
    void xmlSlurperRejectsBillionLaughsByDefault() {
        def slurper = new XmlSlurper()
        assertThrows(SAXParseException, { slurper.parseText(BILLION_LAUGHS) })
    }

    // ---- DOMBuilder.parse ----

    @Test
    void domBuilderParseRejectsDoctypeByDefault() {
        assertThrows(SAXParseException, { DOMBuilder.parse(new StringReader(XXE_PAYLOAD)) })
    }

    @Test
    void domBuilderParseRejectsExternalDtdByDefault() {
        assertThrows(SAXParseException, { DOMBuilder.parse(new StringReader(EXTERNAL_DTD)) })
    }

    @Test
    void domBuilderParseAllowsDoctypeWhenOptedIn() {
        def doc = DOMBuilder.parse(new StringReader('''<?xml version="1.0"?>
<!DOCTYPE foo [<!ELEMENT foo (#PCDATA)>]>
<foo>hello</foo>'''), false, true, true)
        assertEquals('foo', doc.documentElement.tagName)
    }

    // ---- DOMBuilder.newInstance with relax knob ----

    @Test
    void domBuilderNewInstanceWithRelaxKnobAllowsDoctype() {
        // The new (validating, namespaceAware, allowDocTypeDeclaration) overload
        // exposes a relax knob for users building DOM via the factory + DocumentBuilder.
        def builder = DOMBuilder.newInstance(false, true, true)
        def doc = builder.documentBuilder.parse(new InputSource(new StringReader('''<?xml version="1.0"?>
<!DOCTYPE foo [<!ELEMENT foo (#PCDATA)>]>
<foo>hello</foo>''')))
        assertEquals('foo', doc.documentElement.tagName)
    }

    // ---- XmlUtil.serialize ----

    @Test
    void serializeBlocksExternalStylesheetImportByDefault() {
        // An XSLT that imports a stylesheet from a non-resolvable URL must not
        // even attempt resolution; the transform should fail fast before touching
        // the network because ACCESS_EXTERNAL_STYLESHEET is "" by default.
        String xslt = '''<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:import href="http://example.invalid/never-fetched.xsl"/>
</xsl:stylesheet>'''
        def source = new StreamSource(new StringReader(xslt))
        assertThrows(Exception, { XmlUtil.serialize(source) })
    }

    @Test
    void serializeRespectsAllowExternalResourcesFlag() {
        // When opted in, the TransformerFactory will at least *try* to resolve the
        // external stylesheet. We point at an invalid host so we get a network/IO
        // failure rather than silently succeeding; the meaningful assertion is that
        // the failure mode is no longer the fast "external stylesheet not allowed"
        // refusal returned with the default flag.
        String xslt = '''<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:import href="http://example.invalid/never-fetched.xsl"/>
</xsl:stylesheet>'''
        def opts = new SerializeOptions()
        opts.setAllowExternalResources(true)
        def source = new StreamSource(new StringReader(xslt))
        // We don't assert success (the URL is unresolvable on purpose). We just
        // assert the call gets past the access-check phase, which throws a
        // TransformerConfigurationException with message about external access
        // when the flag is *not* set.
        try {
            XmlUtil.serialize(source, new ByteArrayOutputStream(), opts)
        } catch (Exception e) {
            String msg = e.message ?: ''
            assertTrue(!msg.contains('AccessExternalStylesheet') && !msg.contains('access is not allowed'),
                    "should not be blocked by external-access check, got: $msg")
        }
    }

    @Test
    void serializeNormalDomTreeIsUnaffected() {
        // Serialize a regular Node — no DTD, no XSLT. This is the overwhelming
        // majority of XmlUtil.serialize usage and must remain unchanged.
        def root = new XmlParser().parseText('<root><child>v</child></root>')
        String out = XmlUtil.serialize(root)
        assertNotNull(out)
        assertTrue(out.contains('<child>v</child>'))
    }

    // ---- XmlUtil.newSAXParser SchemaFactory still resolves valid schemas ----

    @Test
    void schemaValidationStillWorks() {
        // FSP=true on SchemaFactory must not interfere with normal schema loading.
        String xsd = '''<?xml version="1.0"?>
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
  <xs:element name="root" type="xs:string"/>
</xs:schema>'''
        def saxParser = XmlUtil.newSAXParser(
                javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI,
                new StreamSource(new StringReader(xsd)))
        assertNotNull(saxParser)
    }

    // ---- DOMCategory XPath ----

    @Test
    void xpathStillEvaluates() {
        // FSP=true on XPathFactory must not break normal expressions.
        def doc = DOMBuilder.parse(new StringReader('<root><a>1</a><a>2</a></root>'))
        use(DOMCategory) {
            assertEquals('1', doc.documentElement.xpath('//a[1]/text()'))
        }
    }
}
