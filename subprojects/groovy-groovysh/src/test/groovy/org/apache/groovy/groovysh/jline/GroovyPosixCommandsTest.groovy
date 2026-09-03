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
package org.apache.groovy.groovysh.jline

import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.function.Function

import static org.junit.jupiter.api.Assumptions.assumeFalse

/**
 * Direct unit tests for static methods in {@link GroovyPosixCommands} — the
 * Apache-licensed fork of JLine's PosixCommands. Bypasses the registry stack
 * and constructs a {@link GroovyPosixContext} directly so each test exercises
 * exactly one command function. JLine refactors PosixCommands frequently;
 * these tests reduce regression risk on bumps.
 */
class GroovyPosixCommandsTest {

    private Terminal terminal
    private ByteArrayOutputStream out
    private ByteArrayOutputStream err
    private Path tempDir

    @BeforeEach
    void setUp() {
        terminal = TerminalBuilder.builder()
                .dumb(true)
                .streams(new ByteArrayInputStream(new byte[0]), new ByteArrayOutputStream())
                .encoding(StandardCharsets.UTF_8)
                .name('groovysh-test')
                .build()
        out = new ByteArrayOutputStream()
        err = new ByteArrayOutputStream()
        tempDir = Files.createTempDirectory('groovysh-test-')
    }

    @AfterEach
    void tearDown() {
        terminal?.close()
        // Clean up any files left in the temp dir, then the dir itself.
        tempDir?.toFile()?.deleteDir()
    }

    private GroovyPosixContext context() {
        new GroovyPosixContext(
                new ByteArrayInputStream(new byte[0]),
                new PrintStream(out, true, StandardCharsets.UTF_8),
                new PrintStream(err, true, StandardCharsets.UTF_8),
                tempDir,
                terminal,
                { name -> null } as Function<String, Object>)
    }

    private String stdout() {
        // .normalize() collapses platform line separators to "\n" so
        // line-aware assertions work uniformly — PrintStream.println uses
        // System.lineSeparator() which is "\r\n" on Windows.
        new String(out.toByteArray(), StandardCharsets.UTF_8).normalize()
    }

    /** A name carrying an escape sequence the terminal would otherwise act on. */
    private static final String HOSTILE_NAME = "ok\u001B[2Ktrap.txt"

    /**
     * What the name looks like once neutralised. Stripping removes the ESC that makes the sequence a
     * command; the printable remainder stays and is inert, which is what upstream does. Note the
     * output legitimately contains other ESCs of its own for colour, so the assertion has to name the
     * injected sequence rather than look for ESC in general.
     */
    private static final String NEUTRALISED_NAME = 'ok[2Ktrap.txt'

    // GROOVY-12341
    @Test
    void lsStripsControlCharactersFromFileNames() {
        assumeFalse(System.getProperty('os.name').containsIgnoreCase('windows'),
                'Windows file names cannot contain control characters')
        Files.writeString(tempDir.resolve(HOSTILE_NAME), 'x\n')
        GroovyPosixCommands.ls(context(), ['/ls'] as Object[])
        def output = stdout()

        assert !output.contains('\u001B[2K')
        assert output.contains(NEUTRALISED_NAME)
    }

    // GROOVY-12341
    @Test
    void lsStripsControlCharactersFromSymlinkTargets() {
        assumeFalse(System.getProperty('os.name').containsIgnoreCase('windows'),
                'Windows file names cannot contain control characters')
        Path target = Files.writeString(tempDir.resolve(HOSTILE_NAME), 'x\n')
        try {
            Files.createSymbolicLink(tempDir.resolve('link.txt'), target)
        } catch (UnsupportedOperationException | IOException ignored) {
            return // symlinks unavailable (e.g. Windows without privilege)
        }
        GroovyPosixCommands.ls(context(), ['/ls', '-l'] as Object[])

        assert !stdout().contains('\u001B[2K')
    }

    // GROOVY-12341
    @Test
    void headAndGrepAndWcStripControlCharactersFromNames() {
        assumeFalse(System.getProperty('os.name').containsIgnoreCase('windows'),
                'Windows file names cannot contain control characters')
        // the same treatment upstream applies to every command that echoes a source name
        Path file = Files.writeString(tempDir.resolve(HOSTILE_NAME), 'alpha\nbeta\n')
        Path plain = Files.writeString(tempDir.resolve('plain.txt'), 'alpha\n')

        GroovyPosixCommands.head(context(), ['/head', file.toString(), plain.toString()] as Object[])
        GroovyPosixCommands.grep(context(), ['/grep', '--color=never', 'alpha', file.toString(), plain.toString()] as Object[])
        GroovyPosixCommands.wc(context(), ['/wc', file.toString()] as Object[])
        def output = stdout()

        assert !output.contains('\u001B[2K')
        assert output.contains(NEUTRALISED_NAME)
    }

    // GROOVY-12341
    @Test
    void ordinaryNamesAreUnchanged() {
        Files.writeString(tempDir.resolve('ordinary-name.txt'), 'x\n')
        GroovyPosixCommands.ls(context(), ['/ls'] as Object[])

        assert stdout().contains('ordinary-name.txt')
    }

    @Test
    void catReadsFileContents() {
        Path file = Files.writeString(tempDir.resolve('hello.txt'), "first line\nsecond line\n")
        GroovyPosixCommands.cat(context(), ['/cat', file.toString()] as Object[])
        def output = stdout()
        assert output.contains('first line')
        assert output.contains('second line')
    }

    @Test
    void catWithNumberFlagPrependsLineNumbers() {
        Path file = Files.writeString(tempDir.resolve('numbered.txt'), "alpha\nbeta\n")
        GroovyPosixCommands.cat(context(), ['/cat', '-n', file.toString()] as Object[])
        def output = stdout()
        // -n produces lines like "     1\talpha". Don't assert on exact spacing
        // (it's right-aligned in 6 columns); check the line numbers and content
        // appear together.
        assert output =~ /1\s*\talpha/
        assert output =~ /2\s*\tbeta/
    }

    @Test
    void grepReturnsOnlyMatchingLines() {
        Path file = Files.writeString(tempDir.resolve('fruits.txt'),
                "apple\nbanana\ncherry\nblueberry\n")
        // --color=never disables ANSI match highlighting so the asserted
        // substrings appear contiguously in the output.
        GroovyPosixCommands.grep(context(), ['/grep', '--color=never', 'b', file.toString()] as Object[])
        def output = stdout()
        assert output.contains('banana')
        assert output.contains('blueberry')
        assert !output.contains('apple')
        assert !output.contains('cherry')
    }

    @Test
    void sortReorderLinesAlphabetically() {
        Path file = Files.writeString(tempDir.resolve('mix.txt'),
                "cherry\napple\nbanana\n")
        GroovyPosixCommands.sort(context(), ['/sort', file.toString()] as Object[])
        def lines = stdout().split('\n').findAll { it }
        assert lines == ['apple', 'banana', 'cherry']
    }

    @Test
    void headDefaultsToFirstTenLines() {
        def content = (1..15).collect { "line${it}" }.join('\n') + '\n'
        Path file = Files.writeString(tempDir.resolve('many.txt'), content)
        GroovyPosixCommands.head(context(), ['/head', file.toString()] as Object[])
        def output = stdout()
        // First ten lines appear; eleventh and beyond don't.
        assert output.contains('line1\n')
        assert output.contains('line10')
        assert !output.contains('line11')
    }
}
