package gls.scope

import gls.CompilableTestSupport

class FinalAccessTest extends CompilableTestSupport {

    void testFinalField() {
        shouldNotCompile """
            class Person {
                final String name = "scott"
                def foo() {
                    name = "Dierk"
                }
            }
        """
    }
}