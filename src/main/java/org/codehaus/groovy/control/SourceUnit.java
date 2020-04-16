/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.control;

//import antlr.CharScanner;
//import antlr.MismatchedCharException;
//import antlr.MismatchedTokenException;
//import antlr.NoViableAltException;
//import antlr.NoViableAltForCharException;
import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.io.FileReaderSource;
import org.codehaus.groovy.control.io.ReaderSource;
import org.codehaus.groovy.control.io.StringReaderSource;
import org.codehaus.groovy.control.io.URLReaderSource;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.control.messages.SimpleMessage;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.Reduction;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.tools.Utilities;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Provides an anchor for a single source unit (usually a script file)
 * as it passes through the compiler system.
 */
public class SourceUnit extends ProcessingUnit {

    /**
     * The pluggable parser used to generate the AST - we allow
     * pluggability currently as we need to have Classic and JSR support
     */
    private ParserPlugin parserPlugin;

    /**
     * Where we can get Readers for our source unit
     */
    protected ReaderSource source;

    /**
     * A descriptive name of the source unit. This name shouldn't
     * be used for controlling the SourceUnit, it is only for error
     * messages and to determine the name of the class for
     * a script.
     */
    protected String name;

    /**
     * A Concrete Syntax Tree of the source
     */
    protected Reduction cst;

    /**
     * The root of the Abstract Syntax Tree for the source
     */
    protected ModuleNode ast;

    /**
     * Initializes the SourceUnit from existing machinery.
     */
    public SourceUnit(String name, ReaderSource source, CompilerConfiguration flags,
                      GroovyClassLoader loader, ErrorCollector er) {
        super(flags, loader, er);

        this.name = name;
        this.source = source;
    }

    /**
     * Initializes the SourceUnit from the specified file.
     */
    public SourceUnit(File source, CompilerConfiguration configuration, GroovyClassLoader loader, ErrorCollector er) {
        this(source.getPath(), new FileReaderSource(source, configuration), configuration, loader, er);
    }

    /**
     * Initializes the SourceUnit from the specified URL.
     */
    public SourceUnit(URL source, CompilerConfiguration configuration, GroovyClassLoader loader, ErrorCollector er) {
        this(source.toExternalForm(), new URLReaderSource(source, configuration), configuration, loader, er);
    }

    /**
     * Initializes the SourceUnit for a string of source.
     */
    public SourceUnit(String name, String source, CompilerConfiguration configuration,
                      GroovyClassLoader loader, ErrorCollector er) {
        this(name, new StringReaderSource(source, configuration), configuration, loader, er);
    }

    /**
     * Returns the name for the SourceUnit. This name shouldn't
     * be used for controlling the SourceUnit, it is only for error
     * messages
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
     * Returns the Abstract Syntax Tree produced during convert()ing
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
        // Implementation note - there are several ways for the Groovy compiler
        // to report an unexpected EOF. Perhaps this implementation misses some.
        // If you find another way, please add it.
        if (getErrorCollector().hasErrors()) {
            Message last = (Message) getErrorCollector().getLastError();
            Throwable cause = null;
            if (last instanceof SyntaxErrorMessage) {
                cause = ((SyntaxErrorMessage) last).getCause().getCause();
            }
//            if (cause != null) {
//                if (cause instanceof NoViableAltException) {
//                    return isEofToken(((NoViableAltException) cause).token);
//                } else if (cause instanceof NoViableAltForCharException) {
//                    char badChar = ((NoViableAltForCharException) cause).foundChar;
//                    return badChar == CharScanner.EOF_CHAR;
//                } else if (cause instanceof MismatchedCharException) {
//                    char badChar = (char) ((MismatchedCharException) cause).foundChar;
//                    return badChar == CharScanner.EOF_CHAR;
//                } else if (cause instanceof MismatchedTokenException) {
//                    return isEofToken(((MismatchedTokenException) cause).token);
//                }
//            }
            return true;
        }
        return false;
    }

//    protected boolean isEofToken(antlr.Token token) {
//        return token.getType() == antlr.Token.EOF_TYPE;
//    }

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

        try (Reader reader = source.getReader()) {
            // let's recreate the parser each time as it tends to keep around state
            parserPlugin = getConfiguration().getPluginFactory().createParserPlugin();

            cst = parserPlugin.parseCST(this, reader);
        } catch (IOException e) {
            getErrorCollector().addFatalError(new SimpleMessage(e.getMessage(), this));
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
        } catch (SyntaxException e) {
            if (this.ast == null) {
                // Create a dummy ModuleNode to represent a failed parse - in case a later phase attempts to use the ast
                this.ast = new ModuleNode(this);
            }
            getErrorCollector().addError(new SyntaxErrorMessage(e, this));
        }

        String property = (String) AccessController.doPrivileged((PrivilegedAction) () -> System.getProperty("groovy.ast"));

        if ("xml".equals(property)) {
            XStreamUtils.serialize(name, ast);
        }
    }

    //---------------------------------------------------------------------------
    // SOURCE SAMPLING

    /**
     * Returns a sampling of the source at the specified line and column,
     * or null if it is unavailable.
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
                    sample = "   " + text.substring(start, end) + Utilities.eol() + "   " +
                            marker.substring(start);
                } else {
                    sample = "   " + text + Utilities.eol() + "   " + marker;
                }
            } else {
                sample = text;
            }
        }

        return sample;
    }

    /**
     * This method adds an exception to the error collector. The Exception most likely has no line number attached to it.
     * For this reason you should use this method sparingly. Prefer using addError for syntax errors or add an error
     * to the {@link ErrorCollector} directly by retrieving it with getErrorCollector().
     *
     * @param e the exception that occurred
     * @throws CompilationFailedException on error
     */
    public void addException(Exception e) throws CompilationFailedException {
        getErrorCollector().addException(e, this);
    }

    /**
     * This method adds a SyntaxException to the error collector. The exception should specify the line and column
     * number of the error.  This method should be reserved for real errors in the syntax of the SourceUnit. If
     * your error is not in syntax, and is a semantic error, or more general error, then use addException or use
     * the error collector directly by retrieving it with getErrorCollector().
     *
     * @param se the exception, which should have line and column information
     * @throws CompilationFailedException on error
     */
    public void addError(SyntaxException se) throws CompilationFailedException {
        getErrorCollector().addError(se, this);
    }

    /**
     * Convenience wrapper for {@link ErrorCollector#addFatalError(org.codehaus.groovy.control.messages.Message)}.
     *
     * @param msg the error message
     * @param node the AST node
     * @throws CompilationFailedException on error
     * @since 3.0.0
     */
    public void addFatalError(String msg, ASTNode node) throws CompilationFailedException {
        getErrorCollector().addFatalError(
                new SyntaxErrorMessage(
                        new SyntaxException(
                                msg,
                                node.getLineNumber(),
                                node.getColumnNumber(),
                                node.getLastLineNumber(),
                                node.getLastColumnNumber()
                        ),
                        this
                )
        );
    }

    public void addErrorAndContinue(SyntaxException se) throws CompilationFailedException {
        getErrorCollector().addErrorAndContinue(se, this);
    }

    public ReaderSource getSource() {
        return source;
    }

    public void setSource(ReaderSource source) {
        this.source = source;
    }
}
