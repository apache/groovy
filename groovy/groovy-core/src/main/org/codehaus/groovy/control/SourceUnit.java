/*
 $Id$

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */

package org.codehaus.groovy.control;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.List;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.io.FileReaderSource;
import org.codehaus.groovy.control.io.ReaderSource;
import org.codehaus.groovy.control.io.StringReaderSource;
import org.codehaus.groovy.control.io.URLReaderSource;
import org.codehaus.groovy.control.messages.ExceptionMessage;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.control.messages.SimpleMessage;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.*;
import org.codehaus.groovy.tools.Utilities;

import antlr.MismatchedTokenException;
import antlr.NoViableAltException;

import com.thoughtworks.xstream.XStream;


/**
 * Provides an anchor for a single source unit (usually a script file)
 * as it passes through the compiler system.
 *
 * @author <a href="mailto:cpoirier@dreaming.org">Chris Poirier</a>
 * @author <a href="mailto:b55r@sina.com">Bing Ran</a>
 * @version $Id$
 */

public class SourceUnit extends ProcessingUnit {

    /**
     * The pluggable parser used to generate the AST - we allow pluggability currently as we need to have Classic and JSR support
     */
    private ParserPlugin parserPlugin;

    /**
     * Where we can get Readers for our source unit
     */
    protected ReaderSource source;
    /**
     * A descriptive name of the source unit
     */
    protected String name;
    /**
     * A Concrete Syntax Tree of the source
     */
    protected Reduction cst;

    /**
     * A facade over the CST
     */
    protected SourceSummary sourceSummary;
    /**
     * The root of the Abstract Syntax Tree for the source
     */
    protected ModuleNode ast;


    /**
     * Initializes the SourceUnit from existing machinery.
     */
    public SourceUnit(String name, ReaderSource source, CompilerConfiguration flags, ClassLoader loader, ErrorCollector er) {
        super(flags, loader, er);

        this.name = name;
        this.source = source;
    }


    /**
     * Initializes the SourceUnit from the specified file.
     */
    public SourceUnit(File source, CompilerConfiguration configuration, ClassLoader loader, ErrorCollector er) {
        this(source.getPath(), new FileReaderSource(source, configuration), configuration, loader, er);
    }


    /**
     * Initializes the SourceUnit from the specified URL.
     */
    public SourceUnit(URL source, CompilerConfiguration configuration, ClassLoader loader, ErrorCollector er) {
        this(source.getPath(), new URLReaderSource(source, configuration), configuration, loader, er);
    }


    /**
     * Initializes the SourceUnit for a string of source.
     */
    public SourceUnit(String name, String source, CompilerConfiguration configuration, ClassLoader loader, ErrorCollector er) {
        this(name, new StringReaderSource(source, configuration), configuration, loader, er);
    }


    /**
     * Returns the name for the SourceUnit.
     */
    public String getName() {
        return name;
    }


    /**
     * Returns the Concrete Syntax Tree produced during parse()ing.
     */
    public Reduction getCST() {
        return this.cst;
    }

    /**
     * Returns the Source Summary
     */
    public SourceSummary getSourceSummary() {
        return this.sourceSummary;
    }
    /**
     * Returns the Abstract Syntax Tree produced during parse()ing
     * and expanded during later phases.
     */
    public ModuleNode getAST() {
        return this.ast;
    }


    /**
     * Convenience routine, primarily for use by the InteractiveShell,
     * that returns true if parse() failed with an unexpected EOF.
     */
    public boolean failedWithUnexpectedEOF() {
        boolean result = false;
        if (getErrorCollector().hasErrors()) {
            // Classic support
            Message last = (Message) getErrorCollector().getLastError();
            if (last instanceof SyntaxErrorMessage) {
                SyntaxException cause = ((SyntaxErrorMessage) last).getCause();
                if (cause instanceof UnexpectedTokenException) {
                    Token unexpected = ((UnexpectedTokenException) cause).getUnexpectedToken();
                    if (unexpected.isA(Types.EOF)) {
                        result = true;
                    }
                }
            }
            // JSR support
            if (last instanceof ExceptionMessage) {
                ExceptionMessage exceptionMessage = (ExceptionMessage) last;
                Exception cause = exceptionMessage.getCause();
                if (cause instanceof NoViableAltException) {
                    NoViableAltException antlrException = (NoViableAltException) cause;
                    result = isEofToken(antlrException.token);
                }
                if (cause instanceof MismatchedTokenException) {
                    MismatchedTokenException antlrException = (MismatchedTokenException) cause;
                    result = isEofToken(antlrException.token);
                }
            }
        }
        return result;    
    }

    protected boolean isEofToken(antlr.Token token) {
        return token.getType() == antlr.Token.EOF_TYPE;
    }



    //---------------------------------------------------------------------------
    // FACTORIES


    /**
     * A convenience routine to create a standalone SourceUnit on a String
     * with defaults for almost everything that is configurable.
     */
    public static SourceUnit create(String name, String source) {
        CompilerConfiguration configuration = new CompilerConfiguration();
        configuration.setTolerance(1);

        return new SourceUnit(name, source, configuration, null, new ErrorCollector(configuration));
    }


    /**
     * A convenience routine to create a standalone SourceUnit on a String
     * with defaults for almost everything that is configurable.
     */
    public static SourceUnit create(String name, String source, int tolerance) {
        CompilerConfiguration configuration = new CompilerConfiguration();
        configuration.setTolerance(tolerance);

        return new SourceUnit(name, source, configuration, null, new ErrorCollector(configuration));
    }





