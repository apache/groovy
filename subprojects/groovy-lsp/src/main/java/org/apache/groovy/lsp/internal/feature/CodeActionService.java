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

import org.apache.groovy.lsp.internal.compile.CompilationSnapshot;
import org.apache.groovy.lsp.internal.compile.CompiledDocument;
import org.apache.groovy.lsp.internal.compile.ImportSupport;
import org.apache.groovy.lsp.internal.compile.PackageGuess;
import org.apache.groovy.lsp.internal.position.PositionEncoding;
import org.apache.groovy.lsp.internal.position.Positions;
import org.apache.groovy.lsp.internal.workspace.TextDocument;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Parameter;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extra code actions inspired by Metals: unused imports, implement
 * abstracts, insert inferred types, add package, create missing class.
 */
public final class CodeActionService {

    private static final String CANONICAL = "Canonical";
    private static final Pattern UNRESOLVED_CLASS = Pattern.compile("unable to resolve class ([\\w.]+)", Pattern.CASE_INSENSITIVE);

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
        CompiledDocument compiled = snapshot == null || document == null ? null : snapshot.get(document.getUri());
        if (compiled == null || compiled.getModule() == null) {
            return actions;
        }
        addRemoveUnused(actions, compiled, encoding);
        addPackage(actions, compiled, folders, sourcePaths, encoding);
        addImplementAbstracts(actions, compiled, range, encoding);
        addInferredType(actions, compiled, range, encoding);
        addCreateClass(actions, compiled, diagnostics, encoding);
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
        if (!hasAst(classNode, "ToString") && !hasAst(classNode, CANONICAL)) {
            addGenerate(actions, compiled, "Generate toString()", SourceGeneration.GENERATE_TO_STRING,
                    SourceGeneration.toStringMethod(classNode, compiled, encoding));
        }
        if (!hasAst(classNode, "EqualsAndHashCode") && !hasAst(classNode, CANONICAL)) {
            addGenerate(actions, compiled, "Generate equals() and hashCode()", SourceGeneration.GENERATE_EQUALS,
                    SourceGeneration.equalsAndHashCode(classNode, compiled, encoding));
        }
        if (!hasAst(classNode, "TupleConstructor") && !hasAst(classNode, CANONICAL)
                && !hasAst(classNode, "MapConstructor")) {
            addGenerate(actions, compiled, "Generate constructor", SourceGeneration.GENERATE_CONSTRUCTORS,
                    SourceGeneration.constructor(classNode, compiled, encoding));
        }
        addGenerate(actions, compiled, "Add @Override annotations", CodeActionKind.Source,
                SourceGeneration.overrideAnnotations(classNode, compiled, encoding));
    }

    private static boolean hasAst(final ClassNode classNode, final String simpleName) {
        return classNode.getAnnotations().stream()
                .anyMatch(a -> a.getClassNode() != null && simpleName.equals(a.getClassNode().getNameWithoutPackage()));
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
        CodeAction action = new CodeAction("Remove unused imports");
        action.setKind(CodeActionKind.QuickFix);
        action.setEdit(new WorkspaceEdit(Map.of(compiled.getUri().toString(), edits)));
        actions.add(Either.forRight(action));
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
            edit = new TextEdit(new Range(new Position(0, 0), new Position(0, 0)), "package " + guessed + "\n\n");
        } else {
            Range range = Positions.toRange(compiled.getModule().getPackage(), compiled.getText(), encoding);
            if (range == null) {
                return;
            }
            edit = new TextEdit(range, "package " + guessed);
        }
        CodeAction action = new CodeAction("Add package " + guessed);
        action.setKind(CodeActionKind.Source);
        action.setEdit(new WorkspaceEdit(Map.of(compiled.getUri().toString(), List.of(edit))));
        actions.add(Either.forRight(action));
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
        Range classRange = Positions.toRange(classNode, compiled.getText(), encoding);
        if (classRange == null) {
            return;
        }
        String stub = stubs(missing);
        Position insert = classRange.getEnd();
        String line = Positions.lineText(compiled.getText(), insert.getLine() + 1);
        int brace = line.lastIndexOf('}');
        if (brace >= 0) {
            insert = Positions.toLsp(insert.getLine() + 1, brace + 1, line, encoding);
        }
        CodeAction action = new CodeAction("Implement abstract methods");
        action.setKind(CodeActionKind.QuickFix);
        action.setEdit(new WorkspaceEdit(Map.of(compiled.getUri().toString(),
                List.of(new TextEdit(new Range(insert, insert), stub)))));
        actions.add(Either.forRight(action));
    }

    private static void addInferredType(final List<Either<Command, CodeAction>> actions,
                                        final CompiledDocument compiled, final Range range,
                                        final PositionEncoding encoding) {
        for (ClassNode classNode : compiled.getModule().getClasses()) {
            List<FieldNode> fields = new ArrayList<>();
            fields.addAll(classNode.getFields());
            for (PropertyNode property : classNode.getProperties()) {
                if (property.getField() != null) {
                    fields.add(property.getField());
                }
            }
            for (FieldNode field : fields) {
                addInferredTypeFor(actions, compiled, field, range, encoding);
            }
        }
    }

    private static void addCreateClass(final List<Either<Command, CodeAction>> actions,
                                       final CompiledDocument compiled,
                                       final List<Diagnostic> diagnostics,
                                       final PositionEncoding encoding) {
        if (diagnostics == null) {
            return;
        }
        for (Diagnostic diagnostic : diagnostics) {
            addCreateClassFor(actions, compiled, diagnostic, encoding);
        }
    }

    private static void addInferredTypeFor(final List<Either<Command, CodeAction>> actions,
                                           final CompiledDocument compiled, final FieldNode field,
                                           final Range range, final PositionEncoding encoding) {
        ClassNode type = field.getType();
        if (!SymbolIdentity.isResolvedType(type) && field.getInitialExpression() != null) {
            type = field.getInitialExpression().getType();
        }
        if (field.getLineNumber() <= 0 || !SymbolIdentity.isResolvedType(type)) {
            return;
        }
        Range name = Positions.toNameRange(field, compiled.getText(), encoding);
        if (name == null || (range != null && !overlaps(name, range))) {
            return;
        }
        int groovyLine = field.getLineNumber();
        String line = Positions.lineText(compiled.getText(), groovyLine);
        int defAt = wordIndex(line, "def");
        if (defAt < 0) {
            return;
        }
        Position start = Positions.toLsp(groovyLine, defAt + 1, line, encoding);
        Position end = Positions.toLsp(groovyLine, defAt + 4, line, encoding);
        CodeAction action = new CodeAction("Insert inferred type " + type.getNameWithoutPackage());
        action.setKind(CodeActionKind.QuickFix);
        action.setEdit(new WorkspaceEdit(Map.of(compiled.getUri().toString(),
                List.of(new TextEdit(new Range(start, end), type.getNameWithoutPackage())))));
        actions.add(Either.forRight(action));
    }

    private static void addCreateClassFor(final List<Either<Command, CodeAction>> actions,
                                          final CompiledDocument compiled, final Diagnostic diagnostic,
                                          final PositionEncoding encoding) {
        String message = SupportServices.diagnosticMessage(diagnostic);
        if (message == null) {
            return;
        }
        Matcher matcher = UNRESOLVED_CLASS.matcher(message);
        if (!matcher.find()) {
            return;
        }
        String className = matcher.group(1);
        if (className.contains(".")) {
            className = className.substring(className.lastIndexOf('.') + 1);
        }
        if (!Character.isUpperCase(className.codePointAt(0))) {
            return;
        }
        Range end = compiled.getText().isEmpty() ? new Range(new Position(0, 0), new Position(0, 0))
                : new Range(documentEnd(compiled, encoding), documentEnd(compiled, encoding));
        String stub = "\nclass " + className + " {\n}\n";
        CodeAction action = new CodeAction("Create class " + className);
        action.setKind(CodeActionKind.QuickFix);
        action.setDiagnostics(List.of(diagnostic));
        action.setEdit(new WorkspaceEdit(Map.of(compiled.getUri().toString(),
                List.of(new TextEdit(end, stub)))));
        actions.add(Either.forRight(action));
    }

    static List<MethodNode> missingAbstracts(final ClassNode classNode) {
        List<MethodNode> missing = new ArrayList<>();
        for (MethodNode method : classNode.getAbstractMethods()) {
            if (isUnimplementedAbstract(classNode, method)) {
                missing.add(method);
            }
        }
        return missing;
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
            builder.append("    @Override\n    ");
            builder.append(returnName(method)).append(' ').append(method.getName()).append('(');
            Parameter[] parameters = method.getParameters();
            for (int i = 0; i < parameters.length; i++) {
                if (i > 0) {
                    builder.append(", ");
                }
                builder.append(parameters[i].getType().getNameWithoutPackage()).append(' ')
                        .append(parameters[i].getName());
            }
            builder.append(") {\n        throw new UnsupportedOperationException('TODO')\n    }\n");
        }
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

    private static int wordIndex(final String line, final String word) {
        int from = 0;
        while (from < line.length()) {
            int at = line.indexOf(word, from);
            if (at < 0) {
                return -1;
            }
            boolean startOk = at == 0 || !Character.isJavaIdentifierPart(line.codePointBefore(at));
            int end = at + word.length();
            boolean endOk = end >= line.length() || !Character.isJavaIdentifierPart(line.codePointAt(end));
            if (startOk && endOk) {
                return at;
            }
            from = end;
        }
        return -1;
    }

    private static Position documentEnd(final CompiledDocument compiled, final PositionEncoding encoding) {
        String text = compiled.getText();
        if (text.isEmpty()) {
            return new Position(0, 0);
        }
        int line = 1;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n' || (c == '\r' && (i + 1 >= text.length() || text.charAt(i + 1) != '\n'))) {
                line += 1;
            }
        }
        if (text.charAt(text.length() - 1) == '\n' || text.charAt(text.length() - 1) == '\r') {
            return new Position(line - 1, 0);
        }
        String last = Positions.lineText(text, line);
        return Positions.toLsp(line, last.codePointCount(0, last.length()) + 1, last, encoding);
    }
}
