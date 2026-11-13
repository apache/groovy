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
package org.codehaus.groovy.jsr223

import groovy.junit6.plugin.ForkedJvm
import org.junit.jupiter.api.Test

import javax.script.ScriptContext
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
import javax.script.SimpleScriptContext

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * Repeated {@code ScriptEngine.eval} must not leave a generated script name
 * in a long-lived class loader. A script whose file name was supplied keeps
 * only its MetaClass, BeanInfo and Customizer companions.
 */
@ForkedJvm(jvmArgs = ['--add-opens=java.base/java.lang=ALL-UNNAMED'])
final class Groovy8113 {

    private static final int ITERATIONS = 8

    @Test
    void repeatedEvalDoesNotRetainGeneratedClassNames() {
        // GROOVY-8113
        Set<String> before = lockKeys()
        assertTrue(before != null, 'ClassLoader.parallelLockMap is not readable')

        ScriptEngine engine = new ScriptEngineManager().getEngineByName('groovy')
        assertTrue(engine != null, 'groovy script engine is required')
        for (int i = 0; i < ITERATIONS; i++) {
            assertEquals(i + 1, engine.eval("${i} + 1"))
        }

        SimpleScriptContext named = new SimpleScriptContext()
        named.setAttribute(ScriptEngine.FILENAME, 'Groovy8113Named.groovy', ScriptContext.ENGINE_SCOPE)
        named.setBindings(engine.createBindings(), ScriptContext.ENGINE_SCOPE)
        assertEquals(3, engine.eval('def closure = { it }; assert closure(3) == 3; 1 + 2', named))

        Set<String> leaked = new TreeSet<>(lockKeys())
        leaked.removeAll(before)
        leaked.removeIf { String key -> !isForbidden(key) }
        assert leaked.isEmpty() : "new parallelLockMap keys (${leaked.size()}):\n${leaked.join('\n')}"
    }

    private static boolean isForbidden(String key) {
        String name = key.substring(key.indexOf('|') + 1)
        String base = companionBase(name)
        if (base != null) {
            return isGenerated(base)
        }
        return isGenerated(name) || isNamedScript(name)
    }

    private static String companionBase(String name) {
        String meta = 'groovy.runtime.metaclass.'
        if (name.startsWith(meta) && name.endsWith('MetaClass') && name.length() > meta.length() + 'MetaClass'.length()) {
            return name.substring(meta.length(), name.length() - 'MetaClass'.length())
        }
        for (String prefix : ['com.sun.beans.infos.', 'sun.beans.infos.']) {
            if (name.startsWith(prefix) && name.endsWith('BeanInfo') && name.length() > prefix.length() + 'BeanInfo'.length()) {
                return name.substring(prefix.length(), name.length() - 'BeanInfo'.length())
            }
        }
        if (name.endsWith('BeanInfo') && name.length() > 'BeanInfo'.length()) {
            return name.substring(0, name.length() - 'BeanInfo'.length())
        }
        if (name.endsWith('Customizer') && name.length() > 'Customizer'.length()) {
            return name.substring(0, name.length() - 'Customizer'.length())
        }
        return null
    }

    private static boolean isGenerated(String name) {
        return simpleName(name) ==~ /Script\d+/
    }

    private static boolean isNamedScript(String name) {
        String simple = simpleName(name)
        return simple == 'Groovy8113Named' || name.contains('Groovy8113Named')
    }

    private static String simpleName(String name) {
        int dot = name.lastIndexOf('.')
        String simple = dot < 0 ? name : name.substring(dot + 1)
        int dollar = simple.indexOf('$')
        return dollar < 0 ? simple : simple.substring(0, dollar)
    }

    private static Set<String> lockKeys() {
        Set<String> keys = new TreeSet<>()
        boolean readable = false
        ClassLoader current = Groovy8113.class.classLoader
        while (current != null) {
            Set<String> map = ParallelLockMapKeys.copy(current)
            if (map != null) {
                readable = true
                String loaderName = current.getClass().getName() + '@' + System.identityHashCode(current)
                for (String name : map) {
                    keys.add(loaderName + '|' + name)
                }
            }
            current = current.parent
        }
        ClassLoader context = Thread.currentThread().contextClassLoader
        while (context != null) {
            Set<String> map = ParallelLockMapKeys.copy(context)
            if (map != null) {
                readable = true
                String loaderName = context.getClass().getName() + '@' + System.identityHashCode(context)
                for (String name : map) {
                    keys.add(loaderName + '|' + name)
                }
            }
            context = context.parent
        }
        return readable ? keys : null
    }
}
