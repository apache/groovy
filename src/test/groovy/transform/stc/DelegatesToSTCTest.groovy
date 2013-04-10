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





package groovy.transform.stc

/**
 * Units tests aimed at testing the behaviour of {@link DelegatesTo} in combination
 * with static type checking.
 *
 * @author Cedric Champeau
 */
class DelegatesToSTCTest extends StaticTypeCheckingTestCase {
    void testShouldChooseMethodFromOwner() {
        assertScript '''
            class Delegate {
                int foo() { 2 }
            }
            class Owner {
                int foo() { 1 }
                int doIt(@DelegatesTo(Delegate) Closure cl) {
                    cl.delegate = new Delegate()
                    cl() as int
                }
                int test() {
                    doIt {
                        @ASTTest(phase=INSTRUCTION_SELECTION, value={
                            node = node.rightExpression
                            def target = node.getNodeMetaData(DIRECT_METHOD_CALL_TARGET)
                            assert target != null
                            assert target.declaringClass.name == 'Owner'
                        })
                        def x = foo() // as the delegation strategy is owner first, should return 1
                        x
                    }
                }
            }
            def o = new Owner()
            assert o.test() == 1
        '''
    }

    void testShouldChooseMethodFromDelegate() {
        assertScript '''
            class Delegate {
                int foo() { 2 }
            }
            class Owner {
                int foo() { 1 }
                int doIt(@DelegatesTo(strategy=Closure.DELEGATE_FIRST, value=Delegate) Closure cl) {
                    cl.delegate = new Delegate()
                    cl.resolveStrategy = Closure.DELEGATE_FIRST
                    cl() as int
                }
                int test() {
                    doIt {
                        @ASTTest(phase=INSTRUCTION_SELECTION, value={
                            node = node.rightExpression
                            def target = node.getNodeMetaData(DIRECT_METHOD_CALL_TARGET)
                            assert target != null
                            assert target.declaringClass.name == 'Delegate'
                        })
                        def x = foo() // as the delegation strategy is delegate first, should return 2
                        x
                    }
                }
            }
            def o = new Owner()
            assert o.test() == 2
        '''
    }
    void testShouldAcceptMethodCall() {
        assertScript '''
            class ExecSpec {
                boolean called = false
                void foo() {
                    called = true
                }
            }

            ExecSpec spec = new ExecSpec()

            void exec(ExecSpec spec, @DelegatesTo(value=ExecSpec, strategy=Closure.DELEGATE_FIRST) Closure param) {
                param.delegate = spec
                param()
            }

            exec(spec) {
                foo() // should be recognized because param is annotated with @DelegatesTo(ExecSpec)
            }
            assert spec.isCalled()
        '''
    }

    void testCallMethodFromOwner() {
        assertScript '''
            class Xml {
                boolean called = false
                void bar() { called = true }
                void foo(@DelegatesTo(Xml)Closure cl) { cl.delegate=this;cl() }
            }
            def mylist = [1]
            def xml = new Xml()
            xml.foo {
             mylist.each { bar() }
            }
            assert xml.called
        '''
    }

    void testEmbeddedWithAndShadowing() {
        assertScript '''
            class A {
                int foo() { 1 }
            }
            class B {
                int foo() { 2 }
            }
            def a = new A()
            def b = new B()
            int result
            a.with {
                b.with {
                    result = foo()
                }
            }
            assert result == 2
        '''
    }

    void testEmbeddedWithAndShadowing2() {
        assertScript '''
            class A {
                int foo() { 1 }
            }
            class B {
                int foo() { 2 }
            }
            def a = new A()
            def b = new B()
            int result
            b.with {
                a.with {
                    result = foo()
                }
            }
            assert result == 1
        '''
    }

    void testEmbeddedWithAndShadowing3() {
        assertScript '''
            class A {
                int foo() { 1 }
            }
            class B {
                int foo() { 2 }
            }
            class C {
                int foo() { 3 }
                void test() {
                    def a = new A()
                    def b = new B()
                    int result
                    b.with {
                        a.with {
                            result = foo()
                        }
                    }
                    assert result == 1
                }
            }
            new C().test()
        '''
    }

    void testEmbeddedWithAndShadowing4() {
        assertScript '''
            class A {
                int foo() { 1 }
            }
            class B {
                int foo() { 2 }
            }
            class C {
                int foo() { 3 }
                void test() {
                    def a = new A()
                    def b = new B()
                    int result
                    b.with {
                        a.with {
                            result = this.foo()
                        }
                    }
                    assert result == 3
                }
            }
            new C().test()
        '''
    }

    void testShouldDelegateToParameter() {
        assertScript '''
        class Foo {
            boolean called = false
            def foo() { called = true }
        }

        def with(@DelegatesTo.Target Object target, @DelegatesTo Closure arg) {
            arg.delegate = target
            arg()
        }

        def test() {
            def obj = new Foo()
            with(obj) { foo() }
            assert obj.called
        }
        test()
        '''
    }

    void testShouldDelegateToParameterUsingExplicitId() {
        assertScript '''
        class Foo {
            boolean called = false
            def foo() { called = true }
        }

        def with(@DelegatesTo.Target('target') Object target, @DelegatesTo(target='target') Closure arg) {
            arg.delegate = target
            arg()
        }

        def test() {
            def obj = new Foo()
            with(obj) { foo() }
            assert obj.called
        }
        test()
        '''
    }

