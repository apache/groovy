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
package org.apache.groovy.groovysh.spec

import org.apache.groovy.groovysh.GroovyshOptions
import org.apache.groovy.groovysh.Main
import org.apache.groovy.groovysh.jline.GroovyEngine
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.jline.reader.LineReader
import org.jline.shell.CommandSession
import org.jline.shell.impl.AbstractCommand
import org.jline.shell.impl.SimpleCommandGroup
import org.jline.terminal.Size
import org.jline.terminal.impl.DumbTerminal
import org.junit.jupiter.api.Test

import java.nio.charset.StandardCharsets
import java.nio.file.Path

/**
 * Executable examples for embedding groovysh. Included from
 * {@code groovysh.adoc}.
 */
class EmbeddingSpecTest {

    @Test
    void compilerConfigurationOnTheEngine() {
        // tag::embed_engine_config[]
        def imports = new ImportCustomizer()
        imports.addStarImports('java.util.concurrent.atomic')
        def config = new CompilerConfiguration()
        config.addCompilationCustomizers(imports)

        def engine = new GroovyEngine(config)
        assert engine.execute('new AtomicInteger(7).get()') == 7
        // end::embed_engine_config[]
    }

    @Test
    void optionsBuilderHooks() {
        def terminal = new DumbTerminal('embedded', 'dumb',
            new ByteArrayInputStream(new byte[0]), new ByteArrayOutputStream(), StandardCharsets.UTF_8)
        terminal.size = new Size(80, 24)
        def results = []
        try {
            // tag::embed_options_hooks[]
            int rc = Main.start(GroovyshOptions.builder()
                .terminal(terminal)                                    // host-supplied terminal
                .showBanner(false)                                     // the host prints its own
                .prompt { 'gremlin> ' }                                // primary prompt
                .onReaderReady { LineReader reader ->                  // continuation prompt, key bindings
                    reader.setVariable(LineReader.SECONDARY_PROMPT_PATTERN, '.......> ')
                }
                .historyFile(Path.of(System.getProperty('user.home'), '.myapp_history'))
                .binding('answer', 42)                                 // visible to evaluated code
                .groups(new SimpleCommandGroup('MyApp', new AbstractCommand('/ping') {
                    @Override
                    Object execute(CommandSession session, String[] args) { 'pong' }
                }))
                .resultHandler { printer, result -> results << result }  // render values your way
                .errorHandler { error, defaultTrace -> defaultTrace.accept(error) }
                .build(), '-e', 'answer')
            assert rc == 0
            assert 42 in results
            // end::embed_options_hooks[]
        } finally {
            terminal.close()
        }
    }

    @Test
    void classLoaderAccessor() {
        // tag::embed_classloader[]
        def engine = new GroovyEngine()
        assert engine.classLoader instanceof GroovyEngine.EngineClassLoader
        // end::embed_classloader[]
    }
}
