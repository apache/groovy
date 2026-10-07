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
package org.apache.groovy.lsp.internal.util;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.apache.groovy.parser.antlr4.GroovyLexer;
import org.apache.groovy.parser.antlr4.GroovySyntaxError;

import java.util.ArrayList;
import java.util.List;

/**
 * Live {@link GroovyLexer} walks over a buffer. Used as a fail-closed
 * gate for on-type {@code '}'}', comment-based folds, and lexical
 * semantic tokens (string / number / comment).
 */
public final class GroovySourceTokens {

    private GroovySourceTokens() {
    }

    /**
     * Token covering {@code utf16Offset}, including hidden-channel comments.
     *
     * @param text document text
     * @param utf16Offset UTF-16 offset
     * @return the token, or {@code null}
     */
    public static Token covering(final String text, final int utf16Offset) {
        if (text == null || utf16Offset < 0 || utf16Offset >= text.length()) {
            return null;
        }
        try {
            GroovyLexer lexer = lexer(text);
            for (Token token = lexer.nextToken(); token.getType() != Token.EOF; token = lexer.nextToken()) {
                if (token.getStartIndex() <= utf16Offset && utf16Offset <= token.getStopIndex()) {
                    return token;
                }
                if (token.getStopIndex() >= utf16Offset) {
                    return null;
                }
            }
        } catch (GroovySyntaxError | RuntimeException ignored) {
            return null;
        }
        return null;
    }

    /**
     * Whether the character at {@code utf16Offset} is a structural
     * {@code GroovyLexer.RBRACE}, not a {@code '}'}' inside a string,
     * GString, slashy regex, or comment.
     *
     * @param text document text
     * @param utf16Offset UTF-16 offset of {@code '}'}'
     * @return {@code true} when outdent is safe
     */
    public static boolean isStructuralRbrace(final String text, final int utf16Offset) {
        if (text == null || utf16Offset < 0 || utf16Offset >= text.length()
                || text.charAt(utf16Offset) != '}') {
            return false;
        }
        Token token = covering(text, utf16Offset);
        return token != null && token.getType() == GroovyLexer.RBRACE;
    }

    /**
     * Full token walk including hidden-channel tokens.
     *
     * @param text document text
     * @return tokens, never {@code null}
     */
    public static List<Token> tokenize(final String text) {
        List<Token> tokens = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return tokens;
        }
        try {
            GroovyLexer lexer = lexer(text);
            for (Token token = lexer.nextToken(); token.getType() != Token.EOF; token = lexer.nextToken()) {
                tokens.add(token);
            }
        } catch (GroovySyntaxError | RuntimeException ignored) {
            return List.of();
        }
        return tokens;
    }

    private static GroovyLexer lexer(final String text) {
        GroovyLexer lexer = new GroovyLexer(CharStreams.fromString(text));
        lexer.removeErrorListeners();
        return lexer;
    }
}
