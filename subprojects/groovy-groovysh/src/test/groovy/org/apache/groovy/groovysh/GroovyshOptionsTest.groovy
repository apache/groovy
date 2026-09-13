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
import org.jline.console.CommandRegistry
import org.jline.console.impl.JlineCommandRegistry
import org.jline.terminal.impl.DumbTerminal
import org.junit.jupiter.api.Test

import java.nio.charset.StandardCharsets

import static groovy.test.GroovyAssert.shouldFail

/**
 * Direct tests for {@link GroovyshOptions} builder defaults, copying, and
 * null-handling. The REPL wiring of these options is covered by
 * {@link GroovyshEmbeddingTest}.
 */
class GroovyshOptionsTest {

    @Test
    void defaults() {
        def options = GroovyshOptions.builder().build()
        assert options.compilerConfiguration == null
        assert options.engine == null
        assert options.bindings.isEmpty()
        assert options.extraCommandRegistries.isEmpty()
        assert options.prompt == null
        assert options.resultHandler == null
        assert options.errorHandler == null
        assert options.showBanner
        assert options.terminal == null
    }

    @Test
    void bindingsReplaceThenBindingAdds() {
        def options = GroovyshOptions.builder()
            .bindings(a: 1, b: 2)
            .binding('c', 3)
            .build()
        assert options.bindings == [a: 1, b: 2, c: 3]
    }

    @Test
    void bindingsNullClears() {
        def options = GroovyshOptions.builder()
            .bindings(a: 1)
            .bindings(null)
            .build()
        assert options.bindings.isEmpty()
    }

    @Test
    void nullBindingKeysAreDropped() {
        Map<String, Object> raw = new LinkedHashMap<>()
        raw.put(null, 1)
        raw.put('ok', 2)
        def options = GroovyshOptions.builder().bindings(raw).build()
        assert options.bindings == [ok: 2]
    }

    @Test
    void extraRegistriesAppend() {
        def first = new DummyRegistry('first')
        def second = new DummyRegistry('second')
        def options = GroovyshOptions.builder()
            .extraCommandRegistry(first)
            .extraCommandRegistries([second])
            .build()
        assert options.extraCommandRegistries == [first, second]
    }

    @Test
    void extraCommandRegistriesNullIsIgnored() {
        def options = GroovyshOptions.builder()
            .extraCommandRegistries(null)
            .build()
        assert options.extraCommandRegistries.isEmpty()
    }

    @Test
    void extraCommandRegistryRejectsNull() {
        shouldFail(NullPointerException) {
            GroovyshOptions.builder().extraCommandRegistry(null)
        }
    }

    @Test
    void bindingRejectsNullName() {
        shouldFail(NullPointerException) {
            GroovyshOptions.builder().binding(null, 1)
        }
    }

    @Test
    void mapsAndListsAreUnmodifiable() {
        def options = GroovyshOptions.builder()
            .binding('a', 1)
            .extraCommandRegistry(new DummyRegistry('d'))
            .build()
        shouldFail(UnsupportedOperationException) {
            options.bindings.put('b', 2)
        }
        shouldFail(UnsupportedOperationException) {
            options.extraCommandRegistries.add(new DummyRegistry('x'))
        }
    }

    @Test
    void remainingSettersRoundTrip() {
        def config = new CompilerConfiguration()
        def engine = new GroovyEngine()
        def prompt = { 'gremlin> ' }
        GroovyshOptions.ResultHandler results = { c, r -> }
        GroovyshOptions.ErrorHandler errors = { r, t -> }
        def terminal = new DumbTerminal(
            'groovysh-test', 'dumb',
            new ByteArrayInputStream(new byte[0]),
            new ByteArrayOutputStream(),
            StandardCharsets.UTF_8)
        try {
            def options = GroovyshOptions.builder()
                .compilerConfiguration(config)
                .engine(engine)
                .prompt(prompt)
                .resultHandler(results)
                .errorHandler(errors)
                .showBanner(false)
                .terminal(terminal)
                .build()
            assert options.compilerConfiguration.is(config)
            assert options.engine.is(engine)
            assert options.prompt.get() == 'gremlin> '
            assert options.resultHandler.is(results)
            assert options.errorHandler.is(errors)
            assert !options.showBanner
            assert options.terminal.is(terminal)
        } finally {
            terminal.close()
        }
    }

    private static class DummyRegistry extends JlineCommandRegistry implements CommandRegistry {
        private final String group

        DummyRegistry(String group) {
            this.group = group
        }

        @Override
        String name() {
            group
        }
    }
}
