package groovy.bugs

import gls.CompilableTestSupport

class Groovy3596Bug extends CompilableTestSupport {
    void testMapReferenceWithGenericsTypeParameters() {
    	shouldCompile """
    		interface TypeDescriptor3596 {}
    		interface MapDescriptor3596 extends Map<String, TypeDescriptor3596> {}
    	"""
    }
}
