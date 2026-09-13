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
package org.apache.groovy.groovysh

import org.apache.groovy.groovysh.jline.GroovyEngine
import org.apache.groovy.groovysh.jline.GroovySystemRegistry
import org.jline.builtins.ClasspathResourceUtil
import org.jline.builtins.ConfigurationPath
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.jline.reader.impl.DefaultParser
import org.jline.terminal.Size
import org.jline.terminal.Terminal
import org.jline.terminal.impl.DumbTerminal
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

/**
 * Tests for the public {@link ExtraConsoleCommands} registry. Commands that
 * write through the POSIX context are asserted via the dumb terminal capture;
 * help paths are the portable way to exercise option parsing.
 */
class ExtraConsoleCommandsTest {

    @TempDir
    Path workDir

    private GroovyEngine engine
    private Terminal terminal
    private ByteArrayOutputStream terminalBytes
    private LineReader reader
    private ExtraConsoleCommands extra
    private GroovySystemRegistry system

    @BeforeEach
    void setUp() {
        engine = new GroovyEngine()
        terminalBytes = new ByteArrayOutputStream()
        terminal = new DumbTerminal(
            'groovysh-test', 'dumb',
            new ByteArrayInputStream(new byte[0]),
            terminalBytes,
            StandardCharsets.UTF_8)
        terminal.size = new Size(80, 40)
        reader = LineReaderBuilder.builder()
            .terminal(terminal)
            .parser(new DefaultParser(regexCommand: /\/?[a-zA-Z!]+\S*/))
            .build()
        extra = new ExtraConsoleCommands(workDir, engine, reader)
        def root = ClasspathResourceUtil.getResourcePath(Main.getResource('/nanorc'))
        def configPath = new ConfigurationPath(root, workDir)
        system = new GroovySystemRegistry(reader.parser, terminal, extra::currentDir, configPath).tap {
            setCommandRegistries(extra)
            renameLocal 'exit', '/exit'
            renameLocal 'help', '/help'
        }
    }

    @AfterEach
    void tearDown() {
        system?.close()
        terminal?.close()
    }

    @Test
    void registersTheDocumentedCommandSet() {
        def names = extra.commandNames() as Set
        assert names.containsAll([
            '/clear', '/pwd', '/cd', '/date', '/echo', '/!',
            '/ls', '/wc', '/sort', '/head', '/tail', '/cat', '/grep'
        ])
        assert extra.name() == 'Console Commands'
        assert extra.currentDir() == workDir
        assert ExtraConsoleCommands.POSIX_COMMANDS == ['/ls', '/wc', '/sort', '/head', '/tail', '/cat', '/grep']
    }

    @Test
    void pwdPrintsTheWorkingDirectory() {
        system.execute('/pwd')
        assert terminalOutput().contains(workDir.toString())
    }

    @Test
    void echoWritesItsArguments() {
        system.execute('/echo groovysh-extra')
        assert terminalOutput().contains('groovysh-extra')
    }

    @Test
    void dateWritesSomething() {
        system.execute('/date')
        assert !terminalOutput().trim().isEmpty()
    }

    @Test
    void cdChangesCurrentDirAndPwdBinding() {
        def child = Files.createDirectory(workDir.resolve('child'))
        system.execute("/cd ${forwardSlashes(child)}")
        assert extra.currentDir() == child
        assert engine.get('PWD') == child
    }

    @Test
    void cdToMissingDirectoryLeavesTheWorkingDirectoryUnchanged() {
        try {
            system.execute('/cd /this/path/should/not/exist-groovysh')
        } catch (IOException ignored) {
            // JLine rethrows the exception saved by the command after it returns
        }
        assert extra.currentDir() == workDir
    }

    @Test
    void clearAndClearHelpDoNotThrow() {
        system.execute('/clear')
        system.execute('/clear --help')
    }

    @Test
    void posixHelpAndListingPaths() {
        def file = Files.writeString(workDir.resolve('sample.txt'), 'hello groovysh\n')
        def path = forwardSlashes(file)
        system.execute("/ls ${forwardSlashes(workDir)}")
        system.execute("/cat ${path}")
        system.execute("/grep --color=never hello ${path}")
        system.execute("/head ${path}")
        system.execute("/tail ${path}")
        system.execute("/wc ${path}")
        system.execute("/sort ${path}")
        assert terminalOutput().contains('hello groovysh')
    }

    @Test
    void bangHelpAndEmptyArgs() {
        system.execute('/! --help')
        system.execute('/! -?')
        system.execute('/!')
    }

    @Test
    void bangRunsAPortableEcho() {
        def buf = new ByteArrayOutputStream()
        def old = System.out
        System.out = new PrintStream(buf)
        try {
            system.execute('/! echo groovysh-bang')
        } finally {
            System.out = old
        }
        assert buf.toString().contains('groovysh-bang')
    }

    @Test
    void commandInfoReturnsPosixCommandNames() {
        def info = extra.commandInfo('/pwd')
        assert info instanceof List
        assert !info.isEmpty()
    }

    @Test
    @SuppressWarnings('deprecation')
    void deprecatedNestedClassStillConstructs() {
        def nested = new Main.ExtraConsoleCommands(workDir, engine, reader)
        assert nested.commandNames().contains('/pwd')
        assert nested.currentDir() == workDir
    }

    private String terminalOutput() {
        terminal.writer().flush()
        terminal.flush()
        new String(terminalBytes.toByteArray(), StandardCharsets.UTF_8)
    }

    private static String forwardSlashes(Path path) {
        path.toString().replace('\\', '/')
    }
}
