class CompilerErrorTest extends GroovyTestCase {

    void testBadMethodName() {

        shouldFail {
            println "About to call shell script"
            println "Really am about to call shell script"

            shell = new GroovyShell()
            text = 'badMethod(); println "Called method"'
            println "About to test script ${text}"
            shell.evaluate(text)
        }
    }

    void testBadPropertyName() {

        shouldFail {
            shell = new GroovyShell()
            shell.evaluate """
                x = [:]
                x.$foo = 123
            """
        }
    }

    void testBadVariableName() {

        shouldFail {
            shell = new GroovyShell()
            shell.evaluate """
                $x = 123
            """
        }
    }

}