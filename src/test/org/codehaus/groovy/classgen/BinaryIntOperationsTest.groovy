package org.codehaus.groovy.classgen

/**
 * @author Guillaume Laforge
 */
class BinaryIntOperationsTest extends AbstractBytecodeTestCase {
    @Override
    void assertBytecode() {
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
}
