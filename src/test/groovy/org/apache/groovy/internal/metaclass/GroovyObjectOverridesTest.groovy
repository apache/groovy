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
package org.apache.groovy.internal.metaclass

import groovy.transform.CompileStatic
import groovy.transform.Internal
import org.junit.jupiter.api.Test
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

import static org.apache.groovy.internal.metaclass.GroovyObjectOverrides.hasCustomGetProperty
import static org.apache.groovy.internal.metaclass.GroovyObjectOverrides.overridesGetProperty
import static org.apache.groovy.internal.metaclass.GroovyObjectOverrides.overridesSetProperty
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertThrows
import static org.junit.jupiter.api.Assertions.assertTrue

final class GroovyObjectOverridesTest {

    static class Plain {
        def name
    }

    static class Overriding {
        void setProperty(String name, Object value) { }
    }

    static class InheritsOverride extends Overriding {
    }

    static class RedeclaresOverride extends Overriding {
        @Override
        void setProperty(String name, Object value) { }
    }

    static class JavaStyle extends GroovyObjectSupport {
    }

    static class Reading {
        Object getProperty(String name) { name }
    }

    static class InheritsReading extends Reading {
    }

    static class RedeclaresReading extends Reading {
        @Override
        Object getProperty(String name) { name }
    }

    static class InheritsJavaReading extends Expando {
    }

    static class MarkedInternal {
        @Internal
        Object getProperty(String name) { name }
    }

    static class InheritsMarkedInternal extends MarkedInternal {
    }

    interface DefaultsElsewhere extends GroovyObject {
        default Object getProperty(String name) { name }
        default void setProperty(String name, Object value) { }
    }

    interface ExtendsDefaultsElsewhere extends DefaultsElsewhere {
    }

    interface InternalDefaultElsewhere extends GroovyObject {
        @Internal
        default Object getProperty(String name) { name }
    }

    static class TakesDefaults implements DefaultsElsewhere {
    }

    static class InheritsTakesDefaults extends TakesDefaults {
    }

    static class TakesThroughSubInterface implements ExtendsDefaultsElsewhere {
    }

    static class TakesInternalDefault implements InternalDefaultElsewhere {
    }

    static class Intercepting implements GroovyInterceptable {
    }

    @Test
    void groovyObjectDefaultIsNotAnOverride() {
        // GroovyInterceptable extends GroovyObject without redeclaring its defaults
        assertEquals(GroovyObject, Intercepting.getMethod('setProperty', String, Object).declaringClass)
        assertEquals(GroovyObject, Intercepting.getMethod('getProperty', String).declaringClass)
        for (type in [Plain, JavaStyle, Intercepting]) {
            assertFalse(overridesSetProperty(type), type.name)
            assertFalse(overridesGetProperty(type), type.name)
            assertFalse(hasCustomGetProperty(type), type.name)
        }
    }

    /**
     * The {@code GroovyObject} default is not synthetic, so {@code hasCustomGetProperty}
     * tells it from a custom {@code getProperty} only by its {@code @Internal}, which it must
     * find at run time.
     */
    // GROOVY-12489
    @Test
    void groovyObjectDefaultGetPropertyCarriesInternalAtRunTime() {
        def method = GroovyObject.getMethod('getProperty', String)
        assertTrue(method.isDefault())
        assertFalse(method.isSynthetic())
        assertNotNull(method.getAnnotation(Internal))
    }

    /**
     * A default method of another interface is an override, like a declared method: only
     * {@code GroovyObject}'s own default hands the access to the metaclass.
     */
    // GROOVY-12489
    @Test
    void defaultOfAnotherInterfaceIsAnOverride() {
        for (type in [TakesDefaults, InheritsTakesDefaults, TakesThroughSubInterface]) {
            for (method in [type.getMethod('getProperty', String), type.getMethod('setProperty', String, Object)]) {
                assertTrue(method.isDefault(), method.toString())
                assertEquals(DefaultsElsewhere, method.declaringClass, method.toString())
            }
            assertTrue(overridesSetProperty(type), type.name)
            assertTrue(overridesGetProperty(type), type.name)
            assertTrue(hasCustomGetProperty(type), type.name)
        }
    }

    @Test
    void declaredSetPropertyIsAnOverride() {
        assertTrue(overridesSetProperty(Overriding))
        assertTrue(overridesSetProperty(Script)) // writes to its binding
    }

    @Test
    void inheritedSetPropertyIsAnOverride() {
        assertTrue(overridesSetProperty(InheritsOverride))
    }

    @Test
    void redeclaredSetPropertyIsAnOverride() {
        assertTrue(overridesSetProperty(RedeclaresOverride))
    }

