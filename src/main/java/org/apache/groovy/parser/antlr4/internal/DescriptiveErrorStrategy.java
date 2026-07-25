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

import org.antlr.v4.runtime.ANTLRErrorStrategy;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.FailedPredicateException;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;

/**
 * Fail-fast parser error strategy with friendly diagnostics (default Parrot mode).
 * <p>
 * Cancels the parse with {@link ParseCancellationException} after reporting,
 * matching {@link org.antlr.v4.runtime.BailErrorStrategy}. {@link #sync} is a
 * no-op so the SLL stage of two-stage parsing stays cheap. Diagnostics are
 * emitted from {@link #recover}: after a failed SLL probe the strategy may still
 * be in ANTLR's internal recovery mode, which suppresses
 * {@link #reportError}, while {@code recover} still runs under LL.
 * </p>
 * <p>
 * For IDE multi-error collection use {@link #create(CharStream, boolean)} with
 * {@code recover == true}, which selects
 * {@link RecoveringDescriptiveErrorStrategy}. Hosts should treat
 * {@link org.codehaus.groovy.control.ErrorCollector} as the multi-error source
 * of truth: recovery may still produce a partial tree that fails later during
 * AST building.
 * </p>
 *
 * @see RecoveringDescriptiveErrorStrategy
 * @see org.codehaus.groovy.control.CompilerConfiguration#ERROR_RECOVERY
 */
public class DescriptiveErrorStrategy extends AbstractFriendlyErrorStrategy {

    /**
     * Select fail-fast or recovering strategy.
     *
     * @param charStream source character stream used for snippet extraction
     * @param recover    {@code true} for multi-error resync; {@code false} for fail-fast
     * @return an error strategy instance (never {@code null})
     */
    public static ANTLRErrorStrategy create(final CharStream charStream, final boolean recover) {
        return recover
                ? new RecoveringDescriptiveErrorStrategy(charStream)
                : new DescriptiveErrorStrategy(charStream);
    }

    /**
     * Fail-fast strategy with friendly diagnostics.
     *
     * @param charStream source character stream used for snippet extraction
     */
    public DescriptiveErrorStrategy(final CharStream charStream) {
        super(charStream);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Mark incomplete contexts, emit a friendly LL diagnostic, then cancel.
     * </p>
     */
    @Override
    public void recover(final Parser recognizer, final RecognitionException e) {
        throw cancel(recognizer, e);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Bail-style: never return a synthetic token; report then cancel.
     * </p>
     */
    @Override
    public Token recoverInline(final Parser recognizer) throws RecognitionException {
        throw cancel(recognizer, new InputMismatchException(recognizer));
    }

    /**
     * Shared fail-fast cancel path for {@link #recover} and {@link #recoverInline}.
     * Marks incomplete contexts, emits an LL diagnostic when applicable, and
     * returns a {@link ParseCancellationException} for the caller to throw
     * (bail-style: control never resumes in the strategy after cancel).
     */
    private ParseCancellationException cancel(final Parser recognizer, final RecognitionException e) {
        for (ParserRuleContext context = recognizer.getContext(); context != null; context = context.getParent()) {
            context.exception = e;
        }

        // Report here (not only via reportError): after a failed SLL stage the
        // shared strategy may still have errorRecoveryMode=true, which makes
        // DefaultErrorStrategy.reportError a no-op on the LL retry.
        if (PredictionMode.LL.equals(recognizer.getInterpreter().getPredictionMode())) {
            if (e instanceof NoViableAltException) {
                reportNoViableAlternative(recognizer, (NoViableAltException) e);
            } else if (e instanceof InputMismatchException) {
                reportInputMismatch(recognizer, (InputMismatchException) e);
            } else if (e instanceof FailedPredicateException) {
                reportFailedPredicate(recognizer, (FailedPredicateException) e);
            }
        }

        return new ParseCancellationException(e);
    }

    /**
     * {@inheritDoc}
     * <p>
     * No-op (matches {@link org.antlr.v4.runtime.BailErrorStrategy}) so SLL stays cheap.
     * </p>
     */
    @Override
    public void sync(final Parser recognizer) {
        // intentionally empty
    }
}
