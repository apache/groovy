class CompilerErrorTest extends GroovyTestCase {

    void testBadMethodName() {

        shouldFail {
            println ${name}
        }
    }
}