package groovy.util

import java.io.StringReader

class XmlParserTest extends GroovyTestCase {
    
    void testXmlParser() {
        text = <<<EOF
<characters>
    <character id="1" name="Wallace">
    	<likes>cheese</likes>
    </character>
    <character id="2" name="Gromit">
	    <likes>sleep</likes>
    </character>
</characters>
EOF
        
        parser = new XmlParser()
        node = parser.parse(new StringReader(text))
        
        //new NodePrinter().print(node)
        
        assert node != null
        assert node.children().size() == 2 : "Children ${node.children()}"
        
        characters = node.character
        
        for (c in characters) {
            println(c.attribute('name'))
        }
        
        assert characters.size() == 2
        
        assert node.character.likes.size() == 2 : "Likes ${node.character.likes}"
        
        // lets find Gromit
        gromit = node.character.find { it.attribute('id') == '2' }
        assert gromit != null : "Should have found Gromit!"
        assert gromit.attribute('name') == "Gromit"
        
        
        // lets find what Wallace likes in 1 query
        answer = node.character.find { it.attribute('id') == '1' }.likes.get(0).text()
        assert answer == "cheese"
    }
}
