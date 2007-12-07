package org.codehaus.groovy.antlr;

import groovy.util.GroovyTestCase;

import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;

import org.codehaus.groovy.antlr.parser.GroovyLexer;
import org.codehaus.groovy.antlr.parser.GroovyRecognizer;

public class SourceParserTest extends GroovyTestCase {
    protected void parse(String name, Reader reader) {
        SourceBuffer sourceBuffer = new SourceBuffer();
        UnicodeEscapingReader unicodeReader = new UnicodeEscapingReader(reader, sourceBuffer);
        GroovyLexer lexer = new GroovyLexer(unicodeReader);
        unicodeReader.setLexer(lexer);
        GroovyRecognizer parser = GroovyRecognizer.make(lexer);
        parser.setSourceBuffer(sourceBuffer);
        parser.setFilename(name);

        // start parsing at the compilationUnit rule
        try {
            parser.compilationUnit();
        }
        catch (Exception ex) {
            StringWriter out = new StringWriter();
            out.write(ex.getMessage());
            out.write("\n");
            ex.printStackTrace(new PrintWriter(out));
            fail(out.toString());
        }
    }
}
