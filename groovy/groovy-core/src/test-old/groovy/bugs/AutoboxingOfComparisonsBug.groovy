class AutoboxingOfComparisonsBug extends GroovyTestCase {
    
    void testBug() {
        y = true
        x = y == true
        
        assert x
        
        println(y == true)
        println(y != false)
    }
}