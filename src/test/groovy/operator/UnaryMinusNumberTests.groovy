package groovy.operator

class UnaryMinusNumberTests extends GroovyTestCase {

    void testNegateInteger() {
        def a = -1
        assert a == -1
    }

    void testNegateIntegerExpression() {
        def a = -1
        a = -a
        assert a == 1
    }

    void testNegateDouble() {
        def a = -1.0
        assert a == -1.0
    }

    void testNegateDoubleExpression() {
        def a = -1.0
        a = -a
        assert a == 1.0
    }

}
