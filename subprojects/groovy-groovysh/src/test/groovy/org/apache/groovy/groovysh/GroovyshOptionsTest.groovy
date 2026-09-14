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
import org.jline.shell.CommandGroup
import org.jline.shell.CommandSession
import org.jline.shell.impl.AbstractCommand
import org.jline.shell.impl.SimpleCommandGroup
import org.jline.terminal.impl.DumbTerminal
import org.junit.jupiter.api.Test

import java.nio.charset.StandardCharsets
import java.nio.file.Path

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
        assert options.groups.isEmpty()
        assert options.prompt == null
        assert options.rightPrompt == null
        assert options.historyFile == null
        assert options.onReaderReady == null
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
    void groupsAppend() {
        def first = group('first')
        def second = group('second')
        def options = GroovyshOptions.builder()
            .groups(first)
            .groups([second])
            .build()
        assert options.groups == [first, second]
    }

    @Test
    void groupsNullIsIgnored() {
        assert GroovyshOptions.builder().groups((Iterable) null).build().groups.isEmpty()
        assert GroovyshOptions.builder().groups((CommandGroup[]) null).build().groups.isEmpty()
    }

    @Test
    void groupsRejectNullEntries() {
        shouldFail(NullPointerException) {
            GroovyshOptions.builder().groups([null])
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
            .groups(group('d'))
            .build()
        shouldFail(UnsupportedOperationException) {
            options.bindings.put('b', 2)
        }
        shouldFail(UnsupportedOperationException) {
            options.groups.add(group('x'))
        }
    }

    @Test
    void remainingSettersRoundTrip() {
        def config = new CompilerConfiguration()
        def engine = new GroovyEngine()
        def prompt = { 'gremlin> ' }
        GroovyshOptions.ResultHandler results = { p, r -> }
        GroovyshOptions.ErrorHandler errors = { t, trace -> }
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
                .rightPrompt('[rp]')
                .historyFile(Path.of('history.txt'))
                .resultHandler(results)
                .errorHandler(errors)
                .showBanner(false)
                .terminal(terminal)
                .build()
            assert options.compilerConfiguration.is(config)
            assert options.engine.is(engine)
            assert options.prompt.get() == 'gremlin> '
            assert options.rightPrompt.get() == '[rp]'
            assert options.historyFile.fileName.toString() == 'history.txt'
            assert options.resultHandler.is(results)
            assert options.errorHandler.is(errors)
            assert !options.showBanner
            assert options.terminal.is(terminal)
        } finally {
            terminal.close()
        }
    }

    private static CommandGroup group(String name) {
        new SimpleCommandGroup(name, new AbstractCommand("/${name}") {
            @Override
            Object execute(CommandSession session, String[] args) { null }
        })
    }
}
