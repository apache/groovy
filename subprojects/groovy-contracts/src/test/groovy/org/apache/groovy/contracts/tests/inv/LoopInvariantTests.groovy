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
package org.apache.groovy.contracts.tests.inv

import org.apache.groovy.contracts.tests.basic.BaseTestClass
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * Tests for {@code @Invariant} applied to loop statements.
 */
class LoopInvariantTests extends BaseTestClass {

    @Test
    void invariantOnForInLoop() {
        assertScript '''
            import groovy.contracts.Invariant

            int sum = 0
            @Invariant({ 0 <= i && i <= 4 })
            for (int i in 0..4) {
                sum += i
            }
            assert sum == 10
        '''
    }

    @Test
    void invariantOnClassicForLoop() {
        assertScript '''
            import groovy.contracts.Invariant

            int product = 1
            @Invariant({ product >= 1 })
            for (int i = 1; i <= 5; i++) {
                product *= i
            }
            assert product == 120
        '''
    }

    @Test
    void invariantOnWhileLoop() {
        assertScript '''
            import groovy.contracts.Invariant

            int n = 10
            @Invariant({ n >= 0 })
            while (n > 0) {
                n--
            }
            assert n == 0
        '''
    }

    @Test
    void invariantOnDoWhileLoop() {
        assertScript '''
            import groovy.contracts.Invariant

            int count = 0
            @Invariant({ count >= 0 })
            do {
                count++
            } while (count < 3)
            assert count == 3
        '''
    }

    // GROOVY-12128: a do-while runs its body before the first condition check, so the loop
    // invariant only needs to be established by that first body execution; checking it at loop
    // entry rejects valid loops.
    @Test
    void invariantOnDoWhileEstablishedByFirstBodyExecution() {
        assertScript '''
            import groovy.contracts.*

            class C {
                @Requires({ n >= 1 })
                @Ensures({ result == n })
                static int countUp(int n) {
                    int i = 0
                    @Invariant({ 1 <= i && i <= n })
                    @Decreases({ n - i })
                    do { i++ } while (i < n)
                    return i
                }
            }

            assert C.countUp(1) == 1
            assert C.countUp(3) == 3
        '''
    }

    // GROOVY-12128: the do-while invariant is checked after each body execution, so a body
    // that breaks the invariant is caught even when the invariant held at loop entry.
    @Test
    void invariantOnDoWhileViolatedByBodyThrows() {
        shouldFail AssertionError, '''
            import groovy.contracts.Invariant

            int i = 0
            @Invariant({ i <= 2 })
            do {
                i += 5
            } while (false)
        '''
    }

    // GROOVY-12128: an invariant is not required to hold at a break exit (only on the paths
    // that continue looping), so the do-while end-of-body check must be skipped by break —
    // matching the while/for placement, where break likewise exits after the entry check.
    @Test
    void invariantOnDoWhileNotRequiredAtBreak() {
        assertScript '''
            import groovy.contracts.Invariant

            int i = 0
            @Invariant({ i <= 2 })
            do {
                i = 99
                break
            } while (true)
            assert i == 99
        '''
    }

    @Test
    void multipleInvariantsOnLoop() {
        assertScript '''
            import groovy.contracts.Invariant

            int sum = 0
            @Invariant({ sum >= 0 })
            @Invariant({ sum <= 100 })
            for (int i in 1..5) {
                sum += i
            }
            assert sum == 15
        '''
    }

    @Test
    void invariantViolationThrows() {
        shouldFail AssertionError, '''
            import groovy.contracts.Invariant

            int n = 5
            @Invariant({ n > 0 })
            while (n >= 0) {
                n--
            }
        '''
    }

    @Test
    void invariantWithComplexExpression() {
        assertScript '''
            import groovy.contracts.Invariant

            def items = []
            @Invariant({ items.size() <= 5 })
            for (int i in 1..5) {
                items << i
            }
            assert items == [1, 2, 3, 4, 5]
        '''
    }

    @Test
    void invariantViolationOnFirstIteration() {
        shouldFail AssertionError, '''
            import groovy.contracts.Invariant

            int x = -1
            @Invariant({ x >= 0 })
            for (int i in 0..2) {
                x = i
            }
        '''
    }

    @Test
    void classInvariantStillWorksWithLoopInvariantTransform() {
        assertScript '''
            import groovy.contracts.Invariant

            @Invariant({ property != null })
            class Foo {
                def property
                Foo(val) { property = val }
            }

            def f = new Foo('hello')
            assert f.property == 'hello'
        '''
    }

    @Test
    void invariantOnImportActsAsScriptClassInvariant() {
        assertScript '''
            @groovy.contracts.Invariant({ property != null })
            import groovy.transform.Field

            @Field String property = 'hello'
            assert property == 'hello'
        '''
    }

    @Test
    void invariantOnImportViolationThrows() {
        shouldFail AssertionError, '''
            @groovy.contracts.Invariant({ property != null })
            import groovy.transform.Field

            @Field String property = 'hello'

            def nullify() {
                property = null
            }

            nullify()
        '''
    }

    @Test
    void invariantOnImportBankAccountScript() {
        assertScript '''
            @Invariant({ balance >= 0 })
            import groovy.transform.Field
            import groovy.contracts.Invariant
            import static groovy.test.GroovyAssert.shouldFail
            import org.apache.groovy.contracts.ClassInvariantViolation

            @Field Integer balance = 5

            def withdraw(int amount) { balance -= amount }

            def deposit(int amount) { balance += amount }

            deposit(5)
            assert balance == 10

            shouldFail(ClassInvariantViolation) {
                withdraw(15)
            }

            balance = 10  // restore valid state (withdraw left balance at -5)

            shouldFail(ClassInvariantViolation) {
                deposit(-15)
            }

            balance = 10  // restore valid state before run() ends
        '''
    }

