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

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * Qualified {@code X.super.m()} is legal only when {@code X} is a direct
 * superinterface of the current type, or an enclosing class. javac rejects
 * every other qualifier.
 */
final class Groovy12526 {

    private static final String NOT_DIRECT = 'is not a direct superinterface'

    @Test
    void testInterfaceSuperOnItselfIsRejectedInEveryMode() {
        def body = '''
            interface J {
                default String m() { 'J' }
            }
            interface I extends J {
                default String m() { 'I' }
                default String n() { I.super.m() }
            }
            class C implements I {}
            new C().n()
        '''
        assertRejectedInEveryMode(body, "'I' is not a direct superinterface")
    }

    @Test
    void testEnclosingInterfaceSuperFromNonImplementingInnerIsRejectedInEveryMode() {
        def body = '''
            interface Outer {
                default String m() { 'outer' }
                class Inner {
                    String test() { Outer.super.m() }
                }
            }
            new Outer.Inner().test()
        '''
        assertRejectedInEveryMode(body, "'Outer' is not a direct superinterface")
    }

    @Test
    void testIndirectSuperinterfaceIsRejectedInEveryMode() {
        def body = '''
            interface J {
                default String m() { 'J' }
            }
            interface I extends J {}
            class C implements I {
                String n() { J.super.m() }
            }
            new C().n()
        '''
        assertRejectedInEveryMode(body, "'J' is not a direct superinterface")
    }

    @Test
    void testSuperinterfaceInheritedOnlyFromSuperclassIsRejectedInEveryMode() {
        def body = '''
            interface I {
                default String m() { 'I' }
            }
            class B implements I {}
            class C extends B {
                String n() { I.super.m() }
            }
            new C().n()
        '''
        assertRejectedInEveryMode(body, "'I' is not a direct superinterface")
    }

    @Test
    void testDirectSuperinterfaceCallStillReachesTheInterfaceDefault() {
        def body = '''
            interface I {
                default String m() { 'I' }
            }
            class C implements I {
                @Override String m() { 'C' }
                String n() { I.super.m() }
            }
            assert new C().n() == 'I'
        '''
        assertRunsInEveryMode(body)
    }

    @Test
    void testSuperinterfaceOfTheCurrentInterfaceStillWorks() {
        def body = '''
            interface J {
                default String m() { 'J' }
            }
            interface I extends J {
                default String m() { 'I' }
                default String n() { J.super.m() }
            }
            class C implements I {}
            assert new C().n() == 'J'
        '''
        assertRunsInEveryMode(body)
    }

    @Test
    void testEnclosingClassSuperStillWorks() {
        assertScript '''
            class C {
                String value() { 'C' }
                static class D extends C {
                    @Override String value() { 'D' }
                    class B {
                        String m() { D.super.value() }
                    }
                }
            }
            def d = new C.D()
            assert new C.D.B(d).m() == 'C'
        '''
    }

    @Test
    void testAnonymousImplementationMayCallDirectSuperinterface() {
        def body = '''
            interface I {
                default String m() { 'I' }
            }
            class C {
                String n() {
                    new I() {
                        @Override String m() { I.super.m() }
                    }.m()
                }
            }
            assert new C().n() == 'I'
        '''
        assertRunsInEveryMode(body)
    }

    @Test
    void testAnonymousClassOfAClassCannotQualifyAnInterfaceSuper() {
        def body = '''
            interface I {
                default String m() { 'I' }
            }
            class C {
                String n() {
                    new Object() {
                        String call() { I.super.m() }
                    }.call()
                }
            }
        '''
        assertRejectedInEveryMode(body, "'I' is not a direct superinterface")
    }

    @Test
    void testAnonymousImplementationCannotCallADifferentInterface() {
        def body = '''
            interface J {
                default String m() { 'J' }
            }
            interface I {
                default String m() { 'I' }
            }
            class C {
                String n() {
                    new I() {
                        @Override String m() { J.super.m() }
                    }.m()
                }
            }
        '''
        assertRejectedInEveryMode(body, "'J' is not a direct superinterface")
    }

    @Test
    void testQualifiedThisIsNotTheInterfaceSuperCheck() {
        def err = shouldFail '''
            class A {
                def foo() { A.this.class }
            }
        '''
        assert err.message.contains('only allowed in nested/inner classes') : err.message
        assert !err.message.contains(NOT_DIRECT) : err.message
    }

    @Test
    void testSuperOnAValueIsNotAQualifiedInterfaceSuper() {
        def err = shouldFail '''
            class C {
                def m(Object o) { o.super }
            }
            new C().m(new Object())
        '''
        assert err.message.contains('No such property: super') : err.message
        assert !err.message.contains(NOT_DIRECT) : err.message
    }

    @Test
    void testTraitSuperOnTheDeclaringTraitStillWorks() {
        assertScript '''
            trait T {
                String m() { 'T' }
                String n() { T.super.m() }
            }
            class C implements T {}
            assert new C().n() == 'T'
        '''
    }

    @Test
    void testEnclosingClassSuperIsOutsideTheInterfaceCheck() {
        def body = '''
            class C {
                String value() { 'C' }
                static class D extends C {
                    @Override String value() { 'D' }
                    class B {
                        String m() { D.super.value() }
                    }
                }
            }
            def d = new C.D()
            assert new C.D.B(d).m() == 'C'
        '''
        for (String annotation : ['', '@groovy.transform.TypeChecked ', '@groovy.transform.CompileStatic ']) {
            assertScript(annotation + body)
        }
    }

    @Test
    void testInterfaceMarkedAsTraitSkipsTheDirectSuperinterfaceCheck() {
        def err = shouldFail '''
            @groovy.transform.Trait
            interface T {
                default String m() { 'T' }
                default String n() { T.super.m() }
            }
        '''
        assert err.message.contains('@Trait not allowed for interfaces') : err.message
        assert !err.message.contains(NOT_DIRECT) : err.message
    }

    @Test
    void testImplementingInnerClassMayCallEnclosingInterfaceSuper() {
        def body = '''
            interface Outer {
                default String m() { 'outer' }
                class Inner implements Outer {
                    @Override String m() { 'inner' }
                    String test() { Outer.super.m() }
                }
            }
            assert new Outer.Inner().test() == 'outer'
        '''
        assertRunsInEveryMode(body)
    }

    private static void assertRejectedInEveryMode(String body, String snippet) {
        for (String script : modes(body)) {
            def err = shouldFail(script)
            assert err.message.contains(NOT_DIRECT) : err.message
            assert err.message.contains(snippet) : err.message
            assert !err.message.contains('No such property: super') : err.message
        }
    }

    private static void assertRunsInEveryMode(String body) {
        for (String script : modes(body)) {
            assertScript(script)
        }
    }

    /** Dynamic, {@code @TypeChecked}, and {@code @CompileStatic} forms of the same declarations. */
    private static List<String> modes(String body) {
        ['', '@groovy.transform.TypeChecked ', '@groovy.transform.CompileStatic '].collect { annotation ->
            if (!annotation) {
                body
            } else {
                body.replace('interface ', annotation + 'interface ').replace('class ', annotation + 'class ')
            }
        }
    }
}
