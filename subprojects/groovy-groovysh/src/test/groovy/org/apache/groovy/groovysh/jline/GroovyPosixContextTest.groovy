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

import org.jline.builtins.ConfigurationPath
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.function.Function

/**
 * Tests for {@link GroovyPosixContext}'s {@code configPath} plumbing — the
 * field that {@link GroovyPosixCommands#less} reads to wire JLine's
 * syntax-highlighter into {@code /less}. A regression here surfaces as
 * {@code /less} losing colour rendering, which is silent (no error, just
 * plain text).
 */
class GroovyPosixContextTest {

    private Terminal terminal
    private Path tmpDir

    @BeforeEach
    void setUp() {
        terminal = TerminalBuilder.builder()
                .dumb(true)
                .streams(new ByteArrayInputStream(new byte[0]), new ByteArrayOutputStream())
                .encoding(StandardCharsets.UTF_8)
                .name('groovysh-test')
                .build()
        tmpDir = Files.createTempDirectory('groovysh-context-')
    }

    @AfterEach
    void tearDown() {
        terminal?.close()
        tmpDir?.toFile()?.deleteDir()
    }

    private GroovyPosixContext context(ConfigurationPath cfg = null) {
        new GroovyPosixContext(
                new ByteArrayInputStream(new byte[0]),
                new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8),
                new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8),
                tmpDir,
                terminal,
                { name -> null } as Function<String, Object>,
                cfg)
    }

    @Test
    void configPathDefaultsToNull() {
        // Backward-compatible 6-arg constructor: callers that don't need
        // syntax highlighting (most posix commands) keep working.
        def ctx = context()
        assert ctx.configPath == null
    }

    @Test
    void configPathRoundTrips() {
        // The 7-arg constructor is the path /less now uses; verify the
        // ConfigurationPath survives construction so GroovyPosixCommands.less
        // can hand it to the JLine Less constructor for highlighting.
        ConfigurationPath cfg = new ConfigurationPath(tmpDir, tmpDir)
        def ctx = context(cfg)
        assert ctx.configPath.is(cfg)
    }
}
