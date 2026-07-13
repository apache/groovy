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
package groovy.lang

import org.codehaus.groovy.reflection.MixinInMetaClass
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout

import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertTrue

final class ExpandoMetaClassMixinConcurrencyTest {

    private static final int CATEGORY_COUNT = 40
    private static final int PRE_MIXED_COUNT = 8
    private static final int READER_COUNT = 3
    private static final int TIMEOUT_SECONDS = 30

    static class Target {}

    // Adding a mixin (addMixinClass) mutates the backing mixin set while method
    // dispatch iterates it (findMixinMethod). The set must tolerate that; a plain
    // LinkedHashSet threw ConcurrentModificationException in the dispatching thread.
    @Test
    @Timeout(60)
    void mixinAddIsSafeAgainstConcurrentDispatch() {
        def loader = new GroovyClassLoader()
        // distinct category classes so every mixin is a fresh structural add
        def categories = (0..<CATEGORY_COUNT).collect { i ->
            loader.parseClass("class Cat_$i { def catMethod_$i() { 'x' } }", "Cat_${i}.groovy")
        }

        def emc = new ExpandoMetaClass(Target, false, true)
        emc.initialize()

        // pre-populate so the readers always have something to iterate over
        categories[0..<PRE_MIXED_COUNT].each { MixinInMetaClass.mixinClassesToMetaClass(emc, [it]) }

        def errors = new CopyOnWriteArrayList<Throwable>()
        def stop = new AtomicBoolean(false)
        def noArgs = new Class[0]
        def barrier = new CyclicBarrier(READER_COUNT + 1)

        def readers = (1..READER_COUNT).collect {
            Thread.start {
                try {
                    barrier.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    // findMixinMethod never blocks, so the interrupt flag is the only
                    // way out if the writer dies without setting stop
                    while (!stop.get() && !Thread.currentThread().isInterrupted()) {
                        emc.findMixinMethod('noSuchMixinMethod', noArgs)
                    }
                } catch (Throwable t) {
                    errors << t
                } finally {
                    stop.set(true)
                }
            }
        }
        def writer = Thread.start {
            try {
                barrier.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                categories[PRE_MIXED_COUNT..<CATEGORY_COUNT].each {
                    MixinInMetaClass.mixinClassesToMetaClass(emc, [it])
                }
            } catch (Throwable t) {
                errors << t
            } finally {
                stop.set(true)
            }
        }

        def threads = [writer, *readers]
        threads.each { it.join(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS)) }
        def stuck = threads.findAll { it.alive }
        if (!stuck.isEmpty()) {
            stop.set(true) // release the readers, which spin rather than block
            stuck*.interrupt()
            stuck.each { it.join(TimeUnit.SECONDS.toMillis(1)) }
        }
        assertTrue(stuck.isEmpty(), "threads still running after ${TIMEOUT_SECONDS}s: $stuck")

        if (!errors.isEmpty()) {
            throw new AssertionError(
                    "concurrent mixin add during dispatch iteration threw: ${errors.first()}", errors.first())
        }

        // every concurrent add must still be observable, i.e. no lost updates
        (0..<CATEGORY_COUNT).each { i ->
            assertNotNull(emc.findMixinMethod("catMethod_$i", noArgs), "mixin $i was lost")
        }
    }
}
