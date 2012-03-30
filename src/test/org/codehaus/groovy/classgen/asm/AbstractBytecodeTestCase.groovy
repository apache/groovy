package org.codehaus.groovy.classgen.asm

import org.codehaus.groovy.control.CompilationUnit
import org.objectweb.asm.util.TraceClassVisitor
import org.objectweb.asm.tree.*
import org.objectweb.asm.*
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.control.CompilerConfiguration
import java.security.CodeSource

/**
 * Abstract test case to extend to check the instructions we generate in the bytecode of groovy programs.
 *
 * @author Guillaume Laforge
 */
abstract class AbstractBytecodeTestCase extends GroovyTestCase {

    Map extractionOptions
    InstructionSequence sequence
    Class clazz

    @Override
    protected void setUp() {
        super.setUp()
        extractionOptions = [method: 'run']
    }


    protected void assertScript(final String script) throws Exception {
        GroovyShell shell = new GroovyShell();
        def unit
        shell.loader = new GroovyClassLoader() {
            @Override
            protected CompilationUnit createCompilationUnit(final CompilerConfiguration config, final CodeSource source) {
                unit = super.createCompilationUnit(config, source)

                unit
            }
        }
        try {
            shell.evaluate(script, getTestClassName());
        } finally {
            if (unit) {
                try {
                    sequence = extractSequence(unit.classes[0].bytes, extractionOptions)
                    if (extractionOptions.print) println sequence
                } catch (e) {
                    // probably an error in the script
                }
            }
        }
    }

    /**
     * Compiles a script into bytecode and returns the decompiled string equivalent using ASM.
     *
     * @param scriptText the script to compile
     * @return the decompiled <code>InstructionSequence</code>
     */
    InstructionSequence compile(Map options=[method:"run"], String scriptText) {
        sequence = null
        clazz = null
        def cu = new CompilationUnit()
        def su = cu.addSource("script", scriptText)
        cu.compile(Phases.CONVERSION)
        if (options.conversionAction!=null) {
            options.conversionAction(su)
        }
        cu.compile(Phases.CLASS_GENERATION)

        cu.classes.each {
            if (it.name==~'.*script') {
                sequence = extractSequence(it.bytes, options)
            }
        }
        if (sequence==null && cu.classes.size()>0) {
            sequence = extractSequence(cu.classes[0].bytes, options)
        }
        cu.classes.each {
            try {
                def dep = cu.classLoader.defineClass(it.name, it.bytes)
                if (Script.class.isAssignableFrom(dep)) {
                    clazz = dep
                }
            } catch (Throwable e) {
                System.err.println(sequence)
                e.printStackTrace()
            }
        }
        return sequence
    }

    InstructionSequence extractSequence(byte[] bytes, Map options=[method:"run"]) {
        InstructionSequence sequence
        def output = new StringWriter()
        def tcf;
        tcf = new TraceClassVisitor(new ClassVisitor(Opcodes.ASM4) {
            MethodVisitor visitMethod(int access, String name, String desc, String signature, String... exceptions) {
                if (options.method == name) {
                    tcf.p.text << '--BEGIN--'
                    def res = super.visitMethod(access, name, desc, signature, exceptions)
                    tcf.p.text << '--END--'
                    res
                } else {
                    null
                }
            }

            FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
                if (options.field == name) {
                    tcf.p.text << '--BEGIN--'
                    def res = super.visitField(access, name, desc, signature, value)
                    tcf.p.text << '--END--'
                    res
                } else {
                    null
                }
            }

        }, new PrintWriter(output))
        def cr = new ClassReader(bytes)
        cr.accept(tcf, 0)

        def code = output.toString()
        sequence = new InstructionSequence(instructions: code.split('\n')*.trim())
        return sequence
    }
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
        def idx = offset
        while (true) {
            idx = indexOf(pattern[0], idx)
            if (idx == -1) break
            // not the first call with offset 0 and check that the next instruction match
            // is the exact following instruction in the pattern and in the bytecode instructions
            if (strict && offset > 0 && idx != offset) return false
            if (hasSequence(pattern.tail(), idx+1, strict)) return true
            idx++
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

    String toSequence() {
        def sb = new StringBuilder()
        instructions*.trim().each {
            sb << "'${it}'," << '\n'
        }
        sb.toString()
    }
}
