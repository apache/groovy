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

import groovy.test.NotYetImplemented

/**
 * Unit tests for static type checking : fields and properties.
 */
class FieldsAndPropertiesSTCTest extends StaticTypeCheckingTestCase {

    void testAssignFieldValue() {
        assertScript """
            class A {
                int x
            }

            A a = new A()
            a.x = 1
        """
    }

    void testAssignFieldValueWithWrongType() {
        shouldFailWithMessages '''
            class A {
                int x
            }

            A a = new A()
            a.x = '1'
        ''', 'Cannot assign value of type java.lang.String to variable of type int'
    }

    void testMapDotPropertySyntax() {
        assertScript '''
            HashMap map = [:]
            map['a'] = 1
            map.b = 2
            assert map.get('a') == 1
            assert map.get('b') == 2
        '''
    }

    void testInferenceFromFieldType() {
        assertScript '''
            class A {
                String name = 'Cedric'
            }
            A a = new A()
            def b = a.name
            b.toUpperCase() // type of b should be inferred from field type
        '''
    }

    void testAssignFieldValueWithAttributeNotation() {
        assertScript """
            class A {
                int x
            }

            A a = new A()
            a.@x = 1
        """
    }

    void testAssignFieldValueWithWrongTypeAndAttributeNotation() {
         shouldFailWithMessages '''
             class A {
                 int x
             }

             A a = new A()
             a.@x = '1'
         ''', 'Cannot assign value of type java.lang.String to variable of type int'
     }

    void testInferenceFromAttributeType() {
        assertScript '''
            class A {
                String name = 'Cedric'
            }
            A a = new A()
            def b = a.@name
            b.toUpperCase() // type of b should be inferred from field type
        '''
    }

    void testShouldComplainAboutMissingProperty() {
        shouldFailWithMessages '''
            Object o = new Object()
            o.x = 0
        ''', 'No such property: x for class: java.lang.Object'
    }

    void testShouldComplainAboutMissingProperty2() {
        shouldFailWithMessages '''
            class A {
            }
            A a = new A()
            a.x = 0
        ''', 'No such property: x for class: A'
    }

    @NotYetImplemented
    void testShouldComplainAboutMissingProperty3() {
        shouldFailWithMessages '''
            class A {
                private x
            }
            class B extends A {
                void test() {
                    this.x
                }
            }
        ''', 'The field A.x is not accessible'
    }

    void testShouldComplainAboutMissingAttribute() {
        shouldFailWithMessages '''
            Object o = new Object()
            o.@x = 0
        ''', 'No such attribute: x for class: java.lang.Object'
    }

    void testShouldComplainAboutMissingAttribute2() {
        shouldFailWithMessages '''
            class A {
            }
            A a = new A()
            a.@x = 0
        ''', 'No such attribute: x for class: A'
    }

    void testShouldComplainAboutMissingAttribute3() {
        shouldFailWithMessages '''
            class A {
                def getX() { }
            }
            A a = new A()
            println a.@x
        ''', 'No such attribute: x for class: A'
    }

    void testShouldComplainAboutMissingAttribute4() {
        shouldFailWithMessages '''
            class A {
                def setX(x) { }
            }
            A a = new A()
            a.@x = 0
        ''', 'No such attribute: x for class: A'
    }

    void testShouldComplainAboutMissingAttribute5() {
        shouldFailWithMessages '''
            class A {
                private x
            }
            class B extends A {
                void test() {
                    this.@x
                }
            }
        ''', 'The field A.x is not accessible'
    }

    void testPropertyWithInheritance() {
        assertScript '''
            class A {
                int x
            }
            class B extends A {
            }

            B b = new B()
            assert b.x == 0

            b.x = 2
            assert b.x == 2
        '''
    }

    void testPropertyTypeWithInheritance() {
        shouldFailWithMessages '''
            class A {
                int x
            }
            class B extends A {
            }
            B b = new B()
            b.x = '2'
        ''', 'Cannot assign value of type java.lang.String to variable of type int'
    }

    void testPropertyWithInheritanceFromAnotherSourceUnit() {
        assertScript '''
            class B extends groovy.transform.stc.FieldsAndPropertiesSTCTest.BaseClass {
            }
            B b = new B()
            b.x = 2
        '''
    }

