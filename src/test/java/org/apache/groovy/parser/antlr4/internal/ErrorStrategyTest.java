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
package org.apache.groovy.parser.antlr4.internal;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.FailedPredicateException;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.atn.AbstractPredicateTransition;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNState;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.IntervalSet;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.apache.groovy.parser.antlr4.GroovyLangLexer;
import org.apache.groovy.parser.antlr4.GroovyLangParser;
import org.apache.groovy.parser.antlr4.GroovyParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Behavioural contracts for GROOVY-9192 error strategies (package-local).
 * <p>
 * End-to-end multi-error collection lives in {@code Groovy9192}; this class
 * locks strategy-level control flow and reporting branches that are awkward
 * to assert through the full compilation pipeline alone.
 * </p>
 */
final class ErrorStrategyTest {

    //--------------------------------------------------------------------------
    // Fail-fast cancel contracts
    //--------------------------------------------------------------------------

    @Test
    void failFastRecoverCancelsWithCause() {
        var charStream = CharStreams.fromString("}");
        var strategy = new DescriptiveErrorStrategy(charStream);
        var parser = parser(charStream, strategy, PredictionMode.SLL);
        parser.setContext(new ParserRuleContext());

        InputMismatchException cause = new InputMismatchException(parser);
        ParseCancellationException pce = assertThrows(ParseCancellationException.class,
                () -> strategy.recover(parser, cause));
        assertInstanceOf(InputMismatchException.class, pce.getCause());
        assertSame(cause, pce.getCause());
    }

    @Test
    void failFastRecoverInlineCancelsWithInputMismatch() {
        var charStream = CharStreams.fromString("}");
        var strategy = new DescriptiveErrorStrategy(charStream);
        var parser = parser(charStream, strategy, PredictionMode.SLL);
        parser.setContext(new ParserRuleContext());

        ParseCancellationException pce = assertThrows(ParseCancellationException.class,
                () -> strategy.recoverInline(parser));
        assertInstanceOf(InputMismatchException.class, pce.getCause());
    }

    @Test
    void failFastRecoverInlineViaParserMatchCancels() {
        var charStream = CharStreams.fromString("}");
        var strategy = new DescriptiveErrorStrategy(charStream);
        var parser = parser(charStream, strategy, PredictionMode.LL);
        parser.setContext(new ParserRuleContext());
        assertThrows(ParseCancellationException.class,
                () -> parser.match(GroovyParser.Identifier));
    }

    @Test
    void failFastRecoverUnderLlReportsNoViableAltAndCancels() {
        var charStream = CharStreams.fromString("}");
        var strategy = new DescriptiveErrorStrategy(charStream);
        var messages = new ArrayList<String>();
        var tokens = new CommonTokenStream(new GroovyLangLexer(charStream));
        tokens.fill();
        var parser = new GroovyLangParser(tokens);
        parser.setErrorHandler(strategy);
        parser.getInterpreter().setPredictionMode(PredictionMode.LL);
        attachListener(parser, messages);
        parser.setContext(new ParserRuleContext());

        Token tok = tokens.get(0);
        var nvae = new NoViableAltException(parser, tokens, tok, tok, null, parser.getContext());
        ParseCancellationException pce = assertThrows(ParseCancellationException.class,
                () -> strategy.recover(parser, nvae));
        assertInstanceOf(NoViableAltException.class, pce.getCause());
        assertFalse(messages.isEmpty(), "LL recover must report before cancel");
        assertTrue(messages.stream().anyMatch(ErrorStrategyTest::isNonAntlrJargon),
                "friendly diagnostic expected, got: " + messages);
    }

    @Test
    void failFastRecoverUnderLlReportsInputMismatchAndCancels() {
        var charStream = CharStreams.fromString("}");
        var strategy = new DescriptiveErrorStrategy(charStream);
        var messages = new ArrayList<String>();
        var parser = parser(charStream, strategy, PredictionMode.LL);
        attachListener(parser, messages);
        parser.setContext(new ParserRuleContext());

        InputMismatchException cause = new InputMismatchException(parser);
        ParseCancellationException pce = assertThrows(ParseCancellationException.class,
                () -> strategy.recover(parser, cause));
        assertInstanceOf(InputMismatchException.class, pce.getCause());
        assertFalse(messages.isEmpty(), "LL IME recover must report before cancel: " + messages);
        assertTrue(messages.stream().anyMatch(ErrorStrategyTest::isNonAntlrJargon),
                "friendly diagnostic expected, got: " + messages);
    }

    @Test
    void failFastRecoverUnderLlReportsFailedPredicateAndCancels() {
        var charStream = CharStreams.fromString("class C {}");
        var strategy = new DescriptiveErrorStrategy(charStream);
        var messages = new ArrayList<String>();
        var parser = parser(charStream, strategy, PredictionMode.LL);
        attachListener(parser, messages);

        FailedPredicateException fpe = requireFailedPredicateException(parser);
        parser.setContext(new ParserRuleContext());

        ParseCancellationException pce = assertThrows(ParseCancellationException.class,
                () -> strategy.recover(parser, fpe));
        assertInstanceOf(FailedPredicateException.class, pce.getCause());
        assertFalse(messages.isEmpty(), "LL FPE recover must notify listeners: " + messages);
        assertTrue(messages.contains(fpe.getMessage()),
                "listener should receive FPE message, got: " + messages);
    }

