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
package groovy.bugs

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * Test changes related to https://issues.apache.org/jira/browse/GROOVY-7867
 */
final class Groovy7867 {

    @Test
    void testIntendedBehaviour() {
        // coerce calling constructor with two integers by throwing an Exception
        assertScript '''
            class SingletonList extends Vector {
                SingletonList(Collection c) {
                    super(c)
                    if (c.size() != 1) throw new IllegalStateException()
                }
                SingletonList(int capacity, int increment) {
                    super(capacity, increment)
                }
            }
            
            def myList = [10, 5] as SingletonList
            assert myList.size() == 0
            assert myList.capacity() == 10
        '''
    }

    @Test
    void testRetrievingSuppressedException() {
        // for easier failure analysis it can be crucial to get hold of the original exception
        assertScript '''
            import org.codehaus.groovy.runtime.typehandling.GroovyCastException
            
            class SingletonList extends Vector {
                SingletonList(Collection c) {
                    super(c)
                    if (c.size() != 1) {
                        throw new IllegalArgumentException("expected SingletonList to be initialized with exactly one element")
                    }
                }
            }
            
            // not exactly one argument --> (user defined) exception
            boolean caught = false
            try {
                def myList = [10, 5] as SingletonList
            } catch (ex) {
                caught = true
                assert ex instanceof GroovyCastException
                assert ex.suppressed.length == 1
                assert ex.suppressed[0] instanceof IllegalArgumentException
                assert ex.suppressed[0].message == "expected SingletonList to be initialized with exactly one element"
            }
            assert caught == true
            
            // exactly one argument --> OK
            caught = false
            try {
                def myList = [42] as SingletonList
                assert myList.size() == 1
                assert myList[0] == 42
            } catch (ex) {
                caught = true
            }
            assert caught == false
        '''
    }

    @Test
    void testFallbackToNoArgsConstructor() {
        // the no-arg constructor is the second fallback which comes before calling the constructor with two integers
        assertScript '''
            class SingletonList extends Vector {
                SingletonList() {
                    super()
                }
                SingletonList(Collection c) {
                    super(c)
                    if (c.size() != 1) throw new IllegalStateException()
                }
                SingletonList(int capacity, int increment) {
                    super(capacity, increment)
                }
            }
            
            def myList = [10, 5] as SingletonList
            assert myList.size() == 2
            assert myList[0] == 10
            assert myList[1] == 5
        '''
    }
}
