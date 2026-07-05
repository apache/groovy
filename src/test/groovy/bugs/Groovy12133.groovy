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
package bugs

import groovy.util.regex.BalancedGroup
import org.codehaus.groovy.runtime.StringGroovyMethods
import org.junit.jupiter.api.Test

import java.util.regex.PatternSyntaxException

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertSame
import static org.junit.jupiter.api.Assertions.assertThrows
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * GROOVY-12133: findBalancedGroups — structured alternative to .NET balancing groups.
 */
final class Groovy12133 {

    @Test
    void testNullArgumentsThrowNpe() {
        assertThrows(NullPointerException, () -> StringGroovyMethods.findBalancedGroups(null, '\\(', '\\)'))
        assertThrows(NullPointerException, () -> StringGroovyMethods.findBalancedGroups('test', null, '\\)'))
        assertThrows(NullPointerException, () -> StringGroovyMethods.findBalancedGroups('test', '\\(', null))
    }

    @Test
    void testEmptyOpenOrCloseRegexRejected() {
        assertThrows(IllegalArgumentException, () -> 'x'.findBalancedGroups('', '\\)'))
        assertThrows(IllegalArgumentException, () -> 'x'.findBalancedGroups('\\(', ''))
    }

    @Test
    void testNoMatchReturnsEmptyList() {
        assertTrue('plain text'.findBalancedGroups('\\(', '\\)').isEmpty())
    }

    @Test
    void testPerfectlyNestedBuildsTree() {
        String input = 'Root: (A + (B * C) + (D(E)))'
        List<BalancedGroup> roots = input.findBalancedGroups('\\(', '\\)')

        assertEquals(1, roots.size())

        BalancedGroup root = roots[0]
        assertEquals('(A + (B * C) + (D(E)))', root.matchedString)
        assertNull(root.parent)
        assertEquals(0, root.depth)
        assertEquals(2, root.children.size())

        // Offsets comparable to .NET Capture.Index / Length
        assertEquals(input.indexOf('(A +'), root.start)
        assertEquals(root.start + root.matchedString.length(), root.end)
        assertEquals(root.matchedString.length(), root.length)
        assertEquals(root.start, root.fullStart)
        assertEquals(root.end, root.fullEnd)
        assertEquals(root.matchedString, input.substring(root.start, root.end))

        BalancedGroup nodeB = root.children[0]
        assertEquals('(B * C)', nodeB.matchedString)
        assertSame(root, nodeB.parent)
        assertEquals(1, nodeB.depth)
        assertEquals(input.indexOf('(B * C)'), nodeB.start)

        BalancedGroup nodeD = root.children[1]
        assertEquals('(D(E))', nodeD.matchedString)
        assertSame(root, nodeD.parent)

        BalancedGroup nodeE = nodeD.children[0]
        assertEquals('(E)', nodeE.matchedString)
        assertSame(nodeD, nodeE.parent)
        assertEquals(2, nodeE.depth)
    }

    @Test
    void testLeafChildrenImmutable() {
        BalancedGroup leaf = 'Leaf: ()'.findBalancedGroups('\\(', '\\)')[0]

        assertTrue(leaf.children.isEmpty())
        assertThrows(UnsupportedOperationException, () -> leaf.children.add(null))
    }

    @Test
    void testConstructorWiresParentAndRejectsReparenting() {
        BalancedGroup child = new BalancedGroup('(E)', null)
        BalancedGroup parent = new BalancedGroup('(D(E))', [child])

        assertSame(parent, child.parent)
        assertEquals(1, parent.children.size())
        assertSame(child, parent.children[0])
        assertEquals(0, child.start)
        assertEquals(3, child.end)

        assertThrows(IllegalArgumentException, () -> new BalancedGroup('other', [child]))
        assertThrows(NullPointerException, () -> new BalancedGroup(null, null))
        assertThrows(IllegalArgumentException, () -> new BalancedGroup('ab', 0, 1, 0, 1, null)) // length mismatch
        assertThrows(IllegalArgumentException, () -> new BalancedGroup('a', -1, 1, 0, 1, null))
    }

