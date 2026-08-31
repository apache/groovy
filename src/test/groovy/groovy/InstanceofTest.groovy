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
package groovy

import org.junit.jupiter.api.Test

import org.codehaus.groovy.control.MultipleCompilationErrorsException

import static groovy.test.GroovyAssert.shouldFail

final class InstanceofTest {

    @Test
    void testIsInstance() {
        def o = 12

        assert (o instanceof Integer)
    }

    @Test
    void testNotInstance() {
        def o = 12

        assert !(o instanceof Double)
    }

    @Test
    void testImportedClass() {
        def m = ["xyz":2]

        assert  (m  instanceof Map)
        assert !(m !instanceof Map)
        assert !(m  instanceof Double)
        assert  (m !instanceof Double)
    }

    @Test
    void testFullyQualifiedClass() {
        def l = [1, 2, 3]

        assert (l instanceof java.util.List)
        assert !(l instanceof java.util.Map)
        assert (l !instanceof java.util.Map)
    }

    @Test
    void testBoolean() {
       assert true instanceof Object
       assert true==true instanceof Object
       assert true==false instanceof Object
       assert true==false instanceof Boolean
       assert !new Object() instanceof Boolean
    }

    // GROOVY-11585 — full Java-vs-Groovy matrix is GenericsJavaCompatibilityTest
    // (reifiable List<?>/Map<?,?>, non-reifiable List<String>/Map<String,Integer>,
    // bounded wildcards, rare types). This is the named regression only.
    @Test
    void testGenerics() {
        assert [] instanceof List<?>

        def err = shouldFail '''
            def x = ([] instanceof List<String>)
        '''
        assert err.message =~ 'Cannot perform instanceof check against parameterized type List<String>'
    }

    // GROOVY-11229
    @Test
    void testVariable() {
        def n = (Number) 12345
        if (n instanceof Integer i) {
            assert i.intValue() == 12345
        } else {
            assert false : 'expected Integer'
        }
        if (n instanceof String s) {
            assert false : 'not String'
        } else {
            assert n.intValue() == 12345
        }
        assert (n instanceof Integer i && i.intValue() == 12345)
    }

    // GROOVY-12242: native !instanceof type pattern (JEP 394)
    @Test
    void testNotInstanceofPatternVariable() {
        def n = (Number) 12345
        if (n !instanceof String s) {
            assert n.intValue() == 12345
        } else {
            assert false : 'expected non-String'
        }
        if (n !instanceof Integer i) {
            assert false : 'expected Integer (condition false)'
        } else {
            assert i.intValue() == 12345
        }
    }

    // GROOVY-12242: !instanceof pattern + short-circuit / early return survivors
    @Test
    void testNotInstanceofPatternScope() {
        def f = { Object o ->
            if (o !instanceof String s) {
                return 'not-string'
            } else {
                return s.toUpperCase()
            }
        }
        assert f('hi') == 'HI'
        assert f(1) == 'not-string'

        def g = { Object o ->
            if (o !instanceof String s) return 'early'
            return s.toUpperCase()
        }
        assert g('ab') == 'AB'
        assert g(9) == 'early'
    }

    // GROOVY-12242: !instanceof pattern must not leak into then-block
    @Test
    void testNotInstanceofPatternThenBlockDynamic() {
        def err = shouldFail MissingPropertyException, '''
            def m(Object o) {
                if (o !instanceof String s) {
                    return s
                }
                return 'ok'
            }
            m(1)
        '''
        assert err.message =~ /No such property: s/
    }

    // GROOVY-12242: ternary with !instanceof pattern
    @Test
    void testNotInstanceofPatternTernary() {
        def f = { Object o ->
            (o !instanceof String s) ? 'not' : s.toUpperCase()
        }
        assert f(1) == 'not'
        assert f('xy') == 'XY'
    }

