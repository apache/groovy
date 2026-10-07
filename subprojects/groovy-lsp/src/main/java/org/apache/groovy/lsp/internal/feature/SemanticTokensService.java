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

import org.antlr.v4.runtime.Token;
import org.apache.groovy.lsp.internal.compile.AstQuery;
import org.apache.groovy.lsp.internal.compile.CompiledDocument;
import org.apache.groovy.lsp.internal.position.PositionEncoding;
import org.apache.groovy.lsp.internal.position.Positions;
import org.apache.groovy.lsp.internal.util.GroovyKeywords;
import org.apache.groovy.lsp.internal.util.GroovySourceTokens;
import org.apache.groovy.parser.antlr4.GroovyLexer;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.PackageNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SemanticTokens;
import org.eclipse.lsp4j.SemanticTokensLegend;

import java.util.ArrayList;
import java.util.List;

/**
 * Semantic tokens for a compiled Groovy document.
 */
public final class SemanticTokensService {

    public static final List<String> TOKEN_TYPES = List.of(
            "namespace", "class", "enum", "interface", "method", "property",
            "variable", "parameter", "keyword", "string", "number", "comment"
    );
    public static final List<String> TOKEN_MODIFIERS = List.of("declaration", "static", "deprecated");

    private static final int TOKEN_NAMESPACE = 0;
    private static final int TOKEN_CLASS = 1;
    private static final int TOKEN_ENUM = 2;
    private static final int TOKEN_INTERFACE = 3;
    private static final int TOKEN_METHOD = 4;
    private static final int TOKEN_PROPERTY = 5;
    private static final int TOKEN_VARIABLE = 6;
    private static final int TOKEN_PARAMETER = 7;
    private static final int TOKEN_KEYWORD = 8;
    private static final int TOKEN_STRING = 9;
    private static final int TOKEN_NUMBER = 10;
    private static final int TOKEN_COMMENT = 11;
    private static final int MOD_DECLARATION = 1;
    private static final int MOD_STATIC = 2;
    private static final int MOD_DEPRECATED = 4;

    /**
     * One absolute semantic-token span. {@code line} and {@code character}
     * are 0-based in the negotiated encoding. {@link #encode} is the only
     * place that turns these into protocol deltas.
     *
     * @param line 0-based line
     * @param character 0-based character
     * @param length span length in that encoding
     * @param type index into {@link #TOKEN_TYPES}
     * @param modifiers bit set of {@link #TOKEN_MODIFIERS}
     */
    public record TokenSpan(int line, int character, int length, int type, int modifiers) {
    }

    public static SemanticTokensLegend legend() {
        return new SemanticTokensLegend(TOKEN_TYPES, TOKEN_MODIFIERS);
    }

    /**
     * Absolute token spans for {@code document}.
     *
     * @param document compiled document
     * @param encoding negotiated encoding
     * @return spans, never {@code null}
     */
    public List<TokenSpan> tokenSpans(final CompiledDocument document, final PositionEncoding encoding) {
        return tokenSpans(document, encoding, null);
    }

    /**
     * Absolute token spans that overlap {@code range}.
     *
     * @param document compiled document
     * @param encoding negotiated encoding
     * @param range optional filter
     * @return spans, never {@code null}
     */
    public List<TokenSpan> tokenSpans(final CompiledDocument document, final PositionEncoding encoding,
                                      final Range range) {
        if (document == null) {
            return List.of();
        }
        List<TokenSpan> tokens = new ArrayList<>();
        ModuleNode module = document.getModule();
        if (module != null) {
            for (ASTNode node : AstQuery.declarations(module)) {
                addDeclarationToken(tokens, document, encoding, range, node);
            }
            addParameterTokens(tokens, document, encoding, range, module);
            addUseSiteTokens(tokens, document, encoding, range, module);
        }
        addLexicalTokens(document.getText(), tokens, encoding, range);
        tokens.sort((left, right) -> {
            if (left.line() != right.line()) {
                return Integer.compare(left.line(), right.line());
            }
            if (left.character() != right.character()) {
                return Integer.compare(left.character(), right.character());
            }
            return Integer.compare(right.length(), left.length());
        });
        dedupeSameSpan(tokens);
        return List.copyOf(tokens);
    }

