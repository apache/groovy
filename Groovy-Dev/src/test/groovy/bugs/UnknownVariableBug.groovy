package groovy.bugs

/**
 * @version $Revision$
 */
class UnknownVariableBug extends GroovyTestCase {
    void testBug() {
        def shell = new GroovyShell()
        shouldFail {
            shell.evaluate """
                def x = foo
            """
        }
        shell.evaluate """
            foo = 1
            def x = foo
        """
    }
}