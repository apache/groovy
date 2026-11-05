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

import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.control.SourceUnit
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout

import java.security.CodeSource
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotSame
import static org.junit.jupiter.api.Assertions.assertSame
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * GROOVY-11726: {@code GroovyClassLoader.parseClass(GroovyCodeSource, boolean)}
 * of distinct sources must not queue behind one cache lock, and a repeated
 * source must still resolve to the cached class.
 */
@Timeout(value = 60, unit = TimeUnit.SECONDS)
final class Groovy11726 {

    @Test
    void repeatedSourceUsesTheCachedClass() {
        def loader = new GroovyClassLoader()
        try {
            def source = source('class Cached { int n() { 7 } }', 'Cached.groovy')
            def first = loader.parseClass(source, true)
            def second = loader.parseClass(source, true)
            assertSame(first, second)
            assertEquals(7, first.getDeclaredMethod('n').invoke(first.getDeclaredConstructor().newInstance()))
        } finally {
            loader.close()
        }
    }

    @Test
    void uncachedParseCompilesAgain() {
        def loader = new GroovyClassLoader()
        try {
            def source = source('class Fresh { int n() { 1 } }', 'Fresh.groovy')
            def first = loader.parseClass(source, false)
            def second = loader.parseClass(source, false)
            assertNotSame(first, second)
            assertEquals(first.name, second.name)
        } finally {
            loader.close()
        }
    }

    @Test
    void distinctSourcesCompileConcurrently() {
        int threads = 4
        def entered = new CountDownLatch(threads)
        def active = new AtomicInteger()
        def maxActive = new AtomicInteger()
        def loader = new GroovyClassLoader() {
            @Override
            protected CompilationUnit createCompilationUnit(CompilerConfiguration config, CodeSource codeSource) {
                CompilationUnit unit = super.createCompilationUnit(config, codeSource)
                unit.addPhaseOperation(new CompilationUnit.ISourceUnitOperation() {
                    @Override
                    void call(SourceUnit source) {
                        int now = active.incrementAndGet()
                        maxActive.updateAndGet { current -> Math.max(current, now) }
                        entered.countDown()
                        try {
                            // A serialized compile never reaches the other threads
                            // while this one is inside the phase, so the wait expires.
                            entered.await(2, TimeUnit.SECONDS)
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt()
                        } finally {
                            active.decrementAndGet()
                        }
                    }
                }, Phases.CONVERSION)
                return unit
            }
        }
        def pool = Executors.newFixedThreadPool(threads)
        try {
            def tasks = (0..<threads).collect { int id ->
                pool.submit {
                    def compiled = loader.parseClass(source("""
                        package bench.hot
                        class Job${id} {
                            int value() { ${id} }
                        }
                    """.stripIndent(), "Job${id}.groovy"), true)
                    assertEquals(id, compiled.getDeclaredMethod('value').invoke(compiled.getDeclaredConstructor().newInstance()))
                    return compiled
                }
            }
            def classes = tasks.collect { it.get() }
            assertEquals(threads, classes*.name.toSet().size())
            assertTrue(maxActive.get() > 1, "distinct parseClass calls overlapped ${maxActive.get()} at a time")
        } finally {
            pool.shutdownNow()
            loader.close()
        }
    }

    private static GroovyCodeSource source(String text, String name) {
        def source = new GroovyCodeSource(text, name, '/groovy/script')
        source.cachable = true
        return source
    }
}
