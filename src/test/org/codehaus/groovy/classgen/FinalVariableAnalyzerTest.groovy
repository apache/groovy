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

import groovy.test.GroovyTestCase
import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.InnerClassNode
import org.codehaus.groovy.ast.Variable
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.customizers.builder.CompilerCustomizationBuilder

class FinalVariableAnalyzerTest extends GroovyTestCase {

    protected void assertFinals(final Map<String, Boolean> expectations, final String script) throws Exception {
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

    protected void assertFinalCompilationErrors(List<String> vars, final String script, boolean unInitialized = false) {
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
        assert (vars - checked).empty
    }

    void testVariableShouldBeEffectivelyFinal() {
        assertFinals x: true, 'def x = 1'
    }

    void testVariableDeclaredAsFinal() {
        assertFinals x: true, '''
            final x = 1
        '''
    }

    void testReassignedVarShouldNotBeFinal() {
        assertFinals x: false, '''
            def x = 1
            x = 2
        '''
    }

    void testReassignedVarShouldNotBeFinalWhenUsingMultiAssigment() {
        assertFinals x: false, '''
            def x = 1
            (x) = [2]
        '''
    }

    void testUnassignedVarShouldNotBeConsideredFinal() {
        assertFinals x: false, '''def x'''
    }

    void testVariableReassignedInClosureShouldNotBeFinal() {
        assertFinals x: false, '''
            def x
            cl = { x=1 }
            cl()
        '''
    }

    void testVariableNotReassignedInClosureShouldBeFinal() {
        assertFinals x: true, '''
            def x = 1
            cl = { x }
            cl()
        '''
    }

    void testVariableInitializedInTwoStepsShouldBeFinal() {
        assertFinals x: true, '''
            def x
            x=1
        '''
    }

    void testParameterShouldBeConsideredFinal() {
        assertFinals x: true, '''
            def foo(int x) { x+1 }
       '''
    }

    void testParameterShouldNotBeConsideredFinal() {
        assertFinals x: false, '''
            def foo(int x) { x = x+1 }
       '''
    }

    void testReassignedFinalVariableShouldThrowCompilationError() {
        assertFinalCompilationErrors(['x'], '''
            final x = []
            x += [1]
        ''')
    }

    void testReassignedFinalParamererShouldThrowCompilationError() {
        assertFinalCompilationErrors(['x'], '''
            void foo(final int x) {
               x = 2
            }
        ''')
    }

    void testFinalVariableAssignedInIfBranchesShouldNotBeFinal() {
        assertFinals x: false, '''
            int x
            if (t) {
                x = 1
            }
        '''
    }

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

    void testAssignmentInIfBooleanExpressionShouldFailCompilation() {
        assertFinalCompilationErrors(['a'], '''
            final a = 3

            if ((a = 4) == 4) {
              assert a == 3
            }
        ''')
    }

    void testDirectlyAssignedClosureSharedVariableShouldBeConsideredFinal() {
        assertFinals x: true, '''
            def x = 1
            def cl = { x }
            cl()
        '''
    }

    void testDelayedAssignedClosureSharedVariableShouldNotBeConsideredFinal() {
        assertFinals x: false, '''
            def x
            def cl = { x }
            x=1
        '''
    }

    void testShouldThrowCompilationErrorBecauseClosureSharedVariable() {
        assertFinalCompilationErrors(['x'], '''
            final x

            def cl = { x }
            x=1
        ''')
    }


    void testDirectlyAssignedAICSharedVariableShouldBeConsideredFinal() {
        assertFinals x: true, '''
            def x = 1
            def cl = new Runnable() { void run() { x } }
            cl.run()
        '''
    }

    void testDelayedAssignedAICSharedVariableShouldNotBeConsideredFinal() {
        assertFinals x: false, '''
            def x
            def cl = new Runnable() { void run() { x } }
            cl.run()
            x = 1
        '''
    }

    void testShouldThrowCompilationErrorBecauseUsedInAIC() {
        assertFinalCompilationErrors(['x'], '''
            final x

            def cl = new Runnable() { public void run() { x } }
            x=1
        ''')
    }

    void testShouldBeCompileTimeErrorBecauseOfUninitializedVar() {
        assertFinalCompilationErrors(['x'], '''
            final x
            def y = x
            x = 1
        ''', true)
    }

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

    void testShouldConsiderFinalVarOkayIfNotAccessedButShouldNotBeDeemedFinal() {
        assertFinals x: false, '''
            final x
        '''
    }

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

    @CompileStatic
    private static class AssertionFinalVariableAnalyzer extends FinalVariableAnalyzer {

        private Set<Variable> variablesToCheck
        private Map<String, Boolean> assertionsToCheck

        AssertionFinalVariableAnalyzer(final SourceUnit sourceUnit, final Map<String, Boolean> assertions) {
            super(sourceUnit)
            assertionsToCheck = assertions
        }

        @Override
        void visitVariableExpression(final VariableExpression expression) {
            super.visitVariableExpression(expression)
            if (assertionsToCheck.containsKey(expression.name)) {
                variablesToCheck << expression
                variablesToCheck << expression.accessedVariable
            }
        }

        @Override
        void visitClass(final ClassNode node) {
            def old = variablesToCheck
            variablesToCheck = []
            super.visitClass(node)
            if (!(node instanceof InnerClassNode)) {
                checkAssertions()
            }
            variablesToCheck = old
        }

        private void checkAssertions() {
            assertionsToCheck.each { name, shouldBeFinal ->
                def candidates = variablesToCheck.findAll { it.name == name }
                assert candidates.any { isEffectivelyFinal(it) == shouldBeFinal }

            }
        }
    }
}
