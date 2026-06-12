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

/**
 * Tests for the {@code /reset} command, which clears the engine's tracked
 * imports / types / methods / variable-snippets via {@link
 * org.apache.groovy.groovysh.jline.GroovyEngine#reset}.
 */
class ResetTest extends SystemTestSupport {

    @Test
    void resetClearsTypesMethodsVariablesAndImports() {
        // Populate every tracked map so /reset has something to do. The
        // fixture engine starts empty — anything left after /reset is a leak.
        system.execute('class C {}')
        system.execute('def square(int x) { x * x }')
        system.execute('int n = 42')
        system.execute('import java.awt.Point')

        assert engine.types.containsKey('C')
        assert engine.methodNames.contains('square')
        assert engine.variables.containsKey('n')
        assert !engine.imports.isEmpty()

        system.execute('/reset')

        assert engine.types.isEmpty()
        assert engine.methodNames.isEmpty()
        assert engine.variables.isEmpty()
        assert engine.imports.isEmpty()
    }

    @Test
    void resetIsIdempotent() {
        // Two resets in a row on a virgin engine — no exception, state stays
        // empty. Guards against a future engine.reset() that assumes prior
        // state existed.
        system.execute('/reset')
        system.execute('/reset')
        assert engine.types.isEmpty()
        assert engine.methodNames.isEmpty()
    }

    @Test
    void resetDoesNotEvictBindingVariables() {
        // /reset clears *tracked source* (the buffer) but should not touch
        // shared/binding variables. Users rely on this so that values
        // computed in the REPL survive a buffer wipe.
        engine.put('keepMe', 'still here')
        system.execute('class Tmp {}')
        system.execute('/reset')
        assert engine.types.isEmpty()
        assert engine.hasVariable('keepMe')
        assert engine.execute('keepMe') == 'still here'
    }
}
