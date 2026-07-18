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

import groovy.lang.Tuple2;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.IntervalSet;
import org.apache.groovy.parser.antlr4.util.PositionConfigureUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static org.apache.groovy.parser.antlr4.GroovyParser.AT;
import static org.apache.groovy.parser.antlr4.GroovyParser.BITAND;
import static org.apache.groovy.parser.antlr4.GroovyParser.BooleanLiteral;
import static org.apache.groovy.parser.antlr4.GroovyParser.BuiltInPrimitiveType;
import static org.apache.groovy.parser.antlr4.GroovyParser.CapitalizedIdentifier;
import static org.apache.groovy.parser.antlr4.GroovyParser.DOT;
import static org.apache.groovy.parser.antlr4.GroovyParser.FloatingPointLiteral;
import static org.apache.groovy.parser.antlr4.GroovyParser.GT;
import static org.apache.groovy.parser.antlr4.GroovyParser.GStringBegin;
import static org.apache.groovy.parser.antlr4.GroovyParser.Identifier;
import static org.apache.groovy.parser.antlr4.GroovyParser.IntegerLiteral;
import static org.apache.groovy.parser.antlr4.GroovyParser.LBRACE;
import static org.apache.groovy.parser.antlr4.GroovyParser.LBRACK;
import static org.apache.groovy.parser.antlr4.GroovyParser.LPAREN;
import static org.apache.groovy.parser.antlr4.GroovyParser.LT;
import static org.apache.groovy.parser.antlr4.GroovyParser.NL;
import static org.apache.groovy.parser.antlr4.GroovyParser.RBRACE;
import static org.apache.groovy.parser.antlr4.GroovyParser.RBRACK;
import static org.apache.groovy.parser.antlr4.GroovyParser.RPAREN;
import static org.apache.groovy.parser.antlr4.GroovyParser.SAFE_INDEX;
import static org.apache.groovy.parser.antlr4.GroovyParser.StringLiteral;
import static org.apache.groovy.parser.antlr4.GroovyParser.VOID;

/**
 * Error-path-only diagnostic for a missing closing delimiter
 * ({@code ')'}, {@code ']'}, or {@code '}'}).
 * <p>
 * Successful parses never call into this class, so there is no steady-state
 * cost.  When a parse has already failed, a single linear scan of the token
 * stream recovers a precise "Missing …" location — something ANTLR4's default
 * mismatch reporting cannot do without grammar-level error alternatives
 * (removed after GROOVY-9588 because they hurt parsing performance).
 * </p>
 * <p>
 * Detection is ordered from most specific to least specific:
 * <ol>
 *   <li>RecognitionException whose sole expected token is a single closer
 *       ({@code RPAREN} / {@code RBRACK} / {@code RBRACE})</li>
 *   <li>Cast / parenthesised-type pattern:
 *       {@code '(' type <expr-start>} without the closing {@code ')'}</li>
 *   <li>Unclosed or mismatched {@code (} / {@code [} / {@code ?[} / {@code \{}
 *       on a delimiter stack (innermost first)</li>
 * </ol>
 */
final class MissingDelimiterDiagnostic {

    /**
     * A located missing-delimiter finding.
     */
    static final class Hit {
        final String message;
        final Token at;

        Hit(final String message, final Token at) {
            this.message = message;
            this.at = at;
        }
    }

    private static final String MSG_RPAREN = "Missing ')'";
    private static final String MSG_RBRACK = "Missing ']'";
    private static final String MSG_RBRACE = "Missing '}'";

    private MissingDelimiterDiagnostic() {
    }

    /**
     * @return a hit whose message and caret location describe a missing
     *         closer, or {@code null} if this failure does not look like one
     */
    static Hit locate(final TokenStream tokens, final RecognitionException e) {
        if (tokens == null) {
            return null;
        }

        List<Token> defaultChannel = collectDefaultChannelTokens(tokens);
        if (defaultChannel.isEmpty()) {
            return null;
        }

        // (1) Parser already knows it wanted only one specific closer
        Hit fromExpected = fromSoleExpectedCloser(e, defaultChannel);
        if (fromExpected != null) {
            return fromExpected;
        }

        // (2) Cast form: (type <expr>  — ')' of the cast is missing
        Hit fromCast = fromCastPattern(defaultChannel);
        if (fromCast != null) {
            return fromCast;
        }

        // (3) Unclosed / mismatched delimiters on a stack
        return fromDelimiterStack(defaultChannel);
    }

    //--------------------------------------------------------------------------
    // Strategy 1 — sole expected token is a closer
    //--------------------------------------------------------------------------

