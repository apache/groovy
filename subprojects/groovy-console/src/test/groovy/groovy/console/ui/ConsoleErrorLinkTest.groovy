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

import com.github.javaparser.JavaToken
import com.github.javaparser.ParseProblemException
import com.github.javaparser.Problem
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.TokenRange
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.ErrorCollector
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.messages.SyntaxErrorMessage
import org.codehaus.groovy.syntax.SyntaxException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import javax.swing.JLabel
import javax.swing.JTextPane
import javax.swing.SwingUtilities
import javax.swing.event.HyperlinkEvent
import javax.swing.text.AttributeSet
import javax.swing.text.DefaultStyledDocument
import javax.swing.text.Document
import javax.swing.text.Element
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.Style
import javax.swing.text.StyleConstants
import javax.swing.text.StyleContext
import javax.swing.text.html.HTML
import java.awt.Color
import java.util.prefs.Preferences

/**
 * Activating a location in the output pane moves the editor caret to that
 * line and column. The status bar counts the same character columns.
 */
class ConsoleErrorLinkTest {

    private Preferences previousPrefs
    private Preferences testPrefs
    private Console console

    @BeforeEach
    void setUp() {
        previousPrefs = Console.prefs
        testPrefs = Preferences.userRoot().node('groovy/console/error-link-test')
        testPrefs.clear()
        Console.prefs = testPrefs
        console = new Console()
        console.detachedOutput = false
        console.statusLabel = new JLabel()
        console.rowNumAndColNum = new JLabel('1:1')
        console.inputArea = new JTextPane()
        console.rootElement = console.inputArea.document.defaultRootElement
        console.inputArea.addCaretListener(console)
        wireOutput(console)
    }

    @AfterEach
    void tearDown() {
        if (console != null) {
            Console.consoleControllers.remove(console)
        }
        Console.prefs = previousPrefs
        testPrefs?.removeNode()
    }

    @Test
    void testUnclosedStringLinkPlacesCaretOnTheReportedColumn() {
        onEdt {
            // println '123
            // 123456789
            setEditorText("println '123")
            reportCompilationFailure("println '123")

            def link = linkContaining('column: 9')
            assert link.text.contains('line: 1, column: 9')
            assert link.underlined

            activate(link)
            assert console.rowNumAndColNum.text == '1:9'
            assert console.inputArea.caretPosition == 8
            assert charAtCaret(console.inputArea) == "'"
            assert console.inputArea.selectionStart == console.inputArea.caretPosition
            assert console.inputArea.selectedText == "'"
        }
    }

    @Test
    void testErrorOnALaterLineDoesNotStartAtThePreviousNewline() {
        onEdt {
            setEditorText("println 1\nprintln '123")
            reportCompilationFailure("println 1\nprintln '123")

            activate(linkContaining('line: 2, column: 9'))

            Element line = console.inputArea.document.defaultRootElement.getElement(1)
            assert console.rowNumAndColNum.text == '2:9'
            assert console.inputArea.caretPosition == line.startOffset + 8
            assert console.inputArea.selectionStart == console.inputArea.caretPosition
            assert charAtCaret(console.inputArea) == "'"
            assert console.inputArea.selectionStart != line.startOffset - 1
        }
    }

    @Test
    void testEachErrorLinkKeepsItsOwnColumn() {
        onEdt {
            setEditorText("println '123\nfoo(")
            reportPositions("println '123\nfoo(",
                    new SyntaxException('Unclosed string literal', 1, 9, 1, 10),
                    new SyntaxException('Unexpected character', 2, 4, 2, 5))

            def found = locationLinks()
            assert found.size() == 2
            assert found[0].href == 'file:///groovy-console-pos'
            assert found[0].href == found[1].href
            assert found[0].element != found[1].element

            activate(found[0])
            assert console.rowNumAndColNum.text == '1:9'
            assert charAtCaret(console.inputArea) == "'"

            activate(found[1])
            assert console.rowNumAndColNum.text == '2:4'
            assert charAtCaret(console.inputArea) == '('
            assert console.inputArea.selectedText == '('
        }
    }

