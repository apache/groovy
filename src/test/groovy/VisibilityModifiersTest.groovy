package groovy

import gls.CompilableTestSupport

class VisibilityModifiersTest extends CompilableTestSupport {

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

    public void testMethod() {
        // control
        shouldCompile("class X { private method() {} }")
        // erroneous
        shouldNotCompile("class X { private public method() {} }")
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