    /**
     * Delta-encodes absolute spans the way LSP {@code semanticTokens} requires.
     * A span that would move backwards is dropped.
     *
     * @param tokens absolute spans, or {@code null}
     * @return protocol tokens
     */
    public static SemanticTokens encode(final List<TokenSpan> tokens) {
        List<Integer> data = new ArrayList<>();
        if (tokens == null || tokens.isEmpty()) {
            return new SemanticTokens(data);
        }
        int prevLine = 0;
        int prevChar = 0;
        for (TokenSpan token : tokens) {
            int dLine = token.line() - prevLine;
            int dChar = dLine == 0 ? token.character() - prevChar : token.character();
            if (dLine < 0 || dChar < 0) {
                continue;
            }
            data.add(dLine);
            data.add(dChar);
            data.add(token.length());
            data.add(token.type());
            data.add(token.modifiers());
            prevLine = token.line();
            prevChar = token.character();
        }
        return new SemanticTokens(data);
    }

    public SemanticTokens semanticTokens(final CompiledDocument document, final PositionEncoding encoding) {
        return encode(tokenSpans(document, encoding));
    }

    public SemanticTokens semanticTokens(final CompiledDocument document, final PositionEncoding encoding,
                                         final Range range) {
        return encode(tokenSpans(document, encoding, range));
    }

    private static void addDeclarationToken(final List<TokenSpan> tokens, final CompiledDocument document,
                                            final PositionEncoding encoding, final Range range, final ASTNode node) {
        Range nameRange = node instanceof AnnotatedNode annotated
                ? Positions.toNameRange(annotated, document.getText(), encoding)
                : Positions.toRange(node, document.getText(), encoding);
        if (nameRange == null || !overlaps(nameRange, range)) {
            return;
        }
        int type = tokenType(node);
        int length = Math.max(nameRange.getEnd().getCharacter() - nameRange.getStart().getCharacter(), 1);
        tokens.add(new TokenSpan(nameRange.getStart().getLine(), nameRange.getStart().getCharacter(), length, type,
                tokenModifiers(node)));
    }

    private static void addParameterTokens(final List<TokenSpan> tokens, final CompiledDocument document,
                                           final PositionEncoding encoding, final Range range, final ModuleNode module) {
        for (ClassNode classNode : module.getClasses()) {
            for (MethodNode method : classNode.getMethods()) {
                if (method.isSynthetic() || method.getParameters() == null) {
                    continue;
                }
                for (Parameter parameter : method.getParameters()) {
                    addDeclarationToken(tokens, document, encoding, range, parameter);
                }
            }
        }
    }

    private static void addUseSiteTokens(final List<TokenSpan> tokens, final CompiledDocument document,
                                         final PositionEncoding encoding, final Range range, final ModuleNode module) {
        String text = document.getText();
        AstQuery.walk(module, (node, ctx) -> {
            if (node instanceof MethodCallExpression call) {
                addSpanToken(tokens, Positions.toIdentifierRange(call, text, encoding), range, TOKEN_METHOD, 0);
            } else if (node instanceof StaticMethodCallExpression call) {
                addSpanToken(tokens, Positions.toIdentifierRange(call, text, encoding), range, TOKEN_METHOD, 0);
            } else if (node instanceof PropertyExpression property) {
                addSpanToken(tokens, Positions.toIdentifierRange(property, text, encoding), range, TOKEN_PROPERTY, 0);
            }
        });
    }

    private static void addSpanToken(final List<TokenSpan> tokens, final Range nameRange, final Range range,
                                     final int type, final int modifiers) {
        if (nameRange == null || !overlaps(nameRange, range)) {
            return;
        }
        int length = Math.max(nameRange.getEnd().getCharacter() - nameRange.getStart().getCharacter(), 1);
        tokens.add(new TokenSpan(nameRange.getStart().getLine(), nameRange.getStart().getCharacter(), length, type,
                modifiers));
    }

    private static void addLexicalTokens(final String text, final List<TokenSpan> tokens,
                                         final PositionEncoding encoding, final Range range) {
        if (text == null || text.isEmpty()) {
            return;
        }
        for (Token token : GroovySourceTokens.tokenize(text)) {
            if (token != null) {
                int type = lexicalType(token);
                if (type >= 0) {
                    addLexerSpans(text, tokens, encoding, range, token, type);
                }
            }
        }
    }

    private static int lexicalType(final Token token) {
        int type = token.getType();
        if (type == GroovyLexer.StringLiteral || type == GroovyLexer.GStringBegin
                || type == GroovyLexer.GStringPart || type == GroovyLexer.GStringEnd) {
            return TOKEN_STRING;
        }
        if (type == GroovyLexer.IntegerLiteral || type == GroovyLexer.FloatingPointLiteral) {
            return TOKEN_NUMBER;
        }
        if (type == GroovyLexer.NL) {
            String body = token.getText();
            if (body != null && (body.startsWith("//") || body.startsWith("/*"))) {
                return TOKEN_COMMENT;
            }
            return -1;
        }
        if (type == GroovyLexer.NOT_IN || type == GroovyLexer.NOT_INSTANCEOF
                || GroovyKeywords.isKeyword(token.getText())) {
            return TOKEN_KEYWORD;
        }
        return -1;
    }

