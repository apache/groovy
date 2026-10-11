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
package groovy.console.ui

import groovy.console.ui.language.ConsoleLanguageHost
import groovy.console.ui.text.GroovyFilter
import groovy.console.ui.text.SmartDocumentFilter
import groovy.inspect.Inspector
import groovy.swing.SwingBuilder
import org.junit.jupiter.api.Test

import javax.swing.AbstractAction
import javax.swing.Action
import javax.swing.JCheckBoxMenuItem
import javax.swing.text.DocumentFilter
import javax.swing.JFileChooser
import javax.swing.JLayeredPane
import javax.swing.JRootPane
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextPane
import javax.swing.RootPaneContainer
import javax.swing.text.DefaultStyledDocument
import java.awt.Component
import java.awt.Container
import java.awt.HeadlessException
import java.awt.event.ActionEvent
import java.text.MessageFormat
import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger
import java.util.prefs.Preferences

class ConsoleLanguageWiringTest {

    @Test
    void consolePublishesTheLanguageHostOnShellClasspathAndExit() {
        Preferences prefs = Preferences.userNodeForPackage(Console)
        boolean previousInterrupt = prefs.getBoolean('threadInterrupt', false)
        String previousDir = prefs.get('currentClasspathDir', null)
        boolean previousAssistance = Console.compilerAssistance
        try {
        Console console = new Console()
        def box = new JCheckBoxMenuItem()
        box.selected = true
        console.threadInterruption(new ActionEvent(box, 0, 'interrupt'))

        console.inputArea = new JTextPane()
        console.inputArea.document = new DefaultStyledDocument()
        console.outputArea = new JTextPane()
        console.saveAction = new AbstractAction() {
            @Override void actionPerformed(ActionEvent e) { }
        }
        def editor = Console.getDeclaredField('inputEditor')
        editor.accessible = true
        def unsafeField = Class.forName('sun.misc.Unsafe').getDeclaredField('theUnsafe')
        unsafeField.accessible = true
        def unsafe = unsafeField.get(null)
        def allocate = unsafe.class.getMethod('allocateInstance', Class)
        def allocated = allocate.invoke(unsafe, ConsoleTextEditor)
        allocated.metaClass.repaint = { -> }
        editor.set(console, allocated)
        console.frame = new FakeFrame()
        console.swing = new SwingBuilder()
        def install = Console.getDeclaredMethod('installLanguageHost')
        install.accessible = true
        install.invoke(console)
        console.newScript(console.shell.classLoader.parent, console.shell.context)
        box.selected = false
        console.threadInterruption(new ActionEvent(box, 0, 'interrupt'))
        box.selected = true
        console.threadInterruption(new ActionEvent(box, 0, 'interrupt'))

        def file = File.createTempFile('console-host', '.groovy')
        file.deleteOnExit()
        file.text = 'int kept = 1\n'
        console.loadScriptFile(file)
        assert console.inputArea.text.contains('kept')
        console.inspectCst(null)
        console.inspectTokens(null)

        withApprovingChooser {
            console.addClasspathJar(null)
            console.addClasspathDir(null)
        }
        try {
            console.exit()
        } catch (ExceptionInInitializerError | NoClassDefFoundError | HeadlessException ignored) {
            // FindReplaceUtility builds a dialog after the host is cleared.
        }
        try {
            new Console().exit()
        } catch (ExceptionInInitializerError | NoClassDefFoundError | HeadlessException ignored) {
            // Same dialog, with no language host installed.
        }
        def show = Console.getDeclaredMethod('showComponent', String, Component)
        show.accessible = true
        show.invoke(console, 'Tokens', new JLabel('x'))
        try {
            console.fileNewWindow(null)
        } catch (HeadlessException | ExceptionInInitializerError | NoClassDefFoundError ignored) {
            // A second window needs a display.
        }
        try {
            console.run()
        } catch (HeadlessException | ExceptionInInitializerError | NoClassDefFoundError ignored) {
            // run() shows the frame before attaching the host.
        }
        def panel = new JPanel()
        panel.metaClass.pack = { -> }
        panel.metaClass.show = { -> }
        panel.metaClass.getTitle = { -> 'GroovyConsole' }
        panel.metaClass.setTitle = { String value -> }
        def saved = Console.frameConsoleDelegates
        try {
            Console.frameConsoleDelegates = [
                    rootContainerDelegate: { panel },
                    menuBarDelegate: saved.menuBarDelegate,
            ]
            console.metaClass.nativeFullScreenForMac = { frame -> }
            console.fileNewWindow(null)
        } catch (Throwable ignored) {
            // The stand-in frame still cannot satisfy every view binding.
        } finally {
            Console.frameConsoleDelegates = saved
        }
        try {
            console.run(
                    rootContainerDelegate: { panel },
                    menuBarDelegate: saved.menuBarDelegate)
        } catch (Throwable ignored) {
            // installLanguageHost follows the window block when the frame is not a Window.
        }
        console.systemOutInterceptor?.stop()
        console.systemErrorInterceptor?.stop()
        } finally {
            prefs.putBoolean('threadInterrupt', previousInterrupt)
            if (previousDir == null) {
                prefs.remove('currentClasspathDir')
            } else {
                prefs.put('currentClasspathDir', previousDir)
            }
            Console.compilerAssistance = previousAssistance
            prefs.putBoolean('compilerAssistance', previousAssistance)
        }
    }

