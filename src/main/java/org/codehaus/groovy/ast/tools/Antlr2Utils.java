package org.codehaus.groovy.ast.tools;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import groovy.lang.GroovyRuntimeException;
import org.codehaus.groovy.antlr.AntlrParserPlugin;
import org.codehaus.groovy.antlr.parser.GroovyLexer;
import org.codehaus.groovy.antlr.parser.GroovyRecognizer;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.ParserException;
import org.codehaus.groovy.syntax.Reduction;

import java.io.StringReader;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Utilities for working with the Antlr2 parser
 *
 * @deprecated will be removed when antlr2 parser is removed
 */
@Deprecated
public class Antlr2Utils {
    private Antlr2Utils() {
    }

    public static ClassNode parse(String option) {
        GroovyLexer lexer = new GroovyLexer(new StringReader("DummyNode<" + option + ">"));
        try {
            final GroovyRecognizer rn = GroovyRecognizer.make(lexer);
            rn.classOrInterfaceType(true);
            final AtomicReference<ClassNode> ref = new AtomicReference<ClassNode>();
            AntlrParserPlugin plugin = new AntlrParserPlugin() {
                @Override
                public ModuleNode buildAST(final SourceUnit sourceUnit, final ClassLoader classLoader, final Reduction cst) throws ParserException {
                    ref.set(makeTypeWithArguments(rn.getAST()));
                    return null;
                }
            };
            plugin.buildAST(null, null, null);
            return ref.get();
        } catch (RecognitionException | TokenStreamException | ParserException e) {
            throw new GroovyRuntimeException("Unable to parse '" + option + "'", e);
        }
    }
}
