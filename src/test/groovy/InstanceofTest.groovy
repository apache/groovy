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
}
