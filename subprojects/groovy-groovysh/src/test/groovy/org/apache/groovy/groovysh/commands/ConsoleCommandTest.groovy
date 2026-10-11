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
import org.junit.jupiter.api.Timeout

import java.awt.AWTError
import java.awt.HeadlessException
import java.awt.event.ActionEvent
import java.lang.reflect.Field
import java.util.EventObject
import java.util.concurrent.TimeUnit

/**
 * Tests for the {@code /console} command. The command launches a Swing
 * Groovy console window — we can't (and shouldn't) drive that in CI, but
 * the registration and help paths are deterministic.
 */
class ConsoleCommandTest extends SystemTestSupport {

    @Test
    void consoleIsRegisteredWhenObjectBrowserOnClasspath() {
        // /console is registered only when groovy.console.ui.ObjectBrowser
        // is on the runtime classpath. The test runtime supplies that jar.
        // The shell itself does not compile against groovy-console.
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

    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    void consoleCommandLoadsTheConsoleByName() {
        String stackKey = 'groovy.sanitized.stacktraces'
        String fullKey = 'groovy.full.stacktrace'
        String previousStack = System.getProperty(stackKey)
        String previousFull = System.getProperty(fullKey)
        Class<?> consoleClass = Class.forName('groovy.console.ui.Console')
        def controllers = consoleClass.getDeclaredField('consoleControllers')
        controllers.accessible = true
        Set before = ((List) controllers.get(null)).collect { System.identityHashCode(it) } as Set
        Throwable thrown = null
        try {
            try {
                system.execute('/console')
            } catch (Throwable caught) {
                thrown = caught
            }
            List added = addedSince(controllers, before)
            assert !added.isEmpty() || headless(thrown)
        } finally {
            def exit = consoleClass.getMethod('exit', EventObject)
            addedSince(controllers, before).each { console ->
                try {
                    exit.invoke(console, new ActionEvent(this, 0, 'exit'))
                } catch (Throwable ignored) {
                    // The frame was never shown.
                }
                ((List) controllers.get(null)).remove(console)
            }
            restoreProperty(stackKey, previousStack)
            restoreProperty(fullKey, previousFull)
        }
    }

    private static List addedSince(Field controllers, Set before) {
        ((List) controllers.get(null)).findAll { !before.contains(System.identityHashCode(it)) }
    }

    private static boolean headless(Throwable thrown) {
        Throwable cursor = thrown
        while (cursor != null) {
            if (cursor instanceof HeadlessException || cursor instanceof AWTError) {
                return true
            }
            cursor = cursor.cause
        }
        false
    }

    private static void restoreProperty(String key, String previous) {
        if (previous == null) {
            System.clearProperty(key)
        } else {
            System.setProperty(key, previous)
        }
    }
}
