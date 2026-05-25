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
 * Companion to {@link HelpCommandTest} that exercises JLine's other
 * {@code /help} rendering path — the {@code printCommands} compact
 * multi-column listing.
 * <p>
 * {@code SystemRegistryImpl.helpTopic} chooses between two formats:
 * <ul>
 *   <li>{@code withInfo == true} ({@code commands.size() < terminal.getHeight()})
 *       → {@code printCommandInfo} per command — name + description, one
 *       line each. This is what {@link HelpCommandTest} covers.</li>
 *   <li>{@code withInfo == false} → {@code printCommands} — a compact
 *       grid of command names, no descriptions. This class covers it.</li>
 * </ul>
 * Both paths are reachable in production: the verbose format fires for
 * tall terminals or short command lists; the compact format fires when the
 * terminal is short enough that the verbose listing wouldn't fit on screen.
 * Two tests give both paths coverage.
 */
class HelpCompactCommandTest extends SystemTestSupport {

    /**
     * Force the compact format by reporting a terminal height below the
     * registered command count. The test setup has roughly 20-25 commands
     * across the {@code console} and {@code groovy} registries plus system
     * locals; 10 is safely under any plausible total.
     */
    @Override protected int terminalHeight() { 10 }

    @Test
    void helpInCompactFormatListsKnownCommands() {
        system.execute('/help')
        def out = terminalOutput()
        assert !out.empty
        // Command names appear regardless of format — compact lays them out
        // in a grid rather than per line, but the substrings still match.
        // Asserting on names only keeps the test robust across JLine
        // cosmetic changes to the grid layout.
        assert out.contains('help')
        assert out.contains('show')
        assert out.contains('exit')
    }
}
