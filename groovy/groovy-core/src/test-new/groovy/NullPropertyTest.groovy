/**
 * @author Jeremy Rayner 
 */
class NullPropertyTest extends GroovyTestCase { 
    wensleydale = null 

    void testNullProperty() { 
        assert wensleydale == null 
    } 
} 


