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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Branch coverage for {@link AbstractLexer} unexpected-character diagnostics
 * and {@link AbstractLexer#getCharErrorDisplay(int)}. End-to-end messages live
 * in {@code CommonSyntaxErrorTest}.
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
    void requireUnclosedCommentPointsAtOpener() {
        GroovyLangLexer lexer = new GroovyLangLexer(CharStreams.fromString("/* comment"));
        GroovySyntaxError err = assertThrows(GroovySyntaxError.class, () -> drain(lexer));
        assertEquals("Unclosed comment", err.getMessage());
        assertEquals(1, err.getLine());
        assertEquals(1, err.getColumn());
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
    void requireUnclosedCommentPointsAtIndentedOpener() {
        GroovyLangLexer lexer = new GroovyLangLexer(CharStreams.fromString("    /* comment"));
        GroovySyntaxError err = assertThrows(GroovySyntaxError.class, () -> drain(lexer));
        assertEquals("Unclosed comment", err.getMessage());
        assertEquals(1, err.getLine());
        assertEquals(5, err.getColumn());
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

    private static GroovyLangLexer displayLexer() {
        return new GroovyLangLexer(CharStreams.fromString("x"));
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

    private static List<String> texts(final List<Token> tokens) {
        List<String> texts = new ArrayList<>(tokens.size());
        for (Token t : tokens) {
            texts.add(t.getText());
        }
        return texts;
    }
}
