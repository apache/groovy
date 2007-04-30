package groovy.bugs

/**
 * @author John Wilson
 * @version $Revision$
 */
class TryCatchBug extends GroovyTestCase {
    
    void testBug() {
        try {
            println("Hello")
        }
        finally {
            println("Finally")
        }
    }
}