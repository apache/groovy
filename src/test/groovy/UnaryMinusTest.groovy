class UnaryMinusTest extends GroovyTestCase {

    void testUnaryMinus() {
        value = -1
        
        assert value == -1
        
        x = value + 2
        assert x == 1
        
        y = -value
        assert y == 1
	}   
    
    void testBug() {
        a = 1
        b = -a
        
        assert b == -1
    }
    
    void testShellBug() {
        assertScript("""
a = 1
b = -a
assert b == -1            
""")
    }
}