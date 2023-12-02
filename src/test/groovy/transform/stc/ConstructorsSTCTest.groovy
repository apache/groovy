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

import org.codehaus.groovy.runtime.typehandling.GroovyCastException

/**
 * Unit tests for static type checking : constructors.
 */
class ConstructorsSTCTest extends StaticTypeCheckingTestCase {

    void testConstructFromList() {
        assertScript '''
            import java.awt.Dimension
            Dimension d = [100,200]
            Set set = []
            List list = []
        '''
    }

    void testWrongNumberOfArguments() {
        // test that wrong number of arguments will fail
        shouldFailWithMessages '''
            import java.awt.Dimension
            Dimension d = [100]
        ''',
        'No matching constructor found: java.awt.Dimension(int)'
    }

    void testWrongNumberOfArgumentsWithDefaultConstructor() {
        shouldFailWithMessages '''
            class X {}
            def foo() {
              new X("f")
            }
            println foo()
        ''',
        'Cannot find matching method X#<init>(java.lang.String)'
    }

    void testCreateArrayWithDefaultConstructor() {
        assertScript '''
            String[] strings = ['a','b','c']
            int[] ints = new int[2]
        '''
    }

    void testIncorrectArgumentTypes() {
        // test that wrong number of arguments will fail
        shouldFailWithMessages '''
            import java.awt.Dimension
            Dimension d = ['100','200']
        ''',
        'No matching constructor found: java.awt.Dimension(java.lang.String, java.lang.String)'
    }

    void testConstructFromListAndVariables() {
        assertScript '''
            import java.awt.Dimension
            int x = 100
            int y = 200
            Dimension d = [x,y]
            assert d.width == 100
            assert d.height == 200
        '''
    }

    void testConstructFromListAndVariables2() {
        assertScript '''
            import java.awt.Dimension
            int x = 100
            Dimension d = [x, '200'.toInteger()]
            assert d.width == 100
            assert d.height == 200
        '''
    }

    void testConstructFromVariable() {
        shouldFailWithMessages '''
            import java.awt.Dimension
            List args = [100,200]
            Dimension d = args // not supported
        ''',
        'Cannot assign value of type java.util.ArrayList <java.lang.Integer> to variable of type java.awt.Dimension'
    }

    void testConstructFromMap() {
        assertScript '''
            class A {
                int x
                int y
            }
            A a = [:]
            assert a != null
        '''
        assertScript '''
            class A {
                int x
                int y
            }
            def a = [:] as A
            assert a != null
        '''
        assertScript '''
            class A {
                int x
                int y
            }
            def a = A[:]
            assert a != null
        '''
    }

    void testConstructFromValuedMap1() {
        assertScript '''
            class A {
                int x
                int y
            }
            A a = [x:100, y:200]
            assert a.x == 100
            assert a.y == 200
        '''
    }

    void testConstructFromValuedMap2() {
        assertScript '''
            class A<B,C> {
                int x
                int y
            }
            A<Number,String> a = [x:100, y:200]
            assert a.x == 100
            assert a.y == 200
        '''
    }

    void testMapLiteral() {
        assertScript '''
            def m = [:]
            assert m instanceof Map
        '''
        assertScript '''
            Map m = [:]
            assert m instanceof Map
        '''
        assertScript '''
            Object m = [:]
            assert m instanceof Map
        '''
        assertScript '''
            HashMap m = [:]
            assert m instanceof HashMap
        '''
        assertScript '''
            LinkedHashMap m = [:]
            assert m instanceof LinkedHashMap
        '''

        shouldFail GroovyCastException, '''
            EnumMap m = [:] // constructor fails on empty map
        '''
        shouldFail GroovyCastException, '''
            SortedMap m = [:] // no constructor for interface
        '''
    }

    // GROOVY-9603
    void testDoNotConstructFromValuedMap() {
        assertScript '''
            void test(Map<String, Object> map) {
                // assign to local variable to establish standard behavior
                def foobar = [foo: 'bar']
                map.proper = foobar
                assert map.proper['foo'] == 'bar'

                // put map literal into "map" parameter in various forms:

                map.put('proper', [key: 'abc'])
                assert map.proper['key'] == 'abc'

                map['proper'] = [key: 'def']
                assert map.proper['key'] == 'def'

                map.proper = [key: 'ghi']
                assert map.proper['key'] == 'ghi'
            }
            test([:])
        '''
    }

