class GStringTest extends GroovyTestCase {

    void testWithOneVariable() {
        
        name = "Bob"
        
        template = "hello ${name} how are you?"
					
											 	
		string = template.toString()
		assert string == "hello Bob how are you?"
	}
}
