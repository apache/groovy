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
package org.codehaus.groovy.vmplugin.v9

import org.antlr.v4.runtime.tree.ParseTree
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.control.ResolveVisitor
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.runtime.memoize.UnlimitedConcurrentCache
import org.codehaus.groovy.vmplugin.VMPluginFactory
import org.junit.jupiter.api.Test

import java.util.stream.Collectors

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertThrows
import static org.junit.jupiter.api.Assertions.assertTrue

final class ClassFinderTest {

    @Test
    void findGroovyClass() {
        Map<String, Set<String>> result = ClassFinder.find(GroovySystem.location.toURI(), 'groovy/lang')
        assert ['groovy/lang'] == result.get('GString')?.toList()
        assert null == result.get('GroovydocHolder')
    }

    @Test
    void findGroovyClass2() {
        Map<String, Set<String>> result = ClassFinder.find(GroovySystem.location.toURI(), 'groovy/util')
        assert ['groovy/util'] == result.get('NodeBuilder')?.toList()
    }

    @Test
    void findGroovyClass3() {
        Map<String, Set<String>> result = ClassFinder.find(ResolveVisitor.location.toURI(), 'org/codehaus/groovy/control')
        assert ['org/codehaus/groovy/control'] == result.get('ResolveVisitor')?.toList()
    }

    @Test
    void findGroovyClassRecursive() {
        Map<String, Set<String>> result = ClassFinder.find(GroovySystem.location.toURI(), 'groovy/lang', true)
        assert ['groovy/lang'] == result.get('GString')?.toList()
        assert ['groovy/lang/groovydoc'] == result.get('GroovydocHolder')?.toList()
    }

    @Test
    void findJavaClass() {
        Map<String, Set<String>> result = ClassFinder.find(URI.create('jrt:/modules/java.base/'), 'java/lang')
        assert ['java/lang'] == result.get('String')?.toList()
        assert null == result.get('MethodHandle')
    }

    @Test
    void findJavaClass2() {
        Map<String, Set<String>> result = ClassFinder.find(URI.create('jrt:/modules/java.base/'), 'java/util')
        assert ['java/util'] == result.get('Map')?.toList()
    }

    @Test
    void findJavaClass3() {
        Map<String, Set<String>> result = ClassFinder.find(URI.create('jrt:/modules/java.base/'), 'java/io')
        assert ['java/io'] == result.get('InputStream')?.toList()
    }

    @Test
    void findJavaClass4() {
        Map<String, Set<String>> result = ClassFinder.find(URI.create('jrt:/modules/java.base/'), 'java/net')
        assert ['java/net'] == result.get('Inet4Address')?.toList()
    }

    @Test
    void findJavaClassRecursive() {
        Map<String, Set<String>> result = ClassFinder.find(URI.create('jrt:/modules/java.base/'), 'java/lang', true)
        assert ['java/lang/invoke'] == result.get('MethodHandle')?.toList()
    }

    @Test
    void findJarClass() {
        Map<String, Set<String>> result = ClassFinder.find(ParseTree.location.toURI(), 'org/antlr/v4/runtime/tree')
        assert ['org/antlr/v4/runtime/tree'] == result.get('ParseTree')?.toList()
        assert null == result.get('ParseTreePattern')
    }

    @Test
    void findJarClassRecursive() {
        Map<String, Set<String>> result = ClassFinder.find(ParseTree.location.toURI(), 'org/antlr/v4/runtime/tree', true)
        assert ['org/antlr/v4/runtime/tree'] == result.get('ParseTree')?.toList()
        assert ['org/antlr/v4/runtime/tree/pattern'] == result.get('ParseTreePattern')?.toList()
    }

    @Test
    void defaultImportClasses() {
        Map<String, Set<String>> r1 = VMPluginFactory.getPlugin().getDefaultImportClasses(ResolveVisitor.DEFAULT_IMPORTS) as TreeMap<String, Set<String>>
        assert (ResolveVisitor.DEFAULT_IMPORTS as List).sort() == r1.values().stream().flatMap(e -> e.stream()).collect(Collectors.toSet()).sort()
        // Proxy exists in both java.net and groovy.util; both must be retained in DEFAULT_IMPORTS order
        assertEquals(['java.net.', 'groovy.util.'], r1.get('Proxy')?.toList())
        assertThrows(UnsupportedOperationException) {
            r1.get('String').add('custom.')
        }
    }

