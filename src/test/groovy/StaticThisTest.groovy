package groovy

import gls.CompilableTestSupport

class StaticThisTest extends CompilableTestSupport {

    void testThisFail() {
        staticMethod()
    }

    static def staticMethod() {
        def foo = this
        assert foo != null
        assert foo.name.endsWith("StaticThisTest")

        def s = super
        assert s != null
        assert s.name.endsWith("CompilableTestSupport")
    }

    void testThisMethodInStaticMethodShouldNotCompile() {
        shouldNotCompile """
            class A {
                static method(){
                    this.toString()
                }
            }
            """
    }

    void testSuperMethodInStaticMethodShouldNotCompile() {
        shouldNotCompile """
            class A {
                static method(){
                    super.toString()
                }
            }
            """
    }

}
