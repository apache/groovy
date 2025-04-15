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

final class InheritConstructorsTransformTest {

    private final GroovyShell shell = GroovyShell.withConfig {
        imports { star 'groovy.transform' }
    }

    @Test
    void testStandardCase() {
        assertScript shell, '''
            @InheritConstructors
            class CustomException extends RuntimeException {
            }

            def ce = new CustomException('foo')
            assert ce.message == 'foo'
        '''
    }

    @Test
    void testOverrideCase() {
        assertScript shell, '''
            @InheritConstructors
            class CustomException2 extends RuntimeException {
                CustomException2() { super('bar') }
            }

            def ce = new CustomException2()
            assert ce.message == 'bar'
            ce = new CustomException2('foo')
            assert ce.message == 'foo'
        '''
    }

    @Test
    void testChainedCase() {
        assertScript shell, '''
            @InheritConstructors
            class CustomException5 extends CustomException4 {
            }
            @InheritConstructors
            class CustomException3 extends RuntimeException {
            }
            @InheritConstructors
            class CustomException4 extends CustomException3 {
            }

            def ce = new CustomException5('baz')
            assert ce.message == 'baz'
        '''
    }

    @Test // GROOVY-7059
    void testCopyAnnotations() {
        assertScript shell, '''
            import java.lang.annotation.*

            @Retention(RetentionPolicy.RUNTIME)
            @Target([ElementType.CONSTRUCTOR])
            public @interface Foo1 {
            }

            @Retention(RetentionPolicy.SOURCE)
            @Target([ElementType.CONSTRUCTOR])
            public @interface Foo2 {
            }

            @Retention(RetentionPolicy.RUNTIME)
            @Target([ElementType.PARAMETER])
            public @interface Foo3 {
            }

            class Bar {
                @Foo1 @Foo2
                Bar() {}

                @Foo2
                Bar(@Foo3 String name) {}

                @Foo1
                Bar(Integer num) {}

                Bar(String name, @Foo3 Integer num) {}
            }

            @InheritConstructors(constructorAnnotations=true, parameterAnnotations=true)
            class Baz extends Bar {
            }

            new Baz().class.constructors.each { cons ->
                def ans = cons.annotations.toString() + cons.parameterAnnotations.toString()
                switch(cons.toString()) {
                    case 'public Baz(java.lang.String,java.lang.Integer)':
                        assert ans == '[@groovy.transform.Generated()][[], [@Foo3()]]'
                        break
                    case 'public Baz(java.lang.String)':
                        assert ans == '[@groovy.transform.Generated()][[@Foo3()]]'
                        break
                    case 'public Baz(java.lang.Integer)':
                        assert ans == '[@groovy.transform.Generated(), @Foo1()][[]]'
                        break
                    case 'public Baz()':
                        assert ans == '[@groovy.transform.Generated(), @Foo1()][]'
                        break
                }
            }
        '''
    }

    @Test
    void testInnerClassUsage() {
        assertScript shell, '''
            @InheritConstructors
            class Outer extends RuntimeException {
                @InheritConstructors
                class Inner extends RuntimeException {}
                @InheritConstructors
                static class StaticInner extends RuntimeException {}
                void test() {
                    assert new StaticInner('bar').message == 'bar'
                    assert new Inner('foo').message == 'foo'
                }
            }
            class Outer2 extends Outer {
                @InheritConstructors
                class Inner2 extends Outer.Inner {}
                void test() {
                    assert new Inner2('foobar').message == 'foobar'
                }
            }

            def o = new Outer('baz')
            assert o.message == 'baz'
            o.test()
            new Outer2().test()
        '''
    }

    @Test // GROOVY-6874
    void testParametersWithGenericsAndCompileStatic1() {
        assertScript shell, '''
            abstract class A<X, Y> {
                A(Set<Y> set) {
                }
            }

            @CompileStatic
            @InheritConstructors
            class C<Z> extends A<Integer, Z> {
                void test() {
                    new C<Z>(new HashSet<Z>())
                }
            }

            new C<String>().test()
        '''
    }