    @Test
    void failFastRecoverUnderSllDoesNotReportBeforeCancel() {
        // SLL cancel marks contexts and bails without diagnostics (reportError is LL-only).
        var charStream = CharStreams.fromString("}");
        var strategy = new DescriptiveErrorStrategy(charStream);
        var messages = new ArrayList<String>();
        var parser = parser(charStream, strategy, PredictionMode.SLL);
        attachListener(parser, messages);
        parser.setContext(new ParserRuleContext());

        InputMismatchException cause = new InputMismatchException(parser);
        assertThrows(ParseCancellationException.class, () -> strategy.recover(parser, cause));
        assertTrue(messages.isEmpty(), "SLL recover must not report: " + messages);
    }

    @Test
    void failFastRecoverUnderLlWithOtherRecognitionExceptionCancelsWithoutSpecializedReport() {
        // cancel()'s instanceof chain: NVAE / IME / FPE only. Other RecognitionException
        // types still cancel, without a specialized report branch.
        var charStream = CharStreams.fromString("}");
        var strategy = new DescriptiveErrorStrategy(charStream);
        var messages = new ArrayList<String>();
        var parser = parser(charStream, strategy, PredictionMode.LL);
        attachListener(parser, messages);
        parser.setContext(new ParserRuleContext());

        RecognitionException other = new RecognitionException(parser, parser.getInputStream(), parser.getContext());
        ParseCancellationException pce = assertThrows(ParseCancellationException.class,
                () -> strategy.recover(parser, other));
        assertSame(other, pce.getCause());
        assertTrue(messages.isEmpty(), "no specialized report for generic RE: " + messages);
    }

    @Test
    void failFastSyncIsNoOp() {
        var charStream = CharStreams.fromString("1");
        var strategy = new DescriptiveErrorStrategy(charStream);
        var parser = parser(charStream, strategy, PredictionMode.LL);
        int indexBefore = parser.getInputStream().index();
        strategy.sync(parser);
        assertEquals(indexBefore, parser.getInputStream().index());
    }

    //--------------------------------------------------------------------------
    // Shared reporting (AbstractFriendlyErrorStrategy)
    //--------------------------------------------------------------------------

    @Test
    void nvaeMessageUsesEofMarker() {
        var charStream = CharStreams.fromString("xy");
        var strategy = new StrategyProbe(charStream);
        var tokens = new CommonTokenStream(new GroovyLangLexer(charStream));
        tokens.fill();
        var parser = new GroovyLangParser(tokens);

        Token eof = new CommonToken(Token.EOF);
        var nvae = new NoViableAltException(parser, tokens, eof, eof, null, parser.getContext());
        assertTrue(strategy.exposeCreateNvaeMessage(parser, nvae).contains("<EOF>"),
                strategy.exposeCreateNvaeMessage(parser, nvae));
    }

    @Test
    void nvaeMessageUsesSnippetForNonEofToken() {
        var charStream = CharStreams.fromString("xy");
        var strategy = new StrategyProbe(charStream);
        var tokens = new CommonTokenStream(new GroovyLangLexer(charStream));
        tokens.fill();
        var parser = new GroovyLangParser(tokens);

        Token tok = tokens.get(0);
        var nvae = new NoViableAltException(parser, tokens, tok, tok, null, parser.getContext());
        String msg = strategy.exposeCreateNvaeMessage(parser, nvae);
        assertTrue(msg.startsWith("Unexpected input:"), msg);
        assertFalse(msg.contains("<unknown input>"), msg);
        assertFalse(msg.contains("<EOF>"), msg);
    }

    @Test
    void nvaeMessageUsesUnknownInputWhenTokenStreamNull() {
        var charStream = CharStreams.fromString("1");
        var strategy = new StrategyProbe(charStream);
        var tokens = new CommonTokenStream(new GroovyLangLexer(charStream));
        tokens.fill();
        var real = new GroovyLangParser(tokens);
        Token tok = tokens.get(0);
        var nvae = new NoViableAltException(real, tokens, tok, tok, null, real.getContext());

        var nullStreamParser = new NullTokenStreamParser(real.getATN());
        // escapeWSAndQuote wraps the snippet in single quotes.
        assertEquals("Unexpected input: '<unknown input>'",
                strategy.exposeCreateNvaeMessage(nullStreamParser, nvae));
    }

