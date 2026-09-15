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
import org.apache.groovy.lsp.internal.compile.CompilationSnapshot;
import org.apache.groovy.lsp.internal.compile.CompiledDocument;
import org.apache.groovy.lsp.internal.compile.ImportSupport;
import org.apache.groovy.lsp.internal.compile.TypeIndex;
import org.apache.groovy.lsp.internal.position.PositionEncoding;
import org.apache.groovy.lsp.internal.position.Positions;
import org.apache.groovy.lsp.internal.util.GroovyKeywords;
import org.apache.groovy.lsp.internal.util.GroovySourceTokens;
import org.apache.groovy.lsp.internal.util.Uris;
import org.apache.groovy.lsp.internal.workspace.TextDocument;
import org.apache.groovy.parser.antlr4.GroovyLexer;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.PackageNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentHighlightKind;
import org.eclipse.lsp4j.DocumentLink;
import org.eclipse.lsp4j.FoldingRange;
import org.eclipse.lsp4j.FoldingRangeKind;
import org.eclipse.lsp4j.InlayHint;
import org.eclipse.lsp4j.InlayHintKind;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.ParameterInformation;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SelectionRange;
import org.eclipse.lsp4j.SemanticTokens;
import org.eclipse.lsp4j.SemanticTokensLegend;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureInformation;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Remaining LSP language features that share the compiled snapshot:
 * signature help, highlight, folding, formatting, semantic tokens,
 * inlay hints, code actions, code lenses, hierarchies, selection
 * ranges and document links.
 */
public final class SupportServices {

    private static final Pattern UNRESOLVED_CLASS = Pattern.compile("unable to resolve class ([\\w.]+)", Pattern.CASE_INSENSITIVE);
    private final CodeActionService codeActions = new CodeActionService();

    public static final List<String> TOKEN_TYPES = List.of(
            "namespace", "class", "enum", "interface", "method", "property",
            "variable", "parameter", "keyword", "string", "number", "comment"
    );
    public static final List<String> TOKEN_MODIFIERS = List.of("declaration", "static", "deprecated");

    public static SemanticTokensLegend legend() {
        return new SemanticTokensLegend(TOKEN_TYPES, TOKEN_MODIFIERS);
    }

    public SignatureHelp signatureHelp(final TextDocument document, final CompilationSnapshot snapshot,
                                       final Position position, final PositionEncoding encoding) {
        CompiledDocument compiled = snapshot == null ? null : snapshot.get(document);
        if (compiled == null || compiled.getModule() == null) {
            return new SignatureHelp(List.of(), 0, 0);
        }
        int[] groovy = document.toGroovy(position, encoding);
        ASTNode node = AstQuery.enclosingCall(compiled.getModule(), groovy[0], groovy[1]);
        List<MethodNode> overloads = new ArrayList<>();
        int active = 0;
        int argc = 0;
        Expression arguments = argumentsOf(node);
        if (arguments != null) {
            argc = NavigationService.argumentCount(arguments);
            active = activeParameter(arguments, groovy[0], groovy[1]);
        }
        if (node instanceof MethodCallExpression call) {
            overloads.addAll(NavigationService.overloads(call, snapshot));
        } else if (node instanceof StaticMethodCallExpression call) {
            overloads.addAll(NavigationService.overloads(call.getOwnerType(), call.getMethodAsString()));
        } else if (node instanceof ConstructorCallExpression ctor && ctor.getType() != null) {
            overloads.addAll(List.copyOf(ctor.getType().getDeclaredConstructors()));
        }
        if (overloads.isEmpty()) {
            return new SignatureHelp(List.of(), 0, 0);
        }
        List<SignatureInformation> informations = new ArrayList<>();
        int activeSignature = 0;
        int best = Integer.MAX_VALUE;
        for (int i = 0; i < overloads.size(); i++) {
            MethodNode method = overloads.get(i);
            informations.add(signatureInformation(method));
            int distance = Math.abs(method.getParameters().length - argc);
            if (distance < best) {
                best = distance;
                activeSignature = i;
            }
        }
        int paramCount = overloads.get(activeSignature).getParameters().length;
        if (active >= paramCount) {
            active = Math.max(paramCount - 1, 0);
        }
        return new SignatureHelp(informations, activeSignature, active);
    }

    public List<DocumentHighlight> highlights(final TextDocument document, final CompilationSnapshot snapshot,
                                              final Position position, final PositionEncoding encoding) {
        if (document == null || document.getUri() == null) {
            return List.of();
        }
        List<Location> refs = new NavigationService()
                .references(document, snapshot, position, encoding, true);
        List<DocumentHighlight> highlights = new ArrayList<>();
        String uri = document.getUri().toString();
        for (Location location : refs) {
            if (location != null && uri.equals(location.getUri())) {
                highlights.add(new DocumentHighlight(location.getRange(), DocumentHighlightKind.Text));
            }
        }
        return highlights;
    }

