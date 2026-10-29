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

import groovy.console.ui.ThemeManager
import org.junit.jupiter.api.Test

import javax.swing.event.UndoableEditListener
import javax.swing.text.AttributeSet
import javax.swing.text.BadLocationException
import javax.swing.text.DefaultStyledDocument
import javax.swing.text.StyleConstants
import javax.swing.text.StyleContext
import java.awt.Color

class SmartDocumentFilterTest {

    @Test
    void testInsertStringAddsText() {
        def doc = new DefaultStyledDocument()
        def filter = new SmartDocumentFilter(doc)
        doc.setDocumentFilter(filter)

        doc.insertString(0, 'def x = 1', null)

        assert doc.getText(0, doc.getLength()) == 'def x = 1'
    }

    @Test
    void testReplaceText() {
        def doc = new DefaultStyledDocument()
        def filter = new SmartDocumentFilter(doc)
        doc.setDocumentFilter(filter)

        doc.insertString(0, 'hello world', null)
        doc.replace(0, 5, 'goodbye', null)

        assert doc.getText(0, doc.getLength()) == 'goodbye world'
    }

    @Test
    void testRemoveText() {
        def doc = new DefaultStyledDocument()
        def filter = new SmartDocumentFilter(doc)
        doc.setDocumentFilter(filter)

        doc.insertString(0, 'abcdef', null)
        doc.remove(0, 3)

        assert doc.getText(0, doc.getLength()) == 'def'
    }

    @Test
    void testCarriageReturnStripped() {
        def doc = new DefaultStyledDocument()
        def filter = new SmartDocumentFilter(doc)
        doc.setDocumentFilter(filter)

        doc.insertString(0, "line1\r\nline2", null)

        String text = doc.getText(0, doc.getLength())
        assert !text.contains('\r')
        assert text.contains('line1\nline2')
    }

    @Test
    void testGroovyKeywordHighlighting() {
        def doc = new DefaultStyledDocument()
        def filter = new SmartDocumentFilter(doc)
        doc.setDocumentFilter(filter)

        doc.insertString(0, 'def x = 42', null)

        assert doc.getLength() == 10
        assert doc.getText(0, doc.getLength()) == 'def x = 42'
        assert fg(doc, 0) == themeFg('reserved_word')
        assert fg(doc, 4) == themeFg('ident')
        assert fg(doc, 8) == themeFg('digit')
    }

    @Test
    void testMultipleInsertsAccumulateText() {
        def doc = new DefaultStyledDocument()
        def filter = new SmartDocumentFilter(doc)
        doc.setDocumentFilter(filter)

        doc.insertString(0, 'class Foo {', null)
        doc.insertString(doc.getLength(), '\n  def bar() {}', null)
        doc.insertString(doc.getLength(), '\n}', null)

        def text = doc.getText(0, doc.getLength())
        assert text.contains('class Foo')
        assert text.contains('def bar()')
    }

    @Test
    void testEmptyDocumentParsesWithoutError() {
        def doc = new DefaultStyledDocument()
        def filter = new SmartDocumentFilter(doc)
        doc.setDocumentFilter(filter)

        // Just setting the filter with empty document should not throw
        assert doc.getLength() == 0
    }

    @Test
    void testIsLatestAfterParse() {
        def doc = new DefaultStyledDocument()
        def filter = new SmartDocumentFilter(doc)
        doc.setDocumentFilter(filter)

        doc.insertString(0, 'println "hello"', null)

        assert filter.isLatest()
    }

    @Test
    void testGetLatestTokenListPopulatedAfterParse() {
        def doc = new DefaultStyledDocument()
        def filter = new SmartDocumentFilter(doc)
        doc.setDocumentFilter(filter)

        doc.insertString(0, 'def x = 1', null)

        assert fg(doc, 0) == themeFg('reserved_word')
        assert filter.latestTokenList != null
        assert filter.latestTokenList.empty
    }

    @Test
    void testRenderRangeGetSetNull() {
        def doc = new DefaultStyledDocument()
        def filter = new SmartDocumentFilter(doc)

        assert filter.getRenderRange() == null

        filter.setRenderRange(Tuple.tuple(0, 10))
        assert filter.getRenderRange() != null

        filter.setRenderRange(null)
        assert filter.getRenderRange() == null
    }

