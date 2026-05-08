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

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import java.nio.file.Files
import java.nio.file.Path

/**
 * Tests for the {@code /save} and {@code /load} commands — round-tripping
 * the engine's buffer (variables, methods, type definitions) to a file and
 * back. Flagship persistence feature; previously had no automated coverage.
 */
class SaveLoadTest extends SystemTestSupport {

    @TempDir
    Path tmp

    @Test
    void saveLoadRoundTrip() {
        // Establish session state. Note: /save serialises the engine's
        // *buffer*, which in default (non-interpreter) mode contains
        // imports/types/methods but not bare variable assignments. So we
        // round-trip definitions; the variables-via-shared-data path is a
        // separate code branch (no-arg /save) not covered here.
        engine.execute('import java.awt.Point')
        engine.execute('def doubler(n) { n * 2 }')
        engine.execute('class Probe {}')
        assert engine.methodNames.contains('doubler')
        assert engine.types.containsKey('Probe')
        assert engine.imports.values().any { it.contains('java.awt.Point') }

        Path file = tmp.resolve('session.groovy')
        system.execute("/save ${forwardSlashes(file)}")

        assert Files.exists(file)
        def saved = file.text
        // Assert on identifiers that must round-trip; don't pin to
        // whitespace, ordering, or how the snippets are joined.
        assert saved.contains('java.awt.Point')
        assert saved.contains('doubler')
        assert saved.contains('Probe')

        // Wipe the engine; verify a clean slate.
        engine.reset()
        assert !engine.methodNames.contains('doubler')
        assert !engine.types.containsKey('Probe')

        // Replay the saved buffer.
        system.execute("/load ${forwardSlashes(file)}")

        // Loaded state matches the original; methods evaluate.
        assert engine.methodNames.contains('doubler')
        assert engine.execute('doubler(21)') == 42
        assert engine.types.containsKey('Probe')
        assert engine.imports.values().any { it.contains('java.awt.Point') }
    }
}