    private static Hit fromSoleExpectedCloser(final RecognitionException e,
                                              final List<Token> defaultChannel) {
        if (e == null) {
            return null;
        }
        IntervalSet expected = e.getExpectedTokens();
        if (expected == null || expected.size() != 1) {
            return null;
        }

        final String message;
        final int openDepth;
        if (expected.contains(RPAREN)) {
            message = MSG_RPAREN;
            openDepth = delimiterDepth(defaultChannel, true, false, false);
        } else if (expected.contains(RBRACK)) {
            message = MSG_RBRACK;
            openDepth = delimiterDepth(defaultChannel, false, true, false);
        } else if (expected.contains(RBRACE)) {
            message = MSG_RBRACE;
            openDepth = delimiterDepth(defaultChannel, false, false, true);
        } else {
            return null;
        }

        // If the corresponding openers/closers are already balanced, the
        // failure is an unexpected intermediate token (e.g. `foo(1;2;3)`),
        // not a missing closer — leave the generic error alone.
        if (openDepth == 0) {
            return null;
        }

        Token offending = e.getOffendingToken();
        if (offending == null) {
            return null;
        }
        // EOF's line/column often sit on the following empty line after a
        // trailing newline; point just past the previous real token instead.
        if (offending.getType() == Token.EOF) {
            Token lastReal = lastNonEof(defaultChannel);
            if (lastReal != null) {
                return new Hit(message, insertionPointAfter(lastReal));
            }
        }
        return new Hit(message, offending);
    }

    /**
     * Net open depth for the requested delimiter family at end of stream.
     * Families are counted independently so a missing {@code ')'} is not
     * masked by balanced braces, and vice versa.
     */
    private static int delimiterDepth(final List<Token> tokens,
                                      final boolean parens,
                                      final boolean brackets,
                                      final boolean braces) {
        int depth = 0;
        for (Token t : tokens) {
            int type = t.getType();
            if (parens && type == LPAREN) {
                depth++;
            } else if (parens && type == RPAREN && depth > 0) {
                depth--;
            } else if (brackets && isOpenBracket(type)) {
                depth++;
            } else if (brackets && type == RBRACK && depth > 0) {
                depth--;
            } else if (braces && type == LBRACE) {
                depth++;
            } else if (braces && type == RBRACE && depth > 0) {
                depth--;
            }
        }
        return depth;
    }

    /**
     * Last non-EOF, non-NL token.  Skipping NL keeps the caret on the last
     * meaningful line (same idea as {@link #fromDelimiterStack}) instead of
     * after a trailing newline that often sits alone on the next source line.
     */
    private static Token lastNonEof(final List<Token> tokens) {
        for (int i = tokens.size() - 1; i >= 0; i--) {
            Token t = tokens.get(i);
            int type = t.getType();
            if (type != Token.EOF && type != NL) {
                return t;
            }
        }
        return null;
    }

    //--------------------------------------------------------------------------
    // Strategy 2 — cast / parenthesised-type pattern (')' only)
    //--------------------------------------------------------------------------

    /**
     * Scan for {@code '(' type <token>} where the {@code ')'} of a cast is
     * missing.  The earliest high-confidence site is reported.
     * <p>
     * Confidence rules avoid flagging parenthesised command expressions
     * such as {@code (foo bar)}:
     * <ul>
     *   <li>type is a built-in primitive / {@code void} (e.g. {@code (int 123)}), or</li>
     *   <li>the token after the type is a literal or {@code '('}
     *       (e.g. {@code (Foo)1}, {@code (Foo)(x)})</li>
     * </ul>
     * Reporting the earliest site is intentional: with
     * {@link org.antlr.v4.runtime.BailErrorStrategy} the parser stops at the
     * first recognition failure, so the first incomplete cast is the relevant
     * one.  In a hypothetical multi-error buffer the caret could theoretically
     * point at an earlier cast-like fragment than the failure ANTLR reported;
     * that trade-off is accepted to keep the scan linear and free of false
     * coupling to ANTLR's sometimes-unrelated offending token.
     */
    private static Hit fromCastPattern(final List<Token> tokens) {
        final int n = tokens.size();
        for (int i = 0; i < n; i++) {
            if (tokens.get(i).getType() != LPAREN) {
                continue;
            }
            int typeStart = skipAnnotations(tokens, i + 1);
            if (typeStart < 0 || typeStart >= n) {
                continue;
            }
            boolean primitiveType = isPrimitiveOrVoid(tokens.get(typeStart).getType());
            int afterType = matchType(tokens, i + 1);
            if (afterType <= i + 1) {
                continue; // no type after '('
            }
            if (afterType >= n) {
                return new Hit(MSG_RPAREN, insertionPointAfter(tokens.get(afterType - 1)));
            }
            Token next = tokens.get(afterType);
            int nt = next.getType();
            if (nt == RPAREN || nt == BITAND) {
                continue; // well-formed cast or intersection type
            }
            // Type continuators that matchType should already have consumed;
            // if they remain, skip rather than false-positive.
            if (nt == DOT || nt == LT || nt == LBRACK) {
                continue;
            }
            if (primitiveType || isHighConfidenceCastOperand(nt)) {
                return new Hit(MSG_RPAREN, next);
            }
        }
        return null;
    }

