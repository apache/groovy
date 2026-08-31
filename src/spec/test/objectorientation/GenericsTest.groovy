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

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

final class GenericsTest {

    @Test
    void testGenericArrayCreationWorkaround() {
        assertScript '''
            // tag::generic_array_workaround[]
            class Stack<T> {
                private T[] items
                private int size = 0
                Stack(int capacity) {
                    items = (T[]) new Object[capacity]  // reifiable creation + unchecked cast
                }
                void push(T t) { items[size++] = t }
                T pop() { items[--size] }
            }
            def s = new Stack<String>(4)
            s.push('a')
            s.push('b')
            assert s.pop() == 'b'
            // end::generic_array_workaround[]
        '''
    }

    @Test
    void testGenericThrowableWorkaround() {
        assertScript '''
            // tag::generic_throwable_workaround[]
            class ValidationException extends RuntimeException {
                private final Object payload
                ValidationException(String msg, Object payload) {
                    super(msg)
                    this.payload = payload
                }
                def <T> T payload() { (T) payload }
            }
            try {
                throw new ValidationException('bad input', [field: 'name'])
            } catch (ValidationException e) {
                Map m = e.payload()
                assert m.field == 'name'
            }
            // end::generic_throwable_workaround[]
        '''
    }
}
