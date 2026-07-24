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
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.apache.groovy.parser.antlr4.GroovyLangLexer;
import org.apache.groovy.parser.antlr4.GroovyLangParser;
import org.apache.groovy.parser.antlr4.GroovyParser;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        assertTrue(pce.getCause() == cause);
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
        assertTrue(messages.stream().anyMatch(m ->
                        m.contains("Unexpected input") || m.startsWith("Missing ")),
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

        ParseCancellationException pce = assertThrows(ParseCancellationException.class,
                () -> strategy.recover(parser, new InputMismatchException(parser)));
        assertInstanceOf(InputMismatchException.class, pce.getCause());
        assertFalse(messages.isEmpty(), "LL IME recover must report before cancel: " + messages);
        assertTrue(messages.stream().anyMatch(m ->
                        m.contains("Unexpected input") || m.startsWith("Missing ")),
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

        assertThrows(ParseCancellationException.class,
                () -> strategy.recover(parser, new InputMismatchException(parser)));
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
        assertTrue(pce.getCause() == other);
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
    void reportFriendlyErrorFallsBackWhenLocateThrows() {
        // locate() walks the token stream; a throwing stream must not escape the
        // strategy — fall back to the generic mismatch message instead.
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
        parser.setInputStream(new ThrowingTokenStream());
        strategy.exposeReportInputMismatch(parser, ime);

        assertFalse(messages.isEmpty(), "fallback report must still notify: " + messages);
        assertTrue(messages.stream().anyMatch(m -> m.contains("Unexpected input")),
                "generic fallback expected, got: " + messages);
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
        strategy.sync(parser);
    }

    // --- helpers ----------------------------------------------------------------

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