    @Test
    void testStackTraceLinkSelectsOnlyTheReferencedLine() {
        onEdt {
            setEditorText('alpha\nbeta\ngamma')
            console.appendStacktrace('\tat Script.main(ConsoleScript0.groovy:2)\n')

            def link = links(console.outputArea.document).find { it.text.contains('ConsoleScript0.groovy:2') }
            assert link != null
            activate(link)

            Element line = console.inputArea.document.defaultRootElement.getElement(1)
            assert console.inputArea.selectedText == 'beta'
            assert console.inputArea.selectionStart == line.startOffset
            assert console.inputArea.selectionEnd == line.endOffset - 1
            assert console.rowNumAndColNum.text == '2:1'
        }
    }

    @Test
    void testTabCountsAsOneColumn() {
        onEdt {
            setEditorText("\t'abc")
            reportCompilationFailure("\t'abc")

            def link = linkContaining('column:')
            def column = (link.text =~ /column: (\d+)/)[0][1]
            activate(link)

            assert console.rowNumAndColNum.text == "1:$column"
            assert charAtCaret(console.inputArea) == "'"
            assert link.underlined
        }
    }

    @Test
    void testColumnPastTheEndOfTheLineStaysOnThatLine() {
        onEdt {
            reportPositions('ab\ncd', new SyntaxException('bad', 1, 50))

            activate(locationLinks()[0])

            Element line = console.inputArea.document.defaultRootElement.getElement(0)
            assert console.inputArea.caretPosition == line.endOffset - 1
            assert console.inputArea.selectedText == null
            assert console.rowNumAndColNum.text == '1:3'
        }
    }

    @Test
    void testHugeColumnDoesNotWrapTheCaret() {
        onEdt {
            reportPositions('ab', new SyntaxException('bad', 1, Integer.MAX_VALUE, 1, Integer.MAX_VALUE))

            activate(locationLinks()[0])

            assert console.inputArea.caretPosition == 2
            assert console.rowNumAndColNum.text == '1:3'
        }
    }

    @Test
    void testErrorSpanCanCrossLinesAndCaretStaysAtTheStart() {
        onEdt {
            reportPositions('abcdef\nxyz', new SyntaxException('bad', 1, 2, 2, 3))

            activate(locationLinks()[0])

            assert console.rowNumAndColNum.text == '1:2'
            assert charAtCaret(console.inputArea) == 'b'
            assert console.inputArea.selectedText == 'bcdef\nxy'
            assert console.inputArea.selectionStart == console.inputArea.caretPosition
        }
    }

    @Test
    void testEndLineBeyondTheDocumentClampsToTheLastLine() {
        onEdt {
            reportPositions('abcdef\nxyz', new SyntaxException('bad', 1, 2, 9, 2))

            activate(locationLinks()[0])

            assert console.rowNumAndColNum.text == '1:2'
            assert console.inputArea.selectedText == 'bcdef\nx'
        }
    }

    @Test
    void testEndPositionBeforeTheStartDoesNotSelectBackwards() {
        onEdt {
            reportPositions('abcdef\nxyz', new SyntaxException('bad', 2, 2, 1, 2))

            activate(locationLinks()[0])

            assert console.rowNumAndColNum.text == '2:2'
            assert charAtCaret(console.inputArea) == 'y'
            assert console.inputArea.selectedText == null
        }
    }

    @Test
    void testLineBeyondTheDocumentClampsToTheLastLine() {
        onEdt {
            reportPositions('ab\ncd', new SyntaxException('bad', 50, 2, 50, 3))

            activate(locationLinks()[0])

            assert console.rowNumAndColNum.text == '2:2'
            assert charAtCaret(console.inputArea) == 'd'
        }
    }

    @Test
    void testLineOnlyLinkIgnoresAStoredEnd() {
        onEdt {
            reportPositions('alpha\nbeta', new SyntaxException('bad', 2, 0, 2, 4))

            activate(locationLinks()[0])

            assert console.inputArea.selectedText == 'beta'
            assert console.rowNumAndColNum.text == '2:1'
        }
    }

