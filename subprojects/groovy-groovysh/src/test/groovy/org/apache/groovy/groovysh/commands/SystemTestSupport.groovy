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
import org.jline.terminal.Size
import org.jline.terminal.Terminal
import org.jline.terminal.impl.DumbTerminal
import org.jline.terminal.impl.SixelGraphics
import org.jline.terminal.impl.TerminalGraphics
import org.jline.terminal.impl.TerminalGraphicsManager
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
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
 *       raw bytes decoded as UTF-8; prefer substring matches over full-string
 *       compares to stay robust across JLine cosmetic changes.
 * </ul>
 *
 * See {@code subprojects/groovy-groovysh/AGENTS.md} for the platform-fragility
 * rationale and the layered test design.
 */
abstract class SystemTestSupport extends ConsoleTestSupport {

    protected GroovySystemRegistry system
    protected Terminal terminal
    private ByteArrayOutputStream terminalBytes

    // Force JLine's graphics-protocol detection off for the test lifetime.
    // {@link TerminalGraphicsManager#isGraphicsSupported} would otherwise read
    // host env vars (TERM_PROGRAM, KITTY_WINDOW_ID, ITERM_SESSION_ID,
    // GHOSTTY_RESOURCES_DIR) and probe the underlying PTY, returning true when
    // Gradle is launched from a graphics-capable terminal (WezTerm/Kitty/iTerm2/
    // Ghostty). When true, /img bypasses its summary-line fallback and writes
    // graphics bytes to the terminal directly, which our dumb-terminal capture
    // can't surface — breaking ImgTest assertions.
    //
    // The combo {@code forceProtocol(SIXEL)} + {@code setSixelSupportOverride(false)}
    // narrows {@code getBestProtocol(...)} to only consider Sixel, then forces
    // Sixel to report unsupported — so {@code isGraphicsSupported} returns false
    // regardless of any env-var-driven Kitty/iTerm2 detection.
    @BeforeAll
    static void disableGraphicsProtocolDetection() {
        TerminalGraphicsManager.forceProtocol(TerminalGraphics.Protocol.SIXEL)
        SixelGraphics.setSixelSupportOverride(Boolean.FALSE)
    }

    @AfterAll
    static void restoreGraphicsProtocolDetection() {
        TerminalGraphicsManager.forceProtocol(null)
        SixelGraphics.setSixelSupportOverride(null)
    }

    /**
     * Terminal width reported to JLine. Override in a subclass to vary the
     * per-line padding that {@code printCommandInfo} applies via
     * {@code setLength(terminal().getWidth())}, or to keep the help renderer
     * out of its {@code width == 0} truncation path. Default 80 — any
     * non-zero value avoids the {@code setLength(0)} empty-line bug; the
     * smaller value keeps total bytes per {@code /help} modest, which
     * shrinks the pump-drain race window on graphics-capable host PTYs.
     */
    protected int terminalWidth() { 80 }

    /**
     * Terminal height reported to JLine. Override in a subclass to flip
     * {@code helpTopic}'s {@code withInfo = commands.size() < getHeight()}
     * switch (verbose info-per-line vs compact multi-column). Default 40 —
     * comfortably above the test setup's registered command count, so the
     * verbose format triggers consistently. Drop below the command count to
     * force the compact format (see {@code HelpCompactCommandTest}).
     */
    protected int terminalHeight() { 40 }

    @BeforeEach
    @Override
    void setUp() {
        super.setUp()
        Supplier workDir = { configPath.getUserConfig('.') }
        terminalBytes = new ByteArrayOutputStream()
        // Instantiate DumbTerminal directly rather than going through TerminalBuilder.
        // TerminalBuilder's type('dumb') hint is advisory: when JLine's native
        // bindings (jline-terminal-jni on JDK 17/18, jline-terminal-ffm on JDK 22+)
        // can attach to the inherited TTY, the builder may still create a
        // PosixPtyTerminal — even with explicit streams — and route writes through
        // an asynchronous output-pump thread. That pump's drain timing races with
        // terminalOutput() on certain CI runners (consistently reproduces on
        // Linux + JDK 17/18) and produces mid-line-truncated captures. DumbTerminal
        // is final, has no native code, no pump, and writes synchronously straight
        // through PrintWriter → ByteArrayOutputStream — eliminating the race.
        // An explicit Size avoids JLine's 0x0 path which truncates via setLength(0).
        terminal = new DumbTerminal(
                'groovysh-test',
                'dumb',
                new ByteArrayInputStream(new byte[0]),
                terminalBytes,
                StandardCharsets.UTF_8)
        terminal.setSize(new Size(terminalWidth(), terminalHeight()))
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
        terminal?.flush()
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