    private static void addLexerSpans(final String text, final List<TokenSpan> tokens, final PositionEncoding encoding,
                                      final Range range, final Token token, final int type) {
        String tokenText = token.getText();
        if (tokenText == null || tokenText.isEmpty()) {
            return;
        }
        int line = token.getLine() - 1;
        int codePointColumn = token.getCharPositionInLine();
        String[] parts = tokenText.split("\n", -1);
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            boolean trailingEmpty = part.isEmpty() && i == parts.length - 1 && tokenText.endsWith("\n");
            int lineI = line + i;
            if (!trailingEmpty && lineInRange(range, lineI)) {
                String row = Positions.lineText(text, lineI + 1);
                int startCodePoints = i == 0 ? codePointColumn : 0;
                int endCodePoints = startCodePoints + part.codePointCount(0, part.length());
                int startChar = Positions.groovyColumnToCharacter(row, startCodePoints + 1, encoding);
                int endChar = Positions.groovyColumnToCharacter(row, endCodePoints + 1, encoding);
                tokens.add(new TokenSpan(lineI, startChar, Math.max(endChar - startChar, 1), type, 0));
            }
        }
    }

    private static void dedupeSameSpan(final List<TokenSpan> tokens) {
        if (tokens.size() < 2) {
            return;
        }
        List<TokenSpan> kept = new ArrayList<>(tokens.size());
        for (TokenSpan token : tokens) {
            if (!kept.isEmpty()) {
                TokenSpan last = kept.get(kept.size() - 1);
                if (last.line() == token.line() && last.character() == token.character()
                        && last.length() == token.length()) {
                    if ((token.modifiers() & MOD_DECLARATION) != 0 || last.modifiers() == 0 && token.modifiers() != 0) {
                        kept.set(kept.size() - 1, token);
                    }
                    continue;
                }
            }
            kept.add(token);
        }
        tokens.clear();
        tokens.addAll(kept);
    }

    private static boolean lineInRange(final Range range, final int line) {
        if (range == null || range.getStart() == null || range.getEnd() == null) {
            return true;
        }
        return line >= range.getStart().getLine() && line <= range.getEnd().getLine();
    }

    private static boolean overlaps(final Range token, final Range range) {
        if (range == null || token == null) {
            return true;
        }
        return token.getEnd().getLine() >= range.getStart().getLine()
                && token.getStart().getLine() <= range.getEnd().getLine();
    }

    private static int tokenType(final ASTNode node) {
        if (node instanceof PackageNode || node instanceof ImportNode) {
            return TOKEN_NAMESPACE;
        }
        if (node instanceof ClassNode classNode) {
            if (classNode.isInterface()) {
                return TOKEN_INTERFACE;
            }
            if (classNode.isEnum()) {
                return TOKEN_ENUM;
            }
            return TOKEN_CLASS;
        }
        if (node instanceof MethodNode) {
            return TOKEN_METHOD;
        }
        if (node instanceof PropertyNode || node instanceof FieldNode) {
            return TOKEN_PROPERTY;
        }
        if (node instanceof Parameter) {
            return TOKEN_PARAMETER;
        }
        return TOKEN_VARIABLE;
    }

    private static int tokenModifiers(final ASTNode node) {
        int bits = MOD_DECLARATION;
        if (isStaticMember(node)) {
            bits |= MOD_STATIC;
        }
        if (node instanceof AnnotatedNode annotated && isDeprecated(annotated)) {
            bits |= MOD_DEPRECATED;
        }
        return bits;
    }

    private static boolean isStaticMember(final ASTNode node) {
        if (node instanceof MethodNode method) {
            return method.isStatic();
        }
        if (node instanceof FieldNode field) {
            return field.isStatic();
        }
        return node instanceof PropertyNode property && property.isStatic();
    }

    private static boolean isDeprecated(final AnnotatedNode node) {
        if (node.getAnnotations() == null) {
            return false;
        }
        for (AnnotationNode annotation : node.getAnnotations()) {
            ClassNode type = annotation.getClassNode();
            if (type != null && "java.lang.Deprecated".equals(type.getName())) {
                return true;
            }
        }
        return false;
    }
}
