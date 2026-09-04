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
     * User-facing message for {@code UNEXPECTED_CHAR}.
     * <p>
     * An unexpected {@code '} or {@code "} is reported as an unclosed string
     * literal (javac: {@code unclosed string literal}). Other characters use
     * {@code Unexpected character: '...'} via {@link #getCharErrorDisplay(int)}.
     * </p>
     */
    String unexpectedCharacterMessage() {
        return unexpectedCharacterMessage(getText());
    }

    /**
     * @param text the unexpected character(s), or {@code null}/{@code ""} if the
     *             lexer has no current text (still reported, without a glyph)
     */
    static String unexpectedCharacterMessage(final String text) {
        if (text == null || text.isEmpty()) {
            return "Unexpected character";
        }
        int cp = text.codePointAt(0);
        if (cp == '\'' || cp == '"') {
            return "Unclosed string literal";
        }
        return "Unexpected character: " + quotedCodePoint(cp);
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
