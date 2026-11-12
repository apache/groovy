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

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.runtime.memoize.ConcurrentCommonCache
import org.codehaus.groovy.runtime.memoize.MemoizeCache
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout

import java.net.URL
import java.security.CodeSource
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotSame
import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertSame
import static org.junit.jupiter.api.Assertions.assertThrows
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * GROOVY-11726: {@code GroovyClassLoader.parseClass(GroovyCodeSource, boolean)}
 * of distinct sources must not queue behind one cache lock, a repeated
 * source must still resolve to the cached class, and only the first result
 * published for a key is kept. Concurrent parses that share a package must
 * survive the losing {@code definePackage}.
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

    @Test
    void sameSourceCompiledConcurrentlyKeepsTheFirstClass() {
        int threads = 2
        def entered = new CountDownLatch(threads)
        def loader = new GroovyClassLoader() {
            @Override
            protected CompilationUnit createCompilationUnit(CompilerConfiguration config, CodeSource codeSource) {
                CompilationUnit unit = super.createCompilationUnit(config, codeSource)
                unit.addPhaseOperation(new CompilationUnit.ISourceUnitOperation() {
                    @Override
                    void call(SourceUnit source) {
                        entered.countDown()
                        // Both parses must miss the cache. Waiting here, before
                        // the result is published, is what makes that true.
                        if (!entered.await(10, TimeUnit.SECONDS)) {
                            throw new IllegalStateException('the other parse did not enter compilation')
                        }
                    }
                }, Phases.CONVERSION)
                return unit
            }
        }
        def pool = Executors.newFixedThreadPool(threads)
        try {
            def source = source('class Shared { int n() { 3 } }', 'Shared.groovy')
            def tasks = (0..<threads).collect {
                pool.submit { loader.parseClass(source, true) }
            }
            def classes = tasks.collect { it.get() }
            assertSame(classes[0], classes[1])
            assertSame(classes[0], loader.parseClass(source, true))
            assertEquals(3, classes[0].getDeclaredMethod('n').invoke(classes[0].getDeclaredConstructor().newInstance()))
        } finally {
            pool.shutdownNow()
            loader.close()
        }
    }

    @Test
    void secondClassInAPackageReusesTheDefinedPackage() {
        def loader = new GroovyClassLoader()
        try {
            def first = loader.parseClass(source('''
                package seq.pkg
                class SeqA { int n() { 1 } }
            '''.stripIndent(), 'SeqA.groovy'), true)
            def second = loader.parseClass(source('''
                package seq.pkg
                class SeqB { int n() { 2 } }
            '''.stripIndent(), 'SeqB.groovy'), true)
            assertNotSame(first, second)
            assertEquals('seq.pkg', first.packageName)
            assertEquals('seq.pkg', second.packageName)
            assertEquals('seq.pkg', loader.getDefinedPackage('seq.pkg').name)
            assertEquals(1, first.getDeclaredMethod('n').invoke(first.getDeclaredConstructor().newInstance()))
            assertEquals(2, second.getDeclaredMethod('n').invoke(second.getDeclaredConstructor().newInstance()))
        } finally {
            loader.close()
        }
    }

    @Test
    void concurrentParsesOfOnePackageKeepTheFirstDefinition() {
        int threads = 2
        def arrived = new CountDownLatch(threads)
        def loader = new GroovyClassLoader() {
            @Override
            protected Package definePackage(String name, String specTitle, String specVersion, String specVendor,
                                            String implTitle, String implVersion, String implVendor, URL sealBase) {
                if ('race.pkg' == name) {
                    arrived.countDown()
                    // Hold the first caller before the package is inserted so
                    // the other parse still observes it as missing and both
                    // call definePackage. The platform then rejects the loser.
                    if (!arrived.await(10, TimeUnit.SECONDS)) {
                        throw new IllegalStateException('the other parse did not reach definePackage')
                    }
                }
                return super.definePackage(name, specTitle, specVersion, specVendor, implTitle, implVersion, implVendor, sealBase)
            }
        }
        def pool = Executors.newFixedThreadPool(threads)
        try {
            def tasks = (0..<threads).collect { int id ->
                pool.submit {
                    def compiled = loader.parseClass(source("""
                        package race.pkg
                        class Race${id} {
                            int value() { ${id} }
                        }
                    """.stripIndent(), "Race${id}.groovy"), true)
                    assertEquals(id, compiled.getDeclaredMethod('value').invoke(compiled.getDeclaredConstructor().newInstance()))
                    return compiled
                }
            }
            def classes = tasks.collect { it.get() }
            assertEquals(['race.pkg.Race0', 'race.pkg.Race1'] as Set, classes*.name.toSet())
            assertEquals('race.pkg', loader.getDefinedPackage('race.pkg').name)
        } finally {
            pool.shutdownNow()
            loader.close()
        }
    }

    @Test
    void packageRejectionIsNotSwallowed() {
        def loader = new GroovyClassLoader() {
            @Override
            protected Package definePackage(String name, String specTitle, String specVersion, String specVendor,
                                            String implTitle, String implVersion, String implVendor, URL sealBase) {
                if ('rejected.pkg' == name) {
                    throw new IllegalArgumentException(name)
                }
                return super.definePackage(name, specTitle, specVersion, specVendor, implTitle, implVersion, implVendor, sealBase)
            }
        }
        try {
            def thrown = assertThrows(IllegalArgumentException) {
                loader.parseClass(source('''
                    package rejected.pkg
                    class Rejected { }
                '''.stripIndent(), 'Rejected.groovy'), true)
            }
            assertEquals('rejected.pkg', thrown.message)
            assertNull(loader.getDefinedPackage('rejected.pkg'))
        } finally {
            loader.close()
        }
    }

    @Test
    void unpublishedClassIsReturnedAndCompiledAgain() {
        def cache = new ConcurrentCommonCache<String, Class>() {
            @Override
            Class getAndPut(String key, MemoizeCache.ValueProvider<? super String, ? extends Class> valueProvider, boolean shouldCache) {
                null
            }
        }
        def loader = new GroovyClassLoader(getClass().classLoader, new CompilerConfiguration(), false, cache)
        try {
            def source = source('class Unstored { int n() { 4 } }', 'Unstored.groovy')
            def first = loader.parseClass(source, true)
            def second = loader.parseClass(source, true)
            assertNotSame(first, second)
            assertEquals(first.name, second.name)
            assertEquals(4, first.getDeclaredMethod('n').invoke(first.getDeclaredConstructor().newInstance()))
        } finally {
            loader.close()
        }
    }

    @Test
    void parseThatDefinesNoClassIsNotCached() {
        def defined = new AtomicInteger()
        def loader = new GroovyClassLoader() {
            @Override
            protected ClassCollector createCollector(CompilationUnit unit, SourceUnit su) {
                new ClassCollector(new InnerLoader(this), unit, su) {
                    @Override
                    protected Class createClass(byte[] code, ClassNode classNode) {
                        defined.incrementAndGet()
                        null
                    }
                }
            }
        }
        try {
            def source = source('class NoBytes { }', 'NoBytes.groovy')
            assertNull(loader.parseClass(source, true))
            assertNull(loader.parseClass(source, true))
            // The empty result is not a class the next call can reuse.
            assertTrue(defined.get() >= 2, "compilations that produced bytecode: ${defined.get()}")
        } finally {
            loader.close()
        }
    }

    private static GroovyCodeSource source(String text, String name) {
        def source = new GroovyCodeSource(text, name, '/groovy/script')
        source.cachable = true
        return source
    }
}
