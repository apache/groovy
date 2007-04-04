package org.codehaus.groovy.antlr.treewalker;

import junit.framework.TestCase;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;

/**
 * Testcases for the antlr AST visitor that prints groovy source code nodes as HTML.
 */
public class NodeAsHTMLPrinterTest extends TestCase {

    private static final String HEADER = "<html><head></head><body><pre>";
    private static final String FOOTER = "</pre></body></html>";

    protected void setUp() throws Exception {
        XMLUnit.setIgnoreWhitespace(true);
    }

    public void testAbstract() throws Exception {
        assertXmlEquals(
                HEADER +
                        "<code title='MODIFIERS'><font color='#000000'>" +
                        "  <code title='\"public\"'><font color='#17178B'></font></code>" +
                        "</font></code>" +
                        "<code title='\"abstract\"'><font color='#000000'></font></code>" +
                        "<code title='CLASS_DEF'><font color='#17178B'></font></code>" +
                        "<code title='an identifier'><font color='#000000'></font></code>" +
                        "<code title='EXTENDS_CLAUSE'><font color='#17178B'></font></code>" +
                        "<code title='IMPLEMENTS_CLAUSE'><font color='#17178B'></font></code>" +
                        "<code title='OBJBLOCK'><font color='#000000'></font></code>" +
                        FOOTER,
                pretty("public abstract class Foo{}"));

    }

    public void testArrayDeclarator() throws Exception {
        assertXmlEquals(
                HEADER +
                        "<code title='VARIABLE_DEF'><font color='#000000'>" +
                        "  <code title='TYPE'><font color='#17178B'>" +
                        "    <code title='ARRAY_DECLARATOR'><font color='#000000'>" +
                        "      <code title='\"int\"'><font color='#17178B'></font></code>" +
                        "    </font></code>" +
                        "  </font></code>" +
                        "  <code title='an identifier'><font color='#000000'></font></code>" +
                        "  <code title=\"'='\"><font color='#000000'>" +
                        "    <code title='\"new\"'><font color='#17178B'>" +
                        "      <code title='\"int\"'><font color='#17178B'></font></code>" +
                        "      <code title='ARRAY_DECLARATOR'><font color='#000000'>" +
                        "        <code title='a numeric literal'><font color='#000000'></font></code>" +
                        "      </font></code>" +
                        "    </font></code>" +
                        "  </font></code>" +
                        "</font></code>" +
                        FOOTER,
                pretty("int[] primes = new int[5]"));
    }

    public void testRegexMatch() throws Exception {
        assertXmlEquals(
                HEADER +
                        "<code title='\"if\"'><font color='#17178B'>" +
                        "  <code title='an identifier'><font color='#000000'></font></code>" +
                        "  <code title=\"'==~'\"><font color='#000000'></font></code>" +
                        "  <code title='a string literal'><font color='#008000'></font></code>" +
                        "</font></code>" +
                        "<code title='SLIST'><font color='#000000'></font></code>" +
                        FOOTER,
                pretty("if (foo==~\"bar\"){}"));
    }

    private void assertXmlEquals(String expected, String actual) throws Exception {
        Diff diff = new Diff(expected, actual);
        assertTrue(diff.toString(), diff.similar());
    }

    private String pretty(String input) throws Exception {
        TraversalTestHelper traverser = new TraversalTestHelper();
        return traverser.traverse(input, NodeAsHTMLPrinter.class);
    }

}
