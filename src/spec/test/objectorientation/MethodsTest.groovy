/*
 * Copyright 2003-2015 the original author or authors.
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
package objectorientation

class MethodsTests extends GroovyTestCase {

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
