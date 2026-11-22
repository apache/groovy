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

import groovy.lang.GroovyClassLoader
import org.junit.jupiter.api.Test

import javax.script.ScriptContext
import javax.script.ScriptEngine
import javax.script.SimpleScriptContext

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * Repeated {@code ScriptEngine.eval} must not ask a parent loader for a
 * generated script name. A script whose file name was supplied may be asked
 * for only as a MetaClass, BeanInfo or Customizer companion.
 */
final class Groovy8113 {

    private static final int ITERATIONS = 8

    @Test
    void repeatedEvalDoesNotAskParentForGeneratedNames() {
        // GROOVY-8113
        RecordingParent parent = new RecordingParent(Groovy8113.class.classLoader)
        ScriptEngine engine = new GroovyScriptEngineImpl(new GroovyClassLoader(parent))
        for (int i = 0; i < ITERATIONS; i++) {
            assertEquals(i + 1, engine.eval("${i} + 1"))
        }

        SimpleScriptContext named = new SimpleScriptContext()
        named.setAttribute(ScriptEngine.FILENAME, 'Groovy8113Named.groovy', ScriptContext.ENGINE_SCOPE)
        named.setBindings(engine.createBindings(), ScriptContext.ENGINE_SCOPE)
        assertEquals(3, engine.eval('def closure = { it }; assert closure(3) == 3; 1 + 2', named))

        assertTrue(parent.requested.size() > 0, 'parent loader recorded no loads')
        List<String> unexpected = new ArrayList<>()
        for (String name : parent.requested) {
            if (isGeneratedProbe(name) || isDisallowedNamedProbe(name)) {
                unexpected.add(name)
            }
        }
        assert unexpected.isEmpty() : "parent loads that should not happen:\n${unexpected.join('\n')}"
    }

    private static boolean isGeneratedProbe(String name) {
        return isGeneratedRoot(rootSimple(name)) || isGeneratedRoot(rootSimple(companionSubject(name)))
    }

    private static boolean isDisallowedNamedProbe(String name) {
        if (companionSubject(name)) {
            return false
        }
        String simple = rootSimple(name)
        return simple == 'Groovy8113Named' || name.contains('Groovy8113Named')
    }

    private static String companionSubject(String name) {
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
        return ''
    }

    private static String rootSimple(String name) {
        if (name == null || name.isEmpty()) {
            return ''
        }
        int dot = name.lastIndexOf('.')
        String simple = dot < 0 ? name : name.substring(dot + 1)
        int dollar = simple.indexOf('$')
        return dollar < 0 ? simple : simple.substring(0, dollar)
    }

    private static boolean isGeneratedRoot(String simple) {
        return simple ==~ /Script\d+/
    }

    private static final class RecordingParent extends ClassLoader {
        private final List<String> requested = Collections.synchronizedList(new ArrayList<>())

        private RecordingParent(ClassLoader parent) {
            super(parent)
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            requested.add(name)
            return super.loadClass(name, resolve)
        }
    }
}
