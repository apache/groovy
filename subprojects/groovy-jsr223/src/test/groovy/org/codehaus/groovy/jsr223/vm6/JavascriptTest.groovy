package org.codehaus.groovy.jsr223.vm6

import javax.script.ScriptEngineManager

class JavascriptTest extends GroovyTestCase {
    void testIntegrationWithBuiltinJavaScript() {
        def binding = new Binding()
        binding.x = 10
        binding.y = 5
        def js = ScriptEngineManager.javascript
        if (!js) System.err.println("Warning: JavaScript not available on this JVM - test ignored")
        else {
            def eval = js.&eval.rcurry(binding)
            assert eval('2 * x + y') == 25
            eval 'z = x + y'
            assert binding.z == 15
        }
    }
}