    @Test
    void testMultilineStringThemeFollowsTheOpeningDelimiter() {
        def doc = new DefaultStyledDocument()
        def filter = new SmartDocumentFilter(doc)
        doc.setDocumentFilter(filter)

        doc.insertString(0, "def a = '''\nline\n'''\n", null)
        String text = doc.getText(0, doc.length)
        assert fg(doc, text.indexOf('line')) == themeFg('single_quotes')

        doc.remove(0, doc.length)
        doc.insertString(0, 'def b = """\nline\n"""\n', null)
        text = doc.getText(0, doc.length)
        assert fg(doc, text.indexOf('line')) == themeFg('quotes')

        doc.remove(0, doc.length)
        doc.insertString(0, 'def c = /\nline\n/\n', null)
        text = doc.getText(0, doc.length)
        assert fg(doc, text.indexOf('line')) == themeFg('slashy_quotes')

        doc.remove(0, doc.length)
        doc.insertString(0, 'def d = """\nn:${x}\n"""\n', null)
        text = doc.getText(0, doc.length)
        assert fg(doc, text.indexOf('n:')) == themeFg('quotes')
        assert fg(doc, text.indexOf('$')) != themeFg('quotes')

        doc.remove(0, doc.length)
        doc.insertString(0, 'def x = 1\n/*\nblock\n*/\n', null)
        text = doc.getText(0, doc.length)
        assert fg(doc, text.indexOf('block')) == themeFg('comment')
    }

    @Test
    void testStringCommentAnnotationAndOperationStyles() {
        def doc = new DefaultStyledDocument()
        def filter = new SmartDocumentFilter(doc)
        doc.setDocumentFilter(filter)

        doc.insertString(0, "def a = 'x'\ndef b = \"y\"\ndef c = /z/\n// hi", null)
        String text = doc.getText(0, doc.length)
        assert fg(doc, text.indexOf("'x'")) == themeFg('single_quotes')
        assert fg(doc, text.indexOf('"y"')) == themeFg('quotes')
        assert fg(doc, text.indexOf('/z/')) == themeFg('slashy_quotes')
        assert fg(doc, text.indexOf('// hi')) == themeFg('comment')

        doc.remove(0, doc.length)
        doc.insertString(0, '@Foo.Bar x, y;', null)
        assert fg(doc, 0) == themeFg('annotation')
        assert fg(doc, 1) == themeFg('ident')
        assert fg(doc, 10) == themeFg('operation')
        assert fg(doc, 13) == themeFg('operation')
    }

    @Test
    void testIncompleteInputStillTokenizes() {
        def doc = new DefaultStyledDocument()
        def filter = new SmartDocumentFilter(doc)
        doc.setDocumentFilter(filter)

        doc.insertString(0, 'def x =', null)

        assert filter.isLatest()
        assert fg(doc, 0) == themeFg('reserved_word')
        assert filter.latestTokenList.empty
    }

    @Test
    void testReparseDocumentDoesNotThrow() {
        def doc = new DefaultStyledDocument()
        def filter = new SmartDocumentFilter(doc)
        doc.setDocumentFilter(filter)

        doc.insertString(0, 'for (int i = 0; i < 10; i++) { println i }', null)
        filter.reparseDocument()

        assert filter.isLatest()
        assert doc.getText(0, doc.getLength()).contains('for')
    }

    @Test
    void testComplexGroovyCodeParsesCorrectly() {
        def doc = new DefaultStyledDocument()
        def filter = new SmartDocumentFilter(doc)
        doc.setDocumentFilter(filter)

        def code = '''\
import java.util.stream.Collectors

@groovy.transform.CompileStatic
class Example {
    static List<String> transform(List<Integer> nums) {
        return nums.stream()
            .filter { it > 0 }
            .map { "num: ${it}" }
            .collect(Collectors.toList())
    }
}'''
        doc.insertString(0, code, null)

        assert filter.isLatest()
        def tokens = filter.getLatestTokenList()
        assert tokens.size() > 10
        assert tokens.any { it.text == '{' }
        assert !tokens.any { it.text == 'class' }
    }

    @Test
    void testReplaceWithNullTextTreatedAsEmpty() {
        def doc = new DefaultStyledDocument()
        def filter = new SmartDocumentFilter(doc)
        doc.setDocumentFilter(filter)

        doc.insertString(0, 'abc', null)
        doc.replace(1, 1, null, null)

        assert doc.getText(0, doc.getLength()) == 'ac'
    }

    @Test
    void testGStringDollarSlashyAndNonIdentifierAnnotationExtras() {
        def doc = new DefaultStyledDocument()
        def filter = new SmartDocumentFilter(doc)
        doc.setDocumentFilter(filter)

        doc.insertString(0, 'def s = "n:${x}"', null)
        String gstring = doc.getText(0, doc.length)
        int dollar = gstring.lastIndexOf('$')
        assert dollar > 0
        assert fg(doc, gstring.indexOf('"n:')) == themeFg('quotes')

        doc.remove(0, doc.length)
        doc.insertString(0, 'def s = "foo$', null)
        assert filter.isLatest()

        doc.remove(0, doc.length)
        doc.insertString(0, 'def s = $/z/$', null)
        String slashy = doc.getText(0, doc.length)
        assert fg(doc, slashy.indexOf('$/')) == themeFg('slashy_quotes')

        doc.remove(0, doc.length)
        doc.insertString(0, '@1 @Foo.Bar x', null)
        assert fg(doc, 0) == themeFg('annotation')
        assert fg(doc, 1) != themeFg('annotation')
        assert fg(doc, 3) == themeFg('annotation')
    }