    //---------------------------------------------------------------------------
    // PROCESSING


    /**
     * Parses the source to a CST.  You can retrieve it with getCST().
     */
    public void parse() throws CompilationFailedException {
        if (this.phase > Phases.PARSING) {
            throw new GroovyBugError("parsing is already complete");
        }

        if (this.phase == Phases.INITIALIZATION) {
            nextPhase();
        }


        //
        // Create a reader on the source and run the parser.

        Reader reader = null;
        try {
            reader = source.getReader();

            // lets recreate the parser each time as it tends to keep around state
            parserPlugin = getConfiguration().getPluginFactory().createParserPlugin();

            cst = parserPlugin.parseCST(this, reader);
            sourceSummary = parserPlugin.getSummary();

            reader.close();
            completePhase();
            
        }
        catch (IOException e) {
            getErrorCollector().addFatalError(new SimpleMessage(e.getMessage(),this));
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException e) {
                }
            }
        }
    }


    /**
     * Generates an AST from the CST.  You can retrieve it with getAST().
     */
    public void convert() throws CompilationFailedException {
        if (this.phase == Phases.PARSING && this.phaseComplete) {
            gotoPhase(Phases.CONVERSION);
        }

        if (this.phase != Phases.CONVERSION) {
            throw new GroovyBugError("SourceUnit not ready for convert()");
        }

        
        //
        // Build the AST
        
        try {
            this.ast = parserPlugin.buildAST(this, this.classLoader, this.cst);

            this.ast.setDescription(this.name);
        }
        catch (SyntaxException e) {
            getErrorCollector().addError(new SyntaxErrorMessage(e,this));
        }

        if ("xml".equals(System.getProperty("groovy.ast"))) {
            saveAsXML(name,ast);
        }

        completePhase();
    }

    private void saveAsXML(String name, ModuleNode ast) {
        XStream xstream = new XStream();
        try {
            xstream.toXML(ast,new FileWriter(name + ".xml"));
            System.out.println("Written AST to " + name + ".xml");
        } catch (Exception e) {
            System.out.println("Couldn't write to " + name + ".xml");
            e.printStackTrace();
        }
    }
    //---------------------------------------------------------------------------    // SOURCE SAMPLING

    /**
     * Returns a sampling of the source at the specified line and column,
     * of null if it is unavailable.
     */
    public String getSample(int line, int column, Janitor janitor) {
        String sample = null;
        String text = source.getLine(line, janitor);

        if (text != null) {
            if (column > 0) {
                String marker = Utilities.repeatString(" ", column - 1) + "^";

                if (column > 40) {
                    int start = column - 30 - 1;
                    int end = (column + 10 > text.length() ? text.length() : column + 10 - 1);
                    sample = "   " + text.substring(start, end) + Utilities.eol() + "   " + marker.substring(start, marker.length());
                }
                else {
                    sample = "   " + text + Utilities.eol() + "   " + marker;
                }
            }
            else {
                sample = text;
            }
        }

        return sample;
    }

    /**
     * to quickly create a ModuleNode from a piece of Groovy code
     *
     * @param code
     * @return
     * @throws CompilationFailedException
     */
    public static ModuleNode createModule(String code) throws CompilationFailedException {
        SourceUnit su = create("NodeGen", code);
        su.parse();
        su.convert();
        return su.getAST();
    }

    public static ClassNode createClassNode(String code) throws CompilationFailedException {
        ModuleNode module = createModule(code);
        List classes = module.getClasses();
        if (classes.size() > 1) {
            throw new RuntimeException("The code defines more than one class");
        }
        return (ClassNode) classes.get(0);
    }

    /**
     * Takes a field definition statement and wrap it in class definition. The FieldNode object
     * representing the field is extracted and returned, Types need to be fully qualified.
     *
     * @param code a naked statement to define a field, such as: String prop = "hello"
     * @return a FieldNode object.
     * @throws CompilationFailedException
     */
    public static FieldNode createFieldNode(String code) throws CompilationFailedException {
        ClassNode classNode = createClassNode(wrapCode(code));
        List flds = classNode.getFields();
        if (flds.size() > 1) {
            throw new RuntimeException("The code defines more than one field");
        }
        return (FieldNode) flds.get(0);
    }

    public Statement createStatement(String code) throws CompilationFailedException {
        ModuleNode module = createModule(code);
        BlockStatement block = module.getStatementBlock();
        if (block == null) {
            throw new RuntimeException("no proper statement block is created.");
        }
        List stats = block.getStatements();
        if (stats == null || stats.size() != 1) {
            throw new RuntimeException("no proper statement node is created.");
        }
        return (Statement) stats.get(0);
    }

    public MethodNode createMethodNode(String code) throws CompilationFailedException {
        code = code.trim();
        if (code.indexOf("def") != 0) {
            code = "def " + code;
        }
        ModuleNode module = createModule(code);
        List ms = module.getMethods();
        if (ms == null || ms.size() != 1) {
            throw new RuntimeException("no proper method node is created.");
        }
        return (MethodNode) ms.get(0);
    }

    private static String wrapCode(String code) {
        String prefix = "class SynthedClass {\n";
        String suffix = "\n }";
        return prefix + code + suffix;

    }
    
    public void addException(Exception e) throws CompilationFailedException {
        getErrorCollector().addException(e,this);
    }
    
    public void addError(SyntaxException se) throws CompilationFailedException {
        getErrorCollector().addError(se,this);
    }
}