    @Test
    void declaredGetPropertyIsAnOverride() {
        for (type in [Reading, Script, Expando, ConfigObject, Closure, Binding]) {
            assertTrue(overridesGetProperty(type), type.name)
            assertTrue(hasCustomGetProperty(type), type.name)
        }
    }

    @Test
    void inheritedGetPropertyIsAnOverride() {
        for (type in [InheritsReading, InheritsJavaReading]) {
            assertTrue(overridesGetProperty(type), type.name)
            assertTrue(hasCustomGetProperty(type), type.name)
        }
    }

    @Test
    void redeclaredGetPropertyIsAnOverride() {
        assertTrue(overridesGetProperty(RedeclaresReading))
        assertTrue(hasCustomGetProperty(RedeclaresReading))
    }

    /**
     * The two read criteria differ on purpose: a {@code getProperty} that is synthetic or
     * marked {@code @Internal}, whether declared, inherited, or a default method of another
     * interface, is taken for a generated one, like the {@code GroovyObject} default (which
     * carries {@code @Internal} for this reason), by property-read call sites, but not by
     * {@code ScriptBytecodeAdapter.getProperty}.
     */
    @Test
    @CompileStatic
    void generatedGetPropertyIsAnOverrideButNotCustom() {
        String name = 'org.apache.groovy.internal.metaclass.SyntheticGetProperty'
        def writer = new ClassWriter(0)
        writer.visit(Opcodes.V17, Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, name.replace('.', '/'), null,
                'java/lang/Object', ['groovy/lang/GroovyObject'] as String[])
        writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT | Opcodes.ACC_SYNTHETIC, 'getProperty',
                '(Ljava/lang/String;)Ljava/lang/Object;', null, null).visitEnd()
        writer.visitEnd()
        byte[] bytes = writer.toByteArray()
        String subName = 'org.apache.groovy.internal.metaclass.InheritsSyntheticGetProperty'
        def subWriter = new ClassWriter(0)
        subWriter.visit(Opcodes.V17, Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, subName.replace('.', '/'), null,
                name.replace('.', '/'), null)
        subWriter.visitEnd()
        byte[] subBytes = subWriter.toByteArray()
        def loader = new ClassLoader(getClass().classLoader) {
            @Override
            protected Class<?> findClass(String className) {
                byte[] found = className == name ? bytes : className == subName ? subBytes : null
                found != null ? defineClass(className, found, 0, found.length) : super.findClass(className)
            }
        }
        Class<?> synthetic = loader.loadClass(name)
        Class<?> inheritsSynthetic = loader.loadClass(subName)
        assertTrue(synthetic.getMethod('getProperty', String).isSynthetic())
        assertTrue(inheritsSynthetic.getMethod('getProperty', String).isSynthetic())

        assertTrue(TakesInternalDefault.getMethod('getProperty', String).isDefault())

        for (type in [MarkedInternal, InheritsMarkedInternal, TakesInternalDefault, synthetic, inheritsSynthetic]) {
            assertTrue(overridesGetProperty(type), type.name)
            assertFalse(hasCustomGetProperty(type), type.name)
        }
    }

    /**
     * A class whose public methods reflection cannot read: in a native image, one without
     * reflection metadata; here, one whose public method names a type absent at run time.
     * Its {@code setProperty} and {@code getProperty} are the {@code GroovyObject} defaults,
     * but that cannot be established, so it is reported as overriding both: a write or a read
     * then goes to the object's own method, whichever implementation that is.
     * <p>
     * Statically compiled: a dynamic call on the class would build its metaclass first,
     * which fails the same way.
     */
    @Test
    @CompileStatic
    void unreadableClassIsReportedAsAnOverride() {
        String name = 'org.apache.groovy.internal.metaclass.UnreadableGroovyObject'
        def writer = new ClassWriter(0)
        writer.visit(Opcodes.V17, Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, name.replace('.', '/'), null,
                'java/lang/Object', ['groovy/lang/GroovyObject'] as String[])
        writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, 'take', '(Lno/such/Type;)V', null, null).visitEnd()
        writer.visitEnd()
        byte[] bytes = writer.toByteArray()
        def loader = new ClassLoader(getClass().classLoader) {
            @Override
            protected Class<?> findClass(String className) {
                className == name ? defineClass(className, bytes, 0, bytes.length) : super.findClass(className)
            }
        }
        Class<?> unreadable = loader.loadClass(name)
        assertThrows(NoClassDefFoundError) { unreadable.getMethod('setProperty', String, Object) }
        assertThrows(NoClassDefFoundError) { unreadable.getMethod('getProperty', String) }

        assertTrue(overridesSetProperty(unreadable))
        assertTrue(overridesGetProperty(unreadable))
        assertTrue(hasCustomGetProperty(unreadable))
    }
}
