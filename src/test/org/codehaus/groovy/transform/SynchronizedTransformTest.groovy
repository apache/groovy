/*
 * Copyright 2008-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.transform

import groovy.transform.CompileStatic
import groovy.transform.Synchronized

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * @author Paul King
 * @author Hamlet D'Arcy
 */
class SynchronizedTransformTest extends GroovyTestCase {

    def countReadyLatch = new CountDownLatch(1);
    def testReadyLatch = new CountDownLatch(1);
    CountDownLatch countReadyLatchCS = new CountDownLatch(1);
    CountDownLatch testReadyLatchCS = new CountDownLatch(1);

    void testSynchronized() {
        def c = new Count()
        Thread.start {
            c.incDec()
        }
        testReadyLatch.countDown()
        countReadyLatch.await(5, TimeUnit.SECONDS)
        c.incDec()
    }

    void testSynchronizedCS() {
        def c = new CountCS()
        Thread.start {
            c.incDec()
        }
        testReadyLatchCS.countDown()
        countReadyLatchCS.await(5, TimeUnit.SECONDS)
        c.incDec()
    }

    class Count {
        private val = 0

        @Synchronized
        void incDec() {
            assert val == 0; val++; assert val == 1
            countReadyLatch.countDown()
            testReadyLatch.await(5, TimeUnit.SECONDS)
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
            testReadyLatchCS.await(5, TimeUnit.SECONDS)
            assert val == 1; val--; assert val == 0
        }
    }
}