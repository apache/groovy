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
import groovy.transform.PackageScope

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

    // GROOVY-9955
    void testStaticPropertyWithInheritanceFromAnotherSourceUnit() {
        assertScript '''
            import groovy.transform.stc.FieldsAndPropertiesSTCTest.Public
            assert Public.answer == 42
            assert Public.CONST == 'XX'
            assert Public.VALUE == null
            Public.VALUE = 'YY'
            assert Public.VALUE == 'YY'
            Public.@VALUE = 'ZZ'
            assert Public.@VALUE == 'ZZ'
        '''
    }

    void testDateProperties() {
        assertScript '''
            Date d = new Date()
            def time = d.time
            d.time = 0
        '''
    }

    void testGetterForProperty1() {
        assertScript '''
            class C {
                String p
            }
            def x = new C().getP()
            x = x?.toUpperCase()
        '''
    }

    // GROOVY-9973
    void testGetterForProperty2() {
        assertScript '''
            class C {
                private int f
                int getP() { f }
                Integer m() { 123456 - p }
                Integer m(int i) { i - p }
            }

            def c = new C()
            assert c.m() == 123456 // BUG! exception in phase 'class generation' ...
            assert c.m(123) == 123 // ClassCastException: class org.codehaus.groovy.ast.Parameter cannot be cast to ...
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
                int bar = 1
            }
            new Foo()
        '''
    }

    // GROOVY-5443
    void testFieldInitShouldNotPassBecauseOfIncompatibleTypes() {
        shouldFailWithMessages '''
            class Foo {
                int bar = new Date()
            }
            new Foo()
        ''', 'Cannot assign value of type java.util.Date to variable of type int'
    }

    // GROOVY-5443
    void testFieldInitShouldNotPassBecauseOfIncompatibleTypesWithClosure() {
        shouldFailWithMessages '''
            class Foo {
                Closure<List> bar = { Date date -> date.getTime() }
            }
            new Foo()
        ''', 'Incompatible generic argument types. Cannot assign groovy.lang.Closure <java.lang.Long> to: groovy.lang.Closure <List>'
    }

    void testFieldInitShouldNotPassBecauseOfIncompatibleTypesWithClosure2() {
        shouldFailWithMessages '''
            class Foo {
                java.util.function.Supplier<String> bar = { 123 }
            }
            new Foo()
        ''', 'Incompatible generic argument types. Cannot assign java.util.function.Supplier <java.lang.Integer> to: java.util.function.Supplier <String>'
    }

    // GROOVY-9882
    void testFieldInitShouldPassForCompatibleTypesWithClosure() {
        assertScript '''
            class Foo {
                java.util.function.Supplier<String> bar = { 'abc' }
            }
            assert new Foo().bar.get() == 'abc'
        '''
    }

    void testClosureParameterMismatch() {
        shouldFailWithMessages '''
            class Foo {
                java.util.function.Supplier<String> bar = { baz -> '' }
            }
        ''', 'Wrong number of parameters for method target get()'
        shouldFailWithMessages '''
            class Foo {
                java.util.function.Consumer<String> bar = { -> null }
            }
        ''', 'Wrong number of parameters for method target accept(java.lang.String)'
    }

    // GROOVY-9991
    void testClosureParameterMatch() {
        assertScript '''
            java.util.function.Consumer<String> s = { print it }
        '''
        assertScript '''
            java.util.function.Predicate p = { x -> false }
        '''
        assertScript '''
            java.util.function.Predicate p = { false }
        '''
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

            // a subclass defining its own field
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
        ''', 'The variable [p] is undeclared.'
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

    // GROOVY-7024
    void testOuterPropertyAccess4() {
        assertScript '''
            class Outer {
                static Map props = [bar: 10, baz: 20]
                enum Inner {
                    FOO('foo'),
                    Inner(String name) {
                        props[name] = 30
                    }
                }
            }
            Outer.Inner.FOO
            assert Outer.props == [bar: 10, baz: 20, foo: 30]
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

    void testPrivateFieldAccessInClosure1() {
        assertScript '''
            class A {
                private int x
                void test() {
                    def c = { -> x = 666 }
                    c()
                    assert x == 666
                }
            }
            new A().test()
        '''
    }

    // GROOVY-9683
    void testPrivateFieldAccessInClosure2() {
        assertScript '''
            class A {
                private static X = 'xxx'
                void test() {
                    [:].withDefault { throw new MissingPropertyException(it.toString()) }.with {
                        assert X == 'xxx'
                    }
                }
            }
            new A().test()
        '''
    }

    // GROOVY-5737
    void testGeneratedFieldAccessInClosure() {
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

    // GROOVY-6610
    void testPrivateStaticFieldAccessBeforeThis() {
        assertScript '''
            class Outer {
                static class Inner {
                    public final String value

                    Inner(String string) {
                        value = string
                    }

                    Inner() {
                        this(VALUE.toString())
                    }
                }

                private static Integer VALUE = 42

                static main(args) {
                    assert new Inner().value == '42'
                }
            }
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

    // GROOVY-9893
    void testPropertyWithMultipleSetters2() {
        assertScript '''
            abstract class A { String which
                void setX(String s) { which = 'String' }
            }
            class C extends A {
                void setX(boolean b) { which = 'boolean' }
            }

            void test() {
                def c = new C()
                c.x = 'value'
                assert c.which == 'String'
            }
            test()
        '''
    }

    // GROOVY-9893
    void testPropertyWithMultipleSetters3() {
        assertScript '''
            interface I {
                void setX(String s)
            }
            abstract class A implements I { String which
                void setX(boolean b) { which = 'boolean' }
            }

            void test(A a) {
                a.x = 'value'
                assert a.which == 'String'
            }
            test(new A() { void setX(String s) { which = 'String' } })
        '''
    }

    // GROOVY-9893
    void testPropertyWithMultipleSetters4() {
        assertScript '''
            trait T { String which
                void setX(String s) { which = 'String' }
            }
            class C implements T {
                void setX(boolean b) { which = 'boolean' }
            }

            void test() {
                def c = new C()
                c.x = 'value'
                assert c.which == 'String'
            }
            test()
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

    // GROOVY-9855
    void testShouldInlineStringConcatInTypeAnnotation() {
        assertScript '''
            @SuppressWarnings(C.PREFIX + 'checked') // not 'un'.plus('checked')
            class C {
                public static final String PREFIX = 'un'
            }
            new C()
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

    static interface InterfaceWithField {
        String boo = "I don't fancy fields in interfaces"
    }

    static class BaseClass {
        int x
    }

    static class BaseClass2 extends BaseClass {
    }

    @PackageScope static class PackagePrivate {
        public static Number getAnswer() { 42 }
        public static final String CONST = 'XX'
        public static String VALUE
    }

    static class Public extends PackagePrivate {
    }
}