    @Test
    void testDanglingOpenRescuesCompletedChildren() {
        // .NET strict (?(Open)(?!)) would fail the whole match; we salvage closed spans.
        List<BalancedGroup> roots = '(A (B) (C(D)'.findBalancedGroups('\\(', '\\)')

        assertEquals(2, roots.size())
        assertEquals('(B)', roots[0].matchedString)
        assertEquals('(D)', roots[1].matchedString)
        assertNull(roots[0].parent)
        assertNull(roots[1].parent)
        assertEquals(0, roots[0].depth)
    }

    @Test
    void testDanglingCloseIgnored() {
        List<BalancedGroup> roots = 'A ) B (C) D )'.findBalancedGroups('\\(', '\\)')

        assertEquals(1, roots.size())
        assertEquals('(C)', roots[0].matchedString)
    }

    @Test
    void testIncludeEdgesFalseStripsDelimitersLikeNetBalancingCapture() {
        // .NET (?<Content-Open>\)) captures the interval between Open and close, not the delimiters.
        String input = 'func(arg1, arg2)'
        def options = BalancedGroup.MatchOptions.defaults().withIncludeEdges(false)
        List<BalancedGroup> roots = input.findBalancedGroups('\\(', '\\)', options)

        assertEquals(1, roots.size())
        BalancedGroup g = roots[0]
        assertEquals('arg1, arg2', g.matchedString)
        assertEquals(5, g.start)
        assertEquals(15, g.end)
        assertEquals(4, g.fullStart)  // '('
        assertEquals(16, g.fullEnd)   // after ')'
        assertEquals('(arg1, arg2)', input.substring(g.fullStart, g.fullEnd))
        assertEquals('arg1, arg2', input.substring(g.start, g.end))
    }

    @Test
    void testIncludeEdgesFalseWithEmptyContent() {
        def options = BalancedGroup.MatchOptions.defaults().withIncludeEdges(false)
        List<BalancedGroup> roots = 'Empty: ()'.findBalancedGroups('\\(', '\\)', options)

        assertEquals(1, roots.size())
        assertEquals('', roots[0].matchedString)
        assertEquals(0, roots[0].length)
        assertEquals(roots[0].start, roots[0].end)
        assertTrue(roots[0].fullEnd > roots[0].fullStart)
    }

    @Test
    void testIgnoreRegexSkipsLiterals() {
        String code = 'if(true) { String s = "}"; }'
        def options = BalancedGroup.MatchOptions.defaults()
            .withIgnoreRegex('"(?:\\\\.|[^"\\\\])*"')

        List<BalancedGroup> roots = code.findBalancedGroups('\\{', '\\}', options)

        assertEquals(1, roots.size())
        assertEquals('{ String s = "}"; }', roots[0].matchedString)
    }

    @Test
    void testBlankIgnoreRegexTreatedAsAbsent() {
        def options = BalancedGroup.MatchOptions.defaults().withIgnoreRegex('   ')
        List<BalancedGroup> roots = '(Test)'.findBalancedGroups('\\(', '\\)', options)

        assertEquals('(Test)', roots[0].matchedString)
    }

    @Test
    void testNullOptionsUsesDefaults() {
        List<BalancedGroup> roots = '(A)'.findBalancedGroups('\\(', '\\)', null)
        assertEquals('(A)', roots[0].matchedString)

        List<BalancedGroup> rootsOverload = '[B]'.findBalancedGroups('\\[', '\\]')
        assertEquals('[B]', rootsOverload[0].matchedString)
    }

    @Test
    void testNestedHtmlDivsWithAttributes() {
        String html = '''
            <div id="app" class="container">
                <div class="header" style="color: red;">Header</div>
                <div class="body">
                    <div class="content">Content</div>
                </div>
            </div>
            <div id="footer">Footer</div>
            '''

        List<BalancedGroup> roots = html.findBalancedGroups('<div\\b[^>]*>', '</div>')

        assertEquals(2, roots.size())

        BalancedGroup appDiv = roots[0]
        assertTrue(appDiv.matchedString.startsWith('<div id="app"'))
        assertEquals(2, appDiv.children.size())
        assertEquals(html.substring(appDiv.start, appDiv.end), appDiv.matchedString)

        assertEquals('<div class="header" style="color: red;">Header</div>',
            appDiv.children[0].matchedString)

        BalancedGroup bodyDiv = appDiv.children[1]
        assertEquals(1, bodyDiv.children.size())
        assertEquals('<div class="content">Content</div>',
            bodyDiv.children[0].matchedString)
    }

