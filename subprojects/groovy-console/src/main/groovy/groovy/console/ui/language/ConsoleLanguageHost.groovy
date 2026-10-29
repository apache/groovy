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

import groovy.console.ui.Console
import groovy.transform.CompileStatic
import groovy.transform.Internal
import groovy.transform.PackageScope
import org.apache.groovy.lsp.GroovyLanguageClient
import org.codehaus.groovy.control.CompilerConfiguration
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.Diagnostic

import javax.swing.JList
import javax.swing.JPopupMenu
import javax.swing.JTextPane
import javax.swing.KeyStroke
import javax.swing.SwingUtilities
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.BadLocationException
import javax.swing.text.DefaultHighlighter
import javax.swing.text.Highlighter

import java.awt.Point
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.atomic.AtomicInteger
import java.util.prefs.Preferences

/**
 * In-process language host for Groovy Console. Speaks to
 * {@code GroovyLanguageServer} with the same LSP messages a remote
 * editor sends. Alt+/ offers completions and does not bump the
 * highlight generation. The constructor and {@link #bind} publish the
 * console classpath and imports. A later query resends them only when
 * the loader identity or the classpath URLs changed, so an unchanged
 * buffer reuses the compiled snapshot. Typing debounces diagnostic highlights.
 * Implementation detail of Groovy Console, not an embedding API.
 */
@CompileStatic
@Internal
final class ConsoleLanguageHost implements AutoCloseable {

    /** This editor's unsaved buffer. The server does not assign the name. */
    @PackageScope
    static final String BUFFER_URI = 'groovy-buffer:console'

    private static final int DEBOUNCE_MS = 150

    private final GroovyLanguageClient language
    private final JTextPane input
    private final CompletionPopup completions
    private final DocumentListener documentListener
    private final KeyAdapter completionKeys
    private final DefaultHighlighter.DefaultHighlightPainter errorPainter = new WavyUnderlinePainter()
    private final List<Object> highlights = []
    private final AtomicInteger generation = new AtomicInteger()
    private final ScheduledExecutorService debounce = Executors.newSingleThreadScheduledExecutor { Runnable r ->
        Thread thread = new Thread(r, 'groovy-console-language')
        thread.daemon = true
        thread
    }
    private volatile boolean closed
    /** Set on Alt+/ so the following keyTyped does not insert '/' or Option+/. */
    private boolean swallowNextTyped
    /** Press had no character; the next typed '/' may still be Alt+/. */
    private boolean awaitingTypedSlash
    private volatile ClassLoader parent
    private volatile CompilerConfiguration configuration
    /** True after the current loader identity has been sent. */
    private boolean loaderPublished
    private ClassLoader publishedLoader
    private List<String> publishedClasspath
    private ScheduledFuture<?> pendingHighlight
    private ScheduledFuture<?> pendingCompletion
    /**
     * Counted down when {@link #close()} has released the language client.
     * On the event thread that release runs in the background, so the thread
     * can return while a compile still holds the session lock.
     */
    @PackageScope
    final CountDownLatch languageReleased = new CountDownLatch(1)

    private ConsoleLanguageHost(JTextPane input, ClassLoader loader, CompilerConfiguration configuration) {
        this.input = input
        this.completions = new CompletionPopup(input)
        this.parent = loader
        this.configuration = configuration
        this.language = GroovyLanguageClient.open(BUFFER_URI, textOf(input), true)
        publishSettings()
        this.documentListener = new DocumentListener() {
            @Override
            void insertUpdate(DocumentEvent e) {
                completions.onDocumentEdit(e)
                schedule()
            }

            @Override
            void removeUpdate(DocumentEvent e) {
                completions.onDocumentEdit(e)
                schedule()
            }

            @Override
            void changedUpdate(DocumentEvent e) { }
        }
        this.completionKeys = new KeyAdapter() {
            @Override
            void keyPressed(KeyEvent e) {
                if (completions.isOpen() && completions.handleCompletionKey(e)) {
                    return
                }
                if (isCompletionChord(e)) {
                    showCompletions()
                    e.consume()
                    swallowNextTyped = true
                    awaitingTypedSlash = false
                    return
                }
                awaitingTypedSlash = CompletionChord.awaitsTypedSlash(e, completionStroke())
            }

            @Override
            void keyTyped(KeyEvent e) {
                if (awaitingTypedSlash && e.keyChar == '/' as char) {
                    awaitingTypedSlash = false
                    showCompletions()
                    e.consume()
                    return
                }
                awaitingTypedSlash = false
                if (swallowNextTyped) {
                    swallowNextTyped = false
                    e.consume()
                }
            }

            @Override
            void keyReleased(KeyEvent e) {
                if (isCompletionChord(e)) {
                    swallowNextTyped = false
                }
            }
        }
        if (input?.document != null) {
            input.document.addDocumentListener(documentListener)
            input.addKeyListener(completionKeys)
        }
    }

