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

}
