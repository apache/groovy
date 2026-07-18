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
package org.apache.groovy.parser.antlr4

import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.LexerNoViableAltException
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer
import org.antlr.v4.runtime.Token
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.shouldFail
import static org.apache.groovy.parser.antlr4.GroovyLexer.GSTRING_TYPE_SELECTOR_MODE
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertSame
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * Unit-level coverage for {@link GroovyLangLexer}'s GString dollar-body diagnostics
 * (GROOVY-12171).
 * <p>
 * Exercises every branch of {@code nextToken}, {@code notifyListeners}, {@code recover}
 * and the private dollar-message helpers: both the GString-selector mode and the
 * generic non-GString fall-through, plus the EOF vs. concrete-character message forms.
 */
final class GroovyLangLexerGStringDollarTest {

    private static final String ILLEGAL_AFTER_DOLLAR = 'Illegal string body character after dollar sign'

    // -------------------------------------------------------------------------
    // constructors
    // -------------------------------------------------------------------------

    @Test
    void 'Reader constructor tokenizes the same as CharStream constructor'() {
        def source = 'def x = 1'
        def fromReader = new GroovyLangLexer(new StringReader(source))
        def fromStream = lexer(source)
        assertEquals fromStream.nextToken().type, fromReader.nextToken().type
    }

    // -------------------------------------------------------------------------
    // nextToken — success path (super.nextToken)
    // -------------------------------------------------------------------------

    @Test
    void 'nextToken returns ordinary tokens outside GString dollar mode'() {
        def lexer = lexer('def x = 1')
        def types = []
        Token t
        while ((t = lexer.nextToken()).type != Token.EOF) {
            if (t.channel == Token.DEFAULT_CHANNEL) {
                types << t.type
            }
        }
        assertFalse types.isEmpty()
        assertEquals Token.EOF, t.type
    }

    // -------------------------------------------------------------------------
    // nextToken — _hitEOF + GSTRING_TYPE_SELECTOR_MODE (EOF after bare $)
    // -------------------------------------------------------------------------

    @Test
    void 'nextToken reports dollar-body error when EOF follows bare dollar in GString'() {
        // GStringBegin consumes up through `$`, sets _hitEOF because LA(1)==EOF,
        // and leaves the lexer in GSTRING_TYPE_SELECTOR_MODE.  The subsequent
        // nextToken must not silently emit EOF.
        def lexer = lexer('"hello$')
        Token first = lexer.nextToken()
        assertEquals GroovyLexer.GStringBegin, first.type

        def err = shouldFail(GroovySyntaxError) {
            lexer.nextToken()
        }
        assertEquals ILLEGAL_AFTER_DOLLAR, err.message
        assertEquals GroovySyntaxError.LEXER, err.source
        assertTrue err.line >= 1
        assertTrue err.column >= 1
    }

    @Test
    void 'nextToken EOF dollar error for dollar-only open GString'() {
        def lexer = lexer('"$')
        assertEquals GroovyLexer.GStringBegin, lexer.nextToken().type

        def err = shouldFail(GroovySyntaxError) {
            lexer.nextToken()
        }
        assertEquals ILLEGAL_AFTER_DOLLAR, err.message
        // EOF form has no ": '…'" character display
        assertFalse err.message.contains(':')
    }

    // -------------------------------------------------------------------------
    // nextToken / recover / notifyListeners — illegal character after $
    // -------------------------------------------------------------------------

    @Test
    void 'lex of closing quote after dollar throws GroovySyntaxError with quoted char'() {
        // notifyListeners (GString branch) + recover (GString branch) + char message
        def err = shouldFail(GroovySyntaxError) {
            drain(lexer('"releases$"'))
        }
        assertTrue err.message.startsWith(ILLEGAL_AFTER_DOLLAR + ':')
        assertTrue err.message.contains('"') || err.message.contains('\\"')
        assertEquals GroovySyntaxError.LEXER, err.source
    }

    @Test
    void 'lex of space after dollar throws GroovySyntaxError naming the space'() {
        def err = shouldFail(GroovySyntaxError) {
            drain(lexer('"a$ b"'))
        }
        assertTrue err.message.contains(ILLEGAL_AFTER_DOLLAR)
        assertTrue err.message.contains(' ')
    }