    /**
     * @param input editor pane
     * @param loader GroovyShell class loader
     * @return a host bound to {@code input}
     */
    static ConsoleLanguageHost attach(JTextPane input, ClassLoader loader) {
        attach(input, loader, null)
    }

    /**
     * @param input editor pane
     * @param loader GroovyShell class loader
     * @param configuration console compiler configuration
     * @return a host bound to {@code input}
     */
    static ConsoleLanguageHost attach(JTextPane input, ClassLoader loader, CompilerConfiguration configuration) {
        new ConsoleLanguageHost(input, loader, configuration)
    }

    /**
     * Lenient lexer tokens for the token viewer.
     *
     * @param text buffer text
     * @return token maps, or empty when the host is closed
     */
    List<Map<String, Object>> tokens(String text) {
        if (closed) {
            return []
        }
        language.tokens(text)
    }

    /**
     * Lexer tokens for a one-shot viewer. Used when Compiler Assistance is
     * off and no host is attached. The client is closed before returning.
     *
     * @param text buffer text
     * @param loader shell class loader
     * @param configuration console compiler configuration
     * @return token maps
     */
    @PackageScope
    static List<Map<String, Object>> tokensOf(String text, ClassLoader loader, CompilerConfiguration configuration) {
        GroovyLanguageClient client = openInspect(loader, configuration)
        try {
            return client.tokens(text ?: '')
        } finally {
            client.close()
        }
    }

    /**
     * Concrete syntax tree for the CST viewer.
     *
     * @param text buffer text
     * @return a tree map, or empty when the host is closed
     */
    Map<String, Object> cst(String text) {
        if (closed) {
            return [name: '', text: '', children: []]
        }
        language.cst(text)
    }

    /**
     * Concrete syntax tree for a one-shot viewer. Used when Compiler
     * Assistance is off and no host is attached. The client is closed
     * before returning.
     *
     * @param text buffer text
     * @param loader shell class loader
     * @param configuration console compiler configuration
     * @return a tree map
     */
    @PackageScope
    static Map<String, Object> cstOf(String text, ClassLoader loader, CompilerConfiguration configuration) {
        GroovyLanguageClient client = openInspect(loader, configuration)
        try {
            return client.cst(text ?: '')
        } finally {
            client.close()
        }
    }

    private static GroovyLanguageClient openInspect(ClassLoader loader, CompilerConfiguration configuration) {
        GroovyLanguageClient client = GroovyLanguageClient.open('groovy-buffer:inspect', '', false)
        client.parentLoader(loader)
        if (configuration != null) {
            client.hostConfiguration(configuration)
        }
        client.settings([
                classpath: GroovyLanguageClient.classpathOf(loader),
                scriptBaseClass: configuration?.scriptBaseClass ?: ''
        ])
        client
    }

    /**
     * Compiles {@code text} immediately and returns diagnostics.
     *
     * @param text buffer text
     * @return hits in 0-based UTF-16 coordinates, or empty when the host is closed
     */
    @PackageScope
    List<Hit> compileAndDiagnostics(String text) {
        if (closed) {
            return []
        }
        publishLoader()
        language.compile(text).collect { Hit.from(it) }
    }

    /**
     * Completions at a 0-based UTF-16 position.
     *
     * @param text buffer text
     * @param line 0-based line
     * @param character 0-based character
     * @return candidates
     */
    @PackageScope
    List<Candidate> complete(String text, int line, int character) {
        if (closed) {
            return []
        }
        publishLoader()
        language.complete(text, line, character).collect { Candidate.from(it) }
    }

    /**
     * Hover markdown at a 0-based UTF-16 position.
     *
     * @param text buffer text
     * @param line 0-based line
     * @param character 0-based character
     * @return markdown
     */
    @PackageScope
    String describe(String text, int line, int character) {
        if (closed) {
            return ''
        }
        publishLoader()
        language.hover(text, line, character)
    }

