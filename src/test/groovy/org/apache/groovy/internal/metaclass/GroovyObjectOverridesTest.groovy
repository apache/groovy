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
import org.junit.jupiter.api.Test
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

import static org.apache.groovy.internal.metaclass.GroovyObjectOverrides.overridesSetProperty
import static org.junit.jupiter.api.Assertions.assertFalse
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

    @Test
    void interfaceDefaultIsNotAnOverride() {
        assertFalse(overridesSetProperty(Plain))
        assertFalse(overridesSetProperty(JavaStyle))
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

    /**
     * A class whose public methods reflection cannot read: in a native image, one without
     * reflection metadata; here, one whose public method names a type absent at run time.
     * Its {@code setProperty} is the interface default, but that cannot be established, so
     * it is reported as an override: a write then goes to the object's own
     * {@code setProperty}, whichever implementation that is.
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

        assertTrue(overridesSetProperty(unreadable))
    }
}
