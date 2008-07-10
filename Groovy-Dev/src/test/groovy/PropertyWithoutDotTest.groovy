package groovy

class PropertyWithoutDotTest extends GroovyTestCase {
    def getFoo() {
        return "cheese"
    }
    
    void testProperty() {
        def value = foo
        
        assert value == "cheese"
    }
}