    @Test
    void testHtmlIncludeEdgesFalse() {
        String html = '<div class="wrapper">\n  <p>Hello World</p>\n</div>'
        def options = BalancedGroup.MatchOptions.defaults().withIncludeEdges(false)

        List<BalancedGroup> roots = html.findBalancedGroups('<div\\b[^>]*>', '</div>', options)

        assertEquals(1, roots.size())
        assertEquals('\n  <p>Hello World</p>\n', roots[0].matchedString)
        assertTrue(roots[0].fullStart < roots[0].start)
        assertTrue(roots[0].fullEnd > roots[0].end)
    }

    @Test
    void testIgnoreHtmlComments() {
        String html = '''
            <div id="main">
                <!-- <div class="fake">not real</div> -->
                <div class="real">Valid Content</div>
            </div>
            '''

        def options = BalancedGroup.MatchOptions.defaults()
            .withIgnoreRegex('(?s)<!--.*?-->')

        List<BalancedGroup> roots = html.findBalancedGroups('<div\\b[^>]*>', '</div>', options)

        assertEquals(1, roots.size())

        BalancedGroup mainDiv = roots[0]
        assertTrue(mainDiv.matchedString.contains('Valid Content'))
        assertEquals(1, mainDiv.children.size())
        assertEquals('<div class="real">Valid Content</div>', mainDiv.children[0].matchedString)
    }

    @Test
    void testIgnoreScriptBlocksContainingFakeTags() {
        String html = '''
            <div id="react-root">
                <script type="text/javascript">
                    const fakeHtml = "</div>";
                </script>
                <div class="app">App Ready</div>
            </div>
            '''

        def options = BalancedGroup.MatchOptions.defaults()
            .withIgnoreRegex('(?s)<script\\b[^>]*>.*?</script>')

        List<BalancedGroup> roots = html.findBalancedGroups('<div\\b[^>]*>', '</div>', options)

        assertEquals(1, roots.size())
        assertEquals(1, roots[0].children.size())
        assertEquals('<div class="app">App Ready</div>', roots[0].children[0].matchedString)
    }

    @Test
    void testIgnoreSelfClosingTags() {
        String html = '''
            <div id="parent">
                <div class="placeholder" /> <div class="child">I am child</div>
            </div>
            '''

        def options = BalancedGroup.MatchOptions.defaults()
            .withIgnoreRegex('<div\\b[^>]*/>')

        List<BalancedGroup> roots = html.findBalancedGroups('<div\\b[^>]*>', '</div>', options)

        assertEquals(1, roots.size())
        assertEquals(1, roots[0].children.size())
        assertEquals('<div class="child">I am child</div>', roots[0].children[0].matchedString)
    }

    @Test
    void testMalformedOuterHtmlRescuesInner() {
        String html = '''
            <div class="broken-wrapper">
                <div class="lost-tag"> <div class="target">I am safe!</div>
            '''

        List<BalancedGroup> roots = html.findBalancedGroups('<div\\b[^>]*>', '</div>')

        assertEquals(1, roots.size())
        assertEquals('<div class="target">I am safe!</div>', roots[0].matchedString)
    }

    @Test
    void testMultipleTopLevelGroups() {
        String input = '(a)(b(c))(d)'
        List<BalancedGroup> roots = input.findBalancedGroups('\\(', '\\)')

        assertEquals(3, roots.size())
        assertEquals('(a)', roots[0].matchedString)
        assertEquals('(b(c))', roots[1].matchedString)
        assertEquals('(c)', roots[1].children[0].matchedString)
        assertEquals('(d)', roots[2].matchedString)

        // Successive top-level spans are ordered and non-overlapping
        assertTrue(roots[0].fullEnd <= roots[1].fullStart)
        assertTrue(roots[1].fullEnd <= roots[2].fullStart)
        roots.each { g ->
            assertEquals(input.substring(g.start, g.end), g.matchedString)
        }
    }

    @Test
    void testReturnedListIsUnmodifiable() {
        List<BalancedGroup> roots = '(a)'.findBalancedGroups('\\(', '\\)')
        assertThrows(UnsupportedOperationException, () -> roots.add(null))
    }

