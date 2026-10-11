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
package groovy.console.ui.text

import org.junit.jupiter.api.Test

import javax.swing.JTextPane
import javax.swing.SwingUtilities
import javax.swing.text.DefaultStyledDocument

final class MatchingHighlighterTest {

    @Test
    void caretHighlightsTheMatchingDelimiter() {
        def doc = new DefaultStyledDocument()
        def filter = new SmartDocumentFilter(doc)
        doc.documentFilter = filter
        def pane = new JTextPane(doc)
        def highlighter = new MatchingHighlighter(filter, pane)
        highlighter.caretUpdate(null)
        SwingUtilities.invokeAndWait {}

        doc.insertString(0, 'f(x) { a[0] }', null)
        assert filter.latest
        assert filter.latestTokenList.every { it.offset >= 0 && it.length > 0 && it.text }
        pane.caretPosition = 2
        highlighter.highlight()
        SwingUtilities.invokeAndWait {}
        pane.caretPosition = 3
        highlighter.highlight()
        SwingUtilities.invokeAndWait {}
        pane.caretPosition = 6
        highlighter.highlight()
        SwingUtilities.invokeAndWait {}
        pane.caretPosition = 9
        highlighter.highlight()
        SwingUtilities.invokeAndWait {}
        pane.caretPosition = 11
        highlighter.highlight()
        SwingUtilities.invokeAndWait {}
        pane.caretPosition = 0
        highlighter.highlight()
        SwingUtilities.invokeAndWait {}

        doc.remove(0, doc.length)
        doc.insertString(0, 'a?[0]', null)
        pane.caretPosition = 2
        highlighter.highlight()
        SwingUtilities.invokeAndWait {}
        pane.caretPosition = 4
        highlighter.highlight()
        SwingUtilities.invokeAndWait {}
    }
}
