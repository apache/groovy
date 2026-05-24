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
package org.codehaus.groovy.classgen.asm

import org.junit.jupiter.api.Test

final class IfElseBytecodeReachabilityTest extends AbstractBytecodeTestCase {

    @Test
    void testDynamicIfElseWithReturnDoesNotEmitSyntheticExitJump() {
        assertNoUnreachableBytecodeAndResult('''
            boolean flag = [true].first()
            if (flag) {
                return 1
            } else {
                return 2
            }
        ''', 1)
    }

    @Test
    void testStaticIfElseWithReturnDoesNotEmitSyntheticExitJump() {
        assertNoUnreachableBytecodeAndResult(method: 'runIt', '''
            import groovy.transform.CompileStatic

            @CompileStatic
            int runIt() {
                boolean flag = Boolean.TRUE
                if (flag) {
                    return 1
                } else {
                    return 2
                }
            }

            runIt()
        ''', 1)
    }

    @Test
    void testDynamicIfElseWithThrowDoesNotEmitSyntheticExitJump() {
        assertNoUnreachableBytecodeAndResult(method: 'pick', '''
            int pick(final boolean flag) {
                if (flag) {
                    throw new IllegalStateException('boom')
                } else {
                    return 2
                }
            }

            pick(false)
        ''', 2)
    }

    @Test
    void testNestedAbruptSyntaxCombinationDoesNotEmitSyntheticExitJump() {
        assertNoUnreachableBytecodeAndResult(method: 'runIt', '''
            int runIt() {
                int mode = 1
                if (mode == 1) {
                    switch (mode) {
                        case 1:
                            return 10
                        default:
                            return 20
                    }
                } else {
                    return 30
                }
            }

            runIt()
        ''', 10)
    }

    private void assertNoUnreachableBytecodeAndResult(final String source, final Object expected) {
        assertNoUnreachableBytecodeAndResult([:], source, expected)
    }

    private void assertNoUnreachableBytecodeAndResult(final Map options, final String source, final Object expected) {
        def unreachable = compileAndFindUnreachableInstructions(options, source)
        assert unreachable.isEmpty(): "Unexpected unreachable instructions: ${unreachable}\n${sequence}"
        assert new GroovyShell().evaluate(source) == expected
    }
}