    @Test
    void testCharSequenceOtherThanString() {
        CharSequence cs = new StringBuilder('pre(x(y))post')
        List<BalancedGroup> roots = cs.findBalancedGroups('\\(', '\\)')

        assertEquals(1, roots.size())
        assertEquals('(x(y))', roots[0].matchedString)
        assertEquals('(y)', roots[0].children[0].matchedString)
        assertEquals(3, roots[0].start)
        assertEquals(9, roots[0].end)
    }

    @Test
    void testDeepNestingDepthAndOffsets() {
        String input = '((((a))))'
        BalancedGroup root = input.findBalancedGroups('\\(', '\\)')[0]

        assertEquals(0, root.depth)
        BalancedGroup n = root
        int expectedDepth = 0
        while (!n.children.isEmpty()) {
            n = n.children[0]
            expectedDepth++
            assertEquals(expectedDepth, n.depth)
            assertEquals(input.substring(n.start, n.end), n.matchedString)
        }
        assertEquals('(a)', n.matchedString)
        assertEquals(3, n.depth) // ((((a)))) → four nodes, depths 0..3
    }

    @Test
    void testRepeatedCallsReuseTokenizerCache() {
        // Behavioural smoke test: many identical calls must stay correct (cache hit path).
        100.times {
            def roots = 'x(a(b))y(c)'.findBalancedGroups('\\(', '\\)')
            assertEquals(2, roots.size())
            assertEquals('(a(b))', roots[0].matchedString)
            assertEquals('(b)', roots[0].children[0].matchedString)
            assertEquals('(c)', roots[1].matchedString)
        }
    }

    @Test
    void testAdjacentAndNestedMixLikeNetCaptureCollectionUseCases() {
        // Classic .NET sample shape: <abc><mno<xyz>> as angle-bracket pairs
        String input = '<abc><mno<xyz>>'
        List<BalancedGroup> roots = input.findBalancedGroups('<', '>')

        assertEquals(2, roots.size())
        assertEquals('<abc>', roots[0].matchedString)
        assertTrue(roots[0].children.isEmpty())
        assertEquals('<mno<xyz>>', roots[1].matchedString)
        assertEquals(1, roots[1].children.size())
        assertEquals('<xyz>', roots[1].children[0].matchedString)
    }


    @Test
    void testBalancedGroupFindDirectApiAndToString() {
        List<BalancedGroup> viaStatic = BalancedGroup.find('(x)', '\\(', '\\)')
        assertEquals(1, viaStatic.size())
        assertEquals('(x)', viaStatic[0].toString())
        assertEquals('(x)', viaStatic[0].matchedString)

        List<BalancedGroup> viaStaticOpts = BalancedGroup.find(
            'f(y)', '\\(', '\\)', BalancedGroup.MatchOptions.defaults().withIncludeEdges(false))
        assertEquals('y', viaStaticOpts[0].matchedString)
    }

    @Test
    void testBalancedGroupFindNullTextThrows() {
        assertThrows(NullPointerException, () -> BalancedGroup.find(null, '\\(', '\\)'))
        assertThrows(NullPointerException, () -> BalancedGroup.find(null, '\\(', '\\)', null))
    }

    @Test
    void testInvalidPatternThrowsPatternSyntaxException() {
        assertThrows(PatternSyntaxException, () -> '(a)'.findBalancedGroups('[', '\\)'))
        assertThrows(PatternSyntaxException, () -> BalancedGroup.find('a', '\\(', '*'))
        assertThrows(PatternSyntaxException,
            () -> BalancedGroup.find('{}', '\\{', '\\}',
                BalancedGroup.MatchOptions.defaults().withIgnoreRegex('[')))
    }

    @Test
    void testConstructorRejectsInvalidFullRangeAndEndBeforeStart() {
        assertThrows(IllegalArgumentException, () -> new BalancedGroup('a', 3, 1, 0, 1, null))
        assertThrows(IllegalArgumentException, () -> new BalancedGroup('a', 0, 1, 3, 1, null))
        assertThrows(IllegalArgumentException, () -> new BalancedGroup('a', 0, 1, -1, 0, null))
        assertThrows(NullPointerException, () -> new BalancedGroup('a', 0, 1, 0, 1, [null]))
    }

