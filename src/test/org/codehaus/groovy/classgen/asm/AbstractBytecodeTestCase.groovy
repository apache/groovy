package org.codehaus.groovy.classgen.asm

import org.codehaus.groovy.control.CompilationUnit
import org.objectweb.asm.util.TraceClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.ClassReader
import org.codehaus.groovy.control.Phases
import org.objectweb.asm.commons.EmptyVisitor

/**
 * Abstract test case to extend to check the instructions we generate in the bytecode of groovy programs.
 *
 * @author Guillaume Laforge
 */
abstract class AbstractBytecodeTestCase extends GroovyTestCase {

    /**
     * Compiles a script into bytecode and returns the decompiled string equivalent using ASM.
     *
     * @param scriptText the script to compile
     * @return the decompiled <code>InstructionSequence</code>
     */
    InstructionSequence compile(String scriptText) {
        def cu = new CompilationUnit()
        cu.addSource("script", scriptText)
        cu.compile(Phases.CLASS_GENERATION)

        def output = new StringWriter()
        def tcf = new TraceClassVisitor(new PrintWriter(output)) {
            MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                if (name == "run")
                    super.visitMethod(access, name, desc, signature, exceptions)
                else
                    new EmptyVisitor()
            }
        }
        def cr = new ClassReader(cu.classes[0].bytes)
        cr.accept(tcf, 0)

        def code = output.toString()
        def runMethodCode = code[code.indexOf('public run()Ljava/lang/Object;')+31..-3]

        return new InstructionSequence(instructions: runMethodCode.split('\n')*.trim())
    }

    /**
     * Main test method calling the abstract method <code>assertBytecode</code>
     * that all sub-classes must implement.
     */
    void testAssertBytecode() {
        assertBytecode()
    }

    /**
     * Abstract method to be implemented by all sub-classes.
     */
    abstract void assertBytecode()
}

/**
 * A sequence of instruction with matching and strict matching capabilities
 * to find subsequences of bytecode instructions.
 *
 * @author Guillaume Laforge
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
    boolean hasSequence(List<String> pattern, int offset = 0, boolean strict = false) {
        if (pattern.size() == 0) return true

        def idx = indexOf(pattern[0], offset)
        if (idx > -1) {
            // not the first call with offset 0 and check that the next instruction match
            // is the exact following instruction in the pattern and in the bytecode instructions
            if (strict && offset > 0 && idx - offset > 1) {
                return false
            } else {
                return hasSequence(pattern.tail(), idx, strict)
            }
        } else {
            return false
        }
    }

    /**
     * Find a strict sub-sequence of instructions of the list of instructions.
     *
     * @param pattern the list of instructions to find in the bytecode
     * @param offset at which to find the sub-sequence or remaining sub-sequence (start at offset 0)
     * @param strict whether the search should be strict with contiguous instructions (true by default)
     * @return true if a match is found
     */
    boolean hasStrictSequence(List<String> pattern, int offset = 0, boolean strict = true) {
        hasSequence(pattern, offset, strict)
    }

    /**
     * Finds the index of a single instruction in a list of instructions
     * @param singleInst single instruction to find
     * @param offset the offset from which to start the search
     * @return the index of that single instruction if found, -1 otherwise
     */
    private int indexOf(String singleInst, int offset = 0) {
        for (i in offset..<instructions.size()) {
            if (instructions[i].startsWith(singleInst))
                return i
        }
        return -1
    }

    String toString() {
        instructions.join('\n')
    }
}