    void testPropertyWithInheritanceFromAnotherSourceUnit2() {
        shouldFailWithMessages '''
            class B extends groovy.transform.stc.FieldsAndPropertiesSTCTest.BaseClass {
            }
            B b = new B()
            b.x = '2'
        ''', 'Cannot assign value of type java.lang.String to variable of type int'
    }

    void testPropertyWithSuperInheritanceFromAnotherSourceUnit() {
        assertScript '''
            class B extends groovy.transform.stc.FieldsAndPropertiesSTCTest.BaseClass2 {
            }
            B b = new B()
            b.x = 2
        '''
    }

    void testMethodUsageForProperty() {
        assertScript '''
            class Foo {
                String name
            }
            def name = new Foo().getName()
            name?.toUpperCase()
        '''
    }

    void testDateProperties() {
        assertScript '''
            Date d = new Date()
            def time = d.time
            d.time = 0
        '''
    }

    // GROOVY-5232
    void testSetterForProperty() {
        assertScript '''
            class Person {
                String name

                static Person create() {
                    def p = new Person()
                    p.setName("Guillaume")
                    // but p.name = "Guillaume" works
                    return p
                }
            }

            Person.create()
        '''
    }

    // GROOVY-5443
    void testFieldInitShouldPass() {
        assertScript '''
            class Foo {
                int x = 1
            }
            1
        '''
    }

    // GROOVY-5443
    void testFieldInitShouldNotPassBecauseOfIncompatibleTypes() {
        shouldFailWithMessages '''
            class Foo {
                int x = new Date()
            }
            1
        ''', 'Cannot assign value of type java.util.Date to variable of type int'
    }

    // GROOVY-5443
    void testFieldInitShouldNotPassBecauseOfIncompatibleTypesWithClosure() {
        shouldFailWithMessages '''
            class Foo {
                Closure<List> cls = { Date aDate ->  aDate.getTime() }
            }
            1
        ''', 'Incompatible generic argument types. Cannot assign groovy.lang.Closure <java.lang.Long> to: groovy.lang.Closure <List>'
    }

    // GROOVY-5517
    void testShouldFindStaticPropertyEvenIfObjectImplementsMap() {
        assertScript '''
            class MyHashMap extends HashMap {
                public static int version = 666
            }
            def map = new MyHashMap()
            map['foo'] = 123
            Object value = map.foo
            assert value == 123
            value = map['foo']
            assert value == 123
            int v = MyHashMap.version
            assert v == 666
        '''
    }

    void testListDotProperty() {
        assertScript '''class Elem { int value }
            List<Elem> list = new LinkedList<Elem>()
            list.add(new Elem(value:123))
            list.add(new Elem(value:456))
            assert list.value == [ 123, 456 ]
            list.add(new Elem(value:789))
            assert list.value == [ 123, 456, 789 ]
        '''

        assertScript '''class Elem { String value }
            List<Elem> list = new LinkedList<Elem>()
            list.add(new Elem(value:'123'))
            list.add(new Elem(value:'456'))
            assert list.value == [ '123', '456' ]
            list.add(new Elem(value:'789'))
            assert list.value == [ '123', '456', '789' ]
        '''
    }

    void testClassPropertyOnInterface() {
        assertScript '''
            Class test(Serializable arg) {
                Class<?> clazz = arg.class
                clazz
            }
            assert test('foo') == String
        '''
        assertScript '''
            Class test(Serializable arg) {
                Class<?> clazz = arg.getClass()
                clazz
            }
            assert test('foo') == String
        '''
    }

    void testSetterUsingPropertyNotation() {
        assertScript '''
            class A {
                boolean ok = false;
                void setFoo(String foo) { ok = foo == 'foo' }
            }
            def a = new A()
            a.foo = 'foo'
            assert a.ok
        '''
    }

    void testSetterUsingPropertyNotationOnInterface() {
        assertScript '''
                interface FooAware { void setFoo(String arg) }
                class A implements FooAware {
                    void setFoo(String foo) { }
                }
                void test(FooAware a) {
                    a.foo = 'foo'
                }
                def a = new A()
                test(a)
            '''
    }

    // GROOVY-5700
    void testInferenceOfMapDotProperty() {
        assertScript '''
            def m = [retries: 10]
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == Integer_TYPE
            })
            def r1 = m['retries']

            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == Integer_TYPE
            })
            def r2 = m.retries
        '''
    }

