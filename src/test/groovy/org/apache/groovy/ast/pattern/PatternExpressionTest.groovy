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
package org.apache.groovy.ast.pattern

import org.apache.groovy.ast.pattern.MatchPattern.BindingPattern
import org.apache.groovy.ast.pattern.MatchPattern.ListPattern
import org.apache.groovy.ast.pattern.MatchPattern.RecordPattern
import org.apache.groovy.ast.pattern.MatchPattern.TypePattern
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.CodeVisitorSupport
import org.codehaus.groovy.ast.expr.PatternExpression
import org.codehaus.groovy.ast.stmt.CaseStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.ast.stmt.SwitchStatement
import org.codehaus.groovy.classgen.GeneratorContext
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.customizers.CompilationCustomizer
import org.junit.jupiter.api.Test

import static org.codehaus.groovy.ast.tools.GeneralUtils.block
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX
import static org.codehaus.groovy.ast.tools.GeneralUtils.gtX
import static org.codehaus.groovy.ast.tools.GeneralUtils.ifElseS
import static org.codehaus.groovy.ast.tools.GeneralUtils.plusX
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX

/**
 * The pattern representation (GEP-19): a {@link PatternExpression} holding a
 * {@link MatchPattern} stays on the AST from parsing to code generation, and an
 * AST built programmatically gets the same lowering as parsed source.
 */
final class PatternExpressionTest {

    private static final String DESCRIBER = '''
        record Point(int x, int y) {}
        %s
        class Describer {
            static Object describe(Object o) { null } // body supplied by the customizer
        }
        assert Describer.describe('abc') == 'ABC'
        assert Describer.describe(new Point(1, 2)) == 3
        assert Describer.describe(new Point(-1, 2)) == 'other'
        assert Describer.describe(42) == 'other'
    '''

    /**
     * Replaces the body of {@code methodName} in {@code className}, before semantic analysis.
     */
    private static CompilationCustomizer replacingBody(final String className, final String methodName, final Closure<Statement> body) {
        new CompilationCustomizer(CompilePhase.CONVERSION) {
            @Override
            void call(final SourceUnit source, final GeneratorContext context, final ClassNode classNode) {
                if (classNode.nameWithoutPackage == className) {
                    classNode.getMethods(methodName)[0].code = body()
                }
            }
        }
    }

    private static void runWith(final CompilationCustomizer customizer, final String script) {
        def config = new CompilerConfiguration()
        config.addCompilationCustomizers(customizer)
        new GroovyShell(config).evaluate(script)
    }

    // switch (o) {
    //     case String s -> return s.toUpperCase()
    //     case Point(var x, var y) when x > 0 -> return x + y
    //     default -> return 'other'
    // }
    private static Statement describeBody() {
        def stringArm = new CaseStatement(new PatternExpression(new TypePattern(ClassHelper.STRING_TYPE, 's'), null),
                returnS(callX(varX('s'), 'toUpperCase')))
        def pointArm = new CaseStatement(new PatternExpression(
                new RecordPattern(ClassHelper.make('Point'), [new BindingPattern('x'), new BindingPattern('y')]),
                gtX(varX('x'), constX(0))),
                returnS(plusX(varX('x'), varX('y'))))
        [stringArm, pointArm]*.arrow = true
        block(new SwitchStatement(varX('o'), [stringArm, pointArm], returnS(constX('other'))))
    }

    @Test
    void testProgrammaticPatternSwitch() {
        runWith(replacingBody('Describer', 'describe', this.&describeBody), String.format(DESCRIBER, ''))
    }

    @Test
    void testProgrammaticPatternSwitchCompileStatic() {
        runWith(replacingBody('Describer', 'describe', this.&describeBody), String.format(DESCRIBER, '@groovy.transform.CompileStatic'))
    }

    // if (o instanceof Point(var x, int y)) return x + y else return -1
    private static Statement sumBody() {
        def match = PatternExpression.instanceOf(varX('o'),
                new RecordPattern(ClassHelper.make('Point'), [new BindingPattern('x'), new TypePattern(ClassHelper.int_TYPE, 'y')]))
        block(ifElseS(match, returnS(plusX(varX('x'), varX('y'))), returnS(constX(-1))))
    }

    @Test
    void testProgrammaticInstanceofPattern() {
        ['', '@groovy.transform.CompileStatic'].each { mode ->
            runWith(replacingBody('Summer', 'sum', this.&sumBody), """
                record Point(int x, int y) {}
                $mode
                class Summer {
                    static Object sum(Object o) { null } // body supplied by the customizer
                }
                assert Summer.sum(new Point(1, 2)) == 3
                assert Summer.sum('abc') == -1
            """)
        }
    }

    private static PatternExpression firstPatternLabel(final String source, final int phase) {
        def unit = new CompilationUnit()
        unit.addSource('Probe.groovy', source)
        unit.compile(phase)
        PatternExpression found = null
        def method = unit.AST.classes.find { it.nameWithoutPackage == 'Probe' }.getMethods('m')[0]
        method.code.visit(new CodeVisitorSupport() {
            @Override
            void visitSwitch(final SwitchStatement statement) {
                found = found ?: statement.caseStatements*.expression.find { it instanceof PatternExpression }
                super.visitSwitch(statement)
            }
        })
        found
    }

    private static final String PROBE = '''
        record Point(int x, int y) {}
        class Probe {
            def m(o) {
                switch (o) {
                    case Point(var x, int y) when x > 0 -> x + y
                    default -> 0
                }
            }
        }
    '''

    @Test
    void testParsedLabelKeepsThePattern() {
        def label = firstPatternLabel(PROBE, Phases.CONVERSION)
        assert label.pattern instanceof RecordPattern
        def components = ((RecordPattern) label.pattern).components()
        assert components[0] == new BindingPattern('x')
        assert components[1] instanceof TypePattern && ((TypePattern) components[1]).name() == 'y'
        assert label.hasGuard()
        assert label.lowered == null && label.subject == null // not lowered before semantic analysis

        label = firstPatternLabel(PROBE, Phases.SEMANTIC_ANALYSIS)
        assert label.lowered != null && label.subject != null
        assert label.pattern instanceof RecordPattern // still there once lowered
    }

    @Test
    void testTotalityIsDecidedByThePattern() {
        // after semantic analysis the record type is resolved, so totality can be asked
        def label = firstPatternLabel(PROBE, Phases.SEMANTIC_ANALYSIS)
        def point = (RecordPattern) label.pattern
        assert point.isRecord()
        assert point.isTotal() // var x, and int y on a primitive component
        assert point.coverageType().nameWithoutPackage == 'Point'

        assert new ListPattern([new MatchPattern.RestPattern(null, 'xs')]).isTotal()
        assert !new ListPattern([new MatchPattern.RestPattern(ClassHelper.Integer_TYPE, 'xs')]).isTotal()
        assert new TypePattern(ClassHelper.int_TYPE, 'i').coverageType() == ClassHelper.Integer_TYPE
    }
}
