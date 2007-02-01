package groovy.bugs

/**
 * @author Guillaume Laforge 
 * @version $Revision$
 */
class GuillaumesBug extends GroovyTestCase {
    
    void testBug() {
        if (true) 
            println("true")
    }
}