    // GROOVY-12242: && / || with !instanceof pattern
    @Test
    void testNotInstanceofPatternBooleanOps() {
        // true path of !instanceof is empty — RHS of && must not see s as bound from left
        def err = shouldFail MissingPropertyException, '''
            def m(Object o) {
                return (o !instanceof String s && s.isEmpty())
            }
            m(1)
        '''
        assert err.message =~ /No such property: s/

        // Same for parenthesised negation form (&& isolation is flow-based)
        err = shouldFail MissingPropertyException, '''
            def m(Object o) {
                return (!(o instanceof String s) && s.isEmpty())
            }
            m(1)
        '''
        assert err.message =~ /No such property: s/

        // false path of !instanceof binds s — RHS of || can use s when left is false
        assert (({ Object o -> (o !instanceof String s || s.isEmpty()) }('')) == true)
        assert (({ Object o -> (o !instanceof String s || s.length() > 0) }('ab')) == true)
        assert (({ Object o -> (o !instanceof String s || s.isEmpty()) }(1)) == true)
    }

    // GROOVY-11229
    @Test
    void testVariable2() {
        assert transformString(null) == null
        assert transformString(1234) == 1234
        assert transformString('xx') == 'XX'
    }

    def transformString(o) {
        o instanceof String s ? s.toUpperCase() : o
    }

    // GROOVY-11229
    @Test
    void testVariableScope() {
        def err = shouldFail '''
            def x = null
            if (x instanceof String s) {
            } else {
                s
            }
        '''
        assert err.message =~ /No such property: s/

        def shell = GroovyShell.withConfig {
            ast groovy.transform.TypeChecked
        }

        err = shouldFail shell, '''
            Number n = 12345
            if (n instanceof Integer i) {
            }
            i.toString()
        '''
        assert err.message =~ /The variable .i. is undeclared\.\s+@ line 5, column 13/

        err = shouldFail shell, '''
            Number n = 12345
            if (n instanceof Integer i) ; else {
                i.toString()
            }
        '''
        assert err.message =~ /The variable .i. is undeclared\.\s+@ line 4, column 17/

        err = shouldFail shell, '''
            Number n = 12345
            while (n instanceof Integer i) {
                n = i.doubleValue()
            }
            i.toString()
        '''
        assert err.message =~ /The variable .i. is undeclared\.\s+@ line 6, column 13/

        err = shouldFail shell, '''
            Number n = 12345
            do {
                n = n.doubleValue()
            } while (n instanceof Integer i)
            i.toString()
        '''
        assert err.message =~ /The variable .i. is undeclared\.\s+@ line 6, column 13/

        err = shouldFail shell, '''
            Number n = 12345
            do {
                i.toString()
            } while (n instanceof Integer i && (n = i.doubleValue()))
        '''
        assert err.message =~ /The variable .i. is undeclared\.\s+@ line 4, column 17/

        err = shouldFail shell, '''
            Number n = 12345
            switch (n instanceof Integer i) {
              case true:
                i.toString()
              case false:
                i.toString()
            }
            i.toString()
        '''
        assert err.message =~ /The variable .i. is undeclared\.\s+@ line 9, column 13/

        err = shouldFail shell, '''
            Number n = 12345
            return (n instanceof Integer i && i.intValue() == 12345)
            i.toString()
        '''
        assert err.message =~ /The variable .i. is undeclared\.\s+@ line 4, column 13/

        err = shouldFail shell, '''
            Number n = 12345
            assert (n instanceof Integer i && i.intValue() == 12345)
            i.toString()
        '''
        assert err.message =~ /The variable .i. is undeclared\.\s+@ line 4, column 13/

        err = shouldFail shell, '''
            Number n = 12345
            print(n instanceof Integer i && i.doubleValue())
            i.toString()
        '''
        assert err.message =~ /The variable .i. is undeclared\.\s+@ line 4, column 13/

        err = shouldFail shell, '''
            Number n = 12345;
            {
                print(n instanceof Integer i && i.doubleValue())
            }
            i.toString()
        '''
        assert err.message =~ /The variable .i. is undeclared\.\s+@ line 6, column 13/

        err = shouldFail shell, '''
            Number n = 12345
            boolean b = (n instanceof Integer i && i.intValue())
            i.toString()
        '''
        assert err.message =~ /The variable .i. is undeclared\.\s+@ line 4, column 13/
    }

