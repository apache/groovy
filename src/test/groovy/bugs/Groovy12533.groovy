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

import groovy.lang.MissingFieldException
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * A static nested subclass can read a private field of its outer class with
 * {@code this.@} and {@code super.@}. The same expressions inside a closure
 * or lambda must read that field too.
 */
final class Groovy12533 {

    @Test
    void testMethodBodyReadsPrivateOuterField() {
        assertScript '''
            class Outer {
                private String f = 'outer'
                static class Sub extends Outer {
                    def direct() { [this.@f, super.@f] }
                }
            }
            assert new Outer.Sub().direct() == ['outer', 'outer']
        '''
    }

    @Test
    void testClosureAndLambdaReadPrivateOuterField() {
        assertScript '''
            class Outer {
                private String f = 'outer'
                static class Sub extends Outer {
                    def viaClosure() { [{ -> this.@f }(), { -> super.@f }()] }
                    def viaLambda() { [(() -> this.@f)(), (() -> super.@f)()] }
                    def nested() { { -> { -> [this.@f, super.@f] }() }() }
                    def write(String value) {
                        def setter = { -> this.@f = value }
                        setter.call()
                        def getter = { -> super.@f }
                        getter.call()
                    }
                }
            }
            def sub = new Outer.Sub()
            assert sub.viaClosure() == ['outer', 'outer']
            assert sub.viaLambda() == ['outer', 'outer']
            assert sub.nested() == ['outer', 'outer']
            assert sub.write('updated') == 'updated'
            assert sub.viaClosure() == ['updated', 'updated']
        '''
    }

    @Test
    void testCompileStaticMethodBodyReadsPrivateOuterField() {
        assertScript '''
            class Outer {
                private String f = 'outer'
                @groovy.transform.CompileStatic
                static class Sub extends Outer {
                    def direct() { [this.@f, super.@f] }
                }
            }
            assert new Outer.Sub().direct() == ['outer', 'outer']
        '''
    }

    @Test
    void testCompileStaticClosureAndLambdaReadPrivateOuterField() {
        assertScript '''
            class Outer {
                private String f = 'outer'
                @groovy.transform.CompileStatic
                static class Sub extends Outer {
                    def direct() { [this.@f, super.@f] }
                    def viaClosure() { [{ -> this.@f }(), { -> super.@f }()] }
                    def viaLambda() { [(() -> this.@f)(), (() -> super.@f)()] }
                    def write(String value) {
                        def setter = { -> this.@f = value }
                        setter.call()
                        def getter = { -> super.@f }
                        getter.call()
                    }
                }
            }
            def sub = new Outer.Sub()
            assert sub.direct() == ['outer', 'outer']
            assert sub.viaClosure() == ['outer', 'outer']
            assert sub.viaLambda() == ['outer', 'outer']
            assert sub.write('updated') == 'updated'
            assert sub.viaClosure() == ['updated', 'updated']
        '''
    }

    // this.name and a bare name resolve to the enclosing instance, as in dynamic code;
    // this.@name is the field of this instance
    @Test
    void testInnerSubclassReadsEnclosingInstanceByNameAndItsOwnFieldByAttribute() {
        for (String annotation : ['', '@groovy.transform.CompileStatic']) {
            assertScript """
                $annotation
                class Outer {
                    private String f = 'enclosing'
                    class Sub extends Outer {
                        def byName() { [this.f, f, { -> this.f }(), { -> f }()] }
                        def byAttribute() { [this.@f, { -> this.@f }()] }
                    }
                    void mutate() { this.@f = 'changed' }
                }
                def outer = new Outer()
                def sub = outer.new Outer.Sub()
                outer.mutate()
                assert sub.byName() == ['changed'] * 4
                assert sub.byAttribute() == ['enclosing'] * 2
            """
        }
    }

