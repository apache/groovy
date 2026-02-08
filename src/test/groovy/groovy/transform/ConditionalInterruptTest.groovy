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
package groovy.transform

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * Tests for the {@link ConditionalInterrupt} AST transform.
 */
final class ConditionalInterruptTest {

    private final GroovyShell shell = GroovyShell.withConfig {
        imports {
            star 'groovy.transform'
            staticMember 'groovy.test.GroovyAssert', 'shouldFail'
        }
    }

    @Test
    void testMethodIsVisited_AndExceptionMessage() {
        assertScript shell, '''
            @ConditionalInterrupt(applyToAllClasses=false, value={ visited = true })
            class C {
                protected boolean visited
                def m() { }
            }

            def obj = new C()
            def err = shouldFail(InterruptedException) {
                obj.m()
            }
            assert obj.visited
            assert err.message == 'Execution interrupted. The following condition failed: { visited = true }'
        '''
    }

    @Test
    void testMethodIsVisitedCompileStatic() {
        assertScript shell, '''
            @CompileStatic
            @ConditionalInterrupt(applyToAllClasses=false, value={ visited = true })
            class C {
                protected boolean visited
                def m() { }
            }

            def obj = new C()
            def err = shouldFail(InterruptedException) {
                obj.m()
            }
            assert obj.visited
            assert err.message == 'Execution interrupted. The following condition failed: { visited = true }'
        '''
    }

    @Test
    void testMethodIsVisited_AndCustomExceptionMessage() {
        assertScript shell, '''
            @ConditionalInterrupt(applyToAllClasses=false, thrown=CustomException, value={ visited = true })
            class C {
                protected boolean visited
                def m() { }
            }

            def obj = new C()
            def err = shouldFail(CustomException) {
                obj.m()
            }
            assert obj.visited
            assert err.message == 'Execution interrupted. The following condition failed: { visited = true }'
        '''
    }

    @Test
    void testStaticMethodIsNotVisited() {
        assertScript shell, '''
            @ConditionalInterrupt(applyToAllClasses=false, value={ visited = true })
            class C {
                protected boolean visited
                static m() { }
            }

            def obj = new C()
            obj.m()

            assert !obj.visited
        '''
    }

    @Test
    void testClosureFieldIsVisited() {
        assertScript shell, '''
            @ConditionalInterrupt(applyToAllClasses=false, value={ visited = true })
            class C {
                protected boolean visited
                def m = { -> }
            }

            def obj = new C()
            shouldFail(InterruptedException) {
                obj.m()
            }
            assert obj.visited
        '''
    }

    @Test
    void testWhileLoopVisited() {
        assertScript shell, '''
            @ConditionalInterrupt(applyToAllClasses=false, value={ count > 5 })
            class C {
                protected int count
                def m = { ->
                    while (count < 10) {
                        count += 1
                    }
                }
            }

            def obj = new C()
            shouldFail(InterruptedException) {
                obj.m()
            }
            assert obj.count == 6
        '''
    }

    @Test
    void testForLoopVisited() {
        assertScript shell, '''
            @ConditionalInterrupt(applyToAllClasses=false, value={ count > 5 })
            class C {
                protected int count
                def m = {
                    for (int i = 0; i < 10; i += 1) {
                        count += 1
                    }
                }
            }

            def obj = new C()
            shouldFail(InterruptedException) {
                obj.m()
            }
            assert obj.count == 6
        '''
    }

    @Test
    void testStaticClosureFieldNotVisited() {
        assertScript shell, '''
            @ConditionalInterrupt(applyToAllClasses=false, value={ visited = true })
            class C {
                protected boolean visited
                static m = { -> }
            }

            def obj = new C()
            obj.m()

            assert !obj.visited
        '''
    }

    @Test
    void testSharedContext() {
        assertScript shell, '''
            class Helper {
                static int i
                static def shouldInterrupt() { ++i > 1 }
            }

            @ConditionalInterrupt(applyToAllClasses=false, value={ Helper.shouldInterrupt() })
            class C {
                def m = { -> }
            }

            @ConditionalInterrupt(applyToAllClasses=false, value={ Helper.shouldInterrupt() })
            class D {
                def m() {
                    new C().m()
                }
            }

            shouldFail(InterruptedException) {
                new D().m()
            }
        '''
    }
}
