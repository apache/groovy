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
package bugs

import groovy.lang.MissingPropertyException
import org.codehaus.groovy.ast.ClassCodeVisitorSupport
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.EmptyExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.AssertStatement
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.codehaus.groovy.classgen.GeneratorContext
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.ErrorCollector
import org.codehaus.groovy.control.PlaceholderVisitor
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.customizers.CompilationCustomizer
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail
import static org.codehaus.groovy.ast.tools.GeneralUtils.boolX
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX
import static org.codehaus.groovy.ast.tools.GeneralUtils.declX
import static org.codehaus.groovy.ast.tools.GeneralUtils.eqX
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX
import static org.codehaus.groovy.control.CompilePhase.INSTRUCTION_SELECTION

/**
 * GROOVY-11744: a repeated {@code _} is an unnamed placeholder. It must not
 * replace a real {@code _} that is already in scope, including through a
 * multi-assignment or a closure/lambda parameter. A lone {@code _} stays a
 * normal variable. With no real {@code _} in scope, the placeholders do not
 * introduce one.
 */
final class Groovy11744 {

    @Test
    void testMultiAssignDoesNotOverwritePriorUnderscore() {
        assertScript '''
            def _ = 1
            def (_, _) = [2, 3]
            assert _ == 1
        '''
    }

    @Test
    void testMultiAssignKeepsNamedSlotsAndPriorUnderscore() {
        assertScript '''
            def _ = 1
            def (_, y, _) = [3, 4, 5]
            assert y == 4
            assert _ == 1
        '''
    }

    @Test
    void testMultiAssignInsideClosureDoesNotOverwritePriorUnderscore() {
        assertScript '''
            def _ = 1
            def c = { ->
                def (_, _) = [2, 3]
                _
            }
            assert c() == 1
        '''
    }

    @Test
    void testMultiAssignDoesNotShadowField() {
        assertScript '''
            class C {
                def _ = 1
                def m() {
                    def (_, y, _) = [3, 4, 5]
                    assert y == 4
                    assert _ == 1
                }
            }
            new C().m()
        '''
    }

    @Test
    void testCompileStaticMultiAssignDoesNotOverwritePriorUnderscore() {
        assertScript '''
            @groovy.transform.CompileStatic
            def m() {
                def _ = 1
                def (_, y, _) = [3, 4, 5]
                assert y == 4
                assert _ == 1
            }
            m()
        '''
    }

    @Test
    void testLambdaParameterDoesNotOverwritePriorUnderscore() {
        assertScript '''
            def _ = 1
            def x = (_, _) -> _
            assert x(2, 3) == 1
        '''
    }

    @Test
    void testClosureParameterDoesNotOverwritePriorUnderscore() {
        assertScript '''
            def _ = 1
            def x = { _, _ -> _ }
            assert x(2, 3) == 1
        '''
    }

    @Test
    void testLambdaCanReadPriorUnderscoreAlongsideNamedParams() {
        assertScript '''
            def _ = 9
            def g = (_, a, _, b) -> a + b + _
            assert g(1000, 100, 10, 1) == 110
        '''
    }

    @Test
    void testClosureCanReadPriorUnderscoreAlongsideNamedParams() {
        assertScript '''
            def _ = 9
            def g = { _, a, _, b -> a + b + _ }
            assert g(1000, 100, 10, 1) == 110
        '''
    }

    @Test
    void testCompileStaticLambdaDoesNotOverwritePriorUnderscore() {
        assertScript '''
            import java.util.function.BiFunction

            @groovy.transform.CompileStatic
            def m() {
                int _ = 1
                BiFunction<Integer, Integer, Integer> f = (_, _) -> _
                assert f.apply(2, 3) == 1
            }
            m()
        '''
    }

    @Test
    void testUnnamedMultiAssignDoesNotIntroduceUnderscore() {
        assertScript '''
            def (_, y, _) = [3, 4, 5]
            assert y == 4
        '''
        shouldFail(MissingPropertyException, '''
            def (_, y, _) = [3, 4, 5]
            _
        ''')
    }

    @Test
    void testClosureAssignsPriorUnderscoreAfterUnnamedDeclaration() {
        assertScript '''
            def _ = 1
            def c = { ->
                def (_, _) = [2, 3]
                _ = 8
            }
            c()
            assert _ == 8
        '''
    }

    @Test
    void testSingleUnderscoreStillBinds() {
        assertScript '''
            def (_, y) = [2, 3]
            assert _ == 2
            assert y == 3
        '''
    }

    @Test
    void testAssignmentToRealUnderscoreStillWrites() {
        assertScript '''
            def _ = 1
            def y
            (_, y, _) = [3, 4, 5]
            assert y == 4
            assert _ == 5
        '''
    }

    @Test
    void testPlaceholderSlotsDoNotDisturbNamedVariables() {
        assertScript '''
            def (x, _, y, _, z) = [1, 2, 3, 4, 5]
            assert "$x $y $z" == '1 3 5'
        '''
    }

    @Test
    void testCompileStaticUnderscoreStaysUndeclaredWithoutPriorBinding() {
        shouldFail '''
            @groovy.transform.CompileStatic
            def m() {
                def (x, _, y, _, z) = [1, 2, 3, 4, 5]
                _
            }
            m()
        '''
    }

