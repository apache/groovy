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
import groovy.lang.GroovyClassLoader
import groovy.lang.MetaClassRegistry
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.tools.GroovyClass
import org.junit.jupiter.api.Test

import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNull

/**
 * A custom MetaClass that has been defined, but has no {@code .class}
 * resource, is still the MetaClass of its subject.
 */
@ForkedJvm(jvmArgs = ['--add-opens=java.base/java.lang=ALL-UNNAMED'])
final class Groovy8113pt2 {

    private static final String SUBJECT = 'bugs.Groovy8113DefinedSubject'
    private static final String META = 'groovy.runtime.metaclass.' + SUBJECT + 'MetaClass'

    @Test
    void customMetaClassDefinedWithoutClassResourceIsUsed() {
        // GROOVY-8113
        ClassLoader registryLoader = MetaClassRegistry.classLoader
        assertNull(registryLoader.getResource(META.replace('.', '/') + '.class'))

        Map<String, byte[]> bytes = compile()
        Class<?> customMeta = define(registryLoader, META, bytes[META])
        assertEquals(META, customMeta.getName())
        assertNull(registryLoader.getResource(META.replace('.', '/') + '.class'))
        Class<?> subject = define(registryLoader, SUBJECT, bytes[SUBJECT])

        MetaClass meta = null
        try {
            meta = GroovySystem.metaClassRegistry.getMetaClass(subject)
            assertEquals(META, meta.getClass().getName())
            Object instance = subject.getDeclaredConstructor().newInstance()
            assertEquals('defined-without-resource', meta.invokeMethod(instance, 'marker', new Object[0]))
        } finally {
            if (meta != null) {
                GroovySystem.metaClassRegistry.removeMetaClass(subject)
            }
        }
    }

    private static Map<String, byte[]> compile() {
        CompilerConfiguration config = new CompilerConfiguration()
        config.setTargetDirectory((File) null)
        GroovyClassLoader loader = new GroovyClassLoader(Groovy8113pt2.classLoader, config)
        CompilationUnit unit = new CompilationUnit(config, null, loader)
        unit.addSource(SUBJECT + '.groovy', """
            package bugs
            class Groovy8113DefinedSubject {
            }
        """)
        unit.addSource('Groovy8113DefinedSubjectMetaClass.groovy', """
            package groovy.runtime.metaclass.bugs
            class Groovy8113DefinedSubjectMetaClass extends DelegatingMetaClass {
                Groovy8113DefinedSubjectMetaClass(MetaClass delegate) {
                    super(delegate)
                }
                @Override
                Object invokeMethod(Object object, String methodName, Object[] arguments) {
                    if ('marker' == methodName) {
                        return 'defined-without-resource'
                    }
                    return super.invokeMethod(object, methodName, arguments)
                }
            }
        """)
        unit.compile(Phases.CLASS_GENERATION)
        Map<String, byte[]> bytes = new LinkedHashMap<>()
        for (GroovyClass groovyClass : unit.classes) {
            bytes.put(groovyClass.name, groovyClass.bytes)
        }
        return bytes
    }

    private static Class<?> define(ClassLoader loader, String name, byte[] bytes) {
        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(ClassLoader, MethodHandles.lookup())
        MethodType type = MethodType.methodType(Class, String, byte[], int, int)
        return (Class<?>) lookup.findVirtual(ClassLoader, 'defineClass', type)
                .invokeWithArguments(loader, name, bytes, 0, bytes.length)
    }
}