    public List<FoldingRange> folding(final CompiledDocument document) {
        List<FoldingRange> ranges = new ArrayList<>();
        if (document == null || document.getModule() == null) {
            return ranges;
        }
        for (ASTNode node : AstQuery.declarations(document.getModule())) {
            if (node.getLineNumber() > 0 && node.getLastLineNumber() > node.getLineNumber()) {
                FoldingRange range = new FoldingRange(node.getLineNumber() - 1, node.getLastLineNumber() - 1);
                ranges.add(range);
            }
        }
        addImportFolds(document.getModule(), ranges);
        addCommentFolds(document.getText(), ranges);
        addRegionFolds(document.getText(), ranges);
        return ranges;
    }

    public List<TextEdit> format(final TextDocument document, final Range range, final int tabSize,
                                 final boolean insertSpaces, final PositionEncoding encoding) {
        String text = document.getText();
        PositionEncoding enc = encoding == null ? PositionEncoding.UTF16 : encoding;
        boolean full = range == null;
        Range target = full ? document.fullRange(enc) : range;
        String[] lines = text.split("\n", -1);
        int startLine = Math.max(target.getStart().getLine(), 0);
        int endLine = Math.min(target.getEnd().getLine(), lines.length - 1);
        if (endLine < startLine) {
            return List.of();
        }
        String indentUnit = indentUnit(tabSize, insertSpaces);
        List<String> formatted = formatAllLines(lines, insertSpaces, indentUnit, frozenIndentLines(text));
        if (full) {
            String result = String.join("\n", formatted);
            if (!result.endsWith("\n") && !result.isEmpty()) {
                result += "\n";
            }
            if (result.equals(text)) {
                return List.of();
            }
            return List.of(new TextEdit(document.fullRange(enc), result));
        }
        StringBuilder original = new StringBuilder();
        StringBuilder rebuilt = new StringBuilder();
        for (int i = startLine; i <= endLine; i++) {
            if (i > startLine) {
                original.append('\n');
                rebuilt.append('\n');
            }
            original.append(lines[i]);
            rebuilt.append(i < formatted.size() ? formatted.get(i) : lines[i]);
        }
        if (original.toString().equals(rebuilt.toString())) {
            return List.of();
        }
        String last = lines[endLine];
        Position end = Positions.toLsp(endLine + 1, last.codePointCount(0, last.length()) + 1, last, enc);
        return List.of(new TextEdit(new Range(new Position(startLine, 0), end), rebuilt.toString()));
    }

    private static List<String> formatAllLines(final String[] lines, final boolean insertSpaces,
                                               final String indentUnit, final Set<Integer> frozen) {
        int[] indent = {0};
        List<String> formatted = new ArrayList<>(lines.length);
        for (int i = 0; i < lines.length; i++) {
            formatted.add(frozen.contains(i) ? lines[i] : formatLine(lines[i], insertSpaces, indentUnit, indent));
        }
        return formatted;
    }

    private static String formatLine(final String raw, final boolean insertSpaces, final String indentUnit,
                                     final int[] indent) {
        String line = rtrim(raw).replace("\t", insertSpaces ? indentUnit : "\t");
        String stripped = line.stripLeading();
        applyCloser(stripped, indent);
        String rebuilt = stripped.isEmpty() ? "" : indentUnit.repeat(indent[0]) + stripped;
        applyOpener(stripped, indent);
        return rebuilt;
    }

    private static void updateIndent(final String raw, final int[] indent) {
        String stripped = rtrim(raw).stripLeading();
        applyCloser(stripped, indent);
        applyOpener(stripped, indent);
    }

    private static void applyCloser(final String stripped, final int[] indent) {
        if (indentCloser(stripped)) {
            indent[0] = Math.max(indent[0] - 1, 0);
        }
    }

    private static void applyOpener(final String stripped, final int[] indent) {
        if (indentOpener(stripped)) {
            indent[0] += 1;
        }
    }

    private static boolean indentCloser(final String stripped) {
        return stripped.startsWith("}") || stripped.startsWith(")") || stripped.startsWith("]");
    }

    private static boolean indentOpener(final String stripped) {
        return stripped.endsWith("{") || stripped.endsWith("(") && !stripped.contains(")");
    }

    private static String indentUnit(final int tabSize, final boolean insertSpaces) {
        return insertSpaces ? " ".repeat(Math.max(tabSize, 1)) : "\t";
    }

    private static List<TextEdit> replaceLeadingWhitespace(final String line, final int lineNumber, final String pad) {
        int existing = TextDocument.leadingWhitespace(line);
        if (pad.equals(line.substring(0, existing))) {
            return List.of();
        }
        return List.of(new TextEdit(new Range(new Position(lineNumber, 0), new Position(lineNumber, existing)), pad));
    }

