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

    // GROOVY-11585
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
}
