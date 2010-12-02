package org.codehaus.groovy.classgen

class PrintlnLoadsAConstantTest extends BytecodeAbstractTestCase {
    void assertBytecode() {
        assert compile(''' println "true" ''').hasSequence(['LDC "true"'])
    }
}