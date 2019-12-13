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

import groovy.transform.CompileStatic
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

@CompileStatic
final class Groovy7985 {

    @Test
    void testGenericsCompatibility() {
        assertScript '''
            @groovy.transform.AutoFinal
            @groovy.transform.CompileStatic
            class Pair<L, R> implements Serializable {
                public final L left
                public final R right

                private Pair(L left, R right) {
                    this.left = left
                    this.right = right
                }

                static <L, R> Pair<L, R> of(L left, R right) {
                    return new Pair<>(left, right)
                }
            }

            @groovy.transform.CompileStatic
            Pair<Pair<String, Integer>, Pair<String, Integer>> doSmething() {
                def one = (Pair<String, Integer>) Pair.of('a', 1)
                def two = (Pair<String, Integer>) Pair.of('b', 2)
                return Pair.of(one, two)
            }

            assert doSmething().left.left == 'a'
            assert doSmething().left.right == 1
            assert doSmething().right.left == 'b'
            assert doSmething().right.right == 2
        '''
    }
}
