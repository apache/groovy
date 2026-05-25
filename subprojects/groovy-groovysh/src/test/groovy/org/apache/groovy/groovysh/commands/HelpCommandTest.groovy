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
 * Tests for the {@code /help} command, registered by JLine's
 * {@code SystemRegistryImpl} and renamed in {@link SystemTestSupport} to
 * match the leading-slash convention production uses.
 *
 * The help builtin writes through {@code terminal.writer()} rather than
 * through the printer, so this test demonstrates the
 * {@link SystemTestSupport#terminalOutput()} capture pattern for any
 * future test that needs to assert on terminal-side output.
 */
class HelpCommandTest extends SystemTestSupport {

    @Test
    void helpListsKnownCommands() {
        system.execute('/help')
        def out = terminalOutput()
        assert !out.empty
        // A handful of stable command names that should appear in the listing.
        // Names only — don't assert on layout, alignment, or descriptions, so
        // the test stays robust across JLine cosmetic changes and platforms.
        assert out.contains('help')
        assert out.contains('show')
        assert out.contains('exit')
    }

    /**
     * TEMP DIAGNOSTIC — investigating the JDK-specific flake on helpListsKnownCommands.
     * <p>
     * Peeks at {@code SystemRegistryImpl.exception} after running {@code /help}.
     * JLine's {@code helpTopic} silently stores any per-command exception there via
     * {@code catch (Exception e) { exception = e; }}. If iteration aborts mid-list
     * (one of the registered commands' {@code commandInfo}/description throws),
     * we'd see the exception here.
     * <p>
     * Expected outcomes:
     * <ul>
     *   <li>{@code exception == null} → iteration ran to completion; the truncation
     *       seen in the flake is pump-drain / native-terminal-binding timing.</li>
     *   <li>{@code exception != null} → iteration aborted; the stack trace points
     *       at the offending command.</li>
     * </ul>
     * Remove this test once the flake is understood.
     */
    @Test
    void diagSilentExceptionAfterHelp() {
        system.execute('/help')
        def field = org.jline.console.impl.SystemRegistryImpl.class.getDeclaredField('exception')
        field.setAccessible(true)
        Exception ex = (Exception) field.get(system)
        if (ex) {
            ex.printStackTrace()
            throw new AssertionError(
                "SystemRegistryImpl.exception is non-null after /help: ${ex.class.name}: ${ex.message}",
                ex)
        }
    }
}
