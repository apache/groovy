class LoopBreakTest extends GroovyTestCase {

    void testWhileWithBreak() {
        def x = 0
        while (true) {
            if (x == 5) {
                break
            }
            ++x

            assert x < 10 , "Should never get here"
        }
        
        println "worked: while completed with value ${x}"
    }
    
    
    void testDoWhileWithBreak() {
        def x = 0
        do {
            //println "in do-while loop and x = ${x}"
            
            if (x == 5) {
                break
            }
            ++x
            
            assert x < 10 , "Should never get here"
        }
        while (true)
        
        println "worked: do-while completed with value ${x}"
    }

    void testForWithBreak() {
        for (x in 0..20) {
            if (x == 5) {
                break
            }
            assert x < 10 , "Should never get here"
        }
        
        println "worked: for loop completed with value ${x}"
    }
 }
