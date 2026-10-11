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
package org.apache.groovy.lsp.internal.util

import org.codehaus.groovy.runtime.DefaultGroovyMethods
import org.junit.jupiter.api.Test
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

import java.lang.ref.WeakReference

final class ReflectionParameterNamesTest {

    static class Holder {
        void ping(String person) {}

        Holder(String person) {}
    }

    @Test
    void readsNamesFromTheLocalVariableTable() {
        def charAt = String.getMethod('charAt', int)
        assert !charAt.parameters[0].namePresent
        assert ReflectionParameterNames.names(charAt) == ['index'] as String[]

        def copy = String.getMethod('copyValueOf', char[])
        def copyNames = ReflectionParameterNames.names(copy)
        assert copyNames.length == 1
        assert copyNames[0] != 'arg0'

        def removeAt = DefaultGroovyMethods.declaredMethods.find { it.name == 'removeAt' && it.parameterCount == 2 }
        assert removeAt != null
        assert ReflectionParameterNames.names(removeAt) == ['self', 'index'] as String[]

        assert ReflectionParameterNames.names(null).length == 0
        assert ReflectionParameterNames.names(String.getMethod('length')).length == 0

        def original = String.getConstructor(String)
        assert ReflectionParameterNames.names(original).length == 1

        def ping = Holder.getDeclaredMethod('ping', String)
        assert ReflectionParameterNames.names(ping) == ['person'] as String[]
        def constructed = Holder.getDeclaredConstructor(String)
        assert ReflectionParameterNames.names(constructed) == ['person'] as String[]

        def holder = ReflectionParameterNames.declaredClasses.find { it.simpleName == 'MethodParameterNames' }
        def anyName = holder.getDeclaredMethod('anyName', String[])
        anyName.accessible = true
        assert anyName.invoke(null, [null] as Object[]) == false
        assert anyName.invoke(null, [[null, null] as String[]] as Object[]) == false
        assert anyName.invoke(null, [[null, 'person'] as String[]] as Object[]) == true

        def readClass = ReflectionParameterNames.getDeclaredMethod('readClass', Class)
        readClass.accessible = true
        assert ((Map) readClass.invoke(null, [int] as Object[])).isEmpty()
        assert ((Map) readClass.invoke(null, [String[]] as Object[])).isEmpty()
        assert ((Map) readClass.invoke(null, [null] as Object[])).isEmpty()

        byte[] bytes = classWithoutDebug('cov/Boom')
        def loader = new ClassLoader(ReflectionParameterNamesTest.classLoader) {
            @Override
            InputStream getResourceAsStream(String name) {
                if (name == 'cov/Boom.class') {
                    return new ByteArrayInputStream(new byte[0])
                }
                super.getResourceAsStream(name)
            }

            Class<?> load(byte[] body) {
                defineClass('cov.Boom', body, 0, body.length)
            }
        }
        def boom = loader.load(bytes)
        def boomPing = boom.getDeclaredMethod('ping', int)
        assert ReflectionParameterNames.names(boomPing) == ['arg0'] as String[]
    }

    @Test
    void cacheDoesNotPinAThrowawayLoader() {
        WeakReference<?>[] refs = ParameterNameLeakProbe.defineAndName(classWithoutDebug('cov/Gone'))
        for (int i = 0; i < 40 && (refs[0].get() != null || refs[1].get() != null); i++) {
            System.gc()
            byte[] pressure = new byte[8 * 1024 * 1024]
            pressure[0] = (byte) i
        }
        // Touch the cache so a cleared weak key is expunged.
        ReflectionParameterNames.names(String.getMethod('length'))
        assert refs[1].get() == null
        assert refs[0].get() == null
    }

    private static byte[] classWithoutDebug(String internalName) {
        def writer = new ClassWriter(0)
        writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, internalName, null, 'java/lang/Object', null)
        def init = writer.visitMethod(Opcodes.ACC_PUBLIC, '<init>', '()V', null, null)
        init.visitCode()
        init.visitVarInsn(Opcodes.ALOAD, 0)
        init.visitMethodInsn(Opcodes.INVOKESPECIAL, 'java/lang/Object', '<init>', '()V', false)
        init.visitInsn(Opcodes.RETURN)
        init.visitMaxs(1, 1)
        init.visitEnd()
        def ping = writer.visitMethod(Opcodes.ACC_PUBLIC, 'ping', '(I)V', null, null)
        ping.visitCode()
        ping.visitInsn(Opcodes.RETURN)
        ping.visitMaxs(0, 2)
        ping.visitEnd()
        writer.visitEnd()
        writer.toByteArray()
    }
}
