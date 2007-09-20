package groovy

/**
 * @author Jeremy Rayner 
 */
class NullPropertyTest extends GroovyTestCase { 
    def wensleydale = null

    void testNullProperty() { 
        assert wensleydale == null 
    } 
} 


