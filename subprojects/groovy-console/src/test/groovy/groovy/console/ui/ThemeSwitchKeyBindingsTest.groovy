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

import org.junit.jupiter.api.Test

import javax.swing.AbstractAction
import javax.swing.JComponent
import javax.swing.JRootPane
import javax.swing.JTextPane
import javax.swing.KeyStroke
import javax.swing.SwingUtilities
import java.awt.event.ActionEvent
import java.awt.event.InputEvent
import java.awt.event.KeyEvent

import static groovy.util.HeadlessTestSupport.isHeadless
import static org.junit.jupiter.api.Assumptions.assumeFalse

/**
 * A runtime theme switch refreshes every UI delegate. From JDK 27 that clears the
 * component-owned ActionMap and InputMaps, so the console restores its own key bindings
 * around the refresh.
 */
class ThemeSwitchKeyBindingsTest {

    @Test
    void testEditorKeysStillWorkAfterUIRefresh() {
        // the editor's find/replace support creates a dialog
        assumeFalse(isHeadless())
        onEdt {
            def editor = new ConsoleTextEditor()
            JTextPane textEditor = editor.textEditor
            Console.updateUIPreservingKeyBindings([editor]) { SwingUtilities.updateComponentTreeUI(editor) }

            textEditor.text = 'abc'
            textEditor.caretPosition = 3
            press(textEditor, KeyEvent.VK_BACK_SPACE)
            assert textEditor.text == 'ab'
            press(textEditor, KeyEvent.VK_ENTER)
            assert textEditor.text == 'ab\n'

            def undo = KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK)
            def undoName = textEditor.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(undo)
            assert undoName != null
            assert textEditor.actionMap.get(undoName).is(editor.undoAction)
        }
    }

    @Test
    void testRootPaneBindingSurvivesUIRefresh() {
        onEdt {
            def rootPane = new JRootPane()
            def escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0)
            def close = new AbstractAction() {
                @Override
                void actionPerformed(ActionEvent e) {}
            }
            rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, 'exit')
            rootPane.actionMap.put('exit', close)
            Console.updateUIPreservingKeyBindings([rootPane]) { SwingUtilities.updateComponentTreeUI(rootPane) }

            assert rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(escape) == 'exit'
            assert rootPane.actionMap.get('exit').is(close)
        }
    }

    private static void press(JComponent component, int keyCode) {
        def event = new KeyEvent(component, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, keyCode, KeyEvent.CHAR_UNDEFINED)
        component.processKeyEvent(event)
        assert event.consumed
    }

    private static void onEdt(Closure body) {
        Throwable thrown = null
        SwingUtilities.invokeAndWait {
            try {
                body()
            } catch (Throwable t) {
                thrown = t
            }
        }
        if (thrown) throw thrown
    }
}
