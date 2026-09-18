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
import org.apache.groovy.lsp.internal.position.PositionEncoding;
import org.apache.groovy.lsp.internal.position.Positions;
import org.apache.groovy.lsp.internal.workspace.TextDocument;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
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

/**
 * Extra code actions inspired by Metals: unused imports, implement
 * abstracts, insert inferred types (fields and locals), named arguments,
 * add package, create missing class.
 */
public final class CodeActionService {

    private static final String CANONICAL = "Canonical";

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
        addGenerate(actions, compiled, title, kind, edits, null);
    }

    private static void addGenerate(final List<Either<Command, CodeAction>> actions, final CompiledDocument compiled,
                                    final String title, final String kind, final List<TextEdit> edits,
                                    final List<Diagnostic> diagnostics) {
        if (edits == null || edits.isEmpty()) {
            return;
        }
        CodeAction action = new CodeAction(title);
        action.setKind(kind);
        action.setEdit(new WorkspaceEdit(Map.of(compiled.getUri().toString(), edits)));
        if (diagnostics != null && !diagnostics.isEmpty()) {
            action.setDiagnostics(diagnostics);
        }
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
            edit = new TextEdit(new Range(new Position(0, 0), new Position(0, 0)), "package " + guessed + "\n\n");
        } else {
            Range range = Positions.toRange(compiled.getModule().getPackage(), compiled.getText(), encoding);
            if (range == null) {
                return;
            }
            edit = new TextEdit(range, "package " + guessed);
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
        ClassNode type = TypeInference.of(field);
        if (field == null || field.getLineNumber() <= 0 || !SymbolIdentity.isResolvedType(type)) {
            return;
        }
        Range name = Positions.toNameRange(field, compiled.getText(), encoding);
        replaceDef(actions, compiled, field.getLineNumber(), type, name, range, encoding);
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
        replaceDef(actions, compiled, variable.getLineNumber(), type, name, range, encoding);
    }

    private static void replaceDef(final List<Either<Command, CodeAction>> actions,
                                   final CompiledDocument compiled, final int groovyLine, final ClassNode type,
                                   final Range name, final Range range, final PositionEncoding encoding) {
        if (name == null || (range != null && !overlaps(name, range))) {
            return;
        }
        String line = Positions.lineText(compiled.getText(), groovyLine);
        int defAt = wordIndex(line, "def");
        if (defAt < 0) {
            return;
        }
        Position start = Positions.toLsp(groovyLine, defAt + 1, line, encoding);
        Position end = Positions.toLsp(groovyLine, defAt + 4, line, encoding);
        addGenerate(actions, compiled, "Insert inferred type " + type.getNameWithoutPackage(),
                CodeActionKind.QuickFix, List.of(new TextEdit(new Range(start, end),
                        type.getNameWithoutPackage())));
    }

    private static void addNamedArguments(final List<Either<Command, CodeAction>> actions,
                                          final CompiledDocument compiled, final CompilationSnapshot snapshot,
                                          final Range range, final PositionEncoding encoding) {
        if (compiled.getModule() == null) {
            return;
        }
        AstQuery.walk(compiled.getModule(), (node, ctx) -> {
            if (node instanceof MethodCallExpression call) {
                addNamedArgumentsFor(actions, compiled, call, call.getArguments(),
                        NavigationService.resolveMethod(call, snapshot), range, encoding);
            } else if (node instanceof StaticMethodCallExpression call) {
                addNamedArgumentsFor(actions, compiled, call, call.getArguments(),
                        NavigationService.resolveStatic(call, snapshot), range, encoding);
            } else if (node instanceof ConstructorCallExpression ctor) {
                addNamedArgumentsFor(actions, compiled, ctor, ctor.getArguments(),
                        NavigationService.resolveConstructor(ctor), range, encoding);
            }
        });
    }

    private static void addNamedArgumentsFor(final List<Either<Command, CodeAction>> actions,
                                             final CompiledDocument compiled, final ASTNode call,
                                             final Expression arguments, final MethodNode target,
                                             final Range range, final PositionEncoding encoding) {
        if (target == null || arguments == null || hasNamedArgs(arguments)) {
            return;
        }
        Parameter[] parameters = target.getParameters();
        List<Expression> args = argumentList(arguments);
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

    private static boolean hasNamedArgs(final Expression arguments) {
        if (arguments instanceof MapExpression) {
            return true;
        }
        if (arguments instanceof TupleExpression tuple) {
            for (Expression arg : tuple.getExpressions()) {
                if (arg instanceof MapExpression) {
                    return true;
                }
            }
        }
        return false;
    }

    private static List<Expression> argumentList(final Expression arguments) {
        if (arguments instanceof TupleExpression tuple) {
            return tuple.getExpressions();
        }
        return arguments == null ? List.of() : List.of(arguments);
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
                                          final CompiledDocument compiled, final Diagnostic diagnostic,
                                          final PositionEncoding encoding) {
        String className = SupportServices.unresolvedClassName(SupportServices.diagnosticMessage(diagnostic));
        if (className == null) {
            return;
        }
        if (className.contains(".")) {
            className = className.substring(className.lastIndexOf('.') + 1);
        }
        if (!Character.isUpperCase(className.codePointAt(0))) {
            return;
        }
        Range end = compiled.getText().isEmpty() ? new Range(new Position(0, 0), new Position(0, 0))
                : new Range(documentEnd(compiled, encoding), documentEnd(compiled, encoding));
        addGenerate(actions, compiled, "Create class " + className, CodeActionKind.QuickFix,
                List.of(new TextEdit(end, "\nclass " + className + " {\n}\n")), List.of(diagnostic));
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
