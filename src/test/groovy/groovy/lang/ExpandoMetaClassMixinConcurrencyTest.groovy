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

import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.atomic.AtomicBoolean

import static org.junit.jupiter.api.Assertions.assertTrue

final class ExpandoMetaClassMixinConcurrencyTest {

    static class Target {}

    // Adding a mixin (addMixinClass) mutates the backing mixin set while method
    // dispatch iterates it (findMixinMethod). The set must tolerate that; a plain
    // LinkedHashSet threw ConcurrentModificationException from the dispatching thread.
    @Test
    void mixinAddIsSafeAgainstConcurrentDispatch() {
        def loader = new GroovyClassLoader()
        // distinct category classes so every mixin is a fresh structural add
        def categories = (0..<160).collect { i ->
            loader.parseClass("class Cat_${i} { def catMethod_${i}() { 'x' } }", "Cat_${i}.groovy")
        }

        def emc = new ExpandoMetaClass(Target, false, true)
        emc.initialize()

        // pre-grow the set so each iteration spans a wide window
        (0..<40).each { MixinInMetaClass.mixinClassesToMetaClass(emc, [categories[it]]) }

        def errors = new CopyOnWriteArrayList<Throwable>()
        def stop = new AtomicBoolean(false)
        def emptyArgs = new Class[0]
        def barrier = new CyclicBarrier(5)

        def readers = (1..4).collect {
            Thread.start {
                try {
                    barrier.await()
                    while (!stop.get()) {
                        emc.findMixinMethod('noSuchMixinMethod', emptyArgs)
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
                barrier.await()
                for (int i = 40; i < categories.size() && !stop.get(); i++) {
                    MixinInMetaClass.mixinClassesToMetaClass(emc, [categories[i]])
                }
            } catch (Throwable t) {
                errors << t
            } finally {
                stop.set(true)
            }
        }

        writer.join()
        stop.set(true)
        readers*.join()

        assertTrue(errors.isEmpty(),
                "concurrent mixin add during dispatch iteration threw: ${errors.isEmpty() ? '' : errors.first()}")
    }
}
