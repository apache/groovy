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

// The lint gate: the CodeNarc rules that FAIL the build. Everything in
// codenarc.groovy stays advisory. A rule graduates here once the tree is
// clean for it, or is grandfathered with a baseline of the files that still
// violate it; shrink a baseline as files are cleaned, never grow it.
//
// Run with: ./gradlew lintGate --continue
// CI: .github/workflows/groovy-lint-gate.yml

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotatedNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.GenericsType
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.expr.ArrayExpression
import org.codehaus.groovy.ast.expr.CastExpression
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.CatchStatement
import groovy.transform.CompileStatic
import org.codenarc.rule.AbstractAstVisitor
import org.codenarc.rule.AbstractAstVisitorRule
import org.codenarc.rule.Violation
import org.codenarc.rule.imports.UnusedImportRule
import org.codenarc.source.SourceCode

import java.util.regex.Pattern

/**
 * Flags a package-qualified type name written where the simple name would
 * do: {@code java.util.Map<String, String> m} instead of importing Map.
 * Applies to type positions (fields, parameters, return types, locals,
 * generics, casts, constructor calls, annotations, catch clauses, supertypes),
 * to static references such as {@code java.nio.file.Files.exists(p)}, and to
 * {@code {@link}} targets in comments. A qualification is left alone when the
 * simple name is already taken by an import from another package or by a
 * class declared in the same file, which is the one case where it is needed.
 * Suppress a deliberate exception with
 * {@code @SuppressWarnings('UnnecessaryFullyQualifiedName')}.
 */
/** what a file binds each simple name to, which decides whether a qualification is needed */
@CompileStatic
class QualifiedNames {
    /** two or more lowercase package segments, then a type name */
    static final Pattern QUALIFIED_TYPE = ~/^([a-z][a-z0-9_]*(?:\.[a-z][a-z0-9_]*)+)\.([A-Z]\w*)$/
    /**
     * a static reference is only taken for package-qualified when the chain starts
     * with a package root: a lowercase chain such as {@code map.attributes.Id} is
     * otherwise a property path on a variable
     */
    static final Pattern PACKAGE_PATH = ~/^(?:java|javax|jakarta|jdk|org|com|net|io|groovy)(?:\.[a-z][a-z0-9_]*)+$/
    static final Pattern LINK_TAG = ~/\{@link(?:plain)?\s+([a-z][a-z0-9_]*(?:\.[a-z][a-z0-9_]*)+)\.([A-Z]\w*)/

    /** the packages Groovy imports by default, which bind simple names just as a star import does */
    private static final List<String> DEFAULT_STAR_PACKAGES = ['java.util.', 'java.io.', 'java.net.', 'groovy.lang.', 'groovy.util.']

    private final Map<String, String> importedByName = [:]
    private final Set<String> declaredInFile
    /** star-imported packages, explicit and default, each ending with a dot */
    private final List<String> starPackages

    QualifiedNames(ModuleNode module) {
        module?.imports?.each { importedByName[it.alias] = it.className }
        declaredInFile = (module?.classes*.nameWithoutPackage ?: []) as Set
        starPackages = (module?.starImports*.packageName ?: []) + DEFAULT_STAR_PACKAGES
    }

    /**
     * Whether the qualification is required: the simple name is bound to another
     * type by an import, by a class declared in the same file, or by a star import
     * of a package that has a class of that name (java.awt.* supplying List, say).
     * java.lang is never checked, since def, implicit supertypes and the like
     * surface as java.lang types in the unresolved AST.
     */
    boolean isNeeded(String packagePath, String simpleName) {
        if (packagePath == 'java.lang') return true
        String bound = importedByName[simpleName]
        if (bound != null) return bound != "${packagePath}.${simpleName}".toString()
        if (declaredInFile.contains(simpleName)) return true
        starPackages.any { String pkg -> pkg != packagePath + '.' && classExists(pkg + simpleName) }
    }

    private static boolean classExists(String name) {
        try {
            Class.forName(name, false, QualifiedNames.classLoader)
            true
        } catch (Throwable ignored) {
            false
        }
    }
}

@CompileStatic
class UnnecessaryFullyQualifiedNameRule extends AbstractAstVisitorRule {
    String name = 'UnnecessaryFullyQualifiedName'
    int priority = 1
    Class astVisitorClass = UnnecessaryFullyQualifiedNameAstVisitor

