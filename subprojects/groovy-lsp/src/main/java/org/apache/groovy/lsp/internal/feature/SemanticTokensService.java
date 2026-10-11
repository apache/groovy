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
import java.util.Comparator;
import java.util.List;

/**
 * Semantic tokens for a compiled Groovy document.
 */
public final class SemanticTokensService {

    public static final List<String> TOKEN_TYPES = List.of(
            "namespace", "class", "enum", "interface", "method", "property",
            "variable", "parameter", "keyword", "string", "number", "comment",
            "operator", "decorator", "delimiter"
    );
    public static final List<String> TOKEN_MODIFIERS = List.of(
            "declaration", "static", "deprecated", "slashy", "singleQuoted");

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
    private static final int TOKEN_OPERATOR = 12;
    private static final int TOKEN_DECORATOR = 13;
    private static final int TOKEN_DELIMITER = 14;
    private static final int MOD_DECLARATION = 1;
    private static final int MOD_STATIC = 2;
    private static final int MOD_DEPRECATED = 4;
    private static final int MOD_SLASHY = 8;
    private static final int MOD_SINGLE = 16;
    private static final Comparator<TokenSpan> SPAN_ORDER = (left, right) -> {
        if (left.line() != right.line()) {
            return Integer.compare(left.line(), right.line());
        }
        if (left.character() != right.character()) {
            return Integer.compare(left.character(), right.character());
        }
        return Integer.compare(right.length(), left.length());
    };

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
        tokens.sort(SPAN_ORDER);
        dedupeSameSpan(tokens);
        return List.copyOf(tokens);
    }

    /**
     * Lenient lexical spans for a live highlighter. Incomplete input still
     * tokenizes. No AST and no compile. Identifiers are {@code variable}
     * until a compiled overlay upgrades them.
     *
     * @param text document text
     * @param encoding negotiated encoding
     * @return spans, never {@code null}
     */
    public static List<TokenSpan> lexicalTokenSpans(final String text, final PositionEncoding encoding) {
        if (text == null || text.isEmpty()) {
            return List.of();
        }
        return lexicalTokenSpans(text, GroovySourceTokens.tokenizeLenient(text), encoding);
    }

    /**
     * Lenient lexical spans from an already-tokenized walk so a live
     * highlighter does not lex the buffer twice.
     *
     * @param text document text
     * @param lexerTokens ANTLR tokens, possibly {@code null}
     * @param encoding negotiated encoding
     * @return spans, never {@code null}
     */
    public static List<TokenSpan> lexicalTokenSpans(final String text,
                                                    final List<Token> lexerTokens,
                                                    final PositionEncoding encoding) {
        if (text == null || text.isEmpty() || lexerTokens == null || lexerTokens.isEmpty()) {
            return List.of();
        }
        List<TokenSpan> tokens = new ArrayList<>();
        addLexicalTokens(text, lexerTokens, tokens, encoding, null);
        tokens.sort(SPAN_ORDER);
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

    /**
     * Tokens for an open buffer. A compiled overlay is used only when it
     * describes this exact text, so a highlighter does not wait for a compile.
     *
     * @param text current document text
     * @param overlay compiled document, or {@code null}
     * @param encoding negotiated encoding
     * @return protocol tokens, never {@code null}
     */
    public SemanticTokens semanticTokens(final String text, final CompiledDocument overlay,
                                         final PositionEncoding encoding) {
        if (overlay != null && text != null && text.equals(overlay.getText())) {
            return encode(tokenSpans(overlay, encoding));
        }
        return encode(lexicalTokenSpans(text, encoding));
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
        addLexicalTokens(text, GroovySourceTokens.tokenize(text), tokens, encoding, range);
    }

    private static void addLexicalTokens(final String text, final List<Token> lexerTokens,
                                         final List<TokenSpan> tokens, final PositionEncoding encoding,
                                         final Range range) {
        // GString parts do not repeat the opening delimiter. The modifier
        // chosen for StringLiteral or GStringBegin applies until the next one.
        int openString = 0;
        for (Token token : lexerTokens) {
            openString = recordLexicalToken(text, tokens, encoding, range, token, openString);
        }
    }

    private static int recordLexicalToken(final String text, final List<TokenSpan> tokens,
                                          final PositionEncoding encoding, final Range range,
                                          final Token token, final int openString) {
        if (token == null) {
            return openString;
        }
        int type = lexicalType(token);
        if (type < 0) {
            return openString;
        }
        int currentOpen = openString;
        if (type == TOKEN_STRING && opensString(token.getType())) {
            currentOpen = stringModifier(token.getText());
        }
        int modifiers = type == TOKEN_STRING ? currentOpen : 0;
        addLexerSpans(text, tokens, encoding, range, token, type, modifiers);
        return type == TOKEN_STRING ? currentOpen : openString;
    }

    private static int lexicalType(final Token token) {
        int type = token.getType();
        if (isStringToken(type)) {
            return TOKEN_STRING;
        }
        if (isNumberToken(type)) {
            return TOKEN_NUMBER;
        }
        if (type == GroovyLexer.NL) {
            return commentType(token.getText());
        }
        if (type == GroovyLexer.AT) {
            return TOKEN_DECORATOR;
        }
        if (isOperatorToken(type)) {
            return TOKEN_OPERATOR;
        }
        if (isDelimiterToken(type)) {
            return TOKEN_DELIMITER;
        }
        if (GroovyKeywords.isKeywordType(type)) {
            return TOKEN_KEYWORD;
        }
        if (isIdentifierToken(type)) {
            return TOKEN_VARIABLE;
        }
        return -1;
    }

    private static boolean isStringToken(final int type) {
        return type == GroovyLexer.StringLiteral || type == GroovyLexer.GStringBegin
                || type == GroovyLexer.GStringPart || type == GroovyLexer.GStringEnd;
    }

    private static boolean opensString(final int type) {
        return type == GroovyLexer.StringLiteral || type == GroovyLexer.GStringBegin;
    }

    private static boolean isNumberToken(final int type) {
        return type == GroovyLexer.IntegerLiteral || type == GroovyLexer.FloatingPointLiteral;
    }

    private static int commentType(final String body) {
        if (body != null && (body.startsWith("//") || body.startsWith("/*"))) {
            return TOKEN_COMMENT;
        }
        return -1;
    }

    private static boolean isOperatorToken(final int type) {
        return type == GroovyLexer.COMMA || type == GroovyLexer.SEMI;
    }

    private static boolean isDelimiterToken(final int type) {
        return type == GroovyLexer.LPAREN || type == GroovyLexer.RPAREN
                || type == GroovyLexer.LBRACK || type == GroovyLexer.RBRACK
                || type == GroovyLexer.LBRACE || type == GroovyLexer.RBRACE
                || type == GroovyLexer.SAFE_INDEX;
    }

    private static boolean isIdentifierToken(final int type) {
        return type == GroovyLexer.Identifier || type == GroovyLexer.CapitalizedIdentifier;
    }

    private static void addLexerSpans(final String text, final List<TokenSpan> tokens, final PositionEncoding encoding,
                                      final Range range, final Token token, final int type, final int modifiers) {
        String tokenText = token.getText();
        if (tokenText == null || tokenText.isEmpty()) {
            return;
        }
        TokenPaint paint = new TokenPaint(text, tokens, encoding, range, token, type, modifiers);
        String[] parts = tokenText.split("\n", -1);
        int line = token.getLine() - 1;
        int column = token.getCharPositionInLine();
        for (int i = 0; i < parts.length; i++) {
            addLexerLine(paint, parts, line, column, i);
        }
    }

    private static void addLexerLine(final TokenPaint paint, final String[] parts, final int line,
                                     final int codePointColumn, final int index) {
        String part = parts[index];
        String tokenText = paint.token.getText();
        boolean trailingEmpty = part.isEmpty() && index == parts.length - 1
                && tokenText != null && tokenText.endsWith("\n");
        int lineI = line + index;
        if (trailingEmpty || !lineInRange(paint.range, lineI)) {
            return;
        }
        String painted = withoutClosingDollar(paint.token, part);
        if (painted.isEmpty()) {
            return;
        }
        String row = Positions.lineText(paint.text, lineI + 1);
        int startCodePoints = index == 0 ? codePointColumn : 0;
        int endCodePoints = startCodePoints + painted.codePointCount(0, painted.length());
        int startChar = Positions.groovyColumnToCharacter(row, startCodePoints + 1, paint.encoding);
        int endChar = Positions.groovyColumnToCharacter(row, endCodePoints + 1, paint.encoding);
        paint.tokens.add(new TokenSpan(lineI, startChar, Math.max(endChar - startChar, 1), paint.type, paint.modifiers));
    }

    private static final class TokenPaint {
        private final String text;
        private final List<TokenSpan> tokens;
        private final PositionEncoding encoding;
        private final Range range;
        private final Token token;
        private final int type;
        private final int modifiers;

        private TokenPaint(final String text, final List<TokenSpan> tokens, final PositionEncoding encoding,
                           final Range range, final Token token, final int type, final int modifiers) {
            this.text = text;
            this.tokens = tokens;
            this.encoding = encoding;
            this.range = range;
            this.token = token;
            this.type = type;
            this.modifiers = modifiers;
        }
    }

    private static String withoutClosingDollar(final Token token, final String painted) {
        int tokenType = token.getType();
        if ((tokenType == GroovyLexer.GStringBegin || tokenType == GroovyLexer.GStringPart) && painted.endsWith("$")) {
            return painted.substring(0, painted.length() - 1);
        }
        return painted;
    }

    /**
     * Quote style of the token that opened the literal.
     * A double-quoted literal, including a GString, is modifier {@code 0}.
     */
    private static int stringModifier(final String body) {
        if (body != null && (body.startsWith("$/") || body.startsWith("/"))) {
            return MOD_SLASHY;
        }
        if (body != null && body.startsWith("\"")) {
            return 0;
        }
        return MOD_SINGLE;
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
