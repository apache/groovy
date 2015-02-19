package groovy.text.markup

import groovy.text.markup.MarkupTemplateEngine.TemplateResource;

class TemplateResourceTest extends GroovyTestCase {
	
	void testSimplePath() {
		def resource = TemplateResource.parse("simple.foo")
		assertFalse(resource.hasLocale())
		assertEquals("simple.foo", resource.toString())
		assertEquals("simple_fr_FR.foo", resource.withLocale("fr_FR").toString())
	}
	
	void testPathWithLocale() {
		def resource = TemplateResource.parse("simple_fr_FR.foo")
		assertTrue(resource.hasLocale())
		assertEquals("simple_fr_FR.foo", resource.toString())
		assertEquals("simple.foo", resource.withLocale(null).toString())
	}
	
	void testPathWithMultipleDots() {
		def resource = TemplateResource.parse("simple.foo.bar")
		assertFalse(resource.hasLocale())
		assertEquals("simple.foo.bar", resource.toString())
		assertEquals("simple_fr_FR.foo.bar", resource.withLocale("fr_FR").toString())
	}
	
	void testPathWithLocaleAndMultipleDots() {
		def resource = TemplateResource.parse("simple_fr_FR.foo.bar")
		assertTrue(resource.hasLocale())
		assertEquals("simple_fr_FR.foo.bar", resource.toString())
		assertEquals("simple.foo.bar", resource.withLocale(null).toString())
	}

}
