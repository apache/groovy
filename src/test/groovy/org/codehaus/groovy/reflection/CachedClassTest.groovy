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
package org.codehaus.groovy.reflection

import org.junit.jupiter.api.Test
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

final class CachedClassTest {

    /**
     * GROOVY-12418: when a class cannot be reflected on because a declared
     * signature names a type the runtime lacks, the empty method list is
     * unavoidable, but it must be reported, naming the class and the cause.
     */
    @Test
    void unreadableDeclaredMethodsAreReported() {
        String name = 'org.codehaus.groovy.reflection.MentionsAMissingType'
        def writer = new ClassWriter(0)
        writer.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, name.replace('.', '/'), null, 'java/lang/Object', null)
        writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, 'take', '(Lno/such/Type;)V', null, null).with {
            visitCode()
            visitInsn(Opcodes.RETURN)
            visitMaxs(0, 1)
            visitEnd()
        }
        writer.visitEnd()
        byte[] bytes = writer.toByteArray()
        def loader = new ClassLoader(getClass().classLoader) {
            @Override
            protected Class<?> findClass(String className) {
                className == name ? defineClass(className, bytes, 0, bytes.length) : super.findClass(className)
            }
        }
        Class<?> broken = loader.loadClass(name)

        List<LogRecord> records = []
        def logger = Logger.getLogger(CachedClass.name)
        def handler = new Handler() {
            @Override void publish(LogRecord record) { records << record }
            @Override void flush() { }
            @Override void close() { }
        }
        logger.addHandler(handler)
        try {
            assertEquals(0, ReflectionCache.getCachedClass(broken).methods.length)
        } finally {
            logger.removeHandler(handler)
        }
        assertTrue(records.any { it.level == Level.WARNING && it.message.contains(name) && it.thrown instanceof NoClassDefFoundError },
                "expected a warning naming $name, got ${records*.message}")
    }
}
