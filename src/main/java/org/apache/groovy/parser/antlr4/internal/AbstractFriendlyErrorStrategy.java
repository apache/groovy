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
import org.antlr.v4.runtime.misc.IntervalSet;

import java.util.Objects;

import static org.apache.groovy.parser.antlr4.GroovyParser.ARROW;
import static org.apache.groovy.parser.antlr4.GroovyParser.AT;
import static org.apache.groovy.parser.antlr4.GroovyParser.BooleanLiteral;
import static org.apache.groovy.parser.antlr4.GroovyParser.BuiltInPrimitiveType;
import static org.apache.groovy.parser.antlr4.GroovyParser.CASE;
import static org.apache.groovy.parser.antlr4.GroovyParser.CATCH;
import static org.apache.groovy.parser.antlr4.GroovyParser.COLON;
import static org.apache.groovy.parser.antlr4.GroovyParser.CONST;
import static org.apache.groovy.parser.antlr4.GroovyParser.CapitalizedIdentifier;
import static org.apache.groovy.parser.antlr4.GroovyParser.DEFAULT;
import static org.apache.groovy.parser.antlr4.GroovyParser.DOT;
import static org.apache.groovy.parser.antlr4.GroovyParser.ELSE;
import static org.apache.groovy.parser.antlr4.GroovyParser.FINALLY;
import static org.apache.groovy.parser.antlr4.GroovyParser.FloatingPointLiteral;
import static org.apache.groovy.parser.antlr4.GroovyParser.GOTO;
import static org.apache.groovy.parser.antlr4.GroovyParser.GStringEnd;
import static org.apache.groovy.parser.antlr4.GroovyParser.GT;
import static org.apache.groovy.parser.antlr4.GroovyParser.Identifier;
import static org.apache.groovy.parser.antlr4.GroovyParser.IntegerLiteral;
import static org.apache.groovy.parser.antlr4.GroovyParser.LBRACE;
import static org.apache.groovy.parser.antlr4.GroovyParser.LBRACK;
import static org.apache.groovy.parser.antlr4.GroovyParser.LPAREN;
import static org.apache.groovy.parser.antlr4.GroovyParser.METHOD_POINTER;
import static org.apache.groovy.parser.antlr4.GroovyParser.METHOD_REFERENCE;
import static org.apache.groovy.parser.antlr4.GroovyParser.NL;
import static org.apache.groovy.parser.antlr4.GroovyParser.NullLiteral;
import static org.apache.groovy.parser.antlr4.GroovyParser.RBRACE;
import static org.apache.groovy.parser.antlr4.GroovyParser.RBRACK;
import static org.apache.groovy.parser.antlr4.GroovyParser.RPAREN;
import static org.apache.groovy.parser.antlr4.GroovyParser.SAFE_CHAIN_DOT;
import static org.apache.groovy.parser.antlr4.GroovyParser.SAFE_DOT;
import static org.apache.groovy.parser.antlr4.GroovyParser.SAFE_INDEX;
import static org.apache.groovy.parser.antlr4.GroovyParser.SEMI;
import static org.apache.groovy.parser.antlr4.GroovyParser.SPREAD_DOT;
import static org.apache.groovy.parser.antlr4.GroovyParser.StringLiteral;
import static org.apache.groovy.parser.antlr4.GroovyParser.SUPER;
import static org.apache.groovy.parser.antlr4.GroovyParser.THIS;
import static org.apache.groovy.parser.antlr4.GroovyParser.THREADSAFE;
import static org.apache.groovy.parser.antlr4.GroovyParser.VOID;
import static org.apache.groovy.parser.antlr4.GroovyParser.WHILE;

/**
 * Shared friendly recognition diagnostics for Parrot error strategies.
 * <p>
 * Subclasses choose control flow ({@link DescriptiveErrorStrategy} fail-fast vs
 * {@link RecoveringDescriptiveErrorStrategy} multi-error resync). Reporting
 * stays here so both modes share one message path. A successful parse never
 * enters these methods.
 * </p>
 * <p>
 * Two layers, on purpose: {@link MissingDelimiterDiagnostic} may <em>relocate</em>
 * the caret (insertion point after an unclosed {@code ) } / {@code ] } / {@code } }).
 * Keyword / missing-receiver {@code ?[} / sole-expected-punctuation /
 * unexpected-EOF wording only changes the sentence and keeps ANTLR's
 * offending token, so it is a fallback-string refine rather than a second
 * locate pass.
 * </p>
 */