    @Test
    void missingLanguageServicesLeaveTheHostUnset() {
        // GROOVY-12448
        Preferences prefs = Preferences.userNodeForPackage(Console)
        boolean previousAssistance = Console.compilerAssistance
        boolean previousPref = prefs.getBoolean('compilerAssistance', true)
        Console console = new Console()
        def hostField = Console.getDeclaredField('languageHost')
        hostField.accessible = true
        def closedField = ConsoleLanguageHost.getDeclaredField('closed')
        closedField.accessible = true
        def action = new AbstractAction('Compiler Assistance') {
            @Override void actionPerformed(ActionEvent event) { }
        }
        Logger logger = Logger.getLogger(Console.class.name)
        List<LogRecord> records = []
        Handler handler = new Handler() {
            @Override void publish(LogRecord record) { records << record }
            @Override void flush() { }
            @Override void close() { }
        }
        handler.level = Level.ALL
        Level previousLevel = logger.level
        logger.addHandler(handler)
        logger.level = Level.ALL
        try {
            Console.compilerAssistance = true
            console.inputArea = new JTextPane()
            console.inputArea.document = new DefaultStyledDocument()

            console.installLanguageHost { null }
            assert hostField.get(console) == null
            assert records.empty

            console.installLanguageHost {
                throw new NoClassDefFoundError('com.google.gson.Gson')
            }
            assert hostField.get(console) == null
            assert Console.compilerAssistance
            assert prefs.getBoolean('compilerAssistance', true) == previousPref

            console.swing = new SwingBuilder()
            console.installLanguageHost {
                throw new NoClassDefFoundError('com.google.gson.Gson')
            }
            console.swing.setVariable('compilerAssistanceAction', 'not-an-action')
            console.installLanguageHost {
                throw new NoClassDefFoundError('com.google.gson.Gson')
            }
            assert hostField.get(console) == null

            action.enabled = true
            action.putValue(Action.SELECTED_KEY, Boolean.TRUE)
            console.swing = new SwingBuilder()
            console.swing.setVariable('compilerAssistanceAction', action)
            def previous = ConsoleLanguageHost.attach(console.inputArea, console.shell.classLoader)
            hostField.set(console, previous)
            assert !closedField.getBoolean(previous)
            console.installLanguageHost {
                throw new NoClassDefFoundError('com.google.gson.Gson')
            }
            assert hostField.get(console) == null
            assert closedField.getBoolean(previous)
            assert !action.enabled
            assert Boolean.FALSE.equals(action.getValue(Action.SELECTED_KEY))
            assert Console.compilerAssistance
            assert prefs.getBoolean('compilerAssistance', true) == previousPref
            assert records.size() == 4
            assert records.every { LogRecord record ->
                record.level == Level.WARNING && renderedWarning(record).contains(
                        'Groovy language services are unavailable (NoClassDefFoundError); Compiler Assistance is off.')
            }

            action.enabled = true
            Console.compilerAssistance = false
            boolean called = false
            console.installLanguageHost {
                called = true
                null
            }
            assert !called
            assert action.enabled
            assert hostField.get(console) == null
            assert records.size() == 4
        } finally {
            logger.removeHandler(handler)
            logger.level = previousLevel
            hostField.get(console)?.close()
            Console.consoleControllers.remove(console)
            Console.compilerAssistance = previousAssistance
        }
    }