    // GROOVY-11828
    @Test
    void testVariableShare() {
        def x = 'foo', y
        if (x instanceof String s) {
            y = { -> s + 'bar' }()
        }
        assert y == 'foobar'
    }

    // GROOVY-12242: Java-aligned flow scoping for negated instanceof (JEP 394)
    @Test
    void testVariableScopeNegatedElse() {
        def f = { Object o ->
            if (!(o instanceof String s)) {
                return 'not'
            } else {
                return s.toUpperCase()
            }
        }
        assert f('hi') == 'HI'
        assert f(1) == 'not'
    }

    // GROOVY-12242: pattern variable remains in scope after abrupt then-branch
    @Test
    void testVariableScopeEarlyReturn() {
        def f = { Object o ->
            if (!(o instanceof String s)) return 'early'
            return s.toUpperCase()
        }
        assert f('hi') == 'HI'
        assert f(42) == 'early'
    }

    // GROOVY-12242: pattern variable remains after else that cannot complete normally
    @Test
    void testVariableScopeAfterAbruptElse() {
        def f = { Object o ->
            if (o instanceof String s) {
                // matched
            } else {
                return 'no'
            }
            return s.toUpperCase()
        }
        assert f('ab') == 'AB'
        assert f(9) == 'no'
    }

    // GROOVY-12242: pattern variable must not leak after a declaration statement
    @Test
    void testVariableNoLeakAfterDeclaration() {
        def err = shouldFail MissingPropertyException, '''
            class C {
                Object m(Object o) {
                    boolean b = (o instanceof String s)
                    return s
                }
            }
            new C().m('hi')
        '''
        assert err.message =~ /No such property: s/
    }

    // GROOVY-12242: pattern variable must not leak after an expression statement
    @Test
    void testVariableNoLeakAfterExpressionStatement() {
        def err = shouldFail MissingPropertyException, '''
            class C {
                Object m(Object o) {
                    o instanceof String s && s.length() > 0
                    return s
                }
            }
            new C().m('hi')
        '''
        assert err.message =~ /No such property: s/
    }

    // GROOVY-12242: true branch of negated instanceof must not see the pattern local
    // (CompileStack polarity must match VariableScope — no silent null ALOAD)
    @Test
    void testVariableNegatedIfBranchNotInScope() {
        def err = shouldFail MissingPropertyException, '''
            class C {
                Object m(Object o) {
                    if (!(o instanceof String s)) {
                        return s
                    }
                    return 'matched'
                }
            }
            new C().m(1)
        '''
        assert err.message =~ /No such property: s/
    }

    // GROOVY-12242: true-path binding of left of || is not in scope on the right (Java)
    @Test
    void testVariableOrRightHandSideNotInScope() {
        def shell = GroovyShell.withConfig {
            ast groovy.transform.TypeChecked
        }
        def err = shouldFail shell, '''
            @groovy.transform.TypeChecked
            class C {
                static void m(Object o) {
                    if (o instanceof String s || s.length() > 0) {
                    }
                }
            }
        '''
        assert err.message =~ /The variable .s. is undeclared|Apparent variable .s./
    }

    // GROOVY-12242: false-path binding is in scope on the right of || (Java)
    @Test
    void testVariableOrRightHandSideFalsePathInScope() {
        def f = { Object o ->
            // when o is String, left is false, right sees s
            return (!(o instanceof String s) || s.isEmpty())
        }
        assert f('') == true
        assert f('x') == false
        assert f(1) == true // left true → short-circuit, s not needed
    }

    // GROOVY-12242: ternary false branch must not see true-path pattern variable
    @Test
    void testVariableTernaryFalseBranchNotInScope() {
        def shell = GroovyShell.withConfig {
            ast groovy.transform.TypeChecked
        }
        def err = shouldFail shell, '''
            @groovy.transform.TypeChecked
            class C {
                static Object m(Object o) {
                    return o instanceof String s ? 'yes' : s
                }
            }
        '''
        assert err.message =~ /The variable .s. is undeclared|Apparent variable .s./
    }