    @Test
    void reportNoViableAndInputMismatchNotifyListeners() {
        var charStream = CharStreams.fromString("x");
        var strategy = new StrategyProbe(charStream);
        var messages = new ArrayList<String>();
        var tokens = new CommonTokenStream(new GroovyLangLexer(charStream));
        tokens.fill();
        var parser = new GroovyLangParser(tokens);
        parser.setErrorHandler(strategy);
        attachListener(parser, messages);

        Token tok = tokens.get(0);
        var nvae = new NoViableAltException(parser, tokens, tok, tok, null, parser.getContext());
        strategy.exposeReportNoViable(parser, nvae);
        assertFalse(messages.isEmpty(), "NVAE report must notify: " + messages);

        messages.clear();
        strategy.exposeReportInputMismatch(parser, new InputMismatchException(parser));
        assertFalse(messages.isEmpty(), "IME report must notify: " + messages);
    }

    @Test
    void reportFailedPredicateNotifiesWithPredicateMessage() {
        var charStream = CharStreams.fromString("class C {}");
        var strategy = new StrategyProbe(charStream);
        var messages = new ArrayList<String>();
        var parser = parser(charStream, strategy, PredictionMode.LL);
        attachListener(parser, messages);

        FailedPredicateException fpe = requireFailedPredicateException(parser);
        assertEquals(fpe.getMessage(), strategy.exposeCreateFailedPredicateMessage(parser, fpe));
        strategy.exposeReportFailedPredicate(parser, fpe);
        assertEquals(List.of(fpe.getMessage()), messages);
    }

    @Test
    void reportFriendlyErrorFallsBackWhenLocateThrowsIndexOutOfBounds() {
        // locate() walks the token stream; a throwing stream must not escape the
        // strategy — fall back to the generic mismatch message instead.
        assertFriendlyFallbackAfterLocateFailure(new ThrowingTokenStream());
    }

    @Test
    void reportFriendlyErrorFallsBackWhenLocateThrowsIllegalArgument() {
        // Same defensive catch as IndexOutOfBoundsException (IllegalArgumentException).
        assertFriendlyFallbackAfterLocateFailure(new ThrowingTokenStream(new IllegalArgumentException("test")));
    }

    private static void assertFriendlyFallbackAfterLocateFailure(TokenStream throwingStream) {
        var charStream = CharStreams.fromString("}");
        var strategy = new StrategyProbe(charStream);
        var messages = new ArrayList<String>();
        var tokens = new CommonTokenStream(new GroovyLangLexer(charStream));
        tokens.fill();
        var parser = new GroovyLangParser(tokens);
        parser.setErrorHandler(strategy);
        attachListener(parser, messages);
        parser.setContext(new ParserRuleContext());

        // Build the exception while the stream is still valid, then swap in a
        // throwing stream so locate() fails defensively during reporting.
        InputMismatchException ime = new InputMismatchException(parser);
        parser.setInputStream(throwingStream);
        strategy.exposeReportInputMismatch(parser, ime);

        assertFalse(messages.isEmpty(), "fallback report must still notify: " + messages);
        assertTrue(messages.stream().anyMatch(ErrorStrategyTest::isNonAntlrJargon),
                "fallback report must still notify with a diagnostic, got: " + messages);
    }

