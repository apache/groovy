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
package org.codehaus.groovy.classgen

import groovy.transform.AutoFinal
import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.InnerClassNode
import org.codehaus.groovy.ast.Variable
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.customizers.builder.CompilerCustomizationBuilder
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

@AutoFinal
final class FinalVariableAnalyzerTest {

    protected void assertFinals(Map<String, Boolean> expectations, String script) throws Exception {
        def cc = new CompilerConfiguration()
        CompilerCustomizationBuilder.withConfig(cc) {
            inline(phase: 'SEMANTIC_ANALYSIS') { source, context, classNode ->
                def analyzer = new AssertionFinalVariableAnalyzer(source, expectations)
                analyzer.visitClass(classNode)
            }
        }
        def shell = new GroovyShell(cc)
        shell.parse(script)
    }

    protected void assertFinalCompilationErrors(List<String> vars, String script, boolean unInitialized = false) {
        Set<String> checked = []
        try {
            assertFinals [:], script
        } catch (MultipleCompilationErrorsException e) {
            vars.each { var ->
                if (!unInitialized) {
                    assert (e.message =~ "The variable \\[${var}\\] is declared final but is reassigned" ||
                            e.message =~ "Cannot assign a value to final variable '${var}'")
                } else {
                    assert e.message =~ "The variable \\[${var}\\] may be uninitialized"
                }
                checked << var
            }
        }
        assert (vars - checked).isEmpty()
    }

    @Test
    void testVariableShouldBeEffectivelyFinal() {
        assertFinals x: true, 'def x = 1'
    }

    @Test
    void testVariableDeclaredAsFinal() {
        assertFinals x: true, '''
            final x = 1
        '''
    }

    @Test
    void testReassignedVarShouldNotBeFinal() {
        assertFinals x: false, '''
            def x = 1
            x = 2
        '''
    }

    @Test
    void testReassignedVarShouldNotBeFinalWhenUsingMultiAssignment() {
        assertFinals x: false, '''
            def x = 1
            (x) = [2]
        '''
    }

    @Test
    void testUnassignedVarShouldNotBeConsideredFinal() {
        assertFinals x: false, '''def x'''
    }

    @Test
    void testVariableReassignedInClosureShouldNotBeFinal() {
        assertFinals x: false, '''
            def x
            cl = { x=1 }
            cl()
        '''
    }

    @Test
    void testVariableNotReassignedInClosureShouldBeFinal() {
        assertFinals x: true, '''
            def x = 1
            cl = { x }
            cl()
        '''
    }

    @Test
    void testVariableInitializedInTwoStepsShouldBeFinal() {
        assertFinals x: true, '''
            def x
            x=1
        '''
    }

    @Test
    void testParameterShouldBeConsideredFinal() {
        assertFinals x: true, '''
            def foo(int x) { x+1 }
       '''
    }

    @Test
    void testParameterShouldNotBeConsideredFinal() {
        assertFinals x: false, '''
            def foo(int x) { x = x+1 }
       '''
    }

    @Test
    void testReassignedFinalVariableShouldThrowCompilationError() {
        assertFinalCompilationErrors(['x'], '''
            final x = []
            x += [1]
        ''')
    }

    @Test
    void testReassignedFinalParameterShouldThrowCompilationError() {
        assertFinalCompilationErrors(['x'], '''
            void foo(final int x) {
               x = 2
            }
        ''')
    }

    @Test
    void testFinalVariableAssignedInIfBranchesShouldNotBeFinal() {
        assertFinals x: false, '''
            int x
            if (t) {
                x = 1
            }
        '''
    }

    @Test
    void testFinalVariableAssignedInElseBranchesShouldStillNotBeFinal() {
        assertFinals x: false, '''
            int x
            if (t) {
                // nothing
            } else {
                x = 1
            }
        '''
    }

    @Test
    void testFinalVariableAssignedInIfElseBranchesShouldStillBeFinal() {
        assertFinals x: true, '''
            int x
            if (t) {
                x = 1
            } else {
                x = 2
            }
        '''
    }

    @Test
    void testFinalVariableAssignedInIfElseBranchesShouldNotBeFinal() {
        assertFinals x: false, '''
            int x
            if (t) {
                x = 1
                x = 2
            } else {
                x = 2
            }
        '''
    }

    @Test
    void testFinalVariableAssignedInIfElseBranchesShouldNotBeFinal2() {
        assertFinals x: false, '''
            int x
            if (t) {
                x = 1
            } else {
                x = 1
                x = 2
            }
        '''
    }

    @Test
    void testNestedIfShouldNotBeFinal() {
        assertFinals x: false, '''
            int x
            if (t1) {
                if (t2) {
                    x = 1
                }
            }
            if (t2) {
                if (t3) {
                    x = 1
                }
            }
        '''
    }

