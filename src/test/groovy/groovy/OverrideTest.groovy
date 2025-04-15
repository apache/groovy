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

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class OverrideTest {

    @Test
    void testHappyPath() {
        assertScript '''
            abstract class Parent<T> {
                abstract method()
                void methodTakeT(T t) { }
                T methodMakeT() { return null }
            }

            interface Intf<U> {
                def method4()
                void method5(U u)
                U method6()
            }

            interface IntfString extends Intf<String> {}

            class OverrideAnnotationTest extends Parent<Integer> implements IntfString {
                @Override method() {}
                @Override void methodTakeT(Integer arg) {}
                @Override Integer methodMakeT() {}
                @Override method4() {}
                @Override void method5(String arg) {}
                @Override String method6() {}
            }

            new OverrideAnnotationTest()
        '''
    }

    @Test
    void testUnhappyPath() {
        def err = shouldFail '''
            abstract class Parent<T> {
                abstract method()
                void methodTakeT(T t) { }
                T methodMakeT() { return null }
            }

            interface Intf<U> {
                def method4()
                void method5(U u)
                U method6()
            }

            interface IntfString extends Intf<String> {}

            class OverrideAnnotationTest extends Parent<Integer> implements IntfString {
                @Override method() {}
                @Override void methodTakeT(arg) {}
                @Override Double methodMakeT() {}
                @Override method4() {}
                @Override void method5(String arg) {}
                @Override String method6() {}
            }

            new OverrideAnnotationTest()
        '''
        assert err.message.contains(/The return type of java.lang.Double methodMakeT() in OverrideAnnotationTest is incompatible with java.lang.Integer in Parent/)
        assert err.message.contains(/Method 'methodTakeT' from class 'OverrideAnnotationTest' does not override method from its superclass or interfaces but is annotated with @Override./)
    }

    @Test
    void testSpuriousMethod() {
        def err = shouldFail '''
            interface Intf<U> {
                def method()
            }

            interface IntfString extends Intf<String> {}

            class HasSpuriousMethod implements IntfString {
                @Override method() {}
                @Override someOtherMethod() {}
            }
        '''
        assert err.message.contains("Method 'someOtherMethod' from class 'HasSpuriousMethod' does not override method from its superclass or interfaces but is annotated with @Override.")
    }

    @Test
    void testBadReturnType() {
        def err = shouldFail '''
            interface Intf<U> {
                def method()
                U method6()
            }

            interface IntfString extends Intf<String> {}

            class HasMethodWithBadReturnType implements IntfString {
                @Override method() {}
                @Override methodReturnsObject() {}
            }
        '''
        assert err.message.contains("Method 'methodReturnsObject' from class 'HasMethodWithBadReturnType' does not override method from its superclass or interfaces but is annotated with @Override.")
    }

    @Test
    void testBadParameterType() {
        def err = shouldFail '''
            interface Intf<U> {
                def method()
                void method6(U u)
            }

            interface IntfString extends Intf<String> {}

            class HasMethodWithBadArgType implements IntfString {
                @Override method() {}
                @Override void methodTakesObject(arg) {}
            }
        '''
        assert err.message.contains("Method 'methodTakesObject' from class 'HasMethodWithBadArgType' does not override method from its superclass or interfaces but is annotated with @Override.")
    }

    @Test
    void testCovariantParameterType1() {
        assertScript '''
            class C implements Comparable<C> {
                int index
                int compareTo(C c) {
                    index <=> c.index
                }
            }

            one = new C(index:1)
            two = new C(index:2)
            assert one < two
        '''
    }

    @Test
    void testCovariantParameterType2() {
        assertScript '''
            interface I<T> {
               int handle(long n, T t)
            }

            class C implements I<String> {
                int handle(long n, String something) {
                    1
                }
            }

            c = new C()
            assert c.handle(5,"hi") == 1
        '''
    }

    @Test
    void testCovariantParameterType3() {
        assertScript '''
            interface I<T> {
                int testMethod(T t)
            }
            class C implements I<Date> {
                int testMethod(Date date) {}
                int testMethod(Object obj) {}
            }

            assert C.declaredMethods.count{ it.name=="testMethod" } == 2
        '''
    }

    // GROOVY-6654
    @Test
    void testCovariantParameterType4() {
        assertScript '''
            class C<T> {
                void proc(T t) {}
            }

            class D extends C<String> {
                @Override
                void proc(String s) {}
            }

            def d = new D()
        '''
    }

    // GROOVY-10675
    @Test
    void testCovariantParameterType5() {
        assertScript '''
            @FunctionalInterface
            interface A<I, O> {
                O apply(I in)
            }
            interface B<X, Y> extends A<X, Y> {
            }
            class C implements B<Number, String> {
                @Override String apply(Number n) { 'x' }
            }

            def result = new C().apply(42)
            assert result == 'x'
        '''
    }

    // GROOVY-11550
    @Test
    void testCovariantParameterType6() {
        def err = shouldFail '''
            interface I<T> {
                void m(I<Object> i_of_object)
            }
            class C implements I<Object> {
                void m(I<String> i_of_string) {
                }
            }
        '''
        assert err =~ /name clash: m\(I<java.lang.String>\) in class 'C' and m\(I<java.lang.Object>\) in interface 'I' have the same erasure, yet neither overrides the other./
    }

    @Test
    void testOverrideOnMethodWithDefaultParameters() {
        assertScript '''
            interface TemplatedInterface {
                String execute(Map argument)
            }

            class TemplatedInterfaceImplementation implements TemplatedInterface {
                @Override
                String execute(Map argument = [:]) {
                    return null
                }
            }
            new TemplatedInterfaceImplementation()
        '''
    }

    @Test
    void testOverrideOnMethodWithDefaultParametersVariant() {
        assertScript '''
            interface TemplatedInterface {
                String execute(Map argument)
            }

            class TemplatedInterfaceImplementation implements TemplatedInterface {
                @Override
                String execute(Map argument, String foo = null) {
                    return foo
                }
            }
            new TemplatedInterfaceImplementation()
        '''
    }

    // GROOVY-11548
    @Test
    void testDefaultMethodDoesNotOverride() {
        for (kind in ['def', 'final', 'public']) {
            assertScript """
                class A {
                    $kind func() { 'A' }
                }
                interface B {
                    default func() { 'B' }
                }
                class C extends A implements B {
                }

                assert new C().func() == 'A'
            """
        }
    }
}
