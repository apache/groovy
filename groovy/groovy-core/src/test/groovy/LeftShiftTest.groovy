package groovy

class LeftShiftTest extends GroovyTestCase {

    def foo = [1, 2, 3]

    void testShift() {
        def x = 4

        def y = x << 2

        println "Value is $y"

        assert y == 16

        assert x << 2 == 16
    }

    void testShiftList() {
        def list = []

        for (i in 1..10) {
            list << i
        }

        println "List is $list"
    }

    void testLeftShiftOnExpression() {
        this.foo << 4

        assert foo == [1, 2, 3, 4]
    }

}
