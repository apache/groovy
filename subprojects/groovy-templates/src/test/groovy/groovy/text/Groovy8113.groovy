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
package groovy.text

import groovy.junit6.plugin.ForkedJvm
import groovy.lang.GroovyShell
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * Creating a template must not leave the generated script's companion
 * names in a long-lived class loader.
 */
@ForkedJvm(jvmArgs = ['--add-opens=java.base/java.lang=ALL-UNNAMED'], inheritProperties = ['spock.iKnowWhatImDoing.disableGroovyVersionCheck'])
final class Groovy8113 {

    private static final int ITERATIONS = 6

    @Test
    void repeatedTemplatesDoNotRetainGeneratedClassNames() {
        // GROOVY-8113
        Set<String> before = lockKeys()
        assertTrue(before != null, 'ClassLoader.parallelLockMap is not readable')

        for (int i = 0; i < ITERATIONS; i++) {
            String text = "Hello \${name} ${i}"
            assert new SimpleTemplateEngine().createTemplate(text).make([name: 'Ada']).toString().contains('Ada')
            assert new GStringTemplateEngine().createTemplate(text).make([name: 'Ada']).toString().contains('Ada')
            assert new StreamingTemplateEngine().createTemplate(text).make([name: 'Ada']).toString().contains('Ada')
        }

        Set<String> leaked = new TreeSet<>(lockKeys())
        leaked.removeAll(before)
        leaked.removeIf { String key -> key.find(GENERATED) == null }
        assert leaked.isEmpty() : "new parallelLockMap keys (${leaked.size()}):\n${leaked.join('\n')}"
    }

    private static final String GENERATED =
            /SimpleTemplateScript\d+|GStringTemplateScript\d+|StreamingTemplateScript\d+/

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
}
