package groovy.util

import groovy.xml.TraversalTestSupport
import groovy.xml.GpathSyntaxTestSupport

class XmlParserTest extends GroovyTestCase {

    def getRoot = { xml -> new XmlParser().parseText(xml) }

    void testXmlParser() {
        def text = """
<characters>
    <character id="1" name="Wallace">
    	<likes>cheese</likes>
    </character>
    <character id="2" name="Gromit">
	    <likes>sleep</likes>
    </character>
</characters>
"""
        
        def node = new XmlParser().parseText(text);
        
        assert node != null
        assert node.children().size() == 2 , "Children ${node.children()}"
        
        def characters = node.character
        
        for (c in characters) {
            println c['@name']
        }
        
        assert characters.size() == 2
        
        assert node.character.likes.size() == 2 , "Likes ${node.character.likes}"
        
        // lets find Gromit
        def gromit = node.character.find { it['@id'] == '2' }
        assert gromit != null , "Should have found Gromit!"
        assert gromit['@name'] == "Gromit"
        
        
        // lets find what Wallace likes in 1 query
        def answer = node.character.find { it['@id'] == '1' }.likes[0].text()
        assert answer == "cheese"
    }
    
    void testNodePrinter() {
        def text = """
<p>Please read the <a href="index.html">Home</a> page</p>
"""
        def node = new XmlParser().parseText(text)
        new NodePrinter().print(node)
    }

    void testMixedMarkup() {
        GpathSyntaxTestSupport.checkMixedMarkup(getRoot)
    }

    void testElement() {
        GpathSyntaxTestSupport.checkElement(getRoot)
    }

    void testAttribute() {
        GpathSyntaxTestSupport.checkAttribute(getRoot)
    }

    void testAttributes() {
        GpathSyntaxTestSupport.checkAttributes(getRoot)
    }

    void testDepthFirst() {
        TraversalTestSupport.checkDepthFirst(getRoot)
    }

    void testBreadthFirst() {
        TraversalTestSupport.checkBreadthFirst(getRoot)
    }
}
