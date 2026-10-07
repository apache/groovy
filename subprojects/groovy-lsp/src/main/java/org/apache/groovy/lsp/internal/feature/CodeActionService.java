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
import org.apache.groovy.lsp.internal.compile.PackageGuess;
import org.apache.groovy.lsp.internal.compile.TypeIndex;
import org.apache.groovy.lsp.internal.diagnostic.DiagnosticConverter;
import org.apache.groovy.lsp.internal.position.PositionEncoding;
import org.apache.groovy.lsp.internal.position.Positions;
import org.apache.groovy.lsp.internal.util.Uris;
import org.apache.groovy.lsp.internal.workspace.TextDocument;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CreateFile;
import org.eclipse.lsp4j.CreateFileOptions;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ResourceOperation;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extra code actions inspired by Metals: unused imports, implement
 * abstracts, insert inferred types (fields and locals), named arguments,
 * add package, create missing class.
 */
public final class CodeActionService {

    private static final String PACKAGE_PREFIX = "package ";

    private static final Pattern UNRESOLVED_CLASS = Pattern.compile(
            "unable to resolve class ([\\w.]+)", Pattern.CASE_INSENSITIVE);

    /**
     * @param document open document
     * @param snapshot latest compile
     * @param diagnostics client diagnostics
     * @param range requested range
     * @param encoding negotiated encoding
     * @param folders workspace folders
     * @param sourcePaths client source paths
     * @return extra actions
     */
    public List<Either<Command, CodeAction>> contribute(final TextDocument document,
                                                        final CompilationSnapshot snapshot,
                                                        final List<Diagnostic> diagnostics,
                                                        final Range range, final PositionEncoding encoding,
                                                        final Collection<URI> folders, final List<String> sourcePaths) {
        List<Either<Command, CodeAction>> actions = new ArrayList<>();
        CompiledDocument compiled = snapshot == null || document == null ? null : snapshot.get(document);
        if (compiled == null || compiled.getModule() == null) {
            return actions;
        }
        addRemoveUnused(actions, compiled, encoding);
        addPackage(actions, compiled, folders, sourcePaths, encoding);
        addImplementAbstracts(actions, compiled, range, encoding);
        addInferredType(actions, compiled, range, encoding);
        addNamedArguments(actions, compiled, snapshot, range, encoding);
        addCreateClass(actions, compiled, snapshot, diagnostics);
        addSourceGeneration(actions, compiled, range, encoding);
        return actions;
    }

    private static void addSourceGeneration(final List<Either<Command, CodeAction>> actions,
                                            final CompiledDocument compiled, final Range range,
                                            final PositionEncoding encoding) {
        for (ClassNode classNode : compiled.getModule().getClasses()) {
            addSourceGenerationFor(actions, compiled, classNode, range, encoding);
        }
    }

    private static void addSourceGenerationFor(final List<Either<Command, CodeAction>> actions,
                                               final CompiledDocument compiled, final ClassNode classNode,
                                               final Range range, final PositionEncoding encoding) {
        if (classNode.isInterface() || classNode.isScript() || classNode.getLineNumber() <= 0) {
            return;
        }
        Range full = Positions.toRange(classNode, compiled.getText(), encoding);
        if (range != null && full != null && !overlaps(full, range)) {
            return;
        }
        addGenerate(actions, compiled, "Generate getters and setters", SourceGeneration.GENERATE_ACCESSORS,
                SourceGeneration.accessors(classNode, compiled, encoding));
        addGenerate(actions, compiled, "Generate toString()", SourceGeneration.GENERATE_TO_STRING,
                SourceGeneration.toStringMethod(classNode, compiled, encoding));
        addGenerate(actions, compiled, "Generate equals() and hashCode()", SourceGeneration.GENERATE_EQUALS,
                SourceGeneration.equalsAndHashCode(classNode, compiled, encoding));
        addGenerate(actions, compiled, "Generate constructor", SourceGeneration.GENERATE_CONSTRUCTORS,
                SourceGeneration.constructor(classNode, compiled, encoding));
        addGenerate(actions, compiled, "Add @Override annotations", CodeActionKind.Source,
                SourceGeneration.overrideAnnotations(classNode, compiled, encoding));
    }

