package org.codehaus.groovy.runtime;

import java.util.HashMap;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.Script;
import junit.framework.TestCase;

public class InvokerHelperTest extends TestCase {
    private HashMap bindingVariables;

    protected void setUp() throws Exception {
        bindingVariables = new HashMap();
        bindingVariables.put("name", "hans");
    }

    public void testCreateScriptWithNullClass() {
        Script script = InvokerHelper.createScript(null, new Binding(bindingVariables));
        assertEquals(bindingVariables, script.getBinding().getVariables());
    }

    public void testCreateScriptWithScriptClass() {
        GroovyClassLoader classLoader = new GroovyClassLoader();
        String controlProperty = "text";
        String controlValue = "I am a script";
        String code = controlProperty + " = '" + controlValue + "'";
        GroovyCodeSource codeSource = new GroovyCodeSource(code, "testscript", "/groovy/shell");
        Class scriptClass = classLoader.parseClass(codeSource, false);
        Script script = InvokerHelper.createScript(scriptClass, new Binding(bindingVariables));
        assertEquals(bindingVariables, script.getBinding().getVariables());
        script.run();
        assertEquals(controlValue, script.getProperty(controlProperty));
    }
}
