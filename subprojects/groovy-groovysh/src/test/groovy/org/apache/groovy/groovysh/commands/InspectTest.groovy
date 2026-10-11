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

import org.jline.console.CommandInput
import org.jline.console.CommandRegistry
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout

import javax.swing.SwingUtilities
import java.awt.AWTError
import java.awt.Frame
import java.awt.GraphicsEnvironment
import java.awt.HeadlessException
import java.awt.Window
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.TimeUnit

/**
 * Tests for the {@code /inspect} command. Exercises GroovyCommands and the
 * vendored ObjectInspector — neither of which had any direct test coverage
 * before. The command writes via the printer, so assertions go against
 * {@code printer.output}.
 */
class InspectTest extends SystemTestSupport {

    @Test
    void inspectMethodsListsKnownMembers() {
        console.execute('dummy', "data = [a: 1, b: 2]")
        system.execute('/inspect --methods $data')
        def out = printer.output.join()
        // LinkedHashMap exposes these methods; assert on names without
        // pinning to formatting/columns/parameter shapes.
        assert out.contains('get')
        assert out.contains('put')
        assert out.contains('size')
    }

    @Test
    void inspectInfoListsPropertyCategories() {
        console.execute('dummy', "data = [a: 1, b: 2]")
        system.execute('/inspect --info $data')
        def out = printer.output.join()
        // ObjectInspector.properties() returns a map with these keys.
        assert out.contains('propertyInfo')
        assert out.contains('publicFields')
        assert out.contains('classProps')
    }

    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    void inspectGuiLoadsTheObjectBrowserByName() {
        console.execute('dummy', "data = 'hi'")
        // ObjectBrowser is loaded by name. A headless JVM fails while
        // creating the frame, and JLine rethrows that failure. A JVM with
        // a display opens the window and the command returns. Requiring an
        // exception in both cases fails whenever the display is available.
        Throwable thrown = null
        try {
            system.execute('/inspect --gui $data')
        } catch (Throwable failure) {
            thrown = failure
        } finally {
            closeObjectBrowser()
        }
        if (thrown != null) {
            assert uiStartupFailure(thrown) : thrown
        } else {
            assert !GraphicsEnvironment.isHeadless()
        }
        // Call the command with the flag still in its arguments. The shell
        // parser can consume a registered option before the method runs.
        def direct = new CommandInput('/inspect', ['--gui', 'hi'] as Object[],
                new CommandRegistry.CommandSession(terminal, System.in, System.out, System.err))
        assert groovy.inspect(direct) == null
        closeObjectBrowser()
    }

    private static boolean uiStartupFailure(Throwable failure) {
        Throwable cursor = failure
        while (cursor != null) {
            if (cursor instanceof HeadlessException || cursor instanceof AWTError) {
                return true
            }
            String message = cursor.message
            if (message != null && (message.contains('frame') || message.contains('Headless') || message.contains('X11'))) {
                return true
            }
            cursor = cursor.cause
        }
        return false
    }

    private static void closeObjectBrowser() {
        if (GraphicsEnvironment.isHeadless()) {
            return
        }
        Runnable close = {
            for (Window window : Window.getWindows()) {
                if (window instanceof Frame && 'Groovy Object Browser' == ((Frame) window).title) {
                    window.dispose()
                }
            }
        }
        try {
            if (SwingUtilities.isEventDispatchThread()) {
                close.run()
            } else {
                SwingUtilities.invokeAndWait(close)
            }
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt()
        } catch (InvocationTargetException ignored) {
            // The window is already gone.
        }
    }
}
