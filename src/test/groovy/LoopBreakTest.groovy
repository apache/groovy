class LoopBreakTest extends GroovyTestCase {

    void testWhileWithBreak() {
        x = 0
        while (true) {
            if (x == 5) {
                break
            }
            x++
            
            assert x < 10 : "Should never get here"
        }
        
        println "worked: value ${x}"
	}
    
    
    /* @todo parser
     
    void testDoWhileWithBreak() {
        x = 0
        do {
            if (x == 5) {
                break
            }
            x++
            
            assert x < 10 : "Should never get here"
        }
    	while (true)
        
        println "worked: value ${x}"
    }
     */

    void testForWithBreak() {
        /** @todo commenting out the x = 0 causes bytecode gen error */
        x = 0
        for (x in 0..20) {
            if (x == 5) {
                break
            }
            assert x < 10 : "Should never get here"
        }
        
        println "worked: value ${x}"
    }
 }
