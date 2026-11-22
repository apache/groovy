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
package bugs

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * GROOVY-8340: {@code @CompileStatic} must accept a call whose argument is
 * a parameterized type with an array type parameter (for example
 * {@code Optional<Integer[]>}) when the method is {@code Optional<E[]>}.
 */
final class Groovy8340 {

    @Test
    void testCompileStaticCallWithArrayTypeParameter() {
        assertScript '''
            @groovy.transform.CompileStatic
            class ArrayGenericsIssue {
                static Integer callWithArray() {
                    Optional<Integer[]> o = Optional.of([1, 2, 3] as Integer[])
                    return getFirstFromArray(o)
                }
                static Integer callWithList() {
                    Optional<List<Integer>> o = Optional.of([1, 2, 3])
                    return getFirstFromList(o)
                }
                static <E> E getFirstFromArray(Optional<E[]> optional) {
                    return optional.get()[0]
                }
                static <E> E getFirstFromList(Optional<List<E>> optional) {
                    return optional.get()[0]
                }
            }
            assert ArrayGenericsIssue.callWithArray() == 1
            assert ArrayGenericsIssue.callWithList() == 1
        '''
    }
}
