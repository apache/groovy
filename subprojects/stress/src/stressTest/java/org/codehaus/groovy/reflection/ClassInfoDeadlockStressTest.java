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
package org.codehaus.groovy.reflection;

import groovy.lang.GroovyClassLoader;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.groovy.stress.util.GCUtils;
import org.apache.groovy.stress.util.ThreadUtils;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for deadlocks in the ClassInfo caching.
 *
 */
public class ClassInfoDeadlockStressTest {

    private static final int DEADLOCK_TRIES = 8;
    private static final int THREAD_COUNT = 8;

    private final CountDownLatch startLatch = new CountDownLatch(1);
    private final CountDownLatch completeLatch = new CountDownLatch(THREAD_COUNT);
    private final GroovyClassLoader gcl = new GroovyClassLoader();
    private final AtomicInteger counter = new AtomicInteger();

    /**
     * We first generate a large number of ClassInfo instances for classes
     * that are no longer reachable.  Then queue up threads to all request
     * ClassInfo instances for new classes simultaneously to ensure that
     * clearing the old references wont deadlock the creation of new
     * instances.
     * <p>
     * GROOVY-8067
     */
    @Test
    public void testDeadlock() throws Exception {
        for (int i = 1; i <= DEADLOCK_TRIES; i++) {
            System.out.println("Test Number: " + i);
            generateGarbage();
            GCUtils.gc();
            attemptDeadlock(null);
        }
    }

    @Test
    public void testRequestsForSameClassInfo() throws Exception {
        Class<?> newClass = createRandomClass();
        for (int i = 1; i <= DEADLOCK_TRIES; i++) {
            System.out.println("Test Number: " + i);
            generateGarbage();
            GCUtils.gc();
            attemptDeadlock(newClass);
        }
        ClassInfo newClassInfo = ClassInfo.getClassInfo(newClass);
        for (ClassInfo ci : ClassInfo.getAllClassInfo()) {
            if (ci.getTheClass() == newClass && ci != newClassInfo) {
                fail("Found multiple ClassInfo instances for class");
            }
        }
    }

    private void attemptDeadlock(final Class<?> cls) throws Exception {
        for (int i = 0; i < THREAD_COUNT; i++) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    Class<?> newClass = (cls == null) ? createRandomClass() : cls;
                    ThreadUtils.await(startLatch);
                    ClassInfo ci = ClassInfo.getClassInfo(newClass);
                    assertEquals(newClass, ci.getTheClass());
                    completeLatch.countDown();
                }
            };
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            t.start();
        }
        startLatch.countDown();
        completeLatch.await(10L, TimeUnit.SECONDS);
        if (completeLatch.getCount() != 0) {
            System.err.println("Possible deadlock, grab a thread dump now");
            completeLatch.await(1L, TimeUnit.MINUTES);
            if (completeLatch.getCount() == 0) {
                System.out.println("No deadlock, but took longer than expected");
            } else {
                fail("Deadlock occurred");
            }
        } else {
            System.out.println("No deadlock detected");
        }
    }

    // This may deadlock so run in a separate thread
    private void generateGarbage() throws Exception {
        System.out.println("Generating garbage");
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 5000; i++) {
                    Class<?> c = createRandomClass();
                    ClassInfo ci = ClassInfo.getClassInfo(c);
                }
            }
        };
        Thread t = new Thread(runnable, "GenerateGarbageThread");
        t.setDaemon(true);
        t.start();
        t.join(TimeUnit.SECONDS.toMillis(120L));
        if (t.isAlive()) {
            fail("Deadlock detected while generating garbage");
        }
    }

    private Class<?> createRandomClass() {
        return gcl.parseClass("println foo-" + counter.incrementAndGet(), "Script1.groovy");
    }

}
