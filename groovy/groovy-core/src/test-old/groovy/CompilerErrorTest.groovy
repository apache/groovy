class CompilerErrorTest extends GroovyTestCase {

    void testBadMethodName() {

        shouldFail {
            shell = new GroovyShell()
            text = 'println ${name}'
            println "About to test script ${text}"
            shell.evaluate text
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