    /** the AST visitor runs per class; the comment scan runs here, once per file */
    @Override
    void applyTo(SourceCode sourceCode, List<Violation> violations) {
        super.applyTo(sourceCode, violations)
        ModuleNode module = sourceCode.ast
        if (module == null) return
        QualifiedNames names = new QualifiedNames(module)
        sourceCode.lines.eachWithIndex { String line, int index ->
            String trimmed = line.trim()
            if (!(trimmed.startsWith('*') || trimmed.startsWith('/*') || trimmed.startsWith('//'))) return
            def m = QualifiedNames.LINK_TAG.matcher(line)
            while (m.find()) {
                String packagePath = m.group(1), simpleName = m.group(2)
                if (names.isNeeded(packagePath, simpleName)) continue
                violations << new Violation(rule: this, lineNumber: index + 1, sourceLine: trimmed,
                        message: "Import ${packagePath}.${simpleName} and write {@link ${simpleName}}".toString())
            }
        }
    }
}

@CompileStatic
class UnnecessaryFullyQualifiedNameAstVisitor extends AbstractAstVisitor {
    private QualifiedNames names

    private QualifiedNames names() {
        if (names == null) names = new QualifiedNames(sourceCode.ast)
        names
    }

    private void checkName(String text, ASTNode node, String what) {
        if (!text) return
        def m = QualifiedNames.QUALIFIED_TYPE.matcher(text)
        if (!m.matches()) return
        String packagePath = m.group(1), simpleName = m.group(2)
        if (names().isNeeded(packagePath, simpleName)) return
        addViolation(node, "Import ${packagePath}.${simpleName} and write ${simpleName} for the ${what}".toString())
    }

    private void checkType(ClassNode type, ASTNode node, String what) {
        if (type == null) return
        if (type.array) {
            checkType(type.componentType, node, what)
            return
        }
        checkName(type.name, node, what)
        type.genericsTypes?.each { GenericsType gt ->
            if (gt.placeholder) return
            if (gt.wildcard) {
                gt.upperBounds?.each { checkType(it, node, what) }
                checkType(gt.lowerBound, node, what)
            } else {
                checkType(gt.type, node, what)
            }
        }
    }

    private void checkAnnotations(AnnotatedNode node) {
        node.annotations.each { checkType(it.classNode, it, 'annotation') }
    }

    private void checkParameters(Parameter[] parameters) {
        parameters?.each {
            checkType(it.type, it, 'parameter type')
            checkAnnotations(it)
        }
    }

    @Override
    protected void visitClassEx(ClassNode node) {
        checkAnnotations(node)
        // a script's superclass is the implicit groovy.lang.Script, and an annotation's
        // interface the implicit java.lang.annotation.Annotation: neither is written
        if (!node.script) checkType(node.unresolvedSuperClass, node, 'superclass')
        if (!node.annotationDefinition) node.unresolvedInterfaces?.each { checkType(it, node, 'interface') }
        super.visitClassEx(node)
    }

    @Override
    void visitField(FieldNode node) {
        checkType(node.type, node, 'field type')
        checkAnnotations(node)
        super.visitField(node)
    }

    @Override
    protected void visitConstructorOrMethod(MethodNode node, boolean isConstructor) {
        if (!isConstructor) checkType(node.returnType, node, 'return type')
        checkParameters(node.parameters)
        node.exceptions?.each { checkType(it, node, 'thrown type') }
        checkAnnotations(node)
        super.visitConstructorOrMethod(node, isConstructor)
    }

    @Override
    void visitDeclarationExpression(DeclarationExpression expression) {
        Expression left = expression.leftExpression
        if (isFirstVisit(expression) && left instanceof VariableExpression) {
            checkType(left.originType, expression, 'variable type')
        }
        super.visitDeclarationExpression(expression)
    }

    @Override
    void visitClosureExpression(ClosureExpression expression) {
        if (isFirstVisit(expression)) checkParameters(expression.parameters)
        super.visitClosureExpression(expression)
    }

    @Override
    void visitCatchStatement(CatchStatement statement) {
        if (isFirstVisit(statement)) checkType(statement.variable?.originType, statement, 'caught type')
        super.visitCatchStatement(statement)
    }

    @Override
    void visitCastExpression(CastExpression expression) {
        if (isFirstVisit(expression)) checkType(expression.type, expression, 'cast type')
        super.visitCastExpression(expression)
    }

    @Override
    void visitConstructorCallExpression(ConstructorCallExpression call) {
        if (isFirstVisit(call) && !call.usingAnonymousInnerClass) checkType(call.type, call, 'constructed type')
        super.visitConstructorCallExpression(call)
    }

