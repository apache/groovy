package groovy.xml.dom.vm5

import groovy.xml.DOMBuilder
import static javax.xml.xpath.XPathConstants.*

class DOMCategoryXpathTest extends GroovyTestCase {
    void testXPathWithDomCategory() {
        def reader = new StringReader('<a><b>B1</b><b>B2</b><c a1="4" a2="true">C</c></a>')
        def root = DOMBuilder.parse(reader).documentElement
        use(groovy.xml.dom.DOMCategory) {
            assert root.xpath('c/text()') == 'C'
            def text = { n -> n.xpath('text()') }
            assert text(root.xpath('c', NODE)) == 'C'
            assert root.xpath('c/@a1') == '4'
            assert root.xpath('c/@a1', NUMBER) == 4
            assert root.xpath('c/@a2', BOOLEAN)
            assert root.xpath('b', NODESET).collect(text).join() == 'B1B2'
        }
    }
}