    @Test
    void testUnknownColumnSelectsTheWholeLine() {
        onEdt {
            reportPositions('alpha\nbeta', new SyntaxException('bad', 2, -1, 2, -1))

            activate(locationLinks()[0])

            Element line = console.inputArea.document.defaultRootElement.getElement(1)
            assert console.inputArea.selectedText == 'beta'
            assert console.inputArea.selectionStart == line.startOffset
            assert console.rowNumAndColNum.text == '2:1'
        }
    }

    @Test
    void testZeroLineNumberDoesNotMoveTheCaret() {
        onEdt {
            setEditorText('alpha\nbeta')
            console.inputArea.caretPosition = 3
            console.appendStacktrace('\tat Script.main(ConsoleScript0.groovy:0)\n')

            activate(links(console.outputArea.document).find { it.text.contains(':0') })

            assert console.inputArea.caretPosition == 3
        }
    }

    @Test
    void testJavaParseProblemSelectsTheExclusiveEnd() {
        onEdt {
            String source = 'class {'
            setEditorText(source)
            try {
                StaticJavaParser.parse(source)
                throw new AssertionError('expected a Java parse problem')
            } catch (ParseProblemException e) {
                console.finishException(e, false)
            }

            activate(locationLinks()[0])

            assert console.rowNumAndColNum.text == '1:1'
            assert console.inputArea.selectedText == source
        }
    }

    @Test
    void testKeyboardActivationAtTheEndOfTheLinkStillMovesTheCaret() {
        onEdt {
            setEditorText("println '123")
            reportCompilationFailure("println '123")
            def link = linkContaining('column: 9')
            def doc = (DefaultStyledDocument) console.outputArea.document
            Element boundary = doc.getCharacterElement(link.element.endOffset)
            assert boundary != link.element

            console.inputArea.caretPosition = 0
            console.hyperlinkUpdate(new HyperlinkEvent(
                    console.outputArea,
                    HyperlinkEvent.EventType.ACTIVATED,
                    null,
                    link.href,
                    boundary))

            assert console.rowNumAndColNum.text == '1:9'
            assert charAtCaret(console.inputArea) == "'"
        }
    }

    @Test
    void testDescriptionDoesNotOverrideTheLinkPosition() {
        onEdt {
            setEditorText("println '123")
            reportCompilationFailure("println '123")
            def link = linkContaining('column: 9')

            console.hyperlinkUpdate(new HyperlinkEvent(
                    console.outputArea,
                    HyperlinkEvent.EventType.ACTIVATED,
                    null,
                    'file://ignored.groovy:4',
                    link.element))

            assert console.rowNumAndColNum.text == '1:9'
            assert charAtCaret(console.inputArea) == "'"
        }
    }

    @Test
    void testPositionSurvivesAFontSizeMerge() {
        onEdt {
            setEditorText("println '123")
            reportCompilationFailure("println '123")
            def link = linkContaining('column: 9')
            def size = new SimpleAttributeSet()
            StyleConstants.setFontSize(size, 20)
            console.outputArea.document.setCharacterAttributes(0, console.outputArea.document.length, size, false)

            activate(link)

            assert console.rowNumAndColNum.text == '1:9'
            assert charAtCaret(console.inputArea) == "'"
        }
    }

    @Test
    void testColumnWithoutAnEndSelectsNothing() {
        onEdt {
            reportPositions('abcdef', new SyntaxException('bad', 1, 3, 0, 0))
            activate(locationLinks()[0])
            assert console.rowNumAndColNum.text == '1:3'
            assert charAtCaret(console.inputArea) == 'c'
            assert console.inputArea.selectedText == null

            console.outputArea.text = ''
            reportPositions('abcdef', new SyntaxException('bad', 1, 3, 1, 0))
            activate(locationLinks()[0])
            assert console.rowNumAndColNum.text == '1:3'
            assert console.inputArea.selectedText == null
        }
    }

