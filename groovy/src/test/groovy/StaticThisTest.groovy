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

    void testThisPropertyInStaticMethodShouldNotCompile() {
        shouldNotCompile """
            class A {
                def prop
                static method(){
                    this.prop
                }
            }
            """
    }

    void testSuperPropertyInStaticMethodShouldNotCompile() {
        shouldNotCompile """
            class A { def prop }
            class B extends A {
                static method(){
                    super.prop
                }
            }
            """
    }

}