    @Test
    void missingLanguageServicesUseTheRegexHighlighter() {
        def doc = new DefaultStyledDocument()
        DocumentFilter filter = ConsoleTextEditor.highlightingFilter(doc) {
            throw new NoClassDefFoundError('com.google.gson.Gson')
        }
        assert filter instanceof GroovyFilter
        def installed = ConsoleTextEditor.highlightingFilter(doc)
        assert installed instanceof SmartDocumentFilter
        installed.close()
    }

    @Test
    void compilerAssistanceAppliesToEveryOpenWindow() {
        Preferences prefs = Preferences.userNodeForPackage(Console)
        boolean previousAssistance = Console.compilerAssistance
        boolean previousPref = prefs.getBoolean('compilerAssistance', true)
        Console first = new Console()
        Console second = new Console()
        def host = Console.getDeclaredField('languageHost')
        host.accessible = true
        try {
            first.inputArea = new JTextPane()
            second.inputArea = new JTextPane()
            def firstAction = new AbstractAction('Compiler Assistance') {
                @Override void actionPerformed(ActionEvent event) { }
            }
            def secondAction = new AbstractAction('Compiler Assistance') {
                @Override void actionPerformed(ActionEvent event) { }
            }
            first.swing = new SwingBuilder()
            second.swing = new SwingBuilder()
            first.swing.setVariable('compilerAssistanceAction', firstAction)
            second.swing.setVariable('compilerAssistanceAction', secondAction)
            def on = new JCheckBoxMenuItem()
            on.selected = true
            first.compilerAssistance(new ActionEvent(on, 0, 'assist'))
            assert host.get(first) != null
            assert host.get(second) != null
            assert Boolean.TRUE.equals(firstAction.getValue(Action.SELECTED_KEY))
            assert Boolean.TRUE.equals(secondAction.getValue(Action.SELECTED_KEY))
            def off = new JCheckBoxMenuItem()
            off.selected = false
            second.compilerAssistance(new ActionEvent(off, 0, 'assist'))
            assert host.get(first) == null
            assert host.get(second) == null
            assert Boolean.FALSE.equals(firstAction.getValue(Action.SELECTED_KEY))
            assert Boolean.FALSE.equals(secondAction.getValue(Action.SELECTED_KEY))
            first.inputArea.text = 'def a = 1\n'
            first.inspectCst(null)
            first.inspectTokens(null)
        } finally {
            host.get(first)?.close()
            host.get(second)?.close()
            Console.consoleControllers.remove(first)
            Console.consoleControllers.remove(second)
            Console.compilerAssistance = previousAssistance
            prefs.putBoolean('compilerAssistance', previousPref)
        }
    }

    @Test
    void objectBrowserBoldsKeywordsInTheClassCard() {
        def browser = new ObjectBrowser()
        def swing = new SwingBuilder()
        def method = ObjectBrowser.declaredMethods.find { it.name == 'makeCard' }
        method.accessible = true
        def card = method.invoke(browser, swing, new Inspector('class'), 'path')
        assert card != null
    }

    private static final class FakeFrame implements RootPaneContainer {
        String title = 'GroovyConsole'
        private final JRootPane root = new JRootPane()

        @Override JRootPane getRootPane() { root }
        @Override Container getContentPane() { root.contentPane }
        @Override void setContentPane(Container contentPane) { root.contentPane = contentPane }
        @Override JLayeredPane getLayeredPane() { root.layeredPane }
        @Override void setLayeredPane(JLayeredPane layeredPane) { root.layeredPane = layeredPane }
        @Override Component getGlassPane() { root.glassPane }
        @Override void setGlassPane(Component glassPane) { root.glassPane = glassPane }
    }

    private static String renderedWarning(LogRecord record) {
        Object[] params = record.parameters
        if (params == null || params.length == 0) {
            return record.message ?: ''
        }
        MessageFormat.format(record.message, params)
    }

    private static void withApprovingChooser(Closure body) {
        def jar = File.createTempFile('console-host', '.jar')
        jar.deleteOnExit()
        def dir = jar.parentFile
        JFileChooser.metaClass.showDialog = { parent, approve -> JFileChooser.APPROVE_OPTION }
        JFileChooser.metaClass.getSelectedFiles = { -> [jar] as File[] }
        JFileChooser.metaClass.getSelectedFile = { -> dir }
        JFileChooser.metaClass.getCurrentDirectory = { -> dir }
        try {
            body.call()
        } finally {
            GroovySystem.metaClassRegistry.removeMetaClass(JFileChooser)
        }
    }
}
