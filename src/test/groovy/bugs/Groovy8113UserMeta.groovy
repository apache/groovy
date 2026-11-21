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
import groovy.lang.MetaClass
import groovy.lang.MetaClassRegistry
import groovy.lang.Script
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.tools.GroovyClass
import org.junit.jupiter.api.Test

import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * A caller-chosen script name, and a class declared in that script, still
 * receive a custom MetaClass that was defined with no class resource.
 */
@ForkedJvm(jvmArgs = ['--add-opens=java.base/java.lang=ALL-UNNAMED'])
final class Groovy8113UserMeta {

    private static final String SCRIPT = 'Groovy8113Scripted'
    private static final String DECLARED = 'Groovy8113Declared'
    private static final String SCRIPT_META = 'groovy.runtime.metaclass.' + SCRIPT + 'MetaClass'
    private static final String DECLARED_META = 'groovy.runtime.metaclass.' + DECLARED + 'MetaClass'

    @Test
    void userNamedScriptAndClassDeclaredInItUseCustomMetaClass() {
        // GROOVY-8113
        ClassLoader registryLoader = MetaClassRegistry.classLoader
        assertNull(registryLoader.getResource(SCRIPT_META.replace('.', '/') + '.class'))
        assertNull(registryLoader.getResource(DECLARED_META.replace('.', '/') + '.class'))

        Map<String, byte[]> bytes = compileMetaClasses()
        define(registryLoader, SCRIPT_META, bytes[SCRIPT_META])
        define(registryLoader, DECLARED_META, bytes[DECLARED_META])
        assertNull(registryLoader.getResource(SCRIPT_META.replace('.', '/') + '.class'))
        assertNull(registryLoader.getResource(DECLARED_META.replace('.', '/') + '.class'))

        GroovyShell shell = new GroovyShell(registryLoader)
        Script script = shell.parse('''
            class Groovy8113Declared {
            }
            'ran'
        ''', SCRIPT + '.groovy')
        Class<?> scriptClass = script.getClass()
        Class<?> declared = scriptClass.getClassLoader().loadClass(DECLARED)
        assertTrue(reaches(scriptClass.getClassLoader(), registryLoader))
        assertEquals(SCRIPT, scriptClass.getName())
        assertEquals(DECLARED, declared.getName())

        MetaClass scriptMeta = null
        MetaClass declaredMeta = null
        try {
            scriptMeta = GroovySystem.metaClassRegistry.getMetaClass(scriptClass)
            declaredMeta = GroovySystem.metaClassRegistry.getMetaClass(declared)
            assertEquals(SCRIPT_META, scriptMeta.getClass().getName())
            assertEquals(DECLARED_META, declaredMeta.getClass().getName())
            assertEquals('script-meta', scriptMeta.invokeMethod(script, 'marker', new Object[0]))
            Object instance = declared.getDeclaredConstructor().newInstance()
            assertEquals('declared-meta', declaredMeta.invokeMethod(instance, 'marker', new Object[0]))
        } finally {
            if (scriptMeta != null) {
                GroovySystem.metaClassRegistry.removeMetaClass(scriptClass)
            }
            if (declaredMeta != null) {
                GroovySystem.metaClassRegistry.removeMetaClass(declared)
            }
        }
    }

    @Test
    void callerChosenScriptNameStillUsesCustomMetaClass() {
        // GROOVY-8113
        final String fixed = 'Groovy8113Fixed'
        final String metaName = 'groovy.runtime.metaclass.' + fixed + 'MetaClass'
        ClassLoader registryLoader = MetaClassRegistry.classLoader
        Map<String, byte[]> bytes = compileOne(fixed, 'fixed-meta')
        define(registryLoader, metaName, bytes[metaName])

        GroovyShell shell = new GroovyShell(registryLoader)
        Script script = shell.parse("'ran'", fixed + '.groovy')
        assertEquals(fixed, script.getClass().getName())
        MetaClass meta = null
        try {
            meta = GroovySystem.metaClassRegistry.getMetaClass(script.getClass())
            assertEquals(metaName, meta.getClass().getName())
            assertEquals('fixed-meta', meta.invokeMethod(script, 'marker', new Object[0]))
        } finally {
            if (meta != null) {
                GroovySystem.metaClassRegistry.removeMetaClass(script.getClass())
            }
        }
    }

    private static boolean reaches(ClassLoader start, ClassLoader target) {
        ClassLoader current = start
        while (current != null) {
            if (current.is(target)) {
                return true
            }
            current = current.parent
        }
        return false
    }

    private static Map<String, byte[]> compileOne(String className, String marker) {
        return compileSources(Collections.singletonMap(className, marker))
    }

    private static Map<String, byte[]> compileMetaClasses() {
        Map<String, String> sources = new LinkedHashMap<>()
        sources.put(SCRIPT, 'script-meta')
        sources.put(DECLARED, 'declared-meta')
        return compileSources(sources)
    }

    private static Map<String, byte[]> compileSources(Map<String, String> markers) {
        CompilerConfiguration config = new CompilerConfiguration()
        config.setTargetDirectory((File) null)
        CompilationUnit unit = new CompilationUnit(config, null, new GroovyClassLoader(Groovy8113UserMeta.classLoader, config))
        for (Map.Entry<String, String> entry : markers.entrySet()) {
            unit.addSource(entry.getKey() + 'MetaClass.groovy', metaSource(entry.getKey(), entry.getValue()))
        }
        unit.compile(Phases.CLASS_GENERATION)
        Map<String, byte[]> bytes = new LinkedHashMap<>()
        for (GroovyClass groovyClass : unit.classes) {
            bytes.put(groovyClass.name, groovyClass.bytes)
        }
        return bytes
    }

    private static String metaSource(String className, String marker) {
        return """
            package groovy.runtime.metaclass
            class ${className}MetaClass extends DelegatingMetaClass {
                ${className}MetaClass(MetaClass delegate) {
                    super(delegate)
                }
                @Override
                Object invokeMethod(Object object, String methodName, Object[] arguments) {
                    if ('marker' == methodName) {
                        return '${marker}'
                    }
                    return super.invokeMethod(object, methodName, arguments)
                }
            }
        """
    }

    private static Class<?> define(ClassLoader loader, String name, byte[] bytes) {
        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(ClassLoader, MethodHandles.lookup())
        MethodType type = MethodType.methodType(Class, String, byte[], int, int)
        return (Class<?>) lookup.findVirtual(ClassLoader, 'defineClass', type)
                .invokeWithArguments(loader, name, bytes, 0, bytes.length)
    }
}
