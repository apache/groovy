package org.codehaus.groovy.classgen

class PrintlnLoadsAConstantTest extends AbstractBytecodeTestCase {
    void assertBytecode() {
        assert compile(''' println "true" ''').hasSequence(['LDC "true"'])
    }
}