    private static List<TextEdit> outdentCloser(final TextDocument document, final Position position,
                                                final int tabSize, final boolean insertSpaces,
                                                final PositionEncoding encoding) {
        String text = document.getText();
        int after = TextDocument.offsetOf(text, position, encoding == null ? PositionEncoding.UTF16 : encoding);
        if (after <= 0 || !GroovySourceTokens.isStructuralRbrace(text, after - 1)) {
            return List.of();
        }
        String[] lines = text.split("\n", -1);
        int line = position.getLine();
        if (line < 0 || line >= lines.length) {
            return List.of();
        }
        String current = lines[line];
        String stripped = current.stripLeading();
        if (!stripped.startsWith("}")) {
            return List.of();
        }
        int[] indent = {0};
        for (int i = 0; i < line; i++) {
            updateIndent(lines[i], indent);
        }
        int level = Math.max(indent[0] - 1, 0);
        return replaceLeadingWhitespace(current, line, indentUnit(tabSize, insertSpaces).repeat(level));
    }

    /**
     * Indents the new line after a newline, matching Metals' on-type formatter.
     *
     * @param document open document
     * @param position caret after the typed character
     * @param typed typed character
     * @param tabSize tab size
     * @param insertSpaces whether to insert spaces
     * @return an indent edit, or empty
     */
    public List<TextEdit> onTypeFormat(final TextDocument document, final Position position, final String typed,
                                       final int tabSize, final boolean insertSpaces) {
        return onTypeFormat(document, position, typed, tabSize, insertSpaces, PositionEncoding.UTF16);
    }

    /**
     * Indents after newline or outdents a structural {@code '}'}'.
     *
     * @param encoding negotiated encoding
     * @return a tiny indent edit, or empty
     */
    public List<TextEdit> onTypeFormat(final TextDocument document, final Position position, final String typed,
                                       final int tabSize, final boolean insertSpaces,
                                       final PositionEncoding encoding) {
        if (document == null || typed == null || position.getLine() < 0) {
            return List.of();
        }
        if ("}".equals(typed)) {
            return outdentCloser(document, position, tabSize, insertSpaces, encoding);
        }
        if (!"\n".equals(typed) || position.getLine() <= 0) {
            return List.of();
        }
        String[] lines = document.getText().split("\n", -1);
        int prev = position.getLine() - 1;
        if (prev >= lines.length || position.getLine() >= lines.length) {
            return List.of();
        }
        String previous = lines[prev];
        int indent = TextDocument.leadingWhitespace(previous);
        if (rtrim(previous).endsWith("{")) {
            indent += Math.max(tabSize, 1);
        }
        String pad;
        if (insertSpaces) {
            pad = " ".repeat(indent);
        } else {
            pad = "\t".repeat(Math.max(indent / Math.max(tabSize, 1), 0));
        }
        return replaceLeadingWhitespace(lines[position.getLine()], position.getLine(), pad);
    }

    public SemanticTokens semanticTokens(final CompiledDocument document, final PositionEncoding encoding) {
        return semanticTokens(document, encoding, null);
    }