    @Test
    void testRenderRangeAndIncrementalParseKeepExistingStyles() {
        def doc = new DefaultStyledDocument()
        def filter = new SmartDocumentFilter(doc)
        doc.setDocumentFilter(filter)

        doc.insertString(0, 'def x = 1', null)
        assert fg(doc, 0) == themeFg('reserved_word')

        filter.setRenderRange(Tuple.tuple(-1, 4))
        doc.insertString(doc.length, '\ndef y = 2', null)
        assert fg(doc, 0) == themeFg('reserved_word')

        filter.setRenderRange(Tuple.tuple(9999, 10000))
        doc.insertString(doc.length, '\ndef z = 3', null)
        assert filter.isLatest()

        filter.setRenderRange(Tuple.tuple(0, -1))
        doc.insertString(doc.length, '\ndef u = 6', null)
        assert filter.isLatest()

        filter.setRenderRange(Tuple.tuple(0, 3))
        doc.insertString(doc.length, '\ndef w = 4', null)
        assert fg(doc, 0) == themeFg('reserved_word')

        doc.remove(0, doc.length)
        doc.insertString(0, 'def x = 1\n/*\nblock\n*/\ndef y = 2', null)
        int comment = doc.getText(0, doc.length).indexOf('/*')
        filter.setRenderRange(Tuple.tuple(0, comment + 1))
        doc.insertString(doc.length, '\ndef t = 7', null)
        assert filter.isLatest()

        doc.insertString(doc.length, '\ndef v = 5', null)
        assert fg(doc, 0) == themeFg('reserved_word')

        doc.remove(0, doc.length)
        assert doc.length == 0
        assert filter.isLatest()
    }

    @Test
    void testApplyThemeStyleNullAndBackgroundUnderline() {
        SmartDocumentFilter.applyThemeStyle(null, 'ident')
        new SmartDocumentFilter(new DefaultStyledDocument())
        def file = File.createTempFile('groovy-console-ident', '.theme')
        file.deleteOnExit()
        file.text = '''
ident = #000000, underline on #FFFF00
comment = #5D5D5D
quotes = #7C007C
single_quotes = #007C00
slashy_quotes = #B28C00
digit = #B20000
annotation = #808000
operation = bold
reserved_word = #00007C, bold
'''
        def oldPath = ThemeManager.customLightPath
        def oldMode = ThemeManager.currentMode
        try {
            ThemeManager.customLightPath = file.absolutePath
            ThemeManager.applyTheme(ThemeManager.ThemeMode.LIGHT)
            SmartDocumentFilter.updateStyles()
            def identStyle = StyleContext.defaultStyleContext.getStyle('ident')
            assert StyleConstants.getBackground(identStyle) == Color.YELLOW
            assert StyleConstants.isUnderline(identStyle)
            def doc = new DefaultStyledDocument()
            def filter = new SmartDocumentFilter(doc)
            doc.setDocumentFilter(filter)
            doc.insertString(0, 'name', null)
            def style = doc.getCharacterElement(0).attributes
            assert style.getAttribute(StyleConstants.Background) == Color.YELLOW
            assert style.getAttribute(StyleConstants.Underline)
        } finally {
            ThemeManager.customLightPath = oldPath
            ThemeManager.applyTheme(oldMode)
            SmartDocumentFilter.updateStyles()
            file.delete()
        }
        def sc = StyleContext.defaultStyleContext
        def missing = sc.addStyle('smart-doc-missing', null)
        sc.removeStyle('smart-doc-missing')
        SmartDocumentFilter.applyThemeStyle(sc.getStyle('smart-doc-missing'), 'ident')
        assert missing != null
    }

    @Test
    void testLatestTokenListStillHasBraceTokens() {
        def doc = new DefaultStyledDocument()
        def filter = new SmartDocumentFilter(doc)
        doc.setDocumentFilter(filter)
        doc.insertString(0, 'def c = { 1 }', null)
        def tokens = filter.latestTokenList
        assert tokens.any { it.text == '{' }
        assert tokens.any { it.text == '}' }
        assert tokens.every { it.text in ['{', '}'] }
    }

