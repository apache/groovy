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
        // erroneous
        shouldNotCompile("public private class X {}")
        shouldNotCompile("synchronized class X {}")
    }

    public void testMethodsShouldOnlyHaveOneVisibility() {
        // control
        shouldCompile("class X { private method() {} }")
        // erroneous
        shouldNotCompile("class X { private public method() {} }")
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
        shouldCompile("protected name")
        // erroneous
        shouldNotCompile("private protected name")
    }

}
