/**
 * @version $Revision$
 */
class CallingClosuresWithClosuresBug extends GroovyTestCase {

    void testBug() {
        a = {1}
        b = {a.call()}
        /** @todo this fails
        b = {a()}
        */
        
        value = b()
        
        assert value == 1
    }
}