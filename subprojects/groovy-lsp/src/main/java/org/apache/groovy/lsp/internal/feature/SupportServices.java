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

import org.apache.groovy.lsp.internal.compile.AstQuery;
import org.apache.groovy.lsp.internal.compile.CompilationSnapshot;
import org.apache.groovy.lsp.internal.compile.CompiledDocument;
import org.apache.groovy.lsp.internal.compile.ImportSupport;
import org.apache.groovy.lsp.internal.compile.TypeIndex;
import org.apache.groovy.lsp.internal.position.PositionEncoding;
import org.apache.groovy.lsp.internal.position.Positions;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.apache.groovy.lsp.internal.util.GroovyKeywords;
import org.apache.groovy.lsp.internal.util.Uris;
import org.apache.groovy.parser.antlr4.GroovyLexer;
import org.apache.groovy.lsp.internal.workspace.TextDocument;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;

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
import java.util.List;
import java.util.Map;
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
        CompiledDocument compiled = snapshot == null ? null : snapshot.get(document.getUri());
        if (compiled == null || compiled.getModule() == null) {
            return new SignatureHelp(List.of(), 0, 0);
        }
        int[] groovy = document.toGroovy(position, encoding);
        ASTNode node = AstQuery.enclosingCall(compiled.getModule(), groovy[0], groovy[1]);
        List<MethodNode> overloads = new ArrayList<>();
        int active = 0;
        int argc = 0;
        if (node instanceof MethodCallExpression call) {
            argc = NavigationService.argumentCount(call.getArguments());
            active = activeParameter(call.getArguments(), groovy[0], groovy[1]);
            overloads.addAll(NavigationService.overloads(call, snapshot));
        } else if (node instanceof StaticMethodCallExpression call) {
            argc = NavigationService.argumentCount(call.getArguments());
            active = activeParameter(call.getArguments(), groovy[0], groovy[1]);
            overloads.addAll(NavigationService.overloads(call.getOwnerType(), call.getMethodAsString()));
        } else if (node instanceof ConstructorCallExpression ctor && ctor.getType() != null) {
            argc = NavigationService.argumentCount(ctor.getArguments());
            active = activeParameter(ctor.getArguments(), groovy[0], groovy[1]);
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
        List<Location> refs = new NavigationService()
                .references(document, snapshot, position, encoding, true);
        List<DocumentHighlight> highlights = new ArrayList<>();
        for (Location location : refs) {
            if (document.getUri().toString().equals(location.getUri())) {
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
                if (node instanceof ClassNode) {
                    range.setKind(FoldingRangeKind.Region);
                }
                ranges.add(range);
            }
        }
        addCommentFolds(document.getText(), ranges);
        return ranges;
    }

    public List<TextEdit> format(final TextDocument document, final Range range, final int tabSize,
                                 final boolean insertSpaces, final PositionEncoding encoding) {
        String text = document.getText();
        PositionEncoding enc = encoding == null ? PositionEncoding.UTF16 : encoding;
        Range target = range == null ? document.fullRange(enc) : range;
        String[] lines = text.split("\n", -1);
        int startLine = target.getStart().getLine();
        int endLine = Math.min(target.getEnd().getLine(), lines.length - 1);
        String indentUnit = insertSpaces ? " ".repeat(Math.max(tabSize, 1)) : "\t";
        int[] indent = {0};
        List<String> formatted = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            String rebuilt = formatLine(lines[i], insertSpaces, indentUnit, indent);
            formatted.add(i >= startLine && i <= endLine ? rebuilt : lines[i]);
        }
        String result = String.join("\n", formatted);
        if (!result.endsWith("\n") && !result.isEmpty()) {
            result += "\n";
        }
        if (result.equals(text)) {
            return List.of();
        }
        return List.of(new TextEdit(document.fullRange(enc), result));
    }

    private static String formatLine(final String raw, final boolean insertSpaces, final String indentUnit,
                                     final int[] indent) {
        String line = rtrim(raw).replace("\t", insertSpaces ? indentUnit : "\t");
        String stripped = line.stripLeading();
        if (stripped.startsWith("}") || stripped.startsWith(")") || stripped.startsWith("]")) {
            indent[0] = Math.max(indent[0] - 1, 0);
        }
        String rebuilt = stripped.isEmpty() ? "" : indentUnit.repeat(indent[0]) + stripped;
        if (stripped.endsWith("{") || stripped.endsWith("(") && !stripped.contains(")")) {
            indent[0] += 1;
        }
        return rebuilt;
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
        if (document == null || !"\n".equals(typed) || position.getLine() <= 0) {
            return List.of();
        }
        String[] lines = document.getText().split("\n", -1);
        int prev = position.getLine() - 1;
        if (prev >= lines.length || position.getLine() >= lines.length) {
            return List.of();
        }
        String previous = lines[prev];
        int indent = 0;
        while (indent < previous.length() && (previous.charAt(indent) == ' ' || previous.charAt(indent) == '\t')) {
            indent += 1;
        }
        if (rtrim(previous).endsWith("{")) {
            indent += Math.max(tabSize, 1);
        }
        String current = lines[position.getLine()];
        int existing = 0;
        while (existing < current.length() && (current.charAt(existing) == ' ' || current.charAt(existing) == '\t')) {
            existing += 1;
        }
        String pad;
        if (insertSpaces) {
            pad = " ".repeat(indent);
        } else {
            pad = "\t".repeat(Math.max(indent / Math.max(tabSize, 1), 0));
        }
        if (pad.equals(current.substring(0, existing))) {
            return List.of();
        }
        return List.of(new TextEdit(new Range(new Position(position.getLine(), 0),
                new Position(position.getLine(), existing)), pad));
    }

    public SemanticTokens semanticTokens(final CompiledDocument document, final PositionEncoding encoding) {
        return semanticTokens(document, encoding, null);
    }

    public SemanticTokens semanticTokens(final CompiledDocument document, final PositionEncoding encoding,
                                         final Range range) {
        List<Integer> data = new ArrayList<>();
        if (document == null || document.getModule() == null) {
            return new SemanticTokens(data);
        }
        int prevLine = 0;
        int prevChar = 0;
        List<int[]> tokens = new ArrayList<>();
        for (ASTNode node : AstQuery.declarations(document.getModule())) {
            addDeclarationToken(tokens, document, encoding, range, node);
        }
        addKeywordTokens(document.getText(), tokens, encoding, range);
        tokens.sort((a, b) -> a[0] != b[0] ? Integer.compare(a[0], b[0]) : Integer.compare(a[1], b[1]));
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
        List<InlayHint> hints = new ArrayList<>();
        if (document == null || document.getModule() == null) {
            return hints;
        }
        for (ClassNode classNode : document.getModule().getClasses()) {
            addClassHints(hints, document, range, encoding, classNode);
        }
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
        tokens.add(new int[]{nameRange.getStart().getLine(), nameRange.getStart().getCharacter(), length, type, 1});
    }

    private static void addClassHints(final List<InlayHint> hints, final CompiledDocument document, final Range range,
                                      final PositionEncoding encoding, final ClassNode classNode) {
        for (MethodNode method : classNode.getMethods()) {
            addMethodHints(hints, document, range, encoding, method);
        }
        for (FieldNode field : classNode.getFields()) {
            if (!field.isSynthetic() && field.isDynamicTyped()) {
                addTypeHint(hints, field, TypeInference.of(field), document.getText(), encoding, range);
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
                addTypeHint(hints, parameter, parameter.getType(), document.getText(), encoding, range);
            }
        }
        AstQuery.walk(method.getCode(), (node, ctx) -> {
            if (node instanceof DeclarationExpression decl && !decl.isMultipleAssignmentDeclaration()) {
                VariableExpression variable = decl.getVariableExpression();
                if (variable != null && variable.isDynamicTyped()) {
                    addTypeHint(hints, variable, TypeInference.of(variable, document.getModule()),
                            document.getText(), encoding, range);
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

    private static void addKeywordToken(final String text, final List<int[]> tokens,
                                        final PositionEncoding encoding, final Range range, final Token token) {
        if (!GroovyKeywords.isKeyword(token.getText())) {
            return;
        }
        int line = token.getLine() - 1;
        if (range != null && (line < range.getStart().getLine() || line > range.getEnd().getLine())) {
            return;
        }
        String row = Positions.lineText(text, token.getLine());
        int startUtf16 = token.getCharPositionInLine();
        int endUtf16 = startUtf16 + token.getText().length();
        int startChar = Positions.groovyColumnToCharacter(row,
                Positions.characterToGroovyColumn(row, startUtf16, PositionEncoding.UTF16), encoding);
        int endChar = Positions.groovyColumnToCharacter(row,
                Positions.characterToGroovyColumn(row, endUtf16, PositionEncoding.UTF16), encoding);
        tokens.add(new int[]{line, startChar, Math.max(endChar - startChar, 1), TOKEN_KEYWORD, 0});
    }

    private static void addTypeHint(final List<InlayHint> hints, final AnnotatedNode node, final ClassNode type,
                                    final String text, final PositionEncoding encoding, final Range range) {
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
        hints.add(hint);
    }

    public List<Either<Command, CodeAction>> codeActions(final TextDocument document,
                                                         final CompilationSnapshot snapshot,
                                                         final List<Diagnostic> diagnostics,
                                                         final Range range, final PositionEncoding encoding,
                                                         final Collection<URI> folders,
                                                         final List<String> sourcePaths) {
        List<Either<Command, CodeAction>> actions = new ArrayList<>();
        CompiledDocument compiled = snapshot == null ? null : snapshot.get(document.getUri());
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
        actions.addAll(codeActions.contribute(document, snapshot, diagnostics, range, encoding, folders, sourcePaths));
        return actions;
    }

    public List<CodeLens> codeLenses(final CompiledDocument document, final CompilationSnapshot snapshot,
                                     final PositionEncoding encoding) {
        List<CodeLens> lenses = new ArrayList<>();
        if (document == null || document.getModule() == null || snapshot == null) {
            return lenses;
        }
        TextDocument open = new TextDocument(document.getUri(), "groovy", document.getVersion(), document.getText());
        NavigationService navigation = new NavigationService();
        for (ASTNode node : AstQuery.declarations(document.getModule())) {
            addCodeLens(lenses, open, document, snapshot, encoding, navigation, node);
        }
        return lenses;
    }

    public List<SelectionRange> selectionRanges(final TextDocument document, final CompilationSnapshot snapshot,
                                                final List<Position> positions, final PositionEncoding encoding) {
        List<SelectionRange> ranges = new ArrayList<>();
        CompiledDocument compiled = snapshot == null ? null : snapshot.get(document.getUri());
        ModuleNode module = compiled == null ? null : compiled.getModule();
        for (Position position : positions) {
            SelectionRange current = null;
            if (module != null) {
                int[] groovy = document.toGroovy(position, encoding);
                List<ASTNode> path = AstQuery.containing(module, groovy[0], groovy[1]);
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
            Range range = Positions.toRange(imp, document.getText(), encoding);
            if (range == null) {
                continue;
            }
            if (first == null || comparePosition(range.getStart(), first.getStart()) < 0) {
                first = range;
            }
            if (last == null || comparePosition(range.getEnd(), last.getEnd()) > 0) {
                last = range;
            }
        }
        names.sort(importOrder());
        if (first == null || last == null) {
            return List.of();
        }
        String replacement = String.join("\n", names);
        String current = slice(document.getText(), first, last);
        if (replacement.equals(current)) {
            return List.of();
        }
        return List.of(new TextEdit(new Range(first.getStart(), last.getEnd()), replacement));
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

    private static void addKeywordTokens(final String text, final List<int[]> tokens,
                                         final PositionEncoding encoding, final Range range) {
        if (text == null || text.isEmpty()) {
            return;
        }
        GroovyLexer lexer = new GroovyLexer(CharStreams.fromString(text));
        lexer.removeErrorListeners();
        for (Token token = lexer.nextToken(); token.getType() != Token.EOF; token = lexer.nextToken()) {
            addKeywordToken(text, tokens, encoding, range, token);
        }
    }

    private static int activeParameter(final Expression arguments, final int line, final int column) {
        if (!(arguments instanceof TupleExpression tuple) || tuple.getExpressions().isEmpty()) {
            return 0;
        }
        List<Expression> exprs = tuple.getExpressions();
        int active = 0;
        for (int i = 0; i < exprs.size(); i++) {
            Expression arg = exprs.get(i);
            if (arg.getLineNumber() <= 0) {
                continue;
            }
            int lastLine = arg.getLastLineNumber() > 0 ? arg.getLastLineNumber() : arg.getLineNumber();
            int lastColumn = arg.getLastColumnNumber() > 0 ? arg.getLastColumnNumber() : arg.getColumnNumber();
            if (line > lastLine || (line == lastLine && column > lastColumn)) {
                active = i + 1;
            }
        }
        if (active >= exprs.size()) {
            active = exprs.size() - 1;
        }
        return Math.max(active, 0);
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

    private static List<TypeIndex.TypeHit> importCandidates(final String className, final CompilationSnapshot snapshot) {
        if (snapshot == null || className == null) {
            return List.of();
        }
        if (className.contains(".")) {
            TypeIndex.TypeHit hit = snapshot.types().byName(className);
            return hit == null ? List.of(new TypeIndex.TypeHit(className, className, null, false, false, ""))
                    : List.of(hit);
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

    private static String unresolvedClassName(final String message) {
        if (message == null) {
            return null;
        }
        Matcher matcher = UNRESOLVED_CLASS.matcher(message);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static String importTarget(final ImportNode imp, final CompiledDocument document,
                                       final CompilationSnapshot snapshot) {
        String className = imp.getClassName();
        if (className == null && imp.getPackageName() != null) {
            return null;
        }
        if (snapshot != null && className != null) {
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
        }
        if (document.getUri() == null || !"file".equalsIgnoreCase(document.getUri().getScheme()) || className == null) {
            return null;
        }
        Path parent = Uris.toPath(document.getUri()).getParent();
        if (parent == null) {
            return null;
        }
        return parent.resolve(className.replace('.', '/') + ".groovy").toUri().toString();
    }

    private static final int TOKEN_CLASS = 1;
    private static final int TOKEN_ENUM = 2;
    private static final int TOKEN_INTERFACE = 3;
    private static final int TOKEN_METHOD = 4;
    private static final int TOKEN_PROPERTY = 5;
    private static final int TOKEN_VARIABLE = 6;
    private static final int TOKEN_PARAMETER = 7;
    private static final int TOKEN_KEYWORD = 8;

    private static int tokenType(final ASTNode node) {
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
