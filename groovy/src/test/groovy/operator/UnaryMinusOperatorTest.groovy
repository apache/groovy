package groovy.operator

class UnaryMinusOperatorTest extends GroovyTestCase {

    void testUnaryMinus() {
        def value = -1
        
        assert value == -1
        
        def x = value + 2
        assert x == 1
        
        def y = -value
        assert y == 1
    }   
    
    void testBug() {
        def a = 1
        def b = -a
        
        assert b == -1
    }
    
    void testShellBug() {
        assertScript("""
def a = 1
def b = -a
assert b == -1            
""")
    }
}