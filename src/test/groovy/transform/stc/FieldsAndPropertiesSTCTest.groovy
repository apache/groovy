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
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit

/**
 * Unit tests for static type checking : fields and properties.
 */
class FieldsAndPropertiesSTCTest extends StaticTypeCheckingTestCase {

    void testAssignFieldValue() {
        assertScript '''
            class C { int x }
            C c = new C()
            c.x = 1
        '''
        shouldFailWithMessages '''
            class C { int x }
            C c = new C()
            c.x = '1'
        ''',
        'Cannot assign value of type java.lang.String to variable of type int'
    }

    void testAssignFinalFieldValue() {
        assertScript '''
            class C {
                final x = null
            }
            new C().x
        '''
        assertScript '''
            class C {
                final x
                C() {
                    x = null
                }
            }
            new C().x
        '''
        assertScript '''
            class C {
                final x
                C(def x) {
                    this.x = x
                }
            }
            new C(null).x
        '''
        assertScript '''
            class C {
                final x;
                {
                    x = null
                }
            }
            new C().x
        '''
        assertScript '''
            class C {
                final x;
                {
                    this.x = x
                }
            }
            new C().x
        '''
        assertScript '''
            class C {
                static final x
                static {
                    this.x = null
                }
            }
            new C().x
        '''
    }

    void testAssignFinalFieldValue2() {
        shouldFailWithMessages '''
            int[] array = []
            array.length = 1
        ''',
        'Cannot set read-only property: length'

        shouldFailWithMessages '''
            class C { final x }
            new C().x = null
        ''',
        'Cannot set read-only property: x'

        // GROOVY-5450
        shouldFailWithMessages '''
            class C { final x }
            new C().@x = null
        ''',
        'Cannot set read-only property: x'

        shouldFailWithMessages '''
            class C { final x }
            new C().with { x = null }
        ''',
        'Cannot set read-only property: x'

        shouldFailWithMessages '''
            class C { final x }
            new C().with { delegate.x = null }
        ''',
        'Cannot set read-only property: x'

        shouldFailWithMessages '''
            class C { final x }
            new C().setX(null)
        ''',
        'Cannot find matching method C#setX(<unknown parameter type>).'
    }

    void testInferenceFromFieldType() {
        assertScript '''
            class C {
                String name = 'Cedric'
            }
            C c = new C()
            def x = c.name
            x.toUpperCase() // type of x should be inferred from field type
        '''
    }

    void testAssignFieldValueWithAttributeNotation() {
        assertScript '''
            class C {
                int x
            }
            C c = new C()
            c.@x = 1
        '''
    }

    void testAssignFieldValueWithWrongTypeAndAttributeNotation() {
         shouldFailWithMessages '''
             class C {
                 int x
             }
             C c = new C()
             c.@x = '1'
         ''',
         'Cannot assign value of type java.lang.String to variable of type int'
     }

    void testInferenceFromAttributeType() {
        assertScript '''
            class C {
                String name = 'Cedric'
            }
            C c = new C()
            def x = c.@name
            x.toUpperCase() // type of x should be inferred from field type
        '''
    }

    void testShouldComplainAboutMissingProperty() {
        shouldFailWithMessages '''
            Object o = new Object()
            o.x = 0
        ''',
        'No such property: x for class: java.lang.Object'
    }

    void testShouldComplainAboutMissingProperty2() {
        shouldFailWithMessages '''
            class C {
            }
            C c = new C()
            c.x = 0
        ''',
        'No such property: x for class: C'
    }

    @NotYetImplemented
    void testShouldComplainAboutMissingProperty3() {
        shouldFailWithMessages '''
            class C {
                private x
            }
            class D extends C {
                void test() {
                    this.x
                }
            }
        ''',
        'The field C.x is not accessible'
    }

    void testShouldComplainAboutMissingAttribute() {
        shouldFailWithMessages '''
            Object o = new Object()
            o.@x = 0
        ''',
        'No such attribute: x for class: java.lang.Object'
    }

    void testShouldComplainAboutMissingAttribute2() {
        shouldFailWithMessages '''
            class C {
            }
            C c = new C()
            c.@x = 0
        ''',
        'No such attribute: x for class: C'
    }

