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

import groovy.junit6.plugin.ForkedJvm
import groovy.lang.GroovyShell
import groovy.util.GroovyScriptEngine
import org.codehaus.groovy.reflection.ClassInfo
import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.nio.file.Path

import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * Repeated script evaluation must not leave a name in a long-lived class
 * loader for every generated script class.
 */
@ForkedJvm(jvmArgs = ['--add-opens=java.base/java.lang=ALL-UNNAMED'])
final class Groovy8113 {

    private static final int ITERATIONS = 8

    @Test
    void repeatedEvaluationDoesNotRetainGeneratedClassNames() throws Exception {
        // GROOVY-8113
        Set<String> before = lockKeys()
        assertTrue(before != null, 'ClassLoader.parallelLockMap is not readable')

        GroovyShell shell = new GroovyShell()
        for (int i = 0; i < ITERATIONS; i++) {
            assert shell.evaluate("${i} + 1", "Groovy8113Shell_${i}.groovy") == i + 1
            assert new GroovyShell().evaluate("${i} + 2") == i + 2
        }
        assert shell.evaluate('''
            package pkg
            class Groovy8113Box { String item = 'x' }
            new Groovy8113Box().item
        ''', 'pkg/Groovy8113Holder.groovy') == 'x'

        Path root = Files.createTempDirectory('groovy8113')
        try {
            Path nested = root.resolve('pkg')
            Files.createDirectories(nested)
            for (int i = 0; i < ITERATIONS; i++) {
                Files.writeString(root.resolve("Groovy8113_${i}.groovy"), "${i} + 3\n")
                GroovyScriptEngine engine = new GroovyScriptEngine(root.toUri().toString())
                assert engine.run("Groovy8113_${i}.groovy", '') == "${i + 3}"
            }
            Files.writeString(nested.resolve('Groovy8113P.groovy'), 'package pkg\n4 + 5\n')
            GroovyScriptEngine packaged = new GroovyScriptEngine(root.toUri().toString())
            assert packaged.run('pkg/Groovy8113P.groovy', '') == '9'
        } finally {
            deleteTree(root)
        }

        Set<String> leaked = new TreeSet<>(lockKeys())
        leaked.removeAll(before)
        leaked.removeIf { String key -> key.find(GENERATED) == null }

        List<String> retained = new ArrayList<>()
        for (ClassInfo info : ClassInfo.getAllClassInfo()) {
            Class<?> type = info.getTheClass()
            if (type != null && type.name =~ GENERATED) {
                retained.add(type.name)
            }
        }
        assert leaked.isEmpty() : "new parallelLockMap keys (${leaked.size()}):\n${leaked.join('\n')}\nClassInfo still holding:\n${retained.join('\n')}"
    }

    private static final String GENERATED =
            /script\d+|Groovy8113Shell_\d+|Groovy8113_\d+|pkg\.Groovy8113|SimpleTemplateScript\d+/

    private static Set<String> lockKeys() {
        Set<String> keys = new TreeSet<>()
        boolean readable = false
        for (ClassLoader loader : loaders()) {
            Set<String> map = parallelLockMap(loader)
            if (map == null) {
                continue
            }
            readable = true
            String loaderName = loader.getClass().getName() + '@' + System.identityHashCode(loader)
            for (String name : map) {
                keys.add(loaderName + '|' + name)
            }
        }
        return readable ? keys : null
    }

    private static List<ClassLoader> loaders() {
        Set<ClassLoader> seen = Collections.newSetFromMap(new IdentityHashMap<>())
        List<ClassLoader> out = new ArrayList<>()
        ClassLoader[] roots = [
                Groovy8113.class.classLoader,
                GroovyShell.class.classLoader,
                Thread.currentThread().contextClassLoader,
                ClassLoader.systemClassLoader
        ]
        for (ClassLoader start : roots) {
            ClassLoader current = start
            while (current != null && seen.add(current)) {
                out.add(current)
                current = current.parent
            }
        }
        return out
    }

    private static Set<String> parallelLockMap(ClassLoader loader) {
        return ParallelLockMapKeys.copy(loader)
    }

    private static void deleteTree(Path root) {
        if (root == null || !Files.exists(root)) {
            return
        }
        Files.walk(root).sorted(Comparator.reverseOrder()).forEach { Path path ->
            Files.deleteIfExists(path)
        }
    }
}
