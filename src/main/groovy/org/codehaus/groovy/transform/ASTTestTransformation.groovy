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
package org.codehaus.groovy.transform

import groovy.transform.CompilationUnitAware
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassCodeVisitorSupport
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Janitor
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.CompilationUnit.ISourceUnitOperation
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.runtime.MethodClosure
import org.codehaus.groovy.syntax.SyntaxException

import static org.codehaus.groovy.ast.tools.GeneralUtils.classX
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX
import static org.codehaus.groovy.control.CompilePhase.fromPhaseNumber as toCompilePhase

/**
 * Implements {@link groovy.transform.ASTTest} by scheduling the supplied test closure across compilation phases.
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
class ASTTestTransformation implements ASTTransformation, CompilationUnitAware {

    /**
     * Compilation unit that owns the current transformation.
     */
    CompilationUnit compilationUnit

    /**
     * Validates the annotation and schedules execution of its AST assertions.
     *
     * @param nodes the annotation node and annotated AST node
     * @param source the source unit containing the annotation
     */
    @Override
    void visit(final ASTNode[] nodes, final SourceUnit source) {
        AnnotationNode annotationNode = nodes[0]

        def member = annotationNode.getMember('phase')
        CompilePhase phase = null
        if (member) {
            if (member instanceof VariableExpression) {
                phase = CompilePhase.valueOf(member.text)
            } else if (member instanceof PropertyExpression) {
                phase = CompilePhase.valueOf(member.propertyAsString)
            }
            annotationNode.setMember('phase', propX(classX(ClassHelper.make(CompilePhase)), phase.toString()))

            if (phase.phaseNumber < compilationUnit.phase) {
                throw new SyntaxException('ASTTest phase must be at least ' + toCompilePhase(compilationUnit.phase), member)
            }
        }

        member = annotationNode.getMember('value')
        if (member && !(member instanceof ClosureExpression)) {
            throw new SyntaxException('ASTTest value must be a closure', member)
        }
        if (!member && !annotationNode.getNodeMetaData(ASTTestTransformation)) {
            throw new SyntaxException('Missing test expression', annotationNode)
        }

        // convert value into node metadata so that the expression doesn't mix up with other AST xforms like STC
        annotationNode.setNodeMetaData(ASTTestTransformation, member)
        annotationNode.setMember('value', new ClosureExpression(
            Parameter.EMPTY_ARRAY, EmptyStatement.INSTANCE))
        member.variableScope?.@parent = null

        ISourceUnitOperation astTester = new ASTTester(astNode: nodes[1], sourceUnit: source, testClosure: annotationNode.getNodeMetaData(ASTTestTransformation))
        for (int p = (phase ?: CompilePhase.SEMANTIC_ANALYSIS).phaseNumber, q = (phase ?: CompilePhase.FINALIZATION).phaseNumber; p <= q; p += 1) {
            compilationUnit.addNewPhaseOperation(astTester, p)
        }
    }

    //--------------------------------------------------------------------------

    private class ASTTester implements ISourceUnitOperation {

        /**
         * AST node being exposed to the test closure.
         */
        ASTNode astNode
        /**
         * Source unit that declared the annotated node.
         */
        SourceUnit sourceUnit
        /**
         * Closure expression that contains the AST assertions.
         */
        ClosureExpression testClosure
        private final Binding binding = new Binding([:].withDefault { null })

        /**
         * Runs the AST test for the matching source unit.
         *
         * @param unit the source unit currently being processed
         */
        @Override
        void call(final SourceUnit unit) {
            if (unit == sourceUnit) {
                test()
            }
        }

        private void test() {
            def sb = new StringBuilder()
            for (int i = testClosure.lineNumber, n = testClosure.lastLineNumber; i <= n; i += 1) {
                sb.append(sourceUnit.source.getLine(i, new Janitor())).append('\n')
            }
            sb = sb[testClosure.columnNumber..<sb.length()]
            String testSource = sb[0..<sb.lastIndexOf('}')]

            def compilationUnit = ASTTestTransformation.this.@compilationUnit

            binding['node'] = astNode
            binding['sourceUnit'] = sourceUnit
            binding['compilationUnit'] = compilationUnit
            binding['compilePhase'] = toCompilePhase(compilationUnit.phase)
            binding['lookup'] = new MethodClosure(LabelFinder, 'lookup').curry(astNode)

            def customizer = new ImportCustomizer()
            sourceUnit.AST.imports.each {
                customizer.addImport(it.alias, it.type.name)
            }
            sourceUnit.AST.starImports.each {
                customizer.addStarImports(it.packageName)
            }
            sourceUnit.AST.staticImports.each {
                customizer.addStaticImport(it.value.alias, it.value.type.name, it.value.fieldName)
            }
            sourceUnit.AST.staticStarImports.each {
                customizer.addStaticStars(it.value.className)
            }

            def config = new CompilerConfiguration()
            config.addCompilationCustomizers(customizer)
            def loader = compilationUnit.transformLoader
            new GroovyShell(loader, binding, config).evaluate(testSource)
        }
    }

    private static class LabelFinder extends ClassCodeVisitorSupport {

        /**
         * Finds labeled statements inside the supplied method.
         *
         * @param node the method to inspect
         * @param label the statement label to match
         * @return matching labeled statements
         */
        static List<Statement> lookup(final MethodNode node, final String label) {
            LabelFinder finder = new LabelFinder(label, null)
            node.code.visit(finder)

            finder.targets
        }

        /**
         * Finds labeled statements inside the supplied class.
         *
         * @param node the class whose methods and constructors should be inspected
         * @param label the statement label to match
         * @return matching labeled statements
         */
        static List<Statement> lookup(final ClassNode node, final String label) {
            LabelFinder finder = new LabelFinder(label, null)
            node.methods*.code*.visit(finder)
            node.declaredConstructors*.code*.visit(finder)

            finder.targets
        }

        private final String label
        private final SourceUnit unit
        private final List<Statement> targets = [] as LinkedList

        /**
         * Creates a finder for statements carrying the supplied label.
         *
         * @param label the label to search for
         * @param unit the source unit reported to the visitor infrastructure
         */
        LabelFinder(final String label, final SourceUnit unit) {
            this.label = label
            this.unit = unit
        }

        /**
         * Returns the source unit associated with this visitor.
         *
         * @return the associated source unit, or {@code null}
         */
        @Override
        protected SourceUnit getSourceUnit() {
            unit
        }

        /**
         * Records statements whose labels match the requested target label.
         *
         * @param statement the statement currently being visited
         */
        @Override
        protected void visitStatement(final Statement statement) {
            super.visitStatement(statement)
            if (label in statement.statementLabels) targets << statement
        }

        /**
         * Returns the statements collected for the configured label.
         *
         * @return matching labeled statements
         */
        List<Statement> getTargets() {
            Collections.unmodifiableList(targets)
        }
    }
}
