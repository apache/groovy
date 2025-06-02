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
import org.codehaus.groovy.control.messages.WarningMessage;
import org.codehaus.groovy.syntax.Reduction;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.tools.Utilities;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;

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
            /*
            Message last = (Message) getErrorCollector().getLastError();
            Throwable cause = null;
            if (last instanceof SyntaxErrorMessage) {
                cause = ((SyntaxErrorMessage) last).getCause().getCause();
            }
            if (cause != null) {
                if (cause instanceof groovyjarjarantlr.NoViableAltException) {
                    return isEofToken(((groovyjarjarantlr.NoViableAltException) cause).token);
                } else if (cause instanceof groovyjarjarantlr.NoViableAltForCharException) {
                    char badChar = ((groovyjarjarantlr.NoViableAltForCharException) cause).foundChar;
                    return badChar == groovyjarjarantlr.CharScanner.EOF_CHAR;
                } else if (cause instanceof groovyjarjarantlr.MismatchedCharException) {
                    char badChar = (char) ((groovyjarjarantlr.MismatchedCharException) cause).foundChar;
                    return badChar == groovyjarjarantlr.CharScanner.EOF_CHAR;
                } else if (cause instanceof groovyjarjarantlr.MismatchedTokenException) {
                    return isEofToken(((groovyjarjarantlr.MismatchedTokenException) cause).token);
                }
            }
            */
            return true;
        }
        return false;
    }

    /*protected boolean isEofToken(groovyjarjarantlr.Token token) {
        return token.getType() == groovyjarjarantlr.Token.EOF_TYPE;
    }*/

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

        buildAST();

        if ("xml".equals(getProperty("groovy.ast"))) {
            XStreamUtils.serialize(name, ast);
        }
    }

    @SuppressWarnings("removal") // TODO a future Groovy version should get the property not as a privileged action
    private String getProperty(String key) {
        return java.security.AccessController.doPrivileged((java.security.PrivilegedAction<String>) () -> System.getProperty(key));
    }

    /**
     * Builds the AST.
     */
    public ModuleNode buildAST() {
        if (this.ast == null) {
            try {
                this.ast = parserPlugin.buildAST(this, this.classLoader, this.cst);
                this.ast.setDescription(this.name);
            } catch (SyntaxException e) {
                if (this.ast == null) {
                    // create an empty ModuleNode to represent a failed parse, in case a later phase attempts to use the AST
                    this.ast = new ModuleNode(this);
                }
                getErrorCollector().addError(new SyntaxErrorMessage(e, this));
            }
        }
        return this.ast;
    }

    //--------------------------------------------------------------------------
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

                if (column > 60) {
                    int start = column - 45 - 1;
                    int length = text.length();
                    int end = (column + 25 > length ? length : column + 25 - 1);
                    if (start >= length || end < start)
                        return null; // can happen with CR only files GROOVY-10676
                    sample = "   " + text.substring(start, end) + Utilities.eol() + "   " + marker.substring(start);
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
    public void addException(final Exception e) throws CompilationFailedException {
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
    public void addError(final SyntaxException se) throws CompilationFailedException {
        getErrorCollector().addError(se, this);
    }

    /**
     * Convenience wrapper for {@link ErrorCollector#addFatalError(org.codehaus.groovy.control.messages.Message)}.
     *
     * @param text the error message
     * @param node for locating the offending code
     * @throws CompilationFailedException on error
     *
     * @since 3.0.0
     */
    public void addFatalError(final String text, final ASTNode node) throws CompilationFailedException {
        getErrorCollector().addFatalError(Message.create(new SyntaxException(text, node), this));
    }

    /**
     * @since 4.0.7
     */
    public void addWarning(final String text, final ASTNode node) {
        Token token = new Token(0, "", node.getLineNumber(), node.getColumnNumber()); // ASTNode to CSTNode
        getErrorCollector().addWarning(new WarningMessage(WarningMessage.POSSIBLE_ERRORS, text, token, this));
    }

    public void addErrorAndContinue(final SyntaxException se) {
        getErrorCollector().addErrorAndContinue(se, this);
    }

    public ReaderSource getSource() {
        return source;
    }

    public void setSource(ReaderSource source) {
        this.source = source;
    }
}