    @Test
    void testJavaProblemWithoutAUsableRangeDoesNotMoveTheCaret() {
        onEdt {
            setEditorText('class {')
            console.inputArea.caretPosition = 4

            console.finishException(new ParseProblemException(new IllegalStateException('plain')), false)
            assert locationLinks().empty
            assert console.inputArea.caretPosition == 4

            console.outputArea.text = ''
            console.finishException(new ParseProblemException([
                    new Problem('no range', new TokenRange(JavaToken.INVALID, JavaToken.INVALID), null)
            ]), false)
            assert locationLinks().empty
            assert console.inputArea.caretPosition == 4
        }
    }

    @Test
    void testJavaProblemWithoutAnEndRangePlacesTheCaretOnly() {
        onEdt {
            String source = 'class {'
            setEditorText(source)
            TokenRange located
            try {
                StaticJavaParser.parse(source)
                throw new AssertionError('expected a Java parse problem')
            } catch (ParseProblemException e) {
                located = e.problems[0].location.get()
            }
            console.finishException(new ParseProblemException([
                    new Problem('partial', new TokenRange(located.begin, JavaToken.INVALID), null)
            ]), false)

            activate(locationLinks()[0])
            assert console.rowNumAndColNum.text == '1:1'
            assert console.inputArea.selectedText == null
        }
    }

    @Test
    void testSeveralJavaProblemsAreAllReported() {
        onEdt {
            setEditorText('class {')
            console.inputArea.caretPosition = 2
            console.finishException(new ParseProblemException([
                    new Problem('one', null, null),
                    new Problem('two', null, null)
            ]), false)

            assert locationLinks().empty
            def text = console.outputArea.document.getText(0, console.outputArea.document.length)
            assert text.contains('one')
            assert text.contains('two')
            assert text.contains('2 compilation errors')
            assert console.inputArea.caretPosition == 2
        }
    }

    @Test
    void testStackTraceLinksOnlyTheCurrentScript() {
        onEdt {
            setEditorText('alpha\nbeta')
            console.scriptFile = new File('Foo.java')
            console.appendStacktrace('\tat com.acme.Foo.bar(Foo.java:2)\nnot a frame\n\tat java.lang.Thread.run(Thread.java:1)\n')

            def found = links(console.outputArea.document)
            assert found.size() == 1
            assert found[0].text.contains('Foo.java:2')
            activate(found[0])

            assert console.inputArea.selectedText == 'beta'
            assert console.rowNumAndColNum.text == '2:1'
        }
    }

    @Test
    void testAppendStacktraceWithoutAnOutputPaneDoesNothing() {
        onEdt {
            console.outputArea = null
            console.appendStacktrace('\tat Script.main(ConsoleScript0.groovy:1)\n')
        }
    }

    @Test
    void testIgnoredEventsLeaveTheCaretAlone() {
        onEdt {
            setEditorText("println '123")
            reportCompilationFailure("println '123")
            def link = linkContaining('column: 9')
            console.inputArea.caretPosition = 2

            console.hyperlinkUpdate(new HyperlinkEvent(
                    console.outputArea,
                    HyperlinkEvent.EventType.ENTERED,
                    null,
                    link.href,
                    link.element))
            assert console.inputArea.caretPosition == 2

            console.hyperlinkUpdate(new HyperlinkEvent(
                    console.outputArea,
                    HyperlinkEvent.EventType.ACTIVATED,
                    null))
            assert console.inputArea.caretPosition == 2

            Element plain = console.inputArea.document.defaultRootElement.getElement(0)
            console.hyperlinkUpdate(new HyperlinkEvent(
                    console.outputArea,
                    HyperlinkEvent.EventType.ACTIVATED,
                    null,
                    link.href,
                    plain))
            assert console.inputArea.caretPosition == 2

            setEditorText("alpha\nbeta")
            console.inputArea.caretPosition = 1
            Element laterLine = console.inputArea.document.defaultRootElement.getElement(1)
            console.hyperlinkUpdate(new HyperlinkEvent(
                    console.outputArea,
                    HyperlinkEvent.EventType.ACTIVATED,
                    null,
                    link.href,
                    laterLine))
            assert console.inputArea.caretPosition == 1

            console.inputArea = null
            console.hyperlinkUpdate(new HyperlinkEvent(
                    console.outputArea,
                    HyperlinkEvent.EventType.ACTIVATED,
                    null,
                    link.href,
                    link.element))
        }
    }