    void testShouldComplainAboutMissingAttribute3() {
        shouldFailWithMessages '''
            class C {
                def getX() { }
            }
            C c = new C()
            println c.@x
        ''',
        'No such attribute: x for class: C'
    }

    void testShouldComplainAboutMissingAttribute4() {
        shouldFailWithMessages '''
            class C {
                def setX(x) { }
            }
            C c = new C()
            c.@x = 0
        ''',
        'No such attribute: x for class: C'
    }

    void testShouldComplainAboutMissingAttribute5() {
        shouldFailWithMessages '''
            class C {
                private x
            }
            class D extends C {
                void test() {
                    this.@x
                }
            }
        ''',
        'The field C.x is not accessible'
    }

    void testPropertyWithInheritance() {
        assertScript '''
            class C {
                int x
            }
            class D extends C {
            }
            D d = new D()
            assert d.x == 0
            d.x = 2
            assert d.x == 2
        '''
    }

    void testPropertyTypeWithInheritance() {
        shouldFailWithMessages '''
            class C {
                int x
            }
            class D extends C {
            }
            D d = new D()
            d.x = '2'
        ''',
        'Cannot assign value of type java.lang.String to variable of type int'
    }

    void testPropertyWithInheritanceFromAnotherSourceUnit() {
        assertScript '''
            class C extends groovy.transform.stc.FieldsAndPropertiesSTCTest.BaseClass {
            }
            C c = new C()
            c.x = 2
        '''
    }

    void testPropertyWithInheritanceFromAnotherSourceUnit2() {
        shouldFailWithMessages '''
            class C extends groovy.transform.stc.FieldsAndPropertiesSTCTest.BaseClass {
            }
            C c = new C()
            c.x = '2'
        ''',
        'Cannot assign value of type java.lang.String to variable of type int'
    }