    @Test
    void 'lex of newline after dollar throws GroovySyntaxError naming the newline'() {
        def err = shouldFail(GroovySyntaxError) {
            drain(lexer('"hello$\n'))
        }
        assertTrue err.message.contains(ILLEGAL_AFTER_DOLLAR)
        assertTrue err.message.contains('\\n') || err.message.contains('\n')
    }

    @Test
    void 'lex of digit after dollar is illegal'() {
        // IdentifierInGString cannot start with a digit
        def err = shouldFail(GroovySyntaxError) {
            drain(lexer('"val$1"'))
        }
        assertTrue err.message.contains(ILLEGAL_AFTER_DOLLAR)
        assertTrue err.message.contains('1')
    }

    @Test
    void 'lex of punctuation after dollar is illegal'() {
        def err = shouldFail(GroovySyntaxError) {
            drain(lexer('"val$,rest"'))
        }
        assertTrue err.message.contains(ILLEGAL_AFTER_DOLLAR)
        assertTrue err.message.contains(',')
    }

    @Test
    void 'triple-double-quoted GString trailing dollar reports dollar-body error'() {
        def err = shouldFail(GroovySyntaxError) {
            drain(lexer('"""releases$"""'))
        }
        assertTrue err.message.contains(ILLEGAL_AFTER_DOLLAR)
    }

    @Test
    void 'GStringPart mid-string trailing dollar reports dollar-body error'() {
        // After $name the next $ pushes GSTRING_TYPE_SELECTOR_MODE again; closing "
        // is then illegal
        def err = shouldFail(GroovySyntaxError) {
            drain(lexer('"hello $name$"'))
        }
        assertTrue err.message.contains(ILLEGAL_AFTER_DOLLAR)
    }

    @Test
    void 'valid GString interpolation tokenizes without dollar-body error'() {
        def types = []
        def lexer = lexer('"hello $name!"')
        Token t
        while ((t = lexer.nextToken()).type != Token.EOF) {
            if (t.channel == Token.DEFAULT_CHANNEL) {
                types << t.type
            }
        }
        assertTrue types.contains(GroovyLexer.GStringBegin)
        assertTrue types.contains(GroovyLexer.Identifier)
        assertTrue types.contains(GroovyLexer.GStringEnd)
    }

    @Test
    void 'valid brace interpolation tokenizes without dollar-body error'() {
        def lexer = lexer('"${1+2}"')
        def sawLBrace = false
        Token t
        while ((t = lexer.nextToken()).type != Token.EOF) {
            if (t.type == GroovyLexer.LBRACE) {
                sawLBrace = true
            }
        }
        assertTrue sawLBrace
    }

    // -------------------------------------------------------------------------
    // recover — non-GString fall-through rethrows LexerNoViableAltException
    // -------------------------------------------------------------------------

    @Test
    void 'recover outside GString dollar mode rethrows the recognition failure'() {
        def lexer = lexer('x')
        def e = new LexerNoViableAltException(lexer, lexer.inputStream, 0, null)
        def thrown = shouldFail(LexerNoViableAltException) {
            lexer.recover(e)
        }
        assertSame e, thrown
    }

    // -------------------------------------------------------------------------
    // recover — GString dollar mode → GroovySyntaxError (char + EOF messages)
    // -------------------------------------------------------------------------

    @Test
    void 'recover in GString dollar mode with concrete char throws GroovySyntaxError'() {
        def lexer = lexer('!')
        lexer.pushMode(GSTRING_TYPE_SELECTOR_MODE)
        def e = new LexerNoViableAltException(lexer, lexer.inputStream, 0, null)

        def err = shouldFail(GroovySyntaxError) {
            lexer.recover(e)
        }
        assertTrue err.message.startsWith(ILLEGAL_AFTER_DOLLAR + ':')
        assertEquals GroovySyntaxError.LEXER, err.source
    }

    @Test
    void 'recover in GString dollar mode at EOF throws GroovySyntaxError without char display'() {
        def lexer = lexer('')
        lexer.pushMode(GSTRING_TYPE_SELECTOR_MODE)
        def e = new LexerNoViableAltException(lexer, lexer.inputStream, 0, null)

        def err = shouldFail(GroovySyntaxError) {
            lexer.recover(e)
        }
        assertEquals ILLEGAL_AFTER_DOLLAR, err.message
        assertFalse err.message.contains(':')
        assertEquals GroovySyntaxError.LEXER, err.source
    }

