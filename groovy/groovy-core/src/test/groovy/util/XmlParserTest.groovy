package groovy.util

class XmlParserTest extends GroovyTestCase {
    
    void testXmlParser() {
        text = """
<characters>
    <character id="1" name="Wallace">
    	<likes>cheese</likes>
    </character>
    <character id="2" name="Gromit">
	    <likes>sleep</likes>
    </character>
</characters>
"""
        
        node = new XmlParser().parseText(text);
        
        assert node != null
        assert node.children().size() == 2 : "Children ${node.children()}"
        
        characters = node.character
        
        for (c in characters) {
            println c['@name']
        }
        
        assert characters.size() == 2
        
        assert node.character.likes.size() == 2 : "Likes ${node.character.likes}"
        
        // lets find Gromit
        gromit = node.character.find { it['@id'] == '2' }
        assert gromit != null : "Should have found Gromit!"
        assert gromit['@name'] == "Gromit"
        
        
        // lets find what Wallace likes in 1 query
        answer = node.character.find { it['@id'] == '1' }.likes.get(0).text()
        assert answer == "cheese"
    }
    
    void testMixedMarkup() {
        text = <<<EOF
<p>Please read the <a href="index.html">Home</a> page</p>
EOF
        
        node = new XmlParser().parseText(text)
        
        new NodePrinter().print(node)
        
        assert node != null
        children = node.children()
        assert children.size() == 3 : "Children ${children}"
        assert children[0] instanceof String
        assert children[1] instanceof Node
        assert children[2] instanceof String
    }
}
