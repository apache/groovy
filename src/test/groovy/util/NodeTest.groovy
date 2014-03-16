package groovy.util

/**
 * @author Andrew Hamilton
 **/
class NodeTest extends GroovyTestCase {

    org.w3c.dom.Document document
    Node node

    void setUp() {
        def xml = "<doc><node>node1</node><node>node2</node><node>node3</node><nested><node>nested node</node></nested><empty-nest/></doc>"
        def bais = new ByteArrayInputStream(xml.getBytes("utf-8"))
        def factory = javax.xml.parsers.DocumentBuilderFactory.newInstance()
        def builder = factory.newDocumentBuilder()
        document = builder.parse(bais)
        def parser = new XmlParser()
        node = parser.parseText(xml)
    }

    void testTextEmpty() {
        assertEquals document.getFirstChild().getLastChild().getTextContent(), node.children().last().text()
    }

    void testTextBasic() {
        assertEquals document.getFirstChild().getFirstChild().getTextContent(), node.children().first().text()
    }

    void testTextWithChildren() {
        assertEquals document.getFirstChild().getTextContent(), node.text()
    }
}