    void testInferenceOfListDotProperty() {
        assertScript '''class Foo { int x }
            def list = [new Foo(x:1), new Foo(x:2)]
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def iType = node.getNodeMetaData(INFERRED_TYPE)
                assert iType == make(List)
                assert iType.isUsingGenerics()
                assert iType.genericsTypes[0].type == Integer_TYPE
            })
            def r2 = list.x
            assert r2 == [ 1,2 ]
        '''
    }

    void testTypeCheckerDoesNotThinkPropertyIsReadOnly() {
        assertScript '''
            // a base class defining a read-only property
            class Top {
                private String foo = 'foo'
                String getFoo() { foo }
                String getFooFromTop() { foo }
            }

            // a subclass defining it's own field
            class Bottom extends Top {
                private String foo

                Bottom(String msg) {
                    this.foo = msg
                }

                public String getFoo() { this.foo }
            }

            def b = new Bottom('bar')
            assert b.foo == 'bar'
            assert b.fooFromTop == 'foo'
        '''
    }

    // GROOVY-5779
    void testShouldNotUseNonStaticProperty() {
        assertScript '''import java.awt.Color
        Color c = Color.red // should not be interpreted as Color.getRed()
        '''
    }

    // GROOVY-5725
    void testAccessFieldDefinedInInterface() {
        assertScript '''
            class Foo implements groovy.transform.stc.FieldsAndPropertiesSTCTest.InterfaceWithField {
                void test() {
                    assert boo == "I don't fancy fields in interfaces"
                }
            }
            new Foo().test()
        '''
    }

    void testOuterPropertyAccess1() {
        assertScript '''
            class Outer {
                class Inner {
                    def m() {
                        p
                    }
                }
                def p = 1
            }
            def i = new Outer.Inner(new Outer())
            def x = i.m()
            assert x == 1
        '''
    }

    // GROOVY-9598
    void testOuterPropertyAccess2() {
        shouldFailWithMessages '''
            class Outer {
                static class Inner {
                    def m() {
                        p
                    }
                }
                def p = 1
            }
            def i = new Outer.Inner()
            def x = i.m()
        ''', "Apparent variable 'p' was found in a static scope but doesn't refer to a local variable, static field or class."
    }

    void testOuterPropertyAccess3() {
        shouldFailWithMessages '''
            class Outer {
                static class Inner {
                    def m() {
                        this.p
                    }
                }
                def p = 1
            }
            def i = new Outer.Inner()
            def x = i.m()
        ''', 'No such property: p for class: Outer$Inner'
    }

    void testPrivateFieldAccessInClosure() {
        assertScript '''
            class A {
                private int x
                void foo() {
                    def cl = { x = 666 }
                    cl()
                }
                void ensure() {
                    assert x == 666
                }
            }
            def a = new A()
            a.foo()
            a.ensure()
        '''
    }

    void testPrivateFieldAccessInAIC() {
        assertScript '''
            class A {
                private int x
                void foo() {
                    def aic = new Runnable() { void run() { x = 666 } }
                    aic.run()
                }
                void ensure() {
                    assert x == 666
                }
            }
            def a = new A()
            a.foo()
            a.ensure()
        '''
    }

    // GROOVY-9562
    void testSuperPropertyAccessInAIC() {
        assertScript '''
            abstract class One {
                int prop = 1
            }

            abstract class Two {
                int prop = 2

                abstract baz()
            }

            class Foo extends One {
                Two bar() {
                    new Two() {
                        def baz() {
                            prop
                        }
                    }
                }
            }

            assert new Foo().bar().baz() == 2
        '''
    }

    // GROOVY-5737
    void testAccessGeneratedFieldFromClosure() {
        assertScript '''
            import groovy.transform.*
            import groovy.util.logging.*

            @Log
            class GreetingActor {

              def receive = {
                log.info "test"
              }

            }
            new GreetingActor()
            '''
    }

    // GROOVY-5872
    void testAssignNullToFieldWithGenericsShouldNotThrowError() {
        assertScript '''
            class Foo {
                List<String> list = null // should not throw an error
            }
            new Foo()
        '''
    }

