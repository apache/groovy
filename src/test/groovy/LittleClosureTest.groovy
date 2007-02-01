package groovy

class LittleClosureTest extends GroovyTestCase {

    void testClosure() {
        def block = {x-> return x > 5}
    }
}
