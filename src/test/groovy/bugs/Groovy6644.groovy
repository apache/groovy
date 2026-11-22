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
 * GROOVY-6644: a bare {@code foo()} call inside a closure used from a
 * static method did not invoke a registered static {@code methodMissing},
 * while the explicit {@code this.foo()} form did.
 */
final class Groovy6644 {

    @Test
    void testStaticMethodMissingFromBareCallInClosure() {
        assertScript '''
            class MethodMissing_not_Called {
                static {
                    MethodMissing_not_Called.metaClass.static.methodMissing = { String name, args ->
                        'caught!'
                    }
                }
                static build(Closure c) {
                    c()
                }
                static Object runBare() {
                    def result
                    build {
                        result = foo()
                    }
                    result
                }
            }
            assert MethodMissing_not_Called.runBare() == 'caught!'
        '''
    }

    @Test
    void testStaticMethodMissingFromExplicitThisCallInClosure() {
        assertScript '''
            class MethodMissing_Called {
                static {
                    MethodMissing_Called.metaClass.static.methodMissing = { String name, args ->
                        'caught!'
                    }
                }
                static build(Closure c) {
                    c()
                }
                static Object runExplicit() {
                    def result
                    build {
                        result = this.foo()
                    }
                    result
                }
            }
            assert MethodMissing_Called.runExplicit() == 'caught!'
        '''
    }
}
