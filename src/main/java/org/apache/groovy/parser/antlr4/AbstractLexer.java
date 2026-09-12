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
package org.apache.groovy.parser.antlr4;

import groovy.lang.Tuple;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.IntStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;

/**
 * Because antlr4 does not support generating lexer with specified interface,
 * we have to create a super class for it and implement the interface.
 * <p>
 * Lexer-error-path helpers used from {@code GroovyLexer.g4} actions also live
 * here so the grammar stays thin. Successful tokenisation never calls them.
 * Character display for diagnostics goes through {@link #getCharErrorDisplay(int)}
 * so GString dollar errors and {@code UNEXPECTED_CHAR} share one escape policy.
 * </p>
 */
public abstract class AbstractLexer extends Lexer implements SyntaxErrorReportable {

    public AbstractLexer(CharStream input) {
        super(input);
    }

    /**
     * Bound for {@link #illegalEscapeLaIndex(CharStream, int)}. Error-path
     * only; a longer line falls back to {@code Unclosed string literal}.
     */
    static final int ILLEGAL_ESCAPE_SCAN_LIMIT = 8192;

    /**
     * User-facing message for {@code UNEXPECTED_CHAR}.
     * <p>
     * An unexpected {@code '} or {@code "} is an unclosed string literal
     * (javac: {@code unclosed string literal}), unless a scan-ahead of the
     * remainder finds an illegal escape in an otherwise closed literal
     * ({@code "C:\Users\me"}). Other characters use
     * {@code Unexpected character: '...'} via {@link #getCharErrorDisplay(int)}.
     * </p>
     */
    String unexpectedCharacterMessage() {
        return unexpectedCharacterMessage(getText(), _input);
    }

    /**
     * Report {@link #unexpectedCharacterMessage()} and, for an illegal
     * string escape, put the caret on the backslash rather than the opener.
     *
     * @param errorIgnored when {@code true}, keep tokenising (IDE highlighting)
     */
    void requireUnexpectedCharacter(final boolean errorIgnored) {
        String text = getText();
        int offset = -1;
        if (text != null && !text.isEmpty()) {
            int q = text.codePointAt(0);
            if (q == '\'' || q == '"') {
                int la = illegalEscapeLaIndex(_input, q);
                if (la > 0) {
                    offset = la - 1;
                }
            }
        }
        require(errorIgnored, unexpectedCharacterMessage(), offset, false);
    }

    /**
     * @param text the unexpected character(s), or {@code null}/{@code ""} if the
     *             lexer has no current text (still reported, without a glyph)
     */
    static String unexpectedCharacterMessage(final String text) {
        return unexpectedCharacterMessage(text, null);
    }

    /**
     * @param input remainder after the unexpected character; used only when
     *              {@code text} is {@code '} or {@code "} to distinguish an
     *              illegal escape from a truly unclosed literal
     */
    static String unexpectedCharacterMessage(final String text, final CharStream input) {
        if (text == null || text.isEmpty()) {
            return "Unexpected character";
        }
        int cp = text.codePointAt(0);
        if (cp == '\'' || cp == '"') {
            int la = illegalEscapeLaIndex(input, cp);
            if (la > 0) {
                return illegalEscapeMessage(input, la);
            }
            return "Unclosed string literal";
        }
        return "Unexpected character: " + quotedCodePoint(cp);
    }

    /**
     * 1-based {@code LA} index of an illegal {@code \} after an opening quote,
     * or {@code 0} if none (unclosed, or a closed literal with only legal
     * escapes). Error-path only; stops at newline / EOF / the matching quote.
     */
    static int illegalEscapeLaIndex(final CharStream input, final int quote) {
        if (input == null) {
            return 0;
        }
        for (int i = 1; i <= ILLEGAL_ESCAPE_SCAN_LIMIT; i++) {
            int c = input.LA(i);
            if (c == IntStream.EOF || c == '\n' || c == '\r') {
                return 0;
            }
            if (c == quote) {
                return 0;
            }
            if (c == '\\') {
                int n = validEscapeLength(input, i);
                if (n < 0) {
                    return i;
                }
                i += n - 1;
            }
        }
        return 0;
    }

    /**
     * @param la 1-based look-ahead index of the illegal {@code \}
     */
    static String illegalEscapeMessage(final CharStream input, final int la) {
        int next = input.LA(la + 1);
        String second = next == IntStream.EOF ? "" : displayCodePoint(next);
        return "Illegal escape character: '\\" + second + "'";
    }

    /**
     * Length of a Groovy string {@code EscapeSequence} (see
     * {@code GroovyLexer.g4}) starting at look-ahead {@code i} (the
     * {@code \}), or {@code -1} if that {@code \} is illegal. Keep in sync
     * with that fragment: {@code \btnfrs"'\\}, octal, backslash-u plus four
     * ASCII hex digits, {@code \$}, line continuation.
     */
    private static int validEscapeLength(final CharStream input, final int i) {
        int n = input.LA(i + 1);
        if (n == IntStream.EOF) {
            return -1;
        }
        return switch (n) {
            case 'b', 't', 'n', 'f', 'r', 's', '"', '\'', '\\', '$', '\n' -> 2;
            case '\r' -> input.LA(i + 2) == '\n' ? 3 : 2;
            case 'u' -> unicodeEscapeLength(input, i);
            default -> octalEscapeLength(n, input, i);
        };
    }

