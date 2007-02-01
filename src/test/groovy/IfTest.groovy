package groovy

class IfTest extends GroovyTestCase {

    void testUsingNumber() {
        def x = 1

        if (x) {
            println "${x} is true"
        }
        else {
            fail("should not be false")
        }

        x = 0

        if (x) {
            fail("should not be true")
        }
        else {
            println "${x} is false"
        }

    }

    void testUsingString() {
        def x = "abc"

        if (x) {
            println "${x} is true"
        }
        else {
            fail("should not be false")
        }

        x = ""

        if (x) {
            fail("should not be true")
        }
        else {
            println "${x} is false"
        }
    }
}
