package groovy.bugs

/**
 * @version $Revision$
 */
class FullyQualifiedClassBug extends GroovyTestCase {

    void testBug() {
        java.lang.System.err.println("Hello world")
    }
    
}