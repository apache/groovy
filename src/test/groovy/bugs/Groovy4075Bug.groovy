package groovy.bugs

class Groovy4075Bug extends GroovyTestCase {
    public static void failChecked() throws Exception {
        throw new Exception(new IOException());
    }
    
    public static void failUnchecked() {
        throw new RuntimeException(new IOException());
    }
    
    public void testCheckedFailure() {
        shouldFailWithCause(IOException.class, {Groovy4075Bug.failChecked()})    
    }
    
    public void testUncheckedFailure() {
        shouldFailWithCause(IOException.class, {Groovy4075Bug.failUnchecked()})    
    }
}
