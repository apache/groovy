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

import java.nio.file.Path

/**
 * End-to-end test that drives several commands in sequence — the kind of
 * cross-command regression no single per-command test can catch.
 *
 * <p>Distinct from {@link SaveLoadTest}, which exercises {@code /save} →
 * {@code engine.reset()} → {@code /load} programmatically: this test
 * routes the reset through the {@code /reset} command itself, so the
 * full registry-dispatch path is exercised end-to-end.
 */
class RoundTripTest extends SystemTestSupport {

    @TempDir
    Path tmp

    @Test
    void buildSaveResetLoadRestoresState() {
        system.execute('class Probe {}')
        system.execute('def doubler(n) { n * 2 }')
        system.execute('import java.time.LocalDate')

        Path file = tmp.resolve('session.groovy')
        system.execute("/save ${forwardSlashes(file)}")

        // Reset via the registered /reset command (not engine.reset()).
        // This is the bit SaveLoadTest doesn't cover.
        system.execute('/reset')
        assert engine.types.isEmpty()
        assert engine.methodNames.isEmpty()
        assert engine.imports.isEmpty()

        system.execute("/load ${forwardSlashes(file)}")

        assert engine.types.containsKey('Probe')
        assert engine.methodNames.contains('doubler')
        assert engine.imports.values().any { it.contains('java.time.LocalDate') }
        assert engine.execute('doubler(21)') == 42
    }
}