    void testConstructFromCoercedMap() {
        assertScript '''
            class A {
                int x
                int y
            }
            def a = [x:100, y:200] as A
            assert a.x == 100
            assert a.y == 200
        '''
        assertScript '''
            class A {
                int x
                int y
            }
            def a = A[x:100, y:200]
            assert a.x == 100
            assert a.y == 200
        '''
    }

    void testConstructWithNamedParams() {
        assertScript '''
            class A {
                int x
                int y
            }
            A a = new A(x:100, y:200)
            assert a.x == 100
            assert a.y == 200
        '''
    }

    void testConstructFromValuedMapAndMissingProperty() {
        shouldFailWithMessages '''
            class A {
                int x
                int y
            }
            A a = [x:100, y:200, z: 300]
        ''', 'No such property: z for class: A'
    }

    void testConstructWithNamedParamsAndMissingProperty() {
        shouldFailWithMessages '''
            class A {
                int x
                int y
            }
            A a = new A(x:100, y:200, z: 300)
        ''', 'No such property: z for class: A'
    }

    void testConstructFromValuedMapAndIncorrectTypes() {
        shouldFailWithMessages '''
            class A {
                int x
                int y
            }
            A a = [x:'100', y:200]
        ''', 'Cannot assign value of type java.lang.String to variable of type int'
    }

    void testConstructFromValuedMapAndDynamicKey() {
        shouldFailWithMessages '''
            class A {
                int x
                int y
            }
            A a = ["${'x'}":'100']
        ''', 'Dynamic keys in map-style constructors are unsupported'
    }

    void testConstructWithMapAndInheritance() {
        assertScript '''
            class A {
                int x
            }
            class B extends A {
                int y
            }
            B b = [x:1, y:2]
            assert b.x == 1
            assert b.y == 2
        '''
    }

    // GROOVY-5231
    void testConstructorWithTupleConstructorAnnotation() {
        assertScript '''
        @groovy.transform.TupleConstructor
        class Person {
            String name, city
            static Person create() {
                new Person("Guillaume")
            }
        }

        Person.create()
        '''
    }

    // GROOVY-5531
    void testAccessToClosureVariableFromNamedParamConstructor() {
        // test using "str" as name
        assertScript '''
            class Person { String name }
            def cl = { String str ->
                new Person(name: str)
            }
            assert cl('Cédric').name == 'Cédric'
        '''

        // test using "it" as name
        assertScript '''
            class Person { String name }
            def cl = { String it ->
                new Person(name: it)
            }
            assert cl('Cédric').name == 'Cédric'
        '''

    }

    // GROOVY-5530
    void testUseGStringInNamedParameter() {
        assertScript '''class User {
            String login
            String username
            String domain
            String firstName
            String lastName
        }

        class UserBase {
            List<User> getUsers() {
                [1, 2, 3].collect { Number num ->
                     new User(
                            login:      "login$num",
                            username:   "username$num",
                            domain:     "domain$num",
                            firstName:  "first$num",
                            lastName:   "last$num"
                    )
                }
            }
        }

        def users = new UserBase().getUsers()
        assert users.get(0).login == "login1"
        '''
    }

    // GROOVY-9885
    void testUseGStringTernaryInNamedParameter() {
        assertScript '''
            @groovy.transform.ToString
            class Pogo {
                String value
            }
            def make(String string, whatever) {
                new Pogo(value: string.trim() ?: "$whatever")
            }
            assert make('x','y').toString() == 'Pogo(x)'
            assert make(' ','y').toString() == 'Pogo(y)'
            assert make(' ',123).toString() == 'Pogo(123)'
        '''
    }

    // GROOVY-5578
    void testConstructJavaBeanFromMap() {
        assertScript '''import groovy.transform.stc.MyBean

        MyBean bean = new MyBean<String>(value:'Cedric')
        assert bean.value == 'Cedric'
        '''
    }
    void testConstructJavaBeanFromMapAndSubclass() {
        assertScript '''import groovy.transform.stc.MyBean
        class MyBean2 extends MyBean<String> {
            int age
        }
        MyBean2 bean = new MyBean2(value:'Cedric', age:33)
        assert bean.value == 'Cedric'
        assert bean.age == 33
        '''
    }

