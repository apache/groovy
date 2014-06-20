/*
 * Copyright 2003-2014 the original author or authors.
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


package typing

import groovy.transform.stc.StaticTypeCheckingTestCase

class TypeCheckingTest extends StaticTypeCheckingTestCase {

    void testIntroduction() {
        new GroovyShell().evaluate '''
        // tag::stc_intro_magic[]
        Person.metaClass.getFormattedName = { "$delegate.firstName $delegate.lastName" }
        // end::stc_intro_magic[]
        // tag::stc_intro[]
        class Person {                                                          // <1>
            String firstName
            String lastName
        }
        def p = new Person(firstName: 'Raymond', lastName: 'Devos')             // <2>
        assert p.formattedName == 'Raymond Devos'                               // <3>
        // end::stc_intro[]
        '''
    }

    void testTypeCheckedAnnotation() {
        def shell = new GroovyShell()
        shell.evaluate '''
        // tag::typechecked_class[]
        @groovy.transform.TypeChecked
        class Calculator {
            int sum(int x, int y) { x+y }
        }
        // end::typechecked_class[]
        assert new Calculator().sum(4,5) == 9
        '''
        shell.evaluate '''
        // tag::typechecked_method[]
        class Calculator {
            @groovy.transform.TypeChecked
            int sum(int x, int y) { x+y }
        }
        // end::typechecked_method[]
        assert new Calculator().sum(4,5) == 9
        '''
    }

    void testTypeCheckingAssignmentRules() {
        assertScript '''
            // tag::stc_assign_equals[]
            Date now = new Date()
            // end::stc_assign_equals[]
        '''
        assertScript '''
            // tag::stc_assign_specialcase[]
            String s = new Date() // implicit call to toString
            Boolean boxed = 'some string'       // Groovy truth
            boolean prim = 'some string'        // Groovy truth
            Class clazz = 'java.lang.String'    // class coercion
            // end::stc_assign_specialcase[]
        '''

        assertScript '''
            // tag::stc_assign_null[]
            String s = null         // passes
            // end::stc_assign_null[]
        '''

        shouldFailWithMessages '''
            // tag::stc_assign_null2prim[]
            int i = null            // fails
            // end::stc_assign_null2prim[]
        ''', 'Cannot assign value of type java.lang.Object to variable of type int'

        assertScript '''
            // tag::stc_assign_array[]
            int[] i = new int[4]        // passes
            // end::stc_assign_array[]
        '''

        assertScript '''
            // tag::stc_assign_array_list[]
            int[] i = [1,2,3]               // passes
            // end::stc_assign_array_list[]
        '''

        shouldFailWithMessages '''
            // tag::stc_assign_array[]
            int[] i = new String[4]     // fails
            // end::stc_assign_array[]
        ''', 'Cannot assign value of type java.lang.String[] to variable of type int[]'

        shouldFailWithMessages '''
            // tag::stc_assign_array_list_fail[]
            int[] i = [1,2, new Date()]     // fails
            // end::stc_assign_array_list_fail[]
        ''', 'Cannot assign value of type java.util.Date into array of type int[]'

        assertScript '''
            // tag::stc_assign_superclass[]
            AbstractList list = new ArrayList()     // passes
            // end::stc_assign_superclass[]
        '''

        shouldFailWithMessages '''
            // tag::stc_assign_superclass_fail[]
            LinkedList list = new ArrayList()       // fails
            // end::stc_assign_superclass_fail[]
        ''', 'Cannot assign value of type java.util.ArrayList to variable of type java.util.LinkedList'

        assertScript '''
            // tag::stc_assign_interface[]
            List list = new ArrayList()             // passes
            // end::stc_assign_interface[]
        '''

        shouldFailWithMessages '''
            // tag::stc_assign_interface_fail[]
            RandomAccess list = new LinkedList()    // fails
            // end::stc_assign_interface_fail[]
        ''', 'Cannot assign value of type java.util.LinkedList to variable of type java.util.RandomAccess'

        assertScript '''
            // tag::stc_assign_prim[]
            int i = 0
            Integer bi = 1
            int x = new Integer(123)
            double d = new Float(5f)
            // end::stc_assign_prim[]
        '''

        assertScript '''
            // tag::stc_closure_coercion[]
            Runnable r = { println 'Hello' }
            interface SAMType {
                int doSomething()
            }
            SAMType sam = { 123 }
            assert sam.doSomething() == 123
            abstract class AbstractSAM {
                int calc() { 2* value() }
                abstract int value()
            }
            AbstractSAM c = { 123 }
            assert c.calc() == 246
            // end::stc_closure_coercion[]
        '''

        assertScript '''
            // tag::stc_assign_to_double[]
            Double d1 = 4d
            Double d2 = 4f
            Double d3 = 4l
            Double d4 = 4i
            Double d5 = (short) 4
            Double d6 = (byte) 4
            // end::stc_assign_to_double[]
        '''

        assertScript '''
            // tag::stc_assign_to_float[]
            Float f1 = 4f
            Float f2 = 4l
            Float f3 = 4i
            Float f4 = (short) 4
            Float f5 = (byte) 4
            // end::stc_assign_to_float[]
        '''

        assertScript '''
            // tag::stc_assign_to_long[]
            Long l1 = 4l
            Long l2 = 4i
            Long l3 = (short) 4
            Long l4 = (byte) 4
            // end::stc_assign_to_long[]
        '''

        assertScript '''
            // tag::stc_assign_to_int[]
            Integer i1 = 4i
            Integer i2 = (short) 4
            Integer i3 = (byte) 4
            // end::stc_assign_to_int[]
        '''

        assertScript '''
            // tag::stc_assign_to_short[]
            Short s1 = (short) 4
            Short s2 = (byte) 4
            // end::stc_assign_to_short[]
        '''

        assertScript '''
            // tag::stc_assign_to_byte[]
            Byte b1 = (byte) 4
            // end::stc_assign_to_byte[]
        '''
    }

    void testGroovyConstructors() {
        assertScript '''
            // tag::stc_ctor_point_classic[]
            @groovy.transform.TupleConstructor
            class Person {
                String firstName
                String lastName
            }
            Person classic = new Person('Ada','Lovelace')
            // end::stc_ctor_point_classic[]
            // tag::stc_ctor_point_list[]
            Person list = ['Ada','Lovelace']
            // end::stc_ctor_point_list[]
            // tag::stc_ctor_point_map[]
            Person map = [firstName:'Ada', lastName:'Lovelace']
            // end::stc_ctor_point_map[]
        '''
        shouldFailWithMessages '''
            // tag::stc_ctor_fail[]
            @groovy.transform.TupleConstructor
            class Person {
                String firstName
                String lastName
            }
            Person map = [firstName:'Ada', lastName:'Lovelace', age: 24]     // <1>
            // end::stc_ctor_fail[]
        ''', 'No such property: age for class: Person'
    }

    void testMatchArgumentsWithParameters() {
        assertScript '''
            // tag::stc_argparam_equals[]
            int sum(int x, int y) {
                x+y
            }
            assert sum(3,4) == 7
            // end::stc_argparam_equals[]
        '''
        assertScript '''
            // tag::stc_argparam_specialcase[]
            String format(String str) {
                "Result: $str"
            }
            assert format("${3+4}") == "Result: 7"
            // end::stc_argparam_specialcase[]
        '''

        assertScript '''
            // tag::stc_argparam_null[]
            String format(int value) {
                "Result: $value"
            }
            assert format(7) == "Result: 7"
            // end::stc_argparam_null[]
        '''

        shouldFailWithMessages '''
            String format(int value) {
                "Result: $value"
            }
            // tag::stc_argparam_null2prim[]
            format(null)           // fails
            // end::stc_argparam_null2prim[]
        ''', '#format(int) with arguments [<unknown parameter type>]'

        assertScript '''
            // tag::stc_argparam_array[]
            String format(String[] values) {
                "Result: ${values.join(' ')}"
            }
            assert format(['a','b'] as String[]) == "Result: a b"
            // end::stc_argparam_array[]
        '''

        shouldFailWithMessages '''
            String format(String[] values) {
                "Result: ${values.join(' ')}"
            }
            // tag::stc_argparam_array_fail[]
            format([1,2] as int[])              // fails
            // end::stc_argparam_array_fail[]
        ''', '#format(int[]). Please check if the declared type is right and if the method exists.'


        assertScript '''
            // tag::stc_argparam_superclass[]
            String format(AbstractList list) {
                list.join(',')
            }
            format(new ArrayList())              // passes
            // end::stc_argparam_superclass[]
        '''

        shouldFailWithMessages '''
            // tag::stc_argparam_superclass_fail[]
            String format(LinkedList list) {
                list.join(',')
            }
            format(new ArrayList())              // fails
            // end::stc_argparam_superclass_fail[]
        ''', '#format(java.util.ArrayList). Please check if the declared type is right and if the method exists.'

        assertScript '''
            // tag::stc_argparam_interface[]
            String format(List list) {
                list.join(',')
            }
            format(new ArrayList())                  // passes
            // end::stc_argparam_interface[]
        '''

        shouldFailWithMessages '''
            // tag::stc_argparam_interface_fail[]
            String format(RandomAccess list) {
                'foo'
            }
            format(new LinkedList())                 // fails
            // end::stc_argparam_interface_fail[]
        ''', '#format(java.util.LinkedList). Please check if the declared type is right and if the method exists.'

        assertScript '''
            // tag::stc_argparam_prim[]
            int sum(int x, Integer y) {
                x+y
            }
            assert sum(3, new Integer(4)) == 7
            assert sum(new Integer(3), 4) == 7
            assert sum(new Integer(3), new Integer(4)) == 7
            assert sum(new Integer(3), 4) == 7
            // end::stc_argparam_prim[]
        '''

        assertScript '''
            // tag::stc_arg_closure_coercion[]
            interface SAMType {
                int doSomething()
            }
            int twice(SAMType sam) { 2*sam.doSomething() }
            assert twice { 123 } == 246
            abstract class AbstractSAM {
                int calc() { 2* value() }
                abstract int value()
            }
            int eightTimes(AbstractSAM sam) { 4*sam.calc() }
            assert eightTimes { 123 } == 984
            // end::stc_arg_closure_coercion[]
        '''

    }

    void testNoMatchingMethodError() {
        new GroovyShell().parse '''
            // tag::method_not_type_checked[]
            class MyService {
                void doSomething() {
                    printLine 'Do something'            // <1>
                }
            }
            // end::method_not_type_checked[]
        '''
        shouldFailWithMessages '''
            // tag::method_type_checked[]
            @groovy.transform.TypeChecked
            class MyService {
                void doSomething() {
                    printLine 'Do something'            // <1>
                }
            }
            // end::method_type_checked[]
        ''','Cannot find matching method MyService#printLine(java.lang.String)'
    }

    void testDuckTypingShouldFailWithTypeChecked() {
        shouldFailWithMessages '''
            // tag::ducktyping_failure[]
            class Duck {
                void quack() {              // <1>
                    println 'Quack!'
                }
            }
            class QuackingBird {
                void quack() {              // <2>
                    println 'Quack!'
                }
            }
            @groovy.transform.TypeChecked
            void accept(quacker) {
                quacker.quack()             // <3>
            }
            accept(new Duck())              // <4>
            // end::ducktyping_failure[]
        ''', 'Cannot find matching method java.lang.Object#quack()'
    }
}

