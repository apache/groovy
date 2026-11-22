/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package bugs

import org.codehaus.groovy.control.CompilerConfiguration
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import static groovy.test.GroovyAssert.assertScript

/**
 * GROOVY-8462: {@code @CompileStatic} assignment of a non-String value
 * to a {@code String} local must not emit a call to
 * {@code ShortTypeHandling.castToString()}.
 */
final class Groovy8462 {

    @TempDir
    File tempDir

    @Test
    void testCompileStaticDoesNotEmitCastToString() {
        def src = new File(tempDir, 'Test.groovy')
        src.text = '''
            import groovy.transform.CompileStatic
            @CompileStatic
            class Test {
                static Integer getVal() { 0 }
                static main(args) {
                    String s = getVal()
                    assert s == '0'
                }
            }
        '''
        def config = new CompilerConfiguration(targetDirectory: tempDir)
        def gcl = new GroovyClassLoader(this.class.classLoader, config)
        gcl.parseClass(src)
        def classFile = new File(tempDir, 'Test.class')
        assert classFile.file
        def bytes = classFile.bytes
        def text = new String(bytes, 'ISO-8859-1')
        assert !text.contains('ShortTypeHandling')
        assert !text.contains('castToString')
        new GroovyShell().evaluate(src.text + '\nTest.main()')
    }

    @Test
    void testCompileStaticIntegerToStringAssignmentRuns() {
        assertScript '''
            import groovy.transform.CompileStatic
            @CompileStatic
            class Test {
                static Integer getVal() { 0 }
                static main(args) {
                    String s = getVal()
                    assert s == '0'
                }
            }
        '''
    }
}
