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
package org.apache.groovy.lsp.internal.compile;

import org.apache.groovy.lsp.internal.position.PositionEncoding;
import org.apache.groovy.lsp.internal.position.Positions;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.ResolveVisitor;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.DiagnosticTag;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Import insertion shared by completion additional-text-edits and the
 * add-import code action.
 */
public final class ImportSupport {

    private static final Set<String> DEFAULT_STAR_PACKAGES = defaultStarPackages();
    private static final Set<String> DEFAULT_TYPES = Set.of(
            ClassHelper.BigDecimal_TYPE.getName(), ClassHelper.BigInteger_TYPE.getName());

    private ImportSupport() {
    }

    private static Set<String> defaultStarPackages() {
        Set<String> packages = new LinkedHashSet<>();
        for (String prefix : ResolveVisitor.DEFAULT_IMPORTS) {
            if (prefix != null && !prefix.isEmpty()) {
                packages.add(prefix.endsWith(".") ? prefix.substring(0, prefix.length() - 1) : prefix);
            }
        }
        return Set.copyOf(packages);
    }

    /**
     * @param module module AST
     * @return every import, including star and static forms
     */
    public static List<ImportNode> allImports(final ModuleNode module) {
        List<ImportNode> imports = new ArrayList<>();
        if (module == null) {
            return imports;
        }
        imports.addAll(module.getImports());
        imports.addAll(module.getStarImports());
        imports.addAll(module.getStaticImports().values());
        imports.addAll(module.getStaticStarImports().values());
        return imports;
    }

    /**
     * @param module module AST
     * @return package without a trailing dot, or {@code ""}
     */
    public static String packageName(final ModuleNode module) {
        if (module == null || module.getPackageName() == null) {
            return "";
        }
        String name = module.getPackageName();
        while (name.endsWith(".")) {
            name = name.substring(0, name.length() - 1);
        }
        return name;
    }

    /**
     * @param fqcn fully qualified class name
     * @return the package, or {@code ""}
     */
    public static String packageOf(final String fqcn) {
        if (fqcn == null) {
            return "";
        }
        int dot = fqcn.lastIndexOf('.');
        return dot < 0 ? "" : fqcn.substring(0, dot);
    }

