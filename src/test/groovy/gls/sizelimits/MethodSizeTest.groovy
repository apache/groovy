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
package gls.sizelimits

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * Large methods whose stack map frames are expensive to compute must still compile.
 * Each generated method is roughly two thirds of the way to the 64 KB code limit.
 */
final class MethodSizeTest {

    private static String staticClass(String returnType, String body) {
        """
        import groovy.transform.CompileStatic
        @CompileStatic
        class C {
            $returnType m(int x) {
                $body
            }
        }
        new C().m(3)
        """
    }

    @Test
    void testDynamicLocalsAndBranches() {
        def body = (1..1000).collect { i ->
            "def v$i = v${i - 1}\nif (x > $i) { v$i = v${i - 1} + $i } else { v$i = v${i.intdiv(2)} }"
        }.join('\n')
        assertScript """
            class C {
                def m(int x) {
                    def v0 = x
                    $body
                    v1000
                }
            }
            assert new C().m(3) != null
        """
    }

    private static String staticLocalsAndBranches(int n) {
        def body = (1..n).collect { i ->
            "int v$i = v${i - 1}\nif (x > $i) { v$i = v${i - 1} + $i } else { v$i = v${i.intdiv(2)} }"
        }.join('\n')
        staticClass('int', "int v0 = x\n$body\nv$n")
    }

    @Test
    void testStaticLocalsAndBranches() {
        assertScript staticLocalsAndBranches(1000)
    }


    @Test
    void testStaticLocalsOfMixedTypes() {
        def types = [String: '"s"', Integer: '1', Long: '1L', List: '[]', Map: '[:]', Object: 'null']
        def body = (0..<1200).collect { i ->
            def type = types.keySet()[i % types.size()]
            "$type a$i = ${types[type]}\nObject o$i = (x > $i) ? (Object) a$i : (Object) Integer.valueOf($i)"
        }.join('\n')
        assertScript staticClass('Object', "$body\no1199")
    }

    @Test
    void testClosures() {
        def body = (0..<750).collect { i ->
            "def v$i = x + $i\ndef c$i = { -> acc += v$i }\nif (x > $i) c$i()"
        }.join('\n')
        assertScript """
            class C {
                def m(int x) {
                    def acc = 0
                    $body
                    acc
                }
            }
            assert new C().m(3) == 3 + 4 + 5
        """
    }

    @Test
    void testTryCatchFinally() {
        def body = (0..<550).collect { i ->
            "int v$i = x\ntry { r += v${i}.intdiv(x - $i) } catch (ArithmeticException e) { r -= v$i } finally { r++ }"
        }.join('\n')
        assertScript staticClass('int', "int r = 0\n$body\nr")
    }

    @Test
    void testDynamicSwitch() {
        def cases = (0..<1800).collect { i -> "case $i: r = $i + x; break" }.join('\n')
        assertScript """
            class C {
                def m(x) {
                    def r = 0
                    switch (x) {
                        $cases
                        default: r = -1
                    }
                    r
                }
            }
            assert new C().m(3) == 6
        """
    }

    @Test
    void testStaticSwitch() {
        def locals = (0..<1800).collect { i -> "int v$i = x + $i" }.join('\n')
        def cases = (0..<1800).collect { i -> "case $i: r = v$i; break" }.join('\n')
        assertScript staticClass('int', "int r = 0\n$locals\nswitch (x) {\n$cases\ndefault: r = -1\n}\nr")
    }

    @Test
    void testStaticInitializer() {
        def fields = (0..<1400).collect { i -> "static final List F$i = [$i, '$i', ${i}L]" }.join('\n')
        assertScript """
            class C {
                $fields
            }
            assert C.F1399 == [1399, '1399', 1399L]
        """
    }

    @Test
    void testScriptBody() {
        assertScript((0..<700).collect { i ->
            "def v$i = binding.hasVariable('q') ? $i : null\nif (v$i) { println v$i } else { v$i = ${i}G }"
        }.join('\n'))
    }
}
