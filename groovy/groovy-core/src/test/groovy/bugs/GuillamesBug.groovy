/**
 * @author Guillaume Laforge
 * @version $Revision$
 */
class GuillamesBug extends GroovyTestCase {
    
    void testBug() {
        if (true) 
            println("true")
    }
}