    /** {@code UnicodeEscape}: backslash-u plus four ASCII hex digits, else {@code -1}. */
    private static int unicodeEscapeLength(final CharStream input, final int backslashAt) {
        for (int h = 2; h <= 5; h++) {
            int d = input.LA(backslashAt + h);
            if (d == IntStream.EOF || !isAsciiHexDigit(d)) {
                return -1;
            }
        }
        return 6;
    }

    /**
     * {@code OctalEscape}: one to three octal digits; three only when the first
     * is {@code 0-3}.
     */
    private static int octalEscapeLength(final int firstDigit, final CharStream input, final int backslashAt) {
        if (firstDigit < '0' || firstDigit > '7') {
            return -1;
        }
        int n2 = input.LA(backslashAt + 2);
        if (n2 < '0' || n2 > '7') {
            return 2;
        }
        int n3 = input.LA(backslashAt + 3);
        if (firstDigit > '3' || n3 < '0' || n3 > '7') {
            return 3;
        }
        return 4;
    }

    /** Matches {@code HexDigit} in {@code GroovyLexer.g4}: {@code [0-9a-fA-F]}. */
    private static boolean isAsciiHexDigit(final int c) {
        return (c >= '0' && c <= '9')
                || (c >= 'a' && c <= 'f')
                || (c >= 'A' && c <= 'F');
    }

    /**
     * Report an unclosed {@code /}{@code *} comment at the opener (javac:
     * {@code unclosed comment}), not at EOF after the scan.
     *
     * @param errorIgnored when {@code true}, keep tokenising (IDE highlighting)
     */
    void requireUnclosedComment(final boolean errorIgnored) {
        requireAtTokenStart(errorIgnored, "Unclosed comment");
    }

    /**
     * Invalid octal ({@code 08}, {@code 09}, {@code 08L}) reports at the
     * token start. Using the start index rather than a running digit count keeps
     * the caret correct for a later invalid octal in the same file, and for a
     * suffix after the digits.
     *
     * @param errorIgnored when {@code true}, keep tokenising (IDE highlighting)
     */
    void requireInvalidOctal(final boolean errorIgnored) {
        requireAtTokenStart(errorIgnored, "Invalid octal number");
    }

    /**
     * {@link SyntaxErrorReportable#require} positions relative to the current
     * lexer cursor. After scanning to EOF that cursor is past the comment, so
     * the offset is token-start minus current (line and column).
     */
    private void requireAtTokenStart(final boolean errorIgnored, final String msg) {
        require(errorIgnored, msg,
                Tuple.tuple(_tokenStartLine - getLine(),
                        _tokenStartCharPositionInLine - getCharPositionInLine()),
                false);
    }

    /**
     * Quoted diagnostic form of a code point. Overrides ANTLR's UTF-16
     * {@code (char)} truncation and limited escapes so both unexpected-character
     * and GString-dollar messages can name invisible code points.
     */
    @Override
    public String getCharErrorDisplay(final int c) {
        if (c == Token.EOF) {
            return "'<EOF>'";
        }
        return quotedCodePoint(c);
    }

    private static String quotedCodePoint(final int cp) {
        return "'" + displayCodePoint(cp) + "'";
    }

    /**
     * Render {@code cp} for a diagnostic, matching javac's {@code Convert.quote}
     * for the standard escapes and using a UTF-16 unicode escape for other
     * non-printable / format / ignorable code points, Unicode spaces other
     * than {@code U+0020}, curly quotes, and non-ASCII dashes — those glyphs
     * are easy to confuse with ASCII in a caret line. Printable characters —
     * including non-ASCII such as emoji — are left as-is so the message stays
     * readable when the glyph is visible.
     */
    private static String displayCodePoint(final int cp) {
        switch (cp) {
            case '\b':
                return "\\b";
            case '\t':
                return "\\t";
            case '\n':
                return "\\n";
            case '\f':
                return "\\f";
            case '\r':
                return "\\r";
            case '\'':
                return "\\'";
            case '\\':
                return "\\\\";
            default:
                if (shouldEscape(cp)) {
                    if (cp <= 0xFFFF) {
                        return String.format("\\u%04x", cp);
                    }
                    return String.format("\\u%04x\\u%04x",
                            (int) Character.highSurrogate(cp),
                            (int) Character.lowSurrogate(cp));
                }
                return new String(Character.toChars(cp));
        }
    }

    private static boolean shouldEscape(final int cp) {
        if (cp < 0x20 || cp == 0x7F) {
            return true;
        }
        int type = Character.getType(cp);
        return type == Character.CONTROL
                || type == Character.FORMAT
                || type == Character.LINE_SEPARATOR
                || type == Character.PARAGRAPH_SEPARATOR
                || type == Character.SURROGATE
                || type == Character.PRIVATE_USE
                || type == Character.UNASSIGNED
                || (type == Character.SPACE_SEPARATOR && cp != ' ')
                || type == Character.INITIAL_QUOTE_PUNCTUATION
                || type == Character.FINAL_QUOTE_PUNCTUATION
                || (type == Character.DASH_PUNCTUATION && cp > 0x7F)
                || Character.isIdentifierIgnorable(cp);
    }
}
