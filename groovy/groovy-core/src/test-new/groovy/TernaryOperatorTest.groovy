class TernaryOperatorTest extends GroovyTestCase {

    void testSimpleUse() {
        y = 5

        x = (y > 1) ? "worked" : "failed"
        assert x == "worked"


        x = (y < 4) ? "failed" : "worked"
        assert x == "worked"
    }

    void testUseInParameterCalling() {
        z = 123
        assertCalledWithFoo(z > 100 ? "foo" : "bar")
        assertCalledWithFoo(z < 100 ? "bar" : "foo")
       }

    assertCalledWithFoo(param) {
        println "called with param ${param}"
        assert param == "foo"
    }
}
