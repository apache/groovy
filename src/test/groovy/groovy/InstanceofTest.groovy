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

import static groovy.test.GroovyAssert.assertScript
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

    // GEP-19: record patterns in instanceof

    @Test
    void testRecordPattern() {
        assertScript '''
            record Point(int x, int y) {}
            def p = new Point(3, 4)
            if (p instanceof Point(int x, int y)) {
                assert x == 3 && y == 4
            } else {
                assert false : 'expected match'
            }
            assert !('s' instanceof Point(int x, int y))
        '''
    }

    @Test
    void testRecordPatternInCondition() {
        assertScript '''
            record Point(int x, int y) {}
            def p = new Point(3, 4)
            assert p instanceof Point(int x, _) && x == 3
            def r = p instanceof Point(var a, var b) ? a + b : -1
            assert r == 7
        '''
    }

    @Test
    void testNestedRecordPattern() {
        assertScript '''
            record Point(int x, int y) {}
            record Line(Point start, Point end) {}
            def l = new Line(new Point(0, 1), new Point(4, 5))
            if (l instanceof Line(Point(_, var y1), Point p2)) {
                assert y1 == 1
                assert p2.x() == 4
            } else {
                assert false : 'expected match'
            }
        '''
    }

    @Test
    void testRecordPatternComponentTypeAndArity() {
        assertScript '''
            record Box(Object value) {}
            assert new Box('t') instanceof Box(String s) && s == 't'
            assert !(new Box(42) instanceof Box(String s2))
            record Point(int x, int y) {}
            assert !(new Point(1, 2) instanceof Point(var a))       // arity mismatch
        '''
    }

    @Test
    void testRecordPatternVarBindsNullComponent() {
        assertScript '''
            record Box(Object value) {}
            def b = new Box(null)
            if (b instanceof Box(var v)) {
                assert v == null
            } else {
                assert false : 'var component should match null'
            }
            assert !(b instanceof Box(String s)) // typed component does not match null
        '''
    }

    @Test
    void testRecordPatternInWhileLoop() {
        assertScript '''
            record Cons(Object head, Object tail) {}
            def list = new Cons(1, new Cons(2, new Cons(3, null)))
            def sum = 0
            while (list instanceof Cons(Integer h, var t)) {
                sum += h
                list = t
            }
            assert sum == 6
        '''
    }

    @Test
    void testRecordPatternCompileStatic() {
        assertScript '''
            import groovy.transform.CompileStatic
            record Point(int x, int y) {}
            @CompileStatic
            def m(Object o) {
                if (o instanceof Point(int x, int y)) {
                    return x + y
                }
                return -1
            }
            assert m(new Point(3, 4)) == 7
            assert m('s') == -1
        '''
    }
}
