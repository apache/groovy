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
package org.codehaus.groovy.tools.shell

import antlr.RecognitionException
import antlr.TokenStreamException
import antlr.collections.AST
import org.codehaus.groovy.antlr.SourceBuffer
import org.codehaus.groovy.antlr.UnicodeEscapingReader
import org.codehaus.groovy.antlr.parser.GroovyLexer
import org.codehaus.groovy.antlr.parser.GroovyRecognizer
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.tools.shell.util.Logger
import org.codehaus.groovy.tools.shell.util.Preferences

import java.util.regex.Pattern

interface Parsing {
    ParseStatus parse(final Collection<String> buffer)
}

/**
 * Provides a facade over the parser to recognize valid Groovy syntax.
 */
class Parser
{
    static final String NEWLINE = System.getProperty('line.separator')

    private static final Logger log = Logger.create(Parser)

    private final Parsing delegate

    Parser() {
        String flavor = Preferences.getParserFlavor()

        log.debug("Using parser flavor: $flavor")

        switch (flavor) {
            case Preferences.PARSER_RELAXED:
                delegate = new RelaxedParser()
                break

            case Preferences.PARSER_RIGID:
                delegate = new RigidParser()
                break

            default:
                log.error("Invalid parser flavor: $flavor; using default: $Preferences.PARSER_RIGID")
                delegate = new RigidParser()
                break
        }
    }

    ParseStatus parse(final Collection<String> buffer) {
        return delegate.parse(buffer)
    }
}

/**
 * A relaxed parser, which tends to allow more, but won't really catch valid syntax errors.
 */
final class RelaxedParser implements Parsing
{
    private final Logger log = Logger.create(this.class)

    private SourceBuffer sourceBuffer

    private String[] tokenNames

    @Override
    ParseStatus parse(final Collection<String> buffer) {
        assert buffer

        sourceBuffer = new SourceBuffer()

        def source = buffer.join(Parser.NEWLINE)

        log.debug("Parsing: $source")

        try {
            doParse(new UnicodeEscapingReader(new StringReader(source), sourceBuffer))

            log.debug('Parse complete')

            return new ParseStatus(ParseCode.COMPLETE)
        }
        catch (e) {
            switch (e.getClass()) {
                case TokenStreamException:
                case RecognitionException:
                    log.debug("Parse incomplete: $e (${e.getClass().name})")

                    return new ParseStatus(ParseCode.INCOMPLETE)

                default:
                    log.debug("Parse error: $e (${e.getClass().name})")

                    return new ParseStatus(e)
            }
        }
    }

    protected AST doParse(final UnicodeEscapingReader reader) throws Exception {
        GroovyLexer lexer = new GroovyLexer(reader)
        reader.setLexer(lexer)

        def parser = GroovyRecognizer.make(lexer)
        parser.setSourceBuffer(sourceBuffer)
        tokenNames = parser.tokenNames

        parser.compilationUnit()
        return parser.AST
    }
}

/**
 * A more rigid parser which catches more syntax errors, but also tends to barf on stuff that is really valid from time to time.
 */
final class RigidParser implements Parsing
{
    private static final Pattern ANNOTATION_PATTERN = Pattern.compile('^@[a-zA-Z_][a-zA-Z_0-9]*(.*)$')
    static final String SCRIPT_FILENAME = 'groovysh_parse'

    private final Logger log = Logger.create(this.class)

    @Override
    ParseStatus parse(final Collection<String> buffer) {
        assert buffer

        String source = buffer.join(Parser.NEWLINE)

        log.debug("Parsing: $source")

        SourceUnit parser
        Throwable error

        try {
            parser = SourceUnit.create(SCRIPT_FILENAME, source, /*tolerance*/ 1)
            parser.parse()

            log.debug('Parse complete')

            return new ParseStatus(ParseCode.COMPLETE)
        }
        catch (CompilationFailedException e) {
            // During a shell session often a user will hit enter without having completed a class definition
            // for the parser this means it will raise some kind of compilation exception.
            // The following code has to attempt to hide away all such exceptions that are due to the code being
            // incomplete, but show all exceptions due to the code being incorrect.
            // Unexpected EOF is most common for incomplete code, however there are several other situations
            // where the code is incomplete, but the Exception is raised without failedWithUnexpectedEOF().

            // FIXME: Seems like failedWithUnexpectedEOF() is not always set as expected, as in:
            //
            // class a {               <--- is true here
            //    def b() {            <--- is false here :-(
            //

            if (parser.errorCollector.errorCount > 1 || !parser.failedWithUnexpectedEOF()) {

                // HACK: Super insane hack... we detect a syntax error, but might still ignore
                // it depending on the line ending
                if (ignoreSyntaxErrorForLineEnding(buffer[-1].trim()) ||
                        isAnnotationExpression(e, buffer[-1].trim()) ||
                        hasUnmatchedOpenBracketOrParen(source)) {
                    log.debug("Ignoring parse failure; might be valid: $e")
                } else {
                    error = e
                }
            }
        }
        catch (Throwable e) {
            error = e
        }

        if (error) {
            log.debug("Parse error: $error")

            return new ParseStatus(error)
        }
        log.debug('Parse incomplete')
        return new ParseStatus(ParseCode.INCOMPLETE)
    }

    static boolean ignoreSyntaxErrorForLineEnding(String line) {
        def final lineEndings = ['{', '[', '(', ',', '.', '-', '+', '/', '*', '%', '&', '|', '?', '<', '>', '=', ':', "'''", '"""', '\\']
        for (String lineEnding in lineEndings) {
            if (line.endsWith(lineEnding)) {
                return true
            }
        }
        return false
    }

    static boolean hasUnmatchedOpenBracketOrParen(String source) {
        if (!source) {
            return false
        }
        int parens = 0
        int brackets = 0
        for (ch in source) {
            switch(ch) {
                case '[': ++brackets; break;
                case ']': --brackets; break;
                case '(': ++parens; break;
                case ')': --parens; break;
                default:
                    break
            }
        }
        return (brackets > 0 || parens > 0)
    }

    static boolean isAnnotationExpression(CompilationFailedException e, String line) {
        return e.getMessage().contains('unexpected token: @') && ANNOTATION_PATTERN.matcher(line).find()
    }
}

/**
 * Container for the parse code.
 */
final class ParseCode
{
    static final ParseCode COMPLETE = new ParseCode(0)

    static final ParseCode INCOMPLETE = new ParseCode(1)

    static final ParseCode ERROR = new ParseCode(2)

    final int code

    private ParseCode(int code) {
        this.code = code
    }

    @Override
    String toString() {
        return code
    }
}

/**
 * Container for parse status details.
 */
final class ParseStatus
{
    final ParseCode code

    final Throwable cause

    ParseStatus(final ParseCode code, final Throwable cause) {
        this.code = code
        this.cause = cause
    }

    ParseStatus(final ParseCode code) {
        this(code, null)
    }

    ParseStatus(final Throwable cause) {
        this(ParseCode.ERROR, cause)
    }
}

