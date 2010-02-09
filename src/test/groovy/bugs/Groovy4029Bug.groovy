package groovy.bugs

class Groovy4029Bug extends GroovyTestCase {
    void testAddNullKeyEntryInMapUsingSubscriptNotation() {
		Map m = [:]
		m[null] = null
		assert m.size() == 1
    }
}
