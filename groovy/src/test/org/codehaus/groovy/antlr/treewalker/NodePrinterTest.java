package org.codehaus.groovy.antlr.treewalker;

import junit.framework.TestCase;
import org.custommonkey.xmlunit.XMLUnit;

/**
 * Testcases for the antlr AST visitor that prints groovy source code nodes as Pseudo XML.
 */
public class NodePrinterTest extends TestCase {

    protected void setUp() throws Exception {
        XMLUnit.setIgnoreWhitespace(true);
    }

    public void testAbstract() throws Exception {
        assertPseudoXmlEquals("<MODIFIERS><\"public\"></\"public\"></MODIFIERS><\"abstract\"></\"abstract\"><CLASS_DEF></CLASS_DEF><an identifier></an identifier><EXTENDS_CLAUSE></EXTENDS_CLAUSE><IMPLEMENTS_CLAUSE></IMPLEMENTS_CLAUSE><OBJBLOCK></OBJBLOCK>",
                nodify("public abstract class Foo{}"));
    }

    public void testArrayDeclarator() throws Exception {
        assertPseudoXmlEquals(
                "<VARIABLE_DEF><TYPE><ARRAY_DECLARATOR><\"int\"></\"int\"></ARRAY_DECLARATOR></TYPE><an identifier></an identifier><'='><\"new\"><\"int\"></\"int\"></\"new\"><ARRAY_DECLARATOR><a numeric literal></a numeric literal></ARRAY_DECLARATOR></\"new\"></'='></VARIABLE_DEF>",
                nodify("int[] primes = new int[5]"));
    }

    public void testRegexMatch() throws Exception {
        assertPseudoXmlEquals(
                "<\"if\"><an identifier></an identifier><'==~'></'==~'><a string literal></a string literal></\"if\"><SLIST></SLIST>",
                nodify("if (foo==~\"bar\"){}"));
    }

    private void assertPseudoXmlEquals(String expected, String actual) throws Exception {
        assertEquals(expected, actual);
    }

    private String nodify(String input) throws Exception {
        TraversalTestHelper traverser = new TraversalTestHelper();
        return traverser.traverse(input, NodePrinter.class);
    }

}
