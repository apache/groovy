package groovy

import gls.CompilableTestSupport

class ModifiersTest extends CompilableTestSupport {

    public void testInterface() {
        // control
        shouldCompile("interface X {}")
        // erroneous
        shouldNotCompile("synchronized interface X {}")
    }

    public void testClass() {
        // control
        shouldCompile("public class X {}")
        shouldCompile """
            class X {
                private class Y {}
            }
        """
        // erroneous
        shouldNotCompile("public private class X {}")
        shouldNotCompile("synchronized class X {}")
        shouldNotCompile("private class X {}")
    }

    public void testMethodsShouldOnlyHaveOneVisibility() {
        // control
        shouldCompile("class X { private method() {} }")
        // erroneous
        shouldNotCompile("class X { private public method() {} }")
    }

    public void testFinalMethodParametersShouldNotBeModified() {
        // control
        shouldCompile("class X { private method(x) { x = 1 } }")
        // erroneous
        shouldNotCompile("class X { private method(final x) { x = 1 } }")
    }

    public void testMethodsShouldNotBeVolatile() {
        // control
        shouldCompile("class X { def method() {} }")
        // erroneous
        shouldNotCompile("class X { volatile method() {} }")
    }

    public void testInterfaceMethodsShouldNotBeSynchronizedNativeStrictfp() {
        // control
        shouldCompile("interface X { def method() }")
        // erroneous
        shouldNotCompile("interface X { native method() }")
        shouldNotCompile("interface X { synchronized method() }")
        shouldNotCompile("interface X { strictfp method() }")
    }

    public void testVariableInClass() {
        // control
        shouldCompile("class X { protected name }")
        // erroneous
        shouldNotCompile("class X { protected private name }")
    }

    public void testVariableInScript() {
        // control
        shouldCompile("def name")
        shouldCompile("String name")
        // erroneous
        shouldNotCompile("abstract name")
        shouldNotCompile("native name")
        shouldNotCompile("private name")
        shouldNotCompile("protected name")
        shouldNotCompile("public name")
        shouldNotCompile("static name")
        shouldNotCompile("strictfp name")
        shouldNotCompile("synchronized name")
        shouldNotCompile("transient name")
        shouldNotCompile("volatile name")
        shouldNotCompile("private protected name")
    }

    public void testInvalidModifiersOnConstructor() {
        // control
        shouldCompile("class Foo { Foo() {}}")
        // erroneous
        shouldNotCompile("class Foo { static Foo() {}}")
        shouldNotCompile("class Foo { final Foo() {}}")
        shouldNotCompile("class Foo { abstract Foo() {}}")
        shouldNotCompile("class Foo { native Foo() {}}")
    }

}
