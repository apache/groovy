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

import org.apache.groovy.lsp.internal.engine.GroovyLanguageEngine

import javax.swing.JMenuItem
import javax.swing.JPopupMenu
import javax.swing.JTextPane
import javax.swing.SwingUtilities
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.BadLocationException
import javax.swing.text.DefaultHighlighter
import java.awt.Color
import java.awt.Point
import java.awt.event.InputEvent
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.net.URI
import java.net.URLClassLoader
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Optional in-process language host. Loaded only when groovy-lsp is present.
 * Compiles the unsaved editor buffer through {@link GroovyLanguageEngine}
 * (no class generation) and paints diagnostics; Ctrl-Space offers completions.
 */
final class ConsoleLanguageHost implements AutoCloseable {

    static final URI BUFFER_URI = GroovyLanguageEngine.CONSOLE_URI

    private static final int DEBOUNCE_MS = 150
    private static final int MAX_COMPLETIONS = 20

    private final GroovyLanguageEngine engine
    private final JTextPane input
    private final DefaultHighlighter.DefaultHighlightPainter errorPainter =
            new DefaultHighlighter.DefaultHighlightPainter(new Color(255, 180, 180))
    private final List<Object> highlights = []
    private final AtomicInteger generation = new AtomicInteger()
    private final ScheduledExecutorService debounce = Executors.newSingleThreadScheduledExecutor { Runnable r ->
        Thread thread = new Thread(r, 'groovy-console-language')
        thread.daemon = true
        thread
    }
    private ScheduledFuture<?> pending

    private ConsoleLanguageHost(JTextPane input, ClassLoader loader) {
        this.input = input
        this.engine = new GroovyLanguageEngine()
        engine.setParentLoader(loader)
        engine.openBuffer(BUFFER_URI, 'groovy', textOf(input))
        if (input?.document != null) {
            input.document.addDocumentListener(new DocumentListener() {
                @Override
                void insertUpdate(DocumentEvent e) { schedule() }

                @Override
                void removeUpdate(DocumentEvent e) { schedule() }

                @Override
                void changedUpdate(DocumentEvent e) { }
            })
            input.addKeyListener(new KeyAdapter() {
                @Override
                void keyPressed(KeyEvent e) {
                    if (e.keyCode == KeyEvent.VK_SPACE && (e.modifiersEx & InputEvent.CTRL_DOWN_MASK) != 0) {
                        showCompletions()
                        e.consume()
                    }
                }
            })
        }
    }

    /**
     * @param input editor pane
     * @param loader GroovyShell class loader
     * @return a host bound to {@code input}
     */
    static ConsoleLanguageHost attach(JTextPane input, ClassLoader loader) {
        new ConsoleLanguageHost(input, loader)
    }

    /**
     * Compiles {@code text} immediately and returns diagnostics.
     *
     * @param text buffer text
     * @return hits in 0-based UTF-16 coordinates
     */
    List<GroovyLanguageEngine.Hit> compileAndDiagnostics(String text) {
        engine.updateBuffer(BUFFER_URI, text)
        engine.compileNow()
        engine.diagnostics(BUFFER_URI)
    }

    /**
     * Completions at a 0-based UTF-16 position.
     *
     * @param text buffer text
     * @param line 0-based line
     * @param character 0-based character
     * @return candidates
     */
    List<GroovyLanguageEngine.Candidate> complete(String text, int line, int character) {
        engine.updateBuffer(BUFFER_URI, text)
        engine.compileNow()
        engine.complete(BUFFER_URI, line, character)
    }

    /**
     * Hover markdown at a 0-based UTF-16 position.
     *
     * @param text buffer text
     * @param line 0-based line
     * @param character 0-based character
     * @return markdown
     */
    String describe(String text, int line, int character) {
        engine.updateBuffer(BUFFER_URI, text)
        engine.compileNow()
        engine.describe(BUFFER_URI, line, character)
    }

    /**
     * Compiles the current editor text and paints diagnostic highlights.
     */
    void refresh() {
        generation.incrementAndGet()
        pending?.cancel(false)
        String text = textOf(input)
        paint(text, compileAndDiagnostics(text))
    }

    /**
     * Offers completions at the caret. Used by Ctrl-Space and tests.
     */
    void completeAtCaret() {
        showCompletions()
    }

    /**
     * @param loader GroovyShell class loader after classpath changes
     */
    void setParentLoader(ClassLoader loader) {
        engine.setParentLoader(loader)
        engine.putExtra('classpathGeneration', String.valueOf(urlStamp(loader)))
    }

    @Override
    void close() {
        generation.incrementAndGet()
        pending?.cancel(false)
        debounce.shutdownNow()
        engine.close()
    }

