package groovy.util;

import junit.framework.TestCase;
import org.codehaus.groovy.control.CompilationFailedException;

/**
 * Testing the simple Groovy integration with Eval.
 *
 * @author Dierk Koenig
 */
public class EvalTest extends TestCase {
    public void testMeSimple() throws CompilationFailedException {
        Object result = Eval.me("10");
        assertEquals("10", result.toString());
    }

    public void testMeWithSymbolAndObject() throws CompilationFailedException {
        Object result = Eval.me("x", new Integer(10), "x");
        assertEquals("10", result.toString());
    }

    public void testX() throws CompilationFailedException {
        Object result = Eval.x(new Integer(10), "x");
        assertEquals("10", result.toString());
    }

    public void testXY() throws CompilationFailedException {
        Integer ten = new Integer(10);
        Object result = Eval.xy(ten, ten, "x+y");
        assertEquals("20", result.toString());
    }

    public void testXYZ() throws CompilationFailedException {
        Integer ten = new Integer(10);
        Object result = Eval.xyz(ten, ten, ten, "x+y+z");
        assertEquals("30", result.toString());
    }
}
