import java.util.Map

class InstanceofTest extends GroovyTestCase {

    void testTrue() {

        x = false
        o = 12
        
        if ( o instanceof Integer ) {
            x = true
        }

        assert x == true
    }
    
    void testFalse() {

        x = false
        o = 12
        
        if ( o instanceof Double ) {
            x = true
        }

        assert x == false
    }
    
    void testImportedClass() {
        m = ["xyz":2]
        assert m instanceof Map
        assert !(m instanceof Double)
        
        assertTrue(m instanceof Map)
        assertFalse(m instanceof Double)
    }
    
    void testFullyQualifiedClass() {
        l = [1, 2, 3]
        assert l instanceof java.util.List
        assert !(l instanceof Map)
        
        assertTrue(l instanceof java.util.List)
        assertFalse(l instanceof Map)
    }
}
