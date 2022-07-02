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

    @Test // GROOVY-6654
    void testCovariantParameterType1() {
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

    @Test // GROOVY-10675
    void testCovariantParameterType2() {
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

    @Test // GROOVY-7849
    void testCovariantArrayReturnType1() {
        assertScript '''
            interface Base {}

            interface Derived extends Base {}

            interface I {
                Base[] foo()
            }

            class C implements I {
                Derived[] foo() { null }
            }
            new C().foo()
        '''
    }

    @Test // GROOVY-7185
    void testCovariantArrayReturnType2() {
        assertScript '''
            interface A<T> {
                T[] process();
            }

            class B implements A<String> {
                @Override
                public String[] process() {
                    ['foo']
                }
            }

            class C extends B {
                @Override
                String[] process() {
                    super.process()
                }
            }
            assert new C().process()[0] == 'foo'
        '''
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
}
