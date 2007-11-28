package groovy.bugs

/**
 * @version $Revision$
 */
class RodsBooleanBug extends GroovyTestCase {

    def item = "hi"
    
    void testBug() {
        assert isIt()
    }
    
    def isIt() {
        return item != null && item == "hi"
    }
    
}