    /**
     * @param module current module
     * @param fqcn type to import
     * @return {@code true} when an {@code import} statement is required
     */
    public static boolean needsImport(final ModuleNode module, final String fqcn) {
        if (fqcn == null || fqcn.isBlank() || !fqcn.contains(".")) {
            return false;
        }
        String pkg = packageOf(fqcn);
        if (isDefaultImported(fqcn, pkg, module)) {
            return false;
        }
        for (ImportNode existing : allImports(module)) {
            if (fqcn.equals(existing.getClassName()) || coversStar(existing, pkg)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isDefaultImported(final String fqcn, final String pkg, final ModuleNode module) {
        return DEFAULT_TYPES.contains(fqcn) || DEFAULT_STAR_PACKAGES.contains(pkg) || pkg.equals(packageName(module));
    }

    private static boolean coversStar(final ImportNode existing, final String pkg) {
        if (!existing.isStar() || existing.isStatic()) {
            return false;
        }
        String star = existing.getPackageName();
        if (star == null) {
            return false;
        }
        while (star.endsWith(".")) {
            star = star.substring(0, star.length() - 1);
        }
        return pkg.equals(star);
    }

    /**
     * @param document compiled file
     * @param fqcn type to import
     * @param encoding negotiated encoding
     * @return a single insert edit, or empty
     */
    public static List<TextEdit> addImport(final CompiledDocument document, final String fqcn,
                                           final PositionEncoding encoding) {
        if (document == null || document.getModule() == null || fqcn == null || fqcn.isBlank()) {
            return List.of();
        }
        if (!needsImport(document.getModule(), fqcn)) {
            return List.of();
        }
        Position insert = insertPosition(document, encoding);
        return List.of(new TextEdit(new Range(insert, insert), "import " + fqcn + "\n"));
    }

    /**
     * Simple and static imports whose identifier occurs only on the import line.
     *
     * @param document compiled file
     * @return unused import nodes
     */
    public static List<ImportNode> unused(final CompiledDocument document) {
        List<ImportNode> unused = new ArrayList<>();
        if (document == null || document.getModule() == null) {
            return unused;
        }
        String text = document.getText();
        for (ImportNode imp : allImports(document.getModule())) {
            if (isUnusedImport(imp, text)) {
                unused.add(imp);
            }
        }
        return unused;
    }

    private static boolean isUnusedImport(final ImportNode imp, final String text) {
        if (imp.isStar()) {
            return false;
        }
        String ident = importedName(imp);
        return ident != null && !ident.isEmpty() && Identifiers.count(text, ident) <= 1;
    }

    /**
     * @param document compiled file
     * @param encoding negotiated encoding
     * @return hint diagnostics for unused imports
     */
    public static List<Diagnostic> unusedDiagnostics(final CompiledDocument document,
                                                     final PositionEncoding encoding) {
        List<Diagnostic> diagnostics = new ArrayList<>();
        for (ImportNode imp : unused(document)) {
            Range range = rangeOf(imp, document, encoding);
            if (range == null) {
                continue;
            }
            Diagnostic diagnostic = new Diagnostic();
            diagnostic.setRange(range);
            diagnostic.setSeverity(DiagnosticSeverity.Hint);
            diagnostic.setSource("groovy");
            diagnostic.setMessage("Unused import");
            diagnostic.setTags(List.of(DiagnosticTag.Unnecessary));
            diagnostics.add(diagnostic);
        }
        return diagnostics;
    }

    /**
     * @param document compiled file
     * @param encoding negotiated encoding
     * @return deletions covering unused import lines
     */
    public static List<TextEdit> removeUnused(final CompiledDocument document, final PositionEncoding encoding) {
        List<TextEdit> edits = new ArrayList<>();
        for (ImportNode imp : unused(document)) {
            Range range = rangeOf(imp, document, encoding);
            if (range == null) {
                continue;
            }
            Position end = range.getEnd();
            edits.add(new TextEdit(new Range(range.getStart(), new Position(end.getLine() + 1, 0)), ""));
        }
        return edits;
    }

    private static Range rangeOf(final ImportNode imp, final CompiledDocument document,
                                 final PositionEncoding encoding) {
        Range range = Positions.toRange(imp, document.getText(), encoding);
        if (range != null) {
            return range;
        }
        String snippet = imp.getText();
        if (snippet == null || snippet.isEmpty()) {
            return null;
        }
        int at = document.getText().indexOf(snippet);
        if (at < 0) {
            return null;
        }
        int line = 1;
        for (int i = 0; i < at; i++) {
            if (document.getText().charAt(i) == '\n') {
                line += 1;
            }
        }
        String lineText = Positions.lineText(document.getText(), line);
        return new Range(Positions.toLsp(line, 1, lineText, encoding),
                Positions.toLsp(line, lineText.codePointCount(0, lineText.length()) + 1, lineText, encoding));
    }

    /**
     * @param imp import node
     * @return the identifier introduced into the file
     */
    public static String importedName(final ImportNode imp) {
        if (imp == null) {
            return null;
        }
        if (imp.isStatic() && !imp.isStar()) {
            String alias = imp.getAlias();
            if (alias != null && !alias.isEmpty() && !alias.equals(imp.getFieldName())) {
                return alias;
            }
            return imp.getFieldName();
        }
        String alias = imp.getAlias();
        if (alias != null && !alias.isEmpty()) {
            String simple = simpleName(imp.getClassName());
            if (!alias.equals(simple)) {
                return alias;
            }
        }
        return simpleName(imp.getClassName());
    }

    private static String simpleName(final String fqcn) {
        if (fqcn == null) {
            return null;
        }
        int dot = fqcn.lastIndexOf('.');
        return dot < 0 ? fqcn : fqcn.substring(dot + 1);
    }

    /**
     * @param document compiled file
     * @param encoding negotiated encoding
     * @return insertion point after the package or before existing imports
     */
    public static Position insertPosition(final CompiledDocument document, final PositionEncoding encoding) {
        if (document.getModule().getPackage() != null) {
            Range range = Positions.toRange(document.getModule().getPackage(), document.getText(), encoding);
            if (range != null) {
                return new Position(range.getEnd().getLine() + 1, 0);
            }
        }
        List<ImportNode> imports = allImports(document.getModule());
        if (!imports.isEmpty()) {
            Range range = Positions.toRange(imports.get(0), document.getText(), encoding);
            if (range != null) {
                return range.getStart();
            }
        }
        return new Position(0, 0);
    }
}
