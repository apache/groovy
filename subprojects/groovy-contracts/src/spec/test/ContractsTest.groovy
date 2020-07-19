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
import groovy.test.GroovyTestCase

class ContractsTest extends GroovyTestCase {

    void testBasicExample() {
        assertScript '''
        // tag::basic_example[]
        package acme

        import groovy.contracts.*

        @Invariant({ speed() >= 0 })
        class Rocket {
            int speed = 0
            boolean started = true

            @Requires({ isStarted() })
            @Ensures({ old.speed < speed })
            def accelerate(inc) { speed += inc }

            def isStarted() { started }

            def speed() { speed }
        }

        def r = new Rocket()
        r.accelerate(5)
        // end::basic_example[]
        '''
    }

    void testStackExample() {
        assertScript '''
        /*
        // tag::stack_prelim[]
        @Grab(group='org.apache.groovy', module='groovy-contracts', version='4.0.0')
        // end::stack_prelim[]
        */
        // tag::stack_example[]
        import groovy.contracts.*

        @Invariant({ elements != null })
        class Stack<T> {

            List<T> elements

            @Ensures({ is_empty() })
            def Stack()  {
                elements = []
            }

            @Requires({ preElements?.size() > 0 })
            @Ensures({ !is_empty() })
            def Stack(List<T> preElements)  {
                elements = preElements
            }

            boolean is_empty()  {
                elements.isEmpty()
            }

            @Requires({ !is_empty() })
            T last_item()  {
                elements.get(count() - 1)
            }

            def count() {
                elements.size()
            }

            @Ensures({ result == true ? count() > 0 : count() >= 0  })
            boolean has(T item)  {
                elements.contains(item)
            }

            @Ensures({ last_item() == item })
            def push(T item)  {
               elements.add(item)
            }

            @Requires({ !is_empty() })
            @Ensures({ last_item() == item })
            def replace(T item)  {
                remove()
                elements.add(item)
            }

            @Requires({ !is_empty() })
            @Ensures({ result != null })
            T remove()  {
                elements.remove(count() - 1)
            }

            String toString() { elements.toString() }
        }

        def stack = new Stack<Integer>()
        // end::stack_example[]
        '''
    }
}
