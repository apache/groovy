package groovy.lang

public class ExceptionTest extends GroovyTestCase {

    private int finallyCounter;
    
    def m1() {
        // this code is in a method, because we need to test
        // insertions for return here along with the method
        try { 
            throw new RuntimeException("1") 
        } catch (Throwable t) {
        } finally { 
            finallyCounter++
            throw new RuntimeException("2") 
        }
    }
    
    void testFinallyExceptionOverridingTryException() {
        finallyCounter = 0
        try {
            m1()
            assert false
        } catch (RuntimeException re) {
            assert re.message == "2"
        }
        assert finallyCounter == 1
    }
    
    def m2() {
        try {
            def x = 0
        } catch (Throwable t) {
        } finally { 
            finallyCounter++ 
            throw new RuntimeException("1") 
        }
    }
    
    void testFinallyExceptionAlone() {
        finallyCounter = 0
        try {
            m2()
            assert false
        } catch (RuntimeException re) {
            assert re.message == "1"
        }
        assert finallyCounter == 1
    }
    
    def m3() {    
        try {
          throw new RuntimeException("1")
        } catch (RuntimeException e) {
          finallyCounter++
          throw e
        } finally {
          finallyCounter++
        }
    }
    
    void testExceptionAndCatchBlock() {
        finallyCounter = 0
        try {
            m3()
            assert false
        } catch (RuntimeException re) {
            assert re.message == "1"
        }
        assert finallyCounter == 2
    }        
}
