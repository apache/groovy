class PropertyWithoutDotTest extends GroovyTestCase {
    getFoo() {
        return "cheese"
    }
    
    void testProperty() {
        value = foo
        
        assert value == "cheese"
    }
}