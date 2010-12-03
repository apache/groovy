package org.codehaus.groovy.classgen

/**
 * @author Guillaume Laforge
 */
class PrintlnLoadsAConstantTest extends AbstractBytecodeTestCase {
    void assertBytecode() {
        assert compile(''' println "true" ''').hasSequence(['LDC "true"'])
    }
}