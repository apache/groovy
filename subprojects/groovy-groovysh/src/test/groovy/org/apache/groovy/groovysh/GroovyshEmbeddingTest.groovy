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
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.jline.console.CommandInput
import org.jline.console.CommandMethods
import org.jline.console.CommandRegistry
import org.jline.console.impl.JlineCommandRegistry
import org.jline.terminal.Size
import org.jline.terminal.Terminal
import org.jline.terminal.impl.DumbTerminal
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.function.Function

/**
 * End-to-end coverage of {@link Main#start(GroovyshOptions, String[])}
 * embedding hooks. Each test injects a dumb terminal and uses {@code -e}
 * (or a one-line input stream) so the REPL loop exits on EOF without a TTY.
 */
class GroovyshEmbeddingTest {

    @TempDir
    Path tempHome

    private String oldHome
    private Terminal terminal
    private ByteArrayOutputStream terminalBytes

    @BeforeEach
    void setUp() {
        oldHome = System.getProperty('user.home')
        System.setProperty('user.home', tempHome.toString())
        terminalBytes = new ByteArrayOutputStream()
        terminal = new DumbTerminal(
            'groovysh-test', 'dumb',
            new ByteArrayInputStream(new byte[0]),
            terminalBytes,
            StandardCharsets.UTF_8)
        terminal.size = new Size(80, 24)
    }

    @AfterEach
    void tearDown() {
        if (oldHome != null) {
            System.setProperty('user.home', oldHome)
        }
        terminal?.close()
    }

    @Test
    void startWithHelpAndVersionStillWork() {
        assert Main.start('--help') == 0
        assert Main.start('--version') == 0
        assert Main.start(foo: 1, '--help') == 0
    }

    @Test
    void resultHandlerSeesEvaluatedValue() {
        def results = []
        int rc = Main.start(options()
            .resultHandler { c, r -> results << r }
            .build(), '-e', '2 + 3')
        assert rc == 0
        assert 5 in results
    }

    @Test
    void bindingsAreVisibleToEvaluatedCode() {
        def results = []
        int rc = Main.start(options()
            .binding('answer', 42)
            .resultHandler { c, r -> results << r }
            .build(), '-e', 'answer')
        assert rc == 0
        assert 42 in results
    }

    @Test
    void compilerConfigurationIsUsedWhenNoEngineIsSupplied() {
        def imports = new ImportCustomizer()
        imports.addImports('java.util.concurrent.atomic.AtomicInteger')
        def config = new CompilerConfiguration()
        config.addCompilationCustomizers(imports)
        def results = []
        int rc = Main.start(options()
            .compilerConfiguration(config)
            .resultHandler { c, r -> results << r }
            .build(), '-e', 'new AtomicInteger(9).get()')
        assert rc == 0
        assert 9 in results
    }

    @Test
    void prebuiltEngineWinsOverCompilerConfiguration() {
        def engine = new GroovyEngine()
        engine.put('marker', 'from-engine')
        def results = []
        int rc = Main.start(options()
            .engine(engine)
            .compilerConfiguration(new CompilerConfiguration())
            .resultHandler { c, r -> results << r }
            .build(), '-e', 'marker')
        assert rc == 0
        assert 'from-engine' in results
    }

    @Test
    void extraCommandRegistryIsInvoked() {
        def ping = new PingRegistry()
        int rc = Main.start(options()
            .extraCommandRegistry(ping)
            .resultHandler { c, r -> }
            .build(), '-e', '/ping')
        assert rc == 0
        assert ping.hits == ['ping']
    }

    @Test
    void errorHandlerReceivesThrownFailures() {
        def errors = []
        int rc = Main.start(options()
            .resultHandler { c, r -> }
            .errorHandler { r, t -> errors << t }
            .build(), '-e', 'throw new RuntimeException("boom")')
        assert rc == 0
        assert errors.any { containsMessage(it, 'boom') }
    }

    @Test
    void bannerCanBeSuppressed() {
        def captured = captureStdout {
            Main.start(options().resultHandler { c, r -> }.build(), '-e', '1')
        }
        assert !captured.contains('Groovy Shell')
    }

    @Test
    void bannerIsPrintedWhenEnabled() {
        def captured = captureStdout {
            Main.start(options()
                .showBanner(true)
                .resultHandler { c, r -> }
                .build(), '-e', '1')
        }
        assert captured.contains('Groovy Shell')
    }

    @Test
    void quietBannerStillPrintsVersionWhenBannerEnabled() {
        def captured = captureStdout {
            Main.start(options()
                .showBanner(true)
                .resultHandler { c, r -> }
                .build(), '-q', '-e', '1')
        }
        assert captured.contains(GroovySystem.version)
    }

    @Test
    void customPromptIsUsedWhenReadingALine() {
        terminal.close()
        terminalBytes = new ByteArrayOutputStream()
        terminal = new DumbTerminal(
            'groovysh-test', 'dumb',
            new ByteArrayInputStream('1+1\n'.getBytes(StandardCharsets.UTF_8)),
            terminalBytes,
            StandardCharsets.UTF_8)
        terminal.size = new Size(80, 24)
        def prompts = []
        int rc = Main.start(options()
            .prompt {
                prompts << 'gremlin> '
                'gremlin> '
            }
            .resultHandler { c, r -> }
            .build())
        assert rc == 0
        assert prompts.contains('gremlin> ')
    }

    @Test
    void startRejectsNullOptions() {
        def thrown = false
        try {
            Main.start((GroovyshOptions) null, new String[0])
        } catch (NullPointerException ignored) {
            thrown = true
        }
        assert thrown
    }

    private GroovyshOptions.Builder options() {
        GroovyshOptions.builder().terminal(terminal).showBanner(false)
    }

    private static String captureStdout(Closure<?> body) {
        def buf = new ByteArrayOutputStream()
        def old = System.out
        System.out = new PrintStream(buf)
        try {
            body.call()
        } finally {
            System.out = old
        }
        buf.toString()
    }

    private static boolean containsMessage(Throwable t, String text) {
        while (t != null) {
            if (t.message?.contains(text)) {
                return true
            }
            t = t.cause
        }
        false
    }

    private static class PingRegistry extends JlineCommandRegistry implements CommandRegistry {
        final List<String> hits = []

        PingRegistry() {
            registerCommands([
                '/ping': new CommandMethods((Function) this::ping, this::defaultCompleter)
            ])
        }

        @Override
        String name() {
            'Ping'
        }

        @Override
        List<String> commandInfo(String command) {
            ['ping the extra registry']
        }

        private void ping(CommandInput input) {
            hits << 'ping'
        }
    }
}
