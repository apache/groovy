class LittleClosureTest extends GroovyTestCase {

    void testClosure() {
        block = {x:: return x > 5}
    }
}
