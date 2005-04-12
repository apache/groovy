package groovy.lang;

import org.codehaus.groovy.classgen.TestSupport;
import org.codehaus.groovy.control.CompilationFailedException;


/**
 * @author Steve Goetze
 */
public class ScriptIntegerDivideTest extends TestSupport {

	/**
	 * If a script that invokes integer division is specified as a literal string, the
	 * division token "\" needs to be escaped. 
	 */
	public void testIntegerDivide() throws Exception {
   		assertScript( "assert 4\\3 == 1" );
    }

	/**
	 * If a groovy script is spec'd in a literal string, any intended Integer
	 * divisions need to be escaped.  In cases where the integer division token "\"
	 * is surrounded by whitespace the literal cannot be constructed, which is OK.
	 * In the case where the second operand immediately follows the token, the literal
	 * can be specified, but will result in a compilation error as the token immediately
	 * following the backslash will be taken as the escape sequence.
	 */
	public void testIntegerDivideWithBadEscape() throws Exception {
    	try {
    		assertScript( "assert 4\3 == 1" );
    	} catch (CompilationFailedException e) {
    		return;
    	}
    	fail("Should catch a CompilationFailedException");
    }
}
