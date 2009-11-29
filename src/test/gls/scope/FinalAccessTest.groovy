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

    void testStaticFinalField1() {
        shouldCompile """
            class G3911C1 {
              static final String foo;
              static {
                if (true) {
                  foo = "roshan";
                } else {
                  foo = "jochen";
                }
              }
            }
        """
    }

    void testStaticFinalField2() {
        shouldNotCompile """
            class G3911C2 {
              static final String foo;
              static foo() {
                if (true) {
                  foo = "roshan";
                } else {
                  foo = "jochen";
                }
              }
            }
        """
    }
}