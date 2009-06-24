package groovy.bugs

class Groovy3590Bug extends GroovyTestCase {
    void testMapDefaultValueGetWithPrevKeyHavingNullValue() {
        def map = ['key':null]
        assert map.get('key', this) == null
    }
}
