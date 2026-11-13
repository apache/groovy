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

import groovy.lang.GroovyClassLoader
import org.apache.groovy.vmplugin.internal.DefaultImportPackages
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.control.ResolveVisitor
import org.codehaus.groovy.control.SourceUnit
import org.junit.jupiter.api.Test

import java.lang.reflect.Field
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.TimeUnit

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertSame
import static org.junit.jupiter.api.Assertions.assertThrows
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * Default-import resolution keeps package precedence when the same simple
 * name exists in more than one default-imported package, including when a
 * lower-precedence package was recorded first.
 */
final class Groovy12725 {

    @Test
    void startupIndexResolvesInPrecedenceOrderWithoutReplacingTheSnapshot() {
        Map<String, Set<String>> cache = cache()
        Map<String, Set<String>> index = index()
        ClassNode type = compile(null, 'Indexed.groovy', '''
            class Indexed {
                String s
                List l
                Proxy p
                BigInteger n
                BigDecimal d
            }
        ''')
        assertEquals('java.lang.String', type.getField('s').type.name)
        assertEquals('java.util.List', type.getField('l').type.name)
        assertEquals('java.net.Proxy', type.getField('p').type.name)
        assertEquals('java.math.BigInteger', type.getField('n').type.name)
        assertEquals('java.math.BigDecimal', type.getField('d').type.name)
        assertSame(index.get('String'), cache.get('String'))
        assertSame(index.get('List'), cache.get('List'))
        assertSame(index.get('Proxy'), cache.get('Proxy'))
        assertNull(cache.get('BigInteger'))
        assertNull(cache.get('BigDecimal'))
    }

    @Test
    void seededLowerPrecedenceLosesToAHigherIndexedPackage() {
        withCache('Proxy', Collections.singleton('groovy.util.')) { cache ->
            ClassNode type = compile(null, 'SeededProxy.groovy', 'class SeededProxy { Proxy p }')
            assertEquals('java.net.Proxy', type.getField('p').type.name)
            assertEquals(['java.net.', 'groovy.util.'], cache.get('Proxy').toList())
            assertThrows(UnsupportedOperationException) {
                cache.get('Proxy').add('java.lang.')
            }
            assertFalse(cache.get('Proxy').is(index().get('Proxy')))
        }
        assertSame(index().get('Proxy'), cache().get('Proxy'))
    }

    @Test
    void partialCacheChecksHigherPrefixesBeforeAcceptingAKnownPackage() {
        withCache('Proxy', DefaultImportPackages.ordered(List.of('java.net.', 'groovy.util.'), ResolveVisitor.DEFAULT_IMPORTS)) { cache ->
            Set<String> seeded = cache.get('Proxy')
            ClassNode type = compile(null, 'PartialProxy.groovy', 'class PartialProxy { Proxy p }')
            assertEquals('java.net.Proxy', type.getField('p').type.name)
            assertSame(seeded, cache.get('Proxy'))
        }
    }

    @Test
    void bothUserClassesPreferTheHigherPackageEvenWhenTheLowerOneWasCached() {
        String name = 'OrderProbeHigher'
        GroovyClassLoader loader = define(name, 'groovy.lang', 'groovy.util')
        withCache(name, Collections.singleton('groovy.util.')) { cache ->
            ClassNode type = compile(loader, "${name}Client.groovy", "class ${name}Client extends ${name} {}")
            assertEquals("groovy.lang.${name}".toString(), type.superClass.name)
            assertEquals(['groovy.lang.', 'groovy.util.'], cache.get(name).toList())
            assertThrows(UnsupportedOperationException) {
                cache.get(name).add('java.lang.')
            }
        }
        assertNull(cache().get(name))
    }

    @Test
    void firstResolutionRecordsOnlyTheHighestVisiblePackage() {
        String name = 'OrderProbeFirst'
        GroovyClassLoader loader = define(name, 'groovy.lang', 'groovy.util')
        withCache(name, null) { cache ->
            ClassNode type = compile(loader, "${name}Client.groovy", "class ${name}Client extends ${name} {}")
            assertEquals("groovy.lang.${name}".toString(), type.superClass.name)
            assertEquals(['groovy.lang.'], cache.get(name).toList())
            assertThrows(UnsupportedOperationException) {
                cache.get(name).add('groovy.util.')
            }
        }
    }