    @Test
    void testConstructorAcceptsEmptyListAndAbsoluteOffsets() {
        BalancedGroup leaf = new BalancedGroup('leaf', [])
        assertTrue(leaf.children.isEmpty())
        assertEquals(0, leaf.start)
        assertEquals(4, leaf.end)
        assertEquals(0, leaf.fullStart)
        assertEquals(4, leaf.fullEnd)

        BalancedGroup absolute = new BalancedGroup('xy', 10, 12, 8, 15, null)
        assertEquals('xy', absolute.matchedString)
        assertEquals(10, absolute.start)
        assertEquals(12, absolute.end)
        assertEquals(8, absolute.fullStart)
        assertEquals(15, absolute.fullEnd)
        assertEquals(2, absolute.length)
    }

    @Test
    void testMatchOptionsWithersPreserveOtherFields() {
        def base = BalancedGroup.MatchOptions.defaults()
        assertNull(base.ignoreRegex())
        assertTrue(base.includeEdges())

        def noEdges = base.withIncludeEdges(false)
        assertFalse(noEdges.includeEdges())
        assertNull(noEdges.ignoreRegex())

        def withIgnore = noEdges.withIgnoreRegex('"(?:\\\\.|[^"\\\\])*"')
        assertFalse(withIgnore.includeEdges())
        assertEquals('"(?:\\\\.|[^"\\\\])*"', withIgnore.ignoreRegex())

        def edgesBack = withIgnore.withIncludeEdges(true)
        assertTrue(edgesBack.includeEdges())
        assertEquals(withIgnore.ignoreRegex(), edgesBack.ignoreRegex())

        def cleared = edgesBack.withIgnoreRegex(null)
        assertNull(cleared.ignoreRegex())
        assertTrue(cleared.includeEdges())
    }

    @Test
    void testOnlyDanglingOpensYieldEmptySnapshot() {
        // Orphan rescue with no completed children: addAllChildren early-return path
        assertTrue('(((('.findBalancedGroups('\\(', '\\)').isEmpty())
        assertTrue(BalancedGroup.find('open only (', '\\(', '\\)').isEmpty())
    }

    @Test
    void testCompletedGroupThenDanglingOpen() {
        List<BalancedGroup> roots = '(done)(still-open'.findBalancedGroups('\\(', '\\)')
        assertEquals(1, roots.size())
        assertEquals('(done)', roots[0].matchedString)
    }

    @Test
    void testOrphanRescueMergesIntoParentThatAlreadyHasChildren() {
        // Super-root / outer frames already holding children when a dangling frame is popped
        // exercises PendingGroup.addAllChildren merge branch (addAll vs list steal).
        String input = '(outer (first) (second-open (inner)'
        List<BalancedGroup> roots = input.findBalancedGroups('\\(', '\\)')

        assertEquals(2, roots.size())
        assertEquals('(first)', roots[0].matchedString)
        assertEquals('(inner)', roots[1].matchedString)
    }

    @Test
    void testNestedThenSiblingAfterClose() {
        String input = '(a(b))(c)'
        List<BalancedGroup> roots = input.findBalancedGroups('\\(', '\\)')
        assertEquals(2, roots.size())
        assertEquals('(a(b))', roots[0].matchedString)
        assertEquals('(b)', roots[0].children[0].matchedString)
        assertEquals('(c)', roots[1].matchedString)
        // second addChild on the same pending frame (super-root)
        assertEquals(0, roots[0].depth)
        assertEquals(0, roots[1].depth)
    }

    @Test
    void testIgnoreRegexCacheKeyDistinctFromNoIgnore() {
        String text = '{ "x": 1 }'
        def withIgnore = BalancedGroup.MatchOptions.defaults()
            .withIgnoreRegex('"(?:\\\\.|[^"\\\\])*"')
        // Without ignore, the quote-embedded content is not special; braces still balance.
        assertEquals(1, text.findBalancedGroups('\\{', '\\}').size())
        assertEquals(1, text.findBalancedGroups('\\{', '\\}', withIgnore).size())
        // Hit both tokenizer cache keys (with and without IGNORE alternative)
        assertEquals(1, text.findBalancedGroups('\\{', '\\}').size())
        assertEquals(1, text.findBalancedGroups('\\{', '\\}', withIgnore).size())
    }

