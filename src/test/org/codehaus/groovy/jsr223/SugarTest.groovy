package org.codehaus.groovy.jsr223

import javax.script.ScriptEngineManager

class SugarTest extends GroovyTestCase {
    void testEvalMethodsWithBindingAndMissingPropertyLanguageSelection() {
        def sem = new ScriptEngineManager()
        def binding = new Binding()
        binding.x = 10
        binding.y = 5

        assert sem.getEngineByName("groovy").eval('2 * x + y', binding) == 25
        sem.getEngineByName("groovy").eval('z = x + y', binding)
        assert binding.z == 15
    }
}