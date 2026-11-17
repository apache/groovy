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
package org.codehaus.groovy.tools.shell

import org.codehaus.groovy.tools.shell.util.Logger
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertTrue

class IOTest {

    private static final String ESC = '\u001b['

    @Test
    void testStreamsOtherThanStandardOutputGetPlainText() {
        def out = new ByteArrayOutputStream()
        def io = new IO(new ByteArrayInputStream(new byte[0]), out, new ByteArrayOutputStream())

        assertFalse(io.ansiSupported)
        logWarning(io)
        assertEquals("WARN [${IOTest.name}] probe${System.lineSeparator()}".toString(), out.toString())
    }

    @Test
    void testRedirectedStandardOutputGetsPlainText() {
        def result = runProbe()

        assertTrue(result.out.contains('ansiSupported=false'), result.toString())
        assertTrue(result.out.contains("WARN [${AnsiProbe.name}] probe".toString()), result.toString())
        assertFalse(result.out.contains(ESC), result.toString())
    }

    // a terminal cannot be attached to a forked JVM here, so jansi.mode=force stands in for one
    @Test
    void testStandardOutputRendersAnsiWhenJansiRendersIt() {
        def result = runProbe('-Djansi.mode=force')

        assertTrue(result.out.contains('ansiSupported=true'), result.toString())
        assertTrue(result.out.contains("${ESC}1;31mWARN${ESC}m [${AnsiProbe.name}] probe".toString()), result.toString())
    }

    private static void logWarning(IO io) {
        def previous = Logger.io
        Logger.io = io
        try {
            Logger.create(IOTest).warn('probe')
        } finally {
            Logger.io = previous
        }
    }

    private static Map<String, Object> runProbe(String... jvmArgs) {
        def javaBin = new File(System.getProperty('java.home'), 'bin/java').absolutePath
        def command = [javaBin, *jvmArgs, '-cp', System.getProperty('java.class.path'), AnsiProbe.name]
        def proc = command.execute()
        def out = new StringBuilder(), err = new StringBuilder()
        proc.waitForProcessOutput(out, err)
        [exit: proc.exitValue(), out: out.toString(), err: err.toString()]
    }

    /**
     * Logs one warning through an IO on the JVM's own standard streams.
     */
    static class AnsiProbe {
        static void main(String[] args) {
            def io = new IO()
            io.out.println("ansiSupported=${io.ansiSupported}")
            Logger.io = io
            Logger.create(AnsiProbe).warn('probe')
            io.flush()
        }
    }
}