    void testShouldFailDelegateToParameterUsingWrongId() {
        shouldFailWithMessages '''
        class Foo {
            boolean called = false
            def foo() { called = true }
        }

        def with(@DelegatesTo.Target('target') Object target, @DelegatesTo(target='wrongTarget') Closure arg) {
            arg.delegate = target
            arg()
        }

        def test() {
            def obj = new Foo()
            with(obj) { foo() }
            assert obj.called
        }
        test()
        ''', 'Not enough arguments found for a @DelegatesTo method call', 'Cannot find matching method'
    }

    void testShouldFailDelegateToParameterIfNoTargetSpecified() {
        shouldFailWithMessages '''
        class Foo {
            boolean called = false
            def foo() { called = true }
        }

        def with(Object target, @DelegatesTo Closure arg) {
            arg.delegate = target
            arg()
        }

        def test() {
            def obj = new Foo()
            with(obj) { foo() }
            assert obj.called
        }
        test()
        ''', 'Not enough arguments found for a @DelegatesTo method call', 'Cannot find matching method'
    }

    void testDelegatesToWithSetter() {
        assertScript '''
            class Item {
                int x
            }

            class Builder {
                private Item item = new Item()
                void setConstraints(@DelegatesTo(Item) Closure cl) {
                    def copy = cl.rehydrate(item, this, this)
                    copy()
                }
                int value() { item.x }
            }

            def b = new Builder()
            b.setConstraints {
                x = 5
            }
            assert b.value() == 5
        '''
    }

    void testDelegatesToWithSetterUsedAsProperty() {
        assertScript '''
            class Item {
                int x
            }

            class Builder {
                private Item item = new Item()
                void setConstraints(@DelegatesTo(Item) Closure cl) {
                    def copy = cl.rehydrate(item, this, this)
                    copy()
                }
                int value() { item.x }
            }

            def b = new Builder()
            b.constraints = {
                x = 5
            }
            assert b.value() == 5
        '''
    }

    void testDelegatesToWithSetterUsedAsPropertyAndErrorInPropertyName() {
        shouldFailWithMessages '''
            class Item {
                int x
            }

            class Builder {
                private Item item = new Item()
                void setConstraints(@DelegatesTo(Item) Closure cl) {
                    def copy = cl.rehydrate(item, this, this)
                    copy()
                }
                int value() { item.x }
            }

            def b = new Builder()
            b.constraints = {
                y = 5
            }
            assert b.value() == 5
        ''', '[Static type checking] - The variable [y] is undeclared.'
    }

    void testDelegatesToWithSetterUsedAsPropertyAndWith() {
        assertScript '''
            class Item {
                int x
            }

            class Builder {
                private Item item = new Item()
                void setConstraints(@DelegatesTo(Item) Closure cl) {
                    def copy = cl.rehydrate(item, this, this)
                    copy()
                }
                int value() { item.x }
            }

            def b = new Builder()
            b.with {
                constraints = {
                    x = 5
                }
            }
            assert b.value() == 5
        '''
    }

    // GROOVY-6055
    void testDelegatesToInStaticContext() {
        assertScript '''
            class Person {
                String name
                public static List<Person> findBy(@DelegatesTo(Person) Closure criteria) { [] }
            }
            List<Person> persons = Person.findBy { getName() == 'Cedric' }
            persons = Person.findBy { name == 'Cedric' }
        '''
    }
    void testDelegatesToInStaticContext2() {
        assertScript '''
            class QueryBuilder {
                List<?> where(Map params) {[]}
            }
            class Person {
                String name
                public static List<Person> findBy(@DelegatesTo(QueryBuilder) Closure criteria) { [] }
            }
            List<Person> persons = Person.findBy { where name:'Cédric' }
        '''
    }

    // GROOVY-6021
    void testShouldEnsureLastIsRecognizedAndCompiledProperly() {
        assertScript '''
            def with(@DelegatesTo.Target Object target, @DelegatesTo(strategy = Closure.DELEGATE_FIRST) Closure arg) {
                arg.delegate = target
                arg.setResolveStrategy(Closure.DELEGATE_FIRST)
                arg()
            }

            def test() {
                def obj = [1, 2]
                with(obj) {
                    print(last()) //error is here
                }
            }

            test()
        '''
    }

    // GROOVY-6091
    void testExplicitUseOfDelegateProperty() {
        assertScript '''
            def with(@DelegatesTo.Target Object target, @DelegatesTo(strategy = Closure.DELEGATE_FIRST) Closure arg) {
                arg.delegate = target
                arg.setResolveStrategy(Closure.DELEGATE_FIRST)
                arg()
            }

            def test() {
                def obj = [1, 2]
                with(obj) {
                    print(delegate.last()) //error is here
                }
            }

            test()

        '''
    }

    // GROOVY-6091
    void testExplicitUseOfDelegateMethod() {
        assertScript '''
            def with(@DelegatesTo.Target Object target, @DelegatesTo(strategy = Closure.DELEGATE_FIRST) Closure arg) {
                arg.delegate = target
                arg.setResolveStrategy(Closure.DELEGATE_FIRST)
                arg()
            }

            def test() {
                def obj = [1, 2]
                with(obj) {
                    print(getDelegate().last()) //error is here
                }
            }

            test()

        '''
    }
}
