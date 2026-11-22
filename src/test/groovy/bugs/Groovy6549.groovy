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
 * GROOVY-6549: the right-hand side of {@code e[index()] = value()} used
 * to be evaluated before the index expression, unlike Java (which prints
 * "index" then "value").
 */
final class Groovy6549 {

    @Test
    void testSubscriptAssignmentEvaluatesIndexBeforeValue() {
        assertScript '''
            def order = []
            def e = []
            def f = { -> order << 'LHS'; 1 }
            def g = { -> order << 'RHS'; 4 }
            e[f()] = g()
            assert order == ['LHS', 'RHS']
            assert e == [null, 4]
        '''
    }

    @Test
    void testArrayAssignmentMatchesJavaEvaluationOrder() {
        assertScript '''
            def order = []
            int[] array = new int[1]
            def index = { order << 'index'; 0 }
            def value = { order << 'value'; 1 }
            array[index()] = value()
            assert order == ['index', 'value']
            assert array == [1] as int[]
        '''
    }
}
