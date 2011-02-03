package org.codehaus.groovy.classgen.asm

/**
 * @author Guillaume Laforge
 */
class IfComparisonOnIntTest extends AbstractBytecodeTestCase {

    /**
     * The "optimized" bytecode should contain an int comparison (IF_ICMPGE)
     * after having loaded the int i on the stack, and the constant int 100.
     */
    void assertBytecode() {
        assert compile("""\
            int i = 0
            if (i < 100) println "true"
        """).hasSequence([
                "ILOAD",
                "LDC 100",
                "IF_ICMPGE"
        ])
    }
}