abstract class AbstractFriendlyErrorStrategy extends DefaultErrorStrategy {

    private static final String SAFE_INDEX_NEEDS_RECEIVER = "'?[' requires an expression before it";

    private final CharStream charStream;

    AbstractFriendlyErrorStrategy(final CharStream charStream) {
        this.charStream = charStream;
    }

    /**
     * Prefer a relocated "Missing …" closer when the token stream supports it;
     * otherwise refine the generic {@code Unexpected input: ...} fallback
     * (reserved keyword, sole expected punctuation, unexpected EOF).
     * Locate/refine run in a defensive try; a single listener dispatch is
     * outside so a listener {@link IllegalArgumentException} /
     * {@link IndexOutOfBoundsException} is not mistaken for a lookup failure
     * and re-dispatched.
     */
    private void reportFriendlyError(final Parser recognizer, final RecognitionException e, final String fallbackMessage) {
        Token at = e.getOffendingToken();
        String message;
        try {
            // Incomplete / synthetic contexts can leave token indices out of range,
            // and getExpectedTokens() can reject an invalid ATN state number.
            MissingDelimiterDiagnostic.Hit hit = MissingDelimiterDiagnostic.locate(recognizer.getInputStream(), e);
            if (hit != null) {
                at = hit.at;
                message = hit.message;
            } else {
                message = refineFallbackMessage(e, fallbackMessage);
            }
        } catch (IndexOutOfBoundsException | IllegalArgumentException ignored) {
            message = fallbackMessage;
        }
        recognizer.notifyErrorListeners(at, message, e);
    }

    /**
     * Improve a generic mismatch/NVAE sentence without moving the caret.
     * Precedence: reserved/misplaced keyword, then a leading {@code ?[}
     * without a path to attach to, then a singleton expected punctuation
     * token, then unexpected EOF, then {@code generic}.
     */
    static String refineFallbackMessage(final RecognitionException e, final String generic) {
        if (e == null) {
            return generic;
        }
        Token offending = e.getOffendingToken();
        if (offending != null) {
            String keyword = keywordMessage(offending.getType());
            if (keyword != null) {
                return keyword;
            }
            String misplacedDefault = misplacedDefaultClause(e, offending);
            if (misplacedDefault != null) {
                return misplacedDefault;
            }
            String safeIndex = unexpectedSafeIndex(e, offending);
            if (safeIndex != null) {
                return safeIndex;
            }
        }
        String sole = soleExpectedMessage(e);
        if (sole != null) {
            return sole;
        }
        if (offending != null && offending.getType() == Token.EOF) {
            return "Unexpected end of input";
        }
        return generic;
    }

    /**
     * javac wording for reserved keywords Groovy tokenises but does not
     * implement, and for control-flow keywords that appear as the offending
     * token. Like javac, the sentence names the keyword even when a related
     * construct is nearby but incomplete ({@code if (x) else {}} is
     * {@code 'else' without 'if'} because the then-branch is missing).
     * {@code const} is reserved and unused in Java (JLS 3.9); the replacements
     * are {@code val} (locals; preferred over {@code final} in Groovy 6) and
     * {@code static final} (class constants). {@code threadsafe} is reserved
     * and unused in Groovy. {@code default} itself is <em>not</em> mapped
     * here: it is a valid interface-method and annotation-element keyword, so
     * an offending {@code default} (for example {@code def m() default {1}})
     * is not "outside of switch". {@code default:} / {@code default ->}
     * outside a switch is recognised via {@link #misplacedDefaultClause}.
     */
    static String keywordMessage(final int tokenType) {
        return switch (tokenType) {
            case CONST -> "'const' is not supported; use 'val' or 'static final' instead";
            case GOTO -> "'goto' is not supported";
            case THREADSAFE -> "'threadsafe' is not supported";
            case ELSE -> "'else' without 'if'";
            case CATCH -> "'catch' without 'try'";
            case FINALLY -> "'finally' without 'try'";
            case CASE -> "'case' outside of switch";
            default -> null;
        };
    }