    // GROOVY-8386
    @Test
    void testPotentiallyUninitializedFinalVarOkayIfNotUsed() {
        assertFinals begin: false, '''
            final begin
            try {
                begin = new Date()
            } finally {
                println 'done'
            }
        '''
    }

    // GROOVY-8094
    @Test
    void testFinalVarSetWithinIf() {
        assertFinalCompilationErrors(['z'], '''
            def method() {
                final z = null
                if (z != null) {
                    z = 3
                }
            }
        ''')
    }

    @Test
    void testPrePostfixShouldMakeVarNotFinal() {
        assertFinals x: false, y: false, z: false, o: false, '''
            def x = 0
            def y = 0
            def z = 0
            def o = 0
            x++
            ++y
            z--
            --o
        '''
    }

    @Test
    void testPrePostfixShouldNotCompileWithUninitializedFinalVar() {
        assertFinalCompilationErrors(['x'], '''
            final x
            x++
        ''', true)
        assertFinalCompilationErrors(['y'], '''
            final y
            --y
        ''', true)
    }

    @Test
    void testAssignmentInIfBooleanExpressionShouldFailCompilation() {
        assertFinalCompilationErrors(['a'], '''
            final a = 3

            if ((a = 4) == 4) {
              assert a == 3
            }
        ''')
    }

    @Test
    void testDirectlyAssignedClosureSharedVariableShouldBeConsideredFinal() {
        assertFinals x: true, '''
            def x = 1
            def cl = { x }
            cl()
        '''
    }

    @Test
    void testDelayedAssignedClosureSharedVariableShouldNotBeConsideredFinal() {
        assertFinals x: false, '''
            def x
            def cl = { x }
            x=1
        '''
    }

    @Test
    void testShouldThrowCompilationErrorBecauseClosureSharedVariable() {
        assertFinalCompilationErrors(['x'], '''
            final x

            def cl = { x }
            x=1
        ''')
    }

    @Test
    void testDirectlyAssignedAICSharedVariableShouldBeConsideredFinal() {
        assertFinals x: true, '''
            def x = 1
            def cl = new Runnable() { void run() { x } }
            cl.run()
        '''
    }

    @Test
    void testDelayedAssignedAICSharedVariableShouldNotBeConsideredFinal() {
        assertFinals x: false, '''
            def x
            def cl = new Runnable() { void run() { x } }
            cl.run()
            x = 1
        '''
    }

    @Test
    void testShouldThrowCompilationErrorBecauseUsedInAIC() {
        assertFinalCompilationErrors(['x'], '''
            final x

            def cl = new Runnable() { public void run() { x } }
            x=1
        ''')
    }

    @Test
    void testShouldBeCompileTimeErrorBecauseOfUninitializedVar() {
        assertFinalCompilationErrors(['x'], '''
            final x
            def y = x
            x = 1
        ''', true)
    }

    @Test
    void testShouldConsiderThatXIsEffectivelyFinalWithIfElse() {
        assertFinals x: true, '''
            int x
            if (foo) {
              x=1
            } else {
              x=2
            }

        '''
    }

    @Test
    void testShouldConsiderThatXIsNotEffectivelyFinalWithSubsequentIfs() {
        assertFinals x: false, '''
            int x
            if (foo) {
              x=1
            }
            if (!foo) {
              x=2
            }
        '''
    }

    @Test
    void testShouldConsiderFinalVarOkayIfNotAccessedButShouldNotBeDeemedFinal() {
        assertFinals x: false, '''
            final x
        '''
    }

    @Test
    void testShouldThrowCompileTimeErrorBecauseXIsNotEffectivelyFinalWithSubsequentIfs() {
        assertFinalCompilationErrors(['x'], '''
            final x
            if (foo) {
              x=1
            }
            if (!foo) {
              x=2 // follow Java here, don't try to interpret boolean's across different if statements
                  // so consider this as might have been already initialized at this point
            }
        ''')
    }

    @Test
    void testShouldNotSayThatVarIsNotInitialized() {
        assertScript '''
                final List<String> list = ['a','b'].collect { it.toUpperCase() }
                def result
                if (true) {
                    result = list[0]
                } else {
                    result = list[1]
                }
                result

        '''
        assertScript '''
            private def setValueOfMethod() {
                if (cache) {
                    final int offset = 128;
                }
            }

            1
        '''
    }

