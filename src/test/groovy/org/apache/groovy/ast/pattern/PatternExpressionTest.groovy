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
import org.apache.groovy.ast.pattern.MatchPattern.ConstantPattern
import org.apache.groovy.ast.pattern.MatchPattern.ListPattern
import org.apache.groovy.ast.pattern.MatchPattern.MapPattern
import org.apache.groovy.ast.pattern.MatchPattern.RecordPattern
import org.apache.groovy.ast.pattern.MatchPattern.RestPattern
import org.apache.groovy.ast.pattern.MatchPattern.TypePattern
import org.apache.groovy.ast.pattern.MatchPattern.WildcardPattern
import org.codehaus.groovy.GroovyBugError
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.CodeVisitorSupport
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.NotExpression
import org.codehaus.groovy.ast.expr.PatternExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.CaseStatement
import org.codehaus.groovy.ast.stmt.IfStatement
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

import static groovy.test.GroovyAssert.shouldFail
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
 * {@link MatchPattern} stays on the AST from parsing to code generation, the
 * matching code is derived from it at the start of instruction selection, and
 * an AST built programmatically gets the same lowering as parsed source.
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
     * Replaces the body of {@code methodName} in {@code className} at the given phase.
     */
    private static CompilationCustomizer replacingBody(final String className, final String methodName, final Closure<Statement> body,
                                                       final CompilePhase phase = CompilePhase.CONVERSION, final boolean lowering = false) {
        new CompilationCustomizer(phase) {
            @Override
            void call(final SourceUnit source, final GeneratorContext context, final ClassNode classNode) {
                if (classNode.nameWithoutPackage == className) {
                    classNode.getMethods(methodName)[0].code = body()
                    if (lowering) PatternLowering.lower(classNode, source)
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

    @Test
    void testATransformAfterLoweringLowersWhatItCreates() {
        // from instruction selection on, the lowering has run; a transform that creates
        // a pattern then lowers (and scopes) the class it changed itself
        runWith(replacingBody('Describer', 'describe', this.&describeBody, CompilePhase.INSTRUCTION_SELECTION, true), String.format(DESCRIBER, ''))

        def err = shouldFail(GroovyBugError) {
            runWith(replacingBody('Describer', 'describe', this.&describeBody, CompilePhase.INSTRUCTION_SELECTION, false), String.format(DESCRIBER, ''))
        }
        assert err.message.contains('was not lowered before class generation')
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

    @Test
    void testATransformBeforeLoweringCanReplaceThePattern() {
        // there is no lowered code to keep in step with until instruction selection:
        // whatever pattern a label holds by then is what gets lowered
        def customizer = new CompilationCustomizer(CompilePhase.CANONICALIZATION) {
            @Override
            void call(final SourceUnit source, final GeneratorContext context, final ClassNode classNode) {
                if (classNode.nameWithoutPackage != 'Describer') return
                classNode.getMethods('describe')[0].code.visit(new CodeVisitorSupport() {
                    @Override
                    void visitSwitch(final SwitchStatement statement) {
                        for (CaseStatement arm : statement.caseStatements) {
                            if (arm.expression instanceof PatternExpression label && label.pattern instanceof TypePattern) {
                                assert label.lowered == null
                                arm.expression = new PatternExpression(new TypePattern(ClassHelper.Integer_TYPE, 's'), null)
                            }
                        }
                        super.visitSwitch(statement)
                    }
                })
            }
        }
        runWith(customizer, '''
            class Describer {
                static Object describe(Object o) {
                    switch (o) {
                        case String s -> return "matched $s"
                        default       -> return 'other'
                    }
                }
            }
            assert Describer.describe(42) == 'matched 42'
            assert Describer.describe('abc') == 'other'
        ''')
    }

    @Test
    void testAnExpressionTransformerRebuildsThePattern() {
        // a transform that rebuilds every expression keeps the pattern, and reaches the
        // literal elements written in it
        def customizer = new CompilationCustomizer(CompilePhase.CANONICALIZATION) {
            @Override
            void call(final SourceUnit source, final GeneratorContext context, final ClassNode classNode) {
                if (classNode.nameWithoutPackage != 'Describer') return
                new ClassCodeExpressionTransformer() {
                    @Override
                    protected SourceUnit getSourceUnit() { source }

                    @Override
                    Expression transform(final Expression expression) {
                        if (expression instanceof ConstantExpression && expression.value == 1) return constX(2)
                        super.transform(expression)
                    }
                }.visitClass(classNode)
            }
        }
        runWith(customizer, '''
            class Describer {
                static Object describe(Object o) {
                    switch (o) {
                        case [1, var x] -> return "pair $x"
                        default         -> return 'other'
                    }
                }
            }
            assert Describer.describe([2, 'b']) == 'pair b' // the literal 1 of the pattern became 2
            assert Describer.describe([1, 'b']) == 'other'
        ''')
    }

    private static MethodNode methodOf(final String source, final int phase) {
        def unit = new CompilationUnit()
        unit.addSource('Probe.groovy', source)
        unit.compile(phase)
        unit.AST.classes.find { it.nameWithoutPackage == 'Probe' }.getMethods('m')[0]
    }

    private static CaseStatement firstPatternArm(final String source, final int phase) {
        CaseStatement found = null
        methodOf(source, phase).code.visit(new CodeVisitorSupport() {
            @Override
            void visitSwitch(final SwitchStatement statement) {
                found = found ?: statement.caseStatements.find { it.expression instanceof PatternExpression }
                super.visitSwitch(statement)
            }
        })
        found
    }

    private static PatternExpression firstPatternLabel(final String source, final int phase) {
        (PatternExpression) firstPatternArm(source, phase).expression
    }

    private static IfStatement firstIf(final String source, final int phase) {
        IfStatement found = null
        methodOf(source, phase).code.visit(new CodeVisitorSupport() {
            @Override
            void visitIfElse(final IfStatement statement) {
                found = found ?: statement
                super.visitIfElse(statement)
            }
        })
        found
    }

    /** Collects, by name, the variables that the references in {@code node} resolve to. */
    private static Map<String, List<Object>> referencesIn(final Object node) {
        def references = [:].withDefault { [] }
        node.visit(new CodeVisitorSupport() {
            @Override
            void visitVariableExpression(final VariableExpression variable) {
                if (!variable.accessedVariable.is(variable)) references[variable.name] << variable.accessedVariable
            }
        })
        references
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

    private static final String INSTANCEOF_PROBE = '''
        class Probe {
            def m(o) {
                if (%s) return 1 else return 0
            }
        }
    '''

    @Test
    void testParsedLabelKeepsThePattern() {
        def label = firstPatternLabel(PROBE, Phases.CONVERSION)
        assert label.pattern instanceof RecordPattern
        def components = ((RecordPattern) label.pattern).components()
        assert components[0] instanceof BindingPattern && ((BindingPattern) components[0]).name() == 'x'
        assert components[1] instanceof TypePattern && ((TypePattern) components[1]).name() == 'y'
        assert label.bindings*.name == ['x', 'y']
        assert label.hasGuard()
        assert label.lowered == null && label.subject == null // not lowered before instruction selection

        label = firstPatternLabel(PROBE, Phases.CANONICALIZATION)
        assert label.lowered == null && label.subject == null // transforms see the pattern only

        label = firstPatternLabel(PROBE, Phases.INSTRUCTION_SELECTION)
        assert label.lowered != null && label.subject != null
        assert label.pattern instanceof RecordPattern // still there once lowered
    }

    @Test
    void testReferencesResolveToThePatternVariablesBeforeLowering() {
        // the pattern declares its variables, so scoping binds the guard and the arm
        // to them without any lowered code
        def arm = firstPatternArm(PROBE, Phases.CANONICALIZATION)
        def label = (PatternExpression) arm.expression
        def (x, y) = label.bindings
        def references = referencesIn(label.guard) + referencesIn(arm.code)
        assert references['x'] && references['x'].every { it.is(x) }
        assert references['y'] && references['y'].every { it.is(y) }

        // and the same for the then-branch of an instanceof pattern
        def ifs = firstIf(String.format(INSTANCEOF_PROBE, 'o instanceof String s && s.size() > 1'), Phases.CANONICALIZATION)
        def condition = (BinaryExpression) ifs.booleanExpression.expression
        def s = ((PatternExpression) condition.leftExpression).bindings[0]
        def inCondition = referencesIn(condition.rightExpression)
        assert inCondition['s'] && inCondition['s'].every { it.is(s) }
    }

    @Test
    void testInstanceofWithABindingIsAPattern() {
        def typePattern = firstIf(String.format(INSTANCEOF_PROBE, 'o instanceof String s'), Phases.CONVERSION).booleanExpression.expression
        assert typePattern instanceof PatternExpression
        assert typePattern.instanceOf && typePattern.pattern instanceof TypePattern
        assert typePattern.text == 'o instanceof String s'

        // a plain type test is not a pattern
        assert firstIf(String.format(INSTANCEOF_PROBE, 'o instanceof String'), Phases.CONVERSION).booleanExpression.expression instanceof BinaryExpression

        def negated = firstIf(String.format(INSTANCEOF_PROBE, 'o !instanceof String s'), Phases.CONVERSION).booleanExpression.expression
        assert negated instanceof NotExpression && negated.expression instanceof PatternExpression

        // from instruction selection on, the JEP 394 shape of Groovy 6
        def lowered = firstIf(String.format(INSTANCEOF_PROBE, 'o instanceof String s'), Phases.INSTRUCTION_SELECTION).booleanExpression.expression
        assert lowered instanceof PatternExpression && lowered.lowered instanceof BinaryExpression
        assert lowered.lowered.rightExpression instanceof DeclarationExpression
        assert lowered.lowered.rightExpression.variableExpression.is(lowered.bindings[0])
    }

    @Test
    void testTotalityIsDecidedByThePattern() {
        // after semantic analysis the record type is resolved, so totality can be asked
        def label = firstPatternLabel(PROBE, Phases.SEMANTIC_ANALYSIS)
        def point = (RecordPattern) label.pattern
        assert point.isRecord()
        assert point.isTotal() // var x, and int y on a primitive component
        assert point.coverageType().nameWithoutPackage == 'Point'

        assert new ListPattern([new RestPattern(null, 'xs')]).isTotal()
        assert !new ListPattern([new RestPattern(ClassHelper.Integer_TYPE, 'xs')]).isTotal()
        assert new TypePattern(ClassHelper.int_TYPE, 'i').coverageType() == ClassHelper.Integer_TYPE
    }

    @Test
    void testPatternText() {
        def point = new RecordPattern(ClassHelper.make('Point'), [new BindingPattern('x'), new TypePattern(ClassHelper.int_TYPE, 'y'), new WildcardPattern()])
        assert point.text == 'Point(var x, int y, _)'
        assert new ListPattern([new ConstantPattern(constX(1)), point, new RestPattern(null, 'rest')]).text == '[1, Point(var x, int y, _), var... rest]'
        assert new ListPattern([new RestPattern(ClassHelper.Integer_TYPE, 'ints')]).text == '[java.lang.Integer... ints]'
        assert new ListPattern([new RestPattern(null, (String) null)]).text == '[...]'
        assert new MapPattern([new MapPattern.Entry('name', new BindingPattern('n')), new MapPattern.Entry('age', new WildcardPattern())], new RestPattern(null, (String) null)).text == '[name: var n, age: _, ...]'
        assert new MapPattern([], null).text == '[:]'
        assert new PatternExpression(point, gtX(varX('x'), constX(0))).text == 'Point(var x, int y, _) when (x > 0)'
        assert PatternExpression.instanceOf(varX('o'), point).text == 'o instanceof Point(var x, int y, _)'
    }

    @Test
    void testInstanceofBindsTheWrapperOfAPrimitiveTypePattern() {
        def point = new RecordPattern(ClassHelper.make('Point'), [new TypePattern(ClassHelper.int_TYPE, 'x'), new BindingPattern('y')])
        assert point.bindings()[0].originType == ClassHelper.int_TYPE // a switch arm binds the primitive
        def match = PatternExpression.instanceOf(varX('o'), point)
        assert match.bindings[0].originType == ClassHelper.Integer_TYPE // instanceof binds the tested object
        assert match.bindings[1].is(point.bindings()[1]) // other variables are shared
    }
}
