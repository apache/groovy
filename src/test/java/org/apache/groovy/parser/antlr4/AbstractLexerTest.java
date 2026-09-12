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

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.apache.groovy.parser.antlr4.GroovyLexer.CapitalizedIdentifier;
import static org.apache.groovy.parser.antlr4.GroovyLexer.FloatingPointLiteral;
import static org.apache.groovy.parser.antlr4.GroovyLexer.GStringBegin;
import static org.apache.groovy.parser.antlr4.GroovyLexer.GStringEnd;
import static org.apache.groovy.parser.antlr4.GroovyLexer.GStringPathPart;
import static org.apache.groovy.parser.antlr4.GroovyLexer.Identifier;
import static org.apache.groovy.parser.antlr4.GroovyLexer.IntegerLiteral;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Branch coverage for {@link AbstractLexer} unexpected-character diagnostics
 * and {@link AbstractLexer#getCharErrorDisplay(int)}. End-to-end messages live
 * in {@code ParserNegativeSyntaxTest}.
 */
final class AbstractLexerTest {

    @Test
    void unexpectedCharacterMessageNullOrEmpty() {
        assertEquals("Unexpected character", AbstractLexer.unexpectedCharacterMessage(null));
        assertEquals("Unexpected character", AbstractLexer.unexpectedCharacterMessage(""));
    }

    @Test
    void unexpectedQuoteIsUnclosedString() {
        assertEquals("Unclosed string literal", AbstractLexer.unexpectedCharacterMessage("'"));
        assertEquals("Unclosed string literal", AbstractLexer.unexpectedCharacterMessage("\""));
        assertEquals("Unclosed string literal", AbstractLexer.unexpectedCharacterMessage("'''"));
    }

    @Test
    void closedStringWithIllegalEscapeIsNotUnclosed() {
        var rest = CharStreams.fromString("C:\\Users\\me\"");
        assertEquals("Illegal escape character: '\\U'",
                AbstractLexer.unexpectedCharacterMessage("\"", rest));
        assertEquals(3, AbstractLexer.illegalEscapeLaIndex(rest, '"'));
        var sq = CharStreams.fromString("C:\\Users\\me'");
        assertEquals("Illegal escape character: '\\U'",
                AbstractLexer.unexpectedCharacterMessage("'", sq));
    }

    @Test
    void illegalEscapeAtStartOfString() {
        var rest = CharStreams.fromString("\\q\"");
        assertEquals("Illegal escape character: '\\q'",
                AbstractLexer.unexpectedCharacterMessage("\"", rest));
        assertEquals(1, AbstractLexer.illegalEscapeLaIndex(rest, '"'));
    }

    @Test
    void illegalIncompleteUnicodeEscape() {
        var rest = CharStreams.fromString("\\u12\"");
        assertEquals("Illegal escape character: '\\u'",
                AbstractLexer.unexpectedCharacterMessage("\"", rest));
    }

    @Test
    void legalEscapesAreNotIllegal() {
        assertEquals(0, AbstractLexer.illegalEscapeLaIndex(CharStreams.fromString("a\\n\\t\\\\b\""), '"'));
        assertEquals(0, AbstractLexer.illegalEscapeLaIndex(CharStreams.fromString("\\b\\f\\s\\r\""), '"'));
        assertEquals(0, AbstractLexer.illegalEscapeLaIndex(CharStreams.fromString("\\u0041\""), '"'));
        assertEquals(0, AbstractLexer.illegalEscapeLaIndex(CharStreams.fromString("\\u00AB\""), '"'));
        assertEquals(0, AbstractLexer.illegalEscapeLaIndex(CharStreams.fromString("\\u005cq\""), '"'));
        assertEquals(0, AbstractLexer.illegalEscapeLaIndex(CharStreams.fromString("\\0\""), '"'));
        assertEquals(0, AbstractLexer.illegalEscapeLaIndex(CharStreams.fromString("\\7\""), '"'));
        assertEquals(0, AbstractLexer.illegalEscapeLaIndex(CharStreams.fromString("\\077\""), '"'));
        assertEquals(0, AbstractLexer.illegalEscapeLaIndex(CharStreams.fromString("\\377\""), '"'));
        assertEquals(0, AbstractLexer.illegalEscapeLaIndex(CharStreams.fromString("\\477\""), '"'));
        assertEquals(0, AbstractLexer.illegalEscapeLaIndex(CharStreams.fromString("\\08\""), '"'));
        assertEquals(0, AbstractLexer.illegalEscapeLaIndex(CharStreams.fromString("\\$\""), '"'));
        assertEquals(0, AbstractLexer.illegalEscapeLaIndex(CharStreams.fromString("\\'\""), '"'));
        assertEquals(0, AbstractLexer.illegalEscapeLaIndex(CharStreams.fromString("hello"), '"'));
        assertEquals(0, AbstractLexer.illegalEscapeLaIndex(null, '"'));
        assertEquals(0, AbstractLexer.illegalEscapeLaIndex(CharStreams.fromString("a\\\nb\""), '"'));
        assertEquals(0, AbstractLexer.illegalEscapeLaIndex(CharStreams.fromString("a\\\r\nb\""), '"'));
        assertEquals(0, AbstractLexer.illegalEscapeLaIndex(CharStreams.fromString("a\\\rb\""), '"'));
    }

    @Test
    void escapedQuoteIsNotTheCloser() {
        // \" must be skipped so the real closer is found; no illegal escape
        assertEquals(0, AbstractLexer.illegalEscapeLaIndex(CharStreams.fromString("a\\\"b\""), '"'));
    }

    @Test
    void illegalOctalEight() {
        var rest = CharStreams.fromString("\\8\"");
        assertEquals("Illegal escape character: '\\8'",
                AbstractLexer.unexpectedCharacterMessage("\"", rest));
    }

    @Test
    void backslashAtEofIsIllegalEscape() {
        var rest = CharStreams.fromString("ab\\");
        assertEquals(3, AbstractLexer.illegalEscapeLaIndex(rest, '"'));
        assertEquals("Illegal escape character: '\\'",
                AbstractLexer.illegalEscapeMessage(rest, 3));
    }

    @ParameterizedTest
    @MethodSource("requirePositionCases")
    void requirePositionsTheDiagnostic(final String src, final String message, final int line, final int column) {
        GroovyLangLexer lexer = new GroovyLangLexer(CharStreams.fromString(src));
        GroovySyntaxError err = assertThrows(GroovySyntaxError.class, () -> drain(lexer));
        assertEquals(message, err.getMessage());
        assertEquals(line, err.getLine());
        assertEquals(column, err.getColumn());
    }

    private static Stream<Arguments> requirePositionCases() {
        return Stream.of(
                // 1-based: " C : \   — caret on the illegal backslash
                Arguments.of("\"C:\\Users\\me\"", "Illegal escape character: '\\U'", 1, 4),
                Arguments.of("\"C:\\Users", "Illegal escape character: '\\U'", 1, 4),
                Arguments.of("/* comment", "Unclosed comment", 1, 1),
                Arguments.of("    /* comment", "Unclosed comment", 1, 5)
        );
    }

    @Test
    void twoDigitOctalIsLegal() {
        assertEquals(0, AbstractLexer.illegalEscapeLaIndex(CharStreams.fromString("\\77x\""), '"'));
    }

    @Test
    void scanAheadGivesUpOnAVeryLongLine() {
        char[] buf = new char[AbstractLexer.ILLEGAL_ESCAPE_SCAN_LIMIT + 8];
        Arrays.fill(buf, 'a');
        assertEquals(0, AbstractLexer.illegalEscapeLaIndex(CharStreams.fromString(new String(buf)), '"'));
    }

    @Test
    void scanAheadFindsIllegalEscapeAtLimit() {
        char[] buf = new char[AbstractLexer.ILLEGAL_ESCAPE_SCAN_LIMIT];
        Arrays.fill(buf, 'a');
        buf[AbstractLexer.ILLEGAL_ESCAPE_SCAN_LIMIT - 1] = '\\';
        assertEquals(AbstractLexer.ILLEGAL_ESCAPE_SCAN_LIMIT,
                AbstractLexer.illegalEscapeLaIndex(CharStreams.fromString(new String(buf)), '"'));
    }

    @Test
    void scanAheadMissesIllegalEscapePastLimit() {
        char[] buf = new char[AbstractLexer.ILLEGAL_ESCAPE_SCAN_LIMIT + 2];
        Arrays.fill(buf, 'a');
        buf[AbstractLexer.ILLEGAL_ESCAPE_SCAN_LIMIT] = '\\';
        buf[AbstractLexer.ILLEGAL_ESCAPE_SCAN_LIMIT + 1] = 'q';
        assertEquals(0, AbstractLexer.illegalEscapeLaIndex(CharStreams.fromString(new String(buf)), '"'));
    }

    @Test
    void newlineStopsScanEvenIfCloserFollows() {
        assertEquals(0, AbstractLexer.illegalEscapeLaIndex(CharStreams.fromString("hello\nworld\\q\""), '"'));
        assertEquals(0, AbstractLexer.illegalEscapeLaIndex(CharStreams.fromString("hello\rworld\\q\""), '"'));
    }

    @Test
    void mismatchedCloserIsNotACloser() {
        assertEquals(0, AbstractLexer.illegalEscapeLaIndex(CharStreams.fromString("hello'"), '"'));
        assertEquals("Illegal escape character: '\\q'",
                AbstractLexer.unexpectedCharacterMessage("\"", CharStreams.fromString("hello\\q'")));
    }

    @Test
    void firstIllegalEscapeWins() {
        var rest = CharStreams.fromString("a\\q\\z\"");
        assertEquals(2, AbstractLexer.illegalEscapeLaIndex(rest, '"'));
        assertEquals("Illegal escape character: '\\q'",
                AbstractLexer.unexpectedCharacterMessage("\"", rest));
    }

    @Test
    void illegalEscapeAfterLegalOnes() {
        var rest = CharStreams.fromString("a\\n\\t\\q\"");
        assertEquals(6, AbstractLexer.illegalEscapeLaIndex(rest, '"'));
        assertEquals("Illegal escape character: '\\q'",
                AbstractLexer.unexpectedCharacterMessage("\"", rest));
    }

    @Test
    void extraUInUnicodeEscapeIsIllegal() {
        // UnicodeEscape is a single 'u' then four hex digits; a second u is not hex
        var rest = CharStreams.fromString("\\uu0041\"");
        assertEquals("Illegal escape character: '\\u'",
                AbstractLexer.unexpectedCharacterMessage("\"", rest));
    }

    @Test
    void unicodeEscapeRequiresAsciiHexDigits() {
        var rest = CharStreams.fromString("\\u004g\"");
        assertEquals("Illegal escape character: '\\u'",
                AbstractLexer.unexpectedCharacterMessage("\"", rest));
        // FULLWIDTH DIGIT ZERO is not HexDigit [0-9a-fA-F]
        var fw = CharStreams.fromString("\\u\uff10\uff10\uff10\uff10\"");
        assertEquals("Illegal escape character: '\\u'",
                AbstractLexer.unexpectedCharacterMessage("\"", fw));
    }

    @Test
    void illegalEscapeOfControlCharacterUsesUnicodeDisplay() {
        var rest = CharStreams.fromString("\\\u0001\"");
        assertEquals(1, AbstractLexer.illegalEscapeLaIndex(rest, '"'));
        // backslash + displayCodePoint(SOH); SOH is shown as \u0001
        assertEquals("Illegal escape character: '\\\\u0001'",
                AbstractLexer.illegalEscapeMessage(rest, 1));
    }

    @Test
    void instanceMessageUsesScanAheadAfterQuoteToken() {
        GroovyLangLexer lexer = new GroovyLangLexer(CharStreams.fromString("\"C:\\Users\\me\""));
        GroovySyntaxError err = assertThrows(GroovySyntaxError.class, () -> drain(lexer));
        assertEquals("Illegal escape character: '\\U'", err.getMessage());
        assertEquals("Illegal escape character: '\\U'", lexer.unexpectedCharacterMessage());
    }

    @Test
    void escapedWindowsPathTokenizes() {
        List<Token> tokens = collect("x = \"C:\\\\Users\\\\me\"");
        assertEquals(Token.EOF, tokens.get(tokens.size() - 1).getType());
    }

    @Test
    void errorIgnoredUnclosedQuoteTokenizesWithoutThrowing() {
        GroovyLangLexer lexer = new GroovyLangLexer(CharStreams.fromString("'hello"));
        lexer.setErrorIgnored(true);
        List<Token> tokens = assertDoesNotThrow(() -> collect(lexer));
        assertEquals(Token.EOF, tokens.get(tokens.size() - 1).getType());
    }

    @Test
    void errorIgnoredIllegalEscapeTokenizesWithoutThrowing() {
        GroovyLangLexer lexer = new GroovyLangLexer(CharStreams.fromString("\"\\q\""));
        lexer.setErrorIgnored(true);
        List<Token> tokens = assertDoesNotThrow(() -> collect(lexer));
        assertEquals(Token.EOF, tokens.get(tokens.size() - 1).getType());
    }

    @Test
    void unexpectedPrintableStaysAsGlyph() {
        assertEquals("Unexpected character: '`'", AbstractLexer.unexpectedCharacterMessage("`"));
        assertEquals("Unexpected character: '#'", AbstractLexer.unexpectedCharacterMessage("#"));
        assertEquals("Unexpected character: '😀'", AbstractLexer.unexpectedCharacterMessage("😀"));
    }

    @Test
    void getCharErrorDisplayNamedEscapes() {
        GroovyLangLexer lexer = displayLexer();
        assertEquals("'\\b'", lexer.getCharErrorDisplay('\b'));
        assertEquals("'\\t'", lexer.getCharErrorDisplay('\t'));
        assertEquals("'\\n'", lexer.getCharErrorDisplay('\n'));
        assertEquals("'\\f'", lexer.getCharErrorDisplay('\f'));
        assertEquals("'\\r'", lexer.getCharErrorDisplay('\r'));
        assertEquals("'\\''", lexer.getCharErrorDisplay('\''));
        assertEquals("'\\\\'", lexer.getCharErrorDisplay('\\'));
        assertEquals("'<EOF>'", lexer.getCharErrorDisplay(Token.EOF));
    }

    @Test
    void getCharErrorDisplayUnicodeEscapesForInvisible() {
        GroovyLangLexer lexer = displayLexer();
        assertEquals("'\\u0000'", lexer.getCharErrorDisplay(0));
        assertEquals("'\\u007f'", lexer.getCharErrorDisplay(0x7F));
        assertEquals("'\\u200b'", lexer.getCharErrorDisplay(0x200B));
        assertEquals("'\\ufeff'", lexer.getCharErrorDisplay(0xFEFF));
        assertEquals("'\\u2028'", lexer.getCharErrorDisplay(0x2028));
        assertEquals("'\\u2029'", lexer.getCharErrorDisplay(0x2029));
        assertEquals("'\\ud800'", lexer.getCharErrorDisplay(0xD800));
        assertEquals("'\\ue000'", lexer.getCharErrorDisplay(0xE000));
        assertEquals("'\\u0378'", lexer.getCharErrorDisplay(0x0378));
        assertEquals("'\\u00ad'", lexer.getCharErrorDisplay(0x00AD));
        assertEquals("'\\u00a0'", lexer.getCharErrorDisplay(0x00A0));
        assertEquals("'\\u2000'", lexer.getCharErrorDisplay(0x2000));
        assertEquals("'\\u202f'", lexer.getCharErrorDisplay(0x202F));
        assertEquals("'\\u2018'", lexer.getCharErrorDisplay(0x2018));
        assertEquals("'\\u2019'", lexer.getCharErrorDisplay(0x2019));
        assertEquals("'\\u201c'", lexer.getCharErrorDisplay(0x201C));
        assertEquals("'\\u201d'", lexer.getCharErrorDisplay(0x201D));
        assertEquals("'\\u2013'", lexer.getCharErrorDisplay(0x2013));
        assertEquals("'\\u2014'", lexer.getCharErrorDisplay(0x2014));
        assertEquals("'\\u0080'", lexer.getCharErrorDisplay(0x80));
    }

    @Test
    void getCharErrorDisplaySupplementaryPrivateUseAsUtf16Escapes() {
        assertEquals("'\\udb80\\udc00'", displayLexer().getCharErrorDisplay(0xF0000));
    }

    @Test
    void getCharErrorDisplayPrintableGlyphs() {
        GroovyLangLexer lexer = displayLexer();
        assertEquals("'A'", lexer.getCharErrorDisplay('A'));
        assertEquals("'`'", lexer.getCharErrorDisplay('`'));
        assertEquals("' '", lexer.getCharErrorDisplay(' '));
        assertEquals("'-'", lexer.getCharErrorDisplay('-'));
        assertEquals("'😀'", lexer.getCharErrorDisplay("😀".codePointAt(0)));
    }

    @Test
    void unexpectedCharacterMessageEscapesNbspAndCurlyQuote() {
        assertEquals("Unexpected character: '\\u00a0'",
                AbstractLexer.unexpectedCharacterMessage("\u00A0"));
        assertEquals("Unexpected character: '\\u2018'",
                AbstractLexer.unexpectedCharacterMessage("\u2018"));
        assertEquals("Unexpected character: '\\u2014'",
                AbstractLexer.unexpectedCharacterMessage("\u2014"));
    }

    @Test
    void errorIgnoredUnexpectedCharacterTokenizesWithoutThrowing() {
        GroovyLangLexer lexer = new GroovyLangLexer(CharStreams.fromString("`"));
        lexer.setErrorIgnored(true);
        Token t = lexer.nextToken();
        assertTrue(t.getType() > 0);
        assertEquals(Token.EOF, lexer.nextToken().getType());
    }

    @Test
    void lexerThrowsGroovySyntaxErrorForUnexpectedCharacter() {
        GroovyLangLexer lexer = new GroovyLangLexer(CharStreams.fromString("`"));
        GroovySyntaxError err = assertThrows(GroovySyntaxError.class, lexer::nextToken);
        assertEquals("Unexpected character: '`'", err.getMessage());
        assertEquals(GroovySyntaxError.LEXER, err.getSource());
    }

    @Test
    void lexerThrowsUnclosedStringForLoneQuote() {
        GroovyLangLexer lexer = new GroovyLangLexer(CharStreams.fromString("'hello"));
        GroovySyntaxError err = assertThrows(GroovySyntaxError.class, () -> drain(lexer));
        assertEquals("Unclosed string literal", err.getMessage());
    }

    @Test
    void errorIgnoredUnclosedCommentTokenizesWithoutThrowing() {
        GroovyLangLexer lexer = new GroovyLangLexer(CharStreams.fromString("/* comment"));
        lexer.setErrorIgnored(true);
        List<Token> tokens = assertDoesNotThrow(() -> collect(lexer));
        assertEquals(Token.EOF, tokens.get(tokens.size() - 1).getType());
        assertTrue(tokens.size() >= 2, tokens.toString());
    }

    @Test
    void closedBlockCommentDoesNotConsumeTrailingSource() {
        List<Token> tokens = collect("/* ok */\ndef x = 1\n");
        assertEquals("/* ok */", tokens.get(0).getText());
        assertTrue(texts(tokens).contains("def"), texts(tokens).toString());
        assertTrue(texts(tokens).contains("x"), texts(tokens).toString());
        assertEquals(Token.EOF, tokens.get(tokens.size() - 1).getType());
    }

    @Test
    void closedBlockCommentStopsAtFirstCloser() {
        List<Token> tokens = collect("/* a /* nested */ def x = 1\n");
        assertEquals("/* a /* nested */", tokens.get(0).getText());
        assertTrue(texts(tokens).contains("def"), texts(tokens).toString());
        assertFalse(texts(tokens).get(0).contains("def"), tokens.get(0).getText());
    }

    @Test
    void emptyAndGroovydocBlockCommentsLeaveFollowingSource() {
        List<Token> tokens = collect("/**/\n/** groovydoc */\ndef y = 2\n");
        assertEquals("/**/", tokens.get(0).getText());
        assertTrue(texts(tokens).contains("def"), texts(tokens).toString());
        assertTrue(texts(tokens).contains("y"), texts(tokens).toString());
    }

    @Test
    void closedBlockCommentAtEofDoesNotThrow() {
        List<Token> tokens = collect("/* ok */");
        assertEquals("/* ok */", tokens.get(0).getText());
        assertEquals(Token.EOF, tokens.get(tokens.size() - 1).getType());
    }

    @Test
    void asciiIdentifierSplitIsPureDfa() {
        assertEquals(CapitalizedIdentifier, firstDefaultChannel("Foo").getType());
        assertEquals(CapitalizedIdentifier, firstDefaultChannel("A").getType());
        assertEquals(Identifier, firstDefaultChannel("foo").getType());
        assertEquals(Identifier, firstDefaultChannel("$foo").getType());
        assertEquals(Identifier, firstDefaultChannel("_foo").getType());
    }

    @Test
    void unicodeIdentifierSplitFollowsUppercase() {
        assertEquals(CapitalizedIdentifier, firstDefaultChannel("Äbc").getType());
        assertEquals(Identifier, firstDefaultChannel("λ").getType());
        // title case is not uppercase — still Identifier
        assertEquals(Identifier, firstDefaultChannel("\u01C5").getType());
    }

    /**
     * GROOVY-12398: after a surrogate pair, {@code LA(-1)} is the low
     * surrogate (never uppercase). Classification must use the code point.
     * U+10400 DESERET CAPITAL LETTER LONG I / U+10428 DESERET SMALL LETTER LONG I.
     */
    @Test
    void supplementaryPlaneIdentifierSplitUsesCodePoint() {
        assertEquals(CapitalizedIdentifier, firstDefaultChannel("\uD801\uDC00").getType());
        assertEquals(Identifier, firstDefaultChannel("\uD801\uDC28").getType());
        assertEquals(CapitalizedIdentifier, firstDefaultChannel("\uD801\uDC00Name").getType());
        assertEquals(Identifier, firstDefaultChannel("\uD801\uDC28name").getType());
    }

    @Test
    void invalidOctalReportsAtTokenStart() {
        GroovyLangLexer firstLexer = lexer("08");
        GroovySyntaxError first = assertThrows(GroovySyntaxError.class, () -> drain(firstLexer));
        assertEquals(1, first.getLine());
        assertEquals(1, first.getColumn());

        GroovyLangLexer suffixLexer = lexer("08L");
        GroovySyntaxError withSuffix = assertThrows(GroovySyntaxError.class, () -> drain(suffixLexer));
        assertEquals(1, withSuffix.getColumn());

        GroovyLangLexer prefixLexer = lexer("x=08");
        GroovySyntaxError afterPrefix = assertThrows(GroovySyntaxError.class, () -> drain(prefixLexer));
        assertEquals(3, afterPrefix.getColumn());

        // first of two invalid octals still points at column 1, not a drifted count
        GroovyLangLexer twoLexer = lexer("08 09");
        GroovySyntaxError two = assertThrows(GroovySyntaxError.class, () -> drain(twoLexer));
        assertEquals(1, two.getColumn());
    }

    @Test
    void errorIgnoredTokenizesSuccessiveInvalidOctals() {
        GroovyLangLexer lexer = new GroovyLangLexer(CharStreams.fromString("08 09 08L"));
        lexer.setErrorIgnored(true);
        List<Token> tokens = defaultChannel(lexer);
        assertEquals(3, tokens.size(), texts(tokens).toString());
        assertEquals(IntegerLiteral, tokens.get(0).getType());
        assertEquals(IntegerLiteral, tokens.get(1).getType());
        assertEquals(IntegerLiteral, tokens.get(2).getType());
        assertEquals("08L", tokens.get(2).getText());
    }

    @Test
    void flattenedNumberLiteralsStillMatch() {
        assertEquals(IntegerLiteral, firstDefaultChannel("0").getType());
        assertEquals(IntegerLiteral, firstDefaultChannel("123").getType());
        assertEquals(IntegerLiteral, firstDefaultChannel("1_000").getType());
        assertEquals(IntegerLiteral, firstDefaultChannel("0xFF").getType());
        assertEquals(IntegerLiteral, firstDefaultChannel("0b101").getType());
        assertEquals(IntegerLiteral, firstDefaultChannel("07").getType());
        assertEquals(IntegerLiteral, firstDefaultChannel("1G").getType());
        assertEquals(FloatingPointLiteral, firstDefaultChannel("1.5").getType());
        assertEquals(FloatingPointLiteral, firstDefaultChannel(".5").getType());
        assertEquals(FloatingPointLiteral, firstDefaultChannel("1e10").getType());
        assertEquals(FloatingPointLiteral, firstDefaultChannel("0x1p1").getType());
        assertEquals(FloatingPointLiteral, firstDefaultChannel("1.5G").getType());
    }

    @Test
    void gstringPathStillUsesDotFragmentReplacement() {
        List<Token> tokens = defaultChannel("\"$foo.bar\"");
        List<Integer> types = new ArrayList<>();
        for (Token t : tokens) {
            types.add(t.getType());
        }
        assertTrue(types.contains(GStringBegin), types.toString());
        assertTrue(types.contains(GStringPathPart), types.toString());
        assertTrue(types.contains(GStringEnd), types.toString());
    }

    private static GroovyLangLexer displayLexer() {
        return new GroovyLangLexer(CharStreams.fromString("x"));
    }

    private static GroovyLangLexer lexer(final String src) {
        return new GroovyLangLexer(CharStreams.fromString(src));
    }

    private static void drain(GroovyLangLexer lexer) {
        collect(lexer);
    }

    private static List<Token> collect(final String src) {
        return collect(new GroovyLangLexer(CharStreams.fromString(src)));
    }

    private static List<Token> collect(final GroovyLangLexer lexer) {
        List<Token> tokens = new ArrayList<>();
        Token t;
        do {
            t = lexer.nextToken();
            tokens.add(t);
        } while (t.getType() != Token.EOF);
        return tokens;
    }

    private static Token firstDefaultChannel(final String src) {
        return defaultChannel(src).get(0);
    }

    private static List<Token> defaultChannel(final String src) {
        return defaultChannel(new GroovyLangLexer(CharStreams.fromString(src)));
    }

    private static List<Token> defaultChannel(final GroovyLangLexer lexer) {
        List<Token> tokens = new ArrayList<>();
        for (Token t : collect(lexer)) {
            if (t.getType() != Token.EOF && t.getChannel() == Token.DEFAULT_CHANNEL) {
                tokens.add(t);
            }
        }
        return tokens;
    }

    private static List<String> texts(final List<Token> tokens) {
        List<String> texts = new ArrayList<>(tokens.size());
        for (Token t : tokens) {
            texts.add(t.getText());
        }
        return texts;
    }
}
