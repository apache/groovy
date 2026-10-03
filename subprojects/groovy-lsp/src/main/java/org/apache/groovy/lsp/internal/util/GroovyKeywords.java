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

import org.antlr.v4.runtime.Vocabulary;
import org.apache.groovy.parser.antlr4.GroovyLexer;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * Keyword and modifier names taken from the ANTLR lexer vocabulary
 * ({@code src/antlr/GroovyLexer.g4}), not a handwritten list and not
 * {@code Types.getKeywords()} (that table is the old CST and lags the
 * parser). Primitive type names are lexer fragments under
 * {@code BuiltInPrimitiveType}; they are taken from {@link ClassHelper}.
 */
public final class GroovyKeywords {

    /**
     * Word-like lexer literals plus primitive type names, {@code true},
     * {@code false} and {@code null}, sorted.
     */
    public static final List<String> KEYWORDS = List.copyOf(keywords());

    /**
     * @param word a token text
     * @return {@code true} when {@code word} is a lexer keyword or primitive type
     */
    public static boolean isKeyword(final String word) {
        return word != null && KEYWORDS.contains(word);
    }

    /**
     * Declaration modifiers, using the lexer token types so the spelling
     * still comes from the vocabulary.
     */
    public static final List<String> MODIFIERS = List.copyOf(literals(
            GroovyLexer.ABSTRACT, GroovyLexer.FINAL, GroovyLexer.NATIVE,
            GroovyLexer.PRIVATE, GroovyLexer.PROTECTED, GroovyLexer.PUBLIC,
            GroovyLexer.STATIC, GroovyLexer.STRICTFP, GroovyLexer.SYNCHRONIZED,
            GroovyLexer.TRANSIENT, GroovyLexer.VOLATILE));

    private GroovyKeywords() {
    }

    private static TreeSet<String> keywords() {
        TreeSet<String> words = new TreeSet<>();
        Vocabulary vocabulary = GroovyLexer.VOCABULARY;
        for (int type = 1; type <= vocabulary.getMaxTokenType(); type++) {
            addVocabularyWord(words, vocabulary, type);
        }
        for (ClassNode primitive : List.of(
                ClassHelper.boolean_TYPE, ClassHelper.byte_TYPE, ClassHelper.char_TYPE,
                ClassHelper.short_TYPE, ClassHelper.int_TYPE, ClassHelper.long_TYPE,
                ClassHelper.float_TYPE, ClassHelper.double_TYPE, ClassHelper.VOID_TYPE)) {
            words.add(primitive.getName());
        }
        return words;
    }

    private static void addVocabularyWord(final TreeSet<String> words, final Vocabulary vocabulary, final int type) {
        String symbolic = vocabulary.getSymbolicName(type);
        if ("BooleanLiteral".equals(symbolic)) {
            words.add("true");
            words.add("false");
            return;
        }
        if ("NullLiteral".equals(symbolic)) {
            words.add("null");
            return;
        }
        String literal = unquote(vocabulary.getLiteralName(type));
        if (isWordKeyword(literal)) {
            words.add(literal);
        }
    }

    private static List<String> literals(final int... types) {
        List<String> words = new ArrayList<>();
        Vocabulary vocabulary = GroovyLexer.VOCABULARY;
        for (int type : types) {
            String literal = unquote(vocabulary.getLiteralName(type));
            if (literal != null && !literal.isEmpty()) {
                words.add(literal);
            }
        }
        words.sort(String::compareTo);
        return words;
    }

    private static String unquote(final String literal) {
        if (literal == null || literal.length() < 2 || literal.charAt(0) != '\''
                || literal.charAt(literal.length() - 1) != '\'') {
            return literal;
        }
        return literal.substring(1, literal.length() - 1);
    }

    private static boolean isWordKeyword(final String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        int first = text.codePointAt(0);
        if (!Character.isJavaIdentifierStart(first)) {
            return false;
        }
        int i = Character.charCount(first);
        while (i < text.length()) {
            int cp = text.codePointAt(i);
            if (cp != '-' && !Character.isJavaIdentifierPart(cp)) {
                return false;
            }
            i += Character.charCount(cp);
        }
        return true;
    }
}
