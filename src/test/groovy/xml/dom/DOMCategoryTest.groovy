package groovy.xml.dom

import groovy.xml.DOMBuilder
import groovy.xml.GpathSyntaxTestSupport

class DOMCategoryTest extends GroovyTestCase {

    def getRoot = { xml ->
        def reader = new StringReader(xml)
        def doc    = DOMBuilder.parse(reader)
        def root   = doc.documentElement
    }

//    void testMixedMarkup() {
//        use(DOMCategory) {
//            GpathSyntaxTestSupport.checkMixedMarkup(getRoot)
//        }
//    }

    void testElement() {
        use(DOMCategory) {
            GpathSyntaxTestSupport.checkElement(getRoot)
            GpathSyntaxTestSupport.checkFindElement(getRoot)
            GpathSyntaxTestSupport.checkElementTypes(getRoot)
            GpathSyntaxTestSupport.checkElementClosureInteraction(getRoot)
        }
    }

    void testAttribute() {
        use(DOMCategory) {
            GpathSyntaxTestSupport.checkAttribute(getRoot)
            GpathSyntaxTestSupport.checkAttributes(getRoot)
        }
    }

    void testNavigation() {
        use(DOMCategory) {
            GpathSyntaxTestSupport.checkChildren(getRoot)
            GpathSyntaxTestSupport.checkParent(getRoot)
        }
    }

}
