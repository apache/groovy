package groovy.xml.dom

import groovy.xml.DOMBuilder
import groovy.xml.GpathSyntaxTestSupport
import groovy.xml.TraversalTestSupport
import groovy.xml.MixedMarkupTestSupport

class DOMCategoryTest extends GroovyTestCase {

    def getRoot = { xml ->
        def reader = new StringReader(xml)
        def doc    = DOMBuilder.parse(reader)
        def root   = doc.documentElement
    }

    void testMixedMarkup() {
        use(DOMCategory) {
            MixedMarkupTestSupport.checkMixedMarkup(getRoot)
        }
    }

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
            GpathSyntaxTestSupport.checkNestedSizeExpressions(getRoot)
        }
    }

    void testTraversal() {
        use(DOMCategory) {
            TraversalTestSupport.checkDepthFirst(getRoot)
            TraversalTestSupport.checkBreadthFirst(getRoot)
        }
    }

}