    @Override
    void visitArrayExpression(ArrayExpression expression) {
        if (isFirstVisit(expression)) checkType(expression.elementType, expression, 'array type')
        super.visitArrayExpression(expression)
    }

    @Override
    void visitClassExpression(ClassExpression expression) {
        if (isFirstVisit(expression)) checkType(expression.type, expression, 'type')
        super.visitClassExpression(expression)
    }

    /** a static reference written as a property chain: java.nio.file.Files.exists(p) */
    @Override
    void visitPropertyExpression(PropertyExpression expression) {
        String property = expression.propertyAsString
        if (isFirstVisit(expression) && property && Character.isUpperCase(property.charAt(0))) {
            String path = expression.objectExpression.text
            if (QualifiedNames.PACKAGE_PATH.matcher(path).matches()) {
                checkName("${path}.${property}".toString(), expression, 'static reference')
            }
        }
        super.visitPropertyExpression(expression)
    }
}

// ---- Baselines ---------------------------------------------------------
//
// A file is grandfathered by its package-qualified name (the file name
// without extension, prefixed by its package), which identifies it exactly;
// the Gradle plugin hands CodeNarc bare file names, so path matching cannot.
// A gated rule skips a file on its baseline. Shrink a baseline as files are
// cleaned; never add to it for new code.

/** identifies a source file by package and name */
@CompileStatic
class GateBaseline {
    static String idOf(SourceCode sourceCode) {
        String pkg = sourceCode.ast?.packageName ?: ''   // ends with a dot when present
        String name = sourceCode.name
        int dot = name.lastIndexOf('.')
        pkg + (dot > 0 ? name.substring(0, dot) : name)
    }
}

@CompileStatic
class GateUnnecessaryFullyQualifiedNameRule extends UnnecessaryFullyQualifiedNameRule {
    Set<String> baseline = [] as Set

    @Override
    void applyTo(SourceCode sourceCode, List<Violation> violations) {
        if (!baseline.contains(GateBaseline.idOf(sourceCode))) super.applyTo(sourceCode, violations)
    }
}

@CompileStatic
class GateUnusedImportRule extends UnusedImportRule {
    Set<String> baseline = [] as Set

    @Override
    void applyTo(SourceCode sourceCode, List<Violation> violations) {
        if (!baseline.contains(GateBaseline.idOf(sourceCode))) super.applyTo(sourceCode, violations)
    }
}

def fullyQualifiedNameBaseline = [] as Set   // the tree is clean; keep it so

def unusedImportBaseline = [] as Set   // the tree is clean; keep it so

// Deliberate exceptions, not a baseline: user-guide snippets (src/spec/test,
// included by tag) that show a fully qualified name to the reader on purpose,
// because the import that would replace it lies outside the tagged region.
def documentationSnippets = [
    'DOMBuilderTest',
    'SaxBuilderTest',
    'StaxBuilderTest',
    'SyntaxTest',
    'TemplateEnginesTest',
    'UserGuideXmlSlurperTest',
    'builder.AntBuilderSpecTest',
    'gdk.WorkingWithCollectionsTest',
    'metaprogramming.MacroVariableSubstitutionTest',
    'groovy.csv.CsvBuilderTest',
    'groovy.toml.TomlParserTest',
    'groovy.yaml.YamlParserTest',
    'groovy.xml.UserGuideMarkupBuilderTest',
    'groovy.xml.UserGuideXmlParserTest',
] as Set

// Deliberate exceptions: groovydoc test fixtures whose qualified names are the
// input under test (a qualified superclass, an annotation written in full, an
// adapter linking two namesake classes).
def groovydocFixtures = [
    'org.codehaus.groovy.tools.groovydoc.testfiles.ExampleVisibilityG',
    'org.codehaus.groovy.tools.groovydoc.testfiles.a.DescendantD',
    'org.codehaus.groovy.tools.groovydoc.testfiles.alias.FooAdapter',
    'org.codehaus.groovy.tools.groovydoc.testfiles.anno.Groovy',
] as Set

ruleset {
    description 'The lint gate: rules that fail the build. Advisory rules live in codenarc.groovy.'

    rule(GateUnnecessaryFullyQualifiedNameRule) {
        baseline = fullyQualifiedNameBaseline + documentationSnippets + groovydocFixtures
    }

    rule(GateUnusedImportRule) {
        priority = 1
        baseline = unusedImportBaseline
    }
}
