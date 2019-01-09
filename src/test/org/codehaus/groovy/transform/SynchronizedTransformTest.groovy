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
package org.codehaus.groovy.transform

import groovy.transform.CompileStatic
import groovy.transform.Synchronized
import org.codehaus.groovy.control.CompilationFailedException

import java.util.concurrent.CountDownLatch

import static java.util.concurrent.TimeUnit.SECONDS

class SynchronizedTransformTest extends GroovyTestCase {

    def countReadyLatch = new CountDownLatch(1)
    def testReadyLatch = new CountDownLatch(1)
    CountDownLatch countReadyLatchCS = new CountDownLatch(1)
    CountDownLatch testReadyLatchCS = new CountDownLatch(1)

    void testSynchronized() {
        def c = new Count()
        Thread.start {
            c.incDec()
        }
        testReadyLatch.countDown()
        countReadyLatch.await(5, SECONDS)
        c.incDec()
    }

    void testSynchronizedCustom() {
        def c = new CountCustom()
        Thread.start {
            c.incDec()
        }
        testReadyLatch.countDown()
        countReadyLatch.await(5, SECONDS)
        c.incDec()
    }

    void testSynchronizedCS() {
        def c = new CountCS()
        Thread.start {
            c.incDec()
        }
        testReadyLatchCS.countDown()
        countReadyLatchCS.await(5, SECONDS)
        c.incDec()
    }

    void testSynchronizedAbstractShouldNotCompile() {
        def msg = shouldFail CompilationFailedException, '''
            class Foo {
                @groovy.transform.Synchronized
                abstract void bar()
            }
        '''
        assert msg.contains("annotation not allowed on abstract method 'bar'")
    }

    void testSynchronizedInstanceLockWithStaticMethodShouldNotCompile() {
        def msg = shouldFail CompilationFailedException, '''
            class Foo {
                private mylock = new Object[0]
                @groovy.transform.Synchronized('mylock')
                static void bar() {}
            }
        '''
        assert msg.contains("lock field with name 'mylock' must be static for static method 'bar'")
    }

    class Count {
        private val = 0

        @Synchronized
        void incDec() {
            assert val == 0; val++; assert val == 1
            countReadyLatch.countDown()
            testReadyLatch.await(5, SECONDS)
            assert val == 1; val--; assert val == 0
        }
    }

    class CountCustom {
        private val = 0
        private mylock = new Object[0]

        @Synchronized('mylock')
        void incDec() {
            assert val == 0; val++; assert val == 1
            countReadyLatch.countDown()
            testReadyLatch.await(5, SECONDS)
            assert val == 1; val--; assert val == 0
        }
    }

    @CompileStatic
    class CountCS {
        private int val = 0

        @Synchronized
        void incDec() {
            assert val == 0; val++; assert val == 1
            countReadyLatchCS.countDown()
            testReadyLatchCS.await(5, SECONDS)
            assert val == 1; val--; assert val == 0
        }
    }
}