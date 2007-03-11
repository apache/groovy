package org.codehaus.groovy.antlr.treewalker;

import junit.framework.TestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.Diff;
import org.codehaus.groovy.antlr.parser.GroovyRecognizer;
import org.codehaus.groovy.antlr.parser.GroovyLexer;
import org.codehaus.groovy.antlr.SourceBuffer;
import org.codehaus.groovy.antlr.UnicodeEscapingReader;
import org.codehaus.groovy.antlr.AntlrASTProcessor;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.PrintStream;
import java.util.List;
import java.util.ArrayList;

import antlr.collections.AST;

/**
 * Testcases for the composite visitor.
 */
public class CompositeVisitorTest extends TestCase {

    protected void setUp() throws Exception {
        XMLUnit.setIgnoreWhitespace(true);
    }

    public void testCompositeTransparency() throws Exception {
        // TODO: add more tests in here
        assertCompositeTransparency("public abstract class Foo{}");
        assertCompositeTransparency("int[] primes = new int[5]");
        assertCompositeTransparency("if (foo==~\"bar\"){}");
        assertCompositeTransparency("a=12");
        assertCompositeTransparency("def x=1&2");
        assertCompositeTransparency("x&=2");
        assertCompositeTransparency("def z = ~123");
        assertCompositeTransparency("def y = 1 | 2");
    }

    private void assertCompositeTransparency(String input) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GroovyRecognizer parser;
        SourceBuffer sourceBuffer = new SourceBuffer();
        UnicodeEscapingReader unicodeReader = new UnicodeEscapingReader(new StringReader(input),sourceBuffer);
        GroovyLexer lexer = new GroovyLexer(unicodeReader);
        unicodeReader.setLexer(lexer);
        parser = GroovyRecognizer.make(lexer);
        parser.setSourceBuffer(sourceBuffer);
        String[] tokenNames = parser.getTokenNames();
        parser.compilationUnit();
        AST ast = parser.getAST();

        // determine direct result
        Visitor directVisitor = new SourcePrinter(new PrintStream(baos), tokenNames, false);
        AntlrASTProcessor traverser = new SourceCodeTraversal(directVisitor);
        traverser.process(ast);
        String directResult = new String(baos.toByteArray());

        // determine composite result
        baos.reset();
        List wrappedVisitors = new ArrayList();
        wrappedVisitors.add(directVisitor);
        Visitor compositeVisitor = new CompositeVisitor(wrappedVisitors);
        traverser = new SourceCodeTraversal(compositeVisitor);
        traverser.process(ast);
        String compositeResult = new String(baos.toByteArray());

        assertEquals(directResult, compositeResult);
    }

}
