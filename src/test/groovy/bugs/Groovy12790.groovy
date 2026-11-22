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

import org.junit.jupiter.api.Test

import java.lang.reflect.Modifier

final class Groovy12790 {

    private static Object instance(String text) {
        def ctor = new GroovyClassLoader().parseClass(text, 'M.groovy').getDeclaredConstructor()
        ctor.accessible = true // the class of a script with an instance main is not public
        ctor.newInstance()
    }

    @Test
    void testNonLiteralRightHandSideWithInstanceMain() {
        def m = instance '''
            def (a, b) = "xy".toList()
            def c = 3
            void main() {}
        '''
        assert m.a == 'x'
        assert m.b == 'y'
        assert m.c == 3
    }

    @Test
    void testLaterDeclarationUsesTheAssignedFields() {
        def m = instance '''
            def x = 1
            def (a, b) = "xy".toList()
            def c = a + b + x
            void main() {}
        '''
        assert m.c == 'xy1'
    }

    @Test
    void testNonLiteralRightHandSideWithRunMethod() {
        assert new GroovyShell().evaluate('''
            def (a, b) = "xy".toList()
            def c = 3
            def run() { "$a$b$c" }
        ''') == 'xy3'
    }

    @Test
    void testRestBinding() {
        def m = instance '''
            def (h, *t) = [1, 2, 3]
            void main() {}
        '''
        assert m.h == 1
        assert m.t == [2, 3]
    }

    @Test
    void testMapStyle() {
        def m = instance '''
            def (name: n, age: a) = [name: 'Ada', age: 36]
            void main() {}
        '''
        assert m.n == 'Ada'
        assert m.a == 36
    }

    @Test
    void testTypedTargets() {
        def m = instance '''
            def (int x, int y) = "12".toList()*.toInteger()
            void main() {}
        '''
        assert m.x == 1
        assert m.y == 2
    }

    @Test
    void testFinalTargets() {
        def m = instance '''
            def (val a, b) = "xy".toList()
            final c = a + b
            void main() {}
        '''
        assert m.a == 'x'
        assert m.c == 'xy'
        assert Modifier.isFinal(m.class.getDeclaredField('a').modifiers)
        assert Modifier.isFinal(m.class.getDeclaredField('c').modifiers)
    }

    @Test
    void testCompileStaticMain() {
        def m = instance '''
            def (String a, String b) = "xy".toList()
            def c = a + b

            @groovy.transform.CompileStatic
            void main() {}
        '''
        assert m.c == 'xy'
    }

    @Test
    void testSecondMultipleAssignment() {
        def m = instance '''
            def (a, b) = "xy".toList()
            def (c, d) = [b, a]
            void main() {}
        '''
        assert [m.a, m.b, m.c, m.d] == ['x', 'y', 'y', 'x']
    }

    @Test
    void testRightHandSideEvaluatedOnce() {
        def m = instance '''
            def calls = 0
            def (a, b) = { calls++; [1, 2] }()
            void main() {}
        '''
        assert m.calls == 1
        assert [m.a, m.b] == [1, 2]
    }
}
