package org.codehaus.groovy.antlr;

import groovy.util.GroovyTestCase;
import org.codehaus.groovy.antlr.parser.GroovyLexer;
import org.codehaus.groovy.antlr.parser.GroovyRecognizer;

import java.io.Reader;
import java.io.StringReader;


/**
 * Parser tests for Enum definitions.
 *
 * @author <a href='mailto:the[dot]mindstorm[at]gmail[dot]com'>Alex Popescu</a>
 */
public class EnumSourceParsingTest extends GroovyTestCase {
    public void testParseEnumConstants() {
        StringReader reader = new StringReader(
                "enum One {\n"
                        + "  ONE, TWO, THREE\n"
                        + "}");
        parse(reader);
    }

    public void testParseEnumConstantsOneLiner() {
        StringReader reader = new StringReader(
                "enum One { ONE, TWO, THREE }");
        parse(reader);
    }

    public void testParseEnumImplements() {
        StringReader reader = new StringReader(
                "enum Two implements I1 {\n"
                        + "ONE, TWO, THREE\n"
                        + "}");
        parse(reader);
    }

    public void testParseEnumWithValues() {
        StringReader reader = new StringReader(
                "enum Three1 {\n"
                        + "    ONE(1), TWO(2)\n\n"
                        + "    Three1(val) {\n"
                        + "        value = val\n"
                        + "    }\n\n"
                        + "    private final int value"
                        + "}");
        parse(reader);

        reader = new StringReader(
                "enum Three1 {\n"
                        + "    @Annotation ONE(1), TWO(2)\n\n"
                        + "    Three1(val) {\n"
                        + "        value = val\n"
                        + "    }\n\n"
                        + "    private final int value"
                        + "}");
    }

    public void testParseEnumWithMethodDefinitions() {
        StringReader reader = new StringReader(
                "enum Four {\n"
                        + "    ONE, TWO, THREE\n\n"
                        + "    def someMethod() { }\n"
                        + "    public m2(args) { }\n"
                        + "    int m3(String arg) { }\n"
                        + "}");
        parse(reader);
    }

    public void testParseCompleteEnum() {
        StringReader reader = new StringReader(
                "enum Five {\n"
                        + "    ONE { double eval(int v) { return (double) v } }, \n"
                        + "    TWO {\n"
                        + "        double eval(int v) { return (double) v + 1 }\n"
                        + "    }, THREE\n"
                        + "}");
        parse(reader);
    }

    private void parse(Reader reader) {
        SourceBuffer sourceBuffer = new SourceBuffer();
        UnicodeEscapingReader unicodeReader = new UnicodeEscapingReader(reader, sourceBuffer);
        GroovyLexer lexer = new GroovyLexer(unicodeReader);
        unicodeReader.setLexer(lexer);
        GroovyRecognizer parser = GroovyRecognizer.make(lexer);
        parser.setSourceBuffer(sourceBuffer);
        parser.setFilename("EnumTestScript");

        // start parsing at the compilationUnit rule
        try {
            parser.compilationUnit();
        }
        catch (Exception ex) {
            fail(ex.getMessage());
        }
    }
}
