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
package org.apache.groovy.lsp.internal.feature;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.groovy.lsp.internal.util.GroovySourceTokens;
import org.apache.groovy.parser.antlr4.GroovyLangLexer;
import org.apache.groovy.parser.antlr4.GroovyLangParser;
import org.apache.groovy.parser.antlr4.GroovyLexer;
import org.apache.groovy.parser.antlr4.GroovySyntaxError;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lenient lexer tokens and a concrete syntax tree for an open buffer.
 * Hosts receive maps, not parser objects.
 */
public final class SyntaxView {

    private SyntaxView() {
    }

    /**
     * @param text buffer text, possibly incomplete
     * @return tokens in source order, never {@code null}
     */
    public static List<Map<String, Object>> tokens(final String text) {
        List<Map<String, Object>> rows = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return rows;
        }
        Vocabulary vocabulary = GroovyLexer.VOCABULARY;
        for (Token token : GroovySourceTokens.tokenizeLenient(text)) {
            if (token == null || token.getType() == Token.EOF) {
                continue;
            }
            String name = vocabulary.getSymbolicName(token.getType());
            if (name == null) {
                name = vocabulary.getDisplayName(token.getType());
            }
            String body = token.getText() == null ? "" : token.getText();
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("name", name == null ? "" : name);
            row.put("text", body);
            row.put("line", Math.max(token.getLine() - 1, 0));
            row.put("character", Math.max(token.getCharPositionInLine(), 0));
            row.put("length", body.length());
            rows.add(row);
        }
        return rows;
    }

    /**
     * @param text buffer text, possibly incomplete
     * @return a tree of {@code name}, {@code text} and {@code children}
     */
    public static Map<String, Object> cst(final String text) {
        String body = text == null ? "" : text;
        try {
            GroovyLangLexer lexer = new GroovyLangLexer(CharStreams.fromString(body));
            lexer.removeErrorListeners();
            lexer.setErrorIgnored(true);
            GroovyLangParser parser = new GroovyLangParser(new CommonTokenStream(lexer));
            parser.removeErrorListeners();
            return node(parser.compilationUnit(), parser);
        } catch (GroovySyntaxError | RuntimeException error) {
            return leaf("error", error.getMessage() == null ? "" : error.getMessage());
        }
    }

    private static Map<String, Object> node(final ParseTree tree, final GroovyLangParser parser) {
        if (tree instanceof TerminalNode terminal) {
            return terminalNode(terminal, parser);
        }
        ParserRuleContext rule = (ParserRuleContext) tree;
        String name = parser.getRuleNames()[rule.getRuleIndex()];
        List<Map<String, Object>> children = new ArrayList<>();
        for (int i = 0; i < tree.getChildCount(); i++) {
            children.add(node(tree.getChild(i), parser));
        }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("name", name);
        row.put("text", "");
        row.put("children", children);
        return row;
    }

    private static Map<String, Object> terminalNode(final TerminalNode terminal, final GroovyLangParser parser) {
        Token symbol = terminal.getSymbol();
        if (symbol == null) {
            return leaf("", "");
        }
        if (symbol.getType() == Token.EOF) {
            return leaf("EOF", "");
        }
        String name = parser.getVocabulary().getSymbolicName(symbol.getType());
        if (name == null) {
            name = parser.getVocabulary().getDisplayName(symbol.getType());
        }
        String text = symbol.getText() == null ? "" : symbol.getText();
        return leaf(name == null ? "" : name, text);
    }

    private static Map<String, Object> leaf(final String name, final String text) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("name", name);
        row.put("text", text);
        row.put("children", List.of());
        return row;
    }
}
