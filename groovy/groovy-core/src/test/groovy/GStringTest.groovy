class GStringTest extends GroovyTestCase {

    void testWithOneVariable() {
        
        name = "Bob"
        
        template = "hello ${name} how are you?"
				
		assert template instanceof GString
											 
	 	count = template.getValueCount()
		assert count == 1
		assert template.getValue(0) == "Bob"
											 
		string = template.toString()
		assert string == "hello Bob how are you?"
	}
    
    void testWithVariableAtEnd() {
        name = "Bob"
        template = "hello ${name}"
        string = template.toString()
        
        assert string == "hello Bob"
    }
    
    void testWithVariableAtBeginning() {
        name = "Bob"
        template = "${name} hey"
        string = template.toString()
        
        assert string == "Bob hey"
    }

    void testWithJustVariable() {
        name = "Bob"
        template = "${name}"
        string = template.toString()
        
        assert string == "Bob"
    }

}
