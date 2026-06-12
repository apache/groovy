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
 * Tests for the {@code /vars} command, which lists or removes tracked
 * <em>typed</em> variable declarations (the snippets matched by
 * {@code GroovyEngine.PATTERN_VAR_DEF}). Unscoped assignments like
 * {@code x = 5} create binding/shared variables and don't show up in
 * {@code /vars} — those are observable via {@code GroovyEngine.hasVariable},
 * not via this command.
 */
class VarsTest extends SystemTestSupport {

    @Test
    void varsListsTrackedTypedDeclarations() {
        // Empty registry initially.
        system.execute('/vars')
        def initial = printer.output.join()
        assert !initial.contains('int n = 42')

        system.execute('int n = 42')
        system.execute('def msg = "hi"')

        assert engine.variables.keySet() == ['n', 'msg'] as Set

        printer.output.clear()
        system.execute('/vars')
        def listing = printer.output.join()
        assert listing.contains('int n = 42')
        assert listing.contains('def msg = "hi"')
    }

    @Test
    void varsByNameShowsOnlyThatSource() {
        system.execute('int n = 42')
        system.execute('def msg = "hi"')

        printer.output.clear()
        system.execute('/vars n')
        def out = printer.output.join()
        assert out.contains('int n = 42')
        assert !out.contains('def msg = "hi"')
    }

    @Test
    void varsDeleteRemovesTheTrackedDeclaration() {
        system.execute('int n = 42')
        system.execute('def msg = "hi"')
        assert engine.variables.containsKey('n')

        system.execute('/vars -d n')

        assert !engine.variables.containsKey('n')
        assert engine.variables.containsKey('msg')
    }

    @Test
    void varsDeleteUnknownNameIsHarmless() {
        // /vars -d on an unknown variable should not throw or corrupt the
        // registry — mirrors the pattern other delete-by-name commands
        // promise (TypesTest, MethodsTest).
        system.execute('int n = 42')
        def before = engine.variables.keySet().toSet()
        system.execute('/vars -d noSuchVar')
        assert engine.variables.keySet().toSet() == before
    }

    @Test
    void varsDeleteAllEmptiesTheRegistry() {
        // The wildcard '*' clears every tracked declaration in one go, per
        // maybeRemoveItem's name == '*' branch.
        system.execute('int n = 42')
        system.execute('def msg = "hi"')
        assert engine.variables.size() == 2

        system.execute('/vars -d *')

        assert engine.variables.isEmpty()
    }
}
