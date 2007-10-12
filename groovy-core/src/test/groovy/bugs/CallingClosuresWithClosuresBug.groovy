package groovy.bugs

/**
 * @version $Revision$
 */
class CallingClosuresWithClosuresBug extends GroovyTestCase {

    void testBug() {
        def a = {1}
        // old workaround
        //def b = {a.call()}
        def b = {a()}
        
        def value = b()
        
        assert value == 1
    }
}