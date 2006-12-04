class AutoboxingOfComparisonsBug extends GroovyTestCase {
    
    void testBug() {
        def y = true
        def x = y == true
        
        assert x
        
        println(y == true)
        println(y != false)
    }
}