    private static void addGenerate(final List<Either<Command, CodeAction>> actions, final CompiledDocument compiled,
                                    final String title, final String kind, final List<TextEdit> edits) {
        if (edits == null || edits.isEmpty()) {
            return;
        }
        CodeAction action = new CodeAction(title);
        action.setKind(kind);
        action.setEdit(new WorkspaceEdit(Map.of(compiled.getUri().toString(), edits)));
        actions.add(Either.forRight(action));
    }

    private static void addRemoveUnused(final List<Either<Command, CodeAction>> actions, final CompiledDocument compiled,
                                        final PositionEncoding encoding) {
        List<TextEdit> edits = ImportSupport.removeUnused(compiled, encoding);
        if (edits.isEmpty()) {
            return;
        }
        addGenerate(actions, compiled, "Remove unused imports", CodeActionKind.QuickFix, edits);
        addGenerate(actions, compiled, "Remove unused imports", CodeActionKind.SourceFixAll, edits);
    }

    private static void addPackage(final List<Either<Command, CodeAction>> actions, final CompiledDocument compiled,
                                   final Collection<URI> folders, final List<String> sourcePaths,
                                   final PositionEncoding encoding) {
        String guessed = PackageGuess.fromUri(compiled.getUri(), folders, sourcePaths);
        if (guessed.isEmpty() || guessed.equals(ImportSupport.packageName(compiled.getModule()))) {
            return;
        }
        TextEdit edit;
        if (compiled.getModule().getPackage() == null) {
            edit = new TextEdit(new Range(new Position(0, 0), new Position(0, 0)), PACKAGE_PREFIX + guessed + "\n\n");
        } else {
            Range range = Positions.toRange(compiled.getModule().getPackage(), compiled.getText(), encoding);
            if (range == null) {
                return;
            }
            edit = new TextEdit(range, PACKAGE_PREFIX + guessed);
        }
        addGenerate(actions, compiled, "Add package " + guessed, CodeActionKind.Source, List.of(edit));
    }

    private static void addImplementAbstracts(final List<Either<Command, CodeAction>> actions,
                                              final CompiledDocument compiled, final Range range,
                                              final PositionEncoding encoding) {
        for (ClassNode classNode : compiled.getModule().getClasses()) {
            addImplementAbstractsFor(actions, compiled, classNode, range, encoding);
        }
    }

    private static void addImplementAbstractsFor(final List<Either<Command, CodeAction>> actions,
                                                 final CompiledDocument compiled, final ClassNode classNode,
                                                 final Range range, final PositionEncoding encoding) {
        if (classNode.isInterface() || classNode.isAbstract() || classNode.isEnum() || classNode.isScript()) {
            return;
        }
        if (range != null && !overlapsName(classNode, compiled.getText(), encoding, range)) {
            return;
        }
        List<MethodNode> missing = missingAbstracts(classNode);
        if (missing.isEmpty()) {
            return;
        }
        Position insert = SourceGeneration.beforeClose(classNode, compiled, encoding);
        if (insert == null) {
            return;
        }
        addGenerate(actions, compiled, "Implement abstract methods", CodeActionKind.QuickFix,
                List.of(new TextEdit(new Range(insert, insert), stubs(missing))));
    }

