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
package org.codehaus.groovy.transform.traitx

import groovy.transform.SelfType
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ListExpression
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class TraitASTTransformationTest {

    private final GroovyShell shell = GroovyShell.withConfig {
        imports {
            star 'groovy.transform'
            normal 'org.codehaus.groovy.transform.trait.Traits'
        }
    }

    @Test
    void testTraitOverrideAnnotation() {
        assertScript shell, '''
            interface MyInterface {
                String fooMethod()
                void noMethod()
            }

            trait MyTrait implements MyInterface {
                @Override String fooMethod() { "foo" }
                @Override void noMethod() { }
            }

            class Foo implements MyTrait {}
            def foo = new Foo()

            foo.noMethod()
            assert foo.fooMethod() == "foo"
        '''
    }

    @Test
    void testTraitWithNoMethod() {
        assertScript shell, '''
            trait MyTrait {}

            class Foo implements MyTrait {}
            def foo = new Foo()
            assert foo instanceof MyTrait
        '''
    }

    @Test
    void testTraitWithOneMethod() {
        assertScript shell, '''
            trait MyTrait {
                int a() { 1 }
            }

            class Foo implements MyTrait {}
            def foo = new Foo()
            assert foo instanceof MyTrait
            assert foo.a() == 1
        '''
    }

    @Test
    void testTraitWithTwoMethods() {
        assertScript shell, '''
            trait MyTrait {
                int a() { 1 }
                int b() { a() }
            }

            class Foo implements MyTrait {}
            def foo = new Foo()
            assert foo instanceof MyTrait
            assert foo.a() == 1
            assert foo.b() == 1
        '''
    }

    @Test
    void testTraitWithTwoMethodsAndOneOverride() {
        assertScript shell, '''
            trait MyTrait {
                int a() { 1 }
                int b() { a() }
            }

            class Foo implements MyTrait {
                int a() { 2 }

            }
            def foo = new Foo()
            assert foo instanceof MyTrait
            assert foo.a() == 2
            assert foo.b() == 2
        '''
    }

    @Test
    void testTraitWithTwoMethodsAndOneAbstract() {
        assertScript shell, '''
            trait MyTrait {
                abstract int a()
                int b() { a() }
            }

            class Foo implements MyTrait {
                int a() { 2 }
            }

            def foo = new Foo()
            assert foo instanceof MyTrait
            assert foo.a() == 2
            assert foo.b() == 2
        '''
    }

    @Test
    void testTraitWithTwoMethodsAndOneAbstractNotImplemented() {
        shouldFail shell, '''
            abstract trait MyTrait {
                abstract int a()
                int b() { a() }
            }

            class Foo implements MyTrait {
            }
            def foo = new Foo()
        '''
    }

    static trait TestTrait {
        int a() { 123 }
    }

    @Test
    void testWithPrecompiledTraitWithOneMethod() {
        assertScript shell, """
            import ${this.class.name}.TestTrait

            class Foo implements TestTrait {}
            def foo = new Foo()
            assert foo.a() == 123
        """
    }

    @Test
    void testTraitWithConstructor() {
        shouldFail shell, '''
            abstract trait MyTrait {
                MyTrait() {
                    println 'woo'
                }
            }

            class Foo implements MyTrait {
            }
            def foo = new Foo()
        '''
    }

    @Test
    void testTraitWithField() {
        assertScript shell, '''
            trait MyTrait {
                private String message = 'Hello'
                String getBlah() {
                    message
                }

            }
            class Foo implements MyTrait {}
            def foo = new Foo()
            assert foo.blah == 'Hello'
        '''
    }

    @Test
    void testTraitWithField2() {
        assertScript shell, '''
            import org.codehaus.groovy.transform.traitx.TestTrait2

            class Foo implements TestTrait2 {
                def cat() { "cat" }
            }
            def foo = new Foo()
            assert foo.message == 'Hello'
            assert foo.blah() == 'Hello'
            assert foo.meow() == /Meow! I'm a cat/
        '''
    }

    @Test
    void testTraitWithSetValue() {
        assertScript shell, '''
            trait Named {
                private String name
                void setLabel(String val) { name = val }
                void setLabel2(String val) { this.name = val }
                void setLabel3(String val) { this.@name = val }
                String getName() { name }
            }

            class Person implements Named {}
            def p = new Person()
            assert p.name == null
            p.setLabel('label')
            assert p.name == 'label'
            p.setLabel2('label2')
            assert p.name == 'label2'
            p.setLabel3('label3')
            assert p.name == 'label3'
        '''
    }

    @Test
    void testTraitWithProperty() {
        assertScript shell, '''
            trait Named {
                String name
            }

            class Person implements Named {}

            def p = new Person(name:'Stromae')

            assert p.name == 'Stromae'
        '''
    }

    @Test
    void testClosureExpressionInTrait() {
        assertScript shell, '''
            trait GreetingObject {
                String greeting = 'Welcome!'
                Closure greeter() {
                    return { -> greeting }
                }
            }
            class Hello implements GreetingObject {}
            def hello = new Hello()
            def greeter = hello.greeter()
            assert greeter.thisObject.is(hello)
            assert greeter() == 'Welcome!'
        '''
    }

    @Test
    void testUpdatePropertyFromSelf() {
        assertScript shell, '''
            trait Updater {
                void update() {
                    config.key = 'value'
                }
            }
            class Foo implements Updater {
                def config = [:]
            }
            def foo = new Foo()
            foo.update()

            assert foo.config.key == 'value'
        '''
    }

    @Test
    void testPrivateFieldInTraitShouldBeRemapped() {
        assertScript shell, '''
            trait Foo {
                private int i = 0
                int sum(int x) { x+i }
                void setIndex(int index) { this.i = index }
            }
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.fields.any { it.name == 'Foo__i' }
            })
            class Bob implements Foo {
            }
            def b = new Bob()
            assert b.sum(1) == 1
            b.index = 5
            assert b.sum(1) == 6
        '''
    }

    @Test
    void testStaticallyCompiledTrait() {
        assertScript shell, '''
            @CompileStatic
            trait Foo {
               private String msg = 'foo'
               abstract String bar()
               public String foo() { bar()+msg }

            }

            @CompileStatic
            class A implements Foo { String bar() {'bar'}}

            assert new A().foo() == 'barfoo'
        '''
    }

    @Test
    void testOverridePropertyDefinedInTrait() {
        assertScript shell, '''
            trait Id {
                Long id = 123L
            }

            class Foo implements Id {
                Long id = 456L
            }
            def f = new Foo()
            assert f.id == 456L
        '''
    }

    @Test
    void testOverridePropertyGetterDefinedInTrait() {
        assertScript shell, '''
            trait Id {
                Long id = 123L
            }

            class Foo implements Id {
                Long getId() { 456L }
            }
            def f = new Foo()
            assert f.id == 456L
        '''
    }

    @Test
    void testSimpleTraitInheritance() {
        assertScript shell, '''
            trait Top { String methodFromA() { 'A' } }
            trait Bottom extends Top { String methodFromB() { 'B' }}
            class Foo implements Bottom {}
            def f = new Foo()
            assert f.methodFromA() == 'A'
            assert f.methodFromB() == 'B'
        '''
    }

    @Test
    void testSimpleTraitInheritanceWithTraitOverridingMethodFromParent() {
        10.times {
            assertScript shell, '''
                trait Top { String methodFromA() { 'A' } }
                trait Bottom extends Top {
                    String methodFromA() { 'B' }
                    String methodFromB() { 'B' }
                }
                class Foo implements Bottom {}
                def f = new Foo()
                assert f.methodFromA() == 'B'
                assert f.methodFromB() == 'B'
            '''
        }
    }

    @Test
    void testSimpleTraitInheritanceWithTraitOverridingMethodFromParentAndClass() {
        assertScript shell, '''
            trait Top { String methodFromA() { 'A' } }
            trait Bottom extends Top {
                String methodFromA() { 'B' }
                String methodFromB() { 'B' }
            }
            class Foo implements Bottom {
                String methodFromA() { 'Foo' }
            }
            def f = new Foo()
            assert f.methodFromA() == 'Foo'
            assert f.methodFromB() == 'B'
        '''
    }

    @Test
    void testTraitOnEnum() {
        assertScript shell, '''
            trait WithBar { int bar }

            enum MyEnum implements WithBar {
                X, Y
            }

            class MyClass implements WithBar {}

            def o = new MyClass()
            o.bar = 123
            assert o.bar == 123

            MyEnum.X.bar = 123
            assert MyEnum.X.bar == 123 && MyEnum.Y.bar == 0
        '''
    }

    @Test
    void testClassImplementingTraitWithSameMethod() {
        assertScript shell, '''
            trait A {
                int foo() { 1 }
            }
            trait B {
                int foo() { 2 }
            }
            class AB implements A,B {
            }
            def x = new AB()
            assert x.foo() == 2 // default order, B is first
        '''

        assertScript shell, '''
            trait A {
                int foo() { 1 }
            }
            trait B {
                int foo() { 2 }
            }
            class AB implements B,A {
            }
            def x = new AB()
            assert x.foo() == 1 // default order, A is first
        '''

        assertScript shell, '''
            trait A {
                int foo() { 1 }
            }
            trait B {
                int foo() { 2 }
            }
            class AB implements A,B {
                int foo() {
                    A.super.foo() // explicit use of A
                }
            }
            def x = new AB()
            assert x.foo() == 1
        '''

        assertScript shell, '''
            trait A {
                int foo() { 1 }
            }
            trait B {
                int foo() { 2 }
            }
            class AB implements A,B {
                int foo() {
                    A.super.foo()  // explicit take of A
                }
            }
            def x = new AB()
            assert x.foo() == 1
        '''

        // make sure it is compatible with @CompileStatic
        assertScript shell, '''
            trait A {
                int foo() { 1 }
            }
            trait B {
                int foo() { 2 }
            }
            @CompileStatic
            class AB implements A,B {
                int foo() {
                    B.super.foo()
                }
            }
            def x = new AB()
            assert x.foo() == 2
        '''

        // GROOVY-10144
        assertScript shell, '''
            trait T {
                def m() { 'T' }
            }
            class C implements T {
                @Override
                def m() {
                    'C' + T.super.m()
                }
            }
            String result = new C().m()
            assert result == 'CT'
        '''
    }

    @Test
    void testTraitWithGenerics1() {
        assertScript shell, '''
            trait Provider<T> {
                T get() {
                    null
                }
            }
            class StringProvider implements Provider<String> {}
            def c = new StringProvider()
            assert c.get() == null
        '''

        assertScript shell, '''
            @CompileStatic
            trait Provider<T> {
                T get() {
                    null
                }
            }
            @CompileStatic
            class StringProvider implements Provider<String> {}
            def c = new StringProvider()
            assert c.get() == null
        '''
    }

    @Test
    void testTraitWithGenerics2() {
        assertScript shell, '''
            trait Provider<T> {
                T get(T ref) {
                    ref
                }
            }
            class StringProvider implements Provider<String> {}
            def c = new StringProvider()
            assert c.get('foo') == 'foo'
        '''

        assertScript shell, '''
            @CompileStatic
            trait Provider<T> {
                T get(T ref) {
                    ref
                }
            }
            @CompileStatic
            class StringProvider implements Provider<String> {}
            def c = new StringProvider()
            assert c.get('foo') == 'foo'
        '''
    }

    @Test // GROOVY-9760
    void testTraitWithGenerics3() {
        assertScript shell, '''
            trait Provider<T> {
                T get(T ref) {
                    ref
                }
            }
            class UnspecifiedProvider implements Provider {
            }
            assert new UnspecifiedProvider().get('foo') == 'foo'
        '''

        assertScript shell, '''
            @CompileStatic
            trait Provider<T> {
                T get(T ref) {
                    ref
                }
            }
            @CompileStatic
            class UnspecifiedProvider implements Provider {
            }
            assert new UnspecifiedProvider().get('foo') == 'foo'
        '''
    }

    @Test
    void testTraitWithGenericProperty() {
        assertScript shell, '''
            trait PropertyProvider<T> {
                T foo
            }
            class StringProperty implements PropertyProvider<String> {}
            def c = new StringProperty()
            c.foo = 'foo'
            assert c.foo == 'foo'
        '''

        assertScript shell, '''
            @CompileStatic
            trait PropertyProvider<T> {
                T foo
            }
            @CompileStatic
            class StringProperty implements PropertyProvider<String> {}
            def c = new StringProperty()
            c.foo = 'foo'
            assert c.foo == 'foo'
        '''
    }

    @Test
    void testTraitWithComplexGenericProperty() {
        assertScript shell, '''
            trait PropertyProvider<T> {
                List<T> foo
            }
            class StringProperty implements PropertyProvider<String> {}
            def c = new StringProperty()
            c.foo = ['foo']
            assert c.foo == ['foo']
        '''

        assertScript shell, '''
            @CompileStatic
            trait PropertyProvider<T> {
                List<T> foo
            }
            @CompileStatic
            class StringProperty implements PropertyProvider<String> {}
            def c = new StringProperty()
            c.foo = ['foo']
            assert c.foo == ['foo']
        '''

        assertScript shell, '''
            trait PropertyProvider<T> {
                List<T> foo
            }
            class StringProperty implements PropertyProvider<String> {}

            @CompileStatic
            void test() {
                def c = new StringProperty()
                c.foo = ['foo']
                assert c.foo == ['foo']
            }
            test()
        '''
    }

    @Test
    void testTraitWithGenericField() {
        assertScript shell, '''
            trait PropertyProvider<T> {
                private T foo
                void set(T t) { foo = t}
                T get() { foo }
            }
            class StringProperty implements PropertyProvider<String> {}
            def c = new StringProperty()
            c.set('foo')
            assert c.get() == 'foo'
        '''

        assertScript shell, '''
            @CompileStatic
            trait PropertyProvider<T> {
                private T foo
                void set(T t) { foo = t}
                T get() { foo }
            }
            @CompileStatic
            class StringProperty implements PropertyProvider<String> {}
            def c = new StringProperty()
            c.set('foo')
            assert c.get() == 'foo'
        '''
    }

    @Test
    void testRuntimeTrait() {
        assertScript shell, '''
            trait Flying {
                String fly() {
                    "I'm flying!"
                }
            }
            class Duck {}
            def d = new Duck()
            try {
                d.fly()
            } catch (MissingMethodException e) {
                // doesn't implement Flying
            }
            d = d as Flying
            assert d instanceof Flying
            assert d.fly() == "I'm flying!"
        '''
    }

    @Test
    void testRuntimeDoubleTrait() {
        assertScript shell, '''
            trait Flying {
                String fly() {
                    "I'm flying!"
                }
            }
            trait Speaking {
                String speak() {
                    "I'm speaking!"
                }
            }
            class Duck {}
            def d = new Duck()
            try {
                d.fly()
                d.speak()
            } catch (MissingMethodException e) {
                // doesn't implement Flying
            }
            d = d as Flying
            assert d instanceof Flying
            assert d.fly() == "I'm flying!"
            d = d as Speaking
            assert d instanceof Speaking
            assert d.speak() == "I'm speaking!"
            // but still Flying!
            assert d instanceof Flying
            assert d.fly() == "I'm flying!"
        '''
    }

    @Test
    void testRuntimeWithTraitsDGM() {
        assertScript shell, '''
            trait Flying {
                String fly() {
                    "I'm flying!"
                }
            }
            trait Speaking {
                String speak() {
                    "I'm speaking!"
                }
            }
            class Duck {}
            def d = new Duck()
            try {
                d.fly()
                d.speak()
            } catch (MissingMethodException e) {
                // doesn't implement Flying
            }
            d = d.withTraits(Flying, Speaking)
            assert d instanceof Flying
            assert d.fly() == "I'm flying!"
            assert d instanceof Speaking
            assert d.speak() == "I'm speaking!"
        '''
    }

    @Test
    void testRuntimeWithTraitsDGMAndExplicitOverride() {
        assertScript shell, '''
            trait Flying {
                String fly() {
                    "I'm flying!"
                }
            }
            trait Speaking {
                String speak() {
                    "I'm speaking!"
                }
            }
            class Duck {
                String speak() { "I'm a special duck!" }
            }
            def d = new Duck()
            try {
                d.fly()
                d.speak()
            } catch (MissingMethodException e) {
                // doesn't implement Flying
            }
            // when using runtime traits, inherits methods from trait!
            d = d.withTraits(Flying, Speaking)
            assert d instanceof Flying
            assert d.fly() == "I'm flying!"
            assert d instanceof Speaking
            assert d.speak() == "I'm speaking!"
        '''
    }

    @Test
    void testRuntimeTraitUnderCompileStaticShouldUseMethodFromTrait() {
        assertScript shell, '''
            trait Flying {
                String fly() {
                    "I'm flying!"
                }
            }
            trait Speaking {
                String speak() {
                    "I'm speaking!"
                }
            }
            class Duck {
                String speak() { "I'm a special duck!" }
            }

            @CompileStatic
            void test() {
                def d = new Duck()
                d = d.withTraits(Flying, Speaking)
                assert d.fly() == "I'm flying!"
                // when using runtime traits, inherits methods from trait!
                assert d.speak() == "I'm speaking!"
            }
            test()
        '''
    }

    @Test
    void testRuntimeWithTraitsDGMAndExtraMethodCompileStatic() {
        assertScript shell, '''
            trait Flying {
                String fly() {
                    "I'm flying!"
                }
            }
            trait Speaking {
                String speak() {
                    "I'm speaking!"
                }
            }
            interface Quack { String quack() }
            class Duck implements Quack {
                String quack() { 'Quack!' } // requires an interface to be called statically
                String speak() { "I'm a special duck!"}
            }

            @CompileStatic
            void test() {
                def d = new Duck()
                d = d.withTraits(Flying, Speaking)
                assert d.fly() == "I'm flying!"
                // when using runtime traits, inherits methods from trait!
                assert d.speak() == "I'm speaking!"
                assert d.quack() == "Quack!"
            }
            test()
        '''
    }

    @Test
    void testRuntimeTraitWithMethodOfTheSameSignature() {
        assertScript shell, '''
            trait Flying {
                String ability() { 'fly' }
                String fly() {
                    "I'm flying!"
                }
            }
            class Duck {
                String fly() {
                    "Duck flying!"
                }
            }
            def d = new Duck() as Flying
            // when using runtime traits, inherits methods from trait!
            assert d.fly() == "I'm flying!"
            assert d.ability() == 'fly'
        '''
    }

    @Test
    void testTraitWithDelegatesTo() {
        assertScript shell, '''
            trait Route {
                void from(@DelegatesTo(To) Closure c) {
                    c.delegate = new To()
                }
            }
            class To {
               void test() { println 'Test' }
            }
            class Foo implements Route {}
            @CompileStatic
            void exec() {
               def f = new Foo()
               f.from {
                  test()
               }
            }
            exec()
            '''
    }

    @Test
    void testProxyGenerationShouldNotFail() {
        assertScript shell, '''
            trait T { }
            class C { }
            def o = new C()
            def t = o.withTraits(T)
            def u = t.withTraits(T) // shouldn't fail
        '''
    }

    @Test
    void testShouldNotThrowNPEWithInheritanceUsingExtends() {
        assertScript shell, '''
            trait Named {
                String name
            }

            trait NameSpeakable extends Named {
                String speak() { "My name is $name" }
            }

            class Phone implements NameSpeakable {}

            def phone = new Phone(name: 'Galaxy S3')
            assert phone.speak() == 'My name is Galaxy S3\'
        '''
    }

    @Test
    void testStaticInnerClassInTrait() {
        assertScript shell, '''
            trait Outer {
                Inner doSomething() {
                    new Inner()
                }
                static class Inner {
                    void foo() {
                        println 'Foo'
                    }
                }
            }
            class Foo implements Outer {
            }
            def f = new Foo()
            f.doSomething()
        '''
    }

    @Test
    void testNonStaticInnerClassInTrait() {
        shouldFail shell, '''
            trait Outer {
                Inner doSomething() {
                    new Inner()
                }
            }
            class Foo implements Outer {}
            def f = new Foo()
        '''
    }

    @Test
    void testClosureInsideTrait() {
        assertScript shell, '''
            trait Doubler {
                int foo(int x) {
                    { -> 2*x }.call()
                }
            }
            class Foo implements Doubler {}
            def f = new Foo()
            assert f.foo(4) == 8
        '''
    }

    @Test
    void testClosureInsideTraitAccessingProperty() {
        assertScript shell, '''
            trait Doubler {
                int x
                int foo() {
                    { -> 2*x }.call()
                }
            }
            class Foo implements Doubler {}
            def f = new Foo()
            f.x = 4
            assert f.foo() == 8
        '''
    }

    @Test
    void testThisDotClassInTrait() {
        assertScript shell, '''
            trait Classic {
                Class clazz() {
                    this.class
                }
            }
            class Foo implements Classic {}
            def f = new Foo()
            assert f.clazz() == Foo
        '''
    }

    @Test
    void testShouldNotThrowStackOverflow() {
        assertScript shell, '''
            trait TestTrait {
                private String message = 'Hello'
                String getMessage() { this.message }
                String blah() { message }
                void update(String msg ) { message = msg}
            }
            class Foo implements TestTrait{}
            def foo = new Foo()
            assert foo.message == 'Hello'
            assert foo.blah() == 'Hello'
            foo.update('Groovy')
            assert foo.blah() == 'Groovy'
        '''

        assertScript shell, '''
            @CompileStatic
            trait TestTrait {
                private String message = 'Hello'
                String getMessage() { this.message }
                String blah() { message }
                void update(String msg ) { message = msg}
            }
            @CompileStatic
            class Foo implements TestTrait{}
            @CompileStatic
            void test() {
                def foo = new Foo()
                assert foo.message == 'Hello'
                assert foo.blah() == 'Hello'
                foo.update('Groovy')
                assert foo.blah() == 'Groovy'
            }
            test()
        '''
    }

    @Test // GROOVY-9255
    void testTraitSuperPropertyGet() {
        assertScript shell, '''
            trait T {
                def x = 'value'
            }
            class C implements T {
                def test() {
                    T.super.x
                }
            }
            assert new C().test() == 'value'
        '''

        assertScript shell, '''
            trait T {
                boolean x = true
            }
            class C implements T {
                def test() {
                    T.super.x
                }
            }
            assert new C().test() == true
        '''

        assertScript shell, '''
            trait T {
                def getX() { 'value' }
            }
            class C implements T {
                def test() {
                    T.super.x
                }
            }
            assert new C().test() == 'value'
        '''

        assertScript shell, '''
            trait T {
                boolean isX() { true }
            }
            class C implements T {
                def test() {
                    T.super.x
                }
            }
            assert new C().test() == true
        '''
    }

    @Test // GROOVY-9672
    void testTraitSuperPropertyGetStatic() {
        assertScript shell, '''
            trait T {
                static x = 'value'
            }
            class C implements T {
                def test() {
                    T.super.x
                }
            }
            assert new C().test() == 'value'
        '''

        assertScript shell, '''
            trait T {
                static boolean x = true
            }
            class C implements T {
                def test() {
                    T.super.x
                }
            }
            assert new C().test() == true
        '''

        assertScript shell, '''
            trait T {
                static getX() { 'value' }
            }
            class C implements T {
                def test() {
                    T.super.x
                }
            }
            assert new C().test() == 'value'
        '''

        assertScript shell, '''
            trait T {
                static boolean isX() { true }
            }
            class C implements T {
                def test() {
                    T.super.x
                }
            }
            assert new C().test() == true
        '''

        assertScript shell, '''
            trait A {
                static getX() { 'A' }
            }
            trait B {
                static getX() { 'B' }
            }
            class C implements A, B {
                def test() {
                    A.super.x + B.super.x
                }
            }
            assert new C().test() == 'AB'
        '''
    }

    @Test
    void testTraitSuperPropertySet() {
        assertScript shell, '''
            trait T {
                def x
            }
            class C implements T {
                def test() {
                    T.super.x = 'value'
                    return x
                }
            }
            assert new C().test() == 'value'
        '''

        def err = shouldFail shell, '''
            trait T {
                final x = 'const'
            }
            class C implements T {
                def test() {
                    T.super.x = 'value'
                    return x
                }
            }
            assert new C().test() == 'value'
        '''
        assert err =~ /No such property: super for class: T/

        // TODO: add support for compound assignment
        shouldFail shell, MissingPropertyException, '''
            trait T {
                def x = 'value'
            }
            class C implements T {
                def test() {
                    T.super.x -= ~/e\b/
                    T.super.x += 'able'
                    return x
                }
            }
            assert new C().test() == 'valuable'
        '''

        assertScript shell, '''
            trait T {
                def setX(value) { 'retval' }
            }
            class C implements T {
                def test() {
                    T.super.x = 'value'
                }
            }
            assert new C().test() == 'retval'
        '''
    }

    @Test // GROOVY-9672
    void testTraitSuperPropertySetStatic() {
        assertScript shell, '''
            trait T {
                static x
            }
            class C implements T {
                def test() {
                    T.super.x = 'value'
                    return x
                }
            }
            assert new C().test() == 'value'
        '''

        def err = shouldFail shell, '''
            trait T {
                static final x = 'const'
            }
            class C implements T {
                def test() {
                    T.super.x = 'value'
                    return x
                }
            }
            assert new C().test() == 'value'
        '''
        assert err =~ /No such property: super for class: T/

        assertScript shell, '''
            trait T {
                static setX(value) { 'retval' }
            }
            class C implements T {
                def test() {
                    T.super.x = 'value'
                }
            }
            assert new C().test() == 'retval'
        '''

        assertScript shell, '''
            trait A {
                static setX(value) { 'A' }
            }
            trait B {
                static setX(value) { 'B' }
            }
            class C implements A, B {
                def test() {
                    (A.super.x = 'a') + (B.super.x = 'b')
                }
            }
            assert new C().test() == 'AB'
        '''
    }

    @Test // GROOVY-9673
    void testTraitSuperPropertySetWithOverloads() {
        assertScript shell, '''
            trait T {
                def setX(Number n) {
                    'Number'
                }
                def setX(String s) {
                    'String'
                }
            }
            class C implements T {
                def test() {
                    T.super.x = 42
                }
            }
            assert new C().test() == 'Number'
        '''

        assertScript shell, '''
            trait T {
                def setX(Number n) {
                    'Number'
                }
                def setX(String s) {
                    'String'
                }
            }
            class C implements T {
                def test() {
                    T.super.x = 'x'
                }
            }
            assert new C().test() == 'String'
        '''
    }

    @Test // GROOVY-9672
    void testTraitSuperCallStatic() {
        assertScript shell, '''
            trait A {
                static m() { 'A' }
            }
            trait B {
                static m() { 'B' }
            }
            class C implements A, B {
                def test() {
                    m() + A.super.m() + B.super.m()
                }
            }
            assert new C().test() == 'BAB'
        '''
    }

    @Test
    void testTraitSuperCallWhenExtendingAnotherTrait() {
        assertScript shell, '''
            trait Foo {
                int foo() { 1 }
            }
            trait Bar extends Foo {
                int foo() {
                    2*Foo.super.foo()
                }
            }
            class Baz implements Bar {}
            def b = new Baz()
            assert b.foo() == 2
        '''

        assertScript shell, '''
            @CompileStatic
            trait Foo {
                int foo() { 1 }
            }
            @CompileStatic
            trait Bar extends Foo {
                int foo() {
                    2*Foo.super.foo()
                }
            }
            class Baz implements Bar {}
            def b = new Baz()
            assert b.foo() == 2
        '''
    }

    @Test // GROOVY-9256
    void testTraitSuperCallWithinClosure() {
        assertScript shell, '''
            trait T {
              int getX() { 42 }
            }
            class C implements T {
              def test() {
                { ->
                  T.super.getX()
                }()
              }
            }
            assert new C().test() == 42
        '''

        assertScript shell, '''
            trait T {
              int getX() { 42 }
            }
            class C implements T {
              def test() {
                { p = T.super.getX() ->
                  return p
                }()
              }
            }
            assert new C().test() == 42
        '''
    }

    @Test
    void testTraitShouldNotTakeOverSuperClassMethod() {
        assertScript shell, '''
            trait TestTrait {
                String foo() { 'from Trait' }
            }
            class Bar {
                String foo() { 'from Bar' }
            }
            class Baz extends Bar implements TestTrait {
                String foo() {
                    // force use of Bar#foo
                    super.foo()
                }
            }
            def b = new Baz()
            assert b.foo() == 'from Bar'
        '''
    }

    @Test
    void testTraitShouldTakeOverSuperClassMethod() {
        assertScript shell, '''
            trait TestTrait {
                String foo() { 'from Trait' }
            }
            class Bar {
                String foo() { 'from Bar' }
            }
            class Baz extends Bar implements TestTrait {}
            def b = new Baz()
            assert b.foo() == 'from Trait'
        '''
    }

    @Test
    void testOverrideUsingRuntimeTrait() {
        assertScript shell, '''
            trait TestTrait {
                String foo() { 'from Trait' }
            }
            class Bar {
                String foo() { 'from Bar' }
            }
            def b = new Bar() as TestTrait
            // when using runtime traits, inherits methods from trait!
            assert b.foo() == 'from Trait'
        '''

        assertScript shell, '''
            trait TestTrait {
                String foo() { 'from Trait' }
            }
            class Bar {
                String foo() { 'from Bar' }
            }
            class Baz extends Bar {}
            def b = new Baz() as TestTrait
            // when using runtime traits, inherits methods from trait!
            assert b.foo() == 'from Trait'
        '''
    }

    @Test
    void testTraitOverrideHierarchy() {
        assertScript shell, '''
            trait TestTrait {
                String foo() { 'from Trait' }
                String bar() { 'from Trait' }
            }
            class Top implements TestTrait {} // top has default implementation
            class Middle extends Top {
                String foo() { 'from Middle' } // middle overrides default implementation
                String bar() { 'from Middle' } // middle overrides default implementation
            }
            class Bottom extends Middle implements TestTrait {} // bottom restores default implementation
            def top = new Top()
            def middle = new Middle()
            def bottom = new Bottom()

            assert top.foo() == 'from Trait'
            assert top.bar() == 'from Trait'

            assert middle.foo() == 'from Middle'
            assert middle.bar() == 'from Middle'

            assert bottom.foo() == 'from Trait'
            assert bottom.bar() == 'from Trait'
        '''
    }

    @Test
    void testSAMCoercion1() {
        assertScript shell, '''
            trait SAMTrait {
                String foo() { bar()+bar() }
                abstract String bar()
            }
            SAMTrait sam = { 'hello' }
            assert sam.foo() == 'hellohello'
        '''

        assertScript shell, '''
            trait SAMTrait {
                String foo() { bar()+bar() }
                abstract String bar()
            }
            @CompileStatic
            void test() {
                SAMTrait sam = { 'hello' }
                assert sam.foo() == 'hellohello'
            }
        '''
    }

    @Test
    void testSAMCoercion2() {
        assertScript shell, '''
            trait SAMTrait {
                String foo() { bar()+bar() }
                abstract String bar()
            }
            void test(SAMTrait sam) {
                assert sam.foo() == 'hellohello'
            }
            test { 'hello' } // SAM coercion
        '''

        assertScript shell, '''
            trait SAMTrait {
                String foo() { bar()+bar() }
                abstract String bar()
            }
            void test(SAMTrait sam) {
                assert sam.foo() == 'hellohello'
            }
            @CompileStatic
            void doTest() {
                test { 'hello' } // SAM coercion
            }
            doTest()
        '''
    }

    @Test
    void testSAMCoercion3() {
        assertScript shell, '''
            trait Greeter {
                abstract String getName()
                String greet() { "Hello $name" }
            }
            Greeter greeter = { 'Alice' }
            assert greeter.greet() == 'Hello Alice'
            assert greeter.getName().equals('Alice')
        '''
    }

    @Test
    void testSAMCoercion4() {
        assertScript shell, '''
            trait Greeter {
                abstract String getName()
                String greet() { "Hello $name" }
            }
            def greeter = { 'Alice' } as Greeter
            assert greeter.greet() == 'Hello Alice'
            assert greeter.getName().equals('Alice')
        '''
    }

    @Test // GROOVY-8243
    void testSAMCoercion5() {
        assertScript shell, '''
            trait T {
                abstract foo(int n)
                def bar(double n) {
                    "trait $n".toString()
                }
            }
            interface I extends T {
            }

            I obj = { "proxy $it".toString() }
            assert obj.foo(123) == 'proxy 123'
            assert obj.bar(4.5) == 'trait 4.5'
        '''
    }

    @Test // GROOVY-8244
    void testSAMCoercion6() {
        assertScript shell, '''
            trait T {
                abstract def foo(int a, int b = 2)
            }
            T t = { int a, int b ->
                return a + b
            }
            assert t.foo(40) == 42
        '''
    }

    @Test
    void testMethodMissingInTrait() {
        assertScript shell, '''
            trait MethodMissingProvider {
                def methodMissing(String name, args) {
                    name
                }
            }
            class Foo implements MethodMissingProvider {}
            def foo = new Foo()
            assert foo.bar() == 'bar'
        '''
    }

    @Test
    void testPropertyMissingInTrait() {
        assertScript shell, '''
            trait PropertyMissingProvider {
                def propertyMissing(String name) {
                    name
                }
            }
            class Foo implements PropertyMissingProvider {}
            def foo = new Foo()
            assert foo.bar == 'bar'
        '''
    }

    @Test
    void testShouldUseDefinitionFromClassInsteadOfTrait() {
        assertScript shell, '''
            trait TestTrait {
                String foo() { 'from Trait' }
            }
            class Bar implements TestTrait {
                String foo() { 'from Bar' }
            }
            def b = new Bar()
            assert b.foo() == 'from Bar'
        '''
    }

    @Test
    void testPrivateFieldNameConflict() {
        assertScript shell, '''
            trait Trait1 { private int v = 111; int getValueFromTrait1() { v } }
            trait Trait2 { private int v = 222; int getValueFromTrait2() { v } }
            class Impl implements Trait1,Trait2 {}
            def t = new Impl()
            assert t.valueFromTrait1 == 111
            assert t.valueFromTrait2 == 222
        '''
    }

    @Test
    void testPrivateMethodInTrait() {
        assertScript shell, '''
            trait DoingSecretThings {
                private String secret() { 'secret' }
                String foo() { secret() }
            }
            class Foo implements DoingSecretThings {}
            def foo = new Foo()
            assert foo.foo() == 'secret'
        '''
    }

    @Test
    void testPrivateMethodInTraitAccessingPrivateField() {
        assertScript shell, '''
            trait DoingSecretThings {
                private int x = 0
                private int secret() { x+=1; x }
                int foo() { secret() }
            }
            class Foo implements DoingSecretThings {}
            def foo = new Foo()
            assert foo.foo() == 1
        '''
    }

    @Test
    void testPrivateMethodInTraitWithCompileStatic() {
        assertScript shell, '''
            @CompileStatic
            trait DoingSecretThings {
                private String secret() { 'secret' }
                String foo() { secret() }
            }
            class Foo implements DoingSecretThings {}
            def foo = new Foo()
            assert foo.foo() == 'secret'
        '''
    }

    @Test
    void testPrivateMethodInTraitAccessingPrivateFieldCompileStatic() {
        assertScript shell, '''
            @CompileStatic
            trait DoingSecretThings {
                private int x = 0
                private int secret() { x+=1; x }
                int foo() { secret() }
            }
            class Foo implements DoingSecretThings {}
            def foo = new Foo()
            assert foo.foo() == 1
            assert foo.foo() == 2
        '''
    }

    @Test
    void testNoShadowingPrivateMethodInTraitAccessingPrivateFieldCompileStatic() {
        assertScript shell, '''
            @CompileStatic
            trait DoingSecretThings {
                private int x = 0
                private int secret() { x+=1; x }
                int foo() { secret() }
            }
            class Foo implements DoingSecretThings {
                int secret() { 666 }
            }
            def foo = new Foo()
            assert foo.foo() == 1
            assert foo.foo() == 2
        '''
    }

    @Test
    void testNoShadowingPrivateMethodInTraitAccessingPrivateField() {
        assertScript shell, '''
            trait DoingSecretThings {
                private int x = 0
                private int secret() { x+=1; x }
                int foo() { secret() }
            }
            class Foo implements DoingSecretThings {
                int secret() { 666 }
            }
            def foo = new Foo()
            assert foo.foo() == 1
            assert foo.foo() == 2
        '''
    }

    @Test
    void testMixPrivatePublicMethodsOfSameName() {
        def err = shouldFail shell, '''
            trait DoingSecretThings {
                private String secret(String s) { s.toUpperCase() }
                String secret() { 'public' }
                String foo() { secret('secret') }
            }
            class Foo implements DoingSecretThings {}
            def foo = new Foo()
            assert foo.foo() == 'SECRET'
        '''

        assert err =~ 'Mixing private and public/protected methods of the same name causes multimethods to be disabled'
    }

    @Test
    void testInterfaceExtendingTraitShouldNotTriggerRuntimeError() {
        assertScript shell, '''
            trait A {
                void foo() { println 'A' }
            }
            trait B extends A {
               void bar() {  println 'B'  }
            }

            interface C extends B {
               void baz()
            }
            abstract class D implements C {}
            def d = { println 'BAZ' } as D
            d.foo()
            d.bar()
            d.baz()
        '''
    }

    @Test
    void testTraitWithDelegate() {
        assertScript shell, '''
            trait ListTrait<T> {
                private @Delegate ArrayList<T> list = new ArrayList<T>()
            }
            class Person implements ListTrait<String> {
                void foo() {
                    add('bar')
                }
            }
            def p = new Person()
            p.foo()
            assert p.get(0) == 'bar'
        '''
    }

    @Test // GROOVY-7288
    void testClassWithTraitDelegate() {
        assertScript shell, '''
            trait T {
                final foo = 'bar'
            }
            class D implements T {
                def m() {
                    return 'baz'
                }
            }
            class C { // The class must be declared abstract or the method 'java.lang.String T__foo$get()' must be implemented
                private @Delegate D provider = new D()
            }
            def c = new C()
            assert c.foo == 'bar'
            assert c.m() == 'baz'
        '''
    }

    @Test // GROOVY-9739
    void testTraitExtendsTraitWithDelegate() {
        assertScript shell, '''
            class Main implements ClientSupport {
                static main(args) {
                    def tester = new Main(client: new Client())

                    assert tester.isReady()
                    assert tester.client.getValue() == 'works'
                }
            }

            class Client {
                def getValue() { 'works' }
                boolean waitForServer(int seconds) { true }
            }

            trait ClientDelegate {
                @Delegate Client client
            }

            trait ClientSupport implements ClientDelegate {
                boolean isReady() {
                    boolean ready = client.waitForServer(60)
                    // assert, log, etc.
                    return ready
                }
            }
        '''
    }

    @Test // GROOVY-9901
    void testTraitWithMemozied() {
        assertScript shell, '''
            trait Foo {
                @Memoized
                double method() {
                    Math.random()
                }
            }
            class Bar implements Foo {}
            class Baz implements Foo {}

            def bar = new Bar()
            def a = bar.method()
            def b = bar.method()
            def c = new Bar().method()
            def d = new Baz().method()

            assert a == b
            assert a != c
            assert a != d
            assert c != d
        '''
    }

    @Test
    void testAnnotationShouldBeCarriedOver() {
        assertScript shell, '''
            trait Foo {
                @Deprecated void foo() { 'ok' }
            }
            @ASTTest(phase=CANONICALIZATION, value={
                assert node.getDeclaredMethod('foo').annotations.any {
                    it.classNode.nameWithoutPackage == 'Deprecated'
                }
            })
            class Bar implements Foo {
            }
            def b = new Bar()
            b.foo()
        '''
    }

    @Test // GROOVY-10553
    void testAnnotationShouldBeCarriedOver2() {
        assertScript shell, '''
            import java.lang.annotation.*
            @Retention(RetentionPolicy.RUNTIME)
            @Target([ElementType.FIELD,ElementType.TYPE_USE])
            @interface Foo {
            }

            trait Bar {
                @Foo String string
            }
            class Baz implements Bar {
            }

            @ASTTest(phase=CLASS_GENERATION, value={
                def type = node.rightExpression.type

                assert type.name == 'Baz'
                def field = type.getField('Bar__string')
                assert field.type.typeAnnotations.size() == 1

                field = type.interfaces[1].getField('$ins$1Bar__string')
                assert field.type.typeAnnotations.size() == 1 // no duplicate
            })
            def baz = new Baz(string:'foobar')
        '''
    }

    @Test
    void testShouldCompileTraitMethodStatically() {
        def err = shouldFail shell, '''
            @CompileStatic
            trait Foo {
                int foo() { 1+'foo'}
            }
        '''
        assert err =~ 'Cannot return value of type java.lang.String for method returning int'
    }

    @Test
    void testTraitStaticMethod() {
        assertScript shell, '''
            trait StaticProvider {
                static String foo() { 'static method' }
            }
            class Foo implements StaticProvider {}
            assert Foo.foo() == 'static method'
        '''

        assertScript shell, '''
            trait StaticProvider {
                static String foo() { bar() }
                static String bar() { 'static method' }
            }
            class Foo implements StaticProvider {}
            assert Foo.foo() == 'static method'
        '''
    }

    @Test
    void testTraitStaticField() {
        assertScript shell, '''
            trait StaticFieldProvider {
                public static int VAL = 123
            }
            class Foo implements StaticFieldProvider {}
            assert Foo.StaticFieldProvider__VAL == 123
        '''

        assertScript shell, '''
            trait StaticFieldProvider {
                public static int VAL = 123
                public static void update(int x) { VAL = x }
            }
            class Foo implements StaticFieldProvider {}
            assert Foo.StaticFieldProvider__VAL == 123
            Foo.update(456)
            assert Foo.StaticFieldProvider__VAL == 456
        '''
    }

    @Test
    void testTraitStaticProperty() {
        assertScript shell, '''
            trait StaticPropertyProvider {
                static int VAL = 123
                public static void update(int x) { VAL = x }
            }
            class Foo implements StaticPropertyProvider {
            }
            assert Foo.VAL == 123
            Foo.update(456)
            assert Foo.VAL == 456
        '''

        assertScript shell, '''
            trait T {
                static p = 1
            }
            class C implements T {
                static m() {
                    setP(2)
                    setP(getP() + 1)
                    return getP()
                }
            }
            assert C.m() == 3
        '''

        // GROOVY-9678
        assertScript shell, '''
            trait T {
                static p = 1
            }
            class C implements T {
                static m() {
                    p = 2
                    p += 1
                    return p
                }
            }
            assert C.m() == 3
        '''
    }

    @Test
    void testTraitMethodShouldBeDefaultImplementationUsingReflection() {
        assertScript shell, '''
            trait Foo {
                void foo() {}
            }

            class Bar implements Foo {}

            def method = Bar.getDeclaredMethod('foo')
            assert method.declaringClass == Bar
            assert Traits.isBridgeMethod(method)
            def traitMethod = Traits.getBridgeMethodTarget(method)
            assert traitMethod != null
            assert traitMethod.declaringClass == Foo
            assert traitMethod.name == 'foo'
        '''
    }

    @Test
    void testTraitMethodShouldNotBeDefaultImplementationUsingReflection() {
        assertScript shell, '''
            trait Foo {
                void foo() {}
            }

            class Bar implements Foo {
                void foo() { }
            }

            def method = Bar.getDeclaredMethod('foo')
            assert method.declaringClass == Bar
            assert !Traits.isBridgeMethod(method)
            def traitMethod = Traits.getBridgeMethodTarget(method)
            assert traitMethod == null
        '''
    }

    @Test
    void testTraitMethodShouldBeDefaultImplementationUsingReflectionAndGenericTypes() {
        assertScript shell, '''
            trait Foo<F,T> {
                T foo(F from) {}
            }

            class Bar implements Foo<String,Integer> {}

            def method = Bar.getDeclaredMethod('foo', String)
            assert method.declaringClass == Bar
            assert Traits.isBridgeMethod(method)
            def traitMethod = Traits.getBridgeMethodTarget(method)
            assert traitMethod != null
            assert traitMethod.declaringClass == Foo
            assert traitMethod.name == 'foo'
            assert traitMethod.parameterTypes.length==1
            assert traitMethod.parameterTypes[0] == Object
        '''
    }

    @Test
    void testUseOfThisInInitializer() {
        assertScript shell, '''
            trait Dummyable  {
                String x = this.class.name

                void info() {
                    assert x == this.class.name
                }
            }

            class Util implements Dummyable {}

            def util = new Util()
            util.info()
        '''
    }

    @Test
    void testUseOfMethodInInitializer() {
        assertScript shell, '''
            trait Dummyable  {
                String x = whoAmI()

                String whoAmI() { this.class.name }

                void info() {
                    assert x == this.class.name
                }
            }


            class Util implements Dummyable {}

            def util = new Util()
            util.info()
        '''
    }

    @Test
    void testTraitShouldNotBeAllowedToExtendInterface() {
        // GROOVY-6672
        def err = shouldFail shell, '''
            trait Foo extends Serializable {}
            Foo x = null
        '''
        assert err =~ 'Trait cannot extend an interface.'
    }

    @Test
    void testImplementingingAbstractClass() {
        assertScript shell, '''
            abstract class AbstractSomething {
                abstract String something()
            }

            trait SomethingDoing {
                String something() {
                    "Doing something"
                }
            }

            class Something extends AbstractSomething implements SomethingDoing {
                String foo() {
                    something()
                }
            }

            assert new Something().foo() == 'Doing something'
        '''
    }

    @Test
    void testShouldNotOverrideMethodImplementedFromAbstractClass() {
        assertScript shell, '''
            abstract class AbstractSomething {
                abstract String something()
            }

            trait SomethingDoing {
                String something() {
                    "Doing something"
                }
            }

            class Something extends AbstractSomething  {
                String something() { 'implemented' }
            }

            class BottomSomething extends Something implements SomethingDoing {
                String something() {
                    // in order to avoid getting the default impl from the trait, need to call super
                    super.something()
                }
                String foo() {
                    something()
                }
            }

            assert new BottomSomething().foo() == 'implemented'
        '''
    }

    @Test
    void testIncrementPropertyOfTrait() {
        assertScript shell, '''
            trait Level {
                int maxLevel
                int currentLevel = 0

                void foo() {
                    if( currentLevel < maxLevel ) {
                        currentLevel += 1
                    }
                }
            }

            class Leveller implements Level {
                Leveller() {
                    maxLevel = 3
                }
            }

            def v = new Leveller()
            v.foo()
            v.foo()
            v.foo()
            v.foo()
            assert v.currentLevel == 3
        '''
    }

    @Test
    void testIncrementPropertyOfTraitUsingPlusPlus() {
        def err = shouldFail shell, '''
            trait Level {
                int maxLevel
                int currentLevel = 0

                void foo() {
                    if( currentLevel < maxLevel ) {
                        currentLevel++
                    }
                }
            }

            class Leveller implements Level {
                Leveller() {
                    maxLevel = 3
                }
            }

            def v = new Leveller()
            v.foo()
            v.foo()
            v.foo()
            v.foo()
            assert v.currentLevel == 3
        '''

        assert err =~ 'Postfix expressions on trait fields/properties are not supported in traits'
    }

    @Test
    void testIncrementPropertyOfTraitUsingPrefixPlusPlus() {
        def err = shouldFail shell, '''
            trait Level {
                int maxLevel
                int currentLevel = 0

                void foo() {
                    if( currentLevel < maxLevel ) {
                        ++currentLevel
                    }
                }
            }

            class Leveller implements Level {
                Leveller() {
                    maxLevel = 3
                }
            }

            def v = new Leveller()
            v.foo()
            v.foo()
            v.foo()
            v.foo()
            assert v.currentLevel == 3
        '''

        assert err =~ 'Prefix expressions on trait fields/properties are not supported in traits'
    }

    @Test // GROOVY-6691
    void testTraitImplementingGenericSuperTrait() {
        assertScript shell, '''
            class App {}
            trait Base<T> {
                T value
                void set(T v) { value = v}
                T get() { v }
            }
            trait Applicative extends Base<App> { }
            class Dummy implements Applicative {}
            @TypeChecked
            void test() {
                def d = new Dummy()
                d.set(new App())
            }
            test()
        '''

        def err = shouldFail shell, '''
            class App {}
            trait Base<T> {
                T value
                void set(T v) { value = v}
                T get() { v }
            }
            trait Applicative extends Base<App> { }
            class Dummy implements Applicative {}
            @TypeChecked
            void test() {
                def d = new Dummy()
                d.set('oh noes!')
            }
            test()
        '''

        assert err =~ /Cannot call Dummy#set\(App\) with arguments \[java.lang.String\]/
    }

    @Test
    void testUpdateFieldFromOtherReceiver() {
        assertScript shell, '''
            class Person {
                String name
            }
            trait PersonUpdater {
                void update(Person p) {
                    p.name = 'Test'
                }
            }
            class Updater implements PersonUpdater{}
            def p = new Person()
            def u = new Updater()
            u.update(p)
            assert p.name == 'Test'
        '''
    }

    @Test
    void testUseStaticFieldInTraitBody() {
        assertScript shell, '''
            import java.util.logging.Logger

            trait Loggable {

                static def LOGGER = Logger.getLogger(this.class.name)

                void info(String msg) {
                    LOGGER.info(msg)
                }
            }

            class Test implements Loggable {}

            def t = new Test()
            t.info('foo')
        '''
    }

    @Test
    void testUpdateStaticFieldInTraitBody() {
        assertScript shell, '''
            trait Loggable {

                static int CALLS = 0

                int call() {
                    CALLS += 1
                    CALLS
                }
            }

            class Test implements Loggable {}

            def t = new Test()
            assert t.call() == 1
            assert t.call() == 2
            assert Test.CALLS == 2
        '''
    }

    @Test
    void testProxyTarget() {
        assertScript shell, '''
            trait Helloable implements CharSequence {
                void hello() { println "hello" }
            }

            def x = new String("hello") as Helloable
            x.hello()
            assert !(x instanceof String)
            assert x instanceof Helloable
            assert x instanceof GeneratedGroovyProxy
            assert x.toUpperCase() == "HELLO" // expected
            assert x.proxyTarget.tr('h','*') == "*ello"
        '''
    }

    @Test
    void testTraitsGetAsType() {
        assertScript shell, '''
            trait Helloable implements CharSequence {
                void hello() { println "hello" }
            }
            def str = "hello"
            def x = str as Helloable
            x.hello()
            assert !(x instanceof String)
            assert x instanceof Helloable
            assert x instanceof GeneratedGroovyProxy
            assert x.toUpperCase() == "HELLO" // expected
            assert x.proxyTarget.tr('h','*') == "*ello"
            def proxyTarget = x.proxyTarget
            assert proxyTarget.is(str)
            def converted = Traits.getAsType(x,String)
            assert converted.is(str)
        '''
    }

    @Test
    void testStackableTraits() {
        assertScript shell, '''
            trait A {
                int foo(int x) { x }
            }
            trait B {
                int foo(int x) { x<10?2*super.foo(x):x }
            }
            class C implements A,B {}
            def c = new C()
            (0..9).each {
                assert c.foo(it) == 2*it
            }
            (10..20).each {
                assert c.foo(it) == it
            }
        '''
    }

    @Test
    void testStackableTraitsWithExplicitClasses() {
        assertScript shell, '''
            interface IntQueue {
                Integer get()
                void put(Integer x)
            }
            trait Incrementing {
                void put(Integer x) { println 'Incrementing';super.put(x+1) }
            }
            trait Filtering {
                void put(Integer x) { println 'Filtering'; if(x >=0) super.put(x) }
            }

            class BasicIntQueue implements IntQueue{
                private buf = new ArrayList<Integer>()
                Integer get() { buf.remove(0) }
                void put(Integer x) { buf << x}
                String toString() { buf.toString() }
            }
            class Sub extends BasicIntQueue implements Incrementing, Filtering {}

            def queue = new Sub()
            //queue.put(-1) // filtering -> sink
            queue.put(0) // filtering ok -> incrementing.put -> 1
            queue.put(1)  // filtering ok -> goes to incrementing.put -> 2
            assert queue.get() == 1
            assert queue.get() == 2
            assert queue.toString() == "[]"

            class Sub2 extends BasicIntQueue implements Filtering, Incrementing {}

            def queue2 = new Sub2()
            queue2.put(-1) // incrementing -> put(0) -> filtering ok -> 0
            queue2.put(0) // incrementing -> put(1) -> filtering ok -> 1
            queue2.put(1) // incrementing -> put(2) -> filtering ok -> 2
            assert queue2.get() == 0
            assert queue2.get() == 1
            assert queue2.get() == 2
            assert queue2.toString() == "[]"
        '''
    }

    @Test
    void testStackableTraitsWithDynamicTraits() {
        assertScript shell, '''
            interface IntQueue {
                Integer get()
                void put(Integer x)
            }
            trait Incrementing {
                void put(Integer x) { println 'Incrementing';super.put(x+1) }
            }
            trait Filtering {
                void put(Integer x) { println 'Filtering'; if(x >=0) super.put(x) }
            }

            class BasicIntQueue implements IntQueue{
                private buf = new ArrayList<Integer>()
                Integer get() { buf.remove(0) }
                void put(Integer x) { buf << x}
                String toString() { buf.toString() }
            }

            def queue = new BasicIntQueue().withTraits Incrementing, Filtering
            queue.put(-1) // filtering -> skink
            queue.put(0) // filtering ok -> incrementing -> [1]
            queue.put(1) // filtering ok -> incrementing -> [1,2]
            assert queue.get() == 1
            assert queue.get() == 2
            assert queue.proxyTarget.toString() == "[]"

            def queue2 = new BasicIntQueue().withTraits Filtering, Incrementing
            queue2.put(-1) // incrementing -> 0 -> filtering -> ok -> [0]
            queue2.put(0) // incrementing -> 1 -> filtering ok -> [0,1]
            queue2.put(1) // incrementing -> 2 -> filtering ok -> [0,1,2]
            assert queue2.get() == 0
            assert queue2.get() == 1
            assert queue2.get() == 2
            assert queue.proxyTarget.toString() == "[]"
        '''
    }

    @Test
    void testSuperKeywordInRegularTraitInheritance() {
        assertScript shell, '''
            trait A {
                int foo(x) { 1+x }
            }
            trait B extends A {
                int foo(x) { 2*super.foo(x)}
            }
            class C implements B {}
            def c = new C()
            assert c.foo(2) == 6
        '''
    }

    @Test
    void testSuperKeywordInRegularTraitMultipleInheritance() {
        assertScript shell, '''
            trait A {
                int foo(x) { 1+x }
            }
            trait A2 {
                int foo(x) { 1+super.foo(x) }
            }
            trait B implements A,A2 {
                int foo(x) { 2*super.foo(x)}
            }
            class C implements B {}
            def c = new C()
            assert c.foo(2) == 8
        '''
    }

    @Test
    void testStaticallyCompiledTraitWithCallToSuper() {
        assertScript shell, '''
            @CompileStatic
            trait A {
                int foo(int x) { 1+x }
            }
            @CompileStatic
            trait B extends A {
                int foo(int x) { 2*super.foo(x)}
            }
            class C implements B {}
            def c = new C()
            assert c.foo(2) == 6
        '''
    }

    @Test
    void testStaticallyCompiledTraitWithCallToSuperInPackage() {
        assertScript shell, '''
            package blah
            @CompileStatic
            trait A {
                int foo(int x) { 1+x }
            }
            @CompileStatic
            trait B extends A {
                int foo(int x) { 2*super.foo(x)}
            }
            class C implements B {}
            def c = new C()
            assert c.foo(2) == 6
        '''
    }

    @Test
    void testStaticallyCompiledTraitWithCallToSuperInPackageAndUnderscoreInClassName() {
        assertScript shell, '''
            package blah
            @CompileStatic
            trait A {
                int foo(int x) { 1+x }
            }
            @CompileStatic
            trait B_B extends A {
                int foo(int x) { 2*super.foo(x)}
            }
            class C implements B_B {}
            def c = new C()
            assert c.foo(2) == 6
        '''
    }

    @Test
    void testStaticallyCompiledTraitWithCallToSuperAndNoExplicitSuperTrait() {
        assertScript shell, '''
            @CompileStatic
            trait A {
                int foo(int x) { 1+x }
            }
            @CompileStatic
            trait B {
                int foo(int x) { 2*(int)super.foo(x)}
            }
            class C implements A,B {}
            def c = new C()
            assert c.foo(2) == 6
        '''
    }

    @Test
    void testFieldInTraitAndDynamicProxy() {
        assertScript shell, '''
            trait WithName {
                public String name
            }
            WithName p = new Object() as WithName
            p.WithName__name = 'foo'
            assert p.WithName__name == 'foo'
        '''
    }

    @Test
    void testFieldInTraitModifiers() {
        assertScript shell, '''
            trait A {
                public int foo
            }
            @ASTTest(phase=INSTRUCTION_SELECTION,value={
                def field = node.getField('A__foo')
                assert field.isPublic()
            })
            class B implements A {}
            def b = new B()
        '''

        assertScript shell, '''
            import static java.lang.reflect.Modifier.isPrivate

            trait A {
                private int foo
            }
            @ASTTest(phase=INSTRUCTION_SELECTION,value={
                def field = node.getField('A__foo')
                assert isPrivate(field.modifiers)
            })
            class B implements A {}
            def b = new B()
        '''
    }

    @Test
    void testDecorateFinalClassWithTrait() {
        assertScript shell, '''
            trait Filtering {
                StringBuilder append(String str) {
                    def subst = str.replace('o','')
                    super.append(subst)
                }
                String toString() { super.toString() }
            }
            def sb = new StringBuilder().withTraits Filtering
            sb.append('Groovy')
            assert sb.toString() == 'Grvy'
        '''
    }

    @Test // GROOVY-6708
    void testCovariantReturnTypeWithGenericsInheritance() {
        assertScript shell, '''
            trait Top<X> {
                X self(X x) {x}
            }
            trait Bottom<X> extends Top<X> {}
            class A implements Bottom<Integer> {}
            def a = new A()
            assert a.self(15) == 15
        '''
    }

    @Test
    void testSuperCallInTraitAndDeepHierarchy() {
        assertScript shell, '''
            interface IntQueue {
                Integer get()
                void put(Integer x)
            }

            trait Incrementing/* implements IntQueue */{
                void put(Integer x) {
                    println 'Incrementing'
                    super.put(x+1)
                }
            }
            trait Filtering/* implements IntQueue */{
                void put(Integer x) {
                    println 'Filtering'
                    if(x > 0) {
                      println "Value $x, delegating to super"
                      super.put(x)
                   }
                }
            }

            class BasicIntQueue implements IntQueue {
                private buf = new ArrayList<Integer>()
                Integer get() { buf.remove(0) }
                void put(Integer x) { println 'BasicIntQueue'; buf << x}
                String toString() { buf.toString() }
            }

            class IncrementingQueue extends BasicIntQueue implements Incrementing {}
            class FilteringIncrementingQueue extends IncrementingQueue implements Filtering {}

            def queue = new FilteringIncrementingQueue()
            queue.put(-1)
            queue.put(0)
            queue.put(1)
            assert queue.get() == 2
            assert queue.toString() == "[]"
        '''
    }

    @Test
    void testCallToSuperTraitWithStackable() {
        assertScript shell, '''
            trait T2 {
                void foo() {
                    println 'T2'
                    super.foo()
                }
            }
            trait T3 {
                void foo() {
                    println 'T3'
                    super.foo()
                }
            }
            class D implements T2, T3 {
                void foo() {
                    T3.super.foo() // explicit call
                    println "D::foo"
                }
            }
            def d = new D()
            try {
            d.foo()
            } catch (MissingMethodException) {
                // will fail because T2 calls super.foo() and D.super doesn't define foo
            }
        '''
    }

    @Test // GROOVY-7058
    void testShouldNotThrowNPEBecauseOfIncompleteGenericsTypeInformation() {
        assertScript shell, '''
            class Project { Task task(String name, Map args) {} }
            class Task {}
            interface Plugin<P>{}
            trait PluginUtils {
                abstract Project getProject()

                public <T extends Task> T createTask(String name, Class<T> type, Closure<?> config) {
                  project.task(name, type: type, config)
                }
            }

            class MyPlugin implements Plugin<Project>, PluginUtils { Project project }
            new MyPlugin()
        '''
    }

    @Test // GROOVY-7123
    void testHelperSetterShouldNotReturnVoid() {
        assertScript shell, '''
            trait A {
                def foo
                def bar() { foo = 42 }
            }
            class C implements A {}

            assert new C().bar() == 42
        '''
    }

    @Test
    void testSimpleSelfType() {
        assertScript shell, '''
            trait A {
                int a() { 1 }
            }

            @CompileStatic
            @SelfType(A)
            trait B {
                int b() { 2*a() }
            }
            class C implements A,B {}
            def c = new C()
            assert c.b() == 2
        '''
    }

    @Test
    void testSimpleSelfTypeInSubTrait() {
        assertScript shell, '''
            trait A {
                int a() { 1 }
            }

            @CompileStatic
            @SelfType(A)
            trait B {
                int b() { 2*a() }
            }

            @CompileStatic
            trait SubB extends B {
                int c() { 3*a() }
            }


            class C implements A,SubB {}
            def c = new C()
            assert c.c() == 3
        '''
    }

    @Test // GROOVY-10767
    void testSimpleSelfTypeInSubTrait2() {
        assertScript shell, '''
            trait A {
                void methodA() {
                }
            }
            @TypeChecked
            @SelfType(T)
            trait B implements A {
                void methodB() {
                    methodA() // Cannot find matching method <UnionType:T+B>#methodA()
                }
            }
            class C extends T implements B {
                void method() {
                    methodA()
                    methodB()
                }
            }
            class T {
                void methodT() {
                }
            }

            new C().method()
        '''
    }

    @Test
    void testDoubleSelfType() {
        assertScript shell, '''
            trait A {
                int a() { 1 }
            }
            trait A2 {
                int a2() { 2 }
            }

            @CompileStatic
            @SelfType([A,A2])
            trait B {
                int b() { 2*a()*a2() }
            }
            class C implements A,A2,B {}
            def c = new C()
            assert c.b() == 4
        '''
    }

    @Test
    void testClassDoesNotImplementSelfType() {
        def err = shouldFail shell, '''
            @CompileStatic
            @SelfType([String,Serializable])
            trait B {
                String b() { toUpperCase() }
            }
            class C implements B {}
            def c = new C()
        '''

        assert err =~ "class 'C' implements trait 'B' but does not extend self type class 'java.lang.String'"
        assert err =~ "class 'C' implements trait 'B' but does not implement self type interface 'java.io.Serializable'"
    }

    @Test
    void testClassDoesNotImplementSelfTypeDefinedInInheritedTrait() {
        def err = shouldFail shell, '''
            interface Self { def bar() }
            @SelfType(Self)
            trait Trait {
                def foo() { bar() }
            }
            interface Middle extends Trait { }
            class Child implements Middle { }
            new Child().foo()
        '''

        assert err =~ "class 'Child' implements trait 'Trait' but does not implement self type interface 'Self'"
    }

    @Test
    void testClassDoesNotImplementSelfTypeUsingAbstractClass() {
        def err = shouldFail shell, '''
            @CompileStatic
            @SelfType([String,Serializable])
            trait B {
                String b() { toUpperCase() }
            }
            abstract class C implements B {}
            class D extends C {}
            def c = new D()
        '''

        assert err =~ "class 'C' implements trait 'B' but does not extend self type class 'java.lang.String'"
        assert err =~ "class 'C' implements trait 'B' but does not implement self type interface 'java.io.Serializable'"
    }

    @Test
    void testMethodAcceptingThisAsSelfTrait() {
        assertScript shell, '''
            class CommunicationService {
                static void sendMessage(String from, String to, String message) {
                    println "$from sent [$message] to $to"
                }
            }

            class Device { String id }

            @SelfType(Device)
            @CompileStatic
            trait Communicating {
                void sendMessage(Device to, String message) {
                    SecurityService.check(this)
                    CommunicationService.sendMessage(id, to.id, message)
                }
            }

            class MyDevice extends Device implements Communicating {}

            def bob = new MyDevice(id:'Bob')
            def alice = new MyDevice(id:'Alice')
            bob.sendMessage(alice,'secret')

            class SecurityService {
                static void check(Device d) { if (d.id==null) throw new SecurityException() }
            }
        '''
    }

    @Test
    void testRuntimeSelfType() {
        assertScript shell, '''
            trait A {
                int a() { 1 }
            }

            @CompileStatic
            @SelfType(A)
            trait B {
                int b() { 2*a() }
            }
            class C implements A {}
            def c = new C() as B
            assert c.b() == 2
        '''
    }

    @Test
    void testRuntimeSelfTypeWithInheritance() {
        assertScript shell, '''
            trait A {
                int a() { 1 }
            }

            @CompileStatic
            @SelfType(A)
            trait B {
                int b() { 2*a() }
            }

            trait B2 extends B {}

            class C implements A {}
            def c = new C() as B2
            assert c.b() == 2
        '''
    }

    @SelfType([String, Date])
    trait DoubleSelfTypeTrait {}

    @Test
    void testAnnotationsOfPrecompiledTrait() {
        def cn = ClassHelper.make(DoubleSelfTypeTrait)
        def ann = cn.getAnnotations(ClassHelper.make(SelfType))
        assert ann.size() == 1
        def st = ann[0]
        def val = st.getMember('value')
        assert val instanceof ListExpression
        val.expressions.each {
            assert it instanceof ClassExpression
        }
    }

    trait T10521 {
        def m(Class<?> clazz, Object... array) {
            clazz.name + array
        }
    }

    @Test // GROOVY-10521
    void testVariadicMethodOfPrecompiledTrait() {
        assertScript shell, """import org.codehaus.groovy.ast.*
            class CT implements ${T10521.name} {
                def n(Class<?> clazz, Object... array) {
                }
            }

            def cn = new ClassNode(${T10521.name})
            def mn = cn.getMethods('m')[0]
            def td = mn.typeDescriptor

            assert td == 'java.lang.Object m(java.lang.Class, java.lang.Object[])'
        """

        System.setProperty('spock.iKnowWhatImDoing.disableGroovyVersionCheck','true')
        assertScript shell, """
            @Grab('org.spockframework:spock-core:2.4-M1-groovy-4.0')
            @GrabExclude('org.apache.groovy:*')
            import spock.lang.Specification

            class C extends Specification implements ${T10521.name} {
                void test() {
                  when:
                    String result = m(Object,'x')
                  then:
                    result == 'java.lang.Object[x]'
                }
            }

            org.junit.runner.JUnitCore.runClasses(C)
        """
    }

    @Test // GROOVY-7287
    void testTraitWithMethodLevelGenericsShadowing1() {
        assertScript shell, '''
            trait Configurable<ConfigObject> {
                ConfigObject configObject

                void configure(Closure<Void> configSpec) {
                    configSpec.resolveStrategy = Closure.DELEGATE_FIRST
                    configSpec.delegate = configObject
                    configSpec()
                }
            }
            public <T,U extends Configurable<T>> U configure(Class<U> clazz, @DelegatesTo(type="T") Closure configSpec) {
                Configurable<T> obj = (Configurable<T>) clazz.newInstance()
                obj.configure(configSpec)
                obj
            }


            class Module implements Configurable<ModuleConfig> {
                String value

                Module(){
                    configObject = new ModuleConfig()
                }


                @Override
                void configure(Closure<Void> configSpec) {
                    Configurable.super.configure(configSpec)
                    value = "${configObject.name}-${configObject.version}"
                }
            }


            class ModuleConfig {
                String name
                String version
            }
            def module = configure(Module) {
                name = 'test'
                version = '1.0'
            }
            assert module.value == 'test-1.0'
        '''
    }

    @Test // GROOVY-7287
    void testTraitWithMethodLevelGenericsShadowing2() {
        assertScript shell, '''
            trait SomeTrait {
                def <T extends Number> T someOtherMethod() {}
            }
            class SuperClass<T> implements SomeTrait {}
            class SubClass extends SuperClass<String> implements SomeTrait {}

            SubClass.declaredMethods.findAll { it.name == 'someOtherMethod' }
                                    .each {
                                        assert it.returnType == Number
                                        assert it.genericReturnType.name == 'T'
                                    }
        '''
    }

    trait T7297 {
        String title
        def <U> List<U> m(U data) {
            [data]
        }
    }

    @Test // GROOVY-7297
    void testMethodLevelGenericsFromPrecompiledClass() {
        assertScript shell, """
            class C implements ${T7297.name} {
            }
            def c = new C(title: 'some title')
            assert c.title == 'some title'
            assert c.m('x') == ['x']
        """
    }

    @Test // GROOVY-9763
    void testTraitWithStaticMethodGenericsSC() {
        assertScript shell, '''
            trait T {
                static <U> U m(Closure<U> callable) {
                    callable.call()
                }
            }
            class C implements T {
            }
            @CompileStatic
            def test() {
                C.m({ -> 'works' })
            }
            assert test() == 'works'
        '''
    }

    @Test // GROOVY-8281
    void testFinalFieldsDependency() {
        assertScript shell, '''
            trait MyTrait {
                private final String foo = 'foo'
                private final String foobar = foo.toUpperCase() + 'bar'
                int foobarSize() { foobar.size() }
            }

            class Baz implements MyTrait {}

            assert new Baz().foobarSize() == 6
        '''
    }

    @Test // GROOVY-8282
    void testBareNamedArgumentPrivateMethodCall() {
        assertScript shell, '''
            trait BugReproduction {
                def foo() {
                    bar(a: 1)
                }
                private String bar(Map args) {
                    args.collect{ k, v -> "$k$v" }.join()
                }
            }

            class Main implements BugReproduction {}

            assert new Main().foo() == 'a1'
        '''
    }

    @Test // GROOVY-8730
    void testAbstractMethodsNotNeededInHelperClass() {
        assertScript shell, '''
            import static groovy.test.GroovyAssert.shouldFail

            trait Foo { abstract bar() }

            // appears in interface as expected
            assert Foo.getMethod("bar", [] as Class[])

            // shouldn't appear in trait helper
            shouldFail(NoSuchMethodException) {
                // first and only inner class is the trait helper
                // fragile if current implementation changes drastically
                Foo.classes[0].getMethod("bar", [Foo] as Class[])
            }
        '''
    }

    @Test // GROOVY-8731
    void testStaticMethodsIgnoredWhenExistingInstanceMethodsFound() {
        assertScript shell, '''
            trait StaticFooBarBaz {
                static int foo() { 100 }
                static int baz() { 200 }
                static int bar() { 300 }
            }

            trait InstanceBar {
                int bar() { -10 }
            }

            class FooBarBaz implements StaticFooBarBaz, InstanceBar {
                int baz() { -20 }
            }

            assert FooBarBaz.foo() == 100
            new FooBarBaz().with {
                assert bar() == -10
                assert baz() == -20
            }
        '''
    }

    @Test // GROOVY-6716
    void testAnonymousInnerClassStyleTraitUsage() {
        assertScript shell, '''
            interface Foo { def foo() }
            def f = new Foo() { def foo() { 42 } }
            assert f.foo() == 42

            abstract class Baz { abstract baz() }
            def bz = new Baz() { def baz() { 42 } }
            assert bz.baz() == 42

            trait Bar { def bar() { 42 } }
            def b = new Bar() {}
            assert b.bar() == 42
        '''
    }

    @Test // GROOVY-8722
    void testFinalModifierSupport() {
        assertScript shell, '''
            import static java.lang.reflect.Modifier.isFinal

            trait Foo {
                final int bar() { 2 }
                final int baz() { 4 }
            }

            trait Foo2 {
                int baz() { 6 }
            }

            class FooFoo2 implements Foo, Foo2 { }

            class Foo2Foo implements Foo2, Foo {
                int bar() { 8 }
            }

            def isFinal(Class k, String methodName) {
                isFinal(k.getMethod(methodName, [] as Class[]).modifiers)
            }

            new Foo2Foo().with {
                assert bar() == 8
                assert baz() == 4
                assert !isFinal(Foo2Foo, 'bar')
                assert isFinal(Foo2Foo, 'baz')
            }

            new FooFoo2().with {
                assert bar() == 2
                assert baz() == 6
                assert isFinal(FooFoo2, 'bar')
                assert !isFinal(FooFoo2, 'baz')
            }
        '''

        assertScript shell, '''
            trait Startable {
                final int start() { doStart() * 2 }
                abstract int doStart()
            }

            abstract class Base implements Startable { }

            class Application extends Base {
                int doStart() { 21 }
            }

            assert new Application().start() == 42
        '''
    }

    @Test // GROOVY-8880
    void testTraitWithInitBlock() {
        assertScript shell, '''
            trait MyTrait {
                final String first = 'FOO'
                final String last = 'BAR'
                String full

                {
                    full = "$first$last"
                }
            }

            class MyClass implements MyTrait { }

            def mc = new MyClass()
            assert mc.full == 'FOOBAR'
        '''
    }

    @Test // GROOVY-8880
    void testTraitWithStaticInitBlock() {
        assertScript shell, '''
            trait MyTrait {
                static final String first = 'FOO'
                static final String last = 'BAR'
                static String full
                static {
                    full = "$first$last"
                }
            }

            class MyClass implements MyTrait { }

            assert MyClass.full == 'FOOBAR'
        '''
    }

    @Test // GROOVY-8892
    void testTraitWithStaticInitBlockWithAndWithoutProps() {
        assertScript shell, '''
            class Counter {
                static int count = 0
            }
            trait TraitNoProps {
                {
                    Counter.count += 1
                }
            }
            trait TraitWithProp {
                Integer instanceCounter //immutable, non-shareable
                {
                    Counter.count += 10
                    instanceCounter = 1
                }
            }
            class ClassWithTraits implements TraitNoProps, TraitWithProp { }
            assert new ClassWithTraits().instanceCounter == 1
            assert Counter.count == 11
        '''
    }

    @Test // GROOVY-8954
    void testTraitWithPropertyAlsoFromInterfaceSC() {
        assertScript shell, '''
            interface DomainProp {
                boolean isNullable()
            }

            abstract class OrderedProp implements DomainProp { }

            trait Nullable {
                boolean nullable = true
            }

            @CompileStatic
            abstract class CustomProp extends OrderedProp implements Nullable { }

            assert new CustomProp() {}
        '''
    }

    @Test // GROOVY-8272
    void testTraitAccessToInheritedStaticMethods() {
        assertScript shell, '''
            @CompileStatic
            trait Foo {
                static String go() {
                    'Go!'
                }
            }

            @CompileStatic
            trait Bar extends Foo {
                String doIt() {
                    go().toUpperCase()
                }
            }

            class Main implements Bar {}

            assert new Main().doIt() == 'GO!'
        '''
    }

    @Test // GROOVY-10312
    void testTraitAccessToInheritedStaticMethods2() {
        assertScript shell, '''
            trait Foo {
                static String staticMethod(String string) {
                    return string
                }
            }
            trait Bar extends Foo {
                static String staticMethodWithDefaultArgument(String string = 'works') {
                    staticMethod(string) // MissingMethodException
                }
            }

            class Main implements Bar {
                static test1() {
                    String result = staticMethodWithDefaultArgument()
                    assert result == 'works'
                }
                void test2() {
                    String result = staticMethodWithDefaultArgument()
                    assert result == 'works'
                }
            }

            Main.test1()
            new Main().test2()
        '''
    }

    @Test // GROOVY-10312
    void testTraitAccessToInheritedStaticMethods3() {
        assertScript shell, '''
            interface Foo {
                public static final String BANG = '!'
            }
            trait Bar implements Foo {
                static String staticMethodWithDefaultArgument(String string = 'works') {
                    string + BANG
                }
            }

            class Main implements Bar {
                static test1() {
                    String result = staticMethodWithDefaultArgument()
                    assert result == 'works!'
                }
                void test2() {
                    String result = staticMethodWithDefaultArgument()
                    assert result == 'works!'
                }
            }

            Main.test1()
            new Main().test2()
        '''
    }

    @Test // GROOVY-9386
    void testTraitPropertyInitializedByTap() {
        assertScript shell, '''
            class P {
                int prop
            }
            trait T {
                P pogo = new P().tap {
                    prop = 42 // MissingPropertyException: No such property: prop for class: C
                }
            }
            class C implements T {
            }

            def pogo = new C().pogo
            assert pogo.prop == 42
        '''
    }

    @Test // GROOVY-9386
    void testTraitPropertyInitializedByWith() {
        assertScript shell, '''
            class P {
                int prop
            }
            trait T {
                P pogo = new P().with {
                    prop = 42 // MissingPropertyException: No such property: prop for class: C
                    return it
                }
            }
            class C implements T {
            }
            def pogo = new C().pogo
            assert pogo.prop == 42
        '''
    }

    @Test // GROOVY-8000
    void testTraitMultiLevelGenerics() {
        assertScript shell, '''
            trait TopTrait<X> { X getSomeThing() {}
            }
            trait MiddleTrait<Y> implements TopTrait<Y> {
            }
            trait BottomTrait<Z> implements MiddleTrait<Z> {
            }
            class Implementation implements BottomTrait<String> {
            }

            assert new Implementation().getSomeThing() == null
        '''

        assertScript shell, '''
            trait TopTrait<T> { T getSomeThing() {}
            }
            trait MiddleTrait<T> implements TopTrait<T> {
            }
            trait BottomTrait<T> implements MiddleTrait<T> {
            }
            class Implementation implements BottomTrait<String> {
            }

            assert new Implementation().getSomeThing() == null
        '''
    }

    @Test // GROOVY-9660
    void testAsGenericsParam() {
        assertScript shell, '''
            trait Data {}
            class TestData implements Data {}
            class AbstractData<D extends Data>{ D data }
            new AbstractData<TestData>()
        '''
    }

    // GROOVY-10598
    void testAssignOperators() {
        assertScript shell, '''
            trait T {
            }
            class C implements T {
                @TypeChecked test() {
                    @ASTTest(phase=CANONICALIZATION, value={
                        // simulates GrailsASTUtils#addApiVariableDeclaration DeclarationExpression
                        node.operation.@type = org.codehaus.groovy.syntax.Types.ASSIGNMENT_OPERATOR
                    })
                    def var = null
                }
            }
            new C().test()
        '''
    }
}
