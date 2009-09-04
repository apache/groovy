package groovy.bugs

class Groovy3718Bug extends GroovyTestCase {
    void testPatternMatchOfNull() {
		assertFalse null ==~ /[^0-9]+/
		
		assertFalse null ==~ /[0-9]+/

		assertFalse "test" ==~ null
		
		assertFalse null ==~ null
    }
}
