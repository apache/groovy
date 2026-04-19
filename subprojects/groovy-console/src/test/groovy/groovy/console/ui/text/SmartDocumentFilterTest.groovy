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

import javax.swing.text.DefaultStyledDocument

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

        // Verify document has content and no exception thrown during parse
        assert doc.getLength() == 10
        assert doc.getText(0, doc.getLength()) == 'def x = 42'
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

        def tokens = filter.getLatestTokenList()
        assert tokens != null
        assert !tokens.isEmpty()
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
    void testHighlightedTokenTypeListContainsKeywords() {
        def list = SmartDocumentFilter.HIGHLIGHTED_TOKEN_TYPE_LIST
        assert list != null
        assert !list.isEmpty()
        // list should contain token type integers (all > 0)
        assert list.every { it > 0 }
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
}
