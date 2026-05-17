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
package org.codehaus.groovy.classgen.asm

import org.apache.groovy.io.StringBuilderWriter
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Phases
import org.junit.jupiter.api.BeforeEach
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.analysis.Analyzer
import org.objectweb.asm.tree.analysis.BasicInterpreter
import org.objectweb.asm.util.Printer
import org.objectweb.asm.util.TraceClassVisitor

import java.security.CodeSource

/**
 * Abstract test case to extend to check the instructions we generate in the bytecode of groovy programs.
 */
abstract class AbstractBytecodeTestCase {

    Class clazz
    byte[] classBytes
    Map extractionOptions
    InstructionSequence sequence

    @BeforeEach
    void setUp() {
        classBytes = null
        extractionOptions = [method: 'run']
    }

    protected void assertScript(final String script) throws Exception {
        CompilationUnit unit = null
        GroovyShell shell = new GroovyShell(new GroovyClassLoader() {
            @Override
            protected CompilationUnit createCompilationUnit(final CompilerConfiguration config, final CodeSource source) {
                unit = super.createCompilationUnit(config, source)
            }
        })
        try {
            shell.evaluate(script, this.class.simpleName)
        } finally {
            if (unit != null) {
                try {
                    def firstClass = unit.classes.find { it.name == unit.firstClassNode.name }
                    captureClassBytesAndSequence(firstClass, extractionOptions)
                    if (extractionOptions.print) println(sequence)
                } catch (e) {
                    // probably an error in the script
                }
            }
        }
    }

    /**
     * Compiles a script into bytecode and returns the instructions for a class.
     *
     * @param scriptText the script to compile
     * @return the decompiled <code>InstructionSequence</code>
     */
    protected InstructionSequence compile(Map options = [:], final String scriptText) {
        options = [method: 'run', classNamePattern: '.*script', *: options]
        sequence = null
        clazz = null
        classBytes = null
        def cu = new CompilationUnit()
        def su = cu.addSource('script', scriptText)
        cu.compile(Phases.CONVERSION)
        if (options.conversionAction != null) {
            options.conversionAction(su)
        }
        cu.compile(Phases.CLASS_GENERATION)

        for (gc in cu.classes) {
            if (gc.name ==~ options.classNamePattern) {
                captureClassBytesAndSequence(gc, options)
            }
        }
        if (sequence == null && cu.classes) {
            def gc = cu.classes.find { it.name == cu.firstClassNode.name }
            captureClassBytesAndSequence(gc, options)
        }
        for (gc in cu.classes) {
            try {
                Class c = cu.classLoader.defineClass(gc.name, gc.bytes)
                if (Script.isAssignableFrom(c)) { clazz = c }
                c.isInterface() // trigger verification
            } catch (VerifyError e) {
                throw e
            } catch (Throwable t) {
                t.printStackTrace()
                System.err.println(sequence)
            }
        }
        sequence
    }

    protected InstructionSequence extractSequence(final byte[] bytes, final Map options = [method: 'run']) {
        def out = new StringBuilderWriter()
        def tcv
        tcv = new TraceClassVisitor(new ClassVisitor(CompilerConfiguration.ASM_API_VERSION) {
            @Override
            MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String... exceptions) {
                if (options.method == name) {
                    return withSelectionMarkers(tcv) {
                        super.visitMethod(access, name, desc, signature, exceptions)
                    }
                }
            }
            @Override
            FieldVisitor visitField(final int access, final String name, final String desc, final String signature, final Object value) {
                if (options.field == name) {
                    return withSelectionMarkers(tcv) {
                        super.visitField(access, name, desc, signature, value)
                    }
                }
            }
        }, new PrintWriter(out))

        new ClassReader(bytes).accept(tcv, 0)
        new InstructionSequence(instructions: out.toString().split('\n')*.trim())
    }

    protected List<String> compileAndFindUnreachableInstructions(Map options = [:], final String scriptText) {
        options = [method: 'run', *: options]
        compile(options, scriptText)
        assert classBytes != null: 'No class bytes were captured during compilation'
        findUnreachableInstructions(classBytes, options.method)
    }

    protected List<String> findUnreachableInstructions(final byte[] bytes, final String methodName) {
        def classNode = new ClassNode()
        new ClassReader(bytes).accept(classNode, ClassReader.EXPAND_FRAMES)

        def methodNode = classNode.methods.find { it.name == methodName }
        assert methodNode != null: "Method '${methodName}' was not found in ${classNode.name}"

        def frames = new Analyzer(new BasicInterpreter()).analyze(classNode.name, methodNode)
        def unreachable = []
        for (int i = 0; i < methodNode.instructions.size(); i += 1) {
            def instruction = methodNode.instructions.get(i)
            if (instruction.opcode >= 0 && frames[i] == null) {
                unreachable << "${i}:${Printer.OPCODES[instruction.opcode]}"
            }
        }
        unreachable
    }

    private void captureClassBytesAndSequence(final gc, final Map options) {
        classBytes = gc.bytes
        sequence = extractSequence(gc.bytes, options)
    }

    private static <T> T withSelectionMarkers(final TraceClassVisitor traceClassVisitor, final Closure<T> visitAction) {
        // The penultimate entry is the method/field body container populated by super.visit*.
        traceClassVisitor.p.text.add(traceClassVisitor.p.text.size() - 2, '--BEGIN--\n')
        try {
            visitAction.call()
        } finally {
            traceClassVisitor.p.text.add('--END--\n')
        }
    }
}

/**
 * A sequence of instruction with matching and strict matching capabilities
 * to find subsequences of bytecode instructions.
 */
class InstructionSequence {

    List<String> instructions

    /**
     * Find a sub-sequence of instructions of the list of instructions.
     *
     * @param pattern the list of instructions to find in the bytecode
     * @param offset at which to find the sub-sequence or remaining sub-sequence (start at offset 0)
     * @param strict whether the search should be strict with contiguous instructions (false by default)
     * @return true if a match is found
     */
    boolean hasSequence(final List<String> pattern, final int offset = 0, final boolean strict = false) {
        if (pattern.isEmpty()) return true
        def idx = offset
        while (true) {
            idx = indexOf(pattern[0], idx)
            if (idx == -1) break
            // not the first call with offset 0 and check that the next instruction match
            // is the exact following instruction in the pattern and in the bytecode instructions
            if (strict && offset > 0 && idx != offset) return false
            if (hasSequence(pattern.tail(), idx + 1, strict)) return true
            idx += 1
        }
        return false
    }

    /**
     * Find a strict sub-sequence of instructions of the list of instructions.
     *
     * @param pattern the list of instructions to find in the bytecode
     * @param offset at which to find the sub-sequence or remaining sub-sequence (start at offset 0)
     * @param strict whether the search should be strict with contiguous instructions (true by default)
     * @return true if a match is found
     */
    boolean hasStrictSequence(final List<String> pattern, final int offset = 0) {
        hasSequence(pattern, offset, true)
    }

    /**
     * Finds the index of a single instruction in a list of instructions.
     *
     * @param singleInst single instruction to find
     * @param offset the offset from which to start the search
     * @return the index of that single instruction if found, -1 otherwise
     */
    private int indexOf(final String singleInst, final int offset = 0) {
        for (i in offset..<instructions.size()) {
            if (instructions[i].startsWith(singleInst)) {
                return i
            }
        }
        return -1
    }

    String toSequence() {
        def sb = new StringBuilder()
        for (insn in instructions) {
            sb << "'$insn'," << '\n'
        }
        sb.toString()
    }

    String toString() {
        instructions.join('\n')
    }
}
