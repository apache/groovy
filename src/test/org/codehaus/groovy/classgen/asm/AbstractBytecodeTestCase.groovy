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

import groovy.test.GroovyTestCase
import org.apache.groovy.io.StringBuilderWriter
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Phases
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.util.TraceClassVisitor

import java.security.CodeSource

/**
 * Abstract test case to extend to check the instructions we generate in the bytecode of groovy programs.
 */
abstract class AbstractBytecodeTestCase extends GroovyTestCase {

    Class clazz
    Map extractionOptions
    InstructionSequence sequence

    @Override
    protected void setUp() {
        super.setUp()
        extractionOptions = [method: 'run']
    }

    @Override
    protected void assertScript(final String script) throws Exception {
        CompilationUnit unit = null
        GroovyShell shell = new GroovyShell(new GroovyClassLoader() {
            @Override
            protected CompilationUnit createCompilationUnit(final CompilerConfiguration config, final CodeSource source) {
                unit = super.createCompilationUnit(config, source)
            }
        })
        try {
            shell.evaluate(script, testClassName)
        } finally {
            if (unit != null) {
                try {
                    sequence = extractSequence(unit.classes[0].bytes, extractionOptions)
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
    InstructionSequence compile(Map options = [:], final String scriptText) {
        options = [method: 'run', classNamePattern: '.*script', *: options]
        sequence = null
        clazz = null
        def cu = new CompilationUnit()
        def su = cu.addSource('script', scriptText)
        cu.compile(Phases.CONVERSION)
        if (options.conversionAction != null) {
            options.conversionAction(su)
        }
        cu.compile(Phases.CLASS_GENERATION)

        for (gc in cu.classes) {
            if (gc.name ==~ options.classNamePattern) {
                sequence = extractSequence(gc.bytes, options)
            }
        }
        if (sequence == null && cu.classes) {
            sequence = extractSequence(cu.classes[0].bytes, options)
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

    InstructionSequence extractSequence(final byte[] bytes, final Map options = [method: 'run']) {
        def out = new StringBuilderWriter()
        def tcv
        tcv = new TraceClassVisitor(new ClassVisitor(CompilerConfiguration.ASM_API_VERSION) {
            @Override
            MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String... exceptions) {
                if (options.method == name) {
                    // last in "tcv.p.text" is a list that will be filled by "super.visit"
                    tcv.p.text.add(tcv.p.text.size() - 2, '--BEGIN--\n')
                    try {
                        super.visitMethod(access, name, desc, signature, exceptions)
                    } finally {
                        tcv.p.text.add('--END--\n')
                    }
                }
            }
            @Override
            FieldVisitor visitField(final int access, final String name, final String desc, final String signature, final Object value) {
                if (options.field == name) {
                    // last in "tcv.p.text" is a list that will be filled by "super.visit"
                    tcv.p.text.add(tcv.p.text.size() - 2, '--BEGIN--\n')
                    try {
                        super.visitField(access, name, desc, signature, value)
                    } finally {
                        tcv.p.text.add('--END--\n')
                    }
                }
            }
        }, new PrintWriter(out))

        new ClassReader(bytes).accept(tcv, 0)
        new InstructionSequence(instructions: out.toString().split('\n')*.trim())
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
