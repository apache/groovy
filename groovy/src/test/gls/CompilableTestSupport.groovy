package gls

import org.codehaus.groovy.control.CompilationFailedException;
import groovy.util.GroovyTestCase;

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
}