    // GROOVY-12242: dynamic ternary false branch must not load a pattern local
    @Test
    void testVariableTernaryFalseBranchNotInScopeDynamic() {
        def err = shouldFail MissingPropertyException, '''
            class C {
                Object m(Object o) {
                    return o instanceof String s ? 'yes' : s
                }
            }
            new C().m(1)
        '''
        assert err.message =~ /No such property: s/
    }

    // GROOVY-12242: ternary true branch sees pattern variable
    @Test
    void testVariableTernaryTrueBranch() {
        def f = { Object o -> o instanceof String s ? s.toUpperCase() : 'no' }
        assert f('ab') == 'AB'
        assert f(1) == 'no'
    }

    // GROOVY-12242: reassignment of pattern variable (not implicitly final, JEP 394)
    @Test
    void testVariableReassignment() {
        Object o = 'hi'
        if (o instanceof String s) {
            s = s + '!'
            assert s == 'hi!'
        } else {
            assert false
        }
    }

    // GROOVY-12242: pattern variable shadows a field only where in scope
    @Test
    void testVariableFieldShadowing() {
        def obj = new Object() {
            String s = 'field'
            def test(Object o) {
                if (o instanceof String s) {
                    return "pv=$s"
                }
                return "field=$s"
            }
        }
        assert obj.test('x') == 'pv=x'
        assert obj.test(1) == 'field=field'
    }

    // GROOVY-12242: && chain uses pattern variable on subsequent operands
    @Test
    void testVariableAndChain() {
        Object o = 'hello'
        assert (o instanceof String s && s.length() > 3 && s.startsWith('h'))
        assert !(o instanceof String s && s.length() > 99)
    }

    // GROOVY-12242: while body can use true-path pattern variable
    @Test
    void testVariableWhileBody() {
        Object o = 'ab'
        def n = 0
        while (o instanceof String s && s.length() > 0) {
            n += 1
            o = s.substring(1)
        }
        assert n == 2
        assert o == ''
    }

    // GROOVY-12242: negated while condition — s not in body; not after (partial JLS)
    @Test
    void testWhileNegated_bodyAndAfterNotVisible() {
        def err = shouldFail MissingPropertyException, '''
            def m(Object o) {
                while (!(o instanceof String s)) {
                    return s
                }
                return 'out'
            }
            m(1)
        '''
        assert err.message =~ /No such property: s/

        err = shouldFail MissingPropertyException, '''
            def m(Object o) {
                while (!(o instanceof String s)) {
                    return 'in'
                }
                return s
            }
            m('hi')
        '''
        assert err.message =~ /No such property: s/
    }

    // GROOVY-12242: reuse the same pattern variable name in successive statements
    @Test
    void testVariableNameReuse() {
        Object a = 'x', b = 1
        def r = []
        if (a instanceof String s) r << s
        if (b instanceof Integer s) r << s
        assert r == ['x', 1]
    }

    // GROOVY-12242: type-checked flow scoping for early return
    @Test
    void testVariableScopeEarlyReturnTypeChecked() {
        def shell = GroovyShell.withConfig {
            ast groovy.transform.TypeChecked
        }
        assert shell.evaluate('''
            @groovy.transform.TypeChecked
            class C {
                static String m(Object o) {
                    if (!(o instanceof String s)) return 'early'
                    return s.toUpperCase()
                }
            }
            assert C.m('hi') == 'HI'
            assert C.m(1) == 'early'
            true
        ''')
    }

    // GROOVY-12242: type-checked — positive instanceof still not in else
    @Test
    void testVariableScopePositiveNotInElseTypeChecked() {
        def shell = GroovyShell.withConfig {
            ast groovy.transform.TypeChecked
        }
        def err = shouldFail shell, '''
            Number n = 12345
            if (n instanceof Integer i) {
            } else {
                i.toString()
            }
        '''
        assert err.message =~ /The variable .i. is undeclared/
    }

