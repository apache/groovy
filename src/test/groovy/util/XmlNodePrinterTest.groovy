package groovy.util;

class XmlNodePrinterTest extends GroovyTestCase {

    def namespaceInput = """<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <Locator xmlns="http://www.foo.com/webservices/AddressBook">
      <Address>
        1000 Main St
      </Address>
    </Locator>
  </soap:Body>
</soap:Envelope>
"""

    def attributeWithNamespaceInput = """<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <Locator xmlns="http://www.foo.com/webservices/AddressBook">
      <Address ns1:type="Home" xmlns:ns1="http://www.foo.com/webservices/Address">
        1000 Main St
      </Address>
    </Locator>
  </soap:Body>
</soap:Envelope>
"""

    def noNamespaceInput = """<Envelope>
  <Body>
    <Locator>
      <Address>
        1000 Main St
      </Address>
    </Locator>
  </Body>
</Envelope>
"""

    def attributeInput = """<Field Text="&lt;html&gt;&quot;Some &apos;Text&apos;&quot;&lt;/html&gt;" />"""
    def attributeExpectedOutputQuot = """<Field Text="&lt;html&gt;&quot;Some 'Text'&quot;&lt;/html&gt;"/>\n"""
    def attributeExpectedOutputApos = """<Field Text='&lt;html&gt;"Some &apos;Text&apos;"&lt;/html&gt;'/>\n"""
    def tagWithSpecialCharsInput = """<Field>&lt;&amp;&gt;</Field>"""
    def tagWithSpecialCharsOutput = """<Field>\n  &lt;&amp;&gt;\n</Field>\n"""

    void testNamespaces() {
        def root = new XmlParser().parseText(namespaceInput)
        def writer = new StringWriter()
        new XmlNodePrinter(new PrintWriter(writer), "  ").print(root)
        def result = writer.toString()
        assertEquals namespaceInput, result
    }

    void testNamespacesDisabledOnParsing() {
        def root = new XmlParser(false, false).parseText(namespaceInput)
        def writer = new StringWriter()
        new XmlNodePrinter(new PrintWriter(writer), "  ").print(root)
        def result = writer.toString()
        assertEquals namespaceInput, result
    }

    void testNamespacesDisabledOnPrinting() {
        def root = new XmlParser().parseText(namespaceInput)
        def writer = new StringWriter()
        def printer = new XmlNodePrinter(new PrintWriter(writer), "  ")
        printer.namespaceAware = false
        printer.print(root)
        def result = writer.toString()
        assertEquals noNamespaceInput, result
    }

    void testWithoutNamespaces() {
        def root = new XmlParser().parseText(noNamespaceInput)
        def writer = new StringWriter()
        new XmlNodePrinter(new PrintWriter(writer), "  ").print(root)
        def result = writer.toString()
        assertEquals noNamespaceInput, result
    }

    void testAttributeWithQuot() {
        def root = new XmlParser().parseText(attributeInput)
        def writer = new StringWriter()
        new XmlNodePrinter(new PrintWriter(writer), "  ", "\"").print(root)
        def result = writer.toString()
        assertEquals attributeExpectedOutputQuot, result
    }

    void testAttributeWithApos() {
        def root = new XmlParser().parseText(attributeInput)
        def writer = new StringWriter()
        new XmlNodePrinter(new PrintWriter(writer), "  ", "'").print(root)
        def result = writer.toString()
        assertEquals attributeExpectedOutputApos, result
    }

    void testContentWithSpecialSymbols() {
        def root = new XmlParser().parseText(tagWithSpecialCharsInput)
        def writer = new StringWriter()
        new XmlNodePrinter(new PrintWriter(writer), "  ", "'").print(root)
        def result = writer.toString()
        assertEquals tagWithSpecialCharsOutput, result
    }

    void testAttributeWithNamespaceInput() {
        def root = new XmlParser().parseText(attributeWithNamespaceInput)
        def writer = new StringWriter()
        new XmlNodePrinter(new PrintWriter(writer), "  ").print(root)
        def result = writer.toString()
        assertEquals attributeWithNamespaceInput, result
    }
}
