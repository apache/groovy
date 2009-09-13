package groovy.bugs

import gls.CompilableTestSupport

class Groovy3731Bug extends CompilableTestSupport {
	
	void testWrongGenericsUseInFieldsAndMethods() {
		shouldNotCompile """
			public class G3731A {
				Map<Object> m = [:]
			}
		"""

        shouldNotCompile """
	        public class G3731B {
	            void m1(x, Map<Object> y) {}
	        }
	    """

        shouldNotCompile """
	        public class G3731C {
	            Map<Object> m1(x, y) {null}
	        }
	    """
	}
}