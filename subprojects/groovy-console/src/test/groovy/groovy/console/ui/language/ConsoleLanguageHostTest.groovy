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
package groovy.console.ui.language

import org.junit.jupiter.api.Test

import javax.swing.JTextPane
import java.awt.event.InputEvent
import java.awt.event.KeyEvent

final class ConsoleLanguageHostTest {

    @Test
    void diagnosticsHoverAndCompletionUseTheEngine() {
        def pane = new JTextPane()
        def host = ConsoleLanguageHost.attach(pane, ConsoleLanguageHostTest.classLoader)
        try {
            def broken = 'class Broken { def x = }\n'
            def hits = host.compileAndDiagnostics(broken)
            assert hits.any { it.message() }

            def src = '''\
                class Sample {
                    def ping() { ping() }
                }
                '''.stripIndent()
            def hover = host.describe(src, 0, 6)
            assert hover.contains('Sample')
            def items = host.complete(src, 1, 16)
            assert items != null
            host.setParentLoader(ConsoleLanguageHostTest.classLoader)
            host.refresh()
        } finally {
            host.close()
        }
    }

    @Test
    void offsetOfAndPlainInsertCoverEdges() {
        assert ConsoleLanguageHost.offsetOf(null, 0, 0) == 0
        assert ConsoleLanguageHost.offsetOf('', 0, 0) == 0
        assert ConsoleLanguageHost.offsetOf('ab\ncd', 0, 0) == 0
        assert ConsoleLanguageHost.offsetOf('ab\ncd', 1, 1) == 4
        assert ConsoleLanguageHost.offsetOf('ab\ncd', 9, 0) == 5
        assert ConsoleLanguageHost.offsetOf('ab', -1, 99) == 2
        assert ConsoleLanguageHost.plainInsert(null) == ''
        assert ConsoleLanguageHost.plainInsert('') == ''
        assert ConsoleLanguageHost.plainInsert('length') == 'length'
        assert ConsoleLanguageHost.plainInsert('class ${1:Name} {\n    $0\n}\n') == 'class Name {\n    \n}\n'
        assert ConsoleLanguageHost.BUFFER_URI.toString() == 'groovy-buffer:console'
    }

    @Test
    void refreshPaintsDiagnosticsAndCompleteAtCaretIsSafe() {
        def pane = new JTextPane()
        pane.text = 'class Broken { def x = }\n'
        def host = ConsoleLanguageHost.attach(pane, ConsoleLanguageHostTest.classLoader)
        try {
            host.refresh()
            assert pane.highlighter.highlights.length > 0
            pane.caretPosition = pane.text.indexOf('Broken')
            host.completeAtCaret()
            pane.dispatchEvent(new KeyEvent(pane, KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
                    InputEvent.CTRL_DOWN_MASK, KeyEvent.VK_SPACE, ' ' as char))
            pane.text = 'class Ok {}\n'
            long deadline = System.currentTimeMillis() + 2000
            while (System.currentTimeMillis() < deadline && pane.highlighter.highlights.length > 0) {
                Thread.sleep(50)
            }
        } finally {
            host.close()
        }
    }

    @Test
    void attachWithEmptyPaneAndCloseIsIdempotentEnough() {
        def host = ConsoleLanguageHost.attach(new JTextPane(), null)
        try {
            assert host.compileAndDiagnostics('') != null
            assert host.complete('', 0, 0) != null
            assert host.describe('class A {}', 0, 6) != null
        } finally {
            host.close()
        }
        def detached = ConsoleLanguageHost.attach(null, null)
        try {
            detached.refresh()
            detached.completeAtCaret()
            assert detached.compileAndDiagnostics(null) != null
        } finally {
            detached.close()
        }
    }

    @Test
    void insertAtCaretReplacesPrefixOrInserts() {
        def pane = new JTextPane()
        pane.text = 'na'
        pane.caretPosition = 2
        ConsoleLanguageHost.insertAtCaret(pane, 'name')
        assert pane.text == 'name'
        pane.text = 'xy'
        pane.caretPosition = 2
        ConsoleLanguageHost.insertAtCaret(pane, 'name')
        assert pane.text.contains('name')
        pane.text = ''
        pane.caretPosition = 0
        ConsoleLanguageHost.insertAtCaret(pane, 'x')
        assert pane.text == 'x'
        ConsoleLanguageHost.insertAtCaret(null, 'x')
        pane.text = 'ab\ncd'
        pane.caretPosition = pane.text.length()
        def host = ConsoleLanguageHost.attach(pane, ConsoleLanguageHostTest.classLoader)
        try {
            host.completeAtCaret()
        } finally {
            host.close()
        }
    }
}