    @Test
    void fullScanRecordsALowerPackageAfterTheCachedHigherPrefixMisses() {
        String name = 'OrderProbeMiss'
        GroovyClassLoader loader = define(name, 'groovy.util')
        withCache(name, Collections.singleton('groovy.lang.')) { cache ->
            ClassNode type = compile(loader, "${name}Client.groovy", "class ${name}Client extends ${name} {}")
            assertEquals("groovy.util.${name}".toString(), type.superClass.name)
            assertEquals(['groovy.lang.', 'groovy.util.'], cache.get(name).toList())
        }
    }

    @Test
    void freshResolutionRecordsTheSingleVisiblePackage() {
        String name = 'OrderProbeFresh'
        GroovyClassLoader loader = define(name, 'groovy.util')
        withCache(name, null) { cache ->
            ClassNode type = compile(loader, "${name}Client.groovy", "class ${name}Client extends ${name} {}")
            assertEquals("groovy.util.${name}".toString(), type.superClass.name)
            assertEquals(['groovy.util.'], cache.get(name).toList())
        }
    }

    @Test
    void defaultImportCacheIsIndependentAcrossClassLoaders() {
        String name = 'CrossLoaderProbe12725'
        GroovyClassLoader loaderA = define(name, 'groovy.util')
        GroovyClassLoader loaderB = define(name, 'groovy.lang')
        withCache(name, null) { cache ->
            ClassNode first = compile(loaderA, "${name}A1.groovy", "class ${name}A1 extends ${name} {}")
            assertEquals("groovy.util.${name}".toString(), first.superClass.name)
            assertEquals(['groovy.util.'], cache.get(name).toList())

            ClassNode second = compile(loaderB, "${name}B.groovy", "class ${name}B extends ${name} {}")
            assertEquals("groovy.lang.${name}".toString(), second.superClass.name)
            assertEquals(['groovy.lang.', 'groovy.util.'], cache.get(name).toList())

            ClassNode third = compile(loaderA, "${name}A2.groovy", "class ${name}A2 extends ${name} {}")
            assertEquals("groovy.util.${name}".toString(), third.superClass.name)
            assertEquals(['groovy.lang.', 'groovy.util.'], cache.get(name).toList())
            assertFalse(cache.get(name).is(index().get(name)))
            assertThrows(UnsupportedOperationException) {
                cache.get(name).add('java.lang.')
            }
        }
        assertNull(cache().get(name))
    }

    @Test
    void concurrentResolutionsOfTheSameNameHonorEachClassLoader() {
        String name = 'ConcurrentProbe12725'
        int threads = 8
        int rounds = 4
        String utilSource = "package groovy.util\nclass ${name} {}"
        String langSource = "package groovy.lang\nclass ${name} {}"
        GroovyClassLoader[] loaders = new GroovyClassLoader[threads]
        for (int i = 0; i < threads; i++) {
            GroovyClassLoader loader = new GroovyClassLoader(Groovy12725.class.classLoader)
            loader.parseClass((i % 2 == 0) ? utilSource : langSource)
            loaders[i] = loader
        }
        String[][] resolved = new String[threads][rounds]
        Throwable[] failures = new Throwable[threads]
        CyclicBarrier start = new CyclicBarrier(threads)
        Thread[] workers = new Thread[threads]
        withCache(name, null) { cache ->
            threads.times { int id ->
                workers[id] = Thread.start {
                    try {
                        start.await(30, TimeUnit.SECONDS)
                        rounds.times { int round ->
                            String client = "${name}_${id}_${round}"
                            ClassNode type = compile(loaders[id], "${client}.groovy", "class ${client} extends ${name} {}")
                            resolved[id][round] = type.superClass.name
                        }
                    } catch (Throwable failure) {
                        failures[id] = failure
                    }
                }
            }
            for (int i = 0; i < threads; i++) {
                workers[i].join(60_000)
                assertFalse(workers[i].isAlive(), "worker ${i} did not finish")
                if (failures[i] != null) {
                    throw new AssertionError("worker ${i} failed", failures[i])
                }
                String expected = (i % 2 == 0) ? "groovy.util.${name}" : "groovy.lang.${name}"
                for (int round = 0; round < rounds; round++) {
                    assertEquals(expected, resolved[i][round])
                }
            }
            assertEquals(['groovy.lang.', 'groovy.util.'], cache.get(name).toList())
            assertFalse(cache.get(name).is(index().get(name)))
            assertThrows(UnsupportedOperationException) {
                cache.get(name).add('java.lang.')
            }
        }
        assertNull(cache().get(name))
    }

