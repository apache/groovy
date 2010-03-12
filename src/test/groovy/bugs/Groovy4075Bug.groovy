package groovy.bugs

class Groovy4075Bug extends GroovyTestCase {
    static void failChecked() throws Exception {
        throw new Exception(new IllegalArgumentException(new NullPointerException("NPE in failChecked")))
    }
    
    static void failUnchecked() {
        throw new RuntimeException(new IllegalArgumentException("IAE in failUnchecked", new NullPointerException()))
    }
    
    void testCheckedFailure() {
        assert shouldFailWithCause(NullPointerException) {
            Groovy4075Bug.failChecked()
        } == "NPE in failChecked"
    }
    
    void testUncheckedFailure() {
        assert shouldFailWithCause(IllegalArgumentException) {
            Groovy4075Bug.failUnchecked()
        } == "IAE in failUnchecked"
    }
}
