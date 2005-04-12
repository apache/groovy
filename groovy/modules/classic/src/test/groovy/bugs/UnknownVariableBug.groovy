/**
 * @version $Revision$
 */
class UnknownVariableBug extends GroovyTestCase {

    void testBug() {
        shouldFail {
            println(foo)
        }
    }
}