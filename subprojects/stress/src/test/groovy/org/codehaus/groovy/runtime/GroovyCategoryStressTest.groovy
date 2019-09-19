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
package org.codehaus.groovy.runtime

import groovy.test.GroovyTestCase
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.TimeUnit

/**
 * Stress test the concurrent use of Groovy Categories.
 *
 */
class GroovyCategoryStressTest extends GroovyTestCase {

    private static final int THREAD_COUNT = 4
    private static final int TEST_LOOPS = 500

    final CyclicBarrier barrier = new CyclicBarrier(THREAD_COUNT + 1)
    final ConcurrentMap<Thread, Throwable> failures = new ConcurrentHashMap<Thread, Throwable>()
    final TestThreadExceptionHandler exceptionHandler = new TestThreadExceptionHandler()

    /**
     * GROOVY-7535
     *
     * Detecting failures is dependent on thread timing so it requires
     * looping many times in order to try to replicate the problem.
     */
    void testWithMultipleThreads() {
        TEST_LOOPS.times {
            if (((it + 1) % 50) == 0) {
                println "Iteration ${it + 1} of ${TEST_LOOPS}"
            }
            performTest()
            if (!failures.isEmpty()) {
                println 'Failures: ' + failures.size()
                Throwable t = failures.values().first()
                throw t
            }
        }
    }

    private void performTest() {
        def test = {
            barrier.await()
            TEST_LOOPS.times {
                use(MyTestCategory) {
                    assert 'foo'.testGroovy7535(' bar') == 'foo bar'
                    assert 'foo'.testGroovy7535(' bar') == 'foo bar'
                    assert 'foo'.testGroovy7535(' bar') == 'foo bar'
                    assert 'foo'.testGroovy7535(' bar') == 'foo bar'
                }
            }
            barrier.await()
        }
        THREAD_COUNT.times {
            Thread th = new Thread(test)
            th.setDaemon(true)
            th.setUncaughtExceptionHandler(exceptionHandler)
            th.start()
        }
        barrier.await() // start all threads
        barrier.await(1L, TimeUnit.MINUTES)
    }

    private static class MyTestCategory {
        static String testGroovy7535(String self, String foo) {
            return self + foo
        }
    }

    private class TestThreadExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override
        void uncaughtException(Thread t, Throwable e) {
            failures.put(t, e)
            barrier.await()
        }
    }

}