    private void schedule() {
        int gen = generation.incrementAndGet()
        String text = textOf(input)
        pending?.cancel(false)
        pending = debounce.schedule({
            try {
                if (gen != generation.get()) {
                    return
                }
                def hits = compileAndDiagnostics(text)
                if (gen != generation.get()) {
                    return
                }
                SwingUtilities.invokeLater {
                    if (gen == generation.get()) {
                        paint(text, hits)
                    }
                }
            } catch (RuntimeException ignored) {
            }
        }, DEBOUNCE_MS, TimeUnit.MILLISECONDS)
    }

    private void paint(String text, List<GroovyLanguageEngine.Hit> hits) {
        def highlighter = input?.highlighter
        if (highlighter == null) {
            return
        }
        highlights.each { highlighter.removeHighlight(it) }
        highlights.clear()
        hits?.each { hit ->
            int start = offsetOf(text, hit.startLine(), hit.startCharacter())
            int end = offsetOf(text, hit.endLine(), hit.endCharacter())
            if (end <= start) {
                end = Math.min(start + 1, text == null ? 0 : text.length())
            }
            try {
                highlights << highlighter.addHighlight(start, end, errorPainter)
            } catch (BadLocationException ignored) {
            }
        }
        if (hits) {
            input.toolTipText = hits.take(3).collect { it.message() }.join('\n')
        } else {
            input.toolTipText = null
        }
    }

    private void showCompletions() {
        if (input == null) {
            return
        }
        String text = textOf(input)
        int caret = Math.min(Math.max(input.caretPosition, 0), text.length())
        int line = 0
        int character = 0
        for (int i = 0; i < caret; i++) {
            if (text.charAt(i) == '\n' as char) {
                line++
                character = 0
            } else {
                character++
            }
        }
        pending?.cancel(false)
        pending = debounce.schedule({
            try {
                def items = complete(text, line, character)
                SwingUtilities.invokeLater { showMenu(items) }
            } catch (RuntimeException ignored) {
            }
        }, 0, TimeUnit.MILLISECONDS)
    }

    private void showMenu(List<GroovyLanguageEngine.Candidate> items) {
        if (input == null || !items) {
            return
        }
        try {
            def menu = new JPopupMenu()
            items.take(MAX_COMPLETIONS).each { item ->
                def entry = new JMenuItem(item.label() ?: item.insertText())
                entry.addActionListener {
                    insertAtCaret(input, plainInsert(item.insertText() ?: item.label()))
                }
                menu.add(entry)
            }
            Point point = input.caret.magicCaretPosition ?: new Point(0, 0)
            menu.show(input, point.x, point.y + 16)
        } catch (RuntimeException ignored) {
        }
    }

    static void insertAtCaret(JTextPane pane, String insert) {
        if (pane?.document == null) {
            return
        }
        String text = textOf(pane)
        String piece = insert ?: ''
        int caret = Math.min(Math.max(pane.caretPosition, 0), text.length())
        int start = caret
        while (start > 0 && Character.isJavaIdentifierPart(text.charAt(start - 1))) {
            start--
        }
        String prefix = text.substring(start, caret)
        try {
            if (prefix && piece.startsWith(prefix)) {
                pane.document.insertString(caret, piece.substring(prefix.length()), null)
            } else if (prefix) {
                pane.document.remove(start, caret - start)
                pane.document.insertString(start, piece, null)
            } else {
                pane.document.insertString(caret, piece, null)
            }
        } catch (BadLocationException ignored) {
        }
    }

    static String plainInsert(String insert) {
        if (insert == null || insert.isEmpty()) {
            return ''
        }
        insert.replaceAll(/\$\{\d+:([^}]*)\}/, '$1').replaceAll(/\$\d+/, '')
    }

    private static int urlStamp(ClassLoader loader) {
        if (loader instanceof URLClassLoader) {
            int hash = 1
            loader.URLs.each { url ->
                String form = url == null ? '' : url.toExternalForm()
                hash = 31 * hash + form.hashCode()
            }
            return hash
        }
        System.identityHashCode(loader)
    }

    private static String textOf(JTextPane pane) {
        pane == null ? '' : (pane.text ?: '')
    }

    static int offsetOf(String text, int line, int character) {
        if (text == null || text.isEmpty()) {
            return 0
        }
        int pos = 0
        int current = 0
        int row = Math.max(line, 0)
        while (current < row) {
            int nl = text.indexOf('\n', pos)
            if (nl < 0) {
                return text.length()
            }
            pos = nl + 1
            current++
        }
        Math.min(pos + Math.max(character, 0), text.length())
    }
}
