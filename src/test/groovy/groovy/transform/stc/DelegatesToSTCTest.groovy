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

import org.codehaus.groovy.control.customizers.ImportCustomizer

/**
 * Units tests aimed at testing the behavior of {@link DelegatesTo} in combination
 * with static type checking.
 */
class DelegatesToSTCTest extends StaticTypeCheckingTestCase {

    @Override
    void configure() {
        config.addCompilationCustomizers(
            new ImportCustomizer().addStaticStars('groovy.lang.Closure')
        )
    }

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
                int doIt(@DelegatesTo(value=Delegate, strategy=DELEGATE_FIRST) Closure cl) {
                    cl.delegate = new Delegate()
                    cl.resolveStrategy = DELEGATE_FIRST
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

            void exec(ExecSpec spec, @DelegatesTo(value=ExecSpec, strategy=DELEGATE_FIRST) Closure cl) {
                cl.delegate = spec
                cl()
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
                void foo(@DelegatesTo(Xml) Closure cl) { cl.delegate=this;cl() }
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

            def with(@DelegatesTo.Target Object target, @DelegatesTo Closure cl) {
                cl.delegate = target
                cl()
            }

            def obj = new Foo()
            with(obj) { foo() }
            assert obj.called
        '''
    }

    void testShouldDelegateToParameterUsingExplicitId() {
        assertScript '''
            class Foo {
                boolean called = false
                def foo() { called = true }
            }

            def with(@DelegatesTo.Target('target') Object target, @DelegatesTo(target='target') Closure cl) {
                cl.delegate = target
                cl()
            }

            def obj = new Foo()
            with(obj) { foo() }
            assert obj.called
        '''
    }

    void testShouldFailDelegateToParameterUsingWrongId() {
        shouldFailWithMessages '''
            class Foo {
                boolean called = false
                def foo() { called = true }
            }

            def with(@DelegatesTo.Target('target') Object target, @DelegatesTo(target='wrongTarget') Closure cl) {
                cl.delegate = target
                cl()
            }

            def obj = new Foo()
            with(obj) { foo() }
        ''',
        'Not enough arguments found for a @DelegatesTo method call',
        'Cannot find matching method'
    }

    void testShouldFailDelegateToParameterIfNoTargetSpecified() {
        shouldFailWithMessages '''\
            class Foo {
                boolean called = false
                def bar() { called = true }
            }

            def m(Object o, @DelegatesTo Closure cl) {
                cl.delegate = o
                cl()
            }

            def foo = new Foo()
            m(foo) { -> bar() }
        ''',
        'Not enough arguments found for a @DelegatesTo method call', '@ line 6, column 29',
        'Cannot find matching method'
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
        ''',
        '[Static type checking] - The variable [y] is undeclared.'
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

    // GROOVY-6022
    void testDelegatesToVariadicParameter() {
        assertScript '''
            def m(@DelegatesTo.Target target, @DelegatesTo(strategy=DELEGATE_FIRST) Closure... closures) {
                for (Closure closure : closures) {
                    closure.resolveStrategy = DELEGATE_FIRST
                    closure.delegate = target
                    closure()
                }
            }

            m([x:0], { get('x') }, { put('y',1) })
        '''

        assertScript '''
            class Item {
                int x
            }

            void m(@DelegatesTo.Target Item item, @DelegatesTo(strategy=DELEGATE_FIRST) Closure... closures) {
                for (closure in closures) {
                    closure.resolveStrategy = DELEGATE_FIRST
                    closure.delegate = item
                    closure.call()
                }
            }

            m(new Item(), { x })
        '''

        assertScript '''
            class Item {
                int x
            }

            void m(@DelegatesTo.Target Item item, @DelegatesTo(strategy=DELEGATE_FIRST) Closure... closures) {
                for (@DelegatesTo(strategy=DELEGATE_FIRST) Closure closure : closures) {
                    item.with(closure)
                }
            }

            m(new Item(), { x })
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
            def with(@DelegatesTo.Target Object target, @DelegatesTo(strategy = DELEGATE_FIRST) Closure cl) {
                cl.resolveStrategy = DELEGATE_FIRST
                cl.delegate = target
                cl()
            }

            def obj = [1, 2]
            with(obj) {
                print(last()) //error is here
            }
        '''
    }

    // GROOVY-6091
    void testExplicitUseOfDelegateProperty() {
        assertScript '''
            def with(@DelegatesTo.Target Object target, @DelegatesTo(strategy = DELEGATE_FIRST) Closure cl) {
                cl.resolveStrategy = DELEGATE_FIRST
                cl.delegate = target
                cl.call()
            }

            def list = [1, 2]
            assert 2 == with(list) {
                delegate.last() // error is here
            }
        '''
    }

    // GROOVY-6091
    void testExplicitUseOfDelegateMethod() {
        assertScript '''
            def with(@DelegatesTo.Target Object target, @DelegatesTo(strategy=DELEGATE_FIRST) Closure cl) {
                cl.resolveStrategy = DELEGATE_FIRST
                cl.delegate = target
                cl.call()
            }

            def list = [1, 2]
            assert 2 == with(list) { ->
                getDelegate().last() // error is here
            }
        '''
    }

    void testDelegatesToGenericTypeArgument() {
        assertScript '''
            public <T> Object map(@DelegatesTo.Target List<T> target, @DelegatesTo(genericTypeIndex=0) Closure cl) {
                cl.delegate = target.join('')
                cl()
            }

            def result
            map(['f','o','o']) {
                result = toUpperCase()
            }
            assert result == 'FOO'
        '''
    }

    void testDelegatesToGenericTypeArgumentAndActualArgumentNotUsingGenerics() {
        assertScript '''
            @groovy.transform.InheritConstructors
            class MyList extends LinkedList<String> {}

            public <T> Object map(@DelegatesTo.Target List<T> target, @DelegatesTo(genericTypeIndex=0) Closure cl) {
                cl.delegate = target.join('')
                cl()
            }

            def result
            def mylist = new MyList(['f','o','o'])
            map(mylist) {
                result = toUpperCase()
            }
            assert result == 'FOO'
        '''
    }

    void testDelegatesToGenericTypeArgumentWithMissingGenerics() {
        shouldFailWithMessages '''
            public Object map(@DelegatesTo.Target List target, @DelegatesTo(genericTypeIndex=0) Closure cl) {
                cl.delegate = target.join('')
                cl()
            }

            map(['f','o','o']) {
                toUpperCase()
            }
        ''',
        'Cannot use @DelegatesTo(genericTypeIndex=0) with a type that doesn\'t use generics', 'Cannot find matching method'
    }

    void testDelegatesToGenericTypeArgumentOutOfBounds() {
        shouldFailWithMessages '''
            def <T> Object map(@DelegatesTo.Target List<T> target, @DelegatesTo(genericTypeIndex=1) Closure cl) {
                cl.delegate = target.join('')
                cl()
            }

            map(['f','o','o']) {
                toUpperCase()
            }
        ''',
        'Index of generic type @DelegatesTo(genericTypeIndex=1) greater than those of the selected type', 'Cannot find matching method'
    }

    void testDelegatesToGenericTypeArgumentWithNegativeIndex() {
        shouldFailWithMessages '''
            public <T> Object map(@DelegatesTo.Target List<T> target, @DelegatesTo(genericTypeIndex=-1) Closure cl) {
                cl.delegate = target.join('')
                cl()
            }

            map(['f','o','o']) {
                toUpperCase()
            }
        ''',
        'Index of generic type @DelegatesTo(genericTypeIndex=-1) lower than those of the selected type', 'Cannot find matching method'
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
            public <K,V> void transform(@DelegatesTo.Target Map<K, V> map, @DelegatesTo(genericTypeIndex = 0) Closure<?> cl) {
                map.keySet().each {
                    cl.delegate = map[it]
                    map[it] = cl()
                }
            }

            def map = [1: 'a', 2: 'b', 3: 'c']
            transform(map) {
                toUpperCase()
            }
        ''',
        'Cannot find matching method'
    }

    // GROOVY-6165
    void testDelegatesToGenericArgumentTypeAndTypo() {
        /*
         * Because the Parrot parser provides more accurate node position information,
         * org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor.addError will not be interfered by wrong node position.
         *
         * 1) TestScripttestDelegatesToGenericArgumentTypeAndTypo0.groovy: 17: [Static type checking] - Cannot find matching method TestScripttestDelegatesToGenericArgumentTypeAndTypo0#getname(). Please check if the declared type is correct and if the method exists.
         * 2) TestScripttestDelegatesToGenericArgumentTypeAndTypo0.groovy: 17: [Static type checking] - Cannot find matching method java.lang.Object#toUpperCase(). Please check if the declared type is correct and if the method exists.
         */
        shouldFailWithMessages '''
            @groovy.transform.TupleConstructor
            class Person { String name }

            def <T> List<T> names(
                @DelegatesTo.Target List<T> list,
                @DelegatesTo(genericTypeIndex = 0) Closure cl) {
                    list.collect {
                        cl.delegate = it
                        cl()
                    }
            }

            def test(List<Person> persons) {
                def names = names(persons) {
                    getname().toUpperCase()
                }
                assert names == ['GUILLAUME', 'CEDRIC']
            }

            test([new Person('Guillaume'), new Person('Cedric')])
        ''',
        'Cannot find matching method',
        'Cannot find matching method'
    }

    // GROOVY-6323, GROOVY-6325, GROOVY-6332
    void testStaticContextAndProperty() {
        assertScript '''
            class MyCar {
                String brand
                String model
            }

            MyCar configureCar(@DelegatesTo(value=MyCar, strategy=DELEGATE_FIRST) Closure cl) {
                new MyCar().tap(cl)
            }

            def car = configureCar {
                brand = 'BMW'
                model = "$brand X5"
            }
            assert car.model == 'BMW X5'
        '''

        assertScript '''
            class MyCar {
                private String _brand
                private String _model
                String getBrand() {
                    return _brand
                }
                String getModel() {
                    return _model
                }
                void setBrand(String brand) {
                    _brand = brand
                }
                void setModel(String model) {
                    _model = model
                }
            }

            MyCar configureCar(@DelegatesTo(value=MyCar, strategy=DELEGATE_FIRST) Closure cl) {
                new MyCar().tap(cl)
            }

            def car = configureCar {
                brand = 'BMW'
                model = brand
            }
            assert car.model == 'BMW'
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
                    cl.resolveStrategy = DELEGATE_FIRST
                    cl.call()
                    obj
                }
            }

            def builder = new Builder()
            def car = builder.configure(Car) {
                brand = brand
            }
        '''
    }

    // GROOVY-5998
    void testSubscriptOperatorOnPropertiesWithBuilder() {
        assertScript '''
            class DatasourceBuilder {
                Map<String,String> attrs = [:]
            }

            void datasource(@DelegatesTo(value=DatasourceBuilder, strategy=DELEGATE_FIRST) Closure cl) {
            }

            datasource {
                attrs['some'] = 'foo'
            }
        '''
    }

    void testDelegatesToWithType1() {
        assertScript '''
            trait Configurable<Type> {
                Type configObject
                void configure(@DelegatesTo(type='Type') Closure<Void> spec) {
                    spec.resolveStrategy = DELEGATE_FIRST
                    spec.delegate = configObject
                    spec.call()
                }
            }
            class ModuleConfig { String name, version }
            class Module implements Configurable<ModuleConfig> {
                String value
                Module() {
                    configObject = new ModuleConfig()
                }
                @Override
                void configure(Closure<Void> configSpec) {
                    Configurable.super.configure(configSpec)
                    value = "${configObject.name}-${configObject.version}"
                }
            }

            def <T,U extends Configurable<T>> U configure(Class<U> clazz, @DelegatesTo(type='T') Closure spec) {
                Configurable<T> obj = (Configurable<T>) clazz.newInstance()
                obj.configure(spec)
                obj
            }

            def module = configure(Module) {
                name = 'test'
                version = '1.0'
            }
            assert module.value == 'test-1.0'
        '''
    }

    // GROOVY-11168
    void testDelegatesToWithType2() {
        assertScript '''
            def <T> T m(@DelegatesTo(type='T', strategy=DELEGATE_FIRST) Closure cl) {
                'WORKS'.with(cl)
            }

            String result
            this.<String>m {
                result = toLowerCase()
            }
            assert result == 'works'
        '''
    }

    void testDelegatesToWithType3() {
        assertScript '''
            def <T> boolean evalAsSet(List<T> list, @DelegatesTo(type='Set<T>') Closure<Boolean> cl) {
                cl.delegate = list.toSet()
                cl()
            }

            assert evalAsSet([1,1,2,3]) {
                size() == 3
            }
        '''
    }

    // GROOVY-7996
    void testErrorForMismatchedClosureResolveStrategy() {
        shouldFailWithMessages '''
            class Foo {
                def build(Closure cl) { // resolve strategy OWNER_FIRST
                    this.with(cl) // resolve strategy DELEGATE_FIRST
                }
                def propertyMissing(String name) {
                    'something'
                }
            }

            class Bar {
                protected List bars = []
                def baz() {
                    new Foo().build {
                        bars.isEmpty() // fails if executed; delegate's propertyMissing takes precedence
                    }
                }
            }

            new Bar().baz()
        ''',
        'Closure parameter with resolve strategy OWNER_FIRST passed to method with resolve strategy DELEGATE_FIRST'
    }
}
