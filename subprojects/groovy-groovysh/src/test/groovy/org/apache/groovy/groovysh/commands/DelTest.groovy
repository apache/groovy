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
 * Tests for the {@code /del} command. Uses the full {@link SystemTestSupport}
 * stack so each invocation passes through registry parsing, pipe handling,
 * and console dispatch — exercising the same path as a real REPL session.
 */
class DelTest extends SystemTestSupport {

    @Test
    void testDelVariable() {
        console.execute('dummyName', "x = 42")
        assert console.hasVariable('x')
        system.execute('/del x')
        assert !console.hasVariable('x')
    }

    @Test
    void testDelMultipleVariables() {
        console.execute('dummyName', "a = 1; b = 2; c = 3")
        ['a', 'b', 'c'].each { assert console.hasVariable(it) }
        system.execute('/del a c')
        assert !console.hasVariable('a')
        assert console.hasVariable('b')
        assert !console.hasVariable('c')
    }

    @Test
    void testDelNonexistentIsHarmless() {
        // /del on an unknown variable should be a no-op, not a hard failure
        // that aborts the REPL session.
        system.execute('/del thisVariableDoesNotExist')
        // No exception — pre-existing variables (none) are unaffected.
        assert !console.hasVariable('thisVariableDoesNotExist')
    }
}
