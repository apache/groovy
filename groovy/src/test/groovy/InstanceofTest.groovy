package groovy

class InstanceofTest extends GroovyTestCase {

    void testTrue() {

        def x = false
        def o = 12
        
        if ( o instanceof Integer ) {
            x = true
        }

        assert x == true
    }
    
    void testFalse() {

        def x = false
        def o = 12
        
        if ( o instanceof Double ) {
            x = true
        }

        assert x == false
    }
    
    void testImportedClass() {
        def m = ["xyz":2]
        assert m instanceof Map
        assert !(m instanceof Double)
        
        assertTrue(m instanceof Map)
        assertFalse(m instanceof Double)
    }
    
    void testFullyQualifiedClass() {
        def l = [1, 2, 3]
        assert l instanceof java.util.List
        assert !(l instanceof Map)
        
        assertTrue(l instanceof java.util.List)
        assertFalse(l instanceof Map)
    }
    
    void testBoolean(){
       assert true instanceof Object
       assert true==true instanceof Object
       assert true==false instanceof Object
       assert true==false instanceof Boolean
       assert !new Object() instanceof Boolean
    }
}
