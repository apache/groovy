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

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertThrows
import static org.junit.jupiter.api.Assertions.assertTrue


class XmlNodePrinterTest {

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

    def namespaceWithSpecialUriInput = """\
<foo:Envelope xmlns:foo="http://x/a?b=1&amp;c=2&lt;d">
  <foo:Body>text</foo:Body>
</foo:Envelope>
"""

    def namespaceWithSpecialUriOutput = """\
<foo:Envelope xmlns:foo="http://x/a?b=1&amp;c=2&lt;d">
  <foo:Body>
    text
  </foo:Body>
</foo:Envelope>
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

    @BeforeEach
    void setUp() {
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

    @Test
    void namespacesDefault() {
        checkRoundtrip namespaceInput, namespaceInput
    }

    @Test
    void namespacesPreserving() {
        parser.trimWhitespace = false
        parser.keepIgnorableWhitespace = true
        setUpNoindentingPrinter()
        checkRoundtrip namespaceInput, namespaceInput.trim()
    }

    @Test
    void namespacesDisabledOnParsing() {
        parser = new XmlParser(false, false)
        parser.trimWhitespace = true
        checkRoundtrip namespaceInput, namespaceInput
    }

    @Test
    void namespacesDisabledOnPrinting() {
        printer.namespaceAware = false
        checkRoundtrip namespaceInput, noNamespaceInputVerbose
    }

    @Test
    void withoutNamespacesVerboseInDefaultOut() {
        checkRoundtrip noNamespaceInputVerbose, noNamespaceInputVerbose
    }

    @Test
    void withoutNamespacesVerbosePreserving() {
        parser.trimWhitespace = false
        parser.keepIgnorableWhitespace = true
        setUpNoindentingPrinter()
        checkRoundtrip noNamespaceInputVerbose, noNamespaceInputVerbose.trim()
    }

    @Test
    void withoutNamespacesVerboseInPreserveOut() {
        printer.preserveWhitespace = true
        checkRoundtrip noNamespaceInputVerbose, noNamespaceInputCompact
    }

    @Test
    void withoutNamespacesCompactInPreserveOut() {
        printer.preserveWhitespace = true
        checkRoundtrip noNamespaceInputCompact, noNamespaceInputCompact
    }

    @Test
    void noExpandOfEmptyElements() {
        checkRoundtrip emptyTagExpanded, emptyTagCompact
    }

    @Test
    void expandEmptyElements() {
        printer.expandEmptyElements = true
        checkRoundtrip emptyTagExpanded, emptyTagExpanded
    }

    @Test
    void withoutNamespacesCompactInDefaultOut() {
        checkRoundtrip noNamespaceInputCompact, noNamespaceInputVerbose
    }

    @Test
    void attributeWithQuot() {
        printer = new XmlNodePrinter(pw, "  ", "\"")
        checkRoundtrip attributeInput, attributeExpectedOutputQuot
    }

    @Test
    void attributeWithApos() {
        printer = new XmlNodePrinter(pw, "  ", "'")
        checkRoundtrip attributeInput, attributeExpectedOutputApos
    }

    @Test
    void attributeWithNewline() {
        checkRoundtrip attributeWithNewlineInput, attributeWithNewlineExpectedOutput
    }

    @Test
    void contentWithSpecialSymbolsApos() {
        printer = new XmlNodePrinter(pw, "  ", "'")
        checkRoundtrip tagWithSpecialChars, tagWithSpecialChars
    }

    @Test
    void contentWithSpecialSymbolsQuot() {
        printer = new XmlNodePrinter(pw, "  ", "\"")
        checkRoundtrip tagWithSpecialChars, tagWithSpecialChars
    }

    @Test
    void attributeWithNamespaceInput() {
        checkRoundtrip attributeWithNamespaceInput, attributeWithNamespaceInput
    }

    @Test
    void namespaceUriWithSpecialCharsEscaped() {
        checkRoundtrip namespaceWithSpecialUriInput, namespaceWithSpecialUriOutput
    }

    private static final String XSI = 'http://www.w3.org/2001/XMLSchema-instance'
    private static final String SCHEMA = 'http://www.w3.org/2001/XMLSchema'

    @Test
    void keepsPrefixUsedOnlyByAttributeValue() {
        def xml = '''<ns2:getTableReport xmlns:ns2="http://www.jdisc.com">
                <ns2:genericFilters xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="ns2:containsCondition">
                    <filterValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xsi:type="xs:string">gaucho</filterValue>
                </ns2:genericFilters>
            </ns2:getTableReport>'''
        def node = parse(xml)
        def sw = new StringWriter()
        def localPrinter = new XmlNodePrinter(new PrintWriter(sw))
        localPrinter.preserveWhitespace = true
        localPrinter.namespaceAware = true
        localPrinter.print(node)
        assertEquals '''\
<ns2:getTableReport xmlns:ns2="http://www.jdisc.com">
  <ns2:genericFilters xsi:type="ns2:containsCondition" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <filterValue xsi:type="xs:string" xmlns:xs="http://www.w3.org/2001/XMLSchema">gaucho</filterValue>
  </ns2:genericFilters>
</ns2:getTableReport>
''', sw.toString()
        def filterValue = node.children()[0].children()[0]
        assertEquals 1, filterValue.attributes().size()
        assertEquals([xs: SCHEMA], filterValue.localNamespaceDeclarations())
    }

    @Test
    void dropsPrefixDeclaredButNotReferenced() {
        assertEquals '''\
<e id="1">
  text
</e>
''', printed('<e xmlns:xs="urn:xs" id="1">text</e>')
        assertEquals '''\
<e>
  xs:string
</e>
''', printed('<e xmlns:xs="urn:xs">xs:string</e>')
    }

    @Test
    void emitsBindingFromAncestorAtUseSite() {
        assertEquals '''\
<root>
  <child xsi:type="xs:string" xmlns:xsi="''' + XSI + '''" xmlns:xs="urn:xs">
    gaucho
  </child>
</root>
''', printed('<root xmlns:xs="urn:xs"><child xmlns:xsi="' + XSI + '" xsi:type="xs:string">gaucho</child></root>')
    }

    @Test
    void subtreePrintCarriesAncestorBinding() {
        def child = parse('<root xmlns:xs="urn:xs"><child xmlns:xsi="' + XSI + '" xsi:type="xs:string">gaucho</child></root>').child[0]
        assertEquals '''\
<child xsi:type="xs:string" xmlns:xsi="''' + XSI + '''" xmlns:xs="urn:xs">
  gaucho
</child>
''', printed(child)
    }

    @Test
    void rebindsPrefixWhenAttributeValueUsesNestedUri() {
        def xml = '<root xmlns:xs="urn:a" xmlns:xsi="' + XSI + '" xsi:type="xs:int">' +
                '<child xmlns:xs="urn:b" xsi:type="xs:string"/></root>'
        assertEquals '''\
<root xsi:type="xs:int" xmlns:xsi="''' + XSI + '''" xmlns:xs="urn:a">
  <child xsi:type="xs:string" xmlns:xs="urn:b"/>
</root>
''', printed(xml)
    }

    @Test
    void doesNotRepeatPrefixAlreadyInScope() {
        def xml = '<xs:e xmlns:xs="urn:xs" xmlns:xsi="' + XSI + '" xsi:type="xs:string"/>'
        def text = printed(xml)
        assertEquals '<xs:e xmlns:xs="urn:xs" xsi:type="xs:string" xmlns:xsi="' + XSI + '"/>\n', text
        assertEquals 1, occurrences(text, 'xmlns:xs=')

        def nested = printed('<root xmlns:xs="urn:a" xmlns:xsi="' + XSI + '" xsi:type="xs:int"><child xsi:type="xs:string"/></root>')
        assertEquals '''\
<root xsi:type="xs:int" xmlns:xsi="''' + XSI + '''" xmlns:xs="urn:a">
  <child xsi:type="xs:string"/>
</root>
''', nested
        assertEquals 1, occurrences(nested, 'xmlns:xs=')
    }

    @Test
    void keepsSeveralQualifiedNamesInOneAttribute() {
        assertEquals '<e types="a:x b:y" xmlns:a="urn:a" xmlns:b="urn:b"/>\n',
                printed('<e xmlns:a="urn:a" xmlns:b="urn:b" types="a:x b:y"/>')
        def repeated = printed('<e xmlns:a="urn:a" types="a:x a:y"/>')
        assertEquals '<e types="a:x a:y" xmlns:a="urn:a"/>\n', repeated
        assertEquals 1, occurrences(repeated, 'xmlns:a=')
    }

    @Test
    void ignoresTokensThatAreNotQualifiedNames() {
        assertEquals '<e href="http://example.com/a"/>\n',
                printed('<e xmlns:xs="urn:xs" href="http://example.com/a"/>')
        assertEquals '<e note="xs:string,"/>\n',
                printed('<e xmlns:xs="urn:xs" note="xs:string,"/>')
        assertEquals '<e t="xs:a&amp;b"/>\n',
                printed('<e xmlns:xs="urn:xs" t="xs:a&amp;b"/>')
        assertEquals '<e t=":name p: a:b:c"/>\n',
                printed('<e xmlns:p="urn:p" t=":name p: a:b:c"/>')
    }

    @Test
    void escapesNamespaceUriReferencedByAttributeValue() {
        def xml = '<e xmlns:xs="http://x/a?b=1&amp;c=2&lt;d" xmlns:xsi="' + XSI + '" xsi:type="xs:string"/>'
        assertEquals '<e xsi:type="xs:string" xmlns:xsi="' + XSI + '" xmlns:xs="http://x/a?b=1&amp;c=2&lt;d"/>\n',
                printed(xml)

        def node = parse('<e xmlns:xs="urn:x\'s" xmlns:xsi="' + XSI + '" xsi:type="xs:string"/>')
        def sw = new StringWriter()
        def localPrinter = new XmlNodePrinter(new PrintWriter(sw), '  ', "'")
        localPrinter.print(node)
        assertEquals '<e xsi:type=\'xs:string\' xmlns:xsi=\'' + XSI + '\' xmlns:xs=\'urn:x&apos;s\'/>\n', sw.toString()
    }

    @Test
    void keepsDefaultNamespaceAndAttributeValuePrefix() {
        def xml = '<e xmlns="urn:e" xmlns:xs="urn:xs" xmlns:xsi="' + XSI + '" xsi:type="xs:string"/>'
        assertEquals '<e xmlns="urn:e" xsi:type="xs:string" xmlns:xsi="' + XSI + '" xmlns:xs="urn:xs"/>\n', printed(xml)
    }

    @Test
    void emptyElementCarriesAttributeValuePrefix() {
        def xml = '<filterValue xmlns:xs="' + SCHEMA + '" xmlns:xsi="' + XSI + '" xsi:type="xs:string"/>'
        assertEquals '<filterValue xsi:type="xs:string" xmlns:xsi="' + XSI + '" xmlns:xs="' + SCHEMA + '"/>\n', printed(xml)

        def node = parse(xml)
        def sw = new StringWriter()
        def localPrinter = new XmlNodePrinter(new PrintWriter(sw))
        localPrinter.expandEmptyElements = true
        localPrinter.print(node)
        assertEquals '<filterValue xsi:type="xs:string" xmlns:xsi="' + XSI + '" xmlns:xs="' + SCHEMA + '"></filterValue>\n',
                sw.toString()
    }

    @Test
    void namespaceUnawarePrintingDoesNotEmitAttributeValueBindings() {
        def node = parse('<xs:e xmlns:xs="urn:xs" xmlns:xsi="' + XSI + '" xsi:type="xs:string"/>')
        def sw = new StringWriter()
        def localPrinter = new XmlNodePrinter(new PrintWriter(sw))
        localPrinter.namespaceAware = false
        localPrinter.print(node)
        assertEquals '<e type="xs:string"/>\n', sw.toString()
    }

    @Test
    void namespaceUnawareParsingKeepsLiteralDeclarations() {
        def localParser = new XmlParser(false, false)
        localParser.trimWhitespace = true
        def node = localParser.parseText('<e xmlns:xs="urn:xs" xsi:type="xs:string"/>')
        assertEquals([:], node.localNamespaceDeclarations())
        assertEquals '<e xmlns:xs="urn:xs" xsi:type="xs:string"/>\n', printed(node)
    }

    @Test
    void bindingsFromMultipleAncestorsAreVisibleTogether() {
        def root = parse('<gp xmlns:a="urn:a"><p xmlns:b="urn:b"><c types="a:x b:y"/></p></gp>')
        assertEquals '''\
<gp>
  <p>
    <c types="a:x b:y" xmlns:a="urn:a" xmlns:b="urn:b"/>
  </p>
</gp>
''', printed(root)
        def leaf = root.children()[0].children()[0]
        assertEquals '<c types="a:x b:y" xmlns:a="urn:a" xmlns:b="urn:b"/>\n', printed(leaf)
    }

    @Test
    void ancestorWithoutDeclarationsAndGrandparentBinding() {
        def xml = '<gp xmlns:xs="urn:xs"><p><c xmlns:xsi="' + XSI + '" xsi:type="xs:string"/></p></gp>'
        assertEquals '''\
<gp>
  <p>
    <c xsi:type="xs:string" xmlns:xsi="''' + XSI + '''" xmlns:xs="urn:xs"/>
  </p>
</gp>
''', printed(xml)
        def middle = parse('<root><child xsi:type="xs:string" xmlns:xs="urn:xs" xmlns:xsi="' + XSI + '"/></root>').child[0]
        assertTrue printed(middle).contains('xmlns:xs="urn:xs"')
    }

    @Test
    void cloneAndReplaceKeepBindings() {
        def root = parse('<root xmlns:xs="urn:xs"><child xmlns:xsi="' + XSI + '" xsi:type="xs:string">gaucho</child></root>')
        def cloned = root.clone() as Node
        root.setLocalNamespaceDeclarations(null)
        assertEquals([xs: 'urn:xs'], cloned.localNamespaceDeclarations())
        assertEquals printed(parse('<root xmlns:xs="urn:xs"><child xmlns:xsi="' + XSI + '" xsi:type="xs:string">gaucho</child></root>')),
                printed(cloned)

        def textNode = new Node(null, 'leaf', 'text')
        textNode.setLocalNamespaceDeclarations([p: 'urn:p'])
        def textClone = textNode.clone() as Node
        assertEquals([p: 'urn:p'], textClone.localNamespaceDeclarations())
        assertEquals 'text', textClone.text()
        def rootError = assertThrows(UnsupportedOperationException) {
            textNode.replaceNode(textClone)
        }
        assertEquals 'Replacing the root node is not supported', rootError.message

        def host = parse('<host><old/></host>')
        def replacement = parse('<child xmlns:xs="urn:xs" xmlns:xsi="' + XSI + '" xsi:type="xs:string"/>')
        host.old[0].replaceNode(replacement)
        assertEquals '''\
<host>
  <child xsi:type="xs:string" xmlns:xsi="''' + XSI + '''" xmlns:xs="urn:xs"/>
</host>
''', printed(host)
    }

    @Test
    void recordedBindingsStayOutOfTheAttributeMap() {
        def node = new Node(null, 'plain')
        assertEquals([:], node.localNamespaceDeclarations())
        node.setLocalNamespaceDeclarations(null)
        assertEquals([:], node.localNamespaceDeclarations())
        node.setLocalNamespaceDeclarations([:])
        assertEquals([:], node.localNamespaceDeclarations())

        def parsed = parse('<e xmlns:xs="urn:xs" xmlns:xsi="' + XSI + '" xsi:type="xs:string"/>')
        assertEquals 1, parsed.attributes().size()
        assertEquals([xs: 'urn:xs', xsi: XSI], parsed.localNamespaceDeclarations())
        parsed.setLocalNamespaceDeclarations([:])
        assertEquals '<e xsi:type="xs:string" xmlns:xsi="' + XSI + '"/>\n', printed(parsed)
    }

    @Test
    void handBuiltValuesUseRecordedBindingsOnly() {
        def spaced = new Node(null, 'e', [t: ' p:a\tp:b\np:c\rp:d '])
        spaced.setLocalNamespaceDeclarations(['': 'urn:default', p: 'urn:p', q: ''])
        assertEquals '<e t=" p:a' + '\t' + 'p:b&#10;p:c&#13;p:d " xmlns:p="urn:p"/>\n', printed(spaced)

        def skipped = new Node(null, 'e', [t: 'q:ab', n: 1])
        skipped.setLocalNamespaceDeclarations(['': 'urn:default', p: '', q: 'urn:q'])
        assertEquals '<e t="q:ab" n="1" xmlns:q="urn:q"/>\n', printed(skipped)

        def sw = new StringWriter()
        def localPrinter = new XmlNodePrinter(new PrintWriter(sw))
        localPrinter.printName(spaced, null, true, true)
        assertTrue !sw.toString().contains('xmlns:')
    }

    @Test
    void qualifiedNameCharactersFollowXmlNcName() {
        int[] nameStarts = [
                0x5F, 0x41, 0x5A, 0x61, 0x7A,
                0xC0, 0xD6, 0xD8, 0xF6, 0xF8, 0x2FF, 0x370, 0x37D, 0x37F, 0x1FFF,
                0x200C, 0x200D, 0x2070, 0x218F, 0x2C00, 0x2FEF, 0x3001, 0xD7FF,
                0xF900, 0xFDCF, 0xFDF0, 0xFFFD, 0x10000, 0xEFFFF
        ]
        int[] nameStartRejects = [
                0x21, 0x5B, 0x7B, 0x30, 0x2D, 0x2E,
                0xB7, 0xD7, 0xF7, 0x300, 0x37E, 0x2000, 0x200E, 0x2190, 0x2FF0,
                0xD800, 0xFDD0, 0xFFFE, 0xF0000
        ]
        for (int codePoint : nameStarts) {
            assertTrue declaresPrefix(Character.toString(codePoint)), "start ${Integer.toHexString(codePoint)}"
        }
        for (int codePoint : nameStartRejects) {
            assertTrue !declaresPrefix(Character.toString(codePoint)), "reject start ${Integer.toHexString(codePoint)}"
        }
        int[] nameCharExtra = [0x2D, 0x2E, 0x30, 0x39, 0x62, 0xB7, 0x0300, 0x036F, 0x203F, 0x2040, 0x10000]
        int[] nameCharRejects = [0x21, 0x2F, 0x3A, 0xB6, 0x037E, 0x203E, 0x2041]
        for (int codePoint : nameCharExtra) {
            assertTrue declaresPrefix('a' + Character.toString(codePoint)), "char ${Integer.toHexString(codePoint)}"
        }
        for (int codePoint : nameCharRejects) {
            assertTrue !declaresPrefix('a' + Character.toString(codePoint)), "reject char ${Integer.toHexString(codePoint)}"
        }
        assertTrue declaresPrefix('ab')
        assertTrue declaresPrefix('a' + Character.toString(0x10000))
        assertTrue !declaresPrefix('a!')
        assertTrue !declaresPrefix('1abc')
        def badPrefix = new Node(null, 'e', [t: '1abc:foo'])
        badPrefix.setLocalNamespaceDeclarations(['1abc': 'urn:bad'])
        assertTrue !printed(badPrefix).contains('xmlns:')
    }

    @Test
    void prefixMappingRejectsBindingsThatCannotQualifyAValue() {
        def unaware = new XmlParser(false, false)
        unaware.startPrefixMapping('xs', 'urn:xs')

        def parser = new XmlParser()
        parser.startPrefixMapping(null, 'urn:xs')
        parser.startPrefixMapping('', 'urn:xs')
        parser.startPrefixMapping('xs', null)
        parser.startPrefixMapping('xs', '')
        parser.startPrefixMapping('xs', 'urn:xs')
        def node = parser.parseText('<e t="xs:string"/>')
        assertEquals([:], node.localNamespaceDeclarations())
        assertEquals '<e t="xs:string"/>\n', printed(node)
    }

    @Test
    void nullAttributeMapPrintsWithoutBindings() {
        assertEquals '''\
<e>
  text
</e>
''', printed(new Node(null, 'e', null, 'text'))
        assertEquals '<e/>\n', printed(new Node(null, 'e', null, new NodeList()))
    }

    @Test
    void charSequenceValuesAndReservedPrefixes() {
        def typeName = 'string'
        def gstringNode = new Node(null, 'e', [t: "xs:${typeName}"])
        gstringNode.setLocalNamespaceDeclarations([xs: 'urn:xs'])
        assertEquals '<e t="xs:string" xmlns:xs="urn:xs"/>\n', printed(gstringNode)

        def builderNode = new Node(null, 'e', [t: new StringBuilder('plain')])
        builderNode.setLocalNamespaceDeclarations([xs: 'urn:xs'])
        assertEquals '<e t="plain"/>\n', printed(builderNode)

        def qualified = new Node(null, 'e', [t: new StringBuilder('xs:string')])
        qualified.setLocalNamespaceDeclarations([xs: 'urn:xs', xml: 'urn:xml'])
        assertEquals '<e t="xs:string" xmlns:xs="urn:xs"/>\n', printed(qualified)

        def reserved = new Node(null, 'e', [t: 'xml:lang xmlns:foo XML:bar xmL:baz xs:string xma:keep'])
        reserved.setLocalNamespaceDeclarations([
                xml: 'http://www.w3.org/XML/1998/namespace',
                xmlns: 'http://www.w3.org/2000/xmlns/',
                XML: 'urn:XML',
                xmL: 'urn:xmL',
                xs: 'urn:xs',
                xma: 'urn:xma'
        ])
        def text = printed(reserved)
        assertEquals '<e t="xml:lang xmlns:foo XML:bar xmL:baz xs:string xma:keep" xmlns:xs="urn:xs" xmlns:xma="urn:xma"/>\n', text
        assertEquals 0, occurrences(text, 'xmlns:xml')
        assertEquals 0, occurrences(text, 'xmlns:XML')
        assertEquals 0, occurrences(text, 'xmlns:xmL')
    }

    @Test
    void nullNodeIsRejected() {
        def printer = new XmlNodePrinter(new PrintWriter(new StringWriter()))
        def error = assertThrows(IllegalArgumentException) {
            printer.print((Node) null)
        }
        assertEquals 'Node must not be null!', error.message
    }

    private static Node parse(String xml) {
        def localParser = new XmlParser()
        localParser.trimWhitespace = true
        return localParser.parseText(xml)
    }

    private static String printed(String xml) {
        return printed(parse(xml))
    }

    private static String printed(Node node) {
        def sw = new StringWriter()
        new XmlNodePrinter(new PrintWriter(sw)).print(node)
        return sw.toString()
    }

    private static boolean declaresPrefix(String localPart) {
        def node = new Node(null, 'e', [t: 'p:' + localPart])
        node.setLocalNamespaceDeclarations([p: 'urn:p', '1abc': 'urn:bad'])
        return printed(node).contains('xmlns:p="urn:p"')
    }

    private static int occurrences(String text, String part) {
        int count = 0
        int from = 0
        while (true) {
            int found = text.indexOf(part, from)
            if (found < 0) {
                return count
            }
            count++
            from = found + part.length()
        }
    }

    private checkRoundtrip(String intext, String outtext) {
        def root = parser.parseText(intext)
        printer.print(root)
        assertEquals outtext, writer.toString()
    }
}
