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
 * Tests for the {@code /console} command. The command launches a Swing
 * Groovy console window — we can't (and shouldn't) drive that in CI, but
 * the registration and help paths are deterministic.
 */
class ConsoleCommandTest extends SystemTestSupport {

    @Test
    void consoleIsRegisteredWhenObjectBrowserOnClasspath() {
        // /console is conditionally registered: present iff
        // groovy.console.ui.ObjectBrowser is on the classpath. The test
        // module pulls groovy-console in transitively, so it should be
        // there. If anyone removes that dep without intent, this fails.
        assert '/console' in groovy.commandNames()
    }

    @Test
    void consoleHelpFlagDoesNotLaunchTheConsole() {
        // maybePrintHelp short-circuits before `new Console(...)`, so
        // `/console --help` is the only way to exercise the command in a
        // headless test without opening a frame. Asserts on output growth
        // — the help machinery captures via the printer.
        int before = printer.output.size()
        system.execute('/console --help')
        assert printer.output.size() > before
    }
}
