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

class MethodsTests extends GroovyTestCase {

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

}
