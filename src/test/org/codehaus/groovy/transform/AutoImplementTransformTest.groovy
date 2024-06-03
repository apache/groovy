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
package org.codehaus.groovy.transform

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class AutoImplementTransformTest {

    private final GroovyShell shell = new GroovyShell(new CompilerConfiguration().
        addCompilationCustomizers(new ImportCustomizer().tap { addImports('groovy.transform.AutoImplement') })
    )

    @Test
    void testException() {
        shouldFail shell, UnsupportedOperationException, '''
            @AutoImplement(exception=UnsupportedOperationException)
            class Foo implements Iterator<String> { }

            new Foo().hasNext()
        '''
    }

    @Test
    void testExceptionWithMessage() {
        def err = shouldFail shell, UnsupportedOperationException, '''
            @AutoImplement(exception=UnsupportedOperationException, message='Not supported by Foo')
            class Foo implements Iterator<String> { }

            new Foo().hasNext()
        '''
        assert err.message.contains('Not supported by Foo')
    }

    @Test
    void testClosureBody() {
        shouldFail shell, IllegalStateException, '''
            @AutoImplement(code={ throw new IllegalStateException() })
            class Foo implements Iterator<String> { }

            new Foo().hasNext()
        '''
    }

    @Test
    void testInheritedMethodNotOverwritten() {
        assertScript shell, '''
            class WithNext {
                String next() { 'foo' }
            }

            @AutoImplement
            class Foo extends WithNext implements Iterator<String> { }

            assert new Foo().next() == 'foo'
        '''
    }

    @Test
    void testExistingMethodNotOverwritten() {
        assertScript shell, '''
            @AutoImplement
            class Foo implements Iterator<String> {
                String next() { 'foo' }
            }

            assert new Foo().next() == 'foo'
        '''
    }

    // GROOVY-9816
    @Test
    void testPropertyMethodsNotOverwritten() {
        assertScript shell, '''
            interface Bar {
                def getBaz(); void setBaz(baz)
            }

            @AutoImplement
            class Foo implements Bar {
                def baz
            }

            def foo = new Foo(baz: 123)
            assert foo.baz == 123
        '''

        assertScript shell, '''
            interface Bar {
                def getBaz(); void setBaz(baz)
            }

            @AutoImplement
            class Foo implements Bar {
                final baz = 123
            }

            // setter is independent of constant
            def foo = new Foo(baz: 456)
            assert foo.baz == 123
        '''

        assertScript shell, '''
            interface Bar {
                boolean getBaz(); boolean isBaz()
            }

            @AutoImplement
            class Foo implements Bar {
                boolean baz
            }

            def foo = new Foo(baz: true)
            assert foo.getBaz()
            assert foo.isBaz()
            assert foo.baz
        '''

        assertScript shell, '''
            interface Bar {
                boolean getBaz(); boolean isBaz()
            }

            @AutoImplement
            class Foo implements Bar {
                boolean baz
                boolean getBaz() { baz }
            }

            def foo = new Foo(baz: true)
            assert foo.getBaz()
            assert foo.isBaz()
            assert foo.baz
        '''

        assertScript shell, '''
            interface Bar {
                boolean getBaz(); boolean isBaz()
            }

            @AutoImplement
            class Foo implements Bar {
                boolean baz
                boolean isBaz() { baz }
            }

            def foo = new Foo(baz: true)
            assert foo.getBaz()
            assert foo.isBaz()
            assert foo.baz
        '''
    }

    @Test
    void testVoidReturnType() {
        assertScript shell, '''
            interface Bar {
                void baz()
            }

            @AutoImplement
            class Foo implements Bar { }

            new Foo().baz() // no value to assert
        '''
    }

    @Test
    void testGenericReturnTypes() {
        assertScript shell, '''
            interface HasXs<T> {
                T[] x()
            }

            abstract class HasXsY<E> implements HasXs<Long> {
                abstract E y()
            }

            interface MyIt<T> extends Iterator<T> { }

            @AutoImplement
            class Foo extends HasXsY<Integer> implements MyIt<String> { }

            def publicMethods = Foo.methods.findAll{ it.modifiers == 1 }.collect{ "$it.returnType.simpleName $it.name" }*.toString()
            assert ['boolean hasNext', 'String next', 'Long[] x', 'Integer y'].every{ publicMethods.contains(it) }
            '''
    }

    // GROOVY-8270
    @Test
    void testGenericParameterTypes() {
        assertScript shell, '''
            @AutoImplement
            class Foo implements Comparator<String> { }
            // Can't have an abstract method in a non-abstract class. The class 'Foo' must be declared
            // abstract or the method 'int compare(java.lang.Object, java.lang.Object)' must be implemented.

            assert new Foo().compare('bar', 'baz') == 0
            '''
    }

    // GROOVY-10472
    @Test
    void testCovariantReturnTypes() {
        assertScript shell, '''
            interface Super { List findAll() }
            interface Sub extends Super { Iterable findAll() }

            @AutoImplement
            class ThisClassFails implements Sub{}

            assert !(new ThisClassFails().findAll())
        '''

        assertScript shell, '''
            interface Super { ArrayList findAll() }
            interface Sub extends Super { Iterable findAll() }

            @AutoImplement
            class ThisClassFails implements Sub{}

            assert !(new ThisClassFails().findAll())
        '''

        assertScript shell, '''
            interface Super { Iterable findAll() }
            interface Sub extends Super { List findAll() }

            @AutoImplement
            class ThisClassFails implements Sub{}

            assert !(new ThisClassFails().findAll())
        '''

        assertScript shell, '''
            interface Super { Iterable findAll() }
            interface Sub extends Super { ArrayList findAll() }

            @AutoImplement
            class ThisClassFails implements Sub{}

            assert !(new ThisClassFails().findAll())
        '''

        assertScript shell, '''
            interface Super { AbstractList findAll() }
            interface Sub extends Super { List findAll() }

            @AutoImplement
            class ThisClassFails implements Sub{}

            assert !(new ThisClassFails().findAll())
        '''

        assertScript shell, '''
            interface Super { List findAll() }
            interface Sub extends Super { AbstractList findAll() }

            @AutoImplement
            class ThisClassFails implements Sub{}

            assert !(new ThisClassFails().findAll())
        '''

        assertScript shell, '''
            interface Super { AbstractList findAll() }
            interface Sub extends Super { ArrayList findAll() }

            @AutoImplement
            class ThisClassFails implements Sub{}

            assert !(new ThisClassFails().findAll())
        '''

        assertScript shell, '''
            interface Super { ArrayList findAll() }
            interface Sub extends Super { AbstractList findAll() }

            @AutoImplement
            class ThisClassFails implements Sub{}

            assert !(new ThisClassFails().findAll())
        '''
    }

    // GROOVY-10552
    @Test
    void testMethodWithTypeParameter() {
        assertScript shell, '''
            interface I {
                def <T> T m(List<T> list)
            }

            @AutoImplement
            class C implements I {
            }

            Object result = new C().m([])
            assert result == null
        '''

        assertScript shell, '''
            @AutoImplement
            class C implements java.sql.Connection {
            }

            new C().commit()
        '''
    }

    // GROOVY-11339
    @Test
    void testMethodWithDefaultArgument() {
        assertScript shell, '''
            interface I {
                def foo()
                def foo(bar)
                def foo(bar,baz)
            }
            @AutoImplement
            class C implements I {
                @Override // foo(bar) and foo(bar,baz)
                def foo(bar, baz = null) {
                    return bar
                }
            }
            def c = new C()
            assert c.foo() == null
            assert c.foo(12) == 12
        '''
    }
}