    @Test
    void testMapStylePlaceholdersDoNotOverwritePriorUnderscore() {
        assertScript '''
            def _ = 1
            def (name: _, age: a, city: _) = [name: 'Ada', age: 36, city: 'X']
            assert a == 36
            assert _ == 1
        '''
    }

    @Test
    void testMapStylePlaceholdersDoNotIntroduceUnderscore() {
        assertScript '''
            def (name: _, age: a, city: _) = [name: 'Ada', age: 36, city: 'X']
            assert a == 36
        '''
        shouldFail(MissingPropertyException, '''
            def (name: _, age: a, city: _) = [name: 'Ada', age: 36, city: 'X']
            _
        ''')
    }

    @Test
    void testMiddleRestPlaceholdersDoNotOverwritePriorUnderscore() {
        assertScript '''
            def _ = 1
            def (_, *_, last) = [10, 20, 30, 40]
            assert last == 40
            assert _ == 1
        '''
    }

    @Test
    void testHeadRestPlaceholdersDoNotOverwritePriorUnderscore() {
        assertScript '''
            def _ = 9
            def (*_, mid, _) = [1, 2, 3, 4]
            assert mid == 3
            assert _ == 9
        '''
    }

    @Test
    void testBytecodeNameReusesSlotAndFallsBackWhenSlotIsAbsent() {
        def configuration = new CompilerConfiguration()
        def source = new SourceUnit('Groovy11744', '', configuration, null, new ErrorCollector(configuration))
        def visitor = new PlaceholderVisitor(null as CompilationUnit, source)
        def first = new Parameter(ClassHelper.OBJECT_TYPE, '_')
        def second = new Parameter(ClassHelper.OBJECT_TYPE, '_')
        visitor.visitClosureExpression(new ClosureExpression([first, second] as Parameter[], new EmptyStatement()))
        assert PlaceholderVisitor.bytecodeName(first) == '$_unnamed_0'
        assert PlaceholderVisitor.bytecodeName(second) == '$_unnamed_1'

        // a slot recorded before the visit is left alone
        def slotKey = first.nodeMetaData.keySet().find { String.valueOf(it).endsWith('Slot') }
        def kept = new Parameter(ClassHelper.OBJECT_TYPE, '_')
        kept.setNodeMetaData(slotKey, 5)
        def fresh = new Parameter(ClassHelper.OBJECT_TYPE, '_')
        visitor.visitClosureExpression(new ClosureExpression([fresh, kept] as Parameter[], new EmptyStatement()))
        assert PlaceholderVisitor.bytecodeName(fresh) == '$_unnamed_2'
        assert PlaceholderVisitor.bytecodeName(kept) == '$_unnamed_5'

        // a placeholder parameter whose slot was dropped still must not be named "_"
        new ArrayList<>(first.nodeMetaData.keySet()).each { key ->
            if (String.valueOf(key).endsWith('Slot')) first.removeNodeMetaData(key)
        }
        assert PlaceholderVisitor.bytecodeName(first) == '$_unnamed_' + System.identityHashCode(first)
    }

    @Test
    void testDirectUnnamedDeclarationsDeclareNothing() {
        def configuration = new CompilerConfiguration()
        def customizer = new CompilationCustomizer(INSTRUCTION_SELECTION) {
            boolean patched

            @Override
            void call(SourceUnit source, GeneratorContext context, ClassNode classNode) {
                if (!classNode.script) return
                def marked = []
                new ClassCodeVisitorSupport() {
                    @Override
                    void visitDeclarationExpression(DeclarationExpression expression) {
                        if (expression.multipleAssignmentDeclaration) {
                            expression.tupleExpression.expressions.each { expr ->
                                if (expr instanceof VariableExpression && PlaceholderVisitor.isPlaceholder(expr)) {
                                    marked << expr
                                }
                            }
                        }
                        super.visitDeclarationExpression(expression)
                    }

                    @Override
                    SourceUnit getSourceUnit() { source }
                }.visitClass(classNode)
                if (!marked) return

                def run = classNode.getDeclaredMethod('run', Parameter.EMPTY_ARRAY)
                assert run?.code instanceof BlockStatement
                def block = (BlockStatement) run.code
                // no initializer: the declaration must not create a variable
                block.addStatement(stmt(declX(unnamedCopy(marked[0]), EmptyExpression.INSTANCE)))
                // value discarded: the initializer must be dropped instead of writing "_"
                block.addStatement(stmt(declX(unnamedCopy(marked[0]), constX(7))))
                block.addStatement(new AssertStatement(boolX(eqX(varX('_'), constX(1)))))
                patched = true
            }
        }
        configuration.addCompilationCustomizers(customizer)

        new GroovyShell(configuration).evaluate '''
            def _ = 1
            def (_, y, _) = [3, 4, 5]
            assert y == 4
            assert _ == 1
        '''
        assert customizer.patched
    }

    private static VariableExpression unnamedCopy(VariableExpression marked) {
        def copy = new VariableExpression('_')
        copy.copyNodeMetaData(marked)
        copy.setAccessedVariable(copy)
        copy
    }
}
