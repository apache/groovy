/**
 * @version $Revision$
 */
class CallingClosuresWithClosuresBug extends GroovyTestCase {

    void testBug() {
        a = {1}
        // old workaround
        //b = {a.call()}
        b = {a()}
        
        value = b()
        
        assert value == 1
    }
}