    @Test
    void ensuresOnScriptMethods() {
        assertScript '''
            import groovy.transform.Field
            import groovy.contracts.*
            import static groovy.test.GroovyAssert.shouldFail

            @Field Integer balance = 5

            @Ensures({ balance >= 0 })
            def withdraw(int amount) { balance -= amount }

            @Ensures({ balance >= 0 })
            def deposit(int amount) { balance += amount }

            deposit(5)
            assert balance == 10

            shouldFail(AssertionError) {
                withdraw(15)
            }

            balance = 5

            shouldFail(AssertionError) {
                deposit(-10)
            }
        '''
    }

    @Test
    void requiresOnScriptMethods() {
        assertScript '''
            import groovy.transform.Field
            import groovy.contracts.*
            import static groovy.test.GroovyAssert.shouldFail

            @Field Integer balance = 5

            @Requires({ balance >= amount })
            def withdraw(int amount) { balance -= amount }

            @Requires({ amount >= 0 })
            def deposit(int amount) { balance += amount }

            deposit(5)
            assert balance == 10

            shouldFail(AssertionError) {
                withdraw(15)
            }

            shouldFail(AssertionError) {
                deposit(-15)
            }
        '''
    }

    @Test
    void invariantUnderTypeChecked() {
        assertScript '''
            import groovy.contracts.Invariant
            import groovy.transform.TypeChecked

            @TypeChecked
            def method() {
                int sum = 0
                @Invariant({ sum >= 0 })
                @Invariant({ sum <= 100 })
                for (int i in 1..5) {
                    sum += i
                }
                assert sum == 15
            }
            method()
        '''
    }

    @Test
    void invariantUnderCompileStatic() {
        assertScript '''
            import groovy.contracts.Invariant
            import groovy.transform.CompileStatic

            @CompileStatic
            def method() {
                int sum = 0
                @Invariant({ sum >= 0 })
                for (int i in 1..5) {
                    sum += i
                }
                assert sum == 15
            }
            method()
        '''
    }

    @Test
    void invariantViolationUnderCompileStaticThrows() {
        shouldFail AssertionError, '''
            import groovy.contracts.Invariant
            import groovy.transform.CompileStatic

            @CompileStatic
            def method() {
                int n = 5
                @Invariant({ n > 0 })
                while (n >= 0) {
                    n--
                }
            }
            method()
        '''
    }

    // GROOVY-12072: a statement-level @Invariant sits on a node the compiler's static-import
    // pass never visits, so unqualified statically imported members in the closure must be
    // resolved by the loop transform itself (they are resolved fine in class invariants and
    // preconditions, whose annotations live on AnnotatedNodes).
    @Test
    void invariantWithStaticImportedMethod() {
        assertScript '''
            import groovy.contracts.*
            import static java.lang.Math.max

            @Invariant({ max(3, 4) == 4 })
            class C {
                @Requires({ max(3, 4) == 4 })
                static int iter(int n) {
                    int a = 0
                    @Invariant({ max(3, 4) == 4 })
                    while (a < n) { a++ }
                    return a
                }
            }

            assert C.iter(4) == 4
        '''
    }

    @Test
    void invariantWithStaticStarImportedMethodUnderCompileStatic() {
        assertScript '''
            import groovy.contracts.Invariant
            import groovy.transform.CompileStatic
            import static java.lang.Math.*

            @CompileStatic
            static int loop(int n) {
                int a = 0
                @Invariant({ max(0, a) <= n && min(1, 2) == 1 })
                while (a < n) { a++ }
                return a
            }

            assert loop(5) == 5
        '''
    }

    // GROOVY-12072: a qualified type reference (Math) in the closure is likewise never resolved by
    // the compiler's ResolveVisitor, so the loop transform must resolve it too — otherwise it is
    // reported as an "apparent variable ... in a static scope", especially under @CompileStatic.
    @Test
    void invariantWithQualifiedTypeReferenceUnderCompileStatic() {
        assertScript '''
            import groovy.contracts.*
            import java.lang.Math

            @groovy.transform.CompileStatic
            @Invariant({ Math.max(3, 4) == 4 })
            class C {
                @Requires({ Math.max(3, 4) == 4 })
                static int iter(int n) {
                    int a = 0
                    @Invariant({ Math.max(3, 4) == 4 })
                    while (a < n) { a++ }
                    return a
                }
            }

            assert C.iter(4) == 4
        '''
    }

    @Test
    void invariantWithFullyQualifiedTypeReference() {
        assertScript '''
            import groovy.contracts.Invariant

            int s = 0
            @Invariant({ java.lang.Math.max(1, 2) == 2 })
            for (int i = 0; i < 3; i++) { s += i }
            assert s == 3
        '''
    }

    // A clashing instance method must still win over the static import in an instance-method
    // context, confirming the re-resolution honours the enclosing method's static-ness.
    @Test
    void invariantInstanceMethodShadowsStaticImport() {
        assertScript '''
            import groovy.contracts.Invariant
            import static java.lang.Math.max

            class F {
                int max(int x, int y) { 999 }
                int loop(int n) {
                    int a = 0
                    @Invariant({ max(1, 2) == 999 })
                    while (a < n) { a++ }
                    return a
                }
            }

            assert new F().loop(2) == 2
        '''
    }
}