    @Test
    void packageOutsideDefaultImportsDoesNotBlockTheFullScan() {
        String name = 'OrderProbeForeign'
        GroovyClassLoader loader = define(name, 'groovy.util')
        withCache(name, Collections.singleton('com.example.')) { cache ->
            ClassNode type = compile(loader, "${name}Client.groovy", "class ${name}Client extends ${name} {}")
            assertEquals("groovy.util.${name}".toString(), type.superClass.name)
            assertEquals(['groovy.util.'], cache.get(name).toList())
        }
    }

    @Test
    void indexedMissDoesNotRecordAPackage() {
        Map<String, Set<String>> cache = cache()
        Set<String> snapshot = index().get('Proxy')
        assertSame(snapshot, cache.get('Proxy'))
        GroovyClassLoader loader = new GroovyClassLoader(Groovy12725.class.classLoader) {
            @Override
            URL getResource(String resource) {
                if (resource == 'java/net/Proxy.class' || resource == 'groovy/util/Proxy.class') {
                    return null
                }
                return super.getResource(resource)
            }

            @Override
            Class loadClass(String className, boolean lookupScriptFiles, boolean preferClassOverScript, boolean resolve) {
                if (className == 'java.net.Proxy' || className == 'groovy.util.Proxy') {
                    throw new ClassNotFoundException(className)
                }
                return super.loadClass(className, lookupScriptFiles, preferClassOverScript, resolve)
            }
        }
        CompilationUnit cu = new CompilationUnit(loader)
        cu.addSource('HiddenProxy.groovy', 'class HiddenProxy { Proxy p }')
        assertThrows(CompilationFailedException) {
            cu.compile(Phases.SEMANTIC_ANALYSIS)
        }
        assertSame(snapshot, cache.get('Proxy'))
    }

    @Test
    void lowerCaseNameSkipsDefaultImports() {
        ClassNode type = compile(null, 'Lower.groovy', '''
            class Lower {
                def m() { lowercaseprobe }
            }
        ''')
        def code = type.methods.find { it.name == 'm' }.code
        assertTrue(code instanceof BlockStatement)
        def statement = ((BlockStatement) code).statements[-1]
        assertFalse(statement.expression instanceof ClassExpression)
        assertNull(cache().get('lowercaseprobe'))
    }

    private static GroovyClassLoader define(String name, String... packages) {
        GroovyClassLoader loader = new GroovyClassLoader(Groovy12725.class.classLoader)
        for (String pkg : packages) {
            loader.parseClass("package ${pkg}\nclass ${name} {}")
        }
        cache().remove(name)
        return loader
    }

    private static ClassNode compile(GroovyClassLoader loader, String fileName, String source) {
        CompilationUnit unit = loader == null ? new CompilationUnit() : new CompilationUnit(loader)
        SourceUnit su = unit.addSource(fileName, source)
        unit.compile(Phases.SEMANTIC_ANALYSIS)
        return su.AST.classes[0]
    }

    private static void withCache(String name, Set<String> seeded, Closure<?> body) {
        Map<String, Set<String>> cache = cache()
        Set<String> previous = cache.get(name)
        if (seeded == null) {
            cache.remove(name)
        } else {
            cache.put(name, seeded)
        }
        try {
            body.call(cache)
        } finally {
            if (previous == null) {
                cache.remove(name)
            } else {
                cache.put(name, previous)
            }
        }
    }

    private static Map<String, Set<String>> cache() {
        return staticMap('DEFAULT_IMPORT_CLASS_AND_PACKAGES_CACHE')
    }

    private static Map<String, Set<String>> index() {
        return staticMap('DEFAULT_IMPORT_INDEX')
    }

    private static Map<String, Set<String>> staticMap(String fieldName) {
        Field field = ResolveVisitor.getDeclaredField(fieldName)
        field.accessible = true
        return (Map<String, Set<String>>) field.get(null)
    }
}