    @Test // GROOVY-6874
    void testParametersWithGenericsAndCompileStatic2() {
        assertScript shell, '''
            import java.math.RoundingMode

            @CompileStatic
            abstract class BasePublisher<T, U> {
                final Deque<T> items
                private U mode
                BasePublisher(Deque<T> items) { this.items = items }
                BasePublisher(U mode) {
                    this.mode = mode
                    this.items = new ArrayDeque<>()
                }
                BasePublisher(Set<U> modes) { this(modes[0]) }
                void publish(T item) { items.addFirst(item) }
                void init(U mode) { this.mode = mode }
                String toString() { items.join('|') + "|" + mode.toString() }
            }

            @CompileStatic @InheritConstructors
            class OrderPublisher<V> extends BasePublisher<Integer, V> {
                static OrderPublisher make() {
                    new OrderPublisher<RoundingMode>(new LinkedList<Integer>())
                }
                void foo() { publish(3) }
                void bar(V mode) { init(mode) }
                void baz() {
                    new OrderPublisher<RoundingMode>(RoundingMode.UP)
                    new OrderPublisher<RoundingMode>(new HashSet<RoundingMode>())
                }
            }

            def op = OrderPublisher.make()
            op.foo()
            op.bar(RoundingMode.DOWN)
            op.baz()
            assert op.toString() == '3|DOWN'
        '''
    }

    @Test // GROOVY-6874
    void testParametersWithGenericsAndCompileStatic3() {
        def err = shouldFail shell, '''
            import java.math.RoundingMode

            @CompileStatic
            abstract class BasePublisher<T, U> {
                final Deque<T> items
                private U mode
                BasePublisher(Deque<T> items) { this.items = items }
                BasePublisher(U mode) {
                    this.mode = mode
                    this.items = new ArrayDeque<>()
                }
                BasePublisher(Set<U> modes) { this(modes[0]) }
                void publish(T item) { items.addFirst(item) }
                void init(U mode) { this.mode = mode }
                String toString() { items.join('|') + "|" + mode.toString() }
            }

            @CompileStatic @InheritConstructors
            class OrderPublisher<V> extends BasePublisher<Integer, V> {
                static OrderPublisher make() {
                    new OrderPublisher<RoundingMode>(new LinkedList<String>())
                }
                void foo() { publish(3) }
                void bar(V mode) { init(mode) }
                void baz() {
                    new OrderPublisher<RoundingMode>(new Date())
                    new OrderPublisher<RoundingMode>(new HashSet<Date>())
                }
            }

            def op = OrderPublisher.make()
            op.foo()
            op.bar(RoundingMode.DOWN)
            assert op.toString() == '3|DOWN'
        '''
        assert err.message.contains('Cannot call OrderPublisher#<init>(java.util.Deque<java.lang.Integer>) with arguments [java.util.LinkedList<java.lang.String>]')
        assert err.message.contains('Cannot call OrderPublisher#<init>(java.math.RoundingMode) with arguments [java.util.Date]')
        assert err.message.contains('Cannot call OrderPublisher#<init>(java.util.Set<java.math.RoundingMode>) with arguments [java.util.HashSet<java.util.Date>]')
    }

    @Test // GROOVY-9323
    void testAnnotationsCopiedForConstructorsFromPrecompiledClass() {
        assertScript shell, """
            @InheritConstructors(constructorAnnotations=true)
            class MyChildException extends ${this.class.name}.MyException9323 {}

            def annos = MyChildException.constructors[0].annotations*.annotationType().simpleName
            assert annos.contains('Generated') && annos.contains('Deprecated')
        """
    }

    static class MyException9323 extends RuntimeException {
        @Deprecated MyException9323() {}
    }
}