    @Test
    void testTokenizerCacheEvictionBeyondLimit() {
        // TOKENIZER_CACHE_LIMIT is 64; filling unique ignore keys exercises removeEldestEntry.
        def base = BalancedGroup.MatchOptions.defaults()
        70.times { int i ->
            def opts = base.withIgnoreRegex("never_match_${i}_xyz")
            List<BalancedGroup> roots = '(ok)'.findBalancedGroups('\\(', '\\)', opts)
            assertEquals(1, roots.size())
            assertEquals('(ok)', roots[0].matchedString)
        }
        // Still correct after eviction churn
        assertEquals('(ok)', '(ok)'.findBalancedGroups('\\(', '\\)')[0].matchedString)
    }

    @Test
    void testZeroWidthOpenLookaheadWithIncludeEdgesFalse() {
        // Zero-width OPEN then CLOSE: interior is the span between openEnd and closeStart.
        def opts = BalancedGroup.MatchOptions.defaults().withIncludeEdges(false)
        List<BalancedGroup> roots = BalancedGroup.find('ab', '(?=a)', 'b', opts)
        assertEquals(1, roots.size())
        assertEquals('a', roots[0].matchedString)
        assertEquals(roots[0].start, roots[0].fullStart) // open was zero-width at fullStart
        assertTrue(roots[0].fullEnd > roots[0].end || roots[0].fullEnd >= roots[0].end)
    }

    @Test
    void testCloseWhenOpenStackIsOnlyVirtualRootIsIgnored() {
        // Explicit extra closers before any open — CLOSE branch with stack.size() == 1
        assertTrue(')))'.findBalancedGroups('\\(', '\\)').isEmpty())
        assertEquals(['(x)'], ')))(x)))'.findBalancedGroups('\\(', '\\)')*.matchedString)
    }

    @Test
    void testGStringCharSequenceSource() {
        def name = 'world'
        GString g = "(hello $name)"
        List<BalancedGroup> roots = g.findBalancedGroups('\\(', '\\)')
        assertEquals(1, roots.size())
        assertEquals('(hello world)', roots[0].matchedString)
        assertEquals(g.toString(), roots[0].matchedString)
        assertEquals(0, roots[0].start)
        assertEquals(g.toString().length(), roots[0].end)
    }

    @Test
    void testIncludeEdgesTrueExplicitOptions() {
        def opts = BalancedGroup.MatchOptions.defaults().withIncludeEdges(true)
        BalancedGroup g = '(z)'.findBalancedGroups('\\(', '\\)', opts)[0]
        assertEquals('(z)', g.matchedString)
        assertEquals(g.start, g.fullStart)
        assertEquals(g.end, g.fullEnd)
    }

    @Test
    void testMultipleChildrenOnSameFrameViaAddChild() {
        // Three completed children under one parent → repeated addChild growth
        String input = '(p (a) (b) (c))'
        BalancedGroup root = input.findBalancedGroups('\\(', '\\)')[0]
        assertEquals(3, root.children.size())
        assertEquals(['(a)', '(b)', '(c)'], root.children*.matchedString)
        root.children.each { child ->
            assertSame(root, child.parent)
            assertEquals(1, child.depth)
        }
    }

    @Test
    void testDeepOrphanRescueChain() {
        // Several unfinished opens each carrying a completed child
        String input = '(a (b)) (c (d (e)'
        List<BalancedGroup> roots = input.findBalancedGroups('\\(', '\\)')
        assertEquals(2, roots.size())
        assertEquals('(a (b))', roots[0].matchedString)
        assertEquals('(b)', roots[0].children[0].matchedString)
        assertEquals('(e)', roots[1].matchedString)
    }

    @Test
    void testNullIgnoreRegexOnOptionsIsAbsent() {
        def opts = BalancedGroup.MatchOptions.defaults().withIgnoreRegex(null)
        assertNull(opts.ignoreRegex())
        assertEquals('(t)', '(t)'.findBalancedGroups('\\(', '\\)', opts)[0].matchedString)
    }

    @Test
    void testEmptyInput() {
        assertTrue(''.findBalancedGroups('\\(', '\\)').isEmpty())
        assertTrue(BalancedGroup.find('', '\\(', '\\)').isEmpty())
    }
}
