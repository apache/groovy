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
package groovy.transform.stc

import org.codehaus.groovy.control.ParserVersion

/**
 * Units tests aimed at testing the behavior of {@link DelegatesTo} in combination
 * with static type checking.
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

    void testDelegatesToGenericTypeArgument() {
        assertScript '''
            public <T> Object map(@DelegatesTo.Target List<T> target, @DelegatesTo(genericTypeIndex=0) Closure arg) {
                arg.delegate = target.join('')
                arg()
            }
            def test() {
                def result
                map(['f','o','o']) {
                    result = toUpperCase()
                }

                result
            }
            assert 'FOO'==test()
        '''
    }

    void testDelegatesToGenericTypeArgumentAndActualArgumentNotUsingGenerics() {
        assertScript '''import groovy.transform.InheritConstructors
            @InheritConstructors
            class MyList extends LinkedList<String> {}

            public <T> Object map(@DelegatesTo.Target List<T> target, @DelegatesTo(genericTypeIndex=0) Closure arg) {
                arg.delegate = target.join('')
                arg()
            }
            def test() {
                def result
                def mylist = new MyList(['f','o','o'])
                map(mylist) {
                    result = toUpperCase()
                }

                result
            }
            assert 'FOO'==test()
        '''
    }

    void testDelegatesToGenericTypeArgumentWithMissingGenerics() {
        shouldFailWithMessages '''
            public Object map(@DelegatesTo.Target List target, @DelegatesTo(genericTypeIndex=0) Closure arg) {
                arg.delegate = target.join('')
                arg()
            }
            def test() {
                def result
                map(['f','o','o']) {
                    result = toUpperCase()
                }

                result
            }
            assert 'FOO'==test()
        ''', 'Cannot use @DelegatesTo(genericTypeIndex=0) with a type that doesn\'t use generics', 'Cannot find matching method'
    }

    void testDelegatesToGenericTypeArgumentOutOfBounds() {
        shouldFailWithMessages '''
            public <T> Object map(@DelegatesTo.Target List<T> target, @DelegatesTo(genericTypeIndex=1) Closure arg) {
                arg.delegate = target.join('')
                arg()
            }
            def test() {
                def result
                map(['f','o','o']) {
                    result = toUpperCase()
                }

                result
            }
            assert 'FOO'==test()
        ''', 'Index of generic type @DelegatesTo(genericTypeIndex=1) greater than those of the selected type', 'Cannot find matching method'
    }

    void testDelegatesToGenericTypeArgumentWithNegativeIndex() {
        shouldFailWithMessages '''
            public <T> Object map(@DelegatesTo.Target List<T> target, @DelegatesTo(genericTypeIndex=-1) Closure arg) {
                arg.delegate = target.join('')
                arg()
            }
            def test() {
                def result
                map(['f','o','o']) {
                    result = toUpperCase()
                }

                result
            }
            assert 'FOO'==test()
        ''', 'Index of generic type @DelegatesTo(genericTypeIndex=-1) lower than those of the selected type', 'Cannot find matching method'
    }

    void testDelegatesToGenericTypeArgumentUsingMap() {
        assertScript '''
            public <K,V> void transform(@DelegatesTo.Target Map<K, V> map, @DelegatesTo(genericTypeIndex = 1) Closure<?> closure) {
                map.keySet().each {
                    closure.delegate = map[it]
                    map[it] = closure()
                }
            }
            def map = [1: 'a', 2: 'b', 3: 'c']
            transform(map) {
                toUpperCase()
            }
            assert map == [1: 'A', 2: 'B', 3: 'C']
        '''
    }

    void testDelegatesToGenericTypeArgumentUsingMapAndWrongIndex() {
        shouldFailWithMessages '''
            public <K,V> void transform(@DelegatesTo.Target Map<K, V> map, @DelegatesTo(genericTypeIndex = 0) Closure<?> closure) {
                map.keySet().each {
                    closure.delegate = map[it]
                    map[it] = closure()
                }
            }
            def map = [1: 'a', 2: 'b', 3: 'c']
            transform(map) {
                toUpperCase()
            }
            assert map == [1: 'A', 2: 'B', 3: 'C']
        ''', 'Cannot find matching method'
    }

    // GROOVY-6165
    void testDelegatesToGenericArgumentTypeAndTypo() {

        String code = '''import groovy.transform.*

        @TupleConstructor
        class Person { String name }

        public <T> List<T> names(
            @DelegatesTo.Target List<T> list,
            @DelegatesTo(genericTypeIndex = 0) Closure modify) {
                list.collect {
                    modify.delegate = it
                    modify()
                }
        }

        def test(List<Person> persons) {
            def names = names(persons) {
                getname().toUpperCase()
            }
            assert names == ['GUILLAUME', 'CEDRIC']
        }

        test([new Person('Guillaume'), new Person('Cedric')])
        '''

        String msg = 'Cannot find matching method'

        if (ParserVersion.V_2 == config.parserVersion) {
            shouldFailWithMessages code, msg
        } else {
            /*
             * Because the Parrot parser provides more accurate node position information,
             * org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor.addError will not be interfered by wrong node position.
             *
             * 1) TestScripttestDelegatesToGenericArgumentTypeAndTypo0.groovy: 17: [Static type checking] - Cannot find matching method TestScripttestDelegatesToGenericArgumentTypeAndTypo0#getname(). Please check if the declared type is correct and if the method exists.
             * 2) TestScripttestDelegatesToGenericArgumentTypeAndTypo0.groovy: 17: [Static type checking] - Cannot find matching method java.lang.Object#toUpperCase(). Please check if the declared type is correct and if the method exists.
             *
             */
            shouldFailWithMessages code, msg, msg
        }

    }

    // GROOVY-6323, GROOVY-6325, GROOVY-6332
    void testStaticContextAndProperty() {
        assertScript '''
            class MyCar {
                String brand
                String model
            }

            class MyCarMain {
                MyCar configureCar(@DelegatesTo(MyCar) Closure closure) {
                    def car = new MyCar()
                    closure.delegate = car
                    closure.resolveStrategy = Closure.DELEGATE_FIRST
                    closure()
                    car
                }
                static void main(String[] args) {
                    def main = new MyCarMain()
                    def car = main.configureCar {
                        brand = "BMW"
                        model = brand + " X5"
                    }
                    assert car.model == "BMW X5"
                }
            }
            MyCarMain.main()
        '''

        assertScript '''
            class MyCar {
                private String _brand
                private String _model

                String getBrand() {
                    return _brand
                }

                void setBrand(String brand) {
                    _brand = brand
                }

                String getModel() {
                    return _model
                }

                void setModel(String model) {
                    _model = model
                }
            }

            class MyCarMain {
                MyCar configureCar(@DelegatesTo(value = MyCar, strategy = Closure.DELEGATE_FIRST) Closure closure) {
                    def car = new MyCar()
                    closure.delegate = car
                    closure.resolveStrategy = Closure.DELEGATE_FIRST
                    closure()
                    car
                }

                static void main(String[] args) {
                    def main = new MyCarMain()
                    def car = main.configureCar {
                        brand = "BMW"
                        model = brand
                    }
                    assert car.model == "BMW"
                }
            }
            MyCarMain.main()
        '''

        assertScript '''
            class Car {
              private String _brand
              String getBrand() { _brand }
              void setBrand(String brand) { _brand = brand }
            }

            class Builder {
              def <T> T configure(@DelegatesTo.Target Class<T> target, @DelegatesTo(genericTypeIndex=0) Closure cl) {
                def obj = target.newInstance() 
                cl.delegate = obj
                cl.resolveStrategy = Closure.DELEGATE_FIRST
                cl.call()
                obj 
              }
            }

            class Main {
              void run() {
                def builder = new Builder()
                def car = builder.configure(Car) {
                  brand = brand 
                }
              }
            }

            new Main().run()
        '''
    }

    // GROOVY-5998
    void testSubscriptOperatorOnPropertiesWithBuilder() {
        assertScript '''
            import static groovy.lang.Closure.*

            class DatasourceBuilder {
                Map<String,String> attrs = [:]
            }

            void datasource(@DelegatesTo(strategy = DELEGATE_FIRST, value = DatasourceBuilder) Closure c) {}

            void foo() {
               datasource {
                   attrs['some'] = 'foo'
               }
            }

            foo()
        '''
    }

    void testDelegatesToNestedGenericType() {
        assertScript '''
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

    void testDelegatesToWithType2() {
        assertScript '''
            public <T> boolean evalAsSet(List<T> list, @DelegatesTo(type="Set<T>") Closure<Boolean> cl) {
                cl.delegate = list as Set
                cl()
            }
            assert evalAsSet([1,1,2,3]) {
                size() == 3
            }
        '''
    }
}