    public SemanticTokens semanticTokens(final CompiledDocument document, final PositionEncoding encoding,
                                         final Range range) {
        List<Integer> data = new ArrayList<>();
        if (document == null) {
            return new SemanticTokens(data);
        }
        List<int[]> tokens = new ArrayList<>();
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
            if (left[0] != right[0]) {
                return Integer.compare(left[0], right[0]);
            }
            if (left[1] != right[1]) {
                return Integer.compare(left[1], right[1]);
            }
            return Integer.compare(right[2], left[2]);
        });
        dedupeSameSpan(tokens);
        int prevLine = 0;
        int prevChar = 0;
        for (int[] token : tokens) {
            int dLine = token[0] - prevLine;
            int dChar = dLine == 0 ? token[1] - prevChar : token[1];
            if (dLine < 0 || dChar < 0) {
                continue;
            }
            data.add(dLine);
            data.add(dChar);
            data.add(token[2]);
            data.add(token[3]);
            data.add(token[4]);
            prevLine = token[0];
            prevChar = token[1];
        }
        return new SemanticTokens(data);
    }

    public List<InlayHint> inlayHints(final CompiledDocument document, final Range range,
                                      final PositionEncoding encoding) {
        return inlayHints(document, range, encoding, null);
    }

    public List<InlayHint> inlayHints(final CompiledDocument document, final Range range,
                                      final PositionEncoding encoding, final CompilationSnapshot snapshot) {
        List<InlayHint> hints = new ArrayList<>();
        if (document == null || document.getModule() == null) {
            return hints;
        }
        for (ClassNode classNode : document.getModule().getClasses()) {
            addClassHints(hints, document, range, encoding, classNode);
        }
        addCallSiteHints(hints, document, range, encoding, snapshot);
        return hints;
    }

    private static void addDeclarationToken(final List<int[]> tokens, final CompiledDocument document,
                                            final PositionEncoding encoding, final Range range, final ASTNode node) {
        Range nameRange = node instanceof AnnotatedNode annotated
                ? Positions.toNameRange(annotated, document.getText(), encoding)
                : Positions.toRange(node, document.getText(), encoding);
        if (nameRange == null || !overlaps(nameRange, range)) {
            return;
        }
        int type = tokenType(node);
        int length = Math.max(nameRange.getEnd().getCharacter() - nameRange.getStart().getCharacter(), 1);
        tokens.add(new int[]{nameRange.getStart().getLine(), nameRange.getStart().getCharacter(), length, type,
                tokenModifiers(node)});
    }

    private static void addParameterTokens(final List<int[]> tokens, final CompiledDocument document,
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

    private static void addUseSiteTokens(final List<int[]> tokens, final CompiledDocument document,
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

    private static void addSpanToken(final List<int[]> tokens, final Range nameRange, final Range range,
                                     final int type, final int modifiers) {
        if (nameRange == null || !overlaps(nameRange, range)) {
            return;
        }
        int length = Math.max(nameRange.getEnd().getCharacter() - nameRange.getStart().getCharacter(), 1);
        tokens.add(new int[]{nameRange.getStart().getLine(), nameRange.getStart().getCharacter(), length, type, modifiers});
    }

    private static void addClassHints(final List<InlayHint> hints, final CompiledDocument document, final Range range,
                                      final PositionEncoding encoding, final ClassNode classNode) {
        for (MethodNode method : classNode.getMethods()) {
            addMethodHints(hints, document, range, encoding, method);
        }
        for (FieldNode field : classNode.getFields()) {
            if (!field.isSynthetic() && field.isDynamicTyped()) {
                addTypeHint(hints, field, TypeInference.of(field), document.getText(), encoding, range,
                        TypeInference.usedInitializer(field));
            }
        }
    }

    private static void addMethodHints(final List<InlayHint> hints, final CompiledDocument document, final Range range,
                                       final PositionEncoding encoding, final MethodNode method) {
        if (method.isSynthetic() || method.getCode() == null) {
            return;
        }
        for (Parameter parameter : method.getParameters()) {
            if (parameter.isDynamicTyped()) {
                addTypeHint(hints, parameter, parameter.getType(), document.getText(), encoding, range, false);
            }
        }
        AstQuery.walk(method.getCode(), (node, ctx) -> {
            if (node instanceof DeclarationExpression decl && !decl.isMultipleAssignmentDeclaration()) {
                VariableExpression variable = decl.getVariableExpression();
                if (variable != null && variable.isDynamicTyped()) {
                    addTypeHint(hints, variable, TypeInference.of(variable, document.getModule()),
                            document.getText(), encoding, range,
                            TypeInference.usedInitializer(variable, document.getModule()));
                }
            }
        });
    }

    private static void addCodeLens(final List<CodeLens> lenses, final TextDocument open,
                                    final CompiledDocument document, final CompilationSnapshot snapshot,
                                    final PositionEncoding encoding, final NavigationService navigation,
                                    final ASTNode node) {
        if (!(node instanceof ClassNode) && !(node instanceof MethodNode)) {
            return;
        }
        if (node instanceof MethodNode method && method.isSynthetic()) {
            return;
        }
        Range range = Positions.toNameRange((AnnotatedNode) node, document.getText(), encoding);
        if (range == null) {
            return;
        }
        int count = navigation.references(open, snapshot, range.getStart(), encoding, false).size();
        String label = count == 1 ? "1 reference" : count + " references";
        CodeLens lens = new CodeLens(range);
        lens.setCommand(new Command(label, "groovy.lsp.showReferences",
                List.of(document.getUri().toString(), range.getStart().getLine(), range.getStart().getCharacter())));
        lenses.add(lens);
    }

    private static void addLexicalTokens(final String text, final List<int[]> tokens,
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

    private static void addLexerSpans(final String text, final List<int[]> tokens, final PositionEncoding encoding,
                                      final Range range, final Token token, final int type) {
        String tokenText = token.getText();
        if (tokenText == null || tokenText.isEmpty()) {
            return;
        }
        int line = token.getLine() - 1;
        int utf16Col = token.getCharPositionInLine();
        String[] parts = tokenText.split("\n", -1);
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            boolean trailingEmpty = part.isEmpty() && i == parts.length - 1 && tokenText.endsWith("\n");
            int lineI = line + i;
            if (!trailingEmpty && lineInRange(range, lineI)) {
                String row = Positions.lineText(text, lineI + 1);
                int startUtf16 = i == 0 ? utf16Col : 0;
                int endUtf16 = startUtf16 + part.length();
                int startChar = Positions.groovyColumnToCharacter(row,
                        Positions.characterToGroovyColumn(row, startUtf16, PositionEncoding.UTF16), encoding);
                int endChar = Positions.groovyColumnToCharacter(row,
                        Positions.characterToGroovyColumn(row, endUtf16, PositionEncoding.UTF16), encoding);
                tokens.add(new int[]{lineI, startChar, Math.max(endChar - startChar, 1), type, 0});
            }
        }
    }

    private static void addTypeHint(final List<InlayHint> hints, final AnnotatedNode node, final ClassNode type,
                                    final String text, final PositionEncoding encoding, final Range range,
                                    final boolean inferred) {
        if (!SymbolIdentity.isResolvedType(type)) {
            return;
        }
        Range nameRange = Positions.toNameRange(node, text, encoding);
        if (nameRange == null || (range != null && nameRange.getStart().getLine() < range.getStart().getLine())) {
            return;
        }
        InlayHint hint = new InlayHint(nameRange.getStart(),
                Either.forLeft(type.getNameWithoutPackage() + " "));
        hint.setKind(InlayHintKind.Type);
        hint.setPaddingRight(true);
        if (inferred) {
            hint.setTooltip("inferred");
        }
        hints.add(hint);
    }

    private static void addCallSiteHints(final List<InlayHint> hints, final CompiledDocument document,
                                         final Range range, final PositionEncoding encoding,
                                         final CompilationSnapshot snapshot) {
        if (document.getModule() == null) {
            return;
        }
        AstQuery.walk(document.getModule(), (node, ctx) -> {
            if (node instanceof MethodCallExpression call) {
                MethodNode method = NavigationService.resolveMethod(call, snapshot);
                addParameterNameHints(hints, document, range, encoding, call.getArguments(),
                        method == null ? null : method.getParameters(), call.getMethodTarget() != null);
            } else if (node instanceof StaticMethodCallExpression call) {
                MethodNode method = NavigationService.resolveStatic(call, snapshot);
                addParameterNameHints(hints, document, range, encoding, call.getArguments(),
                        method == null ? null : method.getParameters(), false);
            } else if (node instanceof ConstructorCallExpression ctor) {
                ConstructorNode constructor = NavigationService.resolveConstructor(ctor);
                addParameterNameHints(hints, document, range, encoding, ctor.getArguments(),
                        constructor == null ? null : constructor.getParameters(), false);
            }
        });
    }

    private static void addParameterNameHints(final List<InlayHint> hints, final CompiledDocument document,
                                              final Range range, final PositionEncoding encoding,
                                              final Expression arguments, final Parameter[] parameters,
                                              final boolean bound) {
        if (parameters == null || hasNamedArgs(arguments)) {
            return;
        }
        List<Expression> args = argumentExpressions(arguments);
        int count = Math.min(parameters.length, args.size());
        for (int i = 0; i < count; i++) {
            Parameter parameter = parameters[i];
            Expression arg = args.get(i);
            Range argRange = Positions.toRange(arg, document.getText(), encoding);
            if (skipParameterHint(parameter, arg, argRange, range)) {
                continue;
            }
            InlayHint hint = new InlayHint(argRange.getStart(), Either.forLeft(parameter.getName() + ":"));
            hint.setKind(InlayHintKind.Parameter);
            hint.setPaddingRight(true);
            if (!bound) {
                hint.setTooltip("inferred");
            }
            hints.add(hint);
        }
    }

    private static boolean skipParameterHint(final Parameter parameter, final Expression arg, final Range argRange,
                                             final Range range) {
        if (parameter == null || parameter.isSynthetic() || parameter.getName() == null
                || parameter.getName().startsWith("$")) {
            return true;
        }
        if (arg instanceof VariableExpression variable && parameter.getName().equals(variable.getName())) {
            return true;
        }
        return argRange == null || (range != null && !overlaps(argRange, range));
    }

    private static boolean hasNamedArgs(final Expression arguments) {
        for (Expression arg : argumentExpressions(arguments)) {
            if (arg instanceof MapExpression) {
                return true;
            }
        }
        return false;
    }

    private static List<Expression> argumentExpressions(final Expression arguments) {
        if (arguments instanceof TupleExpression tuple) {
            return tuple.getExpressions();
        }
        if (arguments == null) {
            return List.of();
        }
        return List.of(arguments);
    }

    public List<Either<Command, CodeAction>> codeActions(final TextDocument document,
                                                         final CompilationSnapshot snapshot,
                                                         final List<Diagnostic> diagnostics,
                                                         final Range range, final PositionEncoding encoding,
                                                         final Collection<URI> folders,
                                                         final List<String> sourcePaths) {
        List<Either<Command, CodeAction>> actions = new ArrayList<>();
        if (document == null) {
            return actions;
        }
        CompiledDocument compiled = snapshot == null ? null : snapshot.get(document);
        CodeAction organize = new CodeAction("Organize imports");
        organize.setKind(CodeActionKind.SourceOrganizeImports);
        List<TextEdit> organized = organizeImports(compiled, encoding);
        if (!organized.isEmpty()) {
            organize.setEdit(new WorkspaceEdit(Map.of(document.getUri().toString(), organized)));
        }
        organize.setCommand(new Command("Organize imports", "groovy.lsp.organizeImports",
                List.of(document.getUri().toString())));
        actions.add(Either.forRight(organize));
        if (diagnostics != null) {
            for (Diagnostic diagnostic : diagnostics) {
                String message = diagnosticMessage(diagnostic);
                String className = unresolvedClassName(message);
                if (className == null) {
                    continue;
                }
                for (TypeIndex.TypeHit hit : importCandidates(className, snapshot)) {
                    List<TextEdit> edits = ImportSupport.addImport(compiled, hit.name(), encoding);
                    if (edits.isEmpty()) {
                        continue;
                    }
                    CodeAction action = new CodeAction("Add import for " + hit.name());
                    action.setKind(CodeActionKind.QuickFix);
                    action.setDiagnostics(List.of(diagnostic));
                    action.setEdit(new WorkspaceEdit(Map.of(document.getUri().toString(), edits)));
                    actions.add(Either.forRight(action));
                }
            }
        }
        addUnambiguousImports(actions, compiled, snapshot, diagnostics, encoding);
        actions.addAll(codeActions.contribute(document, snapshot, diagnostics, range, encoding, folders, sourcePaths));
        return actions;
    }

    public List<CodeLens> codeLenses(final CompiledDocument document, final CompilationSnapshot snapshot,
                                     final PositionEncoding encoding) {
        List<CodeLens> lenses = new ArrayList<>();
        if (document == null || document.getModule() == null || snapshot == null) {
            return lenses;
        }
        TextDocument open = document.toTextDocument();
        NavigationService navigation = new NavigationService();
        for (ASTNode node : AstQuery.declarations(document.getModule())) {
            addCodeLens(lenses, open, document, snapshot, encoding, navigation, node);
        }
        return lenses;
    }

    public List<SelectionRange> selectionRanges(final TextDocument document, final CompilationSnapshot snapshot,
                                                final List<Position> positions, final PositionEncoding encoding) {
        List<SelectionRange> ranges = new ArrayList<>();
        CompiledDocument compiled = snapshot == null ? null : snapshot.get(document);
        ModuleNode module = compiled == null ? null : compiled.getModule();
        for (Position position : positions) {
            SelectionRange current = null;
            if (module != null) {
                List<ASTNode> path = AstQuery.containing(module, document, position, encoding);
                for (int i = path.size() - 1; i >= 0; i--) {
                    Range range = Positions.toRange(path.get(i), document.getText(), encoding);
                    if (range != null) {
                        current = new SelectionRange(range, current);
                    }
                }
            }
            if (current == null) {
                current = new SelectionRange(new Range(position, position), null);
            }
            ranges.add(current);
        }
        return ranges;
    }

    public List<DocumentLink> documentLinks(final CompiledDocument document, final CompilationSnapshot snapshot,
                                            final PositionEncoding encoding) {
        List<DocumentLink> links = new ArrayList<>();
        if (document == null || document.getModule() == null) {
            return links;
        }
        for (ImportNode imp : ImportSupport.allImports(document.getModule())) {
            addDocumentLink(links, document, snapshot, encoding, imp);
        }
        return links;
    }

    private static void addDocumentLink(final List<DocumentLink> links, final CompiledDocument document,
                                        final CompilationSnapshot snapshot, final PositionEncoding encoding,
                                        final ImportNode imp) {
        Range range = Positions.toRange(imp, document.getText(), encoding);
        if (range == null) {
            return;
        }
        String target = importTarget(imp, document, snapshot);
        if (target != null) {
            links.add(new DocumentLink(range, target));
        }
    }

    public List<TextEdit> organizeImports(final CompiledDocument document, final PositionEncoding encoding) {
        if (document == null || document.getModule() == null) {
            return List.of();
        }
        List<ImportNode> imports = ImportSupport.allImports(document.getModule());
        if (imports.isEmpty()) {
            return List.of();
        }
        List<String> names = new ArrayList<>();
        Range first = null;
        Range last = null;
        for (ImportNode imp : imports) {
            names.add(imp.getText());
            Range span = Positions.toRange(imp, document.getText(), encoding);
            if (span != null) {
                first = earlier(first, span);
                last = later(last, span);
            }
        }
        names.sort(importOrder());
        if (first == null) {
            return List.of();
        }
        String replacement = String.join("\n", names);
        String current = slice(document.getText(), first, last);
        if (replacement.equals(current)) {
            return List.of();
        }
        return List.of(new TextEdit(new Range(first.getStart(), last.getEnd()), replacement));
    }

    private static Range earlier(final Range current, final Range candidate) {
        if (current == null || comparePosition(candidate.getStart(), current.getStart()) < 0) {
            return candidate;
        }
        return current;
    }

    private static Range later(final Range current, final Range candidate) {
        if (current == null || comparePosition(candidate.getEnd(), current.getEnd()) > 0) {
            return candidate;
        }
        return current;
    }

    private static void addCommentFolds(final String text, final List<FoldingRange> ranges) {
        int blockStart = -1;
        String[] lines = text.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            String stripped = lines[i].strip();
            if (stripped.startsWith("/*") && blockStart < 0) {
                blockStart = i;
            }
            if (stripped.contains("*/") && blockStart >= 0) {
                if (i > blockStart) {
                    FoldingRange range = new FoldingRange(blockStart, i);
                    range.setKind(FoldingRangeKind.Comment);
                    ranges.add(range);
                }
                blockStart = -1;
            }
        }
    }

    private static void addRegionFolds(final String text, final List<FoldingRange> ranges) {
        if (text == null || text.isEmpty()) {
            return;
        }
        List<Integer> open = new ArrayList<>();
        for (Token token : GroovySourceTokens.tokenize(text)) {
            String directive = regionDirective(token);
            if (directive != null) {
                int line = token.getLine() - 1;
                if (REGION.equals(directive)) {
                    open.add(line);
                } else if (ENDREGION.equals(directive) && !open.isEmpty()) {
                    int start = open.remove(open.size() - 1);
                    if (line > start) {
                        FoldingRange range = new FoldingRange(start, line);
                        range.setKind(FoldingRangeKind.Region);
                        ranges.add(range);
                    }
                }
            }
        }
    }

    private static String regionDirective(final Token token) {
        if (token == null || token.getType() != GroovyLexer.NL || token.getText() == null
                || !token.getText().startsWith("//")) {
            return null;
        }
        String body = token.getText().substring(2).strip();
        if (body.startsWith("#")) {
            body = body.substring(1).strip();
        }
        if (body.startsWith(REGION)) {
            return REGION;
        }
        if (body.startsWith(ENDREGION)) {
            return ENDREGION;
        }
        return null;
    }

    private static Set<Integer> frozenIndentLines(final String text) {
        Set<Integer> frozen = new HashSet<>();
        for (Token token : GroovySourceTokens.tokenize(text)) {
            String body = token.getText();
            if (!multilineStringOrComment(token, body)) {
                continue;
            }
            int startLine = token.getLine() - 1;
            int extra = body.split("\n", -1).length - 1;
            for (int i = 1; i <= extra; i++) {
                frozen.add(startLine + i);
            }
        }
        return frozen;
    }

    private static boolean multilineStringOrComment(final Token token, final String body) {
        int type = lexicalType(token);
        return (type == TOKEN_STRING || type == TOKEN_COMMENT) && body != null && body.contains("\n");
    }

    private static void dedupeSameSpan(final List<int[]> tokens) {
        if (tokens.size() < 2) {
            return;
        }
        List<int[]> kept = new ArrayList<>(tokens.size());
        for (int[] token : tokens) {
            if (!kept.isEmpty()) {
                int[] last = kept.get(kept.size() - 1);
                if (last[0] == token[0] && last[1] == token[1] && last[2] == token[2]) {
                    if ((token[4] & MOD_DECLARATION) != 0 || last[4] == 0 && token[4] != 0) {
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

    private static int activeParameter(final Expression arguments, final int line, final int column) {
        if (!(arguments instanceof TupleExpression tuple) || tuple.getExpressions().isEmpty()) {
            return 0;
        }
        List<Expression> exprs = tuple.getExpressions();
        int active = 0;
        for (int i = 0; i < exprs.size(); i++) {
            if (caretAfter(exprs.get(i), line, column)) {
                active = i + 1;
            }
        }
        if (active >= exprs.size()) {
            active = exprs.size() - 1;
        }
        return Math.max(active, 0);
    }

    private static boolean caretAfter(final Expression arg, final int line, final int column) {
        if (arg == null || arg.getLineNumber() <= 0) {
            return false;
        }
        int lastLine = arg.getLastLineNumber() > 0 ? arg.getLastLineNumber() : arg.getLineNumber();
        int lastColumn = arg.getLastColumnNumber() > 0 ? arg.getLastColumnNumber() : arg.getColumnNumber();
        return line > lastLine || (line == lastLine && column > lastColumn);
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

    private static SignatureInformation signatureInformation(final MethodNode method) {
        StringBuilder label = new StringBuilder(method.getName()).append('(');
        List<ParameterInformation> infos = new ArrayList<>();
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            if (i > 0) {
                label.append(", ");
            }
            String part = parameters[i].getType().getNameWithoutPackage() + " " + parameters[i].getName();
            label.append(part);
            infos.add(new ParameterInformation(part));
        }
        label.append(')');
        SignatureInformation information = new SignatureInformation(label.toString());
        information.setParameters(infos);
        return information;
    }

    private static void addUnambiguousImports(final List<Either<Command, CodeAction>> actions,
                                              final CompiledDocument compiled, final CompilationSnapshot snapshot,
                                              final List<Diagnostic> diagnostics, final PositionEncoding encoding) {
        if (compiled == null || diagnostics == null || diagnostics.isEmpty()) {
            return;
        }
        List<TextEdit> edits = new ArrayList<>();
        LinkedHashSet<String> seen = new LinkedHashSet<>();
        for (Diagnostic diagnostic : diagnostics) {
            TypeIndex.TypeHit hit = uniqueUnresolved(diagnostic, snapshot, seen);
            if (hit == null) {
                continue;
            }
            edits.addAll(ImportSupport.addImport(compiled, hit.name(), encoding));
        }
        if (edits.size() < 2) {
            return;
        }
        CodeAction action = new CodeAction("Add all unambiguous imports");
        action.setKind(CodeActionKind.Source);
        action.setEdit(new WorkspaceEdit(Map.of(compiled.getUri().toString(), edits)));
        actions.add(Either.forRight(action));
    }

    private static TypeIndex.TypeHit uniqueUnresolved(final Diagnostic diagnostic, final CompilationSnapshot snapshot,
                                                      final Set<String> seen) {
        String className = unresolvedClassName(diagnosticMessage(diagnostic));
        if (className == null || className.contains(".")) {
            return null;
        }
        List<TypeIndex.TypeHit> hits = snapshot == null ? List.of() : snapshot.types().bySimpleName(className);
        if (hits.size() != 1 || !seen.add(hits.get(0).name())) {
            return null;
        }
        return hits.get(0);
    }

    private static void addImportFolds(final ModuleNode module, final List<FoldingRange> ranges) {
        List<ImportNode> imports = ImportSupport.allImports(module);
        int start = Integer.MAX_VALUE;
        int end = -1;
        for (ImportNode imp : imports) {
            if (imp == null || imp.getLineNumber() <= 0) {
                continue;
            }
            start = Math.min(start, imp.getLineNumber() - 1);
            end = Math.max(end, Math.max(imp.getLastLineNumber(), imp.getLineNumber()) - 1);
        }
        if (end > start) {
            FoldingRange range = new FoldingRange(start, end);
            range.setKind(FoldingRangeKind.Imports);
            ranges.add(range);
        }
    }

    private static List<TypeIndex.TypeHit> importCandidates(final String className, final CompilationSnapshot snapshot) {
        if (snapshot == null || className == null) {
            return List.of();
        }
        if (className.contains(".")) {
            TypeIndex.TypeHit hit = snapshot.types().byName(className);
            return List.of(hit == null ? new TypeIndex.TypeHit(className, className, null, false, false, "") : hit);
        }
        List<TypeIndex.TypeHit> hits = snapshot.types().bySimpleName(className);
        return hits.size() > 5 ? hits.subList(0, 5) : hits;
    }

    private static Comparator<String> importOrder() {
        return (left, right) -> {
            boolean leftStatic = left.startsWith("import static");
            boolean rightStatic = right.startsWith("import static");
            if (leftStatic != rightStatic) {
                return leftStatic ? 1 : -1;
            }
            return String.CASE_INSENSITIVE_ORDER.compare(left, right);
        };
    }

    private static int comparePosition(final Position left, final Position right) {
        if (left.getLine() != right.getLine()) {
            return Integer.compare(left.getLine(), right.getLine());
        }
        return Integer.compare(left.getCharacter(), right.getCharacter());
    }

    private static String slice(final String text, final Range first, final Range last) {
        String[] lines = text.split("\n", -1);
        int startLine = first.getStart().getLine();
        int endLine = last.getEnd().getLine();
        if (startLine < 0 || endLine >= lines.length || startLine > endLine) {
            return "";
        }
        StringBuilder slice = new StringBuilder();
        for (int line = startLine; line <= endLine; line++) {
            String row = lines[line];
            int from = line == startLine ? Math.min(Math.max(first.getStart().getCharacter(), 0), row.length()) : 0;
            int to = line == endLine ? Math.min(Math.max(last.getEnd().getCharacter(), 0), row.length()) : row.length();
            if (from > to) {
                from = to;
            }
            slice.append(line > startLine ? "\n" : "").append(row, from, to);
        }
        return slice.toString();
    }

    private static Expression argumentsOf(final ASTNode node) {
        if (node instanceof MethodCallExpression call) {
            return call.getArguments();
        }
        if (node instanceof StaticMethodCallExpression call) {
            return call.getArguments();
        }
        if (node instanceof ConstructorCallExpression ctor) {
            return ctor.getArguments();
        }
        return null;
    }

    static String unresolvedClassName(final String message) {
        if (message == null) {
            return null;
        }
        Matcher matcher = UNRESOLVED_CLASS.matcher(message);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static String importTarget(final ImportNode imp, final CompiledDocument document,
                                       final CompilationSnapshot snapshot) {
        String className = imp.getClassName();
        if (className == null) {
            return null;
        }
        String workspace = workspaceTypeUri(className, snapshot);
        if (workspace != null) {
            return workspace;
        }
        if (document.getUri() == null || !"file".equalsIgnoreCase(document.getUri().getScheme())) {
            return null;
        }
        Path parent = Uris.toPath(document.getUri()).getParent();
        if (parent == null) {
            return null;
        }
        return Uris.normalize(parent.resolve(className.replace('.', '/') + ".groovy").toUri()).toString();
    }

    private static String workspaceTypeUri(final String className, final CompilationSnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }
        for (CompiledDocument other : snapshot.documents()) {
            if (other.getModule() == null) {
                continue;
            }
            for (ClassNode classNode : other.getModule().getClasses()) {
                if (className.equals(classNode.getName())) {
                    return other.getUri().toString();
                }
            }
        }
        return null;
    }

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
    private static final String REGION = "region";
    private static final String ENDREGION = "endregion";
    private static final int MOD_DECLARATION = 1;
    private static final int MOD_STATIC = 2;
    private static final int MOD_DEPRECATED = 4;

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

    static String diagnosticMessage(final Diagnostic diagnostic) {
        if (diagnostic == null || diagnostic.getMessage() == null) {
            return null;
        }
        var message = diagnostic.getMessage();
        if (message.isLeft()) {
            return message.getLeft();
        }
        if (message.isRight()) {
            return message.getRight().getValue();
        }
        return message.toString();
    }

    private static String rtrim(final String line) {
        int end = line.length();
        while (end > 0 && Character.isWhitespace(line.charAt(end - 1))) {
            end -= 1;
        }
        return line.substring(0, end);
    }
}