    @Test
    void reportFriendlyErrorDoesNotRedispatchWhenListenerThrows() {
        // Single dispatch is outside the defensive catch: a listener IAE must
        // propagate, not be treated as a locate/refine failure and retried.
        var charStream = CharStreams.fromString("x");
        var strategy = new StrategyProbe(charStream);
        var messages = new ArrayList<String>();
        var tokens = new CommonTokenStream(new GroovyLangLexer(charStream));
        tokens.fill();
        var parser = new GroovyLangParser(tokens);
        parser.setErrorHandler(strategy);
        parser.removeErrorListeners();
        parser.addErrorListener(new BaseErrorListener() {
            private int calls;

            @Override
            public <T extends Token> void syntaxError(Recognizer<T, ?> recognizer, T offendingSymbol, int line,
                                                      int charPositionInLine, String msg, RecognitionException e) {
                calls++;
                messages.add(msg);
                if (calls == 1) {
                    throw new IllegalArgumentException("listener");
                }
            }
        });

        Token tok = tokens.get(0);
        var nvae = new NoViableAltException(parser, tokens, tok, tok, null, parser.getContext());
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> strategy.exposeReportNoViable(parser, nvae));
        assertEquals("listener", thrown.getMessage());
        assertEquals(1, messages.size(), "listener must not be invoked a second time, got: " + messages);
    }

    //--------------------------------------------------------------------------
    // Factory / recovery strategy
    //--------------------------------------------------------------------------

    @Test
    void factorySelectsStrategies() {
        var cs = CharStreams.fromString("1");
        assertInstanceOf(RecoveringDescriptiveErrorStrategy.class,
                DescriptiveErrorStrategy.create(cs, true));
        assertInstanceOf(DescriptiveErrorStrategy.class,
                DescriptiveErrorStrategy.create(cs, false));
    }

    @Test
    void recoveringStrategyCompletesMultiFaultSnippetWithoutCancel() {
        var charStream = CharStreams.fromString("class C { def x = ( } class D {}");
        var strategy = new RecoveringDescriptiveErrorStrategy(charStream);
        var parser = parser(charStream, strategy, PredictionMode.LL);
        assertNotNull(parser.compilationUnit());
    }

    @Test
    void recoveringStrategySyncDoesNotThrow() {
        var charStream = CharStreams.fromString("println 1");
        var strategy = new RecoveringDescriptiveErrorStrategy(charStream);
        var parser = parser(charStream, strategy, PredictionMode.LL);
        parser.setState(0);
        parser.setContext(new ParserRuleContext());
        int indexBefore = parser.getInputStream().index();
        strategy.sync(parser);
        // DefaultErrorStrategy.sync may advance for recovery; just assert it completes
        // and leaves a well-defined stream index (regression: no throw / no cancel).
        assertTrue(parser.getInputStream().index() >= indexBefore);
    }

    /**
     * Strategy reporting must not leak ANTLR's stock jargon onto the listener.
     * The exact sentence is locked by {@code CommonSyntaxErrorTest} / delimiter tests.
     */
    private static boolean isNonAntlrJargon(final String m) {
        return !m.contains("no viable alternative") && !m.startsWith("mismatched input");
    }

    @Test
    void refineFallbackPrefersKeywordOverSoleExpected() {
        Token elseTok = token(GroovyParser.ELSE, "else");
        RecognitionException e = stubException(setOf(GroovyParser.LPAREN), elseTok);
        assertEquals("'else' without 'if'",
                AbstractFriendlyErrorStrategy.refineFallbackMessage(e, "Unexpected input: 'else'"));
    }

    @Test
    void refineFallbackNamesSoleExpectedPunctuation() {
        Token trueTok = token(GroovyParser.BooleanLiteral, "true");
        RecognitionException e = stubException(setOf(GroovyParser.LPAREN), trueTok);
        assertEquals("Missing '('",
                AbstractFriendlyErrorStrategy.refineFallbackMessage(e, "Unexpected input: 'true'"));
        assertEquals("Missing ':'", AbstractFriendlyErrorStrategy.soleExpectedMessage(GroovyParser.COLON));
        assertEquals("Missing '>'", AbstractFriendlyErrorStrategy.soleExpectedMessage(GroovyParser.GT));
        assertEquals("Missing 'while'", AbstractFriendlyErrorStrategy.soleExpectedMessage(GroovyParser.WHILE));
        assertEquals("Missing '['", AbstractFriendlyErrorStrategy.soleExpectedMessage(GroovyParser.LBRACK));
        assertEquals("Missing '{'", AbstractFriendlyErrorStrategy.soleExpectedMessage(GroovyParser.LBRACE));
        assertEquals("Missing ';'", AbstractFriendlyErrorStrategy.soleExpectedMessage(GroovyParser.SEMI));
        assertNull(AbstractFriendlyErrorStrategy.soleExpectedMessage(GroovyParser.RPAREN));
        assertNull(AbstractFriendlyErrorStrategy.soleExpectedMessage(GroovyParser.Identifier));
    }

    @Test
    void refineFallbackUnexpectedEof() {
        Token eof = token(Token.EOF, "<EOF>");
        RecognitionException e = stubException(new IntervalSet(), eof);
        assertEquals("Unexpected end of input",
                AbstractFriendlyErrorStrategy.refineFallbackMessage(e, "Unexpected input: '<EOF>'"));
    }

    @Test
    void refineFallbackEofWithSoleColonPrefersMissingColon() {
        Token eof = token(Token.EOF, "<EOF>");
        RecognitionException e = stubException(setOf(GroovyParser.COLON), eof);
        assertEquals("Missing ':'",
                AbstractFriendlyErrorStrategy.refineFallbackMessage(e, "Unexpected input: '<EOF>'"));
    }

    @Test
    void refineFallbackNullExceptionKeepsGeneric() {
        assertEquals("generic", AbstractFriendlyErrorStrategy.refineFallbackMessage(null, "generic"));
        assertNull(AbstractFriendlyErrorStrategy.soleExpectedMessage((RecognitionException) null));
    }

    @Test
    void refineFallbackSwallowsInvalidExpectedTokensState() {
        RecognitionException e = new RecognitionException(null, null, null) {
            @Override
            public IntervalSet getExpectedTokens() {
                throw new IllegalArgumentException("Invalid state number.");
            }

            @Override
            public Token getOffendingToken() {
                return token(GroovyParser.Identifier, "x");
            }
        };
        assertNull(AbstractFriendlyErrorStrategy.soleExpectedMessage(e));
        assertEquals("generic", AbstractFriendlyErrorStrategy.refineFallbackMessage(e, "generic"));
    }

    @Test
    void refineFallbackIgnoresMultiTokenExpectedSet() {
        IntervalSet multi = setOf(GroovyParser.LPAREN, GroovyParser.COLON);
        Token x = token(GroovyParser.Identifier, "x");
        RecognitionException e = stubException(multi, x);
        assertEquals("Unexpected input: 'x'",
                AbstractFriendlyErrorStrategy.refineFallbackMessage(e, "Unexpected input: 'x'"));
        assertNull(AbstractFriendlyErrorStrategy.soleExpectedMessage(stubException(null, x)));
    }

    @Test
    void keywordMessages() {
        assertEquals("'const' is not supported; use 'val' or 'static final' instead",
                AbstractFriendlyErrorStrategy.keywordMessage(GroovyParser.CONST));
        assertEquals("'goto' is not supported",
                AbstractFriendlyErrorStrategy.keywordMessage(GroovyParser.GOTO));
        assertEquals("'threadsafe' is not supported",
                AbstractFriendlyErrorStrategy.keywordMessage(GroovyParser.THREADSAFE));
        assertEquals("'else' without 'if'",
                AbstractFriendlyErrorStrategy.keywordMessage(GroovyParser.ELSE));
        assertEquals("'catch' without 'try'",
                AbstractFriendlyErrorStrategy.keywordMessage(GroovyParser.CATCH));
        assertEquals("'finally' without 'try'",
                AbstractFriendlyErrorStrategy.keywordMessage(GroovyParser.FINALLY));
        assertEquals("'case' outside of switch",
                AbstractFriendlyErrorStrategy.keywordMessage(GroovyParser.CASE));
        assertNull(AbstractFriendlyErrorStrategy.keywordMessage(GroovyParser.DEFAULT),
                "default as offender is a method/annotation keyword, not 'outside of switch'");
        assertNull(AbstractFriendlyErrorStrategy.keywordMessage(GroovyParser.Identifier));
    }

    @Test
    void refineFallbackIgnoresNlWhenSoleExpectedRemains() {
        Token eof = token(Token.EOF, "<EOF>");
        RecognitionException e = stubException(setOf(GroovyParser.WHILE, GroovyParser.NL), eof);
        assertEquals("Missing 'while'",
                AbstractFriendlyErrorStrategy.refineFallbackMessage(e, "Unexpected input: '<EOF>'"));
        assertEquals(GroovyParser.WHILE,
                AbstractFriendlyErrorStrategy.soleNonNlToken(setOf(GroovyParser.WHILE, GroovyParser.NL)));
        assertEquals(Integer.MIN_VALUE,
                AbstractFriendlyErrorStrategy.soleNonNlToken(setOf(GroovyParser.NL)));
        assertEquals(Integer.MIN_VALUE,
                AbstractFriendlyErrorStrategy.soleNonNlToken(setOf(GroovyParser.WHILE, GroovyParser.LPAREN)));
        assertEquals(GroovyParser.LPAREN,
                AbstractFriendlyErrorStrategy.soleNonNlToken(setOf(GroovyParser.LPAREN)));
        assertEquals(Integer.MIN_VALUE,
                AbstractFriendlyErrorStrategy.soleNonNlToken(new IntervalSet()));
        assertEquals(Integer.MIN_VALUE,
                AbstractFriendlyErrorStrategy.soleNonNlToken(null));
    }

    @Test
    void refineFallbackNullOffendingStillUsesSoleExpected() {
        RecognitionException e = stubException(setOf(GroovyParser.LPAREN), null);
        assertEquals("Missing '('",
                AbstractFriendlyErrorStrategy.refineFallbackMessage(e, "generic"));
        assertNull(AbstractFriendlyErrorStrategy.misplacedDefaultClause(e, null));
        assertNull(AbstractFriendlyErrorStrategy.misplacedDefaultClause(null, token(GroovyParser.COLON, ":")));
    }

    @Test
    void refineFallbackSwallowsIndexOutOfBoundsFromExpectedTokens() {
        RecognitionException e = new RecognitionException(null, null, null) {
            @Override
            public IntervalSet getExpectedTokens() {
                throw new IndexOutOfBoundsException("test");
            }

            @Override
            public Token getOffendingToken() {
                return token(GroovyParser.Identifier, "x");
            }
        };
        assertNull(AbstractFriendlyErrorStrategy.soleExpectedMessage(e));
        assertEquals("generic", AbstractFriendlyErrorStrategy.refineFallbackMessage(e, "generic"));
    }

    @Test
    void refineFallbackDefaultColonLooksBack() {
        var charStream = CharStreams.fromString("default: x = 1");
        var tokens = new CommonTokenStream(new GroovyLangLexer(charStream));
        tokens.fill();
        Token colon = tokens.get(1);
        assertEquals(GroovyParser.COLON, colon.getType());
        RecognitionException e = stubException(new IntervalSet(), colon, tokens);
        assertEquals("'default' outside of switch",
                AbstractFriendlyErrorStrategy.refineFallbackMessage(e, "Unexpected input: ':'"));
    }

    @Test
    void refineFallbackDefaultArrowLooksBack() {
        var charStream = CharStreams.fromString("default -> x");
        var tokens = new CommonTokenStream(new GroovyLangLexer(charStream));
        tokens.fill();
        Token arrow = null;
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).getType() == GroovyParser.ARROW) {
                arrow = tokens.get(i);
                break;
            }
        }
        assertNotNull(arrow);
        RecognitionException e = stubException(new IntervalSet(), arrow, tokens);
        assertEquals("'default' outside of switch",
                AbstractFriendlyErrorStrategy.refineFallbackMessage(e, "Unexpected input: '->'"));
    }

    @Test
    void refineFallbackColonAfterIdentifierIsNotDefaultClause() {
        var charStream = CharStreams.fromString("label: x = 1");
        var tokens = new CommonTokenStream(new GroovyLangLexer(charStream));
        tokens.fill();
        Token colon = tokens.get(1);
        RecognitionException e = stubException(new IntervalSet(), colon, tokens);
        assertEquals("generic",
                AbstractFriendlyErrorStrategy.refineFallbackMessage(e, "generic"));
        assertNull(AbstractFriendlyErrorStrategy.misplacedDefaultClause(e, colon));
    }

    @Test
    void misplacedDefaultClauseSkipsNewlineBetweenDefaultAndColon() {
        var charStream = CharStreams.fromString("default\n: x = 1");
        var tokens = new CommonTokenStream(new GroovyLangLexer(charStream));
        tokens.fill();
        Token colon = null;
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).getType() == GroovyParser.COLON) {
                colon = tokens.get(i);
                break;
            }
        }
        assertNotNull(colon);
        RecognitionException e = stubException(new IntervalSet(), colon, tokens);
        assertEquals("'default' outside of switch",
                AbstractFriendlyErrorStrategy.misplacedDefaultClause(e, colon));
    }

    @Test
    void misplacedDefaultClauseIgnoresNonColonOffender() {
        Token brace = token(GroovyParser.RBRACE, "}");
        RecognitionException e = stubException(new IntervalSet(), brace);
        assertNull(AbstractFriendlyErrorStrategy.misplacedDefaultClause(e, brace));
    }

    @Test
    void misplacedDefaultClauseNullStream() {
        CommonToken colon = token(GroovyParser.COLON, ":");
        colon.setTokenIndex(1);
        RecognitionException e = stubException(new IntervalSet(), colon);
        assertNull(AbstractFriendlyErrorStrategy.misplacedDefaultClause(e, colon));
    }

    @Test
    void misplacedDefaultClauseIgnoresUnindexedToken() {
        CommonToken colon = token(GroovyParser.COLON, ":");
        colon.setTokenIndex(0);
        var charStream = CharStreams.fromString("default: x");
        var tokens = new CommonTokenStream(new GroovyLangLexer(charStream));
        tokens.fill();
        RecognitionException e = stubException(new IntervalSet(), colon, tokens);
        assertNull(AbstractFriendlyErrorStrategy.misplacedDefaultClause(e, colon));
    }

    @Test
    void unexpectedSafeIndexWithoutReceiver() {
        var charStream = CharStreams.fromString("?[0]");
        var tokens = new CommonTokenStream(new GroovyLangLexer(charStream));
        tokens.fill();
        Token safe = tokens.get(0);
        assertEquals(GroovyParser.SAFE_INDEX, safe.getType());
        RecognitionException e = stubException(new IntervalSet(), safe, tokens);
        assertEquals("'?[' requires an expression before it",
                AbstractFriendlyErrorStrategy.refineFallbackMessage(e, "Unexpected input: '?['"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "foo(?[0])",
            "x = ?[0]",
            "x+?[0]",
            "foo.?[0]",
            "a?.?[0]",
            "a*.?[0]",
            "a??.?[0]",
            "a.&?[0]",
            "a::?[0]",
            "a.@?[0]",
            "a\n?[0]",
            "/*c*/?[0]",
            "if?[0]",
            "a??[0]"
    })
    void unexpectedSafeIndexAfterNonReceiverIsAMissingReceiver(final String src) {
        assertSafeIndexMessage(src);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "a?[0,]",
            "foo.if?[0,]",
            "foo.\nif?[0,]",
            "foo?.if?[0,]",
            "(a)?[0,]",
            "a[0]?[1,]",
            "{1}?[0,]",
            "a<b>?[0,]",
            "Foo?[0,]",
            "1?[0,]",
            "1.0?[0,]",
            "'s'?[0,]",
            "true?[0,]",
            "null?[0,]",
            "this?[0,]",
            "super?[0,]",
            "int?[0,]",
            "void?[0,]",
            "\"$x\"?[0,]"
    })
    void unexpectedSafeIndexAfterPathEndingTokenIsNotAMissingReceiver(final String src) {
        var tokens = lex(src);
        Token safe = requireSafeIndex(tokens);
        assertNull(AbstractFriendlyErrorStrategy.unexpectedSafeIndex(
                stubException(new IntervalSet(), safe, tokens), safe), src);
    }

    @Test
    void unexpectedSafeIndexPreviousTokenCanBeGt() {
        var tokens = lex("a<b>?[0,]");
        Token safe = requireSafeIndex(tokens);
        // relational or type-argument closer; either way GT can end a path
        assertEquals(GroovyParser.GT, tokens.get(safe.getTokenIndex() - 1).getType());
    }

    @Test
    void canEndPathExpressionCoversPrimariesAndClosers() {
        assertTrue(AbstractFriendlyErrorStrategy.canEndPathExpression(GroovyParser.Identifier));
        assertTrue(AbstractFriendlyErrorStrategy.canEndPathExpression(GroovyParser.CapitalizedIdentifier));
        assertTrue(AbstractFriendlyErrorStrategy.canEndPathExpression(GroovyParser.IntegerLiteral));
        assertTrue(AbstractFriendlyErrorStrategy.canEndPathExpression(GroovyParser.FloatingPointLiteral));
        assertTrue(AbstractFriendlyErrorStrategy.canEndPathExpression(GroovyParser.StringLiteral));
        assertTrue(AbstractFriendlyErrorStrategy.canEndPathExpression(GroovyParser.BooleanLiteral));
        assertTrue(AbstractFriendlyErrorStrategy.canEndPathExpression(GroovyParser.NullLiteral));
        assertTrue(AbstractFriendlyErrorStrategy.canEndPathExpression(GroovyParser.GStringEnd));
        assertTrue(AbstractFriendlyErrorStrategy.canEndPathExpression(GroovyParser.RPAREN));
        assertTrue(AbstractFriendlyErrorStrategy.canEndPathExpression(GroovyParser.RBRACK));
        assertTrue(AbstractFriendlyErrorStrategy.canEndPathExpression(GroovyParser.RBRACE));
        assertTrue(AbstractFriendlyErrorStrategy.canEndPathExpression(GroovyParser.THIS));
        assertTrue(AbstractFriendlyErrorStrategy.canEndPathExpression(GroovyParser.SUPER));
        assertTrue(AbstractFriendlyErrorStrategy.canEndPathExpression(GroovyParser.BuiltInPrimitiveType));
        assertTrue(AbstractFriendlyErrorStrategy.canEndPathExpression(GroovyParser.VOID));
        assertTrue(AbstractFriendlyErrorStrategy.canEndPathExpression(GroovyParser.GT));
        assertFalse(AbstractFriendlyErrorStrategy.canEndPathExpression(GroovyParser.LPAREN));
        assertFalse(AbstractFriendlyErrorStrategy.canEndPathExpression(GroovyParser.ASSIGN));
        assertFalse(AbstractFriendlyErrorStrategy.canEndPathExpression(GroovyParser.QUESTION));
        assertFalse(AbstractFriendlyErrorStrategy.canEndPathExpression(GroovyParser.NL));
        assertFalse(AbstractFriendlyErrorStrategy.canEndPathExpression(GroovyParser.DOT));
        assertFalse(AbstractFriendlyErrorStrategy.canEndPathExpression(GroovyParser.SAFE_INDEX));
    }

    @Test
    void unexpectedSafeIndexNulls() {
        assertNull(AbstractFriendlyErrorStrategy.unexpectedSafeIndex(null, token(GroovyParser.SAFE_INDEX, "?[")));
        assertNull(AbstractFriendlyErrorStrategy.unexpectedSafeIndex(stubException(new IntervalSet(), null), null));
        Token ident = token(GroovyParser.Identifier, "a");
        assertNull(AbstractFriendlyErrorStrategy.unexpectedSafeIndex(stubException(new IntervalSet(), ident), ident));
        CommonToken safe = token(GroovyParser.SAFE_INDEX, "?[");
        safe.setTokenIndex(0);
        assertEquals("'?[' requires an expression before it",
                AbstractFriendlyErrorStrategy.unexpectedSafeIndex(stubException(new IntervalSet(), safe), safe));
    }

    @Test
    void unexpectedSafeIndexSwallowsStreamFailure() {
        CommonToken safe = token(GroovyParser.SAFE_INDEX, "?[");
        safe.setTokenIndex(1);
        RecognitionException e = stubException(new IntervalSet(), safe, new ThrowingTokenStream());
        assertEquals("'?[' requires an expression before it",
                AbstractFriendlyErrorStrategy.unexpectedSafeIndex(e, safe));
        RecognitionException e2 = stubException(new IntervalSet(), safe,
                new ThrowingTokenStream(new IllegalArgumentException("test")));
        assertEquals("'?[' requires an expression before it",
                AbstractFriendlyErrorStrategy.unexpectedSafeIndex(e2, safe));
    }

    @Test
    void misplacedDefaultClauseSwallowsStreamFailure() {
        CommonToken colon = token(GroovyParser.COLON, ":");
        colon.setTokenIndex(1);
        RecognitionException e = stubException(new IntervalSet(), colon, new ThrowingTokenStream());
        assertNull(AbstractFriendlyErrorStrategy.misplacedDefaultClause(e, colon));
        RecognitionException e2 = stubException(new IntervalSet(), colon,
                new ThrowingTokenStream(new IllegalArgumentException("test")));
        assertNull(AbstractFriendlyErrorStrategy.misplacedDefaultClause(e2, colon));
    }

    // --- helpers ----------------------------------------------------------------

    private static void assertSafeIndexMessage(final String src) {
        var tokens = lex(src);
        Token safe = requireSafeIndex(tokens);
        RecognitionException e = stubException(new IntervalSet(), safe, tokens);
        assertEquals("'?[' requires an expression before it",
                AbstractFriendlyErrorStrategy.unexpectedSafeIndex(e, safe));
    }

    private static Token requireSafeIndex(final CommonTokenStream tokens) {
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).getType() == GroovyParser.SAFE_INDEX) {
                return tokens.get(i);
            }
        }
        throw new AssertionError("no SAFE_INDEX in: " + tokens.getText());
    }

    private static CommonTokenStream lex(final String src) {
        var tokens = new CommonTokenStream(new GroovyLangLexer(CharStreams.fromString(src)));
        tokens.fill();
        return tokens;
    }

    private static GroovyLangParser parser(org.antlr.v4.runtime.CharStream charStream,
                                           org.antlr.v4.runtime.ANTLRErrorStrategy strategy,
                                           PredictionMode mode) {
        var parser = new GroovyLangParser(new CommonTokenStream(new GroovyLangLexer(charStream)));
        parser.setErrorHandler(strategy);
        parser.getInterpreter().setPredictionMode(mode);
        parser.removeErrorListeners();
        return parser;
    }

    private static void attachListener(Parser parser, List<String> messages) {
        parser.removeErrorListeners();
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public <T extends Token> void syntaxError(Recognizer<T, ?> recognizer, T offendingSymbol, int line,
                                                      int charPositionInLine, String msg, RecognitionException e) {
                messages.add(msg);
            }
        });
    }

    /**
     * Groovy's grammar always carries semantic predicates; pick the first
     * predicate ATN state so {@link FailedPredicateException} construction is valid.
     */
    private static FailedPredicateException requireFailedPredicateException(Parser parser) {
        ATN atn = parser.getInterpreter().atn;
        for (int i = 0; i < atn.states.size(); i++) {
            ATNState state = atn.states.get(i);
            if (state == null || state.getNumberOfTransitions() == 0) {
                continue;
            }
            try {
                if (state.transition(0) instanceof AbstractPredicateTransition) {
                    parser.setState(i);
                    parser.setContext(new ParserRuleContext());
                    return new FailedPredicateException(parser, "true", "predicate-failed");
                }
            } catch (RuntimeException ignored) {
                // try next state
            }
        }
        throw new AssertionError("Groovy ATN has no AbstractPredicateTransition — grammar change?");
    }

    /** Package subclass exposing protected reporting helpers. */
    private static final class StrategyProbe extends AbstractFriendlyErrorStrategy {
        StrategyProbe(org.antlr.v4.runtime.CharStream charStream) {
            super(charStream);
        }

        String exposeCreateNvaeMessage(Parser p, NoViableAltException e) {
            return createNoViableAlternativeErrorMessage(p, e);
        }

        void exposeReportNoViable(Parser p, NoViableAltException e) {
            reportNoViableAlternative(p, e);
        }

        void exposeReportInputMismatch(Parser p, InputMismatchException e) {
            reportInputMismatch(p, e);
        }

        String exposeCreateFailedPredicateMessage(Parser p, FailedPredicateException e) {
            return createFailedPredicateErrorMessage(p, e);
        }

        void exposeReportFailedPredicate(Parser p, FailedPredicateException e) {
            reportFailedPredicate(p, e);
        }
    }

    private static CommonToken token(int type, String text) {
        CommonToken t = new CommonToken(type, text);
        t.setLine(1);
        t.setCharPositionInLine(0);
        return t;
    }

    private static IntervalSet setOf(int... types) {
        IntervalSet set = new IntervalSet();
        for (int type : types) {
            set.add(type);
        }
        return set;
    }

    private static RecognitionException stubException(IntervalSet expected, Token offending) {
        return stubException(expected, offending, null);
    }

    private static RecognitionException stubException(IntervalSet expected, Token offending, TokenStream input) {
        return new RecognitionException(null, input, null) {
            @Override
            public IntervalSet getExpectedTokens() {
                return expected;
            }

            @Override
            public Token getOffendingToken() {
                return offending;
            }
        };
    }

    /**
     * Parser whose {@link #getInputStream()} is null so NVAE messaging hits the
     * {@code <unknown input>} branch when the strategy reads the stream from the parser.
     */
    private static final class NullTokenStreamParser extends Parser {
        private final ATN atn;

        NullTokenStreamParser(ATN atn) {
            super((TokenStream) null);
            this.atn = atn;
        }

        @Override
        public String[] getRuleNames() {
            return new String[0];
        }

        @Override
        public String getGrammarFileName() {
            return "stub";
        }

        @Override
        public ATN getATN() {
            return atn;
        }

        @Override
        @Deprecated
        public String[] getTokenNames() {
            return new String[0];
        }
    }
}
