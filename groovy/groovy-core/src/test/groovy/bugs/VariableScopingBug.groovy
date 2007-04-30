package groovy.bugs

/**
 * @version $Revision$
 */
class VariableScopingBug extends TestSupport {
    
    void testBug() {
        // undeclared variable x

        shouldFail {
            def shell = new GroovyShell()
            shell.evaluate("""
                class SomeTest {
                    void run() {
                        for (z in 0..2) {
                            def x = [1, 2, 3]
                        }

                        for (t in 0..3) {
                            for (y in x) {
                                println x
                            }
                        }
                    }
               }
               new SomeTest().run()""")
           }
    }

    void testVariableReuse() {
        def shell = new GroovyShell()
        shell.evaluate("""
            for (z in 0..2) {
                def x = [1, 2, 3]
            }

            for (t in 0..3) {
                def x = 123
                println x
            }""")
    }
}