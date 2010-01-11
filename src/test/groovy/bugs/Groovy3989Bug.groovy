package groovy.bugs

import gls.CompilableTestSupport

class Groovy3989Bug extends CompilableTestSupport {
    void testOverridingFinalMethods() {
        shouldNotCompile """
            class A {
                def foo() {}
                final def bar() {}
            }
            class B extends A {
                def foo() {}
                def bar() {}
            }
            B
        """
    }
}
