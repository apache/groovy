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

import groovy.mock.interceptor.StubFor
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.junit.Test

import java.util.concurrent.TimeoutException

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * Tests for the {@link TimedInterrupt} AST transform.
 */
final class TimedInterruptTest {

    private final GroovyShell shell = GroovyShell.withConfig {
        imports {
            star 'groovy.transform'
            staticStar TimedInterruptTest.name
            normal 'groovy.mock.interceptor.StubFor'
        }
    }

    @Test
    void testClassMethodIsVisited() {
        assertScript shell, '''
            @TimedInterrupt(applyToAllClasses=false, value=1L)
            class C {
                def m() { }
            }
            assertPassesNormalFailsSlowExecution(C, methodName: 'm')
        '''
    }

    @Test
    void testClassMethodIsVisitedAndCustomExceptionThrown() {
        assertScript shell, '''
            @TimedInterrupt(applyToAllClasses=false, thrown=CustomException, value=1L)
            class C {
                def m() { }
            }
            assertPassesNormalFailsSlowExecution(C, methodName: 'm', exception: CustomException)
        '''
    }

    @Test
    void testScriptMethodIsVisited() {
        assertScript shell, '''
            @TimedInterrupt(applyToAllClasses=false, value=1L) def m() { }
            assertPassesNormalFailsSlowExecution(this.class, methodName: 'm')
        '''
    }

    @Test
    void testStaticMethodIsNotVisited() {
        assertScript shell, '''
            @TimedInterrupt(applyToAllClasses=false, value=1L)
            class C {
                static myMethod() { }
            }
            assertPassesSlowExecution(C)
        '''
    }

    @Test
    void testClosureFieldIsVisited() {
        assertScript shell, '''
            @TimedInterrupt(applyToAllClasses=false, value=1L)
            class C {
                def m = { -> }
            }
            assertPassesNormalFailsSlowExecution(C, methodName: 'm')
        '''
    }

    @Test
    void testClosureInScriptIsVisited_CheckOnMethodStartIsFalse() {
        def script = shell.parse '''
            @TimedInterrupt(applyToAllClasses=false, applyToAllMembers=false, checkOnMethodStart=false, value=1L)
            def m = { -> }
            m()
        '''
        assertPassesNormalFailsSlowExecution(script.class, methodName: 'run')
    }

    @Test
    void testWhileInScriptIsVisited_CheckOnMethodStartIsFalse() {
        def script = shell.parse '''
            @TimedInterrupt(applyToAllClasses=false, checkOnMethodStart=false, value=1L)
            int x = 1
            while (x < 2) { x = 2 }
        '''
        assertPassesNormalFailsSlowExecution(script.class, methodName: 'run')
    }

    @Test
    void testForInScriptIsVisited_CheckOnMethodStartIsFalse() {
        def script = shell.parse '''
            @TimedInterrupt(applyToAllClasses=false, checkOnMethodStart=false, value=1L)
            def x = [1]
            for (def o : x) { o++ }
        '''
        assertPassesNormalFailsSlowExecution(script.class, methodName: 'run')
    }

    @Test
    void testStaticClosureFieldNotVisited() {
        assertScript shell, '''
            @TimedInterrupt(applyToAllClasses=false, value=1L)
            class C {
                static myMethod = { -> }
            }
            assertPassesSlowExecution(C)
        '''
    }

    @Test
    void testAnnotationParameters() {
        assertScript shell, '''
            import static java.util.concurrent.TimeUnit.*

            @TimedInterrupt(applyToAllClasses=false, value=18000000L, unit=MILLISECONDS)
            def myMethod() { }

            assertPassesNormalFailsSlowExecution(this.class, expireTime: 18000000000666L, units: '18000000', timeUnitName: 'milliseconds') // 5 hours in future
        '''
    }

    @Test // TODO: not sure all these tests are pulling their weight - testing Groovy annotation type handing not subject
    void testErrorHandling() {
        shouldFail shell, MultipleCompilationErrorsException, '''
            @TimedInterrupt(value = "5")
            def myMethod() { }
        '''

        shouldFail shell, MultipleCompilationErrorsException, '''
            @TimedInterrupt(value = foo())
            def myMethod() { }
        '''

        shouldFail shell, MultipleCompilationErrorsException, '''
            @TimedInterrupt(value = 5L, applyToAllClasses = 5)
            def myMethod() { }
        '''

        shouldFail shell, MultipleCompilationErrorsException, '''
            @TimedInterrupt(value = 5L, applyToAllClasses = foo())
            def myMethod() { }
        '''

        shouldFail shell, MultipleCompilationErrorsException, '''
            @TimedInterrupt(value = 5L, checkOnMethodStart = 5)
            def myMethod() { }
        '''

        shouldFail shell, MultipleCompilationErrorsException, '''
            @TimedInterrupt(value = 5L, checkOnMethodStart = foo())
            def myMethod() { }
        '''

        shouldFail shell, MultipleCompilationErrorsException, '''
            @TimedInterrupt(value = 5L, unit = 5)
            def myMethod() { }
        '''

        shouldFail shell, MultipleCompilationErrorsException, '''
            @TimedInterrupt(value = 5L, unit = foo())
            def myMethod() { }
        '''
    }

    @Test
    void testTimedInterruptOnAbstractClass() {
        def script = '''
            @TimedInterrupt(value = 1L)
            abstract class A {
                abstract void m()
            }
            class C extends A {
                void m() {
                    99.times {
                        // do something
                    }
                }
            }
            new C()
        '''
        def system = new StubFor(System)
        // start time initialized to the Long of the Beast
        system.demand.nanoTime(4) { 666L } // 2 times to cover full instantiation
        system.demand.nanoTime() { 1000000667L }
        system.use {
            def instance = shell.evaluate(script)
            // may get false positives if multiple annotations with the same expireTime defined in test script
            assert instance.dump().matches('.*timedInterrupt\\S+\\$expireTime=1000000666 .*')

            shouldFail(TimeoutException) {
                instance.m()
            }
        }
    }

    //--------------------------------------------------------------------------

    static void assertPassesNormalFailsSlowExecution(Map<String,?> args, Class type) {
        def system = new StubFor(System)
        // start time initialized to ...
        system.demand.nanoTime() { 666L }
        def instance
        system.use {
            instance = type.newInstance()
        }
        long expireTime = args.getOrDefault('expireTime', 1000000666L)
        String methodName = args.getOrDefault('methodName', 'myMethod')
        // may get false positives if multiple annotations with the same expireTime defined
        assert instance.dump().matches('.*timedInterrupt\\S+\\$expireTime=' + expireTime + ' .*')

        system.demand.nanoTime() { expireTime }
        system.use {
            instance.(methodName)()
        }

        // one nanosecond too slow
        system.demand.nanoTime() { expireTime + 1 }
        system.use {
            def err = shouldFail(args.getOrDefault('exception', java.util.concurrent.TimeoutException)) {
                instance.(methodName)()
            }
            assert err.message.contains('Execution timed out after ' + args.getOrDefault('units', '1') + ' ' + args.getOrDefault('timeUnitName', 'seconds'))
        }
    }

    static void assertPassesSlowExecution(Class c) {
        def system = new StubFor(System)
        // start time initialized to the Long of the Beast
        system.demand.nanoTime() { 666L }
        def instance
        system.use {
            instance = c.newInstance()
        }
        system.demand.nanoTime() { 1000000667L }
        system.use {
            instance.myMethod()
        }
    }
}
