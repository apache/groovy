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
package objectorientation

import groovy.test.GroovyTestCase

class MethodsTest extends GroovyTestCase {

    void testMethodDefinition() {
        assertScript '''
            // tag::method_definition[]
            def someMethod() { 'method called' }                           //<1>
            String anotherMethod() { 'another method called' }             //<2>
            def thirdMethod(param1) { "$param1 passed" }                   //<3>
            static String fourthMethod(String param1) { "$param1 passed" } //<4>
            // end::method_definition[]
        '''
    }

    void testNamedArguments() {
        assertScript '''
            // tag::named_arguments[]
            def foo(Map args) { "${args.name}: ${args.age}" }
            foo(name: 'Marie', age: 1)
            // end::named_arguments[]
        '''
    }

    void testNamedArgumentsAlongWithOtherArguments() {
        assertScript '''
            // tag::named_arguments_with_additional_arguments[]
            def foo(Map args, Integer number) { "${args.name}: ${args.age}, and the number is ${number}" }
            foo(name: 'Marie', age: 1, 23)  //<1>
            foo(23, name: 'Marie', age: 1)  //<2>
            // end::named_arguments_with_additional_arguments[]
        '''
    }

    void testFailedNamedArgumentsAlongWithOtherArguments() {
        shouldFail '''
            // tag::failed_named_arguments_with_additional_arguments[]
            def foo(Integer number, Map args) { "${args.name}: ${args.age}, and the number is ${number}" }
            foo(name: 'Marie', age: 1, 23)  //<1>
            // end::failed_named_arguments_with_additional_arguments[]
        '''
    }

    void testExplicitNamedArgumentsAlongWithOtherArguments() {
        assertScript '''
            // tag::explicit_named_arguments_with_additional_arguments[]
            def foo(Integer number, Map args) { "${args.name}: ${args.age}, and the number is ${number}" }
            foo(23, [name: 'Marie', age: 1])  //<1>
            // end::explicit_named_arguments_with_additional_arguments[]
        '''
    }

    void testDefaultArguments() {
        assertScript '''
            // tag::default_arguments[]
            def foo(String par1, Integer par2 = 1) { [name: par1, age: par2] }
            assert foo('Marie').age == 1
            // end::default_arguments[]
        '''
    }

    void testVarargs() {
        assertScript '''
            // tag::varargs_example[]
            def foo(Object... args) { args.length }
            assert foo() == 0
            assert foo(1) == 1
            assert foo(1, 2) == 2
            // end::varargs_example[]
        '''
    }

    void testVarargsArrayNotation() {
        assertScript '''
            // tag::varargs_array_notation[]
            def foo(Object[] args) { args.length }
            assert foo() == 0
            assert foo(1) == 1
            assert foo(1, 2) == 2
            // end::varargs_array_notation[]
        '''
    }

    void testVarargsNullParameter() {
        assertScript '''
            // tag::varargs_null_parameter[]
            def foo(Object... args) { args }
            assert foo(null) == null
            // end::varargs_null_parameter[]
        '''
    }

    void testVarargsArrayParameter() {
        assertScript '''
            // tag::varargs_array_parameter[]
            def foo(Object... args) { args }
            Integer[] ints = [1, 2]
            assert foo(ints) == [1, 2]
            // end::varargs_array_parameter[]
        '''
    }

    void testVarargsMethodOverloading() {
        assertScript '''
            // tag::varargs_method_overloading[]
            def foo(Object... args) { 1 }
            def foo(Object x) { 2 }
            assert foo() == 1
            assert foo(1) == 2
            assert foo(1, 2) == 1
            // end::varargs_method_overloading[]
        '''
    }

    void testIdiomaticMethodDeclaration() {
        assertScript '''
            import static groovy.test.GroovyAssert.shouldFail
            // tag::idiomatic_method_declaration[]
            def badRead() {
                new File('doesNotExist.txt').text
            }

            shouldFail(FileNotFoundException) {
                badRead()
            }
            // end::idiomatic_method_declaration[]
        '''
    }

    void testMethodDeclarationWithCheckedException() {
        assertScript '''
            import static groovy.test.GroovyAssert.shouldFail
            // tag::checked_method_declaration[]
            def badRead() throws FileNotFoundException {
                new File('doesNotExist.txt').text
            }

            shouldFail(FileNotFoundException) {
                badRead()
            }
            // end::checked_method_declaration[]
        '''
    }

    void testMultiMethods() {
        assertScript '''
            // tag::multi_methods[]
            def method(Object o1, Object o2) { 'o/o' }
            def method(Integer i, String  s) { 'i/s' }
            def method(String  s, Integer i) { 's/i' }
            // end::multi_methods[]

            // tag::call_single_method[]
            assert method('foo', 42) == 's/i'
            // end::call_single_method[]

            // tag::call_multi_methods[]
            List<List<Object>> pairs = [['foo', 1], [2, 'bar'], [3, 4]]
            assert pairs.collect { a, b -> method(a, b) } == ['s/i', 'i/s', 'o/o']
            // end::call_multi_methods[]
        '''

        assertScript '''
            import static groovy.test.GroovyAssert.shouldFail
            // tag::multi_method_ambiguous[]
            def method(Date d, Object o) { 'd/o' }
            def method(Object o, String s) { 'o/s' }

            def ex = shouldFail {
                println method(new Date(), 'baz')
            }
            assert ex.message.contains('Ambiguous method overloading')
            // end::multi_method_ambiguous[]
            // tag::multi_method_ambiguous_cast[]
            assert method(new Date(), (Object)'baz') == 'd/o'
            assert method((Object)new Date(), 'baz') == 'o/s'
            // end::multi_method_ambiguous_cast[]
        '''

        assertScript '''
            // tag::multi_method_distance_interfaces[]
            interface I1 {}
            interface I2 extends I1 {}
            interface I3 {}
            class Clazz implements I3, I2 {}

            def method(I1 i1) { 'I1' }
            def method(I3 i3) { 'I3' }
            // end::multi_method_distance_interfaces[]

            // tag::multi_method_distance_interfaces_usage[]
            assert method(new Clazz()) == 'I3'
            // end::multi_method_distance_interfaces_usage[]
        '''

        assertScript '''
            // tag::non_varargs_over_vararg[]
            def method(String s, Object... vargs) { 'vararg' }
            def method(String s) { 'non-vararg' }

            assert method('foo') == 'non-vararg'
            // end::non_varargs_over_vararg[]
        '''

        assertScript '''
            // tag::minimal_varargs[]
            def method(String s, Object... vargs) { 'two vargs' }
            def method(String s, Integer i, Object... vargs) { 'one varg' }

            assert method('foo', 35, new Date()) == 'one varg'
            // end::minimal_varargs[]
        '''

        assertScript '''
            // tag::object_array_over_object[]
            def method(Object[] arg) { 'array' }
            def method(Object arg) { 'object' }

            assert method([] as Object[]) == 'array'
            // end::object_array_over_object[]
        '''

        assertScript '''
            // tag::multi_method_distance_interface_over_super[]
            interface I {}
            class Base {}
            class Child extends Base implements I {}

            def method(Base b) { 'superclass' }
            def method(I i) { 'interface' }

            assert method(new Child()) == 'interface'
            // end::multi_method_distance_interface_over_super[]
        '''
    }

}
