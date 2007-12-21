package groovy.bugs

/**
 * @version $Revision$
 */
class AssignmentInsideExpressionBug extends GroovyTestCase {
    
    void testBug() {
        def x
        if ((x = someMethod()) != null) {
            println x
        }
        def y
        if ((y = getFoo()) > 5) {
            println "y is greater than 5"
        }
        
        def a = 123, b = 123
        assert a == 123
        assert b == 123
    }

    def someMethod() {
        return "worked!"
    }
    
    def getFoo() {
        return 7
    }
}