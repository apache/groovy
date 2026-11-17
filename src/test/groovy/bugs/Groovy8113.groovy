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
import groovy.lang.GroovyShell
import groovy.util.GroovyScriptEngine
import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.nio.file.Path

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * Evaluating a script must not ask a parent loader for a generated script
 * name or its companions. A caller-chosen name may be asked for only as a
 * MetaClass, BeanInfo or Customizer companion.
 */
final class Groovy8113 {

    private static final int ITERATIONS = 8

    @Test
    void repeatedEvaluationDoesNotAskParentForGeneratedNames() throws Exception {
        // GROOVY-8113
        RecordingParent parent = new RecordingParent(Groovy8113.class.classLoader)
        GroovyShell shell = new GroovyShell(parent)
        for (int i = 0; i < ITERATIONS; i++) {
            assert shell.evaluate("${i} + 1", "Groovy8113Shell_${i}.groovy") == i + 1
            assert new GroovyShell(parent).evaluate("${i} + 2") == i + 2
            Class<?> parsed = new GroovyClassLoader(parent).parseClass("${i} + 4")
            assert parsed.name ==~ /Script_[0-9a-fA-F]+/
            assert ((Script) parsed.getDeclaredConstructor().newInstance()).run() == i + 4
        }
        assert shell.evaluate('''
            package pkg
            class Groovy8113Box { String item = 'x' }
            def closure = { it }
            assert closure('z') == 'z'
            new Groovy8113Box().item
        ''', 'pkg/Groovy8113Holder.groovy') == 'x'

        Path root = Files.createTempDirectory('groovy8113')
        try {
            Path nested = root.resolve('pkg')
            Files.createDirectories(nested)
            for (int i = 0; i < ITERATIONS; i++) {
                Files.writeString(root.resolve("Groovy8113_${i}.groovy"), "${i} + 3\n")
                GroovyScriptEngine engine = new GroovyScriptEngine(root.toUri().toString(), parent)
                assert engine.run("Groovy8113_${i}.groovy", '') == "${i + 3}"
            }
            Files.writeString(nested.resolve('Groovy8113P.groovy'), 'package pkg\n4 + 5\n')
            GroovyScriptEngine packaged = new GroovyScriptEngine(root.toUri().toString(), parent)
            assert packaged.run('pkg/Groovy8113P.groovy', '') == '9'
        } finally {
            deleteTree(root)
        }

        assertTrue(parent.requested.size() > 0, 'parent loader recorded no loads')
        List<String> unexpected = new ArrayList<>()
        for (String name : parent.requested) {
            if (isGeneratedProbe(name) || isDisallowedUserProbe(name)) {
                unexpected.add(name)
            }
        }
        assert unexpected.isEmpty() : "parent loads that should not happen:\n${unexpected.join('\n')}"
    }

    @Test
    void presentBinaryInnerNameStillLoads() {
        // GROOVY-8113
        GroovyShell shell = new GroovyShell()
        assertEquals(Thread.State, shell.classLoader.loadClass('java.lang.Thread$State'))
        assertEquals(Map.Entry, shell.classLoader.loadClass('java.util.Map$Entry'))
    }

    @Test
    void parentDefinedJavaLangDollarNameStillLoads() {
        // GROOVY-8113
        ClassLoader parent = new ClassLoader(Groovy8113.class.classLoader) {
            @Override
            protected Class<?> findClass(String name) throws ClassNotFoundException {
                if ('java.lang.$Marker' == name) {
                    return MarkerHolder
                }
                return super.findClass(name)
            }
        }
        assertEquals('ok', new GroovyShell(parent).evaluate('''
            this.class.classLoader.loadClass('java.lang.$Marker').getField('VALUE').get(null)
        '''))
    }

    static class MarkerHolder {
        public static final String VALUE = 'ok'
    }

    private static boolean isGeneratedProbe(String name) {
        return isGeneratedRoot(rootSimple(name)) || isGeneratedRoot(rootSimple(companionSubject(name)))
    }

    private static boolean isDisallowedUserProbe(String name) {
        String subject = companionSubject(name)
        if (subject) {
            return false
        }
        return isUserRoot(rootSimple(name))
    }

    private static String companionSubject(String name) {
        if (name == null) {
            return ''
        }
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
        return simple ==~ /Script\d+|script\d+|Script_[0-9a-fA-F]+/
    }

    private static boolean isUserRoot(String simple) {
        return simple ==~ /Groovy8113Shell_\d+|Groovy8113_\d+|Groovy8113P|Groovy8113Holder|Groovy8113Box/
    }

    private static void deleteTree(Path root) {
        if (root == null || !Files.exists(root)) {
            return
        }
        Files.walk(root).sorted(Comparator.reverseOrder()).forEach { Path path ->
            Files.deleteIfExists(path)
        }
    }

    /**
     * Records every class name delegated to this parent.
     */
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
