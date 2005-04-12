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
        
        /** @todo uncomment when the parser allows this
        a = b = 123
        assert a == 123
        assert b == 123
        */
    }

    someMethod() {
        return "worked!"
    }
    
    getFoo() {
        return 7
    }
}