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
import groovy.lang.GroovyShell
import org.junit.jupiter.api.Test
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertSame
import static org.objectweb.asm.Opcodes.ACC_FINAL
import static org.objectweb.asm.Opcodes.ACC_PUBLIC
import static org.objectweb.asm.Opcodes.ACC_STATIC
import static org.objectweb.asm.Opcodes.ALOAD
import static org.objectweb.asm.Opcodes.INVOKESPECIAL
import static org.objectweb.asm.Opcodes.RETURN
import static org.objectweb.asm.Opcodes.V17

/**
 * A parent loader that is not a {@link GroovyClassLoader} can define a
 * binary inner class without exposing a class resource. The child loader
 * still returns that class, and a shell using the parent can compile
 * against it.
 */
@ForkedJvm
final class Groovy8113ParentDefined {

    private static final String OUTER = 'groovy8113.Holder'
    private static final String INNER = 'groovy8113.Holder$Inner'

    @Test
    void parentDefinedInnerClassWithoutResourceLoadsAndCompiles() {
        // GROOVY-8113
        Map<String, byte[]> bytes = new LinkedHashMap<>()
        bytes.put(OUTER, classBytes('groovy8113/Holder', false))
        bytes.put(INNER, classBytes('groovy8113/Holder$Inner', true))

        ClassLoader parent = new DefiningParent(Groovy8113ParentDefined.class.classLoader, bytes)
        assertNull(parent.getResource('groovy8113/Holder$Inner.class'))
        assertNull(parent.getResource('groovy8113/Holder.class'))

        GroovyClassLoader loader = new GroovyClassLoader(parent)
        assertNull(loader.getResource('groovy8113/Holder$Inner.class'))
        Class<?> inner = loader.loadClass(INNER)
        assertSame(inner, parent.loadClass(INNER))
        assertEquals('ok', inner.getField('VALUE').get(null))

        GroovyShell shell = new GroovyShell(parent)
        assertEquals('ok', shell.evaluate('groovy8113.Holder.Inner.VALUE'))
        assertEquals('ok', shell.evaluate('''
            import groovy8113.Holder.Inner
            Inner.VALUE
        '''))
        assertEquals('ok', shell.evaluate('''
            class Sub extends groovy8113.Holder {
                def v() { Inner.VALUE }
            }
            new Sub().v()
        '''))
    }

    private static byte[] classBytes(String internalName, boolean valueField) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS)
        writer.visit(V17, ACC_PUBLIC, internalName, null, 'java/lang/Object', null)
        writer.visitInnerClass('groovy8113/Holder$Inner', 'groovy8113/Holder', 'Inner', ACC_PUBLIC | ACC_STATIC)
        if (valueField) {
            writer.visitField(ACC_PUBLIC | ACC_STATIC | ACC_FINAL, 'VALUE', 'Ljava/lang/String;', null, 'ok').visitEnd()
        }
        MethodVisitor init = writer.visitMethod(ACC_PUBLIC, '<init>', '()V', null, null)
        init.visitCode()
        init.visitVarInsn(ALOAD, 0)
        init.visitMethodInsn(INVOKESPECIAL, 'java/lang/Object', '<init>', '()V', false)
        init.visitInsn(RETURN)
        init.visitMaxs(0, 0)
        init.visitEnd()
        writer.visitEnd()
        return writer.toByteArray()
    }

    /**
     * Defines the supplied classes and does not serve them as resources.
     */
    private static final class DefiningParent extends ClassLoader {

        private final Map<String, byte[]> bytes

        private DefiningParent(ClassLoader parent, Map<String, byte[]> bytes) {
            super(parent)
            this.bytes = bytes
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            byte[] code = bytes.get(name)
            if (code == null) {
                return super.findClass(name)
            }
            return defineClass(name, code, 0, code.length)
        }
    }
}
