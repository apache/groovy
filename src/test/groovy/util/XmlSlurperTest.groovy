package groovy.util

class XmlSlurperTest extends GroovyTestCase {
    
    void testXmlParser() {
        def text = """
<characters>
    <character id="1" name="Wallace">
    	<likes>cheese</likes>
    </character>
    <character id="2" name="Gromit">
	    <likes>sleep</likes>
    </character>
    <numericValue>1</numericValue>
    <booleanValue>y</booleanValue>
    <uriValue>http://example.org/</uriValue>
    <urlValue>http://example.org/</urlValue>
</characters>
"""
        
        def node = new XmlSlurper().parseText(text);
        
        assert node != null
        assert node.children().size() == 6 , "Children ${node.children()}"
        
        def characters = node.character
        
        for (c in characters) {
            println c.@name
        }
        
        assert characters.size() == 2
        
        assert node.character.likes.size() == 2 , "Likes ${node.character.likes}"
        
        // lets find Gromit
        def gromit = node.character.find { it.@id == '2' }
        assert gromit != null , "Should have found Gromit!"
        assert gromit.@name == "Gromit"
        assert gromit.@name.name() == "name"
        
        
        // lets find what Wallace likes in 1 query
        def answer = node.character.find { it.@id.toInteger() == 1 }.likes.text()
        assert answer == "cheese"
        
        //test parent()
        assert gromit.likes.parent()==gromit
        assert gromit.parent()==node
        assert node.parent()==node
        
        assert node.numericValue.toInteger() == 1
        assert node.numericValue.toLong() == 1
        assert node.numericValue.toFloat() == 1
        assert node.numericValue.toDouble() == 1
        assert node.numericValue.toBigInteger() == 1
        assert node.numericValue.toBigDecimal() == 1
        
        assert node.booleanValue.toBoolean() == true
        
        assert node.uriValue.toURI() == "http://example.org/".toURI()
        
        assert node.urlValue.toURL() == "http://example.org/".toURL()
        
        def wsdl = '''                                                                                
            <definitions name="AgencyManagementService">                                              
                <message name="SomeRequest">                                                          
                    <part name="parameters" element="ns1:SomeReq" />                                  
                </message>                                                                            
                <message name="SomeResponse">                                                         
                    <part name="result" element="ns1:SomeRsp" />                                      
                </message>                                                                            
            </definitions>                                                                            
            '''
        def xml = new XmlSlurper().parseText(wsdl)
        assert xml.message.part.@element.findAll {it =~ /.Req$/}.size() == 1
        assert xml.message.part.findAll { true }.size() == 2
        assert xml.message.part.find { it.name() == 'part' }.name() == 'part'
        assert xml.message.findAll { true }.size() == 2
        xml.message.findAll { true }.each { assert it.name() == "message"}
        println xml.message.part.findAll { true }
    }

    void testDepthFirst() {
        XmlTraversalTestUtil.checkDepthFirst(new XmlSlurper())
    }

    void testBreadthFirst() {
        XmlTraversalTestUtil.checkBreadthFirst(new XmlSlurper())
    }
}