    private static boolean isHighConfidenceCastOperand(final int tokenType) {
        return isLiteral(tokenType) || tokenType == LPAREN;
    }

    //--------------------------------------------------------------------------
    // Strategy 3 — delimiter stack (unclosed / mismatched)
    //--------------------------------------------------------------------------

    /**
     * Walk default-channel tokens with a stack of open delimiters.  Reports
     * the innermost missing closer on mismatch or at EOF.
     */
    private static Hit fromDelimiterStack(final List<Token> tokens) {
        Deque<Token> openStack = new ArrayDeque<>();
        Token lastInside = null;

        for (Token t : tokens) {
            int type = t.getType();
            if (type == Token.EOF) {
                break;
            }

            if (isOpenDelimiter(type)) {
                openStack.push(t);
                lastInside = t;
                continue;
            }

            if (isCloseDelimiter(type)) {
                if (openStack.isEmpty()) {
                    // Extra closer — not a *missing* closer.
                    continue;
                }
                Token open = openStack.peek();
                if (closes(open.getType(), type)) {
                    openStack.pop();
                    lastInside = t;
                    continue;
                }
                // Mismatched closer: the opener on top still needs its own
                // closer (e.g. `foo([1, 2)` → missing ']' before ')').
                return hitForOpen(open, lastInside, t);
            }

            // Skip NL so the caret sits after the last meaningful token on a
            // line (e.g. missing '}' after `println 1`), not past the newline.
            if (!openStack.isEmpty() && type != NL) {
                lastInside = t;
            }
        }

        if (!openStack.isEmpty()) {
            Token open = openStack.peek();
            return hitForOpen(open, lastInside, null);
        }
        return null;
    }

    private static Hit hitForOpen(final Token open, final Token lastInside, final Token mismatchCloser) {
        String message = messageForOpen(open.getType());
        // Prefer the insertion point after the last token inside the open
        // delimiter; fall back to the mismatched closer or the opener itself.
        if (lastInside != null && lastInside != open) {
            return new Hit(message, insertionPointAfter(lastInside));
        }
        if (lastInside != null) {
            // Opener with no interior tokens yet — point just past it, or at
            // a mismatched closer if present (e.g. `m( }` → caret at '}').
            if (mismatchCloser != null) {
                return new Hit(message, mismatchCloser);
            }
            return new Hit(message, insertionPointAfter(lastInside));
        }
        if (mismatchCloser != null) {
            return new Hit(message, mismatchCloser);
        }
        return new Hit(message, open);
    }

    private static boolean isOpenDelimiter(final int type) {
        return type == LPAREN || isOpenBracket(type) || type == LBRACE;
    }

    private static boolean isCloseDelimiter(final int type) {
        return type == RPAREN || type == RBRACK || type == RBRACE;
    }

    private static boolean isOpenBracket(final int type) {
        return type == LBRACK || type == SAFE_INDEX;
    }

    private static boolean closes(final int openType, final int closeType) {
        if (openType == LPAREN) {
            return closeType == RPAREN;
        }
        if (isOpenBracket(openType)) {
            return closeType == RBRACK;
        }
        if (openType == LBRACE) {
            return closeType == RBRACE;
        }
        return false;
    }

    private static String messageForOpen(final int openType) {
        if (openType == LPAREN) {
            return MSG_RPAREN;
        }
        if (isOpenBracket(openType)) {
            return MSG_RBRACK;
        }
        return MSG_RBRACE;
    }

    //--------------------------------------------------------------------------
    // Type matching (conservative, token-level) — cast strategy only
    //--------------------------------------------------------------------------

    private static int skipAnnotations(final List<Token> tokens, final int start) {
        int i = start;
        final int n = tokens.size();
        while (i < n && tokens.get(i).getType() == AT) {
            i++;
            if (i >= n || !isIdentifierLike(tokens.get(i).getType())) {
                return -1;
            }
            i++;
            if (i < n && tokens.get(i).getType() == LPAREN) {
                i = skipBalanced(tokens, i, LPAREN, RPAREN);
                if (i < 0) {
                    return -1;
                }
            }
        }
        return i;
    }

