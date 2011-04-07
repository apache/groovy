package org.codehaus.groovy.classgen.asm

/**
 * @author Guillaume Laforge
 */
class BinaryIntOperationsTest extends AbstractBytecodeTestCase {
    
    void testIntPlus() {
        assert compile("""\
            int i = 1
            int j = 2
            int k = i + j
        """).hasSequence([
                "ILOAD",
                "ILOAD",
                "IADD"
        ])
    }
    
    void testIntCompareLessThan() {
        assert compile("""\
            int i = 0
            if (i < 100) println "true"
        """).hasSequence([
                "ILOAD",
                "LDC 100",
                "IF_ICMPGE"
        ])
    }
    
    void testCompareLessThanInClosure() {
        // GROOVY-4741
        assert """
            int a = 0
            [].each {
                if (a < 0) {}
            }
            true
        """
    }
}