    /**
     * Compiles the current editor text and paints diagnostic highlights.
     * The compile runs on the caller. File → Open calls this from its
     * worker, then the paint is applied on the EDT before this method
     * returns. Typing uses {@link #schedule()} instead.
     */
    void refresh() {
        if (closed) {
            return
        }
        int gen = generation.incrementAndGet()
        pendingHighlight?.cancel(false)
        String text = readEditorText()
        List<Hit> hits = compileAndDiagnostics(text)
        if (closed || gen != generation.get()) {
            return
        }
        Runnable draw = {
            if (!closed && gen == generation.get()) {
                paint(text, hits)
            }
        }
        if (SwingUtilities.isEventDispatchThread()) {
            draw.run()
            return
        }
        try {
            SwingUtilities.invokeAndWait(draw)
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt()
        } catch (InvocationTargetException ignored) {
        }
    }

    /** Document text, read on the EDT. Swing documents are not thread-safe. */
    private String readEditorText() {
        if (SwingUtilities.isEventDispatchThread()) {
            return textOf(input)
        }
        String[] holder = new String[1]
        try {
            SwingUtilities.invokeAndWait { holder[0] = textOf(input) }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt()
            return ''
        } catch (InvocationTargetException ignored) {
            return ''
        }
        return holder[0] == null ? '' : holder[0]
    }

    /**
     * Offers completions at the caret. Used by Alt+/ and tests.
     */
    @PackageScope
    void completeAtCaret() {
        showCompletions()
    }

    /**
     * Records the GroovyShell class loader, republishes the current
     * compiler configuration, and schedules a background recompile so
     * highlights catch up without a keystroke.
     *
     * @param loader GroovyShell class loader
     */
    void setParentLoader(ClassLoader loader) {
        bind(loader, configuration)
    }

    /**
     * Records the console compiler configuration (imports, AST customizers)
     * and schedules a recompile so query compiles match {@code GroovyShell}.
     * Call this after replacing the configuration or editing a customizer
     * in place.
     *
     * @param configuration console configuration, or {@code null}
     */
    void setCompilerConfiguration(CompilerConfiguration configuration) {
        bind(parent, configuration)
    }

    /**
     * Records the shell loader and compiler configuration, then schedules
     * a recompile. Call this after replacing the configuration or editing
     * its customizers in place. Queries publish the loader only.
     *
     * @param loader GroovyShell class loader
     * @param configuration console compiler configuration
     */
    void bind(ClassLoader loader, CompilerConfiguration configuration) {
        if (closed) {
            return
        }
        this.parent = loader
        this.configuration = configuration
        publishSettings()
        schedule()
    }

    /** Classpath URLs only, and only when they or the loader identity changed. */
    private void publishLoader() {
        publishParent()
        List<String> classpath = GroovyLanguageClient.classpathOf(parent)
        if (classpath != publishedClasspath) {
            language.settings([classpath: classpath])
            publishedClasspath = classpath
        }
    }

    private void publishSettings() {
        publishParent()
        List<String> classpath = GroovyLanguageClient.classpathOf(parent)
        language.hostConfiguration(configuration)
        // The configuration object already carries imports and AST customizers.
        // Repeating them as strings would add a second ImportCustomizer and
        // bind an aliased type under its simple name as well.
        language.settings([
                classpath: classpath,
                scriptBaseClass: configuration?.scriptBaseClass ?: ''
        ])
        publishedClasspath = classpath
    }

    private void publishParent() {
        if (loaderPublished && sameLoader(publishedLoader, parent)) {
            return
        }
        language.parentLoader(parent)
        publishedLoader = parent
        loaderPublished = true
    }

    private static boolean sameLoader(ClassLoader published, ClassLoader current) {
        if (published == null) {
            return current == null
        }
        return published.is(current)
    }

    @Override
    void close() {
        if (closed) {
            return
        }
        closed = true
        generation.incrementAndGet()
        pendingHighlight?.cancel(false)
        pendingCompletion?.cancel(false)
        if (input != null) {
            if (input.document != null) {
                input.document.removeDocumentListener(documentListener)
            }
            input.removeKeyListener(completionKeys)
            completions.hideCompletion()
        }
        debounce.shutdownNow()
        // A compile holds the session lock until it finishes, and shutdownNow
        // cannot interrupt it. Waiting for that lock on the event thread
        // freezes the window for the rest of the compile.
        Runnable release = {
            try {
                try {
                    debounce.awaitTermination(5, TimeUnit.SECONDS)
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt()
                }
                language.close()
            } finally {
                languageReleased.countDown()
            }
        }
        if (SwingUtilities.isEventDispatchThread()) {
            // Not a daemon: the window returns at once, and the process still
            // waits until the client, its class loaders, and its temp output
            // are released. A daemon would die with the last frame.
            Thread reaper = new Thread(release, 'groovy-console-language-close')
            reaper.start()
        } else {
            release.run()
        }
    }

