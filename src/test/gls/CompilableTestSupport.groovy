package gls

import org.codehaus.groovy.control.CompilationFailedException

public class CompilableTestSupport extends GroovyTestCase {
    protected void shouldNotCompile(String script) {
        try {
            GroovyClassLoader gcl = new GroovyClassLoader()
            gcl.parseClass(script, getTestClassName())
        } catch (CompilationFailedException cfe) {
            assert true
            return
        }
        fail("the compilation succeeded but should have failed")
    }

    protected void shouldCompile(String script) {
        GroovyClassLoader gcl = new GroovyClassLoader()
        gcl.parseClass(script, getTestClassName())
        assert true
    }

    protected void shouldFail(Class th = null, String script) {
        try {
            def shell = new GroovyShell()
            shell.evaluate(script, getTestClassName())
            return
        } catch (Throwable thrown) {
            if (th == null) return
            if (th != thrown.getClass()) {
                fail "script should have thrown $th, but it did throw ${thrown.getClass()}"
            }
            return
        }
        fail "script should have failed, but succeeded"
    }
}