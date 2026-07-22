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
package groovy.markdown

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertThrows

class MarkdownSlurperTest {

    @Test
    void testParseHeading() {
        // tag::parse_heading[]
        def doc = new MarkdownSlurper().parseText('# Hello World')
        def h = doc.headings[0]
        assert h.level == 1
        assert h.text == 'Hello World'
        // end::parse_heading[]
    }

    @Test
    void testCodeBlocksByLanguage() {
        // tag::code_blocks[]
        def md = '''
            Some intro text.

            ```groovy
            println 'hi'
            ```

            ```sql
            SELECT 1
            ```
            '''.stripIndent()
        def doc = new MarkdownSlurper().parseText(md)
        def groovySnippets = doc.codeBlocks.findAll { it.lang == 'groovy' }*.text
        assert groovySnippets == ["println 'hi'\n"]
        // end::code_blocks[]
    }

    @Test
    void testLinks() {
        // tag::links[]
        def doc = new MarkdownSlurper().parseText('See [docs](https://example.com/docs).')
        def link = doc.links[0]
        assert link.href == 'https://example.com/docs'
        assert link.text == 'docs'
        // end::links[]
    }

    @Test
    void testSection() {
        // tag::section[]
        def md = '''
            # Title

            ## Summary

            The work is on track.

            ## Next Steps

            - Item one
            - Item two
            '''.stripIndent()
        def doc = new MarkdownSlurper().parseText(md)
        def summary = doc.section('Summary')
        assert summary[0].type == 'paragraph'
        assert summary[0].children[0].value == 'The work is on track.'

        def next = doc.section('Next Steps')
        assert next[0].type == 'list'
        assert next[0].items*.text == ['Item one', 'Item two']
        // end::section[]
    }

    @Test
    void testTables() {
        // tag::tables[]
        def md = '''
            | name  | age |
            |-------|-----|
            | Alice | 30  |
            | Bob   | 25  |
            '''.stripIndent()
        def doc = new MarkdownSlurper().enableTables(true).parseText(md)
        def rows = doc.tables[0].rows
        assert rows[0].name == 'Alice'
        assert rows[0].age == '30'
        assert rows[1].name == 'Bob'
        // end::tables[]
    }

    @Test
    void testEmptyInput() {
        def doc = new MarkdownSlurper().parseText('')
        assert doc.nodes.isEmpty()
    }

    @Test
    void testParseFromReader() {
        def reader = new StringReader('# Hello')
        def doc = new MarkdownSlurper().parse(reader)
        assert doc.headings[0].text == 'Hello'
    }

    @Test
    void testNestedCodeBlockFound() {
        def md = '''
            > Quoted block
            >
            > ```groovy
            > println 'in quote'
            > ```
            '''.stripIndent()
        def doc = new MarkdownSlurper().parseText(md)
        assert doc.codeBlocks.size() == 1
        assert doc.codeBlocks[0].lang == 'groovy'
    }

    @Test
    void testTextProjection() {
        def doc = new MarkdownSlurper().parseText('**Hello** *world*')
        assert doc.text.trim() == 'Hello world'
    }

    @Test
    void testListItems() {
        def doc = new MarkdownSlurper().parseText('- one\n- two\n- three')
        def list = doc.nodes[0]
        assert list.type == 'list'
        assert list.ordered == false
        assert list.items*.text == ['one', 'two', 'three']
    }

    @Test
    void testTablesDisabledByDefault() {
        def md = '| a | b |\n|---|---|\n| 1 | 2 |\n'
        def doc = new MarkdownSlurper().parseText(md)
        assert doc.tables.isEmpty()
    }

    @Test
    void testDeeplyNestedInputRejected() {
        // a small (~5 KB) document nests block quotes thousands deep; the recursive
        // document walk must not be driven into a StackOverflowError
        def slurper = new MarkdownSlurper()
        slurper.maxNestingDepth = 1000 // explicit, so the ambient property cannot affect the result
        def md = ('>' * 5000) + ' x'
        def ex = assertThrows(MarkdownRuntimeException) { slurper.parseText(md) }
        assert ex.message.contains('nesting depth')
    }

    @Test
    void testMaxNestingDepthConfigurable() {
        def savedDepth = System.clearProperty('groovy.markdown.maxNestingDepth')
        try {
            def slurper = new MarkdownSlurper()
            assert slurper.maxNestingDepth == MarkdownSlurper.DEFAULT_MAX_NESTING_DEPTH
            slurper.maxNestingDepth = 3
            assertThrows(MarkdownRuntimeException) { slurper.parseText(('>' * 10) + ' x') }
            // input within the limit still parses
            assert slurper.parseText('> quoted').nodes[0].type == 'block_quote'
        } finally {
            if (savedDepth != null) System.setProperty('groovy.markdown.maxNestingDepth', savedDepth)
        }
    }

    @Test
    void testDeeplyNestedInlineEmphasisRejected() {
        // second DoS vector: deeply nested inline emphasis overflows *inside* CommonMark's own
        // inline processing, before the block-nesting pre-check can run. It must still surface as
        // a MarkdownRuntimeException, not a raw StackOverflowError.
        def md = ('*' * 50000) + 'a' + ('*' * 50000)
        assertThrows(MarkdownRuntimeException) { new MarkdownSlurper().parseText(md) }
    }

    @Test
    void testNestingDepthDoc() {
        // tag::nesting_depth[]
        def slurper = new MarkdownSlurper()
        slurper.maxNestingDepth = 100   // cap block nesting (default 1000; 0 disables the check)

        // a hostile, deeply nested document is rejected, not allowed to crash the parser
        def rejected = false
        try {
            slurper.parseText(('>' * 5000) + ' text')
        } catch (MarkdownRuntimeException ignored) {
            rejected = true
        }
        assert rejected
        // end::nesting_depth[]
    }
}
