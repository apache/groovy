package groovy.lang;

import org.codehaus.groovy.classgen.TestSupport;
import org.codehaus.groovy.control.CompilationFailedException;


/**
 * @author Steve Goetze
 * @author Jeremy Rayner
 */
public class ScriptIntegerDivideTest extends TestSupport {

	/**
	 * Check integer division which is now a method call rather than the symbol "\".
	 */
	public void testIntegerDivide() throws Exception {
   		assertScript( "assert 4.intdiv(3) == 1" );
    }
}