    @Test
    void testRenderRangePaintsOnlyTheRequestedSpan() {
        def doc = new DefaultStyledDocument()
        def filter = new SmartDocumentFilter(doc)
        doc.setDocumentFilter(filter)
        doc.insertString(0, 'def x = 1', null)
        assert fg(doc, 0) == themeFg('reserved_word')

        filter.setRenderRange(Tuple.tuple(0, 2))
        doc.insertString(doc.length, '\ndef y = 2', null)
        assert fg(doc, 0) == themeFg('reserved_word')
        assert filter.isLatest()
    }

    @Test
    void testReparseToleratesBadLocation() {
        def doc = new BleDocument()
        def filter = new SmartDocumentFilter(doc)
        doc.setDocumentFilter(filter)
        doc.insertString(0, 'def x = 1', null)
        doc.fail = true
        filter.reparseDocument()
        assert doc.fail
    }

    @Test
    void testMissingThemeStyleFallsBackToDefault() {
        def sc = StyleContext.defaultStyleContext
        def doc = new DefaultStyledDocument()
        def filter = new SmartDocumentFilter(doc)
        doc.setDocumentFilter(filter)
        sc.removeStyle('ident')
        try {
            doc.insertString(0, 'name', null)
            assert doc.getCharacterElement(0).attributes != null
        } finally {
            new SmartDocumentFilter(new DefaultStyledDocument())
            SmartDocumentFilter.updateStyles()
        }
    }

    @Test
    void testCarriageReturnSeparatesLines() {
        def doc = new DefaultStyledDocument()
        def filter = new SmartDocumentFilter(doc)
        doc.setDocumentFilter(filter)
        doc.insertString(0, 'def a\rdef b', null)
        assert doc.getText(0, doc.length).contains('\r')
        assert fg(doc, 0) == themeFg('reserved_word')
        assert filter.isLatest()
    }

    @Test
    void testBracketsInsideStringsAreNotDelimiters() {
        def doc = new DefaultStyledDocument()
        def filter = new SmartDocumentFilter(doc)
        doc.setDocumentFilter(filter)
        doc.insertString(0, "def s = '{'\ndef m = a?[0]\n", null)
        def tokens = filter.latestTokenList
        assert !tokens.any { it.text == '{' }
        assert tokens.any { it.text == '?[' }
        assert tokens.any { it.text == ']' }
        assert !tokens.any { it.text == '[' }
    }

    @Test
    void testUnclosedGStringDoesNotFailTheEdit() {
        def doc = new DefaultStyledDocument()
        def filter = new SmartDocumentFilter(doc)
        doc.setDocumentFilter(filter)
        doc.insertString(0, '"$', null)
        assert doc.getText(0, doc.length) == '"$'
        assert filter.isLatest()
    }

    @Test
    void testAttributeEditsAreNotUndoableAndCloseIsIdempotent() {
        def doc = new DefaultStyledDocument()
        int edits = 0
        UndoableEditListener listener = { event -> edits++ }
        doc.addUndoableEditListener(listener)
        def filter = new SmartDocumentFilter(doc)
        doc.setDocumentFilter(filter)
        doc.insertString(0, 'def x = 1\n', null)
        assert edits == 1
        doc.insertString(doc.length, 'def y = 2\n', null)
        assert edits == 2
        assert fg(doc, 0) == themeFg('reserved_word')
        filter.close()
        filter.close()
    }

    private static final class BleDocument extends DefaultStyledDocument {
        boolean fail

        @Override
        String getText(int offset, int length) throws BadLocationException {
            if (fail) {
                throw new BadLocationException('test', offset)
            }
            super.getText(offset, length)
        }
    }

    @Test
    void testMidBufferInsertDoesNotRestyleTheTail() {
        def doc = new CountingDocument()
        def filter = new SmartDocumentFilter(doc)
        doc.setDocumentFilter(filter)
        def line = 'def value = 1\n'
        doc.insertString(0, line * 400, null)
        int middle = doc.length.intdiv(2)
        doc.attributeCalls = 0
        doc.insertString(middle, ' ', null)
        // One line of tokens, not one call per span after the caret.
        assert doc.attributeCalls < 80
        assert filter.isLatest()
        doc.attributeCalls = 0
        doc.remove(middle, 1)
        assert doc.attributeCalls < 80
    }

    private static class CountingDocument extends DefaultStyledDocument {
        int attributeCalls

        @Override
        void setCharacterAttributes(int offset, int length, AttributeSet style, boolean replace) {
            attributeCalls++
            super.setCharacterAttributes(offset, length, style, replace)
        }
    }

    private static Object fg(DefaultStyledDocument doc, int offset) {
        doc.getCharacterElement(offset).attributes.getAttribute(StyleConstants.Foreground)
    }

    private static Object themeFg(String key) {
        ThemeManager.getStyleAttrs(key).foreground
    }
}
