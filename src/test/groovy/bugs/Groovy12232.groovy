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
 * GROOVY-12232: an {@code as} coercion passed directly to an anonymous inner
 * class constructor must not leak a {@code PojoWrapper} into the generated
 * constructor when the super constructor is statically bound.
 */
final class Groovy12232 {

    @Test
    void testCoercionArgumentWithStaticallyBoundSuper() {
        assertScript '''
            class T {
                Set<String> values
                T(Set<String> values) { this.values = values }
            }
            def t = new T(['a'] as Set) {}
            assert t.values == ['a'] as Set
        '''
    }

    @Test
    void testCoercionArgumentAmongOtherArguments() {
        assertScript '''
            class T {
                def a, b
                T(String a, Set<String> b) { this.a = a; this.b = b }
            }
            def t = new T('x', ['a'] as Set) {}
            assert t.a == 'x'
            assert t.b == ['a'] as Set
        '''
    }

    @Test
    void testCoercionArgumentWithAmbiguousSuperStillSelectsByCastType() {
        // GROOVY-9244: with arity-ambiguous super constructors the generated
        // constructor delegates dynamically and the cast type must still drive
        // constructor selection — the wrapper remains required on this path
        assertScript '''
            class T {
                def picked
                T(Set<String> values)  { picked = 'set' }
                T(List<String> values) { picked = 'list' }
            }
            def t1 = new T(['a'] as Set) {}
            assert t1.picked == 'set'
            def t2 = new T(['a'] as List) {}
            assert t2.picked == 'list'
        '''
    }

    @Test
    void testCoercionArgumentWithVargsSuper() {
        assertScript '''
            class T {
                def values
                T(Set<String>... values) { this.values = values }
            }
            def t = new T(['a'] as Set) {}
            assert t.values[0] == ['a'] as Set
        '''
    }

    @Test
    void testCoercionThroughLocalVariableControl() {
        assertScript '''
            class T {
                Set<String> values
                T(Set<String> values) { this.values = values }
            }
            def s = ['a'] as Set
            def t = new T(s) {}
            assert t.values == ['a'] as Set
        '''
    }
}
