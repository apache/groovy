package groovy

class SimplePostfixTest extends GroovyTestCase {

    void testPostfix() {
        def x = 1
        ++x
        println(x)

        assert x == 2
    }

}