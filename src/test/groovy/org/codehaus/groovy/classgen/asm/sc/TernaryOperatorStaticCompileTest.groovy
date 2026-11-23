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
package org.codehaus.groovy.classgen.asm.sc

import groovy.transform.stc.TernaryOperatorSTCTest
import org.junit.jupiter.api.Test

/**
 * Unit tests for static compilation : ternary operator.
 */
final class TernaryOperatorStaticCompileTest extends TernaryOperatorSTCTest implements StaticCompilationTestSupport {

    // GROOVY-12822
    @Test
    void testFunctionalInterfaceTargetOfLambdaOrMethodReferenceBranches() {
        for (flag in [true, false]) {
            assertScript """import java.util.function.*
                class Holder<T> { T value }

                Supplier<String> local(boolean flag) {
                    Supplier<String> s = flag ? () -> 'a' : () -> 'b'
                    s
                }
                Function<String, String> returned(boolean flag) {
                    flag ? String::toUpperCase : String::trim
                }
                Function<String, String> mixed(boolean flag) {
                    flag ? (String s) -> s + '!' : { String s -> s + '?' }
                }
                List<Function<String, String>> slots(boolean flag) {
                    Map<String, Function<String, String>> map = [:]
                    map['k'] = flag ? (String s) -> s + '!' : String::trim
                    def holder = new Holder<Function<String, String>>()
                    holder.value = flag ? (String s) -> s + '!' : (String s) -> s
                    holder.@value = flag ? holder.value : (String s) -> s
                    Function<String, String> local = flag ? (String s) -> s + '!' : String::trim
                    List<Function<String, String>> list = [null]
                    list[0] = flag ? (String s) -> s + '!' : (String s) -> s
                    Function<String, String> none = null
                    Function<String, String> elvis = none ?: (String s) -> s + '#'
                    [map['k'], holder.value, list[0], elvis, local]
                }

                assert local($flag).get() == ($flag ? 'a' : 'b')
                assert returned($flag).apply(' a ') == ($flag ? ' A ' : 'a')
                assert mixed($flag).apply('a') == ($flag ? 'a!' : 'a?')
                assert slots($flag)*.apply('a') == ($flag ? ['a!', 'a!', 'a!', 'a#', 'a!'] : ['a', 'a', 'a', 'a#', 'a'])
            """
        }
    }

    // GROOVY-12822
    @Test
    void testFunctionalInterfaceTargetOfABranchAssignedToASubscriptOrProperty() {
        assertScript '''import java.util.function.Function
            class Holder<T> { T value }

            List<Function<String, String>> slots(Function<String, String> f, boolean flag) {
                Map<String, Function<String, String>> map = [:]
                map['ternary'] = flag ? f : (String s) -> s.trim()
                map['elvis'] = f ?: (String s) -> s.trim()
                def holder = new Holder<Function<String, String>>()
                holder.value = flag ? f : (String s) -> s.trim()
                [map['ternary'], map['elvis'], holder.value]
            }

            Function<String, String> f = (String s) -> s + '!'
            assert slots(f, true)*.apply(' a ') == [' a !', ' a !', ' a !']
            assert slots(f, false)*.apply(' a ') == ['a', ' a !', 'a']
        '''
    }
}
