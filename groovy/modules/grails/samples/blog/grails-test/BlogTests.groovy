class BlogTests extends GroovyTestCase {

	void testValid() {
		def e = new Entry()
		assert !e.validate()
		
		e.title = "Test title"
		e.body = "Test Body"
		assert e.validate()
	}
}