    private static void addInferredType(final List<Either<Command, CodeAction>> actions,
                                        final CompiledDocument compiled, final Range range,
                                        final PositionEncoding encoding) {
        for (ClassNode classNode : compiled.getModule().getClasses()) {
            List<FieldNode> fields = new ArrayList<>(classNode.getFields());
            for (PropertyNode property : classNode.getProperties()) {
                if (property.getField() != null) {
                    fields.add(property.getField());
                }
            }
            for (FieldNode field : fields) {
                addInferredTypeFor(actions, compiled, field, range, encoding);
            }
        }
        AstQuery.walk(compiled.getModule(), (node, ctx) -> {
            if (node instanceof DeclarationExpression declaration
                    && !declaration.isMultipleAssignmentDeclaration()
                    && declaration.getVariableExpression() != null) {
                addInferredTypeFor(actions, compiled, declaration.getVariableExpression(),
                        compiled.getModule(), range, encoding);
            }
        });
    }

    private static void addCreateClass(final List<Either<Command, CodeAction>> actions,
                                       final CompiledDocument compiled, final CompilationSnapshot snapshot,
                                       final List<Diagnostic> diagnostics) {
        if (diagnostics == null) {
            return;
        }
        for (Diagnostic diagnostic : diagnostics) {
            addCreateClassFor(actions, compiled, snapshot, diagnostic);
        }
    }

    private static void addInferredTypeFor(final List<Either<Command, CodeAction>> actions,
                                           final CompiledDocument compiled, final FieldNode field,
                                           final Range range, final PositionEncoding encoding) {
        ClassNode type = TypeInference.of(field);
        if (field == null || field.getLineNumber() <= 0 || !SymbolIdentity.isResolvedType(type)) {
            return;
        }
        Range name = Positions.toNameRange(field, compiled.getText(), encoding);
        replaceDef(actions, compiled, type, name, range, encoding);
    }

    private static void addInferredTypeFor(final List<Either<Command, CodeAction>> actions,
                                           final CompiledDocument compiled, final VariableExpression variable,
                                           final ModuleNode module, final Range range,
                                           final PositionEncoding encoding) {
        if (variable == null || !variable.isDynamicTyped()) {
            return;
        }
        ClassNode type = TypeInference.of(variable, module);
        if (variable.getLineNumber() <= 0 || !SymbolIdentity.isResolvedType(type)) {
            return;
        }
        Range name = Positions.toNameRange(variable, compiled.getText(), encoding);
        replaceDef(actions, compiled, type, name, range, encoding);
    }

    private static void replaceDef(final List<Either<Command, CodeAction>> actions,
                                   final CompiledDocument compiled, final ClassNode type,
                                   final Range name, final Range range, final PositionEncoding encoding) {
        if (name == null || (range != null && !overlaps(name, range))) {
            return;
        }
        int groovyLine = name.getStart().getLine() + 1;
        String line = Positions.lineText(compiled.getText(), groovyLine);
        int limit = Math.min(Math.max(name.getStart().getCharacter(), 0), line.length());
        int defAt = trailingWord(line.substring(0, limit), "def");
        if (defAt < 0) {
            return;
        }
        Position start = Positions.toLsp(groovyLine, defAt + 1, line, encoding);
        Position end = Positions.toLsp(groovyLine, defAt + 4, line, encoding);
        addGenerate(actions, compiled, "Insert inferred type " + type.getNameWithoutPackage(),
                CodeActionKind.QuickFix, List.of(new TextEdit(new Range(start, end),
                        type.getNameWithoutPackage())));
    }

    /**
     * The {@code def} immediately before the name. A {@code def} earlier on
     * the line, or inside a longer identifier, is left alone.
     */
    private static int trailingWord(final String prefix, final String word) {
        int end = prefix.length();
        while (end > 0 && Character.isWhitespace(prefix.charAt(end - 1))) {
            end -= 1;
        }
        int start = end - word.length();
        if (start < 0 || !prefix.regionMatches(start, word, 0, word.length())) {
            return -1;
        }
        if (start > 0 && Character.isJavaIdentifierPart(prefix.charAt(start - 1))) {
            return -1;
        }
        return start;
    }

