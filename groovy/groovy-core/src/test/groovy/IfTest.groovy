class IfTest extends GroovyTestCase {

    void testUsingNumber() {
        x = 1

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
        x = "abc"

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
}
