package org.codehaus.groovy.jsr223.vm6

import javax.script.ScriptEngineManager

class JavascriptTest extends GroovyTestCase {
    void testEvalMethodsWithBindingAndMissingPropertyLanguageSelection() {
        def binding = new Binding()
        binding.x = 10
        binding.y = 5
        def js = ScriptEngineManager.javascript.&eval.rcurry(binding)
        assert js('2 * x + y') == 25
        js 'z = x + y'
        assert binding.z == 15
    }
}