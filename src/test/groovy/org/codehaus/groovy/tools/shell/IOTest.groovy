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

    private static final String WARNING = "WARN [${AnsiProbe.name}] probe"

    @Test
    void testStreamsOtherThanStandardOutputGetPlainText() {
        def out = new ByteArrayOutputStream()
        def io = new IO(new ByteArrayInputStream(new byte[0]), out, new ByteArrayOutputStream())

        assertFalse(io.ansiSupported)
        logWarning(io)
        assertEquals("WARN [${IOTest.name}] probe${System.lineSeparator()}".toString(), out.toString())
    }

    @Test
    void testRedirectedStandardOutputGetsPlainTextWithoutOpeningTheConsole() {
        def result = runProbe([], ['console'])

        assertTrue(result.out.contains('ansiSupported=false'), result.toString())
        assertTrue(result.out.contains(WARNING), result.toString())
        assertFalse(result.out.contains(ESC), result.toString())
        assertTrue(result.out.contains('console=none'), result.toString())
        assertFalse(result.err.contains('restricted method'), result.toString())
    }

    // a terminal cannot be attached to a forked JVM here, so jansi.mode=force stands in for one
    @Test
    void testStandardOutputRendersAnsiWhenJansiRendersIt() {
        def result = runProbe(['-Djansi.mode=force'], ['console'])

        assertTrue(result.out.contains('ansiSupported=true'), result.toString())
        assertTrue(result.out.contains("${ESC}1;31mWARN${ESC}m [${AnsiProbe.name}] probe".toString()), result.toString())
        assertTrue(result.out.contains('console=opened'), result.toString())
    }

    @Test
    void testConsoleIsOpenedOnlyByTheFirstColoredLine() {
        def result = runProbe(['-Djansi.mode=force'], ['console', 'noLog'])

        assertTrue(result.out.contains('ansiSupported=true'), result.toString())
        assertTrue(result.out.contains('console=none'), result.toString())
    }

    @Test
    void testStandardOutputReplacedBeforeTheFirstLineGetsNoConsole() {
        def result = runProbe(['-Djansi.mode=force'], ['console', 'replaceOutAfterIO'])

        assertTrue(result.out.contains('console=none'), result.toString())
        assertTrue(result.out.contains(WARNING), result.toString())
        assertFalse(result.out.contains(ESC), result.toString())
    }

    @Test
    void testStripModeDoesNotOpenTheConsole() {
        def result = runProbe(['-Djansi.mode=strip'], ['console'])

        assertTrue(result.out.contains('ansiSupported=false'), result.toString())
        assertTrue(result.out.contains('console=none'), result.toString())
    }

    // the Ant task's case: a host replaced System.out before Logger created its IO
    @Test
    void testLoggerWritesThroughAStandardOutputReplacedBeforehand() {
        def result = runProbe(['-Djansi.mode=force'], ['replaceOut'])

        assertTrue(result.out.contains('ansiSupported=false'), result.toString())
        assertTrue(result.out.contains("captured=${WARNING}".toString()), result.toString())
        assertTrue(result.out.contains('console=none'), result.toString())
    }

    @Test
    void testStandardOutputGetsPlainTextWithoutJansi() {
        def classPath = System.getProperty('java.class.path').split(File.pathSeparator)
                .findAll { !new File(it).name.startsWith('jansi') }
        def result = runProbe(['-Djansi.mode=force'], ['console'], classPath.join(File.pathSeparator))

        assertTrue(result.out.contains('console=jansi-absent'), result.toString())
        assertTrue(result.out.contains('ansiSupported=false'), result.toString())
        assertTrue(result.out.contains(WARNING), result.toString())
        assertFalse(result.out.contains(ESC), result.toString())
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

    private static Map<String, Object> runProbe(List<String> jvmArgs, List<String> probeArgs,
                                                String classPath = System.getProperty('java.class.path')) {
        def javaBin = new File(System.getProperty('java.home'), 'bin/java').absolutePath
        def command = [javaBin, *jvmArgs, '-cp', classPath, AnsiProbe.name, *probeArgs]
        def proc = command.execute()
        def out = new StringBuilder(), err = new StringBuilder()
        proc.waitForProcessOutput(out, err)
        [exit: proc.exitValue(), out: out.toString(), err: err.toString()]
    }

    /**
     * Logs one warning through {@link Logger} on the JVM's own standard streams, then reports what happened.
     * Arguments: {@code console} logs through {@link IO#forConsole()} rather than the IO the Logger creates
     * itself; {@code replaceOut} replaces {@code System.out} with a buffer first, as a host such as Ant does;
     * {@code replaceOutAfterIO} does so after creating the IO but before the first line; {@code noLog} logs nothing.
     */
    static class AnsiProbe {
        static void main(String[] args) {
            def stdout = System.out
            def captured = null
            if ('replaceOut' in args) {
                captured = new ByteArrayOutputStream()
                System.setOut(new PrintStream(captured, true))
            }
            if ('console' in args) {
                Logger.io = IO.forConsole()
            }
            if ('replaceOutAfterIO' in args) {
                captured = new ByteArrayOutputStream()
                System.setOut(new PrintStream(captured, true))
            }
            if ('noLog' in args) {
                Logger.io = Logger.io ?: new IO()
            } else {
                Logger.create(AnsiProbe).warn('probe')
            }
            Logger.io.flush()
            System.setOut(stdout)
            stdout.println("ansiSupported=${Logger.io.ansiSupported}")
            stdout.println("console=${consoleState()}")
            if (captured != null) {
                stdout.println("captured=${captured}")
            }
            stdout.flush()
        }

        private static String consoleState() {
            try {
                def ansiConsole = Class.forName('org.jline.jansi.AnsiConsole')
                ansiConsole.getMethod('getTerminal').invoke(null) == null ? 'none' : 'opened'
            } catch (ClassNotFoundException ignore) {
                'jansi-absent'
            }
        }
    }
}
