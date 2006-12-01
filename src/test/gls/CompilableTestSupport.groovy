package gls

import org.codehaus.groovy.control.CompilationFailedException;
import groovy.util.GroovyTestCase;

public class CompilableTestSupport extends GroovyTestCase {
	protected void shouldNotCompile(String script) {
	  try {
        GroovyShell shell = new GroovyShell()
        shell.parse(script, getTestClassName())
      } catch (CompilationFailedException cfe) {
        assert true
        return
      }
      fail("the compilation succeeded but should have failed")
	}
	
	protected void shouldCompile(String script) {
      GroovyShell shell = new GroovyShell()
      shell.parse(script, getTestClassName())
      assert true
	}
}