    private static void addNamedArguments(final List<Either<Command, CodeAction>> actions,
                                          final CompiledDocument compiled, final CompilationSnapshot snapshot,
                                          final Range range, final PositionEncoding encoding) {
        if (compiled.getModule() == null) {
            return;
        }
        AstQuery.walk(compiled.getModule(), (node, ctx) -> {
            MethodBinding.ResolvedCall resolved = MethodBinding.resolveCall(node, snapshot);
            if (resolved != null) {
                addNamedArgumentsFor(actions, compiled, resolved.node(), resolved.arguments(),
                        resolved.target(), range, encoding);
            }
        });
    }

    private static void addNamedArgumentsFor(final List<Either<Command, CodeAction>> actions,
                                             final CompiledDocument compiled, final ASTNode call,
                                             final Expression arguments, final MethodNode target,
                                             final Range range, final PositionEncoding encoding) {
        if (target == null || arguments == null || CallSites.hasNamedArgs(arguments)) {
            return;
        }
        Parameter[] parameters = target.getParameters();
        List<Expression> args = CallSites.argumentExpressions(arguments);
        if (parameters == null || parameters.length == 0 || args.size() != parameters.length) {
            return;
        }
        Range ident = Positions.toIdentifierRange(call, compiled.getText(), encoding);
        if (range != null && ident != null && !overlaps(ident, range)) {
            return;
        }
        Range first = Positions.toRange(args.get(0), compiled.getText(), encoding);
        Range last = Positions.toRange(args.get(args.size() - 1), compiled.getText(), encoding);
        String named = namedArgumentText(compiled.getText(), parameters, args, encoding);
        if (first == null || last == null || named == null) {
            return;
        }
        addGenerate(actions, compiled, "Convert to named arguments", CodeActionKind.RefactorRewrite,
                List.of(new TextEdit(new Range(first.getStart(), last.getEnd()), named)));
    }

