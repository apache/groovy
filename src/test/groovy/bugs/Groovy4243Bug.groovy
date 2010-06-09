package groovy.bugs

import org.codehaus.groovy.control.*

class Groovy4243Bug extends GroovyTestCase {
    void testScriptBaseClassWithAnonymousInnerClass() {
        def configuration = new CompilerConfiguration()
        configuration.scriptBaseClass = TestScript4243.name
        def classLoader = new GroovyClassLoader(getClass().classLoader, configuration)

        // This works
        def scriptClass = classLoader.parseClass('''
            def r = new TestRunnable()
            class TestRunnable implements Runnable {
                public void run() {}
            }
        ''')
        assert TestScript4243.isAssignableFrom(scriptClass)

        // This does not work
        scriptClass = classLoader.parseClass('''
            def r = new Runnable() {
                public void run() { }
            }
        ''')
        assert Script.isAssignableFrom(scriptClass)
        assert TestScript4243.isAssignableFrom(scriptClass) // <-- fails here    
    }
}

abstract class TestScript4243 extends Script { }