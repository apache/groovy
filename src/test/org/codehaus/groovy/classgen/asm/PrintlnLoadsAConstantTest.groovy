package org.codehaus.groovy.classgen.asm

/**
 * @author Guillaume Laforge
 */
class PrintlnLoadsAConstantTest extends AbstractBytecodeTestCase {
    void testPrintln() {
        assert compile(''' println "true" ''').hasSequence(['LDC "true"'])
    }
}