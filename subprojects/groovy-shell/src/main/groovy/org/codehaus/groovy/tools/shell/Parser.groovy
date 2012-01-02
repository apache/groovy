/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.tools.shell

import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.tools.shell.util.Logger
import org.codehaus.groovy.tools.shell.util.Preferences
import org.codehaus.groovy.antlr.SourceBuffer
import org.codehaus.groovy.antlr.UnicodeEscapingReader
import org.codehaus.groovy.antlr.parser.GroovyLexer
import org.codehaus.groovy.antlr.parser.GroovyRecognizer
import antlr.collections.AST
import antlr.RecognitionException
import antlr.TokenStreamException

/**
 * Provides a facade over the parser to recognize valid Groovy syntax.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class Parser
{
    static final String NEWLINE = System.getProperty('line.separator')

    private static final Logger log = Logger.create(Parser.class)

    private final def delegate

    Parser() {
        def f = Preferences.parserFlavor

        log.debug("Using parser flavor: $f")
        
        switch (f) {
            case 'relaxed':
                delegate = new RelaxedParser()
                break

            case 'rigid':
                delegate = new RigidParser()
                break

            default:
                log.error("Invalid parser flavor: $f; using default")
                delegate = new RigidParser()
                break
        }
    }
    
    ParseStatus parse(final List buffer) {
        return delegate.parse(buffer)
    }
}

/**
 * A relaxed parser, which tends to allow more, but won't really catch valid syntax errors.
 */
final class RelaxedParser
{
    private final Logger log = Logger.create(this.class)

    private SourceBuffer sourceBuffer

    private String[] tokenNames

    ParseStatus parse(final List buffer) {
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
            switch (e.class) {
                case TokenStreamException:
                case RecognitionException:
                    log.debug("Parse incomplete: $e (${e.class.name})")
    
                    return new ParseStatus(ParseCode.INCOMPLETE)

                default:
                    log.debug("Parse error: $e (${e.class.name})")

                    return new ParseStatus(e)
            }
        }
    }

    protected AST doParse(final UnicodeEscapingReader reader) throws Exception {
        def lexer = new GroovyLexer(reader)
        reader.setLexer(lexer)

        def parser = GroovyRecognizer.make(lexer)
        parser.setSourceBuffer(sourceBuffer)
        tokenNames = parser.getTokenNames()

        parser.compilationUnit()
        return parser.getAST()
    }
}

/**
 * A more rigid parser which catches more syntax errors, but also tends to barf on stuff that is really valid from time to time.
 */
final class RigidParser
{
    static final String SCRIPT_FILENAME = 'groovysh_parse'

    private final Logger log = Logger.create(this.class)

    ParseStatus parse(final List buffer) {
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
            //
            // FIXME: Seems like failedWithUnexpectedEOF() is not always set as expected, as in:
            //
            // class a {               <--- is true here
            //    def b() {            <--- is false here :-(
            //

            if (parser.errorCollector.errorCount > 1 || !parser.failedWithUnexpectedEOF()) {
                //
                // HACK: Super insane hack... if we detect a syntax error, but the last line of the
                //       buffer ends with a {, [, ''', """ or \ then ignore...
                //       and pretend its okay, cause it might be...
                //
                //       This seems to get around the problem with things like:
                //
                //       class a { def b() {
                //

                String tmp = buffer[-1].trim()

                if (tmp.endsWith('{')
                    || tmp.endsWith('[')
                    || tmp.endsWith("'''")
                    || tmp.endsWith('"""')
                    || tmp.endsWith('\\')) {
                    log.debug("Ignoring parse failure; might be valid: $e")
                }
                else {
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
        else {
            log.debug('Parse incomplete')

            return new ParseStatus(ParseCode.INCOMPLETE)
        }
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
