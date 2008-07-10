package org.codehaus.groovy.control.io

import org.codehaus.groovy.control.CompilerConfiguration

class StringReaderSourceTest extends GroovyTestCase {

    void testFileReaderCanNotBeReopened() {
        def dummyString = "return false"
        def writer = new StringReaderSource( dummyString, CompilerConfiguration.DEFAULT )
        assert writer.canReopenSource()
    }
}