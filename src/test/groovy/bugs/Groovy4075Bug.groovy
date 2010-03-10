package groovy.bugs

class Groovy4075Bug extends GroovyTestCase {
    static void failChecked() throws Exception {
        throw new Exception(new IOException(new NullPointerException("NPE in failChecked")));
    }
    
    static void failUnchecked() {
        throw new RuntimeException(new IOException("IOE in failUnchecked", new NullPointerException()));
    }
    
    void testCheckedFailure() {
        assert shouldFailWithCause(NullPointerException) {
            Groovy4075Bug.failChecked()
        } == "NPE in failChecked"
    }
    
    void testUncheckedFailure() {
        assert shouldFailWithCause(IOException) {
            Groovy4075Bug.failUnchecked()
        } == "IOE in failUnchecked"
    }
}
