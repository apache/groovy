package org.codehaus.groovy.jsr223

import javax.script.ScriptEngineManager

class SugarTest extends GroovyTestCase {
    void testEvalMethodsWithBindingAndMissingPropertyLanguageSelection() {
        def binding = new Binding()
        binding.x = 10
        binding.y = 5
        assert ScriptEngineManager.groovy.eval('2 * x + y', binding) == 25
        ScriptEngineManager.groovy.eval('z = x + y', binding)
        assert binding.z == 15
    }
}