    // GROOVY-12242: type-checked — negated instanceof is in else
    @Test
    void testVariableScopeNegatedInElseTypeChecked() {
        def shell = GroovyShell.withConfig {
            ast groovy.transform.TypeChecked
        }
        assert shell.evaluate('''
            @groovy.transform.TypeChecked
            class C {
                static String m(Object o) {
                    if (!(o instanceof String s)) {
                        return 'not'
                    } else {
                        return s.toUpperCase()
                    }
                }
            }
            assert C.m('hi') == 'HI'
            assert C.m(1) == 'not'
            true
        ''')
    }

    // -------------------------------------------------------------------------
    // GROOVY-12242: systematic visibility matrix
    //
    // For each condition shape the test verifies:
    //   - if-block visibility
    //   - else-block visibility
    //   - after-if-else visibility (with and without abrupt completion)
    // -------------------------------------------------------------------------

    // --- (1) simple: o instanceof String s ---

    // pattern var in if-block (already covered by testVariable above);
    // here we also test: NOT in else-block, NOT after if-else (no abrupt branch)
    @Test
    void testSimpleInstanceof_notInElse_notAfterIf() {
        def err = shouldFail MissingPropertyException, '''
            class C {
                def m(Object o) {
                    if (o instanceof String s) { /* ok */ }
                    else { return s }
                }
            }
            new C().m(1)
        '''
        assert err.message =~ /No such property: s/

        err = shouldFail MissingPropertyException, '''
            class C {
                def m(Object o) {
                    if (o instanceof String s) { /* ok */ }
                    return s
                }
            }
            new C().m('x')
        '''
        assert err.message =~ /No such property: s/
    }

    // pattern var IS visible after if when else cannot complete normally
    @Test
    void testSimpleInstanceof_visibleAfterIf_whenElseAbrupt() {
        // else throws → s is visible after
        def f = { Object o ->
            if (o instanceof String s) {
                // matched
            } else {
                throw new IllegalArgumentException('not a string')
            }
            s.toUpperCase()
        }
        assert f('hello') == 'HELLO'
        try { f(1); assert false } catch (IllegalArgumentException ignored) {}
    }

    // --- (2) simple: !(o instanceof String s) ---

    // true-branch (the !instanceof branch) must NOT see s;
    // false-branch (else) MUST see s
    @Test
    void testNegatedInstanceof_truePathHides_falsePathBinds() {
        // else sees s (already covered by testVariableScopeNegatedElse)
        // here: if-branch does NOT see s
        def err = shouldFail MissingPropertyException, '''
            class C {
                def m(Object o) {
                    if (!(o instanceof String s)) {
                        return s
                    }
                    return 'ok'
                }
            }
            new C().m(1)
        '''
        assert err.message =~ /No such property: s/
    }

    // --- (3) !instanceof s, plus return in else block (→ s after if) ---
    // (already covered by testVariableScopeEarlyReturn / testVariableScopeNegatedElse;
    //  repeat here as an explicit cell in the matrix)
    @Test
    void testNegatedInstanceof_earlyReturnInTrue_visibleAfter() {
        def f = { Object o ->
            if (!(o instanceof String s)) return 'nope'
            s.toUpperCase()
        }
        assert f('hi') == 'HI'
        assert f(42) == 'nope'
    }

    // --- (4) o instanceof String s && cond ---

    // if-block: s visible; else-block: s NOT visible; after: NOT visible
    @Test
    void testAndChain_ifBlockVisible_elseNotVisible_afterNotVisible() {
        // if-block is visible (tested by testVariableAndChain and testVariableScope)
        // else-block: NOT visible
        def err = shouldFail MissingPropertyException, '''
            class C {
                def m(Object o) {
                    if (o instanceof String s && s.length() > 0) {
                        /* ok */
                    } else {
                        return s
                    }
                }
            }
            new C().m(1)
        '''
        assert err.message =~ /No such property: s/

        // after if: NOT visible (even if both branches complete normally)
        err = shouldFail MissingPropertyException, '''
            class C {
                def m(Object o) {
                    if (o instanceof String s && s.length() > 0) { /* ok */ }
                    return s
                }
            }
            new C().m('x')
        '''
        assert err.message =~ /No such property: s/
    }

