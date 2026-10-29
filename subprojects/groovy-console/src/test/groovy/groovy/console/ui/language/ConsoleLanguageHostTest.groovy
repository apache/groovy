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
import groovy.console.ui.text.SmartDocumentFilter
import org.apache.groovy.lsp.GroovyLanguageClient
import org.apache.groovy.lsp.internal.compile.CompilerSettings
import org.apache.groovy.lsp.internal.compile.GroovyCompiler
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.junit.jupiter.api.Test

import javax.swing.Box
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPopupMenu
import javax.swing.JScrollPane
import javax.swing.JTextPane
import javax.swing.SwingUtilities
import javax.swing.event.DocumentEvent
import javax.swing.text.AttributeSet
import javax.swing.text.BadLocationException
import javax.swing.text.DefaultEditorKit
import javax.swing.text.DefaultHighlighter
import javax.swing.text.DefaultStyledDocument
import javax.swing.text.Element
import javax.swing.text.Highlighter
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Font
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.awt.Component
import java.awt.Container
import java.awt.Point
import java.awt.Rectangle
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import java.awt.event.ActionEvent
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent

final class ConsoleLanguageHostTest {

    @Test
    void completionChordFollowsThePreference() {
        def previous = Console.prefs.get(Console.COMPLETION_SHORTCUT, null)
        def source = new JTextPane()
        try {
            Console.prefs.remove(Console.COMPLETION_SHORTCUT)
            def alt = new KeyEvent(source, KeyEvent.KEY_PRESSED, 0, InputEvent.ALT_DOWN_MASK, KeyEvent.VK_SLASH, '/' as char)
            assert ConsoleLanguageHost.isCompletionChord(alt)
            def swedish = new KeyEvent(source, KeyEvent.KEY_PRESSED, 0,
                    InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK, KeyEvent.VK_SLASH, '/' as char)
            assert ConsoleLanguageHost.isCompletionChord(swedish)
            def question = new KeyEvent(source, KeyEvent.KEY_PRESSED, 0,
                    InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK, KeyEvent.VK_SLASH, '?' as char)
            assert !ConsoleLanguageHost.isCompletionChord(question)
            def space = new KeyEvent(source, KeyEvent.KEY_PRESSED, 0, InputEvent.CTRL_DOWN_MASK, KeyEvent.VK_SPACE, ' ' as char)
            assert !ConsoleLanguageHost.isCompletionChord(space)
            Console.prefs.put(Console.COMPLETION_SHORTCUT, 'ctrl SPACE')
            assert ConsoleLanguageHost.isCompletionChord(space)
            assert !ConsoleLanguageHost.isCompletionChord(alt)
            def withShift = new KeyEvent(source, KeyEvent.KEY_PRESSED, 0,
                    InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK, KeyEvent.VK_SLASH, '/' as char)
            Console.prefs.put(Console.COMPLETION_SHORTCUT, 'alt shift SLASH')
            assert ConsoleLanguageHost.isCompletionChord(withShift)
            Console.prefs.put(Console.COMPLETION_SHORTCUT, 'not a stroke')
            assert ConsoleLanguageHost.completionStroke().keyCode == KeyEvent.VK_SLASH
            assert !ConsoleLanguageHost.isCompletionChord(null)
        } finally {
            if (previous == null) {
                Console.prefs.remove(Console.COMPLETION_SHORTCUT)
            } else {
                Console.prefs.put(Console.COMPLETION_SHORTCUT, previous)
            }
        }
    }

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
            assert items.any { it.label() == 'ping' || it.insertText()?.contains('ping') }
            host.setParentLoader(ConsoleLanguageHostTest.classLoader)
            host.refresh()
        } finally {
            host.close()
        }
    }

    @Test
    void hostCompilerConfigurationImportsAreVisible() {
        def configuration = new CompilerConfiguration()
        def imports = new ImportCustomizer()
        imports.addImports('java.util.concurrent.ConcurrentHashMap')
        imports.addStarImports('java.nio.file')
        configuration.addCompilationCustomizers(imports)
        def pane = new JTextPane()
        def host = ConsoleLanguageHost.attach(pane, ConsoleLanguageHostTest.classLoader, configuration)
        try {
            def hits = host.compileAndDiagnostics('ConcurrentHashMap m\n')
            assert !hits.any { (it.message() ?: '').contains('ConcurrentHashMap') }
            host.setCompilerConfiguration(null)
            def without = host.compileAndDiagnostics('ConcurrentHashMap m\n')
            assert without.any { (it.message() ?: '').contains('ConcurrentHashMap') }
            host.setCompilerConfiguration(configuration)
            def again = host.compileAndDiagnostics('ConcurrentHashMap m\n')
            assert !again.any { (it.message() ?: '').contains('ConcurrentHashMap') }
        } finally {
            host.close()
        }
    }

    @Test
    void offsetOfPositionOfAndPlainInsertCoverEdges() {
        assert GroovyLanguageClient.offsetOf(null, 0, 0) == 0
        assert GroovyLanguageClient.offsetOf('', 0, 0) == 0
        assert GroovyLanguageClient.offsetOf('ab\ncd', 0, 0) == 0
        assert GroovyLanguageClient.offsetOf('ab\ncd', 1, 1) == 4
        assert GroovyLanguageClient.offsetOf('ab\ncd', 9, 0) == 5
        assert GroovyLanguageClient.offsetOf('ab', -1, 99) == 2
        assert GroovyLanguageClient.offsetOf('ab\ncd', 1, -3) == 3
        assert GroovyLanguageClient.offsetOf('ab\ncd', 0, 99) == 2
        assert GroovyLanguageClient.offsetOf('ab\rcd', 0, 99) == 2
        assert GroovyLanguageClient.positionOf(null, 0) == [0, 0] as int[]
        assert GroovyLanguageClient.positionOf('', 1) == [0, 0] as int[]
        assert GroovyLanguageClient.positionOf('ab\ncd', 0) == [0, 0] as int[]
        assert GroovyLanguageClient.positionOf('ab\ncd', 4) == [1, 1] as int[]
        assert GroovyLanguageClient.positionOf('ab\ncd', 99) == [1, 2] as int[]
        assert GroovyLanguageClient.positionOf('ab\r\ncd', 4) == [1, 0] as int[]
        assert GroovyLanguageClient.positionOf('ab\rcd', 3) == [1, 0] as int[]
        assert GroovyLanguageClient.offsetOf('ab\r\ncd', 1, 0) == 4
        assert GroovyLanguageClient.offsetOf('ab\rcd', 1, 0) == 3
        assert GroovyLanguageClient.flattenSnippet(null) == ''
        assert GroovyLanguageClient.flattenSnippet('') == ''
        assert GroovyLanguageClient.flattenSnippet('length') == 'length'
        assert GroovyLanguageClient.flattenSnippet('class ${1:Name} {\n    $0\n}\n') == 'class Name {\n    \n}\n'
        assert GroovyLanguageClient.flattenSnippet('$0') == ''
        assert GroovyLanguageClient.flattenSnippet('${1:foo}$2') == 'foo'
        assert GroovyLanguageClient.importsOf(null) == ''
        assert GroovyLanguageClient.starImportsOf(null) == ''
        def starOnly = new ImportCustomizer()
        starOnly.addStarImports('java.io')
        def importsField = ImportCustomizer.getDeclaredField('imports')
        importsField.accessible = true
        def starImport = importsField.get(starOnly)[0]
        def starField = starImport.getClass().getDeclaredField('star')
        starField.accessible = true
        starField.set(starImport, null)
        def starConfig = new CompilerConfiguration()
        starConfig.addCompilationCustomizers(starOnly)
        assert GroovyLanguageClient.starImportsOf(starConfig) == ''
        assert GroovyLanguageClient.classpathOf(null).empty
        assert GroovyLanguageClient.classpathOf(new ClassLoader() {}).empty
        def http = new URLClassLoader([URI.create('http://example.invalid/a').toURL()] as URL[])
        assert GroovyLanguageClient.classpathOf(http).empty
        http.close()
        def file = new URLClassLoader([new File('.').toURI().toURL()] as URL[])
        assert GroovyLanguageClient.classpathOf(file).size() == 1
        file.close()
        assert ConsoleLanguageHost.Hit.from(null).message() == ''
        def emptyCandidate = ConsoleLanguageHost.Candidate.from(null)
        assert emptyCandidate.label() == null
        assert emptyCandidate.kind() == null
        assert emptyCandidate.detail() == null
        assert emptyCandidate.insertText() == null
        assert emptyCandidate.parameters() == null
        assert emptyCandidate.returns() == null
        def methodCandidate = ConsoleLanguageHost.Candidate.of('add', 'Method', 'boolean add(Object e)', 'add', 'Object e', 'boolean')
        assert methodCandidate.detail() == 'boolean add(Object e)'
        assert methodCandidate.kind() == 'Method'
        assert methodCandidate.insertText() == 'add'
        assert methodCandidate.parameters() == 'Object e'
        assert methodCandidate.returns() == 'boolean'
        assert ConsoleLanguageHost.BUFFER_URI.toString() == 'groovy-buffer:console'
    }

    @Test
    void highlightSpanClampsInvertedAndZeroWidth() {
        assert GroovyLanguageClient.spanOf('ab\ncd', 0, 2, 0, 0, false) == [2, 3] as int[]
        assert GroovyLanguageClient.spanOf('x', 0, 0, 0, 0, false) == [0, 1] as int[]
        assert GroovyLanguageClient.spanOf('', 0, 0, 0, 0, false) == [0, 0] as int[]
        assert GroovyLanguageClient.spanOf(null, 0, 0, 0, 0, false) == [0, 0] as int[]
        assert GroovyLanguageClient.spanOf('ab', 0, 0, 0, 2, false) == [0, 2] as int[]
        assert GroovyLanguageClient.spanOf('abc', 0, 3, 0, 3, false) == [3, 3] as int[]
        assert GroovyLanguageClient.spanOf('abc', 9, 0, 9, 0, false) == [3, 3] as int[]
        assert GroovyLanguageClient.spanOf('abc', 0, 3, 0, 3, true) == [2, 3] as int[]
    }

    @Test
    void diagnosticMarkIsAThinRedWaveUnderTheSpan() {
        def pane = new JTextPane()
        pane.background = Color.WHITE
        pane.foreground = Color.BLACK
        pane.font = new Font(Font.MONOSPACED, Font.PLAIN, 16)
        pane.text = "def a = '"
        pane.setSize(360, 48)
        pane.UI.getRootView(pane).setSize(360, 48)
        def host = ConsoleLanguageHost.attach(pane, ConsoleLanguageHostTest.classLoader)
        try {
            host.refresh()
            def marks = pane.highlighter.highlights
            assert marks.length > 0
            def painter = marks[0].painter
            assert painter instanceof DefaultHighlighter.DefaultHighlightPainter
            assert ((DefaultHighlighter.DefaultHighlightPainter) painter).color == new Color(220, 38, 38)
            def image = new BufferedImage(360, 48, BufferedImage.TYPE_INT_RGB)
            def graphics = image.createGraphics()
            graphics.color = Color.WHITE
            graphics.fillRect(0, 0, image.width, image.height)
            pane.paint(graphics)
            graphics.dispose()
            int pink = 0
            int red = 0
            for (int y = 0; y < image.height; y++) {
                for (int x = 0; x < image.width; x++) {
                    int rgb = image.getRGB(x, y)
                    int r = (rgb >> 16) & 0xff
                    int g = (rgb >> 8) & 0xff
                    int b = rgb & 0xff
                    if (r > 240 && g > 150 && g < 210 && b > 150 && b < 210) {
                        pink++
                    }
                    if (r > 180 && g < 90 && b < 90) {
                        red++
                    }
                }
            }
            // A filled cell is hundreds of pixels of the old pink. Antialiasing
            // the thin wave against white leaves only a few pink-looking pixels.
            assert pink < 40
            assert red > 0
            assert red < 80
            def wave = image.createGraphics()
            wave.color = Color.WHITE
            wave.fillRect(0, 0, image.width, image.height)
            painter.paint(wave, marks[0].startOffset, marks[0].endOffset, new Rectangle(0, 0, 360, 48), pane)
            pane.text = "a\nb\nc"
            pane.UI.getRootView(pane).setSize(360, 80)
            painter.paint(wave, 0, pane.document.length, new Rectangle(0, 0, 360, 80), pane)
            def root = pane.UI.getRootView(pane)
            painter.paintLayer(wave, root.startOffset, root.endOffset, new Rectangle2D.Double(1, 2, 30, 18), pane, root)
            painter.paintLayer(wave, -1, -1, new Rectangle(0, 0, 10, 10), pane, root)
            def bare = new JTextPane()
            painter.paint(wave, 0, 0, new Rectangle(0, 0, 10, 10), bare)
            wave.dispose()
        } finally {
            host.close()
        }
    }

    @Test
    void incompleteInputDoesNotPaintTheCharacterBeforeTheCaret() {
        ['def', 'def ', 'def a ='].each { source ->
            def pane = new JTextPane()
            pane.text = source
            def host = ConsoleLanguageHost.attach(pane, ConsoleLanguageHostTest.classLoader)
            try {
                host.refresh()
                assert pane.highlighter.highlights.every { it.startOffset >= source.length() }
            } finally {
                host.close()
            }
        }
    }

    @Test
    void refreshPaintsAndClearsTooltipsSynchronously() {
        def pane = new JTextPane()
        pane.text = 'class Broken { def x = }\n'
        def host = ConsoleLanguageHost.attach(pane, ConsoleLanguageHostTest.classLoader)
        try {
            host.refresh()
            assert pane.highlighter.highlights.length > 0
            assert pane.toolTipText
            pane.caretPosition = pane.text.indexOf('Broken')
            host.completeAtCaret()
            pane.dispatchEvent(new KeyEvent(pane, KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
                    InputEvent.ALT_DOWN_MASK, KeyEvent.VK_SLASH, '/' as char))
            pane.text = 'class Ok {}\n'
            host.refresh()
            assert pane.highlighter.highlights.length == 0
            assert pane.toolTipText == null
            pane.highlighter = new DefaultHighlighter() {
                @Override
                Object addHighlight(int p0, int p1, Highlighter.HighlightPainter p) throws BadLocationException {
                    throw new BadLocationException('test', p0)
                }
            }
            pane.text = 'class Broken { def x = }\n'
            host.refresh()
        } finally {
            host.close()
        }
    }

    @Test
    void closeThenEditDoesNotThrowAndCloseIsIdempotent() {
        def pane = new JTextPane()
        pane.text = 'class Broken { def x = }\n'
        def host = ConsoleLanguageHost.attach(pane, ConsoleLanguageHostTest.classLoader)
        try {
            pane.text = 'class Ok {}\n'
            host.completeAtCaret()
        } finally {
            host.close()
        }
        host.close()
        assert host.compileAndDiagnostics('1').empty
        assert host.complete('1', 0, 0).empty
        assert host.describe('1', 0, 0) == ''
        pane.text = 'class Later {}\n'
        pane.dispatchEvent(new KeyEvent(pane, KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
                InputEvent.ALT_DOWN_MASK, KeyEvent.VK_SLASH, '/' as char))
        def detached = ConsoleLanguageHost.attach(null, null)
        try {
            detached.refresh()
            detached.completeAtCaret()
            detached.setParentLoader(null)
            assert detached.compileAndDiagnostics(null) != null
        } finally {
            detached.close()
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
    }

    @Test
    void completeAtCaretFlushesMenuPath() {
        def pane = new JTextPane()
        pane.text = '''\
            class Sample {
                def ping() { 1 }
            }
            new Sample().
            '''.stripIndent()
        pane.caretPosition = pane.text.length()
        def host = ConsoleLanguageHost.attach(pane, ConsoleLanguageHostTest.classLoader)
        try {
            host.completeAtCaret()
            long deadline = System.nanoTime() + 5_000_000_000L
            boolean shown = false
            while (System.nanoTime() < deadline) {
                SwingUtilities.invokeAndWait {}
                if (host.completionShowing()) {
                    shown = true
                    break
                }
                Thread.sleep(20)
            }
            assert shown
        } finally {
            host.close()
        }
    }

    @Test
    void compilePicksUpInPlaceLoaderUrlChanges() {
        def loader = new GroovyClassLoader()
        def host = ConsoleLanguageHost.attach(new JTextPane(), loader)
        try {
            host.compileAndDiagnostics('1')
            loader.addURL(URI.create('file:/tmp/extra-language-host.jar').toURL())
            assert host.compileAndDiagnostics('1') != null
        } finally {
            host.close()
        }
    }

    @Test
    void setParentLoaderThenRefreshSeesNewClasses() {
        def loader = new GroovyClassLoader()
        loader.parseClass('class CpHit {}')
        def host = ConsoleLanguageHost.attach(new JTextPane(), ConsoleLanguageHostTest.classLoader)
        try {
            def before = host.compileAndDiagnostics('CpHit x\n')
            host.setParentLoader(loader)
            def after = host.compileAndDiagnostics('CpHit x\n')
            assert before.any { (it.message() ?: '').contains('CpHit') }
            assert !after.any { (it.message() ?: '').contains('CpHit') }
        } finally {
            host.close()
        }
    }

    @Test
    void refreshPaintsDiagnosticsOnASmartFilterDocument() {
        def doc = new DefaultStyledDocument()
        def filter = new SmartDocumentFilter(doc)
        doc.documentFilter = filter
        def pane = new JTextPane()
        pane.document = doc
        def host = ConsoleLanguageHost.attach(pane, ConsoleLanguageHostTest.classLoader)
        try {
            pane.text = 'class Broken { def x = }\n'
            host.refresh()
            assert pane.highlighter.highlights.length > 0
            assert filter.isLatest()
            assert filter.latestTokenList.any { it.text == '{' }
        } finally {
            host.close()
        }
    }

    @Test
    void dotCompletionListsMembersAndInsertsTheWholeName() {
        def pane = new JTextPane()
        pane.text = 'def a = new ArrayList()\ndef a.'
        pane.document.putProperty(DefaultEditorKit.EndOfLineStringProperty, '\r\n')
        pane.caretPosition = pane.document.length
        def host = ConsoleLanguageHost.attach(pane, ConsoleLanguageHostTest.classLoader)
        try {
            String text = ConsoleLanguageHost.textOf(pane)
            int caret = Math.min(pane.caretPosition, text.length())
            int[] pos = GroovyLanguageClient.positionOf(text, caret)
            assert pos[0] == 1
            assert pos[1] == 'def a.'.length()
            def labels = host.complete(text, pos[0], pos[1]).collect { it.label() }
            assert 'add' in labels
            assert 'get' in labels
            assert 'size' in labels
            assert !('abstract' in labels)

            def partial = 'def a = new ArrayList()\ndef a.ad'
            def partialLabels = host.complete(partial, 1, 'def a.ad'.length()).collect { it.label() }
            assert 'add' in partialLabels
            assert !('size' in partialLabels)
            assert !('abstract' in partialLabels)

            host.acceptCompletion(ConsoleLanguageHost.Candidate.of('add', 'Method', 'boolean add(Object element)', 'add(${1:element})'))
            assert pane.document.getText(0, pane.document.length).endsWith('a.add')
            assert !pane.document.getText(0, pane.document.length).contains('element')
        } finally {
            host.close()
        }
        def keyword = new JTextPane()
        keyword.text = 'def a = new ArrayList()\ndef a.'
        keyword.document.putProperty(DefaultEditorKit.EndOfLineStringProperty, '\r\n')
        keyword.caretPosition = keyword.document.length
        ConsoleLanguageHost.insertAtCaret(keyword, 'abstract')
        assert keyword.document.getText(0, keyword.document.length).endsWith('a.abstract')
        def replaced = new JTextPane()
        replaced.text = 'def a = new ArrayList()\ndef a.ad'
        replaced.caretPosition = replaced.document.length
        ConsoleLanguageHost.insertAtCaret(replaced, 'add')
        assert replaced.document.getText(0, replaced.document.length).endsWith('a.add')
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
        assert pane.text == 'name'
        pane.text = ''
        pane.caretPosition = 0
        ConsoleLanguageHost.insertAtCaret(pane, 'x')
        assert pane.text == 'x'
        ConsoleLanguageHost.insertAtCaret(null, 'x')
        ConsoleLanguageHost.insertAtCaret(pane, '')
        pane.text = 'foo.'
        pane.caretPosition = 4
        ConsoleLanguageHost.insertAtCaret(pane, 'bar')
        assert pane.text == 'foo.bar'
        pane.text = 'name'
        pane.caretPosition = 1
        ConsoleLanguageHost.insertAtCaret(pane, 'name')
        assert pane.text == 'name'
        pane.text = 'ab\ncd'
        pane.caretPosition = pane.text.length()
        def host = ConsoleLanguageHost.attach(pane, ConsoleLanguageHostTest.classLoader)
        try {
            host.completeAtCaret()
            def candidate = ConsoleLanguageHost.Candidate.of('xyz', 'method', 'String xyz(int n)', 'xyz')
            host.completionMenu([ConsoleLanguageHost.Candidate.of(null, 'method', '', 'insert')])
            host.acceptCompletion(ConsoleLanguageHost.Candidate.of('x', 'method', '', null))
            def menu = host.completionMenu([candidate])
            def list = (JList) ((JScrollPane) menu.getComponent(0)).viewport.view
            assert list.model.size == 1
            list.selectedIndex = 0
            list.actionMap.get('choose').actionPerformed(new ActionEvent(list, 0, 'choose'))
            host.acceptCompletion(candidate)
        } finally {
            host.close()
        }
    }

    @Test
    void stringCompletionMenuStaysResponsive() {
        def pane = new JTextPane()
        def text = "def a = ''\na."
        pane.text = text
        pane.caretPosition = text.length()
        def host = ConsoleLanguageHost.attach(pane, ConsoleLanguageHostTest.classLoader)
        try {
            long started = System.nanoTime()
            def items = host.complete(text, 1, 'a.'.length())
            long listed = (System.nanoTime() - started).intdiv(1_000_000L)
            def labels = items.collect { it.label() }
            assert 'splitEachLine' in labels
            assert 'sprintf' in labels
            started = System.nanoTime()
            def menu = host.completionMenu(items)
            long shown = (System.nanoTime() - started).intdiv(1_000_000L)
            def built = (JList) ((JScrollPane) menu.getComponent(0)).viewport.view
            assert built.fixedCellWidth > 0
            assert built.fixedCellHeight > 0
            assert !built.autoscrolls
            assert listed < 5_000L : "complete ${listed} ms, ${items.size()} items"
            assert shown < 500L : "menu ${shown} ms, ${items.size()} items"
        } finally {
            host.close()
        }
    }

    @Test
    void completionMenuShowsParameterTypesAndNamesAndScrolls() {
        def host = ConsoleLanguageHost.attach(new JTextPane(), ConsoleLanguageHostTest.classLoader)
        try {
            def indexed = ConsoleLanguageHost.Candidate.of('add', 'Method',
                    'void add(int index, Object element)', 'add(${1:index}, ${2:element})',
                    'int index, Object element', 'void')
            def plain = ConsoleLanguageHost.Candidate.of('add', 'Method', 'boolean add(Object e)', 'add(${1:e})',
                    'Object e', 'boolean')
            def clear = ConsoleLanguageHost.Candidate.of('clear', 'Method', 'void clear()', 'clear()', '', 'void')
            def keyword = ConsoleLanguageHost.Candidate.of('abstract', 'Keyword', null, null)
            assert CompletionPopup.SignatureParts.of(indexed).parameters == 'int index, Object element'
            assert CompletionPopup.SignatureParts.of(indexed).returns == 'void'
            assert CompletionPopup.SignatureParts.of(plain).parameters == 'Object e'
            assert CompletionPopup.SignatureParts.of(plain).returns == 'boolean'
            assert CompletionPopup.SignatureParts.of(clear).parameters == ''
            assert CompletionPopup.SignatureParts.of(keyword) == null
            def many = [indexed, plain, clear]
            20.times { many << ConsoleLanguageHost.Candidate.of("m$it", 'Method', "void m$it()", "m$it()", '', 'void') }
            def menu = host.completionMenu(many)
            def scroll = (JScrollPane) menu.getComponent(0)
            assert scroll.verticalScrollBarPolicy == JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
            def list = (JList) scroll.viewport.view
            assert list.model.size == many.size()
            assert list.visibleRowCount < list.model.size
            def row = list.cellRenderer.getListCellRendererComponent(list, indexed, 0, false, false)
            def parameters = findLabel(row, 'completion-parameters')
            def returns = findLabel(row, 'completion-return')
            def name = findLabel(row, 'completion-name')
            assert name.text == 'add'
            def badge = findNamed(row, 'completion-kind')
            assert badge.getClientProperty('mark') == 'm'
            assert badge.getClientProperty('fill') == new Color(219, 92, 92)
            assert badge.parent.getComponent(0).name == 'completion-kind'
            assert badge.parent.getComponent(1) instanceof Box.Filler
            assert badge.parent.getComponent(1).preferredSize.width == 8
            assert badge.parent.getComponent(2).name == 'completion-name'
            assert badge.preferredSize.width == 16
            assert badge.preferredSize.height == 16
            assert badge.minimumSize == badge.preferredSize
            badge.setSize(16, 16)
            def image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)
            def graphics = image.createGraphics()
            badge.paint(graphics)
            graphics.dispose()
            assert painted(image, 219, 92, 92)
            assert CompletionPopup.kindMark('Field') == 'f'
            assert CompletionPopup.kindMark('Property') == 'p'
            assert CompletionPopup.kindMark('Variable') == 'v'
            assert CompletionPopup.kindMark('Class') == 'c'
            assert CompletionPopup.kindMark('Interface') == 'i'
            assert CompletionPopup.kindMark('Enum') == 'e'
            assert CompletionPopup.kindMark('Keyword') == 'k'
            assert CompletionPopup.kindMark('Snippet') == 's'
            assert CompletionPopup.kindMark('Function') == 'm'
            assert CompletionPopup.kindMark(null) == '?'
            assert CompletionPopup.kindMark('') == '?'
            assert CompletionPopup.kindColor('Method') == new Color(219, 92, 92)
            assert CompletionPopup.kindColor('Function') == new Color(219, 92, 92)
            assert CompletionPopup.kindColor('Field') == new Color(110, 127, 232)
            assert CompletionPopup.kindColor('Property') == new Color(58, 168, 158)
            assert CompletionPopup.kindColor('Variable') == new Color(122, 138, 153)
            assert CompletionPopup.kindColor('Class') == new Color(214, 158, 54)
            assert CompletionPopup.kindColor('Interface') == new Color(86, 166, 104)
            assert CompletionPopup.kindColor('Enum') == new Color(163, 113, 212)
            assert CompletionPopup.kindColor('Keyword') == new Color(138, 122, 90)
            assert CompletionPopup.kindColor('Snippet') == new Color(76, 141, 255)
            assert CompletionPopup.kindColor(null) == new Color(154, 160, 166)
            def keywordRow = list.cellRenderer.getListCellRendererComponent(list, keyword, 2, false, false)
            assert findNamed(keywordRow, 'completion-kind').getClientProperty('mark') == 'k'
            assert findLabel(keywordRow, 'completion-parameters') == null
            def empty = list.cellRenderer.getListCellRendererComponent(list, null, 3, false, false)
            assert findNamed(empty, 'completion-kind').getClientProperty('mark') == '?'
            assert parameters.text == '(int index, Object element)'
            assert parameters.foreground != list.background
            assert parameters.foreground != list.foreground
            assert returns.text == 'void'
            assert returns.foreground == parameters.foreground
            assert returns.parent != name.parent
            BorderLayout rowLayout = (BorderLayout) returns.parent.layout
            assert rowLayout.getLayoutComponent(BorderLayout.EAST).is(returns)
            assert rowLayout.getLayoutComponent(BorderLayout.CENTER).is(name.parent)
            def selected = list.cellRenderer.getListCellRendererComponent(list, plain, 1, true, false)
            def selectedParameters = findLabel(selected, 'completion-parameters')
            def selectedReturn = findLabel(selected, 'completion-return')
            assert selectedParameters.text == '(Object e)'
            assert selectedReturn.text == 'boolean'
            assert selectedParameters.foreground == selectedReturn.foreground
            assert selectedParameters.foreground != list.selectionBackground
            assert selectedParameters.foreground != list.selectionForeground
            assert ConsoleLanguageHost.completionInsert(indexed) == 'add'
            assert ConsoleLanguageHost.completionInsert(ConsoleLanguageHost.Candidate.of(
                    'iff', 'Snippet', null, 'if (${1:c}) {\n}')) == 'if (c) {\n}'
            list.selectedIndex = 1
            list.setSize(400, list.fixedCellHeight * list.model.size)
            int hoveredIndex = 2
            int hoveredY = list.fixedCellHeight * hoveredIndex + list.fixedCellHeight.intdiv(2)
            assert ConsoleLanguageHost.completionRowAt(list, new Point(10, hoveredY)) == hoveredIndex
            assert ConsoleLanguageHost.completionRowAt(list, new Point(10, list.height + 4)) == -1
            assert ConsoleLanguageHost.completionRowAt(null, new Point(0, 0)) == -1
            assert ConsoleLanguageHost.completionRowAt(list, null) == -1
            def moved = new MouseEvent(list, MouseEvent.MOUSE_MOVED, 0, 0, 10, hoveredY, 0, false)
            list.mouseMotionListeners.each { it.mouseMoved(moved) }
            assert list.selectedIndex == hoveredIndex
            def hovered = list.cellRenderer.getListCellRendererComponent(
                    list, list.model.getElementAt(hoveredIndex), hoveredIndex, list.isSelectedIndex(hoveredIndex), false)
            assert hovered.background == list.selectionBackground
            def idle = list.cellRenderer.getListCellRendererComponent(
                    list, list.model.getElementAt(0), 0, list.isSelectedIndex(0), false)
            assert idle.background == list.background
            list.mouseMotionListeners.each {
                it.mouseMoved(new MouseEvent(list, MouseEvent.MOUSE_MOVED, 0, 0, 10, list.height + 4, 0, false))
            }
            assert list.selectedIndex == hoveredIndex
            list.mouseListeners.each {
                it.mousePressed(new MouseEvent(list, MouseEvent.MOUSE_PRESSED, 0, 0, 10, hoveredY, 1, false))
            }
        } finally {
            host.close()
        }
    }

    @Test
    void typingNarrowsTheCompletionListAndEnterInsertsTheName() {
        def pane = new JTextPane()
        pane.text = 'a.'
        pane.caretPosition = 2
        def host = ConsoleLanguageHost.attach(pane, ConsoleLanguageHostTest.classLoader)
        try {
            assert ConsoleLanguageHost.prefixStart('a.cont', 6) == 2
            assert ConsoleLanguageHost.prefixStart('a.', 2) == 2
            assert ConsoleLanguageHost.prefixStart('', 0) == 0
            assert ConsoleLanguageHost.matchesPrefix('contains', 'cont')
            assert ConsoleLanguageHost.matchesPrefix('containsAll', 'Cont')
            assert !ConsoleLanguageHost.matchesPrefix('add', 'cont')
            assert ConsoleLanguageHost.matchesPrefix('add', '')
            assert !ConsoleLanguageHost.matchesPrefix(null, 'c')
            def contains = ConsoleLanguageHost.Candidate.of('contains', 'Method',
                    'boolean contains(Object o)', 'contains')
            def containsAll = ConsoleLanguageHost.Candidate.of('containsAll', 'Method',
                    'boolean containsAll(Collection c)', 'containsAll')
            def add = ConsoleLanguageHost.Candidate.of('add', 'Method', 'boolean add(Object e)', 'add')
            def list = host.armCompletion([contains, containsAll, add], 2)
            assert host.completionShowing()
            assert list.model.size == 3
            pane.document.insertString(2, 'cont', null)
            assert list.model.size == 2
            assert list.model.getElementAt(0).label() == 'contains'
            assert list.model.getElementAt(1).label() == 'containsAll'
            pane.document.remove(5, 1)
            assert pane.document.getText(0, pane.document.length) == 'a.con'
            assert list.model.size == 2
            press(pane, KeyEvent.VK_DOWN)
            assert list.selectedIndex == 1
            press(pane, KeyEvent.VK_UP)
            assert list.selectedIndex == 0
            press(pane, KeyEvent.VK_ENTER)
            assert pane.document.getText(0, pane.document.length) == 'a.contains'
            assert !host.completionShowing()

            pane.text = 'a.'
            pane.caretPosition = 2
            host.armCompletion([contains, add], 2)
            pane.document.insertString(2, ' ', null)
            assert !host.completionShowing()

            pane.text = 'a.'
            pane.caretPosition = 2
            host.armCompletion([contains], 2)
            press(pane, KeyEvent.VK_ESCAPE)
            assert !host.completionShowing()
            assert pane.document.getText(0, pane.document.length) == 'a.'
        } finally {
            host.close()
        }
    }

    private static void press(JTextPane pane, int keyCode) {
        def event = new KeyEvent(pane, KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
                0, keyCode, KeyEvent.CHAR_UNDEFINED)
        pane.keyListeners.each { it.keyPressed(event) }
    }

    /** True when antialiased painting left an interior pixel of this fill. */
    private static boolean painted(BufferedImage image, int red, int green, int blue) {
        for (int y = 0; y < image.height; y++) {
            for (int x = 0; x < image.width; x++) {
                int argb = image.getRGB(x, y)
                if (((argb >>> 16) & 0xFF) == red && ((argb >>> 8) & 0xFF) == green && (argb & 0xFF) == blue) {
                    return true
                }
            }
        }
        false
    }

    private static JLabel findLabel(Component root, String name) {
        Component found = findNamed(root, name)
        found instanceof JLabel ? (JLabel) found : null
    }

    private static Component findNamed(Component root, String name) {
        if (root?.name == name) {
            return root
        }
        if (root instanceof Container) {
            for (Component child : ((Container) root).components) {
                Component found = findNamed(child, name)
                if (found != null) {
                    return found
                }
            }
        }
        null
    }

    @Test
    void closedHostIgnoresRefreshBindAndSchedule() {
        def pane = new JTextPane()
        pane.text = 'class Broken { def x = }\n'
        def host = ConsoleLanguageHost.attach(pane, ConsoleLanguageHostTest.classLoader)
        Thread.currentThread().interrupt()
        host.close()
        assert Thread.interrupted()
        host.refresh()
        host.bind(ConsoleLanguageHostTest.classLoader, null)
        host.setParentLoader(ConsoleLanguageHostTest.classLoader)
        host.completeAtCaret()
        host.close()
    }

    @Test
    void debounceSchedulePaintsAndDropsStaleGenerations() {
        def doc = new DefaultStyledDocument()
        def filter = new SmartDocumentFilter(doc)
        doc.documentFilter = filter
        def pane = new JTextPane()
        pane.document = doc
        def host = ConsoleLanguageHost.attach(pane, ConsoleLanguageHostTest.classLoader)
        try {
            pane.text = 'class Broken { def x = }\n'
            Thread.sleep(50)
            pane.text = 'class Also { def y = }\n'
            assert becameLatest(filter)

            pane.dispatchEvent(new KeyEvent(pane, KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
                    InputEvent.ALT_DOWN_MASK, KeyEvent.VK_SLASH, KeyEvent.CHAR_UNDEFINED,
                    KeyEvent.KEY_LOCATION_STANDARD))
            pane.keyListeners.each {
                it.keyPressed(new KeyEvent(pane, KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
                        InputEvent.ALT_DOWN_MASK, KeyEvent.VK_SLASH, KeyEvent.CHAR_UNDEFINED,
                        KeyEvent.KEY_LOCATION_STANDARD))
            }
            Thread.sleep(50)
            SwingUtilities.invokeAndWait {}
        } finally {
            host.close()
        }
    }

    @Test
    void refreshPaintsDiagnosticsWhenTheDocumentIsNotASmartFilter() {
        def pane = new JTextPane()
        pane.document = new DefaultStyledDocument()
        pane.text = 'class Broken { def x = }\n'
        def host = ConsoleLanguageHost.attach(pane, ConsoleLanguageHostTest.classLoader)
        try {
            host.refresh()
            assert pane.highlighter.highlights.length > 0
        } finally {
            host.close()
        }
    }

    @Test
    void closeInterruptsWhileAHighlightIsRunning() {
        def pane = new JTextPane()
        def src = new StringBuilder('class Slow {\n')
        200.times { src.append("    def m${it}() { ${it} }\n") }
        src.append('}\n')
        pane.text = src.toString()
        def host = ConsoleLanguageHost.attach(pane, ConsoleLanguageHostTest.classLoader)
        try {
            pane.text = src.toString() + '\n'
            Thread.currentThread().interrupt()
            host.close()
            assert Thread.interrupted()
        } finally {
            host.close()
        }
    }

    @Test
    void closeOnTheEventDispatchThreadDoesNotWaitForTheCompile() {
        def pane = new JTextPane()
        def host = ConsoleLanguageHost.attach(pane, ConsoleLanguageHostTest.classLoader, new CompilerConfiguration())
        def entered = new CountDownLatch(1)
        def release = new CountDownLatch(1)
        def blocker = Thread.start('groovy-console-flag-block') {
            GroovyCompiler.withProcessFlags(CompilerSettings.defaults()) {
                entered.countDown()
                assert release.await(8, TimeUnit.SECONDS)
                null
            }
        }
        assert entered.await(5, TimeUnit.SECONDS)
        def compiling = Thread.start('groovy-console-compile') {
            host.compileAndDiagnostics('class CloseStall {}')
        }
        assert awaitBlocked(compiling)
        def finished = new CountDownLatch(1)
        long[] elapsed = new long[1]
        def watchdog = Thread.start('groovy-console-close-watch') {
            if (!finished.await(2, TimeUnit.SECONDS)) {
                release.countDown()
            }
        }
        try {
            SwingUtilities.invokeAndWait {
                long start = System.nanoTime()
                host.close()
                elapsed[0] = System.nanoTime() - start
                finished.countDown()
            }
        } finally {
            release.countDown()
            assert host.languageReleased.await(10, TimeUnit.SECONDS)
            compiling.join(10_000)
            blocker.join(10_000)
            watchdog.join(5_000)
        }
        assert elapsed[0] < 1_000_000_000L
    }

    @Test
    void viewersWorkWithoutAnAttachedHost() {
        def configuration = new CompilerConfiguration()
        def tree = ConsoleLanguageHost.cstOf('def a = 1\n', ConsoleLanguageHostTest.classLoader, configuration)
        assert tree.name
        def rows = ConsoleLanguageHost.tokensOf('def a = 1\n', ConsoleLanguageHostTest.classLoader, configuration)
        assert rows.any { it.name != null && it.text }
    }

    private static boolean becameLatest(SmartDocumentFilter filter) {
        long deadline = System.nanoTime() + 3_000_000_000L
        while (System.nanoTime() < deadline) {
            SwingUtilities.invokeAndWait {}
            if (filter.isLatest()) {
                return true
            }
            Thread.sleep(20)
        }
        false
    }

    private static boolean awaitBlocked(Thread thread) {
        long deadline = System.nanoTime() + 5_000_000_000L
        while (System.nanoTime() < deadline) {
            if (thread.state == Thread.State.BLOCKED) {
                return true
            }
            if (!thread.alive) {
                return false
            }
            Thread.sleep(5)
        }
        false
    }

    @Test
    void completionChordIsAltSlashAndLeavesCtrlSpaceForTheInputMethod() {
        def pane = new JTextPane()
        pane.text = 'name'
        pane.caretPosition = 2
        def host = ConsoleLanguageHost.attach(pane, ConsoleLanguageHostTest.classLoader)
        try {
            def altSlash = pressed(pane, InputEvent.ALT_DOWN_MASK, KeyEvent.VK_SLASH)
            firePressed(pane, altSlash)
            assert altSlash.consumed
            def typedSlash = typed(pane, InputEvent.ALT_DOWN_MASK, '/' as char)
            fireTyped(pane, typedSlash)
            assert typedSlash.consumed
            assert pane.text == 'name'

            assert !pressedAndFired(pane, InputEvent.CTRL_DOWN_MASK, KeyEvent.VK_SPACE).consumed
            assert !pressedAndFired(pane, InputEvent.CTRL_DOWN_MASK, KeyEvent.VK_SLASH).consumed
            assert !pressedAndFired(pane, InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK, KeyEvent.VK_SLASH).consumed
            def swedish = new KeyEvent(pane, KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
                    InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK, KeyEvent.VK_7, '/' as char)
            firePressed(pane, swedish)
            assert swedish.consumed
            def swedishTyped = typed(pane, InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK, '/' as char)
            fireTyped(pane, swedishTyped)
            assert swedishTyped.consumed
            def deferred = pressed(pane, InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK, KeyEvent.VK_7)
            firePressed(pane, deferred)
            assert !deferred.consumed
            def deferredTyped = typed(pane, InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK, '/' as char)
            fireTyped(pane, deferredTyped)
            assert deferredTyped.consumed
            def question = new KeyEvent(pane, KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
                    InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK, KeyEvent.VK_SLASH, '?' as char)
            firePressed(pane, question)
            assert !question.consumed
            assert !pressedAndFired(pane, InputEvent.ALT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK, KeyEvent.VK_SLASH).consumed
            assert !pressedAndFired(pane, InputEvent.ALT_DOWN_MASK | InputEvent.META_DOWN_MASK, KeyEvent.VK_SLASH).consumed
            assert !pressedAndFired(pane, InputEvent.ALT_DOWN_MASK | InputEvent.ALT_GRAPH_DOWN_MASK, KeyEvent.VK_SLASH).consumed
            assert !pressedAndFired(pane, 0, KeyEvent.VK_SLASH).consumed

            def plainTyped = typed(pane, 0, '/' as char)
            fireTyped(pane, plainTyped)
            assert !plainTyped.consumed

            // A non-slash release leaves the pending swallow in place.
            firePressed(pane, pressed(pane, InputEvent.ALT_DOWN_MASK, KeyEvent.VK_SLASH))
            fireReleased(pane, released(pane, 0, KeyEvent.VK_A))
            def stillSwallowed = typed(pane, InputEvent.ALT_DOWN_MASK, '/' as char)
            fireTyped(pane, stillSwallowed)
            assert stillSwallowed.consumed

            // Releasing slash clears a swallow that never saw keyTyped.
            firePressed(pane, pressed(pane, InputEvent.ALT_DOWN_MASK, KeyEvent.VK_SLASH))
            fireReleased(pane, released(pane, InputEvent.ALT_DOWN_MASK, KeyEvent.VK_SLASH))
            def afterRelease = typed(pane, 0, 'z' as char)
            fireTyped(pane, afterRelease)
            assert !afterRelease.consumed
        } finally {
            host.close()
        }
    }

    private static KeyEvent pressed(JTextPane pane, int modifiers, int keyCode) {
        new KeyEvent(pane, KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
                modifiers, keyCode, KeyEvent.CHAR_UNDEFINED)
    }

    private static KeyEvent released(JTextPane pane, int modifiers, int keyCode) {
        new KeyEvent(pane, KeyEvent.KEY_RELEASED, System.currentTimeMillis(),
                modifiers, keyCode, KeyEvent.CHAR_UNDEFINED)
    }

    private static KeyEvent typed(JTextPane pane, int modifiers, char keyChar) {
        new KeyEvent(pane, KeyEvent.KEY_TYPED, System.currentTimeMillis(),
                modifiers, KeyEvent.VK_UNDEFINED, keyChar)
    }

    private static void firePressed(JTextPane pane, KeyEvent event) {
        pane.keyListeners.each { it.keyPressed(event) }
    }

    private static void fireReleased(JTextPane pane, KeyEvent event) {
        pane.keyListeners.each { it.keyReleased(event) }
    }

    private static void fireTyped(JTextPane pane, KeyEvent event) {
        pane.keyListeners.each { it.keyTyped(event) }
    }

    private static KeyEvent pressedAndFired(JTextPane pane, int modifiers, int keyCode) {
        def event = pressed(pane, modifiers, keyCode)
        firePressed(pane, event)
        event
    }

    @Test
    void guardsCoverClosedScheduleStaleWorkAndBadHighlights() {
        def pane = new JTextPane()
        pane.text = 'name'
        pane.caretPosition = 2
        def host = ConsoleLanguageHost.attach(pane, ConsoleLanguageHostTest.classLoader)
        try {
            pane.dispatchEvent(new KeyEvent(pane, KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
                    0, KeyEvent.VK_A, 'a' as char))
            pane.dispatchEvent(new KeyEvent(pane, KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
                    0, KeyEvent.VK_SPACE, ' ' as char))

            host.@closed = true
            pane.text = 'later'
            host.@closed = false

            def highlight = ConsoleLanguageHost.getDeclaredMethod('runHighlight', int, String)
            highlight.accessible = true
            highlight.invoke(host, -1, 'class A {}')
            def current = host.@generation.get()
            highlight.invoke(host, current, 'class Broken { def x = }')
            host.@closed = true
            SwingUtilities.invokeAndWait {}
            host.@closed = false

            def complete = ConsoleLanguageHost.getDeclaredMethod('runCompletions', int, String, int, int)
            complete.accessible = true
            complete.invoke(host, -1, 'name', 0, 0)
            complete.invoke(host, current, 'name', 0, 2)
            host.@closed = true
            SwingUtilities.invokeAndWait {}
            host.@closed = false
            host.@language.close()
            highlight.invoke(host, current, 'class A {}')
            complete.invoke(host, current, 'name', 0, 2)

            def paint = ConsoleLanguageHost.getDeclaredMethod('paint', String, List)
            paint.accessible = true
            paint.invoke(host, ['name', []] as Object[])
            def add = ConsoleLanguageHost.getDeclaredMethod('addErrorHighlight', Highlighter, int, int,
                    DefaultHighlighter.DefaultHighlightPainter)
            add.accessible = true
            def painter = new DefaultHighlighter.DefaultHighlightPainter(Color.RED)
            def throwing = new DefaultHighlighter() {
                @Override
                Object addHighlight(int p0, int p1, HighlightPainter p) throws BadLocationException {
                    throw new BadLocationException('test', p0)
                }
            }
            assert add.invoke(host, [throwing, 50, 60, painter] as Object[]) == null

            def show = CompletionPopup.getDeclaredMethod('showMenu', List)
            show.accessible = true
            show.invoke(host.@completions, [[]] as Object[])
            def bare = ConsoleLanguageHost.attach(null, null)
            try {
                show.invoke(bare.@completions, [[ConsoleLanguageHost.Candidate.of('x', 'method', '', 'x')]] as Object[])
            } finally {
                bare.close()
            }

            def menuHost = ConsoleLanguageHost.attach(pane, ConsoleLanguageHostTest.classLoader)
            try {
                pane.caret.magicCaretPosition = new Point(3, 4)
                show.invoke(menuHost.@completions, [[ConsoleLanguageHost.Candidate.of('x', 'method', '', 'x')]] as Object[])
                pane.caret.magicCaretPosition = null
                show.invoke(menuHost.@completions, [[ConsoleLanguageHost.Candidate.of('y', 'method', '', 'y')]] as Object[])
            } finally {
                menuHost.close()
            }

            def broken = new JTextPane()
            broken.document = new DefaultStyledDocument() {
                @Override
                void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
                    throw new BadLocationException('test', offs)
                }
            }
            ConsoleLanguageHost.insertAtCaret(broken, 'ab')
            ConsoleLanguageHost.insertAtCaret(broken, '.z')
        } finally {
            host.@closed = true
            host.@debounce?.shutdownNow()
        }
    }

    @Test
    void completionPopupEdgesAndClosedQueries() {
        def pane = new JTextPane()
        pane.text = 'name'
        pane.caretPosition = 4
        def host = ConsoleLanguageHost.attach(pane, ConsoleLanguageHostTest.classLoader)
        try {
            host.@closed = true
            assert host.tokens('class A {}').empty
            assert host.cst('class A {}').name == ''
            host.@closed = false

            assert ConsoleLanguageHost.completionCellSize(null, null) == [280, 20] as int[]
            def noFont = new JList<ConsoleLanguageHost.Candidate>() {
                @Override
                Font getFont() {
                    null
                }
            }
            def sized = ConsoleLanguageHost.completionCellSize(noFont, [
                    ConsoleLanguageHost.Candidate.of(null, 'Method', null, null)])
            assert sized[0] >= 280

            def add = ConsoleLanguageHost.Candidate.of('add', 'Method', 'boolean add(Object e)', 'add')
            def clear = ConsoleLanguageHost.Candidate.of('clear', 'Method', 'void clear()', 'clear')
            def list = host.armCompletion([add, clear], 2)
            list.setSize(400, list.fixedCellHeight * 2)
            def menu = host.@completions.@completionPopup
            menu.popupMenuListeners.findAll { it.class.name.startsWith('groovy.') }.each {
                it.popupMenuWillBecomeVisible(null)
                it.popupMenuWillBecomeInvisible(null)
                it.popupMenuCanceled(null)
            }
            list = host.armCompletion([add, clear], 2)
            list.setSize(400, list.fixedCellHeight * 2)
            list.mouseListeners.each {
                it.mousePressed(new MouseEvent(list, MouseEvent.MOUSE_PRESSED, 0, 0, 1, 4000, 1, false))
            }
            list.addListSelectionListener {
                list.mouseMotionListeners.each {
                    it.mouseMoved(new MouseEvent(list, MouseEvent.MOUSE_MOVED, 0, 0, 8, 1, 0, false))
                }
            }
            int y = list.fixedCellHeight + 1
            list.mouseMotionListeners.each {
                it.mouseMoved(new MouseEvent(list, MouseEvent.MOUSE_MOVED, 0, 0, 8, y, 0, false))
            }
            list.selectedIndex = -1
            list.actionMap.get('choose').actionPerformed(new ActionEvent(list, 0, 'choose'))

            def move = CompletionPopup.getDeclaredMethod('moveSelection', int)
            move.accessible = true
            def choose = CompletionPopup.getDeclaredMethod('chooseArmed')
            choose.accessible = true
            move.invoke(host.@completions, -1)
            move.invoke(host.@completions, 40)
            list.selectedIndex = -1
            choose.invoke(host.@completions)
            host.@completions.@completionList = null
            move.invoke(host.@completions, 1)
            choose.invoke(host.@completions)

            pane.caretPosition = 2
            host.armCompletion([add], 2)
            pane.document.insertString(3, '!', null)
            pane.caretPosition = pane.document.length
            host.armCompletion([add], 2)
            pane.document.insertString(0, 'Q', null)
            host.armCompletion([add], 2)
            pane.document.remove(0, 1)
            int length = pane.document.length
            pane.caretPosition = 1
            host.armCompletion([add], 1)
            if (length > 2) {
                pane.document.remove(2, 1)
            }
            host.armCompletion([add], 1)
            pane.document.remove(0, Math.min(3, pane.document.length))
            host.armCompletion([add], 2)
            host.@completions.@completionList = null
            pane.document.insertString(0, 'Z', null)
            host.armCompletion([add], 0)
            host.@completions.@completionEnd = pane.document.length + 5
            pane.document.insertString(0, 'Q', null)
            host.armCompletion([add], 0)
            pane.document.insertString(Math.min(1, pane.document.length), '-', null)

            assert ConsoleLanguageHost.completionRowAt(null, null) == -1
            assert ConsoleLanguageHost.completionRowAt(list, null) == -1
            assert ConsoleLanguageHost.completionInsert(null) == ''
            assert ConsoleLanguageHost.textOf(null) == ''
            def bad = new JTextPane()
            bad.document = new DefaultStyledDocument() {
                @Override
                String getText(int offset, int len) throws BadLocationException {
                    throw new BadLocationException('test', offset)
                }
            }
            assert ConsoleLanguageHost.textOf(bad) == ''

            host.armCompletion([add], 2)
            host.@completions.@completionPopup = new JPopupMenu() {
                @Override
                boolean isVisible() {
                    true
                }

                @Override
                void setVisible(boolean value) {
                }
            }
            def hide = CompletionPopup.getDeclaredMethod('hideCompletion')
            hide.accessible = true
            hide.invoke(host.@completions)
            assert host.@completions.@completionPopup == null

            def showMenu = CompletionPopup.getDeclaredMethod('showMenu', List)
            showMenu.accessible = true
            showMenu.invoke(host.@completions, [[]] as Object[])
            showMenu.invoke(host.@completions, [[add]] as Object[])
            def showCompletions = ConsoleLanguageHost.getDeclaredMethod('showCompletions')
            showCompletions.accessible = true
            def runCompletions = ConsoleLanguageHost.getDeclaredMethod('runCompletions', int, String, int, int)
            runCompletions.accessible = true
            host.@closed = true
            showCompletions.invoke(host)
            runCompletions.invoke(host, 0, 'a.', 0, 2)
            host.@closed = false
            runCompletions.invoke(host, -1, 'a.', 0, 2)
            host.@debounce.shutdownNow()
            showCompletions.invoke(host)
        } finally {
            host.close()
        }
    }

    @Test
    void completionEditsMoveTheAnchorAndClampTheSelection() {
        def pane = new JTextPane()
        pane.text = 'name'
        pane.caretPosition = 4
        def host = ConsoleLanguageHost.attach(pane, ConsoleLanguageHostTest.classLoader)
        try {
            def add = ConsoleLanguageHost.Candidate.of('add', 'Method', 'boolean add(Object e)', 'add')
            def clear = ConsoleLanguageHost.Candidate.of('clear', 'Method', 'void clear()', 'clear')
            def list = host.armCompletion([add, clear], 4)
            list.setSize(300, Math.max(list.fixedCellHeight, 20) * 2)
            def row = list.getCellBounds(1, 1)
            if (row != null) {
                list.selectedIndex = 0
                int px = (int) row.x + 1
                int py = (int) row.y + 1
                list.addListSelectionListener {
                    list.mouseMotionListeners.each {
                        it.mouseMoved(new MouseEvent(list, MouseEvent.MOUSE_MOVED, 0, 0, px, py, 0, false))
                    }
                }
                list.mouseMotionListeners.each {
                    it.mouseMoved(new MouseEvent(list, MouseEvent.MOUSE_MOVED, 0, 0, px, py, 0, false))
                }
                list.listSelectionListeners.each { list.removeListSelectionListener(it) }
            }
            list.selectedIndex = 0
            def move = CompletionPopup.getDeclaredMethod('moveSelection', int)
            move.accessible = true
            move.invoke(host.@completions, [-1] as Object[])
            assert list.selectedIndex == 0
            list.selectedIndex = 1
            move.invoke(host.@completions, [20] as Object[])
            assert list.selectedIndex == 1

            def onEdit = CompletionPopup.getDeclaredMethod('onDocumentEdit', DocumentEvent)
            onEdit.accessible = true
            def edit = { DocumentEvent.EventType kind, int offset, int length ->
                [
                        getType: { kind },
                        getOffset: { offset },
                        getLength: { length },
                        getDocument: { pane.document },
                        getChange: { Element element -> null },
                ] as DocumentEvent
            }
            host.@completions.@completionAnchor = 3
            host.@completions.@completionEnd = 3
            host.@completions.@completionOpen = true
            onEdit.invoke(host.@completions, [edit(DocumentEvent.EventType.INSERT, 0, 1)] as Object[])
            assert host.@completions.@completionAnchor == 4
            host.@completions.@completionAnchor = 3
            host.@completions.@completionEnd = 4
            host.@completions.@completionOpen = true
            onEdit.invoke(host.@completions, [edit(DocumentEvent.EventType.REMOVE, 0, 1)] as Object[])
            assert host.@completions.@completionAnchor == 2
            host.@completions.@completionList = null
            host.@completions.@completionOpen = true
            host.@completions.@completionAnchor = 3
            host.@completions.@completionEnd = 3
            onEdit.invoke(host.@completions, [edit(DocumentEvent.EventType.INSERT, 0, 1)] as Object[])
            host.armCompletion([add], 2)
            host.@completions.@completionEnd = 50
            onEdit.invoke(host.@completions, [edit(DocumentEvent.EventType.INSERT, 0, 1)] as Object[])
            assert !host.@completions.@completionOpen
        } finally {
            host.close()
        }
    }
}
