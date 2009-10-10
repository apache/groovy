package groovy.bugs

import gls.CompilableTestSupport

class Groovy3817Bug extends CompilableTestSupport {
    void testUsageOfRangeExpressionJustAfterTryCatch() {
        shouldCompile """
            try { println "nix" } 
            catch ( Exception e ) { e.printStackTrace() }
            (1..10).each{ print it }
        """

        shouldCompile """
            try { println "nix" } 
            catch ( Exception e ) { 
                e.printStackTrace() 
            }
            (1..10).each{ print it }
        """

        shouldCompile """
            try { println "nix" } catch ( Exception e ) { e.printStackTrace() }
            (1..10).each{ print it }
        """
    }
}
