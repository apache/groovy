package org.codehaus.groovy.antlr.treewalker;

import junit.framework.TestCase;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;

/**
 * Testcases for the antlr AST visitor that prints groovy source code nodes as Freemind compatible XML.
 */
public class MindMapPrinterTest extends TestCase {

    private static final String HEADER = "<map version='0.7.1'><node TEXT='AST'>";
    private static final String FOOTER = "</node></map>";

    protected void setUp() throws Exception {
        XMLUnit.setIgnoreWhitespace(true);
    }

    public void testAbstract() throws Exception {
        assertXmlEquals(
                HEADER +
                        "<node TEXT='MODIFIERS &lt;5&gt;' POSITION='right' COLOR=\"#000000\">" +
                        "  <node TEXT='public  &lt;112&gt;' POSITION='right' COLOR=\"#17178B\"></node>" +
                        "</node>" +
                        "<node TEXT='abstract  &lt;38&gt;' POSITION='right' COLOR=\"#006699\"></node>" +
                        "<node TEXT='CLASS_DEF &lt;13&gt;' POSITION='right' COLOR=\"#17178B\"></node>" +
                        "<node TEXT='an identifier &lt;84&gt; : Foo' POSITION='right' COLOR=\"#006699\"></node>" +
                        "<node TEXT='EXTENDS_CLAUSE &lt;17&gt;' POSITION='right' COLOR=\"#17178B\"></node>" +
                        "<node TEXT='IMPLEMENTS_CLAUSE &lt;18&gt;' POSITION='right' COLOR=\"#17178B\"></node>" +
                        "<node TEXT='OBJBLOCK &lt;6&gt;' POSITION='right' COLOR=\"#006699\"></node>" +
                        FOOTER,
                pretty("public abstract class Foo{}"));

    }

    public void testArrayDeclarator() throws Exception {
        assertXmlEquals(
                HEADER +
                        "<node TEXT='VARIABLE_DEF &lt;9&gt; : primes' POSITION='right' COLOR=\"#000000\">" +
                        "  <node TEXT='TYPE &lt;12&gt;' POSITION='right' COLOR=\"#17178B\">" +
                        "    <node TEXT='ARRAY_DECLARATOR &lt;16&gt; : [' POSITION='right' COLOR=\"#000000\">" +
                        "      <node TEXT='int  &lt;104&gt;' POSITION='right' COLOR=\"#17178B\"></node>" +
                        "    </node>" +
                        "  </node>" +
                        "  <node TEXT='an identifier &lt;84&gt; : primes' POSITION='right' COLOR=\"#006699\"></node>" +
                        "  <node TEXT='=  &lt;120&gt;' POSITION='right' COLOR=\"#000000\">" +
                        "    <node TEXT='new  &lt;192&gt;' POSITION='right' COLOR=\"#17178B\">" +
                        "      <node TEXT='int  &lt;104&gt;' POSITION='right' COLOR=\"#17178B\"></node>" +
                        "      <node TEXT='ARRAY_DECLARATOR &lt;16&gt; : [' POSITION='right' COLOR=\"#000000\">" +
                        "        <node TEXT='a numeric literal &lt;196&gt; : 5' POSITION='right' COLOR=\"#006699\"></node>" +
                        "      </node>" +
                        "    </node>" +
                        "  </node>" +
                        "</node>" +
                        FOOTER,
                pretty("int[] primes = new int[5]"));
    }

    public void testRegexMatch() throws Exception {
        assertXmlEquals(
                HEADER +
                        "<node TEXT='if  &lt;134&gt;' POSITION='right' COLOR=\"#17178B\">" +
                        "  <node TEXT='an identifier &lt;84&gt; : foo' POSITION='right' COLOR=\"#006699\"></node>" +
                        "  <node TEXT='==~  &lt;172&gt;' POSITION='right' COLOR=\"#000000\"></node>" +
                        "  <node TEXT='a string literal &lt;130&gt; : bar' POSITION='right' COLOR=\"#008000\"></node>" +
                        "</node>" +
                        "<node TEXT='SLIST &lt;7&gt; : {' POSITION='right' COLOR=\"#006699\"></node>" +
                        FOOTER,
                pretty("if (foo==~\"bar\"){}"));
    }

    private void assertXmlEquals(String expected, String actual) throws Exception {
        Diff diff = new Diff(expected, actual);
        assertTrue(diff.toString(), diff.similar());
    }

    private String pretty(String input) throws Exception {
        TraversalTestHelper traverser = new TraversalTestHelper();
        return traverser.traverse(input, MindMapPrinter.class);
    }

}
