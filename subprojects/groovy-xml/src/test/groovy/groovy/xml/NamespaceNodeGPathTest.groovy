package groovy.xml

/**
 * Test the use of GPath navigation with namespaces
 */
class NamespaceNodeGPathTest extends TestXmlSupport {

    void testTree() {
        Node root = new XmlParser().parseText("""
<xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema">
  <xsd:annotation xsd:cheese="Edam">
     <xsd:documentation>Purchase order schema for Example.com.</xsd:documentation>
  </xsd:annotation>
</xsd:schema>
""")

        Namespace xsd = new Namespace('http://www.w3.org/2001/XMLSchema', 'xsd')

        def children = root.children()

        def name = root.name()

        root.children().each {println "has a child with name ${it.name()} and content $it"}

        def foo = xsd.annotation
        println "qname url is $foo.namespaceURI"
        println "qname prefix is $foo.prefix"
        println "qname localPart is $foo.localPart"

        def a = root[xsd.annotation]
        println "Found results $a"

        assert a.size() == 1: " size is $a.size()"

        def aNode = a[0]
        def cheese = aNode.attributes()[xsd.cheese]
        assert cheese == "Edam"
        println "Found namespaced attribute $cheese"

        cheese = aNode.attribute(xsd.cheese)
        assert cheese == "Edam"
        println "Found namespaced attribute $cheese"

        def e = root[xsd.annotation][xsd.documentation]
        assert e.size() == 1: " size is $e.size()"
        assert e.text() == "Purchase order schema for Example.com."

        e = a[xsd.documentation]
        assert e.size() == 1: " size is $e.size()"
        assert e.text() == "Purchase order schema for Example.com."
    }
}