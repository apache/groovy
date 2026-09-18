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
package org.codehaus.groovy.runtime

import org.junit.jupiter.api.Test
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Handle
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

import java.time.Duration


/**
 * Tests for DefaultGroovyStaticMethods
 */
class DefaultGroovyStaticMethodsTest {
    @Test
    void testCurrentTimeSeconds() {
	    long timeMillis = System.currentTimeMillis()
        long timeSeconds = System.currentTimeSeconds()
        long timeMillis2 = System.currentTimeMillis()
        assert timeMillis/1000 as int <= timeSeconds
        assert timeMillis2/1000 as int >= timeSeconds
    }

    @Test
    void testDumpAll() {
        assert Thread.dumpAll().contains("dumpAll")
    }

    /**
     * GROOVY-12418: a platform without {@code java.management} (Android, a jlinked
     * runtime) refuses reflection over a class whose declared methods mention its
     * types, and then none of the static extension methods can be registered. D8
     * turns a lambda or method reference into a declared method of the class that
     * holds it, so bootstrap arguments count as signatures here too.
     */
    @Test
    void testNoJavaManagementTypesInDeclaredSignatures() {
        def offenders = []
        def stream = DefaultGroovyStaticMethods.getResourceAsStream('DefaultGroovyStaticMethods.class')
        new ClassReader(stream).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                if (descriptor.contains('java/lang/management')) offenders << "$name$descriptor"
                new MethodVisitor(Opcodes.ASM9) {
                    @Override
                    void visitInvokeDynamicInsn(String indyName, String indyDescriptor, Handle bootstrap, Object... bootstrapArguments) {
                        bootstrapArguments.findAll { it instanceof Handle || it instanceof Type }.each {
                            if (it.toString().contains('java/lang/management')) offenders << "$name: $it"
                        }
                    }
                }
            }
        }, ClassReader.SKIP_DEBUG)
        assert offenders.isEmpty()
    }

    @Test
    void testAllThreads() {
        assert Thread.allThreads().stream().anyMatch(t -> 'Finalizer' == t.name)
    }

    @Test
    void testTimedNanos() {
        boolean ran = false
        long elapsed = System.timedNanos { ran = true; (1..1000).sum() }
        assert ran
        assert elapsed >= 0
    }

    @Test
    void testTimedMillis() {
        boolean ran = false
        long elapsed = System.timedMillis { ran = true; (1..1000).sum() }
        assert ran
        assert elapsed >= 0
    }

    @Test
    void testTimed() {
        def t = System.timed { (1..1000).sum() }
        assert t.result == 500500
        assert t.nanos >= 0
        assert t.millis == t.nanos.intdiv(1_000_000)
        assert t.duration == Duration.ofNanos(t.nanos)
    }

    @Test
    void testTimedPropagatesException() {
        def ex = new IllegalStateException('boom')
        def caught = null
        try {
            System.timed { throw ex }
        } catch (IllegalStateException e) {
            caught = e
        }
        assert caught.is(ex)
    }
}
