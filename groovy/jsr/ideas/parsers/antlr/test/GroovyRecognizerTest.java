import junit.framework.*;

import java.io.Reader;
import java.io.StringReader;

public class GroovyRecognizerTest extends TestCase {
    private GroovyRecognizer g;

    public GroovyRecognizerTest(String name) {
        super(name);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(GroovyRecognizerTest.class);
    }

    protected void setUp() {
        Reader r = new StringReader("public class Foo {}");
        GroovyLexer lexer = new GroovyLexer(r);
        g = new GroovyRecognizer(lexer);
    }

    public void testInstatiation() {
        assertNotNull("groovy recognizer should exist",g);
    }

    public void testParse() {
	try {
	    g.compilationUnit();
	} catch (Exception e) {
        fail(e.getMessage());
	}
    }

    protected void tearDown() {
        g = null;
    }
}