    private static void wireOutput(Console console) {
        JTextPane output = new JTextPane()
        output.contentType = 'text/html'
        DefaultStyledDocument doc = (DefaultStyledDocument) output.styledDocument
        Style regular = doc.addStyle('regular', StyleContext.defaultStyleContext.getStyle(StyleContext.DEFAULT_STYLE))
        console.commandStyle = doc.addStyle('command', regular)
        console.stacktraceStyle = doc.addStyle('stacktrace', regular)
        console.hyperlinkStyle = doc.addStyle('hyperlink', regular)
        StyleConstants.setForeground(console.hyperlinkStyle, Color.BLUE)
        StyleConstants.setUnderline(console.hyperlinkStyle, true)
        console.promptStyle = doc.addStyle('prompt', regular)
        console.outputStyle = doc.addStyle('output', regular)
        console.resultStyle = doc.addStyle('result', regular)
        console.outputArea = output
    }

    private void setEditorText(String text) {
        console.inputArea.text = text
    }

    private void reportCompilationFailure(String source) {
        try {
            console.shell.evaluate(source)
            throw new AssertionError('expected a compilation failure')
        } catch (MultipleCompilationErrorsException e) {
            console.finishException(e, false)
        }
    }

    private void reportPositions(String source, SyntaxException... errors) {
        setEditorText(source)
        def config = new CompilerConfiguration()
        def collector = new ErrorCollector(config)
        def unit = new SourceUnit('ConsoleScript.groovy', source, config, new GroovyClassLoader(), collector)
        errors.each { collector.addErrorAndContinue(new SyntaxErrorMessage(it, unit)) }
        console.finishException(new MultipleCompilationErrorsException(collector), false)
    }

    private void activate(Map link) {
        console.hyperlinkUpdate(new HyperlinkEvent(
                console.outputArea,
                HyperlinkEvent.EventType.ACTIVATED,
                null,
                link.href,
                link.element))
    }

    private List<Map> locationLinks() {
        links(console.outputArea.document).findAll { it.text.contains('line:') }
    }

    private Map linkContaining(String snippet) {
        def match = links(console.outputArea.document).find { it.text.contains(snippet) }
        assert match != null : "no link containing '${snippet}' in\n${console.outputArea.document.getText(0, console.outputArea.document.length)}"
        match
    }

    private static List<Map> links(Document doc) {
        DefaultStyledDocument styled = (DefaultStyledDocument) doc
        List<Map> found = []
        int pos = 0
        while (pos < styled.length) {
            Element elem = styled.getCharacterElement(pos)
            def anchor = elem.attributes.getAttribute(HTML.Tag.A)
            if (anchor instanceof AttributeSet) {
                def href = anchor.getAttribute(HTML.Attribute.HREF)
                if (href != null) {
                    String text = styled.getText(elem.startOffset, Math.max(0, elem.endOffset - elem.startOffset))
                    found << [
                            href: href.toString(),
                            text: text,
                            element: elem,
                            underlined: StyleConstants.isUnderline(elem.attributes)
                    ]
                }
            }
            int next = elem.endOffset
            if (next <= pos) {
                break
            }
            pos = next
        }
        found
    }

    private static String charAtCaret(JTextPane editor) {
        int pos = editor.caretPosition
        if (pos < 0 || pos >= editor.document.length) {
            return ''
        }
        editor.document.getText(pos, 1)
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
        if (thrown != null) {
            throw thrown
        }
        // hyperlinkUpdate posts the editor focus request to the EDT
        SwingUtilities.invokeAndWait { }
    }
}
