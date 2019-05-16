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

class XmlNodePrinterTest extends GroovyTestCase {

    StringWriter writer
    PrintWriter pw
    XmlNodePrinter printer
    XmlParser parser

    def namespaceInput = """\
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <Locator xmlns="http://www.foo.com/webservices/AddressBook">
      <Address>
        1000 Main St
      </Address>
    </Locator>
  </soap:Body>
</soap:Envelope>
"""

    def attributeWithNamespaceInput = """\
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <Locator xmlns="http://www.foo.com/webservices/AddressBook">
      <Address ns1:type="Home" xmlns:ns1="http://www.foo.com/webservices/Address" ns2:country="AU" xmlns:ns2="http://www.foo.com/webservices/Address">
        1000 Main St
      </Address>
    </Locator>
  </soap:Body>
</soap:Envelope>
"""

    def noNamespaceInputVerbose = """\
<Envelope>
  <Body>
    <Locator>
      <Address>
        1000 Main St
      </Address>
    </Locator>
  </Body>
</Envelope>
"""

    def noNamespaceInputCompact = """\
<Envelope>
  <Body>
    <Locator>
      <Address>1000 Main St</Address>
    </Locator>
  </Body>
</Envelope>
"""

    def attributeInput = """<Field Text="&lt;html&gt;&quot;Some &apos;Text&apos;&quot;&lt;/html&gt;" />"""
    def attributeExpectedOutputQuot = """<Field Text="&lt;html&gt;&quot;Some 'Text'&quot;&lt;/html&gt;"/>\n"""
    def attributeExpectedOutputApos = """<Field Text='&lt;html&gt;"Some &apos;Text&apos;"&lt;/html&gt;'/>\n"""

    def tagWithSpecialChars = """<Field>\n  &lt;&amp;&gt;'"\n</Field>\n"""

    def attributeWithNewlineInput = "<Field Text=\"Some&#10;Text&#10;&#13;\"/>"
    def attributeWithNewlineExpectedOutput = "<Field Text=\"Some&#10;Text&#10;&#13;\"/>\n"

    def emptyTagExpanded = "<tag></tag>\n"
    def emptyTagCompact = "<tag/>\n"

    protected void setUp() {
        writer = new StringWriter()
        pw = new PrintWriter(writer)
        printer = new XmlNodePrinter(pw, "  ")
        parser = new XmlParser()
        parser.setTrimWhitespace(true)
    }

    private void setUpNoindentingPrinter() {
        printer = new XmlNodePrinter(new IndentPrinter(pw, "", false))
        printer.preserveWhitespace = true
    }

    void testNamespacesDefault() {
        checkRoundtrip namespaceInput, namespaceInput
    }

    void testNamespacesPreserving() {
        parser.trimWhitespace = false
        parser.keepIgnorableWhitespace = true
        setUpNoindentingPrinter()
        checkRoundtrip namespaceInput, namespaceInput.trim()
    }

    void testNamespacesDisabledOnParsing() {
        parser = new XmlParser(false, false)
        parser.trimWhitespace = true
        checkRoundtrip namespaceInput, namespaceInput
    }

    void testNamespacesDisabledOnPrinting() {
        printer.namespaceAware = false
        checkRoundtrip namespaceInput, noNamespaceInputVerbose
    }

    void testWithoutNamespacesVerboseInDefaultOut() {
        checkRoundtrip noNamespaceInputVerbose, noNamespaceInputVerbose
    }

    void testWithoutNamespacesVerbosePreserving() {
        parser.trimWhitespace = false
        parser.keepIgnorableWhitespace = true
        setUpNoindentingPrinter()
        checkRoundtrip noNamespaceInputVerbose, noNamespaceInputVerbose.trim()
    }

    void testWithoutNamespacesVerboseInPreserveOut() {
        printer.preserveWhitespace = true
        checkRoundtrip noNamespaceInputVerbose, noNamespaceInputCompact
    }

    void testWithoutNamespacesCompactInPreserveOut() {
        printer.preserveWhitespace = true
        checkRoundtrip noNamespaceInputCompact, noNamespaceInputCompact
    }

    void testNoExpandOfEmptyElements() {
        checkRoundtrip emptyTagExpanded, emptyTagCompact
    }

    void testExpandEmptyElements() {
        printer.expandEmptyElements = true
        checkRoundtrip emptyTagExpanded, emptyTagExpanded
    }

    void testWithoutNamespacesCompactInDefaultOut() {
        checkRoundtrip noNamespaceInputCompact, noNamespaceInputVerbose
    }

    void testAttributeWithQuot() {
        printer = new XmlNodePrinter(pw, "  ", "\"")
        checkRoundtrip attributeInput, attributeExpectedOutputQuot
    }

    void testAttributeWithApos() {
        printer = new XmlNodePrinter(pw, "  ", "'")
        checkRoundtrip attributeInput, attributeExpectedOutputApos
    }

    void testAttributeWithNewline() {
        checkRoundtrip attributeWithNewlineInput, attributeWithNewlineExpectedOutput
    }

    void testContentWithSpecialSymbolsApos() {
        printer = new XmlNodePrinter(pw, "  ", "'")
        checkRoundtrip tagWithSpecialChars, tagWithSpecialChars
    }

    void testContentWithSpecialSymbolsQuot() {
        printer = new XmlNodePrinter(pw, "  ", "\"")
        checkRoundtrip tagWithSpecialChars, tagWithSpecialChars
    }

    void testAttributeWithNamespaceInput() {
        checkRoundtrip attributeWithNamespaceInput, attributeWithNamespaceInput
    }

    private checkRoundtrip(String intext, String outtext) {
        def root = parser.parseText(intext)
        printer.print(root)
        assertEquals outtext, writer.toString()
    }
}
