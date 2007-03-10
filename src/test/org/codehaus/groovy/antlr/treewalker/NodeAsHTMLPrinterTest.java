package org.codehaus.groovy.antlr.treewalker;

import org.codehaus.groovy.antlr.parser.GroovyRecognizer;
import org.codehaus.groovy.antlr.parser.GroovyLexer;
import org.codehaus.groovy.antlr.SourceBuffer;
import org.codehaus.groovy.antlr.UnicodeEscapingReader;
import org.codehaus.groovy.antlr.AntlrASTProcessor;

import java.io.StringReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import antlr.collections.AST;
import org.custommonkey.xmlunit.*;

import junit.framework.TestCase;

/**
 * Testcases for the antlr AST visitor that prints groovy source code nodes as HTML.
 */
public class NodeAsHTMLPrinterTest extends TestCase {

    protected void setUp() throws Exception {
        XMLUnit.setIgnoreWhitespace(true);
    }

    public void testAbstract() throws Exception {
        assertXmlEquals(
                "<html>" +
                        "  <head></head>" +
                        "  <body><pre>" +
                        "    <code title='MODIFIERS'><font color='#000000'>" +
                        "      <code title='\"public\"'><font color='#17178B'></font></code>" +
                        "    </font></code>" +
                        "    <code title='\"abstract\"'><font color='#000000'></font></code>" +
                        "    <code title='CLASS_DEF'><font color='#17178B'></font></code>" +
                        "    <code title='an identifier'><font color='#000000'></font></code>" +
                        "    <code title='EXTENDS_CLAUSE'><font color='#17178B'></font></code>" +
                        "    <code title='IMPLEMENTS_CLAUSE'><font color='#17178B'></font></code>" +
                        "    <code title='OBJBLOCK'><font color='#000000'></font></code>" +
                        "  </pre></body>" +
                        "</html>",
                pretty("public abstract class Foo{}"));

    }

    public void testArrayDeclarator() throws Exception {
        assertXmlEquals(
                "<html>" +
                        "  <head></head>" +
                        "  <body><pre>" +
                        "    <code title='VARIABLE_DEF'><font color='#000000'>" +
                        "      <code title='TYPE'><font color='#17178B'>" +
                        "        <code title='ARRAY_DECLARATOR'><font color='#000000'>" +
                        "          <code title='\"int\"'><font color='#17178B'></font></code>" +
                        "        </font></code>" +
                        "      </font></code>" +
                        "      <code title='an identifier'><font color='#000000'></font></code>" +
                        "      <code title=\"'='\"><font color='#000000'>" +
                        "        <code title='\"new\"'><font color='#17178B'>" +
                        "          <code title='\"int\"'><font color='#17178B'></font></code>" +
                        "        </font></code>" +
                        "        <code title='ARRAY_DECLARATOR'><font color='#000000'>" +
                        "          <code title='a numeric literal'><font color='#000000'></font></code>" +
                        "        </font></code>" +
                        "      </font></code>" +
                        "    </font></code>" +
                        "  </pre></body>" +
                        "</html>",
                pretty("int[] primes = new int[5]"));
    }

    public void testRegexMatch() throws Exception {
        assertXmlEquals(
                "<html>" +
                        "  <head></head>" +
                        "  <body><pre>" +
                        "    <code title='\"if\"'><font color='#17178B'>" +
                        "      <code title='an identifier'><font color='#000000'></font></code>" +
                        "      <code title=\"'==~'\"><font color='#000000'></font></code>" +
                        "      <code title='a string literal'><font color='#008000'></font></code>" +
                        "    </font></code>" +
                        "    <code title='SLIST'><font color='#000000'></font></code>" +
                        "  </pre></body>" +
                        "</html>",
                pretty("if (foo==~\"bar\"){}"));
    }

    private void assertXmlEquals(String expected, String actual) throws Exception {
        Diff diff = new Diff(expected, actual);
        assert diff.similar();
    }

    private String pretty(String input) throws Exception {
        GroovyRecognizer parser;
        SourceBuffer sourceBuffer = new SourceBuffer();
        UnicodeEscapingReader unicodeReader = new UnicodeEscapingReader(new StringReader(input), sourceBuffer);
        GroovyLexer lexer = new GroovyLexer(unicodeReader);
        unicodeReader.setLexer(lexer);
        parser = GroovyRecognizer.make(lexer);
        parser.setSourceBuffer(sourceBuffer);

        String[] tokenNames;
        tokenNames = parser.getTokenNames();
        parser.compilationUnit();
        AST ast = parser.getAST();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Visitor visitor = new NodeAsHTMLPrinter(new PrintStream(baos), tokenNames);
        AntlrASTProcessor traverser = new SourceCodeTraversal(visitor);

        traverser.process(ast);

        return new String(baos.toByteArray());
    }

}
