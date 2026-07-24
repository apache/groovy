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

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.FailedPredicateException;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.Interval;

/**
 * Shared friendly recognition diagnostics for Parrot error strategies.
 * <p>
 * Subclasses choose control flow ({@link DescriptiveErrorStrategy} fail-fast vs
 * {@link RecoveringDescriptiveErrorStrategy} multi-error resync). Reporting —
 * including {@link MissingDelimiterDiagnostic} — stays here so both modes share
 * one message path.
 * </p>
 */
abstract class AbstractFriendlyErrorStrategy extends DefaultErrorStrategy {

    private final CharStream charStream;

    AbstractFriendlyErrorStrategy(final CharStream charStream) {
        this.charStream = charStream;
    }

    /**
     * Prefer a precise "Missing …" delimiter diagnostic when the token stream
     * clearly indicates an unclosed / incomplete construct; otherwise fall
     * back to the generic message.
     */
    private void reportFriendlyError(final Parser recognizer, final RecognitionException e, final String fallbackMessage) {
        MissingDelimiterDiagnostic.Hit hit = null;
        try {
            // Incomplete / synthetic contexts can leave token indices out of range.
            hit = MissingDelimiterDiagnostic.locate(recognizer.getInputStream(), e);
        } catch (IndexOutOfBoundsException | IllegalArgumentException ignored) {
            // Fall through to the generic message. Catch only locate()'s known
            // defensive failures — never listener-side fatals (e.g. addFatalError).
        }
        if (hit != null) {
            recognizer.notifyErrorListeners(hit.at, hit.message, e);
            return;
        }
        notifyErrorListeners(recognizer, fallbackMessage, e);
    }

    protected String createNoViableAlternativeErrorMessage(final Parser recognizer, final NoViableAltException e) {
        TokenStream tokens = recognizer.getInputStream();
        String input;
        if (tokens != null) {
            if (e.getStartToken().getType() == Token.EOF) {
                input = "<EOF>";
            } else {
                input = charStream.getText(Interval.of(e.getStartToken().getStartIndex(), e.getOffendingToken().getStopIndex()));
            }
        } else {
            input = "<unknown input>";
        }

        return "Unexpected input: " + escapeWSAndQuote(input);
    }

    @Override
    protected void reportNoViableAlternative(final Parser recognizer, final NoViableAltException e) {
        reportFriendlyError(recognizer, e, createNoViableAlternativeErrorMessage(recognizer, e));
    }

    protected String createInputMismatchErrorMessage(final Parser recognizer, final InputMismatchException e) {
        return "Unexpected input: " + getTokenErrorDisplay(e.getOffendingToken(recognizer));
    }

    @Override
    protected void reportInputMismatch(final Parser recognizer, final InputMismatchException e) {
        reportFriendlyError(recognizer, e, createInputMismatchErrorMessage(recognizer, e));
    }

    protected String createFailedPredicateErrorMessage(final Parser recognizer, final FailedPredicateException e) {
        return e.getMessage();
    }

    @Override
    protected void reportFailedPredicate(final Parser recognizer, final FailedPredicateException e) {
        notifyErrorListeners(recognizer, createFailedPredicateErrorMessage(recognizer, e), e);
    }
}
