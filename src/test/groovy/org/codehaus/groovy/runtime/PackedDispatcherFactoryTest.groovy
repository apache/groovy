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

import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Phases
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.ResourceLock
import org.junit.jupiter.api.parallel.Resources
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Handle
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertThrows

/**
 * The packed-closure dispatcher linkage (GROOVY-12227): the hosting class's compiler-emitted
 * {@code $packedDispatchersFactory$} builds the bundle from bytecode-level
 * {@code LambdaMetafactory} sites, invoked once through
 * {@link GeneratedDispatcher#bootstrap}. Exercises every dispatch shape through that linkage,
 * including the transparent propagation of checked exceptions the dispatch interfaces do not
 * declare, and repeats both through the legacy three-argument bootstrap kept for class files
 * emitted by earlier 6.0 pre-releases (recreated by downgrading the accessor's bootstrap
 * reference, the only difference between the two class-file formats).
 */
@ResourceLock(Resources.SYSTEM_PROPERTIES)
final class PackedDispatcherFactoryTest {

    /** Exercises every dispatch shape: array (3 values), arity-1, arity-2, and a checked throw. */
    private static final String SRC = '''
        class Host {
            static List<String> run() {
                def results = []
                def one = { int a -> a * 2 }                          // arity-1 table
                def two = { int a, int b -> a + b }                   // arity-2 table
                def three = { int a, int b, int c -> a + b + c }      // array table
                results << one(21).toString()
                results << two(20, 22).toString()
                results << three(10, 14, 18).toString()
                results << [1, 2, 3].collect { it + 1 }.toString()    // through the GDK
                results
            }
            static void boom() {
                def thrower = { throw new java.io.IOException('checked, undeclared') }
                thrower()
            }
        }
    '''

    private static Class parsePacked() {
        withPacking {
            def loader = new GroovyClassLoader()
            def host = loader.parseClass(SRC, 'Host.groovy')
            assert host.declaredMethods.any { it.name == '$packedDispatch$' } : 'packing did not engage'
            assert host.declaredMethods.any { it.name == '$packedDispatchersFactory$' } : 'factory not emitted'
            host
        }
    }

    private static <T> T withPacking(Closure<T> work) {
        String previous = System.getProperty(CompilerConfiguration.CLOSURE_PACKING)
        System.setProperty(CompilerConfiguration.CLOSURE_PACKING, 'true')
        try {
            work.call()
        } finally {
            if (previous != null) {
                System.setProperty(CompilerConfiguration.CLOSURE_PACKING, previous)
            } else {
                System.clearProperty(CompilerConfiguration.CLOSURE_PACKING)
            }
        }
    }

    private static final String LEGACY_BSM_DESC =
        '(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;'

    /**
     * Recreates the class-file format of earlier 6.0 pre-releases: the accessor's
     * {@code invokedynamic} referenced the three-argument {@code packedDispatchers} bootstrap
     * with no bootstrap arguments (runtime {@code Lookup.findStatic} over the table methods,
     * which the current format still emits as the factory's implementation methods). Dropping
     * the factory bootstrap argument is the only difference between the two formats.
     */
    private static byte[] toLegacyFormat(final byte[] modern) {
        boolean rewritten = false
        def reader = new ClassReader(modern)
        def writer = new ClassWriter(reader, 0)
        reader.accept(new ClassVisitor(Opcodes.ASM9, writer) {
            @Override
            MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                new MethodVisitor(Opcodes.ASM9, super.visitMethod(access, name, descriptor, signature, exceptions)) {
                    @Override
                    void visitInvokeDynamicInsn(String indyName, String indyDescriptor, Handle bootstrap, Object... bootstrapArguments) {
                        if (bootstrap.name == 'packedDispatchers' && bootstrapArguments.length == 1) {
                            rewritten = true
                            super.visitInvokeDynamicInsn(indyName, indyDescriptor,
                                new Handle(Opcodes.H_INVOKESTATIC, bootstrap.owner, bootstrap.name, LEGACY_BSM_DESC, false))
                        } else {
                            super.visitInvokeDynamicInsn(indyName, indyDescriptor, bootstrap, bootstrapArguments)
                        }
                    }
                }
            }
        }, 0)
        assert rewritten : 'no four-argument packedDispatchers site found to downgrade'
        writer.toByteArray()
    }

    private static Class parsePackedLegacyFormat() {
        Map<String, byte[]> classes = withPacking {
            def cu = new CompilationUnit()
            cu.addSource('Host.groovy', SRC)
            cu.compile(Phases.CLASS_GENERATION)
            cu.classes.collectEntries { [it.name, it.bytes] }
        }
        def loader = new GroovyClassLoader()
        Class host = null
        classes.each { name, bytes ->
            def clazz = loader.defineClass(name, name == 'Host' ? toLegacyFormat(bytes) : bytes)
            if (name == 'Host') host = clazz
        }
        assert host.declaredMethods.any { it.name == '$packedDispatch$' } : 'packing did not engage'
        host
    }

    @Test
    void 'every dispatch shape links and dispatches through the emitted factory'() {
        assertEquals(['42', '42', '42', '[2, 3, 4]'], parsePacked().run())
    }

    @Test
    void 'undeclared checked exceptions propagate unchanged through packed dispatch'() {
        def thrown = assertThrows(IOException) { parsePacked().boom() }
        assertEquals('checked, undeclared', thrown.message)
    }

    @Test
    void 'class files from earlier snapshots link through the legacy three-argument bootstrap'() {
        assertEquals(['42', '42', '42', '[2, 3, 4]'], parsePackedLegacyFormat().run())
    }

    @Test
    void 'undeclared checked exceptions propagate unchanged through the legacy bootstrap'() {
        def thrown = assertThrows(IOException) { parsePackedLegacyFormat().boom() }
        assertEquals('checked, undeclared', thrown.message)
    }
}