    @Test
    void testFinalVariableInitializedInTryCatchFinally() {
        // x initialized in try block
        assertFinals x: false, '''
            int x
            try {
              x=1
            } finally {

            }
        '''

        // x initialized in catch block
        assertFinals x: false, '''
            int x
            try {
            } catch (e) {
               x = 1
            } finally {

            }
        '''

        // x initialized in finally block
        assertFinals x: true, '''
            int x
            try {
            } catch(e) {
            } finally {
               x = 1
            }
        '''
    }

    @Test
    void testVarInitializedInBothTryCatchAndFinallyBlocksShouldBeCompileTimeError() {
        assertFinalCompilationErrors(['x'], '''
            final x
            try {
                x = 1
            } finally {
                x = 2
            }
        ''')
        assertFinalCompilationErrors(['x'], '''
            final x
            try {
            } catch(e) {
                x = 1
            }finally {
                x = 2
            }
        ''')
    }

    // GROOVY-8093
    @Test
    void testLocalVarsInClosureOnlyCheckedWithinClosure() {
        assertScript '''
            class Foo {
                public Closure bar = {
                    final RANKINGS = ["year": 0, "month": 10]
                    RANKINGS.size()
                }
            }

            assert new Foo().bar() == 2
        '''
    }

    // GROOVY-8472
    @Test
    void testTryCatchWithReturnInTry() {
        assertScript '''
            def method(String foo) {
                final str
                try {
                    return foo.trim()
                }
                catch(e) {
                    str = '-1'
                }
                int exitCode = str.toInteger()
                exitCode
            }

            assert method(null) == -1
            assert method('  foo  ') == 'foo'
        '''
    }

    // GROOVY-8472
    @Test
    void testTryCatchWithReturnInCatch() {
        assertScript '''
            def method(String foo) {
                final str
                try {
                    str = foo.trim()
                }
                catch(RuntimeException re) {
                    return re.message
                }
                catch(Throwable t) {
                    return -1
                }
                int exitCode = str.isInteger() ? str.toInteger() : null
                exitCode
            }

            assert method(null) == 'Cannot invoke method trim() on null object'
            assert method('  42  ') == 42
        '''
    }

    // GROOVY-9424
    @Test
    void testFinalVarInitializedByAllSwitchBranches() {
        assertScript '''
            final String result

            switch (2) {
                case 1: result = 'a'; break
                case 2: // fallthrough
                case 3: result = 'b'; break
                case 4: throw new RuntimeException('Boom')
                case 5: return
                default: result = 'x'
            }

            assert result == 'b'
        '''
    }

    // GROOVY-9438
    @Test
    void testFinalVarWithSwitchAsLastStatementAndCaseContainingOnlyBreak() {
        assertScript '''
            switch(1) {
                case 1:
                    break
                default:
                    println 2
            }
        '''
    }

    // GROOVY-10580
    @Test
    void testFinalVarIfElseStatementReturningBranch() {
        assertScript '''
            final int i
            final boolean a = true
            if (a) {
                i = 1
            } else {
                throw new IllegalStateException('a is false')
            }
            def result = i
            assert result == 1

            def evalJ() {
                final int j
                final boolean b = true
                if (!b) {
                    return null
                } else {
                    j = 2
                }
                def result = j
                return result
            }
            assert evalJ() == 2
        '''
    }

    // GROOVY-11273
    @Test
    void testReassignedFinalVariableInInterfaceDefaultMethodShouldThrowCompilationError() {
        assertFinalCompilationErrors(['x'], '''
            interface Test {
                default void test() {
                    final int x = 1;
                    x = 2;
                }
            }
        ''')
    }

    @AutoFinal @CompileStatic
    private static class AssertionFinalVariableAnalyzer extends FinalVariableAnalyzer {

        private final Map<String, Boolean> assertionsToCheck
        private Set<Variable> variablesToCheck

        AssertionFinalVariableAnalyzer(SourceUnit sourceUnit, Map<String, Boolean> assertions) {
            super(sourceUnit)
            assertionsToCheck = assertions
        }

        @Override
        void visitVariableExpression(VariableExpression expression) {
            super.visitVariableExpression(expression)
            if (assertionsToCheck.containsKey(expression.name)) {
                variablesToCheck << expression
                variablesToCheck << expression.accessedVariable
            }
        }

        @Override
        void visitClass(ClassNode node) {
            def vars = variablesToCheck
            try {
                variablesToCheck = []
                super.visitClass(node)
                if (node !instanceof InnerClassNode) {
                    checkAssertions()
                }
            } finally {
                variablesToCheck = vars
            }
        }

        private void checkAssertions() {
            for (entry in assertionsToCheck) {
                def name = entry.key, shouldBeFinal = entry.value
                def candidates = variablesToCheck.findAll { it.name == name }
                assert candidates.any { isEffectivelyFinal(it) == shouldBeFinal }
            }
        }
    }
}
