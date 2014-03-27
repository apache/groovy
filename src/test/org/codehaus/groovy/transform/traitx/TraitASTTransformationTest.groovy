/*
 * Copyright 2003-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.transform.traitx

class TraitASTTransformationTest extends GroovyTestCase {
    void testTraitWithNoMethod() {
        assertScript '''
        trait MyTrait {}

        class Foo implements MyTrait {}
        def foo = new Foo()
        assert foo instanceof MyTrait
        '''
    }

    void testTraitWithOneMethod() {
        assertScript '''
        trait MyTrait {
            int a() { 1 }

        }

        class Foo implements MyTrait {}
        def foo = new Foo()
        assert foo instanceof MyTrait
        assert foo.a() == 1
        '''

    }

    void testTraitWithTwoMethods() {
        assertScript '''
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

    void testTraitWithTwoMethodsAndOneOverride() {
        assertScript '''
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

    void testTraitWithTwoMethodsAndOneAbstract() {
        assertScript '''
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

    void testTraitWithTwoMethodsAndOneAbstractNotImplemented() {
        shouldFail {
            assertScript '''
            abstract trait MyTrait {
                abstract int a()
                int b() { a() }
            }

            class Foo implements MyTrait {
            }
            def foo = new Foo()
        '''
        }
    }

    void testWithPrecompiledTraitWithOneMethod() {
        assertScript '''import org.codehaus.groovy.transform.traitx.TraitASTTransformationTest.TestTrait as TestTrait

            class Foo implements TestTrait {}
            def foo = new Foo()
            assert foo.a() == 123
        '''
    }

    void testTraitWithConstructor() {
        shouldFail {
            assertScript '''
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
    }

    void testTraitWithField() {
        assertScript '''
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

    void testTraitWithField2() {
        assertScript '''import org.codehaus.groovy.transform.traitx.TestTrait2
        class Foo implements TestTrait2 {
            def cat() { "cat" }
        }
        def foo = new Foo()
        assert foo.message == 'Hello'
        assert foo.blah() == 'Hello'
        assert foo.meow() == /Meow! I'm a cat/
        '''
    }

    void testTraitWithSetValue() {
        assertScript '''
            import groovy.transform.Trait

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

    void testTraitWithProperty() {
        assertScript '''

            trait Named {
                String name
            }

            class Person implements Named {}

            def p = new Person(name:'Stromae')

            assert p.name == 'Stromae'

        '''
    }

    void testClosureExpressionInTrait() {
        assertScript '''import groovy.transform.*

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

    void testUpdatePropertyFromSelf() {
        assertScript '''
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

    void testPrivateFieldInTraitShouldBeRemapped() {
        assertScript '''import groovy.transform.ASTTest
import org.codehaus.groovy.control.CompilePhase

trait Foo {
    private int i = 0
    int sum(int x) { x+i }
    void setIndex(int index) { this.i = index }
}
@ASTTest(phase=CompilePhase.INSTRUCTION_SELECTION, value={
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

    void testStaticallyCompiledTrait() {
        assertScript '''
import groovy.transform.CompileStatic

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

    void testOverridePropertyDefinedInTrait() {
        assertScript '''
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


    void testOverridePropertyGetterDefinedInTrait() {
        assertScript '''
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

    void testSimpleTraitInheritance() {
        assertScript '''
trait Top { String methodFromA() { 'A' } }
trait Bottom extends Top { String methodFromB() { 'B' }}
class Foo implements Bottom {}
def f = new Foo()
assert f.methodFromA() == 'A'
assert f.methodFromB() == 'B'
'''
    }

    void testSimpleTraitInheritanceWithTraitOverridingMethodFromParent() {
        10.times {
            assertScript '''
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

    void testSimpleTraitInheritanceWithTraitOverridingMethodFromParentAndClass() {
        assertScript '''
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

    void testTraitOnEnum() {
        assertScript '''trait WithBar { int bar }

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

    void testClassImplementingTraitWithSameMethod() {
        10.times {
            assertScript '''
            trait A {
                int foo() { 1 }
            }
            trait B {
                int foo() { 2 }
            }
            class AB implements A,B {
            }
            def x = new AB()
            assert x.foo() == 1 // default order, A is first
            '''

            assertScript '''
            trait A {
                int foo() { 1 }
            }
            trait B {
                int foo() { 2 }
            }
            class AB implements B,A {
            }
            def x = new AB()
            assert x.foo() == 2 // default order, B is first
            '''

            assertScript '''
            trait A {
                int foo() { 1 }
            }
            trait B {
                int foo() { 2 }
            }
            class AB implements A,B {
                int foo() {
                    B.super.foo() // explicit take of B
                }
            }
            def x = new AB()
            assert x.foo() == 2
            '''

            assertScript '''
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
        }

        // make sure it is compatible with @CompileStatic
        assertScript '''
            trait A {
                int foo() { 1 }
            }
            trait B {
                int foo() { 2 }
            }
            @groovy.transform.CompileStatic
            class AB implements A,B {
                int foo() {
                    B.super.foo()
                }
            }
            def x = new AB()
            assert x.foo() == 2
        '''
    }

    void testTraitWithGenerics1() {
        assertScript '''
            trait Provider<T> {
                T get() {
                    null
                }
            }
            class StringProvider implements Provider<String> {}
            def c = new StringProvider()
            assert c.get() == null
        '''

        assertScript '''
            @groovy.transform.CompileStatic
            trait Provider<T> {
                T get() {
                    null
                }
            }
            @groovy.transform.CompileStatic
            class StringProvider implements Provider<String> {}
            def c = new StringProvider()
            assert c.get() == null
        '''
    }

    void testTraitWithGenerics2() {
        assertScript '''
            trait Provider<T> {
                T get(T ref) {
                    ref
                }
            }
            class StringProvider implements Provider<String> {}
            def c = new StringProvider()
            assert c.get('foo') == 'foo'
        '''

        assertScript '''
            @groovy.transform.CompileStatic
            trait Provider<T> {
                T get(T ref) {
                    ref
                }
            }
            @groovy.transform.CompileStatic
            class StringProvider implements Provider<String> {}
            def c = new StringProvider()
            assert c.get('foo') == 'foo\'
        '''
    }

    void testTraitWithGenericProperty() {
        assertScript '''
            trait PropertyProvider<T> {
                T foo
            }
            class StringProperty implements PropertyProvider<String> {}
            def c = new StringProperty()
            c.foo = 'foo'
            assert c.foo == 'foo'
        '''

        assertScript '''
            @groovy.transform.CompileStatic
            trait PropertyProvider<T> {
                T foo
            }
            @groovy.transform.CompileStatic
            class StringProperty implements PropertyProvider<String> {}
            def c = new StringProperty()
            c.foo = 'foo'
            assert c.foo == 'foo'
        '''
    }

    void testTraitWithComplexGenericProperty() {
        assertScript '''
            trait PropertyProvider<T> {
                List<T> foo
            }
            class StringProperty implements PropertyProvider<String> {}
            def c = new StringProperty()
            c.foo = ['foo']
            assert c.foo == ['foo']
        '''

        assertScript '''
            @groovy.transform.CompileStatic
            trait PropertyProvider<T> {
                List<T> foo
            }
            @groovy.transform.CompileStatic
            class StringProperty implements PropertyProvider<String> {}
            def c = new StringProperty()
            c.foo = ['foo']
            assert c.foo == ['foo']
        '''

        assertScript '''
            trait PropertyProvider<T> {
                List<T> foo
            }
            class StringProperty implements PropertyProvider<String> {}

            @groovy.transform.CompileStatic
            void test() {
                def c = new StringProperty()
                c.foo = ['foo']
                assert c.foo == ['foo']
            }
            test()
        '''
    }

    void testTraitWithGenericField() {
        assertScript '''
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

        assertScript '''
            @groovy.transform.CompileStatic
            trait PropertyProvider<T> {
                private T foo
                void set(T t) { foo = t}
                T get() { foo }
            }
            @groovy.transform.CompileStatic
            class StringProperty implements PropertyProvider<String> {}
            def c = new StringProperty()
            c.set('foo')
            assert c.get() == 'foo'
        '''
    }

    void testRuntimeTrait() {
        assertScript '''
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

    void testRuntimeDoubleTrait() {
        assertScript '''
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

    void testRuntimeWithTraitsDGM() {
        assertScript '''
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

   void testRuntimeWithTraitsDGMAndExplicitOverride() {
        assertScript '''
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
            d = d.withTraits(Flying, Speaking)
            assert d instanceof Flying
            assert d.fly() == "I'm flying!"
            assert d instanceof Speaking
            assert d.speak() == "I'm a special duck!"
        '''
    }

   void testRuntimeWithTraitsDGMAndExplicitOverrideAndCompileStatic() {
        assertScript '''
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

            @groovy.transform.CompileStatic
            void test() {
                def d = new Duck()
                d = d.withTraits(Flying, Speaking)
                assert d.fly() == "I'm flying!"
                assert d.speak() == "I'm a special duck!"
            }
            test()
        '''
    }

   void testRuntimeWithTraitsDGMAndExtraMethodCompileStatic() {
        assertScript '''
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

            @groovy.transform.CompileStatic
            void test() {
                def d = new Duck()
                d = d.withTraits(Flying, Speaking)
                assert d.fly() == "I'm flying!"
                assert d.speak() == "I'm a special duck!"
                assert d.quack() == "Quack!"
            }
            test()
        '''
    }

    void testRuntimeTraitWithMethodOfTheSameSignature() {
        assertScript '''
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
            assert d.fly() == 'Duck flying!'
            assert d.ability() == 'fly'

        '''
    }

    void testTraitWithDelegatesTo() {
        assertScript '''
            trait Route {
                void from(@DelegatesTo(To) Closure c) {
                    c.delegate = new To()

                }
            }
            class To {
               void test() { println 'Test' }
            }
            class Foo implements Route {}
            @groovy.transform.CompileStatic
            void exec() {
               def f = new Foo()
               f.from {
                  test()
               }
            }
            exec()
            '''
    }

    static trait TestTrait {
        int a() { 123 }
    }
}