    // --- (5) o instanceof String s || cond ---

    // JLS §6.3.1.2 (note): no rule for when-true of || — so VariableScopeVisitor does
    // NOT declare s in the if-block scope. Dynamic Groovy resolves the undeclared s as a
    // DynamicVariable, which yields MissingPropertyException at runtime (same as TypeChecked).
    // There is no need to use @TypeChecked here — the scope decision is made entirely by
    // VariableScopeVisitor (JLS §6.3.2.2-200-A: e.whenTrue is {} for this shape).
    @Test
    void testOrChain_noVisibilityInIfBlock() {
        // s must not be visible in the if-block when condition is instanceof s || ...
        def err = shouldFail MissingPropertyException, '''
            class C {
                def m(Object o) {
                    if (o instanceof String s || true) {
                        return s
                    }
                    return 'ok'
                }
            }
            new C().m('hello')
        '''
        assert err.message =~ /No such property: s/
    }

    // --- (6) !(o instanceof String s) && cond ---
    // De Morgan: ≡ (!instanceof s) && cond
    // JLS §6.3.1.3 + §6.3.1.1: !(o instanceof String s).whenTrue = {} (no when-false for instanceof),
    // && propagates no false-bindings, so the if-block (true path) does NOT see s.
    // VariableScopeVisitor declares nothing in the if-block → DynamicVariable → MissingPropertyException.
    @Test
    void testNegatedAndCond_noVisibilityInIfBlock() {
        def err = shouldFail MissingPropertyException, '''
            class C {
                def m(Object o) {
                    if (!(o instanceof String s) && true) {
                        return s
                    }
                    return 'out'
                }
            }
            new C().m(1)
        '''
        assert err.message =~ /No such property: s/
    }

    // --- (7) !(o instanceof String s) && cond, plus abrupt else-block ---
    // After the if: JLS §6.3.2.2-200-C-B: var introduced when true=?, S cannot complete normally, T can.
    // Here if-block true-path has no s binding (whenTrue={}), so even with abrupt else, s is NOT after.
    @Test
    void testNegatedAndCond_withElseReturn_noVisibilityAfter() {
        def err = shouldFail MissingPropertyException, '''
            class C {
                def m(Object o) {
                    if (!(o instanceof String s) && true) {
                        // true path: s not definitely bound
                    } else {
                        return 'out'
                    }
                    return s
                }
            }
            new C().m(1)
        '''
        assert err.message =~ /No such property: s/
    }

    // --- (NEW) !(o instanceof String s) with abrupt else-block only ---
    // missing case:
    //   if (!(o instanceof String s)) { println "not String" } else { println "String"; return }
    //   println s  // <-- must be INVALID
    //
    // JLS §6.3.2.2-200-C analysis:
    //   e = !(o instanceof String s): whenTrue={}, whenFalse={s}
    //   S = if-block (println "not String"): can complete normally
    //   T = else-block (println "String"; return): cannot complete normally
    //   C-A: e.whenTrue={} -> nothing even if T abrupt
    //   C-B: e.whenFalse={s}, S cannot complete normally? NO (S falls through) -> C-B does NOT apply
    //   -> s is NOT introduced after the if-else statement
    @Test
    void testNegatedInstanceof_abruptElseOnly_noVisibilityAfter() {
        def err = shouldFail MissingPropertyException, '''
            class C {
                def m(Object o) {
                    if (!(o instanceof String s)) {
                        println "not String"
                    } else {
                        println "String"
                        return s
                    }
                    return s  // s NOT in scope: if-block falls through, C-B does not apply
                }
            }
            new C().m(1)
        '''
        assert err.message =~ /No such property: s/
    }

