package groovy.util

import groovy.xml.TraversalTestSupport
import groovy.xml.GpathSyntaxTestSupport

class XmlParserTest extends GroovyTestCase {

    def getRoot = { xml -> new XmlParser().parseText(xml) }
    
    void testNodePrinter() {
        def text = """
<p>Please read the <a href="index.html">Home</a> page</p>
"""
        def node = new XmlParser().parseText(text)
        new NodePrinter().print(node)
    }

    void testElement() {
        GpathSyntaxTestSupport.checkElement(getRoot)
        GpathSyntaxTestSupport.checkFindElement(getRoot)
        GpathSyntaxTestSupport.checkElementTypes(getRoot)
        GpathSyntaxTestSupport.checkElementClosureInteraction(getRoot)
    }

    void testAttribute() {
        GpathSyntaxTestSupport.checkAttribute(getRoot)
        GpathSyntaxTestSupport.checkAttributes(getRoot)
    }

    void testNavigation() {
        GpathSyntaxTestSupport.checkChildren(getRoot)
        GpathSyntaxTestSupport.checkParent(getRoot)
    }

    void testTraversal() {
        TraversalTestSupport.checkDepthFirst(getRoot)
        TraversalTestSupport.checkBreadthFirst(getRoot)
    }

    void testMixedMarkup() {
        GpathSyntaxTestSupport.checkMixedMarkup(getRoot)
    }
}
