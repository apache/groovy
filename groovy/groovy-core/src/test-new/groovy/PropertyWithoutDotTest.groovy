class PropertyWithoutDotTest extends GroovyTestCase {
    def getFoo() {
        return "cheese"
    }
    
    void testProperty() {
        value = foo
        
        assert value == "cheese"
    }
}