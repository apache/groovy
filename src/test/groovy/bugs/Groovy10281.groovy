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

import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Phases
import org.junit.Test

@CompileStatic
final class Groovy10281 {
    @Test
    void testLoggingClassGenWithStacktrace() {
        def result = logClassGen(true)
        assert result.contains('// org.codehaus.groovy.classgen.AsmClassGenerator#visitClass:')
        assert result.contains('public class helloWorld extends groovy/lang/Script {')
        assert result.contains('LDC "Hello, world!"')
    }

    @Test
    void testLoggingClassGenWithoutStacktrace() {
        def result = logClassGen(false)
        assert !result.contains('// org.codehaus.groovy.classgen.AsmClassGenerator#visitClass:')
        assert result.contains('public class helloWorld extends groovy/lang/Script {')
        assert result.contains('LDC "Hello, world!"')
    }

    @Test
    void testLoggingClassGenWithStacktraceWithNoDepth() {
        def result = logClassGen(true, 0)
        assert !result.contains('// org.codehaus.groovy.classgen.AsmClassGenerator#visitClass:')
        assert result.contains('public class helloWorld extends groovy/lang/Script {')
        assert result.contains('LDC "Hello, world!"')
    }

    private static String logClassGen(boolean stacktrace, int maxDepth=50) {
        def code = """
                println 'Hello, world!'
            """
        def result = new StringWriter()
        PrintWriter pw = new PrintWriter(result)
        def config = stacktrace ? new CompilerConfiguration(logClassgen: true, logClassgenStackTraceMaxDepth: maxDepth, output: pw)
                                                    : new CompilerConfiguration(logClassgen: true, output: pw)

        new CompilationUnit(config).with {
            addSource 'helloWorld.groovy', code
            compile Phases.CLASS_GENERATION
        }

        pw.close()
        return result.toString()
    }
}
