package groovy.bugs

class ClosureWithBitwiseDefaultParamTest extends GroovyTestCase {
    void testAmbiguousStuff() {
        def c = { x, y = 1 | 2, z = 0->
            println x
            println y
            println z
        }

        // now lets invoke c
        // TODO when closures support default parameters
        //c.call()
    }
}