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

import groovy.lang.Binding
import groovy.lang.GroovyClassLoader
import groovy.lang.GroovyCodeSource
import groovy.lang.GroovyShell
import groovy.lang.Script
import groovy.ui.GroovySocketServer
import org.junit.jupiter.api.Test

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.ArrayList
import java.util.List
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicInteger

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertTrue
import static org.junit.jupiter.api.Assertions.fail

/**
 * Anonymous evaluate and parse record a generated name on the
 * {@link GroovyCodeSource} they create, and a class whose simple name is a
 * generated script name plus {@code Customizer} still loads.
 */
final class Groovy8113Compatibility {

    @Test
    void evaluateStringRecordsGeneratedNameOnCodeSource() {
        // GROOVY-8113
        int[] seen = [0]
        int[] marked = [0]
        GroovyShell shell = new GroovyShell() {
            @Override
            Script parse(GroovyCodeSource codeSource, Binding binding) {
                seen[0]++
                if (codeSource.isNameGenerated()) {
                    marked[0]++
                }
                return super.parse(codeSource, binding)
            }
        }
        assertEquals(2, shell.evaluate('1 + 1'))
        assertTrue(seen[0] >= 1)
        assertEquals(seen[0], marked[0])
    }

    @Test
    void parseClassStringRecordsGeneratedNameOnCodeSource() {
        // GROOVY-8113
        int[] seen = [0]
        int[] marked = [0]
        GroovyClassLoader loader = new GroovyClassLoader() {
            @Override
            Class parseClass(GroovyCodeSource codeSource) {
                seen[0]++
                if (codeSource.isNameGenerated()) {
                    marked[0]++
                }
                return super.parseClass(codeSource)
            }
        }
        Class<?> parsed = loader.parseClass('3 + 4')
        assertEquals(1, seen[0])
        assertEquals(1, marked[0])
        assertTrue(parsed.name ==~ /Script_[0-9a-fA-F]+/)
        assertEquals(7, parsed.getDeclaredConstructor().newInstance().run())
    }

    @Test
    void generatedScriptCanLoadItsOwnCustomizerClass() {
        // GROOVY-8113
        GroovyShell shell = new GroovyShell()
        List<?> loaded = shell.evaluate('''
            class Script1Customizer {
            }
            [this.class.name, this.class.classLoader.loadClass('Script1Customizer').name]
        ''')
        assertEquals(['Script1', 'Script1Customizer'], loaded)
        assertEquals('Script1Customizer', shell.classLoader.loadClass('Script1Customizer').name)
    }

    @Test
    void socketServerMarksInlineScriptNameGenerated() throws Exception {
        // GROOVY-8113
        Method method = GroovySocketServer.getDeclaredMethod('getCodeSource', boolean.class, String.class)
        method.setAccessible(true)
        GroovyCodeSource inline = (GroovyCodeSource) method.invoke(null, false, 'println 1')
        assertTrue(inline.isNameGenerated())
        assertTrue(inline.name ==~ /ServerSocketScript\d+\.groovy/)

        File file = File.createTempFile('Groovy8113Socket', '.groovy')
        file.deleteOnExit()
        file.write('println 2\n')
        GroovyCodeSource fromFile = (GroovyCodeSource) method.invoke(null, true, file.absolutePath)
        assertFalse(fromFile.isNameGenerated())

        GroovyCodeSource fromUri = (GroovyCodeSource) method.invoke(null, true, file.toURI().toString())
        assertFalse(fromUri.isNameGenerated())

        try {
            method.invoke(null, true, 'C:\\Users\\RUNNER~1\\AppData\\Local\\Temp\\Groovy8113Missing.groovy')
            fail('a missing Windows path should fail as a file')
        } catch (InvocationTargetException e) {
            assertTrue(e.cause instanceof GroovyRuntimeException)
            assertTrue(e.cause.message.startsWith('Unable to get script from:'))
            assertFalse(e.cause.message.contains('URI'))
        }
    }

    @Test
    void sharedShellAndLoaderMarkEveryConcurrentParse() throws Exception {
        // GROOVY-8113
        final int threads = 8
        final int each = 50
        AtomicInteger shellTotal = new AtomicInteger()
        AtomicInteger shellMarked = new AtomicInteger()
        GroovyShell shell = new GroovyShell() {
            @Override
            Script parse(GroovyCodeSource codeSource, Binding binding) {
                shellTotal.incrementAndGet()
                if (codeSource.isNameGenerated()) {
                    shellMarked.incrementAndGet()
                }
                return null
            }
        }
        AtomicInteger loaderTotal = new AtomicInteger()
        AtomicInteger loaderMarked = new AtomicInteger()
        GroovyClassLoader loader = new GroovyClassLoader() {
            @Override
            Class parseClass(GroovyCodeSource codeSource) {
                loaderTotal.incrementAndGet()
                if (codeSource.isNameGenerated()) {
                    loaderMarked.incrementAndGet()
                }
                return null
            }
        }
        runTogether(threads, each) { int id, int i ->
            shell.parse("${id}-${i}")
            loader.parseClass("${id}-${i}")
        }
        assertEquals(threads * each, shellTotal.get())
        assertEquals(threads * each, shellMarked.get())
        assertEquals(threads * each, loaderTotal.get())
        assertEquals(threads * each, loaderMarked.get())
    }

    @Test
    void callerChosenNameIsNotMarkedGenerated() {
        // GROOVY-8113
        AtomicInteger marked = new AtomicInteger()
        GroovyShell shell = new GroovyShell() {
            @Override
            Script parse(GroovyCodeSource codeSource, Binding binding) {
                if (codeSource.isNameGenerated()) {
                    marked.incrementAndGet()
                }
                return null
            }
        }
        shell.parse('2', 'Script1.groovy')
        assertEquals(0, marked.get())
    }

    @Test
    void nestedEvaluateStillRuns() {
        // GROOVY-8113
        assertEquals(2, new GroovyShell().evaluate('evaluate("1 + 1")'))
    }

    private static void runTogether(int threads, int each, GroovyClosure work) throws Exception {
        CyclicBarrier start = new CyclicBarrier(threads)
        ExecutorService pool = Executors.newFixedThreadPool(threads)
        try {
            List<Future<?>> futures = new ArrayList<>()
            for (int t = 0; t < threads; t++) {
                int id = t
                futures.add(pool.submit {
                    start.await()
                    for (int i = 0; i < each; i++) {
                        work.accept(id, i)
                    }
                    return null
                })
            }
            for (Future<?> future : futures) {
                future.get()
            }
        } finally {
            pool.shutdown()
        }
    }

    @FunctionalInterface
    private interface GroovyClosure {
        void accept(int id, int i) throws Exception
    }
}
