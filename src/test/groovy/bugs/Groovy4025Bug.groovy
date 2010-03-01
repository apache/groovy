package groovy.bugs

import gls.CompilableTestSupport

class Groovy4025Bug extends CompilableTestSupport {
    public void testAMethodWithBodyInAnInterface() {
        shouldNotCompile """
            interface ITest {
                def foo(a, b) {
                    return a + b
                }
            }
        """
    }    
    public void testAbstractMethodInAClass() {
        shouldNotCompile """
            abstract class Test {
                abstract foo(a, b) {
                    return a + b
                }
            }
        """
    }    
}
