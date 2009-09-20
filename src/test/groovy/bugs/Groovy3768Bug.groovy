package groovy.bugs

import gls.CompilableTestSupport

class Groovy3768Bug extends CompilableTestSupport {
    void testLocalVariableMarkedStatic() {
        
    	shouldNotCompile """
    	    static int x = 3
    	"""
    	
        shouldNotCompile """
            def m() {
    	        static int x = 3
    	    }
        """
        
        shouldNotCompile """
	        class G3768A {
                def m() {
                    static int x = 3
                }
	        }
	    """
	    
        shouldCompile """
            class G3768B {
                static int x = 3
            }
        """
    }
}
