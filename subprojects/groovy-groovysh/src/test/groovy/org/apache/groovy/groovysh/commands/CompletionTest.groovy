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
 * Smoke test for tab completion. Each registered command in
 * {@link org.apache.groovy.groovysh.jline.GroovyCommands} wires up a
 * completer factory; previously, none of those factories were exercised
 * by any test.
 *
 * <p>The smoke we test here is "every factory compiles without throwing".
 * Driving end-to-end completion (typing a partial line, asserting
 * candidates) needs a real LineReader pumping the parser — this unit
 * harness can't fake that reliably. Even so, this catches the most
 * common breakage: a typo or missing import in a completer factory
 * function that would NPE the first time a user hits TAB.
 */
class CompletionTest extends SystemTestSupport {

    @Test
    void everyCommandsCompleterFactoryCompilesWithoutThrowing() {
        // CommandRegistry.compileCompleters() invokes every per-command
        // completer factory function. If any of them throws (typo in a
        // class reference, missing JLine import, broken constructor
        // chain), this test surfaces it.
        def systemCompleter = groovy.compileCompleters()
        assert systemCompleter != null
        assert !systemCompleter.compiled
        systemCompleter.compile()
        assert systemCompleter.compiled
    }
}
