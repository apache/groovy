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

import static org.apache.groovy.parser.antlr4.GroovyParser.ABSTRACT;
import static org.apache.groovy.parser.antlr4.GroovyParser.ARROW;
import static org.apache.groovy.parser.antlr4.GroovyParser.ASSIGN;
import static org.apache.groovy.parser.antlr4.GroovyParser.AT;
import static org.apache.groovy.parser.antlr4.GroovyParser.BooleanLiteral;
import static org.apache.groovy.parser.antlr4.GroovyParser.BuiltInPrimitiveType;
import static org.apache.groovy.parser.antlr4.GroovyParser.CASE;
import static org.apache.groovy.parser.antlr4.GroovyParser.CATCH;
import static org.apache.groovy.parser.antlr4.GroovyParser.COLON;
import static org.apache.groovy.parser.antlr4.GroovyParser.COMMA;
import static org.apache.groovy.parser.antlr4.GroovyParser.CONST;
import static org.apache.groovy.parser.antlr4.GroovyParser.CapitalizedIdentifier;
import static org.apache.groovy.parser.antlr4.GroovyParser.DEFAULT;
import static org.apache.groovy.parser.antlr4.GroovyParser.DEF;
import static org.apache.groovy.parser.antlr4.GroovyParser.DOT;
import static org.apache.groovy.parser.antlr4.GroovyParser.DO;
import static org.apache.groovy.parser.antlr4.GroovyParser.ELSE;
import static org.apache.groovy.parser.antlr4.GroovyParser.ENUM;
import static org.apache.groovy.parser.antlr4.GroovyParser.EXTENDS;
import static org.apache.groovy.parser.antlr4.GroovyParser.FINAL;
import static org.apache.groovy.parser.antlr4.GroovyParser.FINALLY;
import static org.apache.groovy.parser.antlr4.GroovyParser.FloatingPointLiteral;
import static org.apache.groovy.parser.antlr4.GroovyParser.GOTO;
import static org.apache.groovy.parser.antlr4.GroovyParser.GStringEnd;
import static org.apache.groovy.parser.antlr4.GroovyParser.GT;
import static org.apache.groovy.parser.antlr4.GroovyParser.IMPORT;
import static org.apache.groovy.parser.antlr4.GroovyParser.INTERFACE;
import static org.apache.groovy.parser.antlr4.GroovyParser.Identifier;
import static org.apache.groovy.parser.antlr4.GroovyParser.IntegerLiteral;
import static org.apache.groovy.parser.antlr4.GroovyParser.LBRACE;
import static org.apache.groovy.parser.antlr4.GroovyParser.LBRACK;
import static org.apache.groovy.parser.antlr4.GroovyParser.LPAREN;
import static org.apache.groovy.parser.antlr4.GroovyParser.LT;
import static org.apache.groovy.parser.antlr4.GroovyParser.METHOD_POINTER;
import static org.apache.groovy.parser.antlr4.GroovyParser.METHOD_REFERENCE;
import static org.apache.groovy.parser.antlr4.GroovyParser.NEW;
import static org.apache.groovy.parser.antlr4.GroovyParser.NL;
import static org.apache.groovy.parser.antlr4.GroovyParser.NullLiteral;
import static org.apache.groovy.parser.antlr4.GroovyParser.PRIVATE;
import static org.apache.groovy.parser.antlr4.GroovyParser.PROTECTED;
import static org.apache.groovy.parser.antlr4.GroovyParser.PUBLIC;
import static org.apache.groovy.parser.antlr4.GroovyParser.QUESTION;
import static org.apache.groovy.parser.antlr4.GroovyParser.RBRACE;
import static org.apache.groovy.parser.antlr4.GroovyParser.RBRACK;
import static org.apache.groovy.parser.antlr4.GroovyParser.RPAREN;
import static org.apache.groovy.parser.antlr4.GroovyParser.SAFE_CHAIN_DOT;
import static org.apache.groovy.parser.antlr4.GroovyParser.SAFE_DOT;
import static org.apache.groovy.parser.antlr4.GroovyParser.SAFE_INDEX;
import static org.apache.groovy.parser.antlr4.GroovyParser.SEMI;
import static org.apache.groovy.parser.antlr4.GroovyParser.SPREAD_DOT;
import static org.apache.groovy.parser.antlr4.GroovyParser.STATIC;
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
 * Keyword / missing-receiver {@code ?[} / array-creation / unclosed type
 * argument / method-in-the-wrong-place / unmatched {@code do} / enum-constant
 * separator / sole-expected-punctuation / unexpected-EOF wording only changes
 * the sentence and keeps ANTLR's offending token, so it is a fallback-string
 * refine rather than a second locate pass.
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
     * (reserved keyword, array creation, unclosed type argument, and the
     * other sentence-only cases in {@link #refineFallbackMessage}).
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
     * without a path to attach to, then array-creation / unclosed type
     * argument / method definition in a bad position / unmatched {@code do}
     * / enum constants needing {@code ;}, then a singleton expected
     * punctuation token, then unexpected EOF, then a named unexpected
     * closer or semicolon, then {@code generic}.
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
            String array = arrayCreationMessage(e, offending);
            if (array != null) {
                return array;
            }
            String typeArg = unmatchedTypeArgument(e, offending);
            if (typeArg != null) {
                return typeArg;
            }
            String method = methodDefinitionNotExpected(e, offending);
            if (method != null) {
                return method;
            }
            String doWhile = unmatchedDoWhile(e, offending);
            if (doWhile != null) {
                return doWhile;
            }
            String enumComma = enumMemberAfterComma(e, offending);
            if (enumComma != null) {
                return enumComma;
            }
            String defaultAfterHeader = interfaceDefaultAfterHeader(e, offending);
            if (defaultAfterHeader != null) {
                return defaultAfterHeader;
            }
        }
        String sole = soleExpectedMessage(e);
        if (sole != null) {
            return sole;
        }
        if (offending != null && offending.getType() == Token.EOF) {
            return "Unexpected end of input";
        }
        String punct = unexpectedPunctuation(offending);
        if (punct != null) {
            return punct;
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
     * are {@code val} (locals) and {@code static final} (class constants).
     * {@code threadsafe} is reserved and unused in Groovy. {@code default}
     * itself is <em>not</em> mapped here: it is a valid interface-method and
     * annotation-element keyword, so an offending {@code default} (for example
     * {@code def m() default {1}}) is not "outside of switch".
     * {@code default:} / {@code default ->} outside a switch is recognised
     * via {@link #misplacedDefaultClause}. {@code import} is only legal as a
     * compilation-unit member; a misplaced {@code import} (inside a method or
     * closure) is named here rather than dumped as {@code Unexpected input}.
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
            case IMPORT -> "'import' is only allowed at the beginning of a compilation unit";
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

    /**
     * javac: {@code array dimension missing}. Groovy's grammar commits to
     * {@code dim0+ arrayInitializer} after {@code new T[]}, so the sole
     * expected token at EOF is {@code \{} — {@code Missing '\{'} is the
     * wrong advice. A sized dim after an empty dim ({@code new T[][n]}) and
     * combining a size with an initializer ({@code new T[n] \{...\}}) are
     * the same family.
     */
    static String arrayCreationMessage(final RecognitionException e, final Token offending) {
        if (e == null || offending == null) {
            return null;
        }
        if (!(e.getInputStream() instanceof TokenStream tokens)) {
            return null;
        }
        try {
            Token[] prefix = defaultChannelPrefix(tokens, offending);
            int newAt = lastIndexOfType(prefix, NEW);
            if (newAt < 0) {
                return null;
            }
            int i = newAt + 1;
            while (i < prefix.length && isCreatedNameToken(prefix[i].getType())) {
                i++;
            }
            if (i >= prefix.length || prefix[i].getType() != LBRACK) {
                return null;
            }
            boolean sawEmpty = false;
            boolean sawSized = false;
            while (i < prefix.length && prefix[i].getType() == LBRACK) {
                i++;
                if (i < prefix.length && prefix[i].getType() == RBRACK) {
                    sawEmpty = true;
                    i++;
                    continue;
                }
                if (sawEmpty) {
                    return "Cannot specify an array size after an empty dimension";
                }
                while (i < prefix.length) {
                    int t = prefix[i].getType();
                    if (t == RBRACK) {
                        sawSized = true;
                        i++;
                        break;
                    }
                    if (t == LBRACK || t == LBRACE || t == Token.EOF) {
                        return null;
                    }
                    i++;
                }
                if (!sawSized) {
                    return null;
                }
            }
            int leftover = i < prefix.length ? prefix[i].getType() : Token.EOF;
            if (leftover == Token.EOF && sawEmpty && !sawSized) {
                return "Array dimension missing; specify a size or add an initializer '{}'";
            }
            if (leftover == LBRACE && sawSized) {
                return "Cannot combine an array size with an array initializer";
            }
            return null;
        } catch (IndexOutOfBoundsException | IllegalArgumentException ignored) {
            return null;
        }
    }

    /**
     * {@code List<Integer name} / {@code new ArrayList<Integer(} — an
     * unmatched {@code <} after a capitalized type name is a missing
     * {@code >}, not a comparison. {@code x < y z} (lowercase left
     * operand) is left to the generic sentence.
     * <p>
     * ANTLR's offending token for {@code List<Integer name} is often
     * {@code List} (the NVAE start), so a prefix-up-to-offender scan
     * never sees the {@code <}. Look ahead from the error start until a
     * statement boundary — not the rest of the file, or a later unclosed
     * generic would steal an earlier unrelated diagnostic.
     * </p>
     */
    static String unmatchedTypeArgument(final RecognitionException e, final Token offending) {
        if (e == null || offending == null || offending.getType() == GT) {
            return null;
        }
        if (!(e.getInputStream() instanceof TokenStream tokens)) {
            return null;
        }
        try {
            int lo = offending.getTokenIndex();
            if (e instanceof NoViableAltException nvae && nvae.getStartToken() != null) {
                int start = nvae.getStartToken().getTokenIndex();
                if (start >= 0 && (lo < 0 || start < lo)) {
                    lo = start;
                }
            }
            // Include the capitalized type name before '<'; ANTLR's offender is
            // often the type, the '<', or the following identifier.
            if (lo > 0) {
                lo = Math.max(0, lo - 4);
            } else if (lo < 0) {
                lo = 0;
            }
            int depth = 0;
            boolean typeOpener = false;
            Token prev = null;
            int n = tokens.size();
            int seen = 0;
            for (int i = lo; i < n && seen < 24; i++) {
                Token t = tokens.get(i);
                if (t.getChannel() != Token.DEFAULT_CHANNEL) {
                    continue;
                }
                int type = t.getType();
                if (type == Token.EOF || type == SEMI || type == ASSIGN
                        || (type == NL && depth == 0 && i > lo)) {
                    if (i > lo) {
                        break;
                    }
                    continue;
                }
                seen++;
                if (type == LT) {
                    // `<<` is two LT tokens (shift), not nested type arguments.
                    if (prev != null && prev.getType() == LT) {
                        prev = t;
                        continue;
                    }
                    if (prev != null && prev.getType() == CapitalizedIdentifier) {
                        depth++;
                        typeOpener = true;
                    } else if (depth > 0) {
                        depth++;
                    }
                } else if (type == GT && depth > 0) {
                    depth--;
                }
                prev = t;
            }
            return depth > 0 && typeOpener ? "Missing '>'" : null;
        } catch (IndexOutOfBoundsException | IllegalArgumentException ignored) {
            return null;
        }
    }

    /**
     * {@code \{ -> def say(String msg) \{ } }()} — the unexpected {@code (}
     * after {@code def ident} is a method header in a position that only
     * allows statements. Successful parses never enter this method, so
     * script- and class-level {@code def m()} is unaffected.
     */
    static String methodDefinitionNotExpected(final RecognitionException e, final Token offending) {
        if (e == null || offending == null || offending.getType() != LPAREN) {
            return null;
        }
        if (!(e.getInputStream() instanceof TokenStream tokens)) {
            return null;
        }
        try {
            Token prev = previousDefaultChannel(tokens, offending.getTokenIndex(), true);
            if (prev == null || (prev.getType() != Identifier && prev.getType() != CapitalizedIdentifier)) {
                return null;
            }
            Token prev2 = previousDefaultChannel(tokens, prev.getTokenIndex(), true);
            if (prev2 == null || !isMethodHeaderPrefix(prev2.getType())) {
                return null;
            }
            return "Method definition not expected here";
        } catch (IndexOutOfBoundsException | IllegalArgumentException ignored) {
            return null;
        }
    }

    /**
     * {@code do stmt; stmt; while (...)} — after the {@code do} body the
     * parser wants {@code while}. The expected set is rarely a singleton
     * (command expressions continue), so {@link #soleExpectedMessage} stays
     * silent. javac reports {@code while expected} at the end of the first
     * statement; that is misleading when {@code while} is already on the next
     * line (the body was two statements without {@code \{ \}}). If a later
     * {@code while} is present, name the multi-statement body; otherwise
     * {@code Missing 'while'} (true omission, javac's wording family).
     */
    static String unmatchedDoWhile(final RecognitionException e, final Token offending) {
        if (e == null || offending == null) {
            return null;
        }
        if (!(e.getInputStream() instanceof TokenStream tokens)) {
            return null;
        }
        try {
            IntervalSet expected;
            try {
                expected = e.getExpectedTokens();
            } catch (IllegalArgumentException | IndexOutOfBoundsException ignored) {
                return null;
            }
            if (expected == null || !expected.contains(WHILE)) {
                return null;
            }
            Token[] prefix = defaultChannelPrefix(tokens, offending);
            int lastDo = -1;
            int lastWhile = -1;
            int limit = prefix.length;
            if (limit > 0 && prefix[limit - 1] == offending) {
                limit--;
            }
            for (int i = 0; i < limit; i++) {
                int type = prefix[i].getType();
                if (type == DO && !isNamePart(prefix, i)) {
                    lastDo = i;
                } else if (type == WHILE && !isNamePart(prefix, i)) {
                    lastWhile = i;
                }
            }
            if (lastDo <= lastWhile) {
                return null;
            }
            return whileAppearsAfter(tokens, offending)
                    ? "do-while body must be a single statement; wrap multiple statements in '{ }'"
                    : "Missing 'while'";
        } catch (IndexOutOfBoundsException | IllegalArgumentException ignored) {
            return null;
        }
    }

    /**
     * A {@code while} keyword after the offender — not {@code foo.while}.
     * Distinguishes a missing closer from a multi-statement {@code do} body
     * that already has its {@code while} on a later line.
     */
    private static boolean whileAppearsAfter(final TokenStream tokens, final Token offending) {
        int from = offending.getTokenIndex() + 1;
        if (from < 1) {
            return false;
        }
        Token prevDefault = null;
        int n = tokens.size();
        for (int i = from; i < n; i++) {
            Token t = tokens.get(i);
            if (t.getChannel() != Token.DEFAULT_CHANNEL) {
                continue;
            }
            if (t.getType() == WHILE) {
                boolean namePart = prevDefault != null && isMemberSelection(prevDefault.getType());
                if (!namePart) {
                    return true;
                }
            }
            if (t.getType() == Token.EOF) {
                return false;
            }
            prevDefault = t;
        }
        return false;
    }

    /**
     * javac: {@code ';' expected} between enum constants and members.
     * {@code enum E \{ X, Y, def z() \{\} \}} — a comma then a method
     * header, not another constant.
     */
    static String enumMemberAfterComma(final RecognitionException e, final Token offending) {
        if (e == null || offending == null || !isEnumMemberStart(offending.getType())) {
            return null;
        }
        if (!(e.getInputStream() instanceof TokenStream tokens)) {
            return null;
        }
        try {
            Token prev = previousDefaultChannel(tokens, offending.getTokenIndex(), true);
            if (prev == null || prev.getType() != COMMA) {
                return null;
            }
            Token[] prefix = defaultChannelPrefix(tokens, offending);
            if (!insideEnumConstantList(prefix)) {
                return null;
            }
            return "';' expected after the last enum constant";
        } catch (IndexOutOfBoundsException | IllegalArgumentException ignored) {
            return null;
        }
    }

    /**
     * {@code interface I \{ def m() default \{1\} \}} — annotation-element
     * {@code default} after a method header. Interface default methods put
     * {@code default} <em>before</em> the name. {@code @interface} element
     * defaults parse successfully and never reach this method.
     */
    static String interfaceDefaultAfterHeader(final RecognitionException e, final Token offending) {
        if (e == null || offending == null || offending.getType() != DEFAULT) {
            return null;
        }
        if (!(e.getInputStream() instanceof TokenStream tokens)) {
            return null;
        }
        try {
            Token prev = previousDefaultChannel(tokens, offending.getTokenIndex(), true);
            if (prev == null || prev.getType() != RPAREN) {
                return null;
            }
            Token[] prefix = defaultChannelPrefix(tokens, offending);
            int iface = lastIndexOfType(prefix, INTERFACE);
            if (iface < 0) {
                return null;
            }
            if (iface > 0 && prefix[iface - 1].getType() == AT) {
                return null;
            }
            return "'default' cannot follow a method header; put 'default' before the method name";
        } catch (IndexOutOfBoundsException | IllegalArgumentException ignored) {
            return null;
        }
    }

    /**
     * Name an extra closer or semicolon rather than {@code Unexpected input: ')'}.
     */
    static String unexpectedPunctuation(final Token offending) {
        if (offending == null) {
            return null;
        }
        return switch (offending.getType()) {
            case RPAREN -> "Unexpected ')'";
            case RBRACK -> "Unexpected ']'";
            case RBRACE -> "Unexpected '}'";
            case SEMI -> "Unexpected ';'";
            case COMMA -> "Unexpected ','";
            default -> null;
        };
    }

    private static boolean isCreatedNameToken(final int type) {
        return switch (type) {
            case AT, Identifier, CapitalizedIdentifier, BuiltInPrimitiveType,
                 DOT, LT, GT, COMMA, QUESTION, EXTENDS, SUPER -> true;
            default -> false;
        };
    }

    private static boolean isMethodHeaderPrefix(final int type) {
        return switch (type) {
            case DEF, VOID, BuiltInPrimitiveType,
                 PUBLIC, PRIVATE, PROTECTED, STATIC, FINAL, ABSTRACT -> true;
            default -> false;
        };
    }

    private static boolean isNamePart(final Token[] prefix, final int index) {
        if (index < 1) {
            return false;
        }
        return isMemberSelection(prefix[index - 1].getType());
    }

    /**
     * {@code enum E { X, Y, def z() }} — still in the constant list of an
     * open enum body (no {@code ;} yet). A closed {@code enum} earlier in
     * the file must not rewrite {@code [1, def]}.
     */
    private static boolean insideEnumConstantList(final Token[] prefix) {
        int brace = 0;
        boolean inEnum = false;
        boolean seenSemi = false;
        for (Token t : prefix) {
            int type = t.getType();
            if (type == ENUM && !inEnum) {
                inEnum = true;
                brace = 0;
                seenSemi = false;
            } else if (inEnum && type == LBRACE) {
                brace++;
            } else if (inEnum && type == RBRACE) {
                brace--;
                if (brace <= 0) {
                    inEnum = false;
                }
            } else if (inEnum && brace == 1 && type == SEMI) {
                seenSemi = true;
            }
        }
        return inEnum && brace > 0 && !seenSemi;
    }

    private static boolean isEnumMemberStart(final int type) {
        return switch (type) {
            case DEF, VOID, BuiltInPrimitiveType,
                 PUBLIC, PRIVATE, PROTECTED, STATIC, FINAL, ABSTRACT, DEFAULT -> true;
            default -> false;
        };
    }

    private static int lastIndexOfType(final Token[] prefix, final int type) {
        for (int i = prefix.length - 1; i >= 0; i--) {
            if (prefix[i].getType() == type) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Default-channel tokens up to and including {@code offending}, skipping
     * newlines. Error-path only.
     */
    private static Token[] defaultChannelPrefix(final TokenStream tokens, final Token offending) {
        int end = offending.getTokenIndex();
        if (end < 0) {
            end = tokens.size() - 1;
        }
        if (end < 0) {
            return new Token[0];
        }
        int n = 0;
        Token[] buf = new Token[end + 1];
        for (int i = 0; i <= end; i++) {
            Token t = tokens.get(i);
            if (t.getChannel() == Token.DEFAULT_CHANNEL && t.getType() != NL) {
                buf[n++] = t;
            }
        }
        if (n == buf.length) {
            return buf;
        }
        Token[] exact = new Token[n];
        System.arraycopy(buf, 0, exact, 0, n);
        return exact;
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
