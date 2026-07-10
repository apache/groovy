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
package org.codehaus.groovy.transform

import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Phases
import org.junit.jupiter.api.Test
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * A packed closure's hoisted body must carry the debug metadata a debugger relies on —
 * the packed counterpart of {@code LambdaHoistTest#hoistedLambdaMethodCarriesDebugMetadata}.
 * Because the hoisted method reuses the original AST statements, source positions propagate
 * unchanged; these tests pin that: a {@code LineNumberTable} entry per body line (line
 * breakpoints, single-step and stack frames land on the right source lines) and a
 * {@code LocalVariableTable} naming the captured values, the closure parameters (including
 * implicit {@code it}) and the body locals (the variables view is complete). The hoisted
 * method and all generated dispatch machinery are {@code ACC_SYNTHETIC}, so tools that hide
 * synthetics (member lists, Groovydoc, stubs) do not surface them.
 */
final class PackedClosureDebugMetadataTest {

    @Test
    void hoistedBodyCarriesDebugMetadataStaticProvenPath() {
        // multi-line body, a read-only capture (base), a written capture (total, threaded as a
        // shared Reference but named for the debugger), a typed parameter and body locals
        String src = '''import groovy.transform.CompileStatic
            @CompileStatic
            @groovy.transform.PackedClosures
            class D {
                int sum(List<Integer> xs, int base) {
                    int total = 0
                    xs.each { Integer x ->
                        int a = x + base
                        int b = a * 2
                        total += b
                    }
                    total
                }
            }'''
        byte[] bytes = compiledBytes(src, 'D')
        def method = packedMethods(bytes)
        assertTrue(!method.isEmpty(), 'expected a hoisted $packed$closure$ method on D')
        def info = debugInfo(bytes, method[0])
        // one LineNumberTable entry per body statement (the three lines of the block body)
        assertTrue(info.lineCount >= 3, "expected >=3 line-number entries, got ${info.lineCount}")
        // captures (read-only and written), the typed parameter, and the in-body locals all named
        assertTrue(info.locals.containsAll(['base', 'total', 'x', 'a', 'b']),
                "expected named locals base/total/x/a/b in LocalVariableTable, got ${info.locals}")
        assertTrue(info.synthetic, 'hoisted body should be ACC_SYNTHETIC so tools hide it')
        assertTrue(info.priv, 'hoisted body should be private (exact dispatch, no API surface)')
    }

    @Test
    void hoistedBodyCarriesDebugMetadataDynamicTrustPath() {
        // dynamic (untyped) closure with implicit `it`: the debugger must still see `it` by name
        String src = '''@groovy.transform.PackedClosures
            class E {
                def doubles(List xs) {
                    xs.collect {
                        def twice = it * 2
                        twice
                    }
                }
            }'''
        byte[] bytes = compiledBytes(src, 'E')
        def method = packedMethods(bytes)
        assertTrue(!method.isEmpty(), 'expected a hoisted $packed$closure$ method on E')
        def info = debugInfo(bytes, method[0])
        assertTrue(info.lineCount >= 2, "expected >=2 line-number entries, got ${info.lineCount}")
        assertTrue(info.locals.containsAll(['it', 'twice']),
                "expected named locals it/twice in LocalVariableTable, got ${info.locals}")
        assertTrue(info.synthetic && info.priv, 'hoisted body should be private ACC_SYNTHETIC')
    }

    @Test
    void allDispatchMachineryIsSynthetic() {
        // the dispatch tables and the bundle accessor are implementation detail: every generated
        // member beyond the user's own methods must be ACC_SYNTHETIC so tooling hides it
        String src = '''@groovy.transform.PackedClosures
            class F {
                def a(List xs) { xs.collect { it + 1 } }
                def b(List xs) { int s = 0; xs.each { s += it }; s }
            }'''
        byte[] bytes = compiledBytes(src, 'F')
        def nonSynthetic = []
        new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            MethodVisitor visitMethod(int access, String name, String desc, String sig, String[] exc) {
                if (name.contains('packed') && (access & Opcodes.ACC_SYNTHETIC) == 0) nonSynthetic << name
                return null
            }
        }, ClassReader.SKIP_CODE)
        assertEquals([], nonSynthetic, 'all $packed... machinery should be ACC_SYNTHETIC')
    }

    /** Compiles the source with debug info on and returns the named class's bytes. */
    private static byte[] compiledBytes(String src, String className) {
        def config = new CompilerConfiguration(debug: true)
        def cu = new CompilationUnit(config)
        cu.addSource("${className}.groovy", src)
        cu.compile(Phases.CLASS_GENERATION)
        cu.classes.find { it.name == className }.bytes
    }

    /** Names of the hoisted {@code $packed$closure$} methods in the class. */
    private static List<String> packedMethods(byte[] bytes) {
        List<String> found = []
        new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            MethodVisitor visitMethod(int access, String name, String desc, String sig, String[] exc) {
                if (name.startsWith('$packed$closure$')) found << name
                return null
            }
        }, ClassReader.SKIP_CODE)
        found
    }

    /** LineNumberTable entry count, LocalVariableTable names, synthetic and private flags. */
    private static Map debugInfo(byte[] bytes, String methodName) {
        int[] lineCount = [0]
        List<String> locals = []
        boolean[] synthetic = [false]
        boolean[] priv = [false]
        new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            MethodVisitor visitMethod(int access, String name, String desc, String sig, String[] exc) {
                if (name != methodName) return null
                synthetic[0] = (access & Opcodes.ACC_SYNTHETIC) != 0
                priv[0] = (access & Opcodes.ACC_PRIVATE) != 0
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override
                    void visitLineNumber(int line, Label start) { lineCount[0]++ }
                    @Override
                    void visitLocalVariable(String n, String d, String s, Label st, Label e, int idx) { locals << n }
                }
            }
        }, 0)
        [lineCount: lineCount[0], locals: locals, synthetic: synthetic[0], priv: priv[0]]
    }
}