    // a static nested subclass has the field but no enclosing instance
    @Test
    void testCompileStaticStaticNestedSubclassReadsItsOwnFieldByName() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Outer {
                private String f = 'outer'
                static class Sub extends Outer {
                    def byName() { [this.f, f, { -> this.f }(), { -> f }()] }
                    def write(String value) { f = value; this.f }
                }
            }
            def sub = new Outer.Sub()
            assert sub.byName() == ['outer'] * 4
            assert sub.write('updated') == 'updated'
            assert sub.byName() == ['updated'] * 4
        '''
    }

    @Test
    void testInnerSubclassClosureReadsItsOwnInheritedField() {
        assertScript '''
            class Outer {
                private String f = 'enclosing'
                class Sub extends Outer {
                    def own() { { -> this.@f }() }
                    def enclosing() { { -> Outer.this.@f }() }
                }
                void mutate() { this.@f = 'changed' }
            }
            def outer = new Outer()
            def sub = outer.new Outer.Sub()
            assert sub.own() == 'enclosing'
            assert sub.enclosing() == 'enclosing'
            outer.mutate()
            assert sub.enclosing() == 'changed'
            assert sub.own() == 'enclosing'
        '''
    }

    @Test
    void testCompileStaticInnerSubclassClosureReadsItsOwnInheritedField() {
        assertScript '''
            class Outer {
                private String f = 'enclosing'
                @groovy.transform.CompileStatic
                class Sub extends Outer {
                    def own() { { -> this.@f }() }
                    def enclosing() { { -> Outer.this.@f }() }
                }
                void mutate() { this.@f = 'changed' }
            }
            def outer = new Outer()
            def sub = outer.new Outer.Sub()
            assert sub.own() == 'enclosing'
            assert sub.enclosing() == 'enclosing'
            outer.mutate()
            assert sub.enclosing() == 'changed'
            assert sub.own() == 'enclosing'
        '''
    }

    @Test
    void testDeclaringClassClosureStillReadsItsPrivateField() {
        assertScript '''
            class C {
                private String f = 'c'
                def viaClosure() { { -> this.@f }() }
                @groovy.transform.CompileStatic
                def viaStatic() { { -> this.@f }() }
            }
            def c = new C()
            assert c.viaClosure() == 'c'
            assert c.viaStatic() == 'c'
        '''
    }

    @Test
    void testClosureReadsAndWritesPrivateStaticSuperField() {
        assertScript '''
            class Outer {
                private static String f = 'outer'
                static class Sub extends Outer {
                    def direct() { [this.@f, super.@f] }
                    def viaClosure() { [{ -> this.@f }(), { -> super.@f }()] }
                    def write(String value) {
                        def setter = { -> this.@f = value }
                        setter.call()
                        def getter = { -> super.@f }
                        getter.call()
                    }
                }
            }
            def sub = new Outer.Sub()
            assert sub.direct() == ['outer', 'outer']
            assert sub.viaClosure() == ['outer', 'outer']
            assert sub.write('updated') == 'updated'
            assert new Outer.Sub().viaClosure() == ['updated', 'updated']
        '''
    }

    @Test
    void testCompileStaticClosureReadsAndWritesPrivateStaticSuperField() {
        assertScript '''
            class Outer {
                private static String f = 'outer'
                @groovy.transform.CompileStatic
                static class Sub extends Outer {
                    def direct() { [this.@f, super.@f] }
                    def viaClosure() { [{ -> this.@f }(), { -> super.@f }()] }
                    def write(String value) {
                        def setter = { -> this.@f = value }
                        setter.call()
                        def getter = { -> super.@f }
                        getter.call()
                    }
                }
            }
            def sub = new Outer.Sub()
            assert sub.direct() == ['outer', 'outer']
            assert sub.viaClosure() == ['outer', 'outer']
            assert sub.write('updated') == 'updated'
            assert new Outer.Sub().viaClosure() == ['updated', 'updated']
        '''
    }

    @Test
    void testCompileStaticGrandchildClosureReadsPrivateField() {
        assertScript '''
            class Outer {
                private String f = 'outer'
                static class Mid extends Outer {
                    @groovy.transform.CompileStatic
                    static class Sub extends Mid {
                        def direct() { this.@f }
                        def viaClosure() { { -> this.@f }() }
                    }
                }
            }
            def sub = new Outer.Mid.Sub()
            assert sub.direct() == 'outer'
            assert sub.viaClosure() == 'outer'
        '''
    }

    @Test
    void testNonNestedSubclassClosureCannotReadPrivateSuperField() {
        def err = shouldFail MissingFieldException, '''
            class A {
                private String f = 'hidden'
            }
            class B extends A {
                def viaClosure() { { -> this.@f }() }
            }
            new B().viaClosure()
        '''
        assert err.message.contains('f')
    }
}
