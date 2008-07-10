package groovy

class CompilerErrorTest extends GroovyTestCase {

    void testBadMethodName() {

        shouldFail {
            println "About to call shell script"
            println "Really am about to call shell script"

            def shell = new GroovyShell()
            def text = 'badMethod(); println "Called method"'
            println "About to test script ${text}"
            shell.evaluate(text)
        }
    }

    void testBadPropertyName() {

        shouldFail {
            def shell = new GroovyShell()
            shell.evaluate """
                def x = [:]
                x.0foo = 123
            """
        }
    }

    void testBadVariableName() {

        shouldFail {
            def shell = new GroovyShell()
            shell.evaluate """
                def 1x = 123
            """
        }
    }

}