/*
 * Copyright 2003-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.transform.stc


/**
 * Unit tests for static type checking : constructors.
 *
 * @author Cedric Champeau
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
        ''', 'No matching constructor found: java.awt.Dimension<init>(int)'
    }

    void testIncorrectArgumentTypes() {
        // test that wrong number of arguments will fail
        shouldFailWithMessages '''
            import java.awt.Dimension
            Dimension d = ['100','200']
        ''', 'No matching constructor found: java.awt.Dimension<init>(java.lang.String, java.lang.String)'
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
        ''', 'Cannot assign value of type java.util.List to variable of type java.awt.Dimension'
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
    }

    void testConstructMap() {
        assertScript '''
            def a = [:]
            Map b = [:]
            Object c = [:]
            HashMap d = [:]
        '''
    }

    void testConstructFromValuedMap() {
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
}

