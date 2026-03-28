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

import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Phases
import org.junit.jupiter.api.Test

import java.util.logging.Handler
import java.util.logging.LogRecord
import java.util.logging.Logger

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
        def result = new StringBuilder()

        // Capture System.Logger output via JUL handler
        def loggerName = 'org.codehaus.groovy.classgen.asm.util.LoggableTextifier'
        def julLogger = Logger.getLogger(loggerName)
        def originalLevel = julLogger.level
        julLogger.level = java.util.logging.Level.ALL
        def handler = new Handler() {
            void publish(LogRecord record) { result.append(record.message) }
            void flush() {}
            void close() {}
        }
        handler.level = java.util.logging.Level.ALL
        julLogger.addHandler(handler)

        try {
            def config = stacktrace ? new CompilerConfiguration(logClassgen: true, logClassgenStackTraceMaxDepth: maxDepth)
                                    : new CompilerConfiguration(logClassgen: true)

            new CompilationUnit(config).with {
                addSource 'helloWorld.groovy', code
                compile Phases.CLASS_GENERATION
            }
        } finally {
            julLogger.removeHandler(handler)
            julLogger.level = originalLevel
        }

        return result.toString()
    }
}