    void testPropertyWithSuperInheritanceFromAnotherSourceUnit() {
        assertScript '''
            class C extends groovy.transform.stc.FieldsAndPropertiesSTCTest.BaseClass2 {
            }
            C c = new C()
            c.x = 2
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
            Public.VALUE = null
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

    // GROOVY-10981
    void testGetterForProperty2() {
        for (mode in ['', 'public', 'private', 'protected', '@groovy.transform.PackageScope']) {
            assertScript """
                abstract class A {
                    $mode Object p = 'field'
                    CharSequence getP() { 'property' }
                }
                class C extends A {
                    def m() {
                        final int len = p.length()
                        if (p instanceof String) {
                            p.toLowerCase()
                            p.toUpperCase()
                        }
                    }
                }

                String which = new C().m()
                assert which == 'PROPERTY'
            """
        }
    }

    // GROOVY-9973
    void testGetterForProperty3() {
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

    // GROOVY-11005
    void testGetterForProperty4() {
        File parentDir = File.createTempDir()
        config.with {
            targetDirectory = File.createTempDir()
            jointCompilationOptions = [memStub: true]
        }
        try {
            def a = new File(parentDir, 'Pogo.groovy')
            a.write '''
                class Pogo {
                    String value
                    String getValue() { value }
                }
            '''
            def b = new File(parentDir, 'Test.groovy')
            b.write '''
                class Test extends Pogo {
                    void test() {
                        value = 'string'
                    }
                }
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(a, b)
            cu.compile()

            loader.loadClass('Test').newInstance().test()
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
        }
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
            class C {
                int bar = 1
            }
            new C()
        '''
    }

    // GROOVY-5443
    void testFieldInitShouldNotPassBecauseOfIncompatibleTypes() {
        shouldFailWithMessages '''
            class C {
                int bar = new Date()
            }
            new C()
        ''',
        'Cannot assign value of type java.util.Date to variable of type int'
    }

    // GROOVY-5443
    void testFieldInitShouldNotPassBecauseOfIncompatibleTypesWithClosure() {
        shouldFailWithMessages '''
            class C {
                Closure<List> bar = { Date date -> date.getTime() }
            }
            new C()
        ''',
        'Incompatible generic argument types. Cannot assign groovy.lang.Closure <java.lang.Long> to: groovy.lang.Closure <List>'
    }

    void testFieldInitShouldNotPassBecauseOfIncompatibleTypesWithClosure2() {
        shouldFailWithMessages '''
            class C {
                java.util.function.Supplier<String> bar = { 123 }
            }
            new C()
        ''',
        'Incompatible generic argument types. Cannot assign java.util.function.Supplier <java.lang.Integer> to: java.util.function.Supplier <String>'
    }

    // GROOVY-9882
    void testFieldInitShouldPassForCompatibleTypesWithClosure() {
        assertScript '''
            class C {
                java.util.function.Supplier<String> bar = { 'abc' }
            }
            assert new C().bar.get() == 'abc'
        '''
    }

    void testClosureParameterMismatch() {
        shouldFailWithMessages '''
            class C {
                java.util.function.Supplier<String> bar = { baz -> '' }
            }
        ''',
        'Wrong number of parameters for method target: get()'

        shouldFailWithMessages '''
            class C {
                java.util.function.Consumer<String> bar = { -> null }
            }
        ''',
        'Wrong number of parameters for method target: accept(java.lang.String)'
    }

    // GROOVY-9991
    void testClosureParameterMatch() {
        assertScript '''
            java.util.function.Consumer<String> c = { print it }
        '''
        assertScript '''
            java.util.function.Predicate p = { x -> false }
        '''
        assertScript '''
            java.util.function.Predicate p = { false }
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
            class C {
                boolean ok = false
                void setFoo(String foo) { ok = (foo == 'foo') }
            }
            def c = new C()
            c.foo = 'foo'
            assert c.ok
        '''
    }

    void testSetterUsingPropertyNotationOnInterface() {
        assertScript '''
            interface FooAware { void setFoo(String arg) }
            class C implements FooAware {
                void setFoo(String foo) { }
            }
            void test(FooAware fa) {
                fa.foo = 'foo'
            }
            def c = new C()
            test(c)
        '''
    }

    void testListDotProperty1() {
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

    void testListDotProperty2() {
        assertScript '''
            class C { int x }
            def list = [new C(x:1), new C(x:2)]
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def type = node.getNodeMetaData(INFERRED_TYPE)
                assert type.toString(false) == 'java.util.List <java.lang.Integer>'
            })
            def x = list.x
            assert x == [1,2]
        '''
    }

    // GROOVY-5700
    void testMapPropertyAccess1() {
        assertScript '''
            def map = [key: 123]
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == Integer_TYPE
            })
            def val = map.key
            assert val == 123
        '''
    }

    // GROOVY-5700, GROOVY-8788
    void testMapPropertyAccess2() {
        assertScript '''
            def map = [key: 123]
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == Integer_TYPE
            })
            def val = map['key']
            assert val == 123
        '''
    }

    // GROOVY-8074
    void testMapPropertyAccess3() {
        assertScript '''
            class C extends HashMap {
                def foo = 1
            }
            def map = new C()
            map.put('foo', 42)
            assert map.foo == 42
        '''

        assertScript """
            def map = new ${MapType.name}()
            map.put('foo', 42)
            assert map.foo == 42
        """
    }

    // GROOVY-5517
    void testMapPropertyAccess4() {
        assertScript '''
            class C extends HashMap {
                public static int version = 666
            }
            def map = new C()
            map.foo = 123
            def value = map.foo
            assert value == 123
            map['foo'] = 4.5
            value = map['foo']
            assert value == 4.5
            value = map.version
            assert value == null
            assert C.version == 666
        '''
    }

    void testTypeCheckerDoesNotThinkPropertyIsReadOnly() {
        assertScript '''
            // a base class defining a read-only property
            class C {
                private String foo = 'foo'
                String getFoo() { foo }
                String getFooFromC() { foo }
            }

            // a subclass defining its own field
            class D extends C {
                private String foo

                D(String msg) {
                    this.foo = msg
                }

                public String getFoo() { this.foo }
            }

            def d = new D('bar')
            assert d.foo == 'bar'
            assert d.fooFromC == 'foo'
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
            class C implements groovy.transform.stc.FieldsAndPropertiesSTCTest.InterfaceWithField {
                void test() {
                    assert boo == "I don't fancy fields in interfaces"
                }
            }
            new C().test()
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

    void testOuterPropertyAccess2() {
        assertScript '''
            class Outer {
                class Inner {
                    def m() {
                        getP()
                    }
                }
                def p = 1
            }
            def i = new Outer.Inner(new Outer())
            def x = i.m()
            assert x == 1
        '''
    }

    // GROOVY-10414
    void testOuterPropertyAccess3() {
        assertScript '''
            class Outer {
                class Inner {
                    def m() {
                        setP(2)
                        getP()
                    }
                }
                def p = 1
            }
            def i = new Outer.Inner(new Outer())
            def x = i.m()
            assert x == 2
        '''
    }

    // GROOVY-8050
    void testOuterPropertyAccess4() {
        shouldFailWithMessages '''
            class Outer {
                class Inner {
                }
                def p = 1
            }
            def i = new Outer.Inner(new Outer())
            def x = i.p
        ''',
        'No such property: p for class: Outer$Inner'
    }

    // GROOVY-8050
    void testOuterPropertyAccess5() {
        shouldFailWithMessages '''
            class Outer {
                class Inner {
                }
                def p = 1
            }
            def i = new Outer.Inner(new Outer())
            def x = i.getP()
        ''',
        'Cannot find matching method Outer$Inner#getP()'
    }

    // GROOVY-9598
    void testOuterPropertyAccess6() {
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
        ''',
        'The variable [p] is undeclared.'
    }

    void testOuterPropertyAccess7() {
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
        ''',
        'No such property: p for class: Outer$Inner'
    }

    // GROOVY-7024
    void testOuterPropertyAccess8() {
        assertScript '''
            class Outer {
                static Map props = [bar: 10, baz: 20]
                enum Inner {
                    FOO('foo');
                    Inner(String name) {
                        props[name] = 30
                    }
                }
            }
            Outer.Inner.FOO
            assert Outer.props == [bar: 10, baz: 20, foo: 30]
        '''
    }

    // GROOVY-10981, GROOVY-10985
    void testOuterPropertyAccess9() {
        for (propertySource in [
                'def get(String name){if(name=="VALUE")return 2}',
                'def getProperty(String name){if(name=="VALUE")return 2}',
                '@Delegate private Map<String,Object> map = [VALUE:(Object)2]']) {
            assertScript """
                class Outer {
                    static private int VALUE = 1
                    static class Inner {
                        $propertySource
                        int test(int i) {
                            if (i > VALUE) {
                                // ...
                            }
                            return VALUE
                        }
                    }
                }
                Number value = new Outer.Inner().test(0)
                assert value == 1 // this is legacy result
            """
        }
    }

    // GROOVY-11029
    void testSuperPropertyAccess1() {
        assertScript '''
            class Foo {
                Object myThing
            }
            class Bar extends Foo {
                @Override
                Object getMyThing() {
                    super.myThing
                }
                @Override
                void setMyThing(Object object) {
                    super.myThing = object
                }
            }

            def bar = new Bar()
            def value = 'thing'
            bar.myThing = value
            assert bar.myThing === value
        '''
    }

    // GROOVY-9562
    void testSuperPropertyAccess2() {
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

    void testPrivateFieldAccessInAIC() {
        assertScript '''
            class C {
                private int x
                void foo() {
                    def aic = new Runnable() { void run() { x = 666 } }
                    aic.run()
                }
                void ensure() {
                    assert x == 666
                }
            }
            def c = new C()
            c.foo()
            c.ensure()
        '''
    }

    void testPrivateFieldAccessInClosure1() {
        assertScript '''
            class C {
                private int x
                void test() {
                    def c = { -> x = 666 }
                    c()
                    assert x == 666
                }
            }
            new C().test()
        '''
    }

    // GROOVY-9683
    void testPrivateFieldAccessInClosure2() {
        assertScript '''
            class C {
                private static X = 'xxx'
                void test() {
                    [:].with {
                        assert X == 'xxx'
                    }
                }
            }
            new C().test()
        '''
    }

    void testPrivateFieldAccessInClosure3() {
        assertScript '''
            class C {
                private static X = 'xxx'
                void test() {
                    [:].withDefault { throw new MissingPropertyException(it.toString()) }.with {
                        assert X == 'xxx'
                    }
                }
            }
            new C().test()
        '''
    }

    // GROOVY-9695
    void testPrivateFieldAccessInClosure4() {
        assertScript '''
            class C {
                private static final X = 'xxx'
                void test() {
                    Map m = [:]
                    def c = { ->
                        assert X == 'xxx'
                        m[X] = 123
                    }
                    c()
                    assert m == [xxx:123]
                }
            }
            new C().test()

            class D extends C {
            }
            new D().test()
        '''
    }

    // GROOVY-5737
    void testGeneratedFieldAccessInClosure() {
        assertScript '''
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

    // GROOVY-7890
    void testNonStaticPropertyAndStaticMethodClosure() {
        shouldFailWithMessages '''
            class C {
                List<String> replace
                static String m(String s) {
                    s.collectReplacements {
                        (it in replace) ? 'o' : null
                    }
                }
            }
        ''',
        'The variable [replace] is undeclared'

        assertScript '''
            class C {
              List<String> replace
              String m(String s) {
                s.collectReplacements {
                  (it in replace) ? 'o' : null
                }
              }
            }
            String result = new C(replace:['a','b','c']).m('foobar')
            assert result == 'foooor'
        '''
    }

    // GROOVY-5872
    void testAssignNullToFieldWithGenericsShouldNotThrowError() {
        assertScript '''
            class C {
                List<String> list = null // should not throw an error
            }
            new C()
        '''
    }

    void testSetterInWith() {
        assertScript '''
            class C {
                private int y
                void setFoo(int x) { y = x }
                int value() { y }
            }
            def c = new C()
            c.with {
                setFoo(5)
            }
            assert c.value() == 5
        '''
    }

    void testSetterInWithUsingPropertyNotation() {
        assertScript '''
            class C {
                private int y
                void setFoo(int x) { y = x }
                int value() { y }
            }
            def c = new C()
            c.with {
                foo = 5
            }
            assert c.value() == 5
        '''
    }

    void testSetterInWithUsingPropertyNotationAndClosureSharedVariable() {
        assertScript '''
            class C {
                private int y
                void setFoo(int x) { y = x }
                int value() { y }
            }
            def c = new C()
            def csv = 0
            c.with {
                foo = 5
                csv = 10
            }
            assert c.value() == 5
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
        assertScript '''
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
                        names = new ArrayList()
                    }
                }
            }
            new FooWorker().doSomething()
        '''
    }

    void testShouldFailWithIncompatibleGenericTypes() {
        shouldFailWithMessages '''
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
        'Cannot assign java.util.ArrayList <Integer> to: java.util.List <String>'
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
        assertScript '''
            import org.codehaus.groovy.ast.expr.*
            import org.codehaus.groovy.ast.stmt.*

            class C {
                private field
                void setX(Integer a) {field=a}
                void setX(String b) {field=b}
                def getX(){field}
            }

            @ASTTest(phase=INSTRUCTION_SELECTION, value={
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
                def c = new C()
                test1:
                c.x = 1
                assert c.x==1
                test2:
                c.x = "3"
                assert c.x == "3"
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

            def c = new C()
            c.x = 'value'
            assert c.which == 'String'
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

            def c = new C()
            c.x = 'value'
            assert c.which == 'String'
        '''
    }

    void testPropertyAssignmentAsExpression() {
        assertScript '''
            class C {
                int x = 2
            }
            def c = new C()
            def x = c.x = 3
            assert x == 3
        '''
    }

    void testPropertyAssignmentInSubClassAndMultiSetter() {
        10.times {
            assertScript '''
                class C {
                    int which

                    C() {
                        contentView = 42L
                        assert which == 2
                    }

                    void setContentView(Date value) { which = 1 }
                    void setContentView(Long value) { which = 2 }
                }

                class D extends C {
                    void m() {
                        contentView = 42L
                        assert which == 2
                        contentView = new Date()
                        assert which == 1
                    }
                }

                new D().m()
            '''
        }
    }

    void testPropertyAssignmentInSubClassAndMultiSetterThroughDelegation() {
        10.times {
            assertScript '''
                class C {
                    int which

                    void setContentView(Date value) { which = 1 }
                    void setContentView(Long value) { which = 2 }
                }

                class D extends C {
                }

                new D().with {
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
        ''',
        'Cannot assign value of type Bar to variable of type int'

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
        ''',
        'Cannot assign value of type int to variable of type Bar'

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

    //--------------------------------------------------------------------------

    static interface InterfaceWithField {
        String boo = "I don't fancy fields in interfaces"
    }

    static class MapType extends HashMap<String,Object> {
        def foo = 1
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