    @Test
    void defaultImportCachePreservesPrecedenceAndImmutability() {
        GroovyClassLoader gcl = new GroovyClassLoader(ClassFinderTest.classLoader)
        gcl.parseClass('package groovy.util\nclass DefaultImportOrderProbe {}')
        gcl.parseClass('package groovy.lang\nclass DefaultImportOrderProbe {}')

        // 1. Resolve when only groovy.util.DefaultImportOrderProbe is visible
        GroovyClassLoader utilOnlyLoader = new GroovyClassLoader(ClassFinderTest.classLoader) {
            @Override
            Class<?> loadClass(String name, boolean lookupScriptFiles, boolean preferClassOverScript, boolean resolve) throws ClassNotFoundException {
                if (name == 'groovy.lang.DefaultImportOrderProbe') throw new ClassNotFoundException(name)
                if (name == 'groovy.util.DefaultImportOrderProbe') return gcl.loadClass(name)
                return super.loadClass(name, lookupScriptFiles, preferClassOverScript, resolve)
            }
        }
        CompilationUnit cu1 = new CompilationUnit(utilOnlyLoader)
        SourceUnit su1 = cu1.addSource('Probe1.groovy', 'class Probe1 extends DefaultImportOrderProbe {}')
        cu1.compile(Phases.SEMANTIC_ANALYSIS)
        assertEquals('groovy.util.DefaultImportOrderProbe', su1.AST.classes[0].superClass.name)

        // 2. Now resolve when groovy.lang.DefaultImportOrderProbe is visible (groovy.util is missing in this loader so it falls back to DEFAULT_IMPORTS scan)
        GroovyClassLoader langOnlyLoader = new GroovyClassLoader(ClassFinderTest.classLoader) {
            @Override
            Class<?> loadClass(String name, boolean lookupScriptFiles, boolean preferClassOverScript, boolean resolve) throws ClassNotFoundException {
                if (name == 'groovy.util.DefaultImportOrderProbe') throw new ClassNotFoundException(name)
                if (name == 'groovy.lang.DefaultImportOrderProbe') return gcl.loadClass(name)
                return super.loadClass(name, lookupScriptFiles, preferClassOverScript, resolve)
            }
        }
        CompilationUnit cu2 = new CompilationUnit(langOnlyLoader)
        SourceUnit su2 = cu2.addSource('Probe2.groovy', 'class Probe2 extends DefaultImportOrderProbe {}')
        cu2.compile(Phases.SEMANTIC_ANALYSIS)
        assertEquals('groovy.lang.DefaultImportOrderProbe', su2.AST.classes[0].superClass.name)

        // 3. When both are visible, DEFAULT_IMPORTS precedence (groovy.lang. before groovy.util.) must win!
        def cacheField = ResolveVisitor.getDeclaredField('DEFAULT_IMPORT_CLASS_AND_PACKAGES_CACHE')
        cacheField.accessible = true
        Map<String, Set<String>> cache = (Map<String, Set<String>>) cacheField.get(null)
        try {
            assertEquals(['groovy.lang.', 'groovy.util.'], cache.get('DefaultImportOrderProbe')?.toList())

            CompilationUnit cu3 = new CompilationUnit(gcl)
            SourceUnit su3 = cu3.addSource('Probe3.groovy', 'class Probe3 extends DefaultImportOrderProbe {}')
            cu3.compile(Phases.SEMANTIC_ANALYSIS)
            assertEquals('groovy.lang.DefaultImportOrderProbe', su3.AST.classes[0].superClass.name)

            assertThrows(UnsupportedOperationException) {
                cache.get('DefaultImportOrderProbe').add('other.')
            }
        } finally {
            cache.remove('DefaultImportOrderProbe')
        }
    }

    @Test
    void unlimitedConcurrentCacheAtomicMapOperations() {
        UnlimitedConcurrentCache<String, Integer> cache = new UnlimitedConcurrentCache<>()
        assertEquals(1, cache.computeIfAbsent('a', k -> 1))
        assertEquals(1, cache.computeIfAbsent('a', k -> 2))
        assertEquals(1, cache.putIfAbsent('a', 3))
        assertEquals(10, cache.computeIfPresent('a', (k, v) -> v * 10))
        assertTrue(cache.replace('a', 10, 20))
        assertEquals(20, cache.replace('a', 30))
        assertEquals(35, cache.merge('a', 5, Integer::sum))
        assertTrue(cache.remove('a', 35))
        assertTrue(cache.isEmpty())
    }

    // GROOVY-9480
    @Test
    void noNoSuchFieldException() {
        def e = assertThrows(ClassFindFailedException) {
            ClassFinder.find(URI.create('file:/'), 'NOT_EXISTS', 'org/', false, true)
        }
        assert e.message.contains('Failed to find classes')
    }
}