    // GROOVY-5698
    void testMapConstructorWithInterface() {
        assertScript '''class CustomServletOutputStream extends OutputStream {
                OutputStream out

                void write(int i) {
                    out.write(i)
                }

                void write(byte[] bytes) {
                    out.write(bytes)
                }

                void write(byte[] bytes, int offset, int length) {
                    out.write(bytes, offset, length)
                }

                void flush() {
                    out.flush()
                }

                void close() {
                    out.close()
                }
            }

            class Test {
                static void test() {
                    def csos = new CustomServletOutputStream(out: new ByteArrayOutputStream())
                }
            }
            Test.test()
        '''
    }

    void testMapConstructorShouldFail() {
        shouldFailWithMessages '''
            class Foo {
                ByteArrayOutputStream out
            }
            void m(OutputStream o) { new Foo(out:o) }
        ''', 'Cannot assign value of type java.io.OutputStream to variable of type java.io.ByteArrayOutputStream'
    }

    void testTypeCheckingInfoShouldNotBeAddedToConstructor() {

        Class fooClass = assertClass '''
        class Foo {
            @groovy.transform.TypeChecked
            Foo() {}
        }
        '''

        def constructor = fooClass.getDeclaredConstructor()
        assert constructor.declaredAnnotations.size() == 0
    }

    // GROOVY-6616
    void testConstructorsWithVarargsAndArrayParameters() {
        assertScript '''
            class MultipleConstructors {

                public MultipleConstructors(String s, short[] arr) {}
                public MultipleConstructors(String s, int... arr) {}
                public MultipleConstructors(short[] arr) {}
            }

            class Clz {
                  void run() {
                        new MultipleConstructors('d',1)
                }
            }

            new Clz().run()
        '''
    }

    // GROOVY-6929
    void testShouldNotThrowNPEDuringConstructorCallCheck() {
        assertScript '''
            class MyBean {
                private String var
                void setFoo(String foo) {
                    var = foo
                }
                String toString() { var }
            }
            def b = new MyBean(foo: 'Test')
            assert b.toString() == 'Test'
        '''
    }

    void testMapStyleConstructorShouldNotCarrySetterInfoToOuterBinExp() {
        assertScript '''
            class Blah {
                void setA(String a) {}
            }

            void blah(Map attrs) {
               Closure c = {
                  def blah = new Blah(a:attrs.a as String)
               }
            }
            blah(a:'foo')
        '''
    }

    // GROOVY-7164
    void testMapStyleConstructorWhenSetterParamAndFieldHaveDifferentTypes() {
        assertScript '''
            class C {
                private long timestamp

                Date getTimestamp() {
                    return timestamp ? new Date(timestamp) : null
                }

                void setTimestamp(Date timestamp) {
                    this.timestamp = timestamp.time
                }
            }
            new C(timestamp: new Date())
        '''
    }

    // GROOVY-10787
    void testMapStyleConstructorWithParameterizedProperty() {
        assertScript '''
            abstract class A<X extends Serializable> {
                X x
            }
            class C<Y extends Serializable> extends A<Y> {
            }

            def <Z extends Number> C<Z> fn(List<Z> list_of_z) {
                new C<Z>(x: list_of_z.first())
            }

            def c = fn([42])
            assert c.x == 42
        '''
    }

    // GROOVY-11119
    void testMapStyleConstructorWithOverloadedSetterName() {
        assertScript '''import java.util.regex.Pattern
            class C {
                void setP(Pattern p) {
                    which = 'Pattern'
                }
                void setP(String re) {
                    which = 'String'
                }
                def which
            }

            assert new C(p:"xx").which == 'String'
            assert new C(p:~/x/).which == 'Pattern'
        '''
    }

    // GROOVY-11122
    void testMapStyleInnerClassConstructorWithinClosure() {
        assertScript '''
            class A {
                class B {
                    def p
                }
                B test() {
                    { ->
                        new B(p:"x")
                    }.call()
                }
            }

            assert new A().test().getP() == "x"
        '''
    }

    // GROOVY-9422
    void testInnerClassConstructorCallWithinClosure() {
        assertScript '''
            class A {
              class B {
                B(param) {}
                String x = 'value'
              }
              def test() {
                ['s'].collect { String s ->
                  new B(s).x // expect outer class, not closure as implicit first param to inner class constructor
                }
              }
            }

            assert new A().test() == ['value']
        '''
    }
}
