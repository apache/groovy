package groovy.bugs

class AutoboxingOfComparisonsBug extends GroovyTestCase {
    void testBug() {
        def y = true
        def x = y == true
        def z = y != false
        assert x && z
    }
}