package groovy.bugs

class VerifyErrorBug extends GroovyTestCase {
    void testShouldNotThrowVerifyError1() {
        assertScript """
            x = this.&println
            for (def i = 1; i < 10; i++) {
                x { i - 1 }
                x { 1 - i }
            }
        """
    }
    void testShouldNotThrowVerifyError2() {
        assertScript """
            for (int i = 1; i < 10; i++) {
                i - 1
                1 - i
            }
        """
    }
    void testShouldNotThrowVerifyError3() {
        assertScript """
            x = this.&println
            for (int i = 1; i < 10; i++) {
                x { i - 1 }
                x { 1 - i }
            }
        """
    }
    void testShouldNotThrowVerifyError4() {
        assertScript """
            x = this.&println
            int i = 0
            x { i-1 }
        """
    }
}