    void testSetterInWith() {
        assertScript '''
            class Builder {
                private int y
                void setFoo(int x) { y = x}
                int value() { y }
            }
            def b = new Builder()
            b.with {
                setFoo(5)
            }
            assert b.value() == 5
        '''
    }

    void testSetterInWithUsingPropertyNotation() {
        assertScript '''
            class Builder {
                private int y
                void setFoo(int x) { y = x}
                int value() { y }
            }
            def b = new Builder()
            b.with {
                foo = 5
            }
            assert b.value() == 5
        '''
    }

    void testSetterInWithUsingPropertyNotationAndClosureSharedVariable() {
        assertScript '''
            class Builder {
                private int y
                void setFoo(int x) { y = x}
                int value() { y }
            }
            def b = new Builder()
            def csv = 0
            b.with {
                foo = 5
                csv = 10
            }
            assert b.value() == 5
            assert csv == 10
        '''
    }

    // GROOVY-9653
    void testSetterInWithUsingPropertyNotation_DelegateAndOwnerHaveSetter() {
        assertScript '''
            class C {
                final result = new D().with {
                    something = 'value' // ClassCastException: D cannot be cast to C
                    return object
                }
                void setSomething(value) { }
            }

            class D {
                void setSomething(value) { }
                Object getObject() { 'works' }
            }

            assert new C().result == 'works'
        '''
    }

    // GROOVY-6230
    void testAttributeWithGetterOfDifferentType() {
        assertScript '''import java.awt.Dimension
            def d = new Dimension(800,600)

            @ASTTest(phase=INSTRUCTION_SELECTION,value={
                def rit = node.rightExpression.getNodeMetaData(INFERRED_TYPE)
                assert rit == int_TYPE
            })
            int width = d.@width
            assert width == 800
            assert (d.@width).getClass() == Integer
        '''
    }

    // GROOVY-6489
    void testShouldNotThrowUnmatchedGenericsError() {
        assertScript '''public class Foo {

    private List<String> names;

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }
}

class FooWorker {

    public void doSomething() {
        new Foo().with {
            names = new ArrayList()
        }
    }
}

new FooWorker().doSomething()'''
    }

    void testShouldFailWithIncompatibleGenericTypes() {
        shouldFailWithMessages '''\
            public class Foo {
                private List<String> names;

                public List<String> getNames() {
                    return names;
                }

                public void setNames(List<String> names) {
                    this.names = names;
                }
            }

            class FooWorker {
                public void doSomething() {
                    new Foo().with {
                        names = new ArrayList<Integer>()
                    }
                }
            }

            new FooWorker().doSomething()
        ''',
        'Cannot assign value of type java.util.ArrayList <Integer> to variable of type java.util.List <String>'
    }

    void testAICAsStaticProperty() {
        assertScript '''
            class Foo {
                static x = new Object() {}
            }
            assert Foo.x instanceof Object
        '''
    }

    void testPropertyWithMultipleSetters() {
        assertScript '''import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.BooleanExpression
import org.codehaus.groovy.ast.stmt.AssertStatement
            class A {
                private field
                void setX(Integer a) {field=a}
                void setX(String b) {field=b}
                def getX(){field}
            }

            @ASTTest(phase=INSTRUCTION_SELECTION,value={
                lookup('test1').each { stmt ->
                    def exp = stmt.expression
                    assert exp instanceof BinaryExpression
                    def left = exp.leftExpression
                    def md = left.getNodeMetaData(DIRECT_METHOD_CALL_TARGET)
                    assert md
                    assert md.name == 'setX'
                    assert md.parameters[0].originType == Integer_TYPE
                }
                lookup('test2').each { stmt ->
                    def exp = stmt.expression
                    assert exp instanceof BinaryExpression
                    def left = exp.leftExpression
                    def md = left.getNodeMetaData(DIRECT_METHOD_CALL_TARGET)
                    assert md
                    assert md.name == 'setX'
                    assert md.parameters[0].originType == STRING_TYPE
                }
            })
            void testBody() {
                def a = new A()
                test1:
                a.x = 1
                assert a.x==1
                test2:
                a.x = "3"
                assert a.x == "3"
            }
            testBody()
        '''
    }

