/**
 * @version $Revision$
 */
class UnknownVariableBug extends GroovyTestCase {

    void testBug() {
        shouldFail {
            def shell = new GroovyShell()
            shell.evaluate """
                println(foo)
            """
        }
    }
}