    // --- (8) !(o instanceof String s) || cond ---
    // Equivalent to !s || cond. False path of || = both sides false → s is bound when
    // left is false (i.e. o instanceof String s) AND right is false.
    // So else-block sees s; after-if with abrupt if-block sees s.
    @Test
    void testNegatedOr_elseBlockSees_afterAbruptIfBlockSees() {
        // else-block: !(!(s)) || cond is false → !(o instanceof s) is false → s bound
        def f = { Object o ->
            if (!(o instanceof String s) || s.isEmpty()) {
                return 'branch-true'
            } else {
                // here s is definitely bound (the !instanceof was false, so instanceof matched)
                return 'has-s:' + s
            }
        }
        assert f('hello') == 'has-s:hello'   // !instanceof false, so else
        assert f('') == 'branch-true'         // !instanceof false but s.isEmpty true → if
        assert f(42) == 'branch-true'         // !instanceof true → if

        // after if with abrupt else (throw): s visible after
        def g = { Object o ->
            if (!(o instanceof String s) || s.isEmpty()) {
                /* fell through */
            } else {
                throw new IllegalStateException('non-empty string')
            }
            // s NOT visible here (if-block can complete normally without binding s)
        }
        g('') // no exception
        g(42) // no exception
    }

    // --- (9) !(o instanceof String s) || cond, plus return in else block ---
    // true-path completes normally → s NOT visible after if-else when else abruptly returns
    // (because the if-path does not guarantee s is bound)
    @Test
    void testNegatedOr_elseReturn_noVisibilityAfter() {
        def err = shouldFail MissingPropertyException, '''
            class C {
                def m(Object o) {
                    if (!(o instanceof String s) || s.isEmpty()) {
                        /* true branch: no guarantee s is bound */
                    } else {
                        return 'else'
                    }
                    return s
                }
            }
            new C().m('')
        '''
        assert err.message =~ /No such property: s/
    }

    // -------------------------------------------------------------------------
    // Additional De Morgan / compound cases
    // -------------------------------------------------------------------------

    // De Morgan: !(a && b) ≡ !a || !b
    // !(o instanceof String s && cond) — no binding anywhere (conservative)
    @Test
    void testDeMorgan_notAndNegation_noBinding() {
        def err = shouldFail MissingPropertyException, '''
            class C {
                def m(Object o) {
                    if (!(o instanceof String s && s.length() > 0)) {
                        return s
                    }
                    return 'ok'
                }
            }
            new C().m(1)
        '''
        assert err.message =~ /No such property: s/
    }

    // Double negation: !!(o instanceof String s) ≡ o instanceof String s
    @Test
    void testDoubleNegation_positiveBinding() {
        def f = { Object o ->
            if (!!(o instanceof String s)) {
                return s.toUpperCase()
            }
            return 'no'
        }
        assert f('ab') == 'AB'
        assert f(1)   == 'no'
    }

    // -------------------------------------------------------------------------
    // GROOVY-12242: redeclaration where pattern variable is / is not visible
    // -------------------------------------------------------------------------

    /**
     * Else-block of positive instanceof: s not in scope → fresh local is allowed
     * and correctly used at runtime (class generation must free the name).
     */
    @Test
    void testRedeclareInElse_wherePatternNotVisible_runtime() {
        def f = { Object o ->
            if (o instanceof String s) {
                return 'pat:' + s
            } else {
                def s = 'local'
                return s
            }
        }
        assert f('hi') == 'pat:hi'
        assert f(1) == 'local'
    }

    /**
     * After if with both branches falling through: s not in scope → fresh local OK.
     */
    @Test
    void testRedeclareAfterIf_wherePatternNotVisible_runtime() {
        def f = { Object o ->
            if (o instanceof String s) {
                // matched; s does not escape
            }
            def s = 99
            return s
        }
        assert f('x') == 99
        assert f(1) == 99
    }

