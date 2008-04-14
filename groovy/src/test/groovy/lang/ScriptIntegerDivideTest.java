package groovy.lang;

import org.codehaus.groovy.classgen.TestSupport;


/**
 * @author Steve Goetze
 * @author Jeremy Rayner
 */
public class ScriptIntegerDivideTest extends TestSupport {

    /**
     * Check integer division which is now a method call rather than the symbol "\".
     */
    public void testIntegerDivide() throws Exception {
        assertScript("assert 4.intdiv(3) == 1");
    }
}
