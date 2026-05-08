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
package org.apache.groovy.groovysh.commands

import org.apache.groovy.groovysh.jline.GroovySystemRegistry
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.function.Supplier

/**
 * Support for testing commands involving {@link GroovySystemRegistry}.
 *
 * The terminal is built explicitly as a {@code dumb} terminal with empty input
 * and a captured byte buffer for output. This keeps tests deterministic across
 * platforms — no TTY probing, no native FFM/JNI bindings, no signal handlers
 * tied to the JVM's actual stdin/stdout.
 *
 * <p>Two output-capture paths are available; pick by where the command writes:
 * <ul>
 *   <li>{@code printer.output} — for commands that produce results via
 *       {@code printer.println(options, object)}. Most {@code GroovyCommands}
 *       commands ({@code /show}, {@code /prnt}, {@code /inspect},
 *       {@code /classloader}, {@code /types}, {@code /methods}, …) take this
 *       path. The captured strings are the {@code object.toString()} forms;
 *       Groovy MetaClass dispatch is preserved, so a Map renders as
 *       {@code [k:v]}.
 *   <li>{@link #terminalOutput()} — for JLine builtins that write directly
 *       through {@code terminal.writer()} (e.g. {@code /help}). Returns the
 *       raw bytes decoded as UTF-8; the dumb terminal also emits a couple of
 *       capability-probe escapes at startup, so prefer substring matches over
 *       full-string compares.
 * </ul>
 *
 * See {@code subprojects/groovy-groovysh/AGENTS.md} for the platform-fragility
 * rationale and the layered test design.
 */
abstract class SystemTestSupport extends ConsoleTestSupport {

    protected GroovySystemRegistry system
    protected Terminal terminal
    private ByteArrayOutputStream terminalBytes

    @BeforeEach
    @Override
    void setUp() {
        super.setUp()
        Supplier workDir = { configPath.getUserConfig('.') }
        terminalBytes = new ByteArrayOutputStream()
        terminal = TerminalBuilder.builder()
                .dumb(true)
                .streams(new ByteArrayInputStream(new byte[0]), terminalBytes)
                .encoding(StandardCharsets.UTF_8)
                .name('groovysh-test')
                .build()
        system = new GroovySystemRegistry(reader.parser, terminal, workDir, configPath).tap {
            setCommandRegistries(console, groovy)
            // Match production wiring: SystemRegistryImpl's built-in commands
            // are renamed to use the leading-slash convention groovysh exposes
            // to users.
            renameLocal 'exit', '/exit'
            renameLocal 'help', '/help'
        }
    }

    @AfterEach
    void tearDownSystem() {
        terminal?.close()
    }

    /**
     * Returns text written to the terminal so far, decoded as UTF-8. The
     * underlying terminal is {@code dumb}, so no ANSI escape sequences are
     * produced — the returned string is plain text suitable for substring
     * assertions.
     */
    protected String terminalOutput() {
        terminal?.writer()?.flush()
        new String(terminalBytes.toByteArray(), StandardCharsets.UTF_8)
    }

    /**
     * Returns a forward-slash form of the supplied path, suitable for
     * interpolating into a {@code system.execute(...)} line. JLine's
     * DefaultParser treats {@code \} as an escape character, so a
     * Windows-native path like {@code C:\Users\runner\…\foo.json} would
     * have its separators eaten before reaching the command. Java NIO
     * accepts forward-slash paths on Windows, so this normalisation
     * works on every platform.
     */
    protected static String forwardSlashes(Path path) {
        path.toString().replace('\\', '/')
    }

}
