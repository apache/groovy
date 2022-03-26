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

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class AutoImplementTransformTest {

    @Test
    void testException() {
        shouldFail UnsupportedOperationException, '''
            import groovy.transform.*

            @AutoImplement(exception=UnsupportedOperationException)
            class Foo implements Iterator<String> { }

            new Foo().hasNext()
        '''
    }

    @Test
    void testExceptionWithMessage() {
        def err = shouldFail UnsupportedOperationException, '''
            import groovy.transform.*

            @AutoImplement(exception=UnsupportedOperationException, message='Not supported by Foo')
            class Foo implements Iterator<String> { }

            new Foo().hasNext()
        '''
        assert err.message.contains('Not supported by Foo')
    }

    @Test
    void testClosureBody() {
        shouldFail IllegalStateException, '''
            import groovy.transform.*

            @AutoImplement(code={ throw new IllegalStateException() })
            class Foo implements Iterator<String> { }

            new Foo().hasNext()
        '''
    }

    @Test
    void testInheritedMethodNotOverwritten() {
        assertScript '''
            class WithNext {
                String next() { 'foo' }
            }

            @groovy.transform.AutoImplement
            class Foo extends WithNext implements Iterator<String> { }

            assert new Foo().next() == 'foo'
        '''
    }

    @Test
    void testExistingMethodNotOverwritten() {
        assertScript '''
            @groovy.transform.AutoImplement
            class Foo implements Iterator<String> {
                String next() { 'foo' }
            }

            assert new Foo().next() == 'foo'
        '''
    }

    @Test // GROOVY-9816
    void testPropertyMethodsNotOverwritten() {
        assertScript '''
            interface Bar {
                def getBaz(); void setBaz(baz)
            }

            @groovy.transform.AutoImplement
            class Foo implements Bar {
                def baz
            }

            def foo = new Foo(baz: 123)
            assert foo.baz == 123
        '''

        assertScript '''
            interface Bar {
                def getBaz(); void setBaz(baz)
            }

            @groovy.transform.AutoImplement
            class Foo implements Bar {
                final baz = 123
            }

            // setter is independent of constant
            def foo = new Foo(baz: 456)
            assert foo.baz == 123
        '''

        assertScript '''
            interface Bar {
                boolean getBaz(); boolean isBaz()
            }

            @groovy.transform.AutoImplement
            class Foo implements Bar {
                boolean baz
            }

            def foo = new Foo(baz: true)
            assert foo.getBaz()
            assert foo.isBaz()
            assert foo.baz
        '''

        assertScript '''
            interface Bar {
                boolean getBaz(); boolean isBaz()
            }

            @groovy.transform.AutoImplement
            class Foo implements Bar {
                boolean baz
                boolean getBaz() { baz }
            }

            def foo = new Foo(baz: true)
            assert foo.getBaz()
            assert foo.isBaz()
            assert foo.baz
        '''

        assertScript '''
            interface Bar {
                boolean getBaz(); boolean isBaz()
            }

            @groovy.transform.AutoImplement
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
        assertScript '''
            interface Bar {
                void baz()
            }

            @groovy.transform.AutoImplement
            class Foo implements Bar { }

            new Foo().baz() // no value to assert
        '''
    }

    @Test
    void testGenericReturnTypes() {
        assertScript '''
            interface HasXs<T> {
                T[] x()
            }

            abstract class HasXsY<E> implements HasXs<Long> {
                abstract E y()
            }

            interface MyIt<T> extends Iterator<T> { }

            @groovy.transform.AutoImplement
            class Foo extends HasXsY<Integer> implements MyIt<String> { }

            def publicMethods = Foo.methods.findAll{ it.modifiers == 1 }.collect{ "$it.returnType.simpleName $it.name" }*.toString()
            assert ['boolean hasNext', 'String next', 'Long[] x', 'Integer y'].every{ publicMethods.contains(it) }
            '''
    }

    @Test // GROOVY-8270
    void testGenericParameterTypes() {
        assertScript '''
            @groovy.transform.AutoImplement
            class Foo implements Comparator<String> { }
            // Can't have an abstract method in a non-abstract class. The class 'Foo' must be declared
            // abstract or the method 'int compare(java.lang.Object, java.lang.Object)' must be implemented.

            assert new Foo().compare('bar', 'baz') == 0
            '''
    }

    @Test // GROOVY-10472
    void testCovariantReturnTypes() {
        assertScript '''
            import groovy.transform.AutoImplement

            interface Super { List findAll() }
            interface Sub extends Super { Iterable findAll() }

            @AutoImplement
            class ThisClassFails implements Sub{}

            assert !(new ThisClassFails().findAll())
        '''
        assertScript '''
            import groovy.transform.AutoImplement

            interface Super { ArrayList findAll() }
            interface Sub extends Super { Iterable findAll() }

            @AutoImplement
            class ThisClassFails implements Sub{}

            assert !(new ThisClassFails().findAll())
        '''
        assertScript '''
            import groovy.transform.AutoImplement

            interface Super { Iterable findAll() }
            interface Sub extends Super { List findAll() }

            @AutoImplement
            class ThisClassFails implements Sub{}

            assert !(new ThisClassFails().findAll())
        '''
        assertScript '''
            import groovy.transform.AutoImplement

            interface Super { Iterable findAll() }
            interface Sub extends Super { ArrayList findAll() }

            @AutoImplement
            class ThisClassFails implements Sub{}

            assert !(new ThisClassFails().findAll())
        '''
        assertScript '''
            import groovy.transform.AutoImplement

            interface Super { AbstractList findAll() }
            interface Sub extends Super { List findAll() }

            @AutoImplement
            class ThisClassFails implements Sub{}

            assert !(new ThisClassFails().findAll())
        '''
        assertScript '''
            import groovy.transform.AutoImplement

            interface Super { List findAll() }
            interface Sub extends Super { AbstractList findAll() }

            @AutoImplement
            class ThisClassFails implements Sub{}

            assert !(new ThisClassFails().findAll())
        '''
        assertScript '''
            import groovy.transform.AutoImplement

            interface Super { AbstractList findAll() }
            interface Sub extends Super { ArrayList findAll() }

            @AutoImplement
            class ThisClassFails implements Sub{}

            assert !(new ThisClassFails().findAll())
        '''
        assertScript '''
            import groovy.transform.AutoImplement

            interface Super { ArrayList findAll() }
            interface Sub extends Super { AbstractList findAll() }

            @AutoImplement
            class ThisClassFails implements Sub{}

            assert !(new ThisClassFails().findAll())
        '''
    }

    @Test // GROOVY-10552
    void testMethodWithTypeParameter() {
        assertScript '''
            interface I {
                def <T> T m(List<T> list)
            }

            @groovy.transform.AutoImplement
            class C implements I {
            }

            Object result = new C().m([])
            assert result == null
        '''

        assertScript '''
            @groovy.transform.AutoImplement
            class C implements java.sql.Connection {
            }

            new C().commit()
        '''
    }
}