    private static int matchType(final List<Token> tokens, final int start) {
        int i = skipAnnotations(tokens, start);
        if (i < 0) {
            return start;
        }
        final int n = tokens.size();
        if (i >= n) {
            return start;
        }

        int head = tokens.get(i).getType();
        if (isPrimitiveOrVoid(head)) {
            i++;
        } else if (isIdentifierLike(head)) {
            i++;
            while (i + 1 < n
                    && tokens.get(i).getType() == DOT
                    && isIdentifierLike(tokens.get(i + 1).getType())) {
                i += 2;
            }
            if (i < n && tokens.get(i).getType() == LT) {
                i = skipBalanced(tokens, i, LT, GT);
                if (i < 0) {
                    return start;
                }
            }
        } else {
            return start;
        }

        while (i + 1 < n
                && tokens.get(i).getType() == LBRACK
                && tokens.get(i + 1).getType() == RBRACK) {
            i += 2;
        }

        return i;
    }

    private static int skipBalanced(final List<Token> tokens, final int openIdx,
                                    final int openType, final int closeType) {
        int depth = 0;
        for (int i = openIdx; i < tokens.size(); i++) {
            int t = tokens.get(i).getType();
            if (t == openType) {
                depth++;
            } else if (t == closeType) {
                depth--;
                if (depth == 0) {
                    return i + 1;
                }
            } else if (t == Token.EOF) {
                return -1;
            }
        }
        return -1;
    }

    //--------------------------------------------------------------------------
    // Token helpers
    //--------------------------------------------------------------------------

    private static List<Token> collectDefaultChannelTokens(final TokenStream tokens) {
        // Must fill through EOF so trailing closers are visible — otherwise an
        // early failure inside a delimiter looks like an unclosed one
        // (e.g. `foo(1;2;3)` fails at ';' before ')' has been pulled).
        if (tokens instanceof BufferedTokenStream) {
            ((BufferedTokenStream) tokens).fill();
        }

        List<Token> result = new ArrayList<>();
        try {
            for (int i = 0; ; i++) {
                Token t = tokens.get(i);
                if (t.getType() == Token.EOF) {
                    result.add(t);
                    break;
                }
                if (t.getChannel() == Token.DEFAULT_CHANNEL) {
                    result.add(t);
                }
            }
        } catch (IndexOutOfBoundsException | IllegalArgumentException ignored) {
            // Non-buffered / partially filled streams may reject absolute get(i)
            // (e.g. UnbufferedTokenStream outside its window).  Partial default-
            // channel tokens are still useful for best-effort diagnostics.
        }
        return result;
    }

    private static boolean isPrimitiveOrVoid(final int tokenType) {
        return tokenType == BuiltInPrimitiveType || tokenType == VOID;
    }

    private static boolean isIdentifierLike(final int tokenType) {
        return tokenType == Identifier || tokenType == CapitalizedIdentifier;
    }

    private static boolean isLiteral(final int tokenType) {
        return tokenType == IntegerLiteral
                || tokenType == FloatingPointLiteral
                || tokenType == StringLiteral
                || tokenType == GStringBegin
                || tokenType == BooleanLiteral;
    }

    /**
     * Caret sits just past {@code token} (the usual "insert closer here" point).
     * <p>
     * Multi-line tokens (triple-quoted strings, multi-line GString fragments,
     * …) place the caret on the <em>last</em> line of the token, after its
     * final content.  Adding the full code-point length to the start column
     * alone would overshoot on the start line when the token text contains
     * newlines.
     * </p>
     * <p>
     * End line/column rules match {@link PositionConfigureUtils#endPosition}
     * so diagnostic carets stay consistent with AST last-position handling
     * (code-point based columns, newline counting).
     * </p>
     */
    private static Token insertionPointAfter(final Token token) {
        CommonToken point = new CommonToken(Token.INVALID_TYPE);
        point.setText("");
        String text = token.getText();
        if (text == null) {
            point.setLine(token.getLine());
            point.setCharPositionInLine(token.getCharPositionInLine());
            return point;
        }
        // endPosition → (line, 1-based exclusive column); ANTLR columns are 0-based
        Tuple2<Integer, Integer> end = PositionConfigureUtils.endPosition(token);
        point.setLine(end.getV1());
        point.setCharPositionInLine(end.getV2() - 1);
        return point;
    }
}
