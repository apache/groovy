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
import org.xml.sax.ErrorHandler
import org.xml.sax.InputSource

import javax.xml.transform.stream.StreamSource

import static groovy.xml.XmlAssert.assertXmlEquals
import static groovy.xml.XmlUtil.escapeControlCharacters
import static groovy.xml.XmlUtil.escapeXml
import static groovy.xml.XmlUtil.newSAXParser
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI

class XmlUtilTest extends GroovyTestCase {
    def xml = """\
    <?xml version="1.0" encoding="UTF-8"?>
    <Schl\u00FCssel>
    text content
    </Schl\u00FCssel>
    """.stripIndent()

    // GROOVY-5158
    void testSerializeOfGPathResultShouldRoundTrip() {
        def source = new InputSource(new StringReader(xml))
        source.encoding = "UTF-8"
        assertXmlEquals(xml, XmlUtil.serialize(new XmlSlurper().parse(source)))
    }

    // GROOVY-5361
    void testSchemaValidationUtilityMethod() {
        Locale dl = Locale.getDefault()
        Locale.setDefault(Locale.ENGLISH)

        def cases = [
            "<person><first>James</first><last>Kirk</last></person>",
            "<person><first>James</first><middle>T.</middle><last>Kirk</last></person>",
            "<person title='Captain'><first>James</first><last>Kirk</last></person>"
        ]

        def xsd = '''<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
          <xs:element name="person">
            <xs:complexType>
              <xs:sequence>
                <xs:element name="first" type="xs:NCName"/>
                <xs:element name="last" type="xs:NCName"/>
              </xs:sequence>
            </xs:complexType>
          </xs:element>
        </xs:schema>'''

        def expected = [
            /Kirk, James No Error/,
            /Kirk, James .*Invalid content.*middle.*last.*expected/,
            /Kirk, James .*title.*not allowed.*in element.*person/
        ]

        try {
            def message
            def parser = new XmlParser(newSAXParser(W3C_XML_SCHEMA_NS_URI, new StreamSource(new StringReader(xsd))))
            def results = []
            parser.errorHandler = { message = it.message } as ErrorHandler
            cases.each {
                message = 'No Error'
                def p = parser.parseText(it)
                results << "${p.last.text()}, ${p.first.text()} $message"
            }
            assert results.size() == 3
            (0..2).each {
                assert results[it] =~ expected[it]
            }
        } finally {
            Locale.setDefault(dl)
        }
    }

    // GROOVY-5775
    void testEscaping() {
      def ans = escapeControlCharacters(escapeXml('"bread" & "butter"\r\n'))
      assert ans == '&quot;bread&quot; &amp; &quot;butter&quot;&#13;&#10;'
    }
}
