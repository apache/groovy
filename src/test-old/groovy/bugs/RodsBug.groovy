/**
 * @author Rod Cope
 * @version $Revision$
 */
class RodsBug extends GroovyTestCase {
    
    void testBug() {
        doTest(true)
        /*
         x = 1
         if (x > 0) {
         String name = "Rod"
         println(name)
         }
         */
    }
    
    void testBug2() {
        x = 1
        if (x > 0) {
            //String name = "Rod"
            name = "Rod"
            println(name)
        }
    }
    
    void doTest(flag) {
        if (flag) {
            String name = "Rod"
            //name = "Rod"
            doAssert(name)
        }
    }
    
    void doTest() {
        String name = "Rod"
        doAssert(name)
    }
    
    void doAssert(text) {
        assert text != null
    }
}