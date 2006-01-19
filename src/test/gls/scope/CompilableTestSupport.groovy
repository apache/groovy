package gls.scope;

import org.codehaus.groovy.control.CompilationFailedException;
import groovy.util.GroovyTestCase;

public class CompilableTestSupport extends GroovyTestCase {
	protected void shouldNotCompile(String script) {
	  try {
        GroovyShell shell = new GroovyShell();
        shell.evaluate(script, getTestClassName());
      } catch (CompilationFailedException cfe) {
        assert true
        return
      }
      assert false
	}
}