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
package org.apache.groovy.contracts.tests.doc

import org.apache.groovy.contracts.tests.basic.BaseTestClass
import org.junit.Test

import static org.junit.Assert.assertTrue

class DocumentationExampleTests extends BaseTestClass {

    def example_person = '''
    @Contracted
    package tests
    
    import groovy.contracts.*
    
    @Invariant({ firstName != null && lastName != null })
    class Person {
        String firstName
        String lastName
    
        @Requires ({ delimiter in ['.', ',', ' '] })
        @Ensures({ result -> result == (firstName + delimiter + lastName) })
        def String getName(String delimiter) {
            return delimiter
        }
    }
    '''

    def example_eiffel_stack = '''
    @Contracted
    package tests

    import groovy.contracts.*

    @Invariant({ elements != null })
    class EiffelStack {

        private List elements

        @Ensures({ is_empty() })
        public EiffelStack()  {
            elements = []
        }

        @Requires({ preElements?.size() > 0 })
        @Ensures({ !is_empty() })
        public EiffelStack(List preElements)  {
            elements = preElements
        }

        def boolean is_empty()  {
            elements.isEmpty()
        }

        @Requires({ !is_empty() })
        def last_item()  {
            elements.last()
        }

        def count() {
            elements.size()
        }

        @Ensures({ result == true ? count() > 0 : count() >= 0  })
        def boolean has(def item)  {
            elements.contains(item)
        }

        @Ensures({ last_item() == item })
        def put(def item)  {
           elements.push(item)
        }

        @Requires({ !is_empty() })
        @Ensures({ last_item() == item })
        def replace(def item)  {
            remove()
            elements.push(item)
        }

        @Requires({ !is_empty() })
        @Ensures({ result != null })
        def remove()  {
            elements.pop()
        }
    }    
    '''


    @Test
    void test_stack_creation() {
        create_instance_of(example_eiffel_stack)
    }

    @Test
    void test_stack_creation_with_list() {
        create_instance_of(example_eiffel_stack, [[1, 2, 3, 4]])
    }

    @Test
    void test_stack_put() {
        def stack = create_instance_of(example_eiffel_stack)
        stack.put("hello world")

        assertTrue stack.last_item() == 'hello world'
    }

    @Test
    void test_stack_replace() {
        def stack = create_instance_of(example_eiffel_stack)
        stack.put("hello world")
        stack.replace("hallo welt")

        assertTrue stack.last_item() == 'hallo welt'
        assertTrue stack.count() == 1
    }

    @Test
    void test_stack_remove() {
        def stack = create_instance_of(example_eiffel_stack)
        stack.put("hello world")
        stack.remove()

        assertTrue stack.count() == 0
    }

    @Test
    void test_person_creation() {
        shouldFail AssertionError, {
            create_instance_of(example_person)
        }
    }
}