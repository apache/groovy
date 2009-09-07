package groovy.bugs

import gls.CompilableTestSupport

class Groovy3721Bug extends CompilableTestSupport {
    void testCompilationWithDuplicateJavaBeanProperties() {
    	shouldNotCompile """
    		class Foo3721V1 {
    			def F = 1
    			def f = 2
    		}
    	"""
        shouldNotCompile """
	        class Foo3721V2 {
	            def f = 0
	            def a = 1
	            def F = 2
	        }
	    """
    }
}
