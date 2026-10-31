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
package org.codehaus.groovy.classgen.asm.sc

import groovy.transform.stc.ConstructorsSTCTest
import org.junit.jupiter.api.Test

/**
 * Unit tests for static compilation: constructors.
 */
final class StaticCompileConstructorsTest extends ConstructorsSTCTest implements StaticCompilationTestSupport {

    @Test
    void testMapConstructorError() {
        assertScript '''
            class C {
                static void test() {
                    new Person(name:"First")
                    first(new Person(name:"First"))
                }
                static Person first(Person p) {
                    p
                }
            }
            @groovy.transform.Canonical
            class Person {
                String name
            }
            C.test()
        '''
    }

    // GROOVY-11843
    @Test
    void testMapConstructorOptimization() {
        assertScript '''
            class C {
                int i, j, k
            }
            new C(i:1, j:2, k:3)
        '''
        String script = astTrees.find{ it.key != 'C' }.value[1]
        Number offset = script.indexOf('run()Ljava/lang/Object;')
        String method = script.substring(offset, script.indexOf('\n' + System.lineSeparator(), offset) + 1)
        assert method.count('ACONST_NULL') == 0
    }

    // GROOVY-12568
    @Override
    @Test
    void testProtectedConstructorOfAnotherPackage() {
        assertScript '''package p
            class A {
                final String value
                protected A(String value) { this.value = value }
            }
            true
        '''
        shouldFailWithMessages '''package q
            class C extends p.A {
                C() { super('c') }
                static p.A make() {
                    new p.A('x') // only super(...) or an anonymous inner class can call it
                }
            }
        ''',
        'Cannot call protected constructor for p.A from class q.C'

        shouldFailWithMessages '''
            class C extends ObjectInputStream {
                static ObjectInputStream make() {
                    new ObjectInputStream() // only super() or an anonymous inner class can call it
                }
            }
        ''',
        'Cannot call protected constructor for java.io.ObjectInputStream from class C'
    }

    // GROOVY-12568
    @Test
    void testMapStyleConstructionWithProtectedConstructorOfAnotherPackage() {
        shouldFailWithMessages '''
            class C extends ObjectOutputStream {
                C() { super() }
                static ObjectOutputStream make() {
                    new ObjectOutputStream([:]) // map-style construction calls the protected ObjectOutputStream()
                }
            }
        ''',
        'Cannot call protected constructor for java.io.ObjectOutputStream from class C'
    }

    // GROOVY-12568
    @Test
    void testProtectedConstructorOfAnotherPackageIsCalledDirectlyThroughSuperOrAnonymousInnerClass() {
        testProtectedConstructorOfAnotherPackageThroughSuperOrAnonymousInnerClass()

        String c = astTrees['C'][1]
        assert c.contains('INVOKESPECIAL java/io/ObjectInputStream.<init> ()V')
        assert c.contains('NEW C$1')
        String aic = astTrees['C$1'][1]
        assert aic.contains('INVOKESPECIAL java/io/ObjectInputStream.<init> ()V')
        assert !(c + aic).contains('ScriptBytecodeAdapter.invokeNew')
    }

    // GROOVY-12453
    @Test
    void testMapConstructorWithinPowerAssert() {
        assertScript '''
            class Person {
                def name, age
            }
            def fred = 'fred', n = 25
            assert new Person([name: fred, age: n])
            assert new Person(name: fred, age: n).name == 'fred'
            assert Objects.equals(fred, new Person(name: fred, age: n).name) // operand already on stack

            def err = null
            try {
                assert new Person(name: fred, age: n).age == 26
            } catch (AssertionError e) {
                err = e
            }
            def lines = err.message.normalize().readLines()*.stripTrailing()
            int i = lines.indexOf('assert new Person(name: fred, age: n).age == 26')
            assert i >= 0
            assert lines[i + 1] == '                        |          |  |'
            assert lines[i + 2] == "                        'fred'     25 25"
        '''
    }

    @Test
    void testPrivateConstructorFromClosure() {
        assertScript '''
            class C {
                String s
                private C(String s) {
                    this.s = s
                }
                static C make(String s) {
                    def cl = { new C(s) }
                    cl()
                }
            }
            assert C.make('pls').s == 'pls'
        '''
    }

    @Test
    void testPrivateConstructorFromStaticInnerClass() {
        assertScript '''
            class Foo {
                String s
                private Foo(String s) {
                    this.s = s
                }
                static class Bar {
                    static Foo makeFoo(String s) { new Foo(s) }
                }
            }
            assert Foo.Bar.makeFoo('pls').s == 'pls'
        '''
        String bar = astTrees['Foo$Bar'][1]
        assert bar.contains('NEW Foo')
        assert bar.contains('INVOKESPECIAL Foo.<init> (Ljava/lang/String;)V')
    }

    @Test
    void testPrivateConstructorFromAnonymousInnerClass() {
        assertScript '''
            class Foo {
                String s
                private Foo(String s) {
                    this.s = s
                }
                static Foo makeFoo(String s) {
                    new Object() {
                        Foo makeFoo(String x) {
                            new Foo(x)
                        }
                    }.makeFoo(s)
                }
            }
            assert Foo.makeFoo('pls').s == 'pls'
        '''
    }

    // GROOVY-11119, GROOVY-11451
    @Override @Test
    void testMapStyleConstructorWithOverloadedSetterName() {
        super.testMapStyleConstructorWithOverloadedSetterName()
        String  asserts = astTrees.find{ it.key != 'C' }.value[1]
        assert  asserts.contains('INVOKEVIRTUAL C.setP (Ljava/lang/String;)V')
        assert  asserts.contains('INVOKEVIRTUAL C.setP (Ljava/util/regex/Pattern;)V')
        assert !asserts.contains('INVOKESTATIC org/codehaus/groovy/runtime/ScriptBytecodeAdapter.setGroovyObjectProperty')
    }

    // GROOVY-11956, GROOVY-12290: testMapStyleConstructorWithInaccessibleSetter is
    // inherited: the read-only property error is now reported by the type checker,
    // identically for @TypeChecked and @CompileStatic
}