    // -------------------------------------------------------------------------
    // notifyListeners — GString dollar mode emits dedicated diagnostic
    // -------------------------------------------------------------------------

    @Test
    void 'notifyListeners in GString dollar mode reports illegal dollar-body message'() {
        def lexer = lexer('!')
        lexer.pushMode(GSTRING_TYPE_SELECTOR_MODE)
        def messages = []
        lexer.removeErrorListeners()
        lexer.addErrorListener(new BaseErrorListener() {
            @Override
            void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                             int line, int charPositionInLine, String msg,
                             RecognitionException e) {
                messages << msg
            }
        })
        def e = new LexerNoViableAltException(lexer, lexer.inputStream, 0, null)
        lexer.notifyListeners(e)

        assertEquals 1, messages.size()
        assertTrue messages[0].startsWith(ILLEGAL_AFTER_DOLLAR + ':')
    }

    @Test
    void 'notifyListeners in GString dollar mode at EOF reports message without char'() {
        def lexer = lexer('')
        lexer.pushMode(GSTRING_TYPE_SELECTOR_MODE)
        def messages = []
        lexer.removeErrorListeners()
        lexer.addErrorListener(new BaseErrorListener() {
            @Override
            void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                             int line, int charPositionInLine, String msg,
                             RecognitionException e) {
                messages << msg
            }
        })
        def e = new LexerNoViableAltException(lexer, lexer.inputStream, 0, null)
        lexer.notifyListeners(e)

        assertEquals 1, messages.size()
        assertEquals ILLEGAL_AFTER_DOLLAR, messages[0]
    }

    // -------------------------------------------------------------------------
    // notifyListeners — non-GString fall-through (generic Antlr message)
    // -------------------------------------------------------------------------

    @Test
    void 'notifyListeners outside GString dollar mode uses generic recognition message'() {
        def lexer = lexer('!')
        // super.notifyListeners reads [_tokenStartCharIndex, index]; seed a valid span
        // (same package → protected Lexer fields are accessible).
        lexer._tokenStartCharIndex = 0
        def messages = []
        lexer.removeErrorListeners()
        lexer.addErrorListener(new BaseErrorListener() {
            @Override
            void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                             int line, int charPositionInLine, String msg,
                             RecognitionException e) {
                messages << msg
            }
        })
        def e = new LexerNoViableAltException(lexer, lexer.inputStream, 0, null)
        lexer.notifyListeners(e)

        assertEquals 1, messages.size()
        assertTrue messages[0].startsWith('token recognition error'),
                "expected generic Antlr message, got: ${messages[0]}"
        assertFalse messages[0].contains(ILLEGAL_AFTER_DOLLAR)
    }

    // -------------------------------------------------------------------------
    // Integration-style: forced dollar-selector mode (match path, not just
    // the nextToken _hitEOF short-circuit after GStringBegin)
    // -------------------------------------------------------------------------

    @Test
    void 'EOF while already in GString dollar mode yields dollar-body error on subsequent nextToken'() {
        // Antlr's ATN returns Token.EOF without throwing when the input is already
        // exhausted and nothing was consumed (failOrAccept).  That sets _hitEOF;
        // the next call then hits GroovyLangLexer.nextToken's dollar-mode guard.
        def lexer = lexer('')
        lexer.pushMode(GSTRING_TYPE_SELECTOR_MODE)
        assertEquals Token.EOF, lexer.nextToken().type

        def err = shouldFail(GroovySyntaxError) {
            lexer.nextToken()
        }
        assertEquals ILLEGAL_AFTER_DOLLAR, err.message
        assertEquals GroovySyntaxError.LEXER, err.source
    }

    @Test
    void 'match failure on illegal char in GString dollar mode yields dollar-body error'() {
        def lexer = lexer(' ')
        lexer.pushMode(GSTRING_TYPE_SELECTOR_MODE)
        def err = shouldFail(GroovySyntaxError) {
            lexer.nextToken()
        }
        assertTrue err.message.startsWith(ILLEGAL_AFTER_DOLLAR + ':')
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private static GroovyLangLexer lexer(String source) {
        new GroovyLangLexer(CharStreams.fromString(source))
    }

    /** Consume tokens until EOF or an exception escapes. */
    private static void drain(GroovyLangLexer lexer) {
        Token t
        while ((t = lexer.nextToken()).type != Token.EOF) {
            // keep draining
        }
    }
}
