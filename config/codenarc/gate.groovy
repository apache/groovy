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
    static final Pattern PACKAGE_PATH = ~/^[a-z][a-z0-9_]*(?:\.[a-z][a-z0-9_]*)+$/
    static final Pattern LINK_TAG = ~/\{@link(?:plain)?\s+([a-z][a-z0-9_]*(?:\.[a-z][a-z0-9_]*)+)\.([A-Z]\w*)/

    private final Map<String, String> importedByName = [:]
    private final Set<String> declaredInFile

    QualifiedNames(ModuleNode module) {
        module?.imports?.each { importedByName[it.alias] = it.className }
        declaredInFile = (module?.classes*.nameWithoutPackage ?: []) as Set
    }

    /**
     * Whether the qualification is required: the simple name is bound to another
     * type by an import or by a class declared in the same file. java.lang is
     * never checked, since def, implicit supertypes and the like surface as
     * java.lang types in the unresolved AST.
     */
    boolean isNeeded(String packagePath, String simpleName) {
        if (packagePath == 'java.lang') return true
        String bound = importedByName[simpleName]
        (bound != null && bound != "${packagePath}.${simpleName}".toString()) || declaredInFile.contains(simpleName)
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
        checkType(node.unresolvedSuperClass, node, 'superclass')
        node.unresolvedInterfaces?.each { checkType(it, node, 'interface') }
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

def fullyQualifiedNameBaseline = [
    'DOMBuilderTest',
    'SaxBuilderTest',
    'StaxBuilderTest',
    'TemplateEnginesTest',
    'UserGuideXmlSlurperTest',
    'builder.AntBuilderSpecTest',
    'groovy.DateTest',
    'groovy.SimpleTemplateEngineTest',
    'groovy.ant.AntTest',
    'groovy.bugs.Groovy5025Bug',
    'groovy.bugs.groovy4585.Groovy4585Bug',
    'groovy.cli.commons.CliBuilder',
    'groovy.cli.commons.package-info',
    'groovy.cli.picocli.CliBuilder',
    'groovy.cli.picocli.package-info',
    'groovy.console.ui.AstBrowser',
    'groovy.console.ui.Console',
    'groovy.console.ui.ConsoleActions',
    'groovy.console.ui.ConsolePreferences',
    'groovy.console.ui.ConsoleView',
    'groovy.console.ui.HistoryRecordGetTextToRunTests',
    'groovy.console.ui.ThemeManager',
    'groovy.console.ui.view.BasicContentPane',
    'groovy.console.ui.view.BasicMenuBar',
    'groovy.console.ui.view.BasicStatusBar',
    'groovy.console.ui.view.BasicToolBar',
    'groovy.console.ui.view.Defaults',
    'groovy.console.ui.view.GTKDefaults',
    'groovy.console.ui.view.MacOSXDefaults',
    'groovy.console.ui.view.MacOSXMenuBar',
    'groovy.console.ui.view.WindowsDefaults',
    'groovy.csv.CsvBuilderTest',
    'groovy.grape.ivy.GrapeIvy',
    'groovy.grape.ivy.StrictCachedGrapesResolverTest',
    'groovy.grape.maven.GrapeMaven',
    'groovy.http.HttpBuilderClientTest',
    'groovy.jmx.ImportModuleJmxTest',
    'groovy.jmx.builder.JmxBeanFactoryTest',
    'groovy.jmx.builder.JmxBeansFactoryTest',
    'groovy.jmx.builder.JmxBuilderTools',
    'groovy.jmx.builder.JmxEmbeddedMetaMapBuilderTest',
    'groovy.jmx.builder.JmxEmitterFactoryTest',
    'groovy.jmx.builder.JmxMetaMapBuilderTest',
    'groovy.jmx.builder.JmxTimerFactory',
    'groovy.json.DefaultJsonGeneratorTest',
    'groovy.json.JsonBuilderTest',
    'groovy.json.JsonSlurperClassicTest',
    'groovy.json.JsonSlurperMalformedStringTest',
    'groovy.json.StreamingJsonBuilderTest',
    'groovy.json.StringEscapeUtilsTest',
    'groovy.junit6.plugin.GroovyDisabledIf',
    'groovy.junit6.plugin.GroovyEnabledIf',
    'groovy.servlet.AbstractHttpServletTest',
    'groovy.servlet.ServletBindingTest',
    'groovy.sql.SqlHelperTestCase',
    'groovy.sql.SqlTest',
    'groovy.sql.SqlTestConstants',
    'groovy.swing.SwingBuilder',
    'groovy.swing.SwingBuilderBindingsTest',
    'groovy.swing.SwingBuilderTableTest',
    'groovy.swing.SwingBuilderTest',
    'groovy.swing.binding.JTableMetaMethods',
    'groovy.swing.factory.BoxLayoutFactory',
    'groovy.swing.factory.ColumnFactory',
    'groovy.swing.factory.ColumnModelFactory',
    'groovy.swing.factory.DialogFactory',
    'groovy.swing.factory.InternalFrameFactory',
    'groovy.swing.factory.LayoutFactory',
    'groovy.swing.factory.ScrollPaneFactory',
    'groovy.test.suite.ATestScriptThatsNoTestCase',
    'groovy.toml.TomlParserTest',
    'groovy.typecheckers.CombinerChecker',
    'groovy.typecheckers.CombinerCheckerTest',
    'groovy.typecheckers.FormatMethod',
    'groovy.typecheckers.FormatStringCheckerTest',
    'groovy.typecheckers.ModifiesChecker',
    'groovy.typecheckers.ModifiesCheckerTest',
    'groovy.typecheckers.MonadicShapeChecker',
    'groovy.typecheckers.MonadicShapeCheckerTest',
    'groovy.typecheckers.NullCheckerTest',
    'groovy.typecheckers.PurityCheckerTest',
    'groovy.typecheckers.RegexChecker',
    'groovy.typecheckers.RegexCheckerTest',
    'groovy.typecheckers.SqlInjectionCheckerTest',
    'groovy.typecheckers.package-info',
    'groovy.xml.GpathSyntaxTestSupport',
    'groovy.xml.MarkupWithWriterTest',
    'groovy.xml.MixedMarkupTestSupport',
    'groovy.xml.StaxBuilderTest',
    'groovy.xml.UseMarkupWithWriterScript',
    'groovy.xml.UserGuideMarkupBuilderTest',
    'groovy.xml.UserGuideXmlParserTest',
    'groovy.xml.XmlSecurityTest',
    'groovy.xml.script.AtomTestScript',
    'groovy.yaml.YamlParserTest',
    'org.apache.groovy.contracts.spock.SpockIntegrationTests',
    'org.apache.groovy.contracts.tests.post.OldVariablePostconditionTests',
    'org.apache.groovy.datetime.TimeCategoryTest',
    'org.apache.groovy.dateutil.TimeCategoryTest',
    'org.apache.groovy.dateutil.extensions.DateUtilExtensionsTest',
    'org.apache.groovy.docgenerator.GDKDocTool',
    'org.apache.groovy.docgenerator.JavaExtensionSourceSetTest',
    'org.apache.groovy.groovysh.commands.CompletionTest',
    'org.apache.groovy.groovysh.commands.DocTest',
    'org.apache.groovy.groovysh.commands.HelpFlagTest',
    'org.apache.groovy.groovysh.jline.GroovyCommands',
    'org.apache.groovy.nio.extensions.NioExtensionsTest',
    'org.apache.groovy.typecheckers.package-info',
    'org.codehaus.groovy.ant.GroovyTest2Class',
    'org.codehaus.groovy.ast.builder.AstBuilderFromCodeTest',
    'org.codehaus.groovy.ast.builder.WithAstBuilder',
    'org.codehaus.groovy.control.customizers.ASTTransformationCustomizerTest',
    'org.codehaus.groovy.macro.matcher.ASTMatcher',
    'org.codehaus.groovy.runtime.callsite.CachedMethodCallSitesTest',
    'org.codehaus.groovy.tools.groovydoc.GroovyDocToolTestSampleGroovy',
    'org.codehaus.groovy.tools.groovydoc.testfiles.ClassWithClosureInAnnotation',
    'org.codehaus.groovy.tools.groovydoc.testfiles.ClassWithSpockStyleAnnotations',
    'org.codehaus.groovy.tools.groovydoc.testfiles.ExampleVisibilityG',
    'org.codehaus.groovy.tools.groovydoc.testfiles.Script',
    'org.codehaus.groovy.tools.groovydoc.testfiles.ScriptWithMarkdownTopLevelDoc',
    'org.codehaus.groovy.tools.groovydoc.testfiles.ScriptWithOnlyMemberDoc',
    'org.codehaus.groovy.tools.groovydoc.testfiles.ScriptWithTopLevelDoc',
    'org.codehaus.groovy.tools.groovydoc.testfiles.a.DescendantD',
    'org.codehaus.groovy.tools.groovydoc.testfiles.alias.FooAdapter',
    'org.codehaus.groovy.tools.groovydoc.testfiles.anno.Groovy',
    'org.codehaus.groovy.tools.groovydoc.testfiles.generics.Groovy',
    'testable.MyTest',
    'testable.MyTestable',
    // core
    'ScriptAsUnitTest',
    'SyntaxTest',
    'TraitsSpecificationTest',
    'binarytrees',
    'bugs.CustomMetaClassTest',
    'bugs.Groovy10281',
    'bugs.Groovy10587',
    'bugs.Groovy11062',
    'bugs.Groovy12046',
    'bugs.Groovy12062',
    'bugs.Groovy12142',
    'bugs.Groovy12191',
    'bugs.Groovy2666',
    'bugs.Groovy4139Bug',
    'bugs.Groovy4720Bug',
    'bugs.Groovy4861Bug',
    'bugs.Groovy5239',
    'bugs.Groovy5359',
    'bugs.Groovy558_616_Bug',
    'bugs.Groovy596',
    'bugs.Groovy779_Bug',
    'bugs.Groovy8283',
    'bugs.Groovy8444',
    'bugs.Groovy9238',
    'bugs.Groovy9292',
    'bugs.Groovy9293',
    'bugs.Groovy9572',
    'bugs.Groovy9932',
    'bugs.POJOCallSiteBug',
    'bugs.groovy10121.SomeCollectedAnnotations',
    'bugs.scriptForGroovy1567',
    'bugs.scriptForGroovy3934',
    'fannkuch',
    'gdk.ConfigSlurperTest',
    'gdk.WorkingWithCollectionsTest',
    'gdk.WorkingWithIOSpecTest',
    'gls.annotations.ConstAnnotation',
    'gls.annotations.XmlEnum',
    'gls.annotations.XmlEnumValue',
    'gls.annotations.closures.AnnotationClosureTest',
    'gls.generics.GenericsJavaCompatibilityTest',
    'gls.invocation.CovariantReturnTest',
    'gls.statements.MultipleAssignmentDeclarationTest',
    'groovy.ArrayParamMethodTest',
    'groovy.BinaryStreamsTest',
    'groovy.EqualsTest',
    'groovy.GStringTest',
    'groovy.GroovyMethodsTest',
    'groovy.IllegalAccessTests',
    'groovy.ImportTest',
    'groovy.InstanceofFlowBindingsTest',
    'groovy.InstanceofScopeTest',
    'groovy.InstanceofTest',
    'groovy.NestedClassTest',
    'groovy.NewExpressionTest',
    'groovy.PropertyTest',
    'groovy.SqlDateTest',
    'groovy.StaticImportTest',
    'groovy.annotations.MyIntegerAnno',
    'groovy.beans.ListenerList',
    'groovy.beans.ListenerListASTTransformation',
    'groovy.benchmarks.createLoop',
    'groovy.cli.OptionField',
    'groovy.cli.UnparsedField',
    'groovy.cli.internal.CliBuilderInternal',
    'groovy.concurrent.AgentChangesTest',
    'groovy.concurrent.BroadcastChannelAsPublisherTest',
    'groovy.concurrent.ChannelCompositionTest',
    'groovy.concurrent.FlowPublisherAdapterTest',
    'groovy.lang.ClosureSerializationCycleTest',
    'groovy.lang.InterceptorTest',
    'groovy.lang.IntersectionCastE2ETest',
    'groovy.lang.IntersectionCoercionTest',
    'groovy.lang.MixinTest',
    'groovy.lang.ReferenceSerializationTest',
    'groovy.lang.ReferenceTest',
    'groovy.lang.WithMethodTest',
    'groovy.operator.BitwiseOperatorsTest',
    'groovy.operator.StringOperatorsTest',
    'groovy.script.scriptWithPackageStatement',
    'groovy.transform.AnnotationCollectorLegacyTest',
    'groovy.transform.AnnotationCollectorTest',
    'groovy.transform.AutoExternalize',
    'groovy.transform.Canonical',
    'groovy.transform.CompileDynamic',
    'groovy.transform.ConditionalInterrupt',
    'groovy.transform.Immutable',
    'groovy.transform.ImmutableProperties',
    'groovy.transform.ReadWriteLockTest',
    'groovy.transform.RecordType',
    'groovy.transform.ThreadInterrupt',
    'groovy.transform.ThreadInterruptTest',
    'groovy.transform.TimedInterrupt',
    'groovy.transform.stc.ClosuresSTCTest',
    'groovy.transform.stc.IOGMClosureParamTypeInferenceSTCTest',
    'groovy.transform.stc.IntersectionCastSTCTest',
    'groovy.transform.stc.LambdaTest',
    'groovy.transform.stc.ResourceGMClosureParamTypeInferenceSTCTest',
    'groovy.transform.stc.SocketGMClosureParamTypeInferenceSTCTest',
    'groovy.transform.stc.StringGMClosureParamTypeInferenceSTCTest',
    'groovy.ui.GroovyMainTest',
    'groovy.util.ConfigSlurper',
    'groovy.util.ConfigSlurperTest',
    'groovy.util.GroovyScriptEngineReloadingTest',
    'groovy.util.MiscScriptTest',
    'groovy.util.ObservableListTest',
    'groovy.util.ObservableSetTests',
    'groovy.util.ProxyGeneratorAdapterTest',
    'metaprogramming.ASTMatcherFilteringTest',
    'metaprogramming.ASTMatcherTestingTest',
    'metaprogramming.MacroClassTest',
    'metaprogramming.MacroExpressionTest',
    'metaprogramming.MacroStatementTest',
    'metaprogramming.MacroVariableSubstitutionTest',
    'metaprogramming.MyTransformToDebug',
    'org.apache.groovy.internal.runtime.invoke.InvokerFactoryTest',
    'org.apache.groovy.parser.antlr4.Groovy12173',
    'org.apache.groovy.parser.antlr4.internal.MissingDelimiterDiagnosticTest',
    'org.apache.groovy.runtime.indy.IndyInvalidationTest',
    'org.codehaus.groovy.ast.AnnotationNodeTest',
    'org.codehaus.groovy.ast.Groovy9871',
    'org.codehaus.groovy.ast.query.AstQueryTest',
    'org.codehaus.groovy.ast.tools.GenericsUtilsTest',
    'org.codehaus.groovy.classgen.ExtendedVerifierTest',
    'org.codehaus.groovy.classgen.Groovy12255',
    'org.codehaus.groovy.classgen.RecordTest',
    'org.codehaus.groovy.classgen.asm.PeepholeOptimizingMethodVisitorTest',
    'org.codehaus.groovy.classgen.asm.indy.IndyCompoundAssignTest',
    'org.codehaus.groovy.classgen.asm.sc.CompatWithASTXFormStaticCompileTest',
    'org.codehaus.groovy.classgen.asm.sc.CompileDynamicTest',
    'org.codehaus.groovy.classgen.asm.sc.ResourceGMClosureParamTypeInferenceStaticCompileTest',
    'org.codehaus.groovy.classgen.asm.sc.SocketGMClosureParamTypeInferenceStaticCompileTest',
    'org.codehaus.groovy.classgen.asm.sc.StaticCompilationTestSupport',
    'org.codehaus.groovy.classgen.genArrayAccess',
    'org.codehaus.groovy.classgen.genArrayUtil',
    'org.codehaus.groovy.classgen.genDgmMath',
    'org.codehaus.groovy.classgen.genMathModification',
    'org.codehaus.groovy.control.ClassNodeResolverTest',
    'org.codehaus.groovy.control.ClassWriterCommonSuperClassTest',
    'org.codehaus.groovy.control.customizers.ASTTransformationCustomizer',
    'org.codehaus.groovy.control.customizers.SecureASTCustomizerTest',
    'org.codehaus.groovy.control.customizers.SourceAwareCustomizerTest',
    'org.codehaus.groovy.reflection.ClassInfoSoftModeStressProbe',
    'org.codehaus.groovy.reflection.ReflectionUtilsTest',
    'org.codehaus.groovy.reflection.utils.ReflectionUtilsTest',
    'org.codehaus.groovy.runtime.DefaultGroovyMethodsTest',
    'org.codehaus.groovy.runtime.DefaultGroovyStaticMethodsTest',
    'org.codehaus.groovy.runtime.NumberAwareComparatorTest',
    'org.codehaus.groovy.runtime.PackedClosureMetaClassTest',
    'org.codehaus.groovy.runtime.m12n.ExtensionModuleTest',
    'org.codehaus.groovy.runtime.powerassert.AssertionRenderingTest',
    'org.codehaus.groovy.runtime.powerassert.AssertionsInDifferentLocationsTest',
    'org.codehaus.groovy.runtime.powerassert.EvaluationTest',
    'org.codehaus.groovy.runtime.powerassert.ImplicitClosureCallRenderingTest',
    'org.codehaus.groovy.runtime.powerassert.ScriptEvaluationTest',
    'org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformationTest',
    'org.codehaus.groovy.syntax.TokenTest',
    'org.codehaus.groovy.tools.stubgenerator.AnnotationCollectorStubTest',
    'org.codehaus.groovy.tools.stubgenerator.AutoCloneHashMapJointCompilationStubTest',
    'org.codehaus.groovy.tools.stubgenerator.BuilderJointCompilationStubTest',
    'org.codehaus.groovy.tools.stubgenerator.DelegateJointCompilationStubTest',
    'org.codehaus.groovy.tools.stubgenerator.ListenerListJointCompilationStubTest',
    'org.codehaus.groovy.transform.ASTTestTransformation',
    'org.codehaus.groovy.transform.AsyncTransformHelperTest',
    'org.codehaus.groovy.transform.AutoFinalTransformTest',
    'org.codehaus.groovy.transform.DelegateTransformTest',
    'org.codehaus.groovy.transform.PackedClosuresTransformTest',
    'org.codehaus.groovy.transform.classloading.TransformsAndCustomClassLoadersTest',
    'org.codehaus.groovy.transform.stc.ClassTagExtensionModuleTest',
    'org.codehaus.groovy.transform.traitx.TraitASTTransformationTest',
    'org.codehaus.groovy.transform.traitx.TraitWithClosureOrLambda',
    'org.codehaus.groovy.util.ReferenceManagerTest',
    'org.codehaus.groovy.vmplugin.v8.IndyScopedSwitchPointTest',
    'org.codehaus.groovy.vmplugin.v8.PluginDefaultGroovyMethodsTest',
    'org.codehaus.groovy.vmplugin.v9.ClassFinderTest',
    'partialsums',
    'rayTracer',
    'recursive',
    'script0',
    'script1',
    'script120',
    'script240',
    'script30',
    'script300',
    'script300WithCategory',
    'script60',
    'scriptArgs',
    'scriptHelloWorld',
    'scriptHelloWorld2',
    'scriptMethodReflection',
    'scriptThatCallsAnother',
    'scriptWithClass',
    'scriptWithClosure',
    'scriptWithEval',
    'spectralnorm',
] as Set

def unusedImportBaseline = [] as Set   // the tree is clean; keep it so

ruleset {
    description 'The lint gate: rules that fail the build. Advisory rules live in codenarc.groovy.'

    rule(GateUnnecessaryFullyQualifiedNameRule) {
        baseline = fullyQualifiedNameBaseline
    }

    rule(GateUnusedImportRule) {
        priority = 1
        baseline = unusedImportBaseline
    }
}
