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

    /**
     * The compiled class resulting from the bytecode compilation.
     * This is typically a Script-derived class and is populated during the compile process.
     */
    Class clazz

    /**
     * The raw bytecode bytes of the compiled class.
     * Populated during compilation to enable detailed bytecode inspection and instruction analysis.
     */
    byte[] classBytes

    /**
     * Configuration options for extracting and analyzing bytecode instructions.
     * Common keys include:
     * <ul>
     *   <li>{@code method}: the method name to extract (default: 'run')</li>
     *   <li>{@code field}: the field name to extract</li>
     *   <li>{@code classNamePattern}: regex pattern to match target class name</li>
     *   <li>{@code print}: whether to print the extracted sequence</li>
     * </ul>
     */
    Map extractionOptions

    /**
     * The sequence of bytecode instructions extracted from the compiled class.
     * Contains the decompiled instructions in human-readable format for pattern matching and assertion.
     */
    InstructionSequence sequence

    @BeforeEach
    void setUp() {
        classBytes = null
        extractionOptions = [method: 'run']
    }

    /**
     * Evaluates a Groovy script and captures its compiled bytecode for inspection.
     * 
     * This method creates a GroovyShell with a custom GroovyClassLoader to intercept
     * the compilation unit during script evaluation. The resulting bytecode is extracted
     * and made available via the {@link #sequence} field according to {@link #extractionOptions}.
     * 
     * @param script the Groovy script source code to compile and evaluate
     * @throws Exception if script evaluation or bytecode capture fails
     */
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

    /**
     * Extracts bytecode instructions from raw class bytes into a human-readable instruction sequence.
     * 
     * Uses ASM's TraceClassVisitor to decompile the bytecode and selectively extracts instructions
     * from the specified method or field according to the options provided. The extraction employs
     * selection markers (--BEGIN-- and --END--) to isolate the target bytecode segment.
     * 
     * @param bytes the raw class bytecode to analyze
     * @param options configuration map with keys:
     *        <ul>
     *          <li>{@code method}: method name to extract instructions from (default: 'run')</li>
     *          <li>{@code field}: field name to extract declarations from</li>
     *        </ul>
     * @return an InstructionSequence containing the extracted bytecode instructions
     * @see InstructionSequence
     */
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

    /**
     * Compiles a Groovy script and identifies any unreachable instructions in the generated bytecode.
     * 
     * This method performs compilation followed by dataflow analysis to detect instructions
     * that are unreachable under all execution paths. This is useful for verifying that the
     * compiler generates optimal code without dead code paths.
     * 
     * @param options configuration map passed to compile, with method name specification
     * @param scriptText the Groovy script source code to compile
     * @return a list of unreachable instructions in the format "index:opcode_name"
     * @throws AssertionError if no class bytes were captured during compilation or method not found
     * @see #findUnreachableInstructions(byte[], String)
     */
    protected List<String> compileAndFindUnreachableInstructions(Map options = [:], final String scriptText) {
        options = [method: 'run', *: options]
        compile(options, scriptText)
        assert classBytes != null: 'No class bytes were captured during compilation'
        findUnreachableInstructions(classBytes, options.method)
    }

    /**
     * Identifies unreachable instructions in compiled bytecode using dataflow analysis.
     * 
     * Performs control flow analysis on the specified method using ASM's Analyzer with
     * BasicInterpreter to determine which instructions can never be reached during normal
     * execution. This is effective for detecting dead code generated by the compiler or
     * verifying optimization correctness.
     * 
     * @param bytes the raw class bytecode containing the method to analyze
     * @param methodName the name of the method within the class to analyze
     * @return a list of unreachable instruction entries, each formatted as "index:opcode_name"
     * @throws AssertionError if the specified method is not found in the class
     */
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

    /**
     * The list of bytecode instructions in decompiled, human-readable format.
     * Each instruction string represents one line of disassembled bytecode,
     * typically including opcode name, operand(s), and optional metadata.
     */
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
     * @return true if a match is found with strict contiguous matching
     * @see #hasSequence(List, int, boolean)
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

    /**
     * Converts the instruction sequence to a formatted string suitable for code generation.
     * 
     * Each instruction is prefixed with a single quote and suffixed with a comma and newline,
     * producing a format that can be used as test pattern literals in Groovy code.
     * 
     * @return a formatted string representation of instructions for test pattern generation
     */
    String toSequence() {
        def sb = new StringBuilder()
        for (insn in instructions) {
            sb << "'$insn'," << '\n'
        }
        sb.toString()
    }

    /**
     * Converts the instruction sequence to a human-readable string representation.
     * 
     * @return all instructions joined with newlines for display and debugging purposes
     */
    String toString() {
        instructions.join('\n')
    }
}
