/**
 * @version $Revision$
 */
class AssignmentInsideExpressionBug extends GroovyTestCase {
    
    void testBug() {
        if ((x = someMethod()) != null) {
            println x
        }
        if ((y = getFoo()) > 5) {
            println "y is greater than 5"
        }
        
        def a = b = 123
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