    /**
     * True-branch of negated instanceof: s not in scope → fresh local OK.
     */
    @Test
    void testRedeclareInNegatedIf_wherePatternNotVisible_runtime() {
        def f = { Object o ->
            if (!(o instanceof String s)) {
                def s = 'shadow'
                return s
            } else {
                return 'pat:' + s
            }
        }
        assert f(1) == 'shadow'
        assert f('hi') == 'pat:hi'
    }

    /**
     * Nested: outer pattern s remains usable after an inner if that introduces i.
     */
    @Test
    void testNestedInstanceof_outerPatternStillVisible() {
        def f = { Object o, Object p ->
            if (o instanceof String s) {
                if (p instanceof Integer i) {
                    return s + ':' + i
                }
                return s + ':no-i'
            }
            return 'no-s'
        }
        assert f('ab', 3) == 'ab:3'
        assert f('ab', 'x') == 'ab:no-i'
        assert f(1, 3) == 'no-s'
    }

    // --- shouldNotCompile: cannot redeclare where pattern s is visible ---

    @Test
    void testShouldNotCompile_redeclareInIfBlock_wherePatternVisible() {
        def err = shouldFail MultipleCompilationErrorsException, '''
            def m(Object o) {
                if (o instanceof String s) {
                    def s = 'nope'
                }
            }
        '''
        assert err.message =~ /already contains a variable of the name s/
    }

    @Test
    void testShouldNotCompile_redeclareAfterEarlyReturn_wherePatternVisible() {
        def err = shouldFail MultipleCompilationErrorsException, '''
            def m(Object o) {
                if (!(o instanceof String s)) return
                def s = 'nope'
            }
        '''
        assert err.message =~ /already contains a variable of the name s/
    }

    @Test
    void testShouldNotCompile_redeclareAfterAbruptElse_wherePatternVisible() {
        def err = shouldFail MultipleCompilationErrorsException, '''
            def m(Object o) {
                if (o instanceof String s) {
                } else {
                    return
                }
                def s = 'nope'
            }
        '''
        assert err.message =~ /already contains a variable of the name s/
    }

    @Test
    void testShouldNotCompile_redeclareInElseOfNegated_wherePatternVisible() {
        def err = shouldFail MultipleCompilationErrorsException, '''
            def m(Object o) {
                if (!(o instanceof String s)) {
                } else {
                    def s = 'nope'
                }
            }
        '''
        assert err.message =~ /already contains a variable of the name s/
    }

    /**
     * Successive ifs reusing pattern name {@code s}: second condition must
     * re-bind the slot so else-path hide still frees the name for redeclaration.
     */
    @Test
    void testSuccessiveIfs_reusePatternName_redeclareInSecondElse() {
        def f = { Object a, Object b ->
            if (a instanceof String s) {
                // first binding
            }
            if (b instanceof Integer s) {
                return 'int:' + s
            } else {
                def s = 'local'
                return s
            }
        }
        assert f('x', 7) == 'int:7'
        assert f('x', 'y') == 'local'
        assert f(1, 7) == 'int:7'
        assert f(1, 'y') == 'local'
    }

    /**
     * Expression-statement pattern then a later if reusing the name — hide must
     * still apply (identity-based "introduced", not name-set diff).
     */
    @Test
    void testIsolatedPatternExpr_thenIfReuseName_redeclareInElse() {
        def f = { Object o ->
            (o instanceof String s) // isolated; must not leak
            if (o instanceof Integer s) {
                return 'int:' + s
            } else {
                def s = 'ok'
                return s
            }
        }
        assert f(5) == 'int:5'
        assert f('hi') == 'ok'
    }

    /**
     * After an early-return negated instanceof, survivor {@code s} remains usable
     * even when a prior isolated pattern used the same name.
     */
    @Test
    void testNameReuse_thenSurvivorAfterEarlyReturn() {
        def f = { Object o ->
            (o instanceof Number n) // different name; isolation
            if (!(o instanceof String s)) return 'early'
            return s.toUpperCase()
        }
        assert f('ab') == 'AB'
        assert f(1) == 'early'
    }
}