    void testPropertyAssignmentAsExpression() {
        assertScript '''
            class Foo {
                int x = 2
            }
            def f = new Foo()
            def v = f.x = 3
            assert v == 3
'''
    }

    void testPropertyAssignmentInSubClassAndMultiSetter() {
        10.times {
            assertScript '''
                class A {
                    int which

                    A() {
                        contentView = 42L
                        assert which == 2
                    }

                    void setContentView(Date value) { which = 1 }
                    void setContentView(Long value) { which = 2 }
                }

                class B extends A {
                    void m() {
                        contentView = 42L
                        assert which == 2
                        contentView = new Date()
                        assert which == 1
                    }
                }

                new B().m()
            '''
        }
    }

    void testPropertyAssignmentInSubClassAndMultiSetterThroughDelegation() {
        10.times {
            assertScript '''\
                class A {
                    int which

                    void setContentView(Date value) { which = 1 }
                    void setContentView(Long value) { which = 2 }
                }

                class B extends A {
                }

                new B().with {
                    contentView = 42L
                    assert which == 2
                    contentView = new Date()
                    assert which == 1
                }
            '''
        }
    }

    void testShouldAcceptPropertyAssignmentEvenIfSetterOnlyBecauseOfSpecialType() {
        assertScript '''
            class BooleanSetterOnly {
                void setFlag(boolean b) {}
            }

            def b = new BooleanSetterOnly()
            b.flag = 'foo'
        '''
        assertScript '''
            class StringSetterOnly {
                void setFlag(String b) {}
            }

            def b = new StringSetterOnly()
            b.flag = false
        '''
        assertScript '''
            class ClassSetterOnly {
                void setFlag(Class b) {}
            }

            def b = new ClassSetterOnly()
            b.flag = 'java.lang.String'
        '''
    }

    // GROOVY-6590
    void testShouldFindStaticPropertyOnPrimitiveType() {
        assertScript '''
            int i=1
            i.MAX_VALUE
        '''
        assertScript '''
            def i="d"
            i=1
            i.MAX_VALUE
        '''
    }

    void testImplicitPropertyOfDelegateShouldNotPreferField() {
        assertScript '''
            Calendar.instance.with {
                Date d1 = time // Date getTime() vs. long time
            }
        '''
    }

    void testPropertyStyleSetterArgShouldBeCheckedAgainstParamType() {
        shouldFailWithMessages '''
            class Foo {
                Bar bar;

                void setBar(int x) {
                    this.bar = new Bar(x: x)
                }
            }

            class Bar {
                int x
            }

            Foo foo = new Foo()
            foo.bar = new Bar()
        ''', 'Cannot assign value of type Bar to variable of type int'

        assertScript '''
            class Foo {
                Bar bar;

                void setBar(int x) {
                    this.bar = new Bar(x: x)
                }
            }

            class Bar {
                int x
            }

            Foo foo = new Foo()
            foo.bar = 1
            assert foo.bar.x == 1
        '''
    }

    void testPropertyStyleGetterUsageShouldBeCheckedAgainstReturnType() {
        shouldFailWithMessages '''
            class Foo {
                Bar bar;

                int getBar() {
                    bar.x
                }
            }

            class Bar {
                int x
            }

            Foo foo = new Foo(bar: new Bar(x: 1))
            Bar bar = foo.bar
        ''', 'Cannot assign value of type int to variable of type Bar'

        assertScript '''
            class Foo {
                Bar bar;

                int getBar() {
                    bar.x
                }
            }

            class Bar {
                int x
            }

            Foo foo = new Foo(bar: new Bar(x: 1))
            int x = foo.bar
            assert x == 1
        '''
    }

    // GROOVY-9759
    void testPrivateFieldAsMapKeyInClosure() {
        assertScript '''
            class Foo {
                private static final Integer ANSWER = 42

                def checkMap() {
                    Map failures = [:]

                    def closure = {
                        assert ANSWER.longValue() == 42L // OKAY
                        failures[ANSWER.longValue()] = 1  // <== STC error here
                    }
                    closure()
                    failures
                }
            }
            assert new Foo().checkMap() == [42L:1]
        '''
    }

    static interface InterfaceWithField {
        String boo = "I don't fancy fields in interfaces"
    }

    static class BaseClass {
        int x
    }

    static class BaseClass2 extends BaseClass {
    }
}
