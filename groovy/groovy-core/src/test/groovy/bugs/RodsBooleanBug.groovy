/**
 * @version $Revision$
 */
class RodsBooleanBug extends GroovyTestCase {

    item = "hi"
    
    void testBug() {
        assert isIt()
    }
    
    isIt() {
        return item != null && item == "hi"
    }
    
}