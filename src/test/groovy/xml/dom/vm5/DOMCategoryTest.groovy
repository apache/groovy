package groovy.xml.dom.vm5

import groovy.xml.DOMBuilder
import static javax.xml.xpath.XPathConstants.*

class DOMCategoryTest extends GroovyTestCase {
    void testXPathWithDomCategory() {
        def reader = new StringReader('<a><b>B1</b><b>B2</b><c a1="4" a2="true">C</c></a>')
        def root = DOMBuilder.parse(reader)
        use(DOMCategory) {
            assert root.xpath('a/c/text()') == 'C'
            def text = { n -> n.xpath('text()') }
            assert text(root.xpath('a/c', NODE)) == 'C'
            assert root.xpath('a/c/@a1') == '4'
            assert root.xpath('a/c/@a1', NUMBER) == 4
            assert root.xpath('a/c/@a2', BOOLEAN)
            assert root.xpath('a/b', NODESET).collect(text).join() == 'B1B2'
        }
    }
}
