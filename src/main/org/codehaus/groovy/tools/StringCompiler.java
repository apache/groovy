package org.codehaus.groovy.tools;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Category;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.ClassGenerator;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.classgen.Verifier;
import org.codehaus.groovy.syntax.lexer.InputStreamCharStream;
import org.codehaus.groovy.syntax.lexer.Lexer;
import org.codehaus.groovy.syntax.lexer.LexerTokenStream;
import org.codehaus.groovy.syntax.lexer.StringCharStream;
import org.codehaus.groovy.syntax.parser.ASTBuilder;
import org.codehaus.groovy.syntax.parser.CSTNode;
import org.codehaus.groovy.syntax.parser.Parser;
import org.codehaus.groovy.syntax.parser.SemanticVerifier;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.DumpClassVisitor;

public class StringCompiler
{
    private Compiler compiler;

    public StringCompiler()
    {
        this.compiler = new Compiler();
    }

    protected Compiler getCompiler()
    {
        return this.compiler;
    }

    public void setClasspath(String classpath)
        throws Exception
    {
        getCompiler().setClasspath( classpath );
    }

    public GroovyClass[] compile(String source)
        throws Exception
    {
        StringCharStream charStream = new StringCharStream( source );

        return getCompiler().compile( charStream );
    }
}