    private void schedule() {
        if (closed || debounce.isShutdown()) {
            return
        }
        int gen = generation.incrementAndGet()
        String text = textOf(input)
        pendingHighlight?.cancel(false)
        try {
            pendingHighlight = debounce.schedule({
                runHighlight(gen, text)
            }, DEBOUNCE_MS, TimeUnit.MILLISECONDS)
        } catch (RejectedExecutionException ignored) {
        }
    }

    private void runHighlight(int gen, String text) {
        try {
            if (closed || gen != generation.get()) {
                return
            }
            def hits = compileAndDiagnostics(text)
            if (closed || gen != generation.get()) {
                return
            }
            SwingUtilities.invokeLater {
                if (!closed && gen == generation.get()) {
                    paint(text, hits)
                }
            }
        } catch (RuntimeException ignored) {
        }
    }

    private void paint(String text, List<Hit> hits) {
        def highlighter = input?.highlighter
        if (highlighter == null) {
            return
        }
        highlights.each { highlighter.removeHighlight(it) }
        highlights.clear()
        hits.each { hit ->
            int[] span = GroovyLanguageClient.spanOf(text, hit.startLine, hit.startCharacter,
                    hit.endLine, hit.endCharacter, false)
            if (span[0] >= span[1]) {
                return
            }
            Object tag = addErrorHighlight(highlighter, span[0], span[1], errorPainter)
            if (tag != null) {
                highlights << tag
            }
        }
        if (hits) {
            input.toolTipText = hits.take(3).collect { it.message() }.join('\n')
        } else {
            input.toolTipText = null
        }
    }

    private static Object addErrorHighlight(Highlighter highlighter, int start, int end,
                                            DefaultHighlighter.DefaultHighlightPainter painter) {
        try {
            return highlighter.addHighlight(start, end, painter)
        } catch (BadLocationException ignored) {
            return null
        }
    }


    private void showCompletions() {
        if (closed || input == null || debounce.isShutdown()) {
            return
        }
        String text = textOf(input)
        int caret = Math.min(Math.max(input.caretPosition, 0), text.length())
        int[] position = GroovyLanguageClient.positionOf(text, caret)
        int gen = generation.get()
        pendingCompletion?.cancel(false)
        try {
            pendingCompletion = debounce.schedule({
                runCompletions(gen, text, position[0], position[1])
            }, 0, TimeUnit.MILLISECONDS)
        } catch (RejectedExecutionException ignored) {
        }
    }

    private void runCompletions(int gen, String text, int line, int character) {
        try {
            if (closed || gen != generation.get()) {
                return
            }
            def items = complete(text, line, character)
            SwingUtilities.invokeLater {
                if (!closed && gen == generation.get()) {
                    completions.showMenu(items)
                }
            }
        } catch (RuntimeException ignored) {
        }
    }


    @PackageScope
    JPopupMenu completionMenu(List<Candidate> items) {
        completions.completionMenu(items)
    }

    @PackageScope
    JList<Candidate> armCompletion(List<Candidate> items, int anchor) {
        completions.armCompletion(items, anchor)
    }

    @PackageScope
    boolean completionShowing() {
        completions.completionShowing()
    }

    @PackageScope
    void acceptCompletion(Candidate item) {
        completions.acceptCompletion(item)
    }

    @PackageScope
    static int[] completionCellSize(JList<Candidate> list, List<Candidate> items) {
        CompletionPopup.completionCellSize(list, items)
    }

    @PackageScope
    static int completionRowAt(JList<?> list, Point point) {
        CompletionPopup.completionRowAt(list, point)
    }

    @PackageScope
    static int prefixStart(String text, int caret) {
        CompletionPopup.prefixStart(text, caret)
    }

    @PackageScope
    static boolean matchesPrefix(String label, String prefix) {
        CompletionPopup.matchesPrefix(label, prefix)
    }

    @PackageScope
    static String completionInsert(Candidate item) {
        CompletionPopup.completionInsert(item)
    }