    private static String namedArgumentText(final String text, final Parameter[] parameters,
                                            final List<Expression> args, final PositionEncoding encoding) {
        StringBuilder named = new StringBuilder();
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i] == null || parameters[i].getName() == null
                    || parameters[i].getName().startsWith("$")) {
                return null;
            }
            Range argRange = Positions.toRange(args.get(i), text, encoding);
            if (argRange == null) {
                return null;
            }
            if (i > 0) {
                named.append(", ");
            }
            named.append(parameters[i].getName()).append(": ").append(slice(text, argRange, encoding));
        }
        return named.toString();
    }

    private static String slice(final String text, final Range range, final PositionEncoding encoding) {
        if (text == null || range == null) {
            return "";
        }
        int start = TextDocument.offsetOf(text, range.getStart(), encoding);
        int end = TextDocument.offsetOf(text, range.getEnd(), encoding);
        if (start < 0) {
            start = 0;
        }
        if (end < start) {
            end = start;
        }
        if (end > text.length()) {
            end = text.length();
        }
        return text.substring(start, end);
    }

    private static void addCreateClassFor(final List<Either<Command, CodeAction>> actions,
                                          final CompiledDocument compiled, final CompilationSnapshot snapshot,
                                          final Diagnostic diagnostic) {
        String className = unresolvedClassName(DiagnosticConverter.diagnosticMessage(diagnostic));
        if (className == null) {
            return;
        }
        String packageName = ImportSupport.packageName(compiled.getModule());
        String simple = className;
        int dot = className.lastIndexOf('.');
        if (dot >= 0) {
            if (!className.substring(0, dot).equals(packageName)) {
                return;
            }
            simple = className.substring(dot + 1);
        }
        if (simple.isEmpty() || !Character.isUpperCase(simple.codePointAt(0)) || !RenameService.isIdentifier(simple)) {
            return;
        }
        String qualified = packageName.isEmpty() ? simple : packageName + "." + simple;
        if (snapshot != null && snapshot.types().byName(qualified) != null) {
            return;
        }
        String target = siblingGroovyUri(compiled.getUri(), simple);
        if (target == null || siblingExists(target, snapshot)) {
            return;
        }
        String stub = packageName.isEmpty() ? "class " + simple + " {\n}\n"
                : PACKAGE_PREFIX + packageName + "\n\nclass " + simple + " {\n}\n";
        CreateFile create = new CreateFile(target, new CreateFileOptions(Boolean.FALSE, Boolean.FALSE));
        TextEdit text = new TextEdit(new Range(new Position(0, 0), new Position(0, 0)), stub);
        TextDocumentEdit documentEdit = new TextDocumentEdit(
                new VersionedTextDocumentIdentifier(target, null), List.of(Either.forLeft(text)));
        WorkspaceEdit edit = new WorkspaceEdit(List.of(
                Either.<TextDocumentEdit, ResourceOperation>forRight(create),
                Either.<TextDocumentEdit, ResourceOperation>forLeft(documentEdit)));
        CodeAction action = new CodeAction("Create class " + simple);
        action.setKind(CodeActionKind.QuickFix);
        action.setEdit(edit);
        action.setDiagnostics(List.of(diagnostic));
        actions.add(Either.forRight(action));
    }

    static String siblingGroovyUri(final URI documentUri, final String simpleName) {
        if (documentUri == null || !"file".equalsIgnoreCase(documentUri.getScheme())) {
            return null;
        }
        String text = documentUri.toString();
        int slash = Uris.lastSeparator(text);
        if (slash < 0) {
            return null;
        }
        return text.substring(0, slash + 1) + simpleName + ".groovy";
    }

    private static boolean siblingExists(final String target, final CompilationSnapshot snapshot) {
        URI uri;
        try {
            uri = Uris.parse(target);
        } catch (RuntimeException ex) {
            return true;
        }
        if (snapshot != null && snapshot.get(uri) != null) {
            return true;
        }
        try {
            return Files.exists(Uris.toPath(uri));
        } catch (RuntimeException ex) {
            return true;
        }
    }

    static List<MethodNode> missingAbstracts(final ClassNode classNode) {
        return classNode.getAbstractMethods().stream()
                .filter(method -> isUnimplementedAbstract(classNode, method))
                .toList();
    }

    private static boolean isUnimplementedAbstract(final ClassNode classNode, final MethodNode method) {
        if (method.isSynthetic()) {
            return false;
        }
        ClassNode owner = method.getDeclaringClass();
        if (owner != null && ("groovy.lang.GroovyObject".equals(owner.getName())
                || "groovy.lang.GroovyObjectSupport".equals(owner.getName()))) {
            return false;
        }
        MethodNode declared = classNode.getDeclaredMethod(method.getName(), method.getParameters());
        if (declared != null && declared.getCode() != null && !declared.isAbstract()) {
            return false;
        }
        return declared != method;
    }

    private static String stubs(final List<MethodNode> methods) {
        StringBuilder builder = new StringBuilder();
        for (MethodNode method : methods) {
            builder.append("    ").append(overrideSnippet(method).replace("\n", "\n    ")).append('\n');
        }
        return builder.toString();
    }

    /**
     * One {@code @Override} method stub for completions and generate.
     *
     * @param method abstract method
     * @return snippet text
     */
    static String overrideSnippet(final MethodNode method) {
        StringBuilder builder = new StringBuilder("@Override\n");
        builder.append(returnName(method)).append(' ').append(method.getName()).append('(');
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(parameters[i].getType().getNameWithoutPackage()).append(' ')
                    .append(parameters[i].getName());
        }
        builder.append(") {\n    throw new UnsupportedOperationException('TODO')\n}");
        return builder.toString();
    }

    private static String returnName(final MethodNode method) {
        ClassNode type = method.getReturnType();
        if (type == null || ClassHelper.isObjectType(type) || ClassHelper.isDynamicTyped(type)) {
            return "def";
        }
        if (ClassHelper.isPrimitiveVoid(type) || "void".equals(type.getName())) {
            return "void";
        }
        return type.getNameWithoutPackage();
    }

    private static boolean overlapsName(final ClassNode classNode, final String text, final PositionEncoding encoding,
                                        final Range range) {
        Range name = Positions.toNameRange(classNode, text, encoding);
        Range full = Positions.toRange(classNode, text, encoding);
        return overlaps(name, range) || overlaps(full, range);
    }

    private static boolean overlaps(final Range left, final Range right) {
        if (left == null || right == null) {
            return true;
        }
        return left.getEnd().getLine() >= right.getStart().getLine()
                && left.getStart().getLine() <= right.getEnd().getLine();
    }

    public List<Either<Command, CodeAction>> collect(final TextDocument document,
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
        addAmbiguousImportFixes(actions, document, compiled, snapshot, diagnostics, encoding);
        addUnambiguousImports(actions, compiled, snapshot, diagnostics, encoding);
        actions.addAll(contribute(document, snapshot, diagnostics, range, encoding, folders, sourcePaths));
        return actions;
    }

    private static void addAmbiguousImportFixes(final List<Either<Command, CodeAction>> actions,
                                                final TextDocument document, final CompiledDocument compiled,
                                                final CompilationSnapshot snapshot, final List<Diagnostic> diagnostics,
                                                final PositionEncoding encoding) {
        if (diagnostics == null) {
            return;
        }
        for (Diagnostic diagnostic : diagnostics) {
            String className = unresolvedClassName(DiagnosticConverter.diagnosticMessage(diagnostic));
            if (className == null) {
                continue;
            }
            for (TypeIndex.TypeHit hit : importCandidates(className, snapshot)) {
                addImportFix(actions, document, compiled, encoding, diagnostic, hit);
            }
        }
    }

    private static void addImportFix(final List<Either<Command, CodeAction>> actions, final TextDocument document,
                                     final CompiledDocument compiled, final PositionEncoding encoding,
                                     final Diagnostic diagnostic, final TypeIndex.TypeHit hit) {
        List<TextEdit> edits = ImportSupport.addImport(compiled, hit.name(), encoding);
        if (edits.isEmpty()) {
            return;
        }
        CodeAction action = new CodeAction("Add import for " + hit.name());
        action.setKind(CodeActionKind.QuickFix);
        action.setDiagnostics(List.of(diagnostic));
        action.setEdit(new WorkspaceEdit(Map.of(document.getUri().toString(), edits)));
        actions.add(Either.forRight(action));
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
        String current = blockText(document.getText(), first, last);
        if (replacement.equals(current)) {
            return List.of();
        }
        return List.of(new TextEdit(new Range(first.getStart(), last.getEnd()), replacement));
    }

    private static Range earlier(final Range current, final Range candidate) {
        if (current == null || candidate.getStart().getLine() < current.getStart().getLine()
                || (candidate.getStart().getLine() == current.getStart().getLine()
                && candidate.getStart().getCharacter() < current.getStart().getCharacter())) {
            return candidate;
        }
        return current;
    }

    private static Range later(final Range current, final Range candidate) {
        if (current == null || candidate.getEnd().getLine() > current.getEnd().getLine()
                || (candidate.getEnd().getLine() == current.getEnd().getLine()
                && candidate.getEnd().getCharacter() > current.getEnd().getCharacter())) {
            return candidate;
        }
        return current;
    }

    private static String blockText(final String text, final Range first, final Range last) {
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
            if (!slice.isEmpty()) {
                slice.append('\n');
            }
            slice.append(row, from, to);
        }
        return slice.toString();
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
        String className = unresolvedClassName(DiagnosticConverter.diagnosticMessage(diagnostic));
        if (className == null || className.contains(".")) {
            return null;
        }
        List<TypeIndex.TypeHit> hits = snapshot == null ? List.of() : snapshot.types().bySimpleName(className);
        if (hits.size() != 1 || !seen.add(hits.get(0).name())) {
            return null;
        }
        return hits.get(0);
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

    static String unresolvedClassName(final String message) {
        if (message == null) {
            return null;
        }
        Matcher matcher = UNRESOLVED_CLASS.matcher(message);
        return matcher.find() ? matcher.group(1) : null;
    }

}
