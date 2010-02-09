package groovy.bugs

import gls.CompilableTestSupport

class Groovy4043Bug extends CompilableTestSupport {
    void testResolveInnerClsDefByAParent() {
        shouldCompile """
            class A4043 {
                static class B4043 {}
            }
            
            class C4043 extends A4043 {
                B4043 b
            }
        """        
    }
}