    @PackageScope
    static void insertAtCaret(JTextPane pane, String insert) {
        if (pane?.document == null) {
            return
        }
        String text = textOf(pane)
        String piece = insert ?: ''
        if (piece.isEmpty()) {
            return
        }
        int caret = Math.min(Math.max(pane.caretPosition, 0), text.length())
        int start = caret
        while (start > 0 && Character.isJavaIdentifierPart(text.charAt(start - 1))) {
            start--
        }
        int end = caret
        while (end < text.length() && Character.isJavaIdentifierPart(text.charAt(end))) {
            end++
        }
        String prefix = text.substring(start, caret)
        try {
            if (prefix && piece.startsWith(prefix)) {
                if (end > caret) {
                    pane.document.remove(caret, end - caret)
                }
                pane.document.insertString(caret, piece.substring(prefix.length()), null)
            } else if (start < end) {
                pane.document.remove(start, end - start)
                pane.document.insertString(start, piece, null)
            } else {
                pane.document.insertString(caret, piece, null)
            }
        } catch (BadLocationException ignored) {
        }
    }

    /**
     * Document text, not {@link JTextPane#getText()}. The editor kit
     * rewrites line breaks using the document's end-of-line property, so
     * the returned string can be longer than the document. Caret offsets
     * are document offsets; using the rewritten text shifts the caret
     * backward by one character per break and makes {@code a.} complete
     * as the prefix {@code a}.
     */
    @PackageScope
    static String textOf(JTextPane pane) {
        if (pane?.document == null) {
            return ''
        }
        try {
            return pane.document.getText(0, pane.document.length) ?: ''
        } catch (BadLocationException ignored) {
            return ''
        }
    }

    /**
     * The completion chord from the console preference
     * {@code completionShortcut}. The default is Alt+/. Ctrl+Space is not
     * the default: it switches the input method on Chinese desktops and on
     * macOS. Shift is accepted when the layout needs it to type the
     * configured character, which is how Swedish keyboards produce {@code /}.
     * Another stroke, for example {@code ctrl SPACE}, can be stored instead.
     */
    @PackageScope
    static boolean isCompletionChord(KeyEvent e) {
        CompletionChord.matches(e, completionStroke())
    }

    @PackageScope
    static KeyStroke completionStroke() {
        Preferences preferences = (Preferences) Console.prefs
        String spec = preferences.get(Console.COMPLETION_SHORTCUT, Console.DEFAULT_COMPLETION_SHORTCUT)
        KeyStroke stroke = CompletionChord.parse(spec)
        stroke == null ? CompletionChord.parse(Console.DEFAULT_COMPLETION_SHORTCUT) : stroke
    }

    @PackageScope
    static final class Hit {
        final int startLine
        final int startCharacter
        final int endLine
        final int endCharacter
        final String message

        private Hit(int startLine, int startCharacter, int endLine, int endCharacter, String message) {
            this.startLine = startLine
            this.startCharacter = startCharacter
            this.endLine = endLine
            this.endCharacter = endCharacter
            this.message = message
        }

        String message() { message }

        static Hit from(Diagnostic diagnostic) {
            def range = diagnostic?.range
            def start = range?.start
            def end = range?.end
            new Hit((start?.line ?: 0) as int, (start?.character ?: 0) as int,
                    (end?.line ?: 0) as int, (end?.character ?: 0) as int,
                    GroovyLanguageClient.diagnosticText(diagnostic))
        }
    }

    @PackageScope
    static final class Candidate {
        final String label
        final String kind
        final String detail
        final String insertText
        /** Text inside the parentheses, or {@code null} when this is not a method. */
        final String parameters
        /** Return type, or {@code null} when this is not a method. */
        final String returns

        private Candidate(String label, String kind, String detail, String insertText,
                          String parameters, String returns) {
            this.label = label
            this.kind = kind
            this.detail = detail
            this.insertText = insertText
            this.parameters = parameters
            this.returns = returns
        }

        String label() { label }

        String kind() { kind }

        String detail() { detail }

        String insertText() { insertText }

        String parameters() { parameters }

        String returns() { returns }

        static Candidate from(CompletionItem item) {
            GroovyLanguageClient.MethodDetail method = GroovyLanguageClient.methodDetail(item)
            new Candidate(item?.label, item?.kind?.name(), item?.detail, item?.insertText ?: item?.label,
                    method?.parameters, method?.returns)
        }

        static Candidate of(String label, String kind, String detail, String insertText) {
            of(label, kind, detail, insertText, null, null)
        }

        static Candidate of(String label, String kind, String detail, String insertText,
                            String parameters, String returns) {
            new Candidate(label, kind, detail, insertText, parameters, returns)
        }
    }

}