    /**
     * {@code default: x} / {@code default -> x} at script or method scope:
     * ANTLR's offending token is the {@code :} or {@code ->}, so
     * {@link #keywordMessage(int)} on the offender is silent. Look one
     * default-channel token back; skip {@code NL} so a line break between
     * {@code default} and the clause marker still names {@code default}.
     * Restricted to those two markers so {@code interface I { default }}
     * (offender {@code }}) is not mislabelled.
     */
    static String misplacedDefaultClause(final RecognitionException e, final Token offending) {
        if (e == null || offending == null) {
            return null;
        }
        int type = offending.getType();
        if (type != COLON && type != ARROW) {
            return null;
        }
        if (!(e.getInputStream() instanceof TokenStream tokens)) {
            return null;
        }
        int index = offending.getTokenIndex();
        if (index < 1) {
            return null;
        }
        try {
            for (int i = index - 1; i >= 0; i--) {
                Token prev = tokens.get(i);
                int prevType = prev.getType();
                if (prevType == NL || prev.getChannel() != Token.DEFAULT_CHANNEL) {
                    continue;
                }
                return prevType == DEFAULT ? "'default' outside of switch" : null;
            }
        } catch (IndexOutOfBoundsException | IllegalArgumentException ignored) {
            return null;
        }
        return null;
    }

    /**
     * {@code ?[} is a single token (Groovy 4 safe index) and only appears as
     * {@code indexPropertyArgs}, a {@code pathElement}. It cannot start a
     * primary. Name a missing receiver rather than dumping a generic
     * {@code Unexpected input} span.
     * <p>
     * {@code indexPropertyArgs} has no leading {@code NL*}, so a visible
     * newline before {@code ?[} is a statement break, not trivia. Hidden-channel
     * tokens (comments; newlines inside parens) are skipped. Leave the generic
     * sentence only when the previous token can end a path — a primary /
     * {@code pathElement} / type-argument closer, or a {@code namePart} after
     * a member-selection operator ({@code foo.if?[0)} still reports
     * {@code Missing ']'}).
     * </p>
     */
    static String unexpectedSafeIndex(final RecognitionException e, final Token offending) {
        if (e == null || offending == null || offending.getType() != SAFE_INDEX) {
            return null;
        }
        if (!(e.getInputStream() instanceof TokenStream tokens)) {
            return SAFE_INDEX_NEEDS_RECEIVER;
        }
        int index = offending.getTokenIndex();
        if (index < 1) {
            return SAFE_INDEX_NEEDS_RECEIVER;
        }
        try {
            Token prev = previousDefaultChannel(tokens, index, false);
            if (prev == null) {
                return SAFE_INDEX_NEEDS_RECEIVER;
            }
            int prevType = prev.getType();
            if (canEndPathExpression(prevType)) {
                return null;
            }
            // foo.?[ — the selector itself is not a receiver
            if (isMemberSelection(prevType)) {
                return SAFE_INDEX_NEEDS_RECEIVER;
            }
            // foo.if?[ — a namePart after a selector is a receiver
            Token before = previousDefaultChannel(tokens, prev.getTokenIndex(), true);
            if (before != null && isMemberSelection(before.getType())) {
                return null;
            }
            return SAFE_INDEX_NEEDS_RECEIVER;
        } catch (IndexOutOfBoundsException | IllegalArgumentException ignored) {
            return SAFE_INDEX_NEEDS_RECEIVER;
        }
    }

    /**
     * Last default-channel token before {@code startExclusive}, or {@code null}.
     * {@code skipNl} is for {@code DOT NL* namePart}; {@code indexPropertyArgs}
     * does not skip newlines.
     */
    private static Token previousDefaultChannel(final TokenStream tokens, final int startExclusive,
                                                final boolean skipNl) {
        for (int i = startExclusive - 1; i >= 0; i--) {
            Token t = tokens.get(i);
            if (t.getChannel() != Token.DEFAULT_CHANNEL || (skipNl && t.getType() == NL)) {
                continue;
            }
            return t;
        }
        return null;
    }

    /**
     * Last token of a {@code primary}, {@code pathElement}, or
     * {@code typeArguments} — the things {@code ?[} can attach to.
     */
    static boolean canEndPathExpression(final int type) {
        return switch (type) {
            case Identifier, CapitalizedIdentifier,
                 IntegerLiteral, FloatingPointLiteral, StringLiteral,
                 BooleanLiteral, NullLiteral, GStringEnd,
                 RPAREN, RBRACK, RBRACE,
                 THIS, SUPER,
                 BuiltInPrimitiveType, VOID,
                 GT -> true;
            default -> false;
        };
    }

    private static boolean isMemberSelection(final int type) {
        return switch (type) {
            case DOT, SAFE_DOT, SPREAD_DOT, SAFE_CHAIN_DOT,
                 METHOD_POINTER, METHOD_REFERENCE, AT -> true;
            default -> false;
        };
    }

    /**
     * When ANTLR's expected set is a single punctuation token, name it
     * (javac: {@code '(' expected} → {@code Missing '('}). Closers are omitted:
     * {@link MissingDelimiterDiagnostic} already decides those with a depth check,
     * so a balanced {@code [1,,2]} is not reported as {@code Missing ']'}.
     */
    static String soleExpectedMessage(final RecognitionException e) {
        if (e == null) {
            return null;
        }
        final IntervalSet expected;
        try {
            expected = e.getExpectedTokens();
        } catch (IllegalArgumentException | IndexOutOfBoundsException ignored) {
            // ATN.getExpectedTokens rejects an invalid offending state.
            return null;
        }
        if (expected == null) {
            return null;
        }
        int sole = soleNonNlToken(expected);
        if (sole == Integer.MIN_VALUE) {
            return null;
        }
        return soleExpectedMessage(sole);
    }

    /**
     * The single expected token once optional newlines are ignored, or
     * {@link Integer#MIN_VALUE} if that is not a singleton. Groovy inserts
     * {@code NL} into many expected sets as trivia, so {@code WHILE | NL}
     * is treated as the singleton {@code WHILE}.
     */
    static int soleNonNlToken(final IntervalSet expected) {
        if (expected == null) {
            return Integer.MIN_VALUE;
        }
        int found = Integer.MIN_VALUE;
        int count = 0;
        for (int t : expected.toArray()) {
            if (t == NL) {
                continue;
            }
            count++;
            found = t;
            if (count > 1) {
                return Integer.MIN_VALUE;
            }
        }
        return count == 1 ? found : Integer.MIN_VALUE;
    }

    static String soleExpectedMessage(final int tokenType) {
        return switch (tokenType) {
            case LPAREN -> "Missing '('";
            case LBRACK -> "Missing '['";
            case LBRACE -> "Missing '{'";
            case COLON -> "Missing ':'";
            case SEMI -> "Missing ';'";
            case GT -> "Missing '>'";
            case WHILE -> "Missing 'while'";
            default -> null;
        };
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

    /**
     * Format a failed-predicate diagnostic.
     * <p>
     * {@code recognizer} is part of the protected hook surface (parity with
     * {@link #createNoViableAlternativeErrorMessage} /
     * {@link #createInputMismatchErrorMessage}) so subclasses can include
     * parser state when customising the message.
     * </p>
     */
    protected String createFailedPredicateErrorMessage(final Parser recognizer, final FailedPredicateException e) {
        // Non-null contract only; default message is already complete on the exception.
        Objects.requireNonNull(recognizer, "recognizer");
        return e.getMessage();
    }

    @Override
    protected void reportFailedPredicate(final Parser recognizer, final FailedPredicateException e) {
        notifyErrorListeners(recognizer, createFailedPredicateErrorMessage(recognizer, e), e);
    }
}
