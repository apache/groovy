class LeftShiftTest extends GroovyTestCase {

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
}
