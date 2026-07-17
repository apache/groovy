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
package org.codehaus.groovy.tools.groovydoc

import org.junit.jupiter.api.Test


class SimpleGroovyClassDocTests {

    @Test
    void testReplaceTags_link() {
        def text = 'Use the {@link #getComponentAt(int, int) getComponentAt} method.'

        def result = classDoc.replaceTags(text)

        assert result == "Use the <a href='#getComponentAt(int, int)'>getComponentAt</a> method."
    }

    @Test
    void testReplaceTags_literal() {
        def text = 'text with literal {@literal A<B>C} tag'

        def result = classDoc.replaceTags(text)

        assert result == 'text with literal A&lt;B&gt;C tag'
    }

    @Test
    void testReplaceTags_code() {
        def text = 'text with code {@code A<B>C} tag'

        def result = classDoc.replaceTags(text)

        assert result == 'text with code <CODE>A&lt;B&gt;C</CODE> tag'
    }

    /**
     * GROOVY-12095: {@code {@code ...}} must use brace-balanced body parsing so
     * nested braces (e.g. Groovy closures) do not terminate the tag early.
     */
    @Test
    void testReplaceTags_codeWithNestedBraces() {
        def text = '''\
            <pre>{@code
            def results = AsyncScope.withScope { scope ->
                def userTask  = scope.async { fetchUser(id) }
                def orderTask = scope.async { fetchOrders(id) }
                return [user: await(userTask), orders: await(orderTask)]
            }
            // Both tasks guaranteed complete here
            }</pre>
            '''.stripIndent()

        def result = classDoc.replaceTags(text)

        assert result.startsWith('<pre><CODE>')
        assert result.endsWith('</CODE></pre>\n') || result.endsWith('</CODE></pre>')
        assert !result.contains('</CODE>\n') || result.indexOf('</CODE>') == result.lastIndexOf('</CODE>')
        // Full body retained through the outer closing brace of withScope { ... }
        assert result.contains('scope.async { fetchUser(id) }')
        assert result.contains('scope.async { fetchOrders(id) }')
        assert result.contains('// Both tasks guaranteed complete here')
        // Single CODE wrap — the matching } of {@code ...} is the last one
        assert result.count('<CODE>') == 1
        assert result.count('</CODE>') == 1
        // Angle brackets / arrows escaped inside the code body
        assert result.contains('scope -&gt;')
    }

    @Test
    void testReplaceTags_codeWithNestedBracesInline() {
        def text = 'maps like {@code Map{K, V}} and closures {@code { x -> x * 2 }}'

        def result = classDoc.replaceTags(text)

        assert result == 'maps like <CODE>Map{K, V}</CODE> and closures <CODE>{ x -&gt; x * 2 }</CODE>'
    }

    @Test
    void testReplaceTags_literalWithNestedBraces() {
        def text = 'literal {@literal {a} and {b}} tag'

        def result = classDoc.replaceTags(text)

        assert result == 'literal {a} and {b} tag'
    }

    @Test
    void testReplaceTags_codeInsideBlockTagWithNestedBraces() {
        // skipInlineTag must also balance braces so a } inside {@code} does not
        // end the surrounding block-tag body early.
        def text = '''\
            Description.
            @param x a closure {@code { a -> a + 1 }} applied to the input
            @return the result
            '''.stripIndent()

        def result = classDoc.replaceTags(text)

        assert result.contains('<CODE>{ a -&gt; a + 1 }</CODE>')
        assert result.contains('Parameters')
        assert result.contains('Returns')
        assert result.contains('applied to the input')
    }

    @Test
    void testReplaceTags_codeDeeplyNestedBraces() {
        def text = '{@code outer { mid { inner } } end}'

        def result = classDoc.replaceTags(text)

        assert result == '<CODE>outer { mid { inner } } end</CODE>'
    }

    @Test
    void testReplaceTags_unbalancedCodeLeftLiteral() {
        // Unterminated {@code ...} — no matching close brace — is emitted
        // verbatim (renderer returns 0 and the tokenizer falls through).
        def text = 'before {@code { still open after'

        def result = classDoc.replaceTags(text)

        assert result.contains('{@code')
        assert result.contains('still open')
    }

    @Test
    void testReplaceTags_emptyCodeBody() {
        def text = 'empty {@code} tag'

        def result = classDoc.replaceTags(text)

        assert result == 'empty <CODE></CODE> tag'
    }

    @Test
    void testReplaceTags_codeThenSiblingText() {
        // Early intentional close still works: first balanced pair ends the tag.
        def text = '{@code {a}} trailing'

        def result = classDoc.replaceTags(text)

        assert result == '<CODE>{a}</CODE> trailing'
    }

    /**
     * GROOVY-12095: braces inside double-quoted string literals must not
     * terminate {@code {@code ...}}.
     */
    @Test
    void testReplaceTags_codeWithBraceInsideDoubleQuotedString() {
        def text = '{@code System.out.println(" } ")}'

        def result = classDoc.replaceTags(text)

        assert result == '<CODE>System.out.println(" } ")</CODE>'
    }

    @Test
    void testReplaceTags_codeWithBraceInsideSingleQuotedString() {
        def text = "{@code char c = '}'; return c}"

        def result = classDoc.replaceTags(text)

        assert result == "<CODE>char c = '}'; return c</CODE>"
    }

    @Test
    void testReplaceTags_codeWithOpenBraceInsideString() {
        def text = '{@code System.out.println(" { ")}'

        def result = classDoc.replaceTags(text)

        assert result == '<CODE>System.out.println(" { ")</CODE>'
    }

    @Test
    void testReplaceTags_codeWithGStringBracesInsideQuotes() {
        def text = '{@code println "value=${x}"}'

        def result = classDoc.replaceTags(text)

        assert result == '<CODE>println "value=${x}"</CODE>'
    }

    @Test
    void testReplaceTags_codeWithEscapedQuotesAndBraces() {
        def text = '{@code s = "a \\" } \\" b"}'

        def result = classDoc.replaceTags(text)

        assert result == '<CODE>s = "a \\" } \\" b"</CODE>'
    }

    @Test
    void testReplaceTags_codeMultilineWithStringBraceAndClosures() {
        def text = '''\
            {@code
            System.out.println(" } ")
            def results = withScope { scope ->
                scope.async { work() }
            }
            }
            '''.stripIndent()

        def result = classDoc.replaceTags(text)

        assert result.count('<CODE>') == 1
        assert result.count('</CODE>') == 1
        assert result.contains('System.out.println(" } ")')
        assert result.contains('scope.async { work() }')
        assert result.contains('scope -&gt;')
    }

    /**
     * GROOVY-12095: braces inside {@code //} line comments must not terminate
     * {@code {@code ...}}.
     */
    @Test
    void testReplaceTags_codeWithBraceInsideLineComment() {
        def text = '''\
            {@code
            int x = 1 // }
            def y = { it }
            }
            '''.stripIndent()

        def result = classDoc.replaceTags(text)

        assert result.count('<CODE>') == 1
        assert result.count('</CODE>') == 1
        assert result.contains('int x = 1 // }')
        assert result.contains('def y = { it }')
    }

    @Test
    void testReplaceTags_codeWithStandaloneLineCommentBrace() {
        def text = '''\
            {@code
            // }
            done()
            }
            '''.stripIndent()

        def result = classDoc.replaceTags(text)

        assert result.count('<CODE>') == 1
        assert result.count('</CODE>') == 1
        assert result.contains('// }')
        assert result.contains('done()')
        // Structural closer is the final `}`, not the one in the line comment.
        assert result.indexOf('// }') < result.indexOf('done()')
        assert result.indexOf('done()') < result.lastIndexOf('</CODE>')
    }

    @Test
    void testReplaceTags_codeWithLineCommentBraceOnSameLineAsCloserIsNotClosed() {
        // The only `}` sits inside `// ...`, so there is no structural closer.
        def text = '{@code x // note about braces }'

        def result = classDoc.replaceTags(text)

        assert result.contains('{@code')
        assert result.contains('note about braces')
    }

    @Test
    void testReplaceTags_snippetWithBraceInsideLineComment() {
        def text = '''\
            {@snippet lang="groovy" :
            // }
            if (ok) { work() }
            }
            '''.stripIndent()

        def result = classDoc.replaceTags(text)

        assert result.contains('<pre><code')
        assert result.contains('// }')
        assert result.contains('if (ok) { work() }')
        assert !result.contains('{@snippet')
        assert result.contains('</code></pre>')
    }

    @Test
    void testReplaceTags_codeInsideBlockTagWithLineCommentBrace() {
        def text = '''\
            Description.
            @param x uses {@code
            // }
            f(x)
            } carefully
            @return ok
            '''.stripIndent()

        def result = classDoc.replaceTags(text)

        assert result.contains('// }')
        assert result.contains('f(x)')
        assert result.contains('carefully')
        assert result.contains('Parameters')
        assert result.contains('Returns')
    }

    @Test
    void testReplaceTags_codeLineCommentDoesNotConsumeNextLine() {
        // After // comment ends at newline, braces on the next line still count.
        def text = '''\
            {@code
            // ignored }
            { real }
            }
            '''.stripIndent()

        def result = classDoc.replaceTags(text)

        assert result.count('<CODE>') == 1
        assert result.count('</CODE>') == 1
        assert result.contains('// ignored }')
        assert result.contains('{ real }')
    }

    @Test
    void testReplaceTags_codeInsideBlockTagWithStringBrace() {
        def text = '''\
            Description.
            @param x uses {@code System.out.println(" } ")} carefully
            @return ok
            '''.stripIndent()

        def result = classDoc.replaceTags(text)

        assert result.contains('<CODE>System.out.println(" } ")</CODE>')
        assert result.contains('carefully')
        assert result.contains('Parameters')
        assert result.contains('Returns')
    }

    @Test
    void testReplaceTags_snippetWithBraceInsideString() {
        def text = '''\
            {@snippet lang="groovy" :
            System.out.println(" } ")
            if (ok) { work() }
            }
            '''.stripIndent()

        def result = classDoc.replaceTags(text)

        assert result.contains('<pre><code')
        assert result.contains('System.out.println(" } ")')
        assert result.contains('if (ok) { work() }')
        assert !result.contains('{@snippet')
        assert result.contains('</code></pre>')
    }

    @Test
    void testReplaceTags_literalWithBraceInsideString() {
        def text = '{@literal println(" } ")}'

        def result = classDoc.replaceTags(text)

        assert result == 'println(" } ")'
    }

    @Test
    void testReplaceTags_codeWithBraceInsideBlockComment() {
        def text = '{@code int x = 1; /* brace } here */ return x}'

        def result = classDoc.replaceTags(text)

        assert result == '<CODE>int x = 1; /* brace } here */ return x</CODE>'
    }

    @Test
    void testReplaceTags_emptyStringsDoNotConfuseMatcher() {
        def text = '{@code s = ""; t = \'\'; u = " } "}'

        def result = classDoc.replaceTags(text)

        assert result == '<CODE>s = ""; t = \'\'; u = " } "</CODE>'
    }

    /**
     * GROOVY-12095: a multi-kilobyte string literal inside {@code {@code ...}}
     * must be scanned in linear time without blowing the call stack.
     */
    @Test
    void testReplaceTags_codeWithVeryLongStringLiteralDoesNotStackOverflow() {
        def longPayload = 'x' * 50_000
        def text = '{@code s = "' + longPayload + ' } more"; done}'

        def result = classDoc.replaceTags(text)

        assert result.startsWith('<CODE>s = "')
        assert result.endsWith('</CODE>')
        assert result.contains(longPayload + ' } more')
        assert result.count('<CODE>') == 1
        assert result.count('</CODE>') == 1
    }

    /**
     * GROOVY-12095: braces inside triple-double-quote text blocks must not
     * terminate {@code {@code ...}}.
     */
    @Test
    void testReplaceTags_codeWithBraceInsideDoubleQuoteTextBlock() {
        def text = '{@code s = """ } """; done}'

        def result = classDoc.replaceTags(text)

        assert result == '<CODE>s = """ } """; done</CODE>'
    }

    @Test
    void testReplaceTags_codeWithBraceInsideSingleQuoteTextBlock() {
        def text = "{@code s = ''' } '''; done}"

        def result = classDoc.replaceTags(text)

        assert result == "<CODE>s = ''' } '''; done</CODE>"
    }

    @Test
    void testReplaceTags_codeWithMultilineDoubleQuoteTextBlockContainingBrace() {
        def text = '''\
            {@code
            def s = """
              multi-line with }
              and more
            """
            def x = { it }
            }
            '''.stripIndent()

        def result = classDoc.replaceTags(text)

        assert result.count('<CODE>') == 1
        assert result.count('</CODE>') == 1
        assert result.contains('multi-line with }')
        assert result.contains('and more')
        assert result.contains('def x = { it }')
    }

    @Test
    void testReplaceTags_codeWithMultilineSingleQuoteTextBlockContainingBrace() {
        def text = """\
            {@code
            def s = '''
              multi-line with }
              and more
            '''
            def x = { it }
            }
            """.stripIndent()

        def result = classDoc.replaceTags(text)

        assert result.count('<CODE>') == 1
        assert result.count('</CODE>') == 1
        assert result.contains('multi-line with }')
        assert result.contains("'''")
        assert result.contains('def x = { it }')
    }

    @Test
    void testReplaceTags_codeWithEmptyTextBlockThenBraceInCode() {
        def text = '{@code s = """"""; t = { x }}'

        def result = classDoc.replaceTags(text)

        assert result == '<CODE>s = """"""; t = { x }</CODE>'
    }

    @Test
    void testReplaceTags_codeWithTextBlockContainingEscapedQuotesAndBrace() {
        // \""" inside the block should not close it early; the real closer is later.
        def text = '{@code s = """a \\"\\"\\" } b"""; done}'

        def result = classDoc.replaceTags(text)

        assert result == '<CODE>s = """a \\"\\"\\" } b"""; done</CODE>'
    }

    @Test
    void testReplaceTags_snippetWithMultilineTextBlockContainingBrace() {
        def text = '''\
            {@snippet lang="groovy" :
            def s = """
              }
            """
            if (ok) { work() }
            }
            '''.stripIndent()

        def result = classDoc.replaceTags(text)

        assert result.contains('<pre><code')
        assert result.contains('}')
        assert result.contains('if (ok) { work() }')
        assert !result.contains('{@snippet')
        assert result.contains('</code></pre>')
    }

    @Test
    void testReplaceTags_codeInsideBlockTagWithTextBlockBrace() {
        def text = '''\
            Description.
            @param x uses {@code s = """ } """} carefully
            @return ok
            '''.stripIndent()

        def result = classDoc.replaceTags(text)

        assert result.contains('<CODE>s = """ } """</CODE>')
        assert result.contains('carefully')
        assert result.contains('Parameters')
        assert result.contains('Returns')
    }

    @Test
    void testReplaceTags_codeWithUnterminatedTextBlockConsumesRest() {
        // Unterminated """ treats the remainder as inside the block, so the
        // would-be tag closer is not seen — the tag is left literal.
        def text = '{@code s = """ still open }'

        def result = classDoc.replaceTags(text)

        assert result.contains('{@code')
        assert result.contains('still open')
    }

    @Test
    void testEncodeAngleBracketsInTagBody() {
        def text = 'text with <tag1> outside and {@code <tag2> inside} a @code tag'
        def regex = SimpleGroovyClassDoc.CODE_REGEX
        def encodedText = SimpleGroovyClassDoc.encodeAngleBracketsInTagBody(text, regex)
        assert encodedText == 'text with <tag1> outside and {@code &lt;tag2&gt; inside} a @code tag'
    }

    @Test
    void testEncodeAngleBracketsInTagBodyWithDollar() {
        def text = 'text with dollar {@code $foo.bar} and dollar with less than {@code $foo < $bar}'
        def regex = SimpleGroovyClassDoc.CODE_REGEX
        def encodedText = SimpleGroovyClassDoc.encodeAngleBracketsInTagBody(text, regex)
        assert encodedText == 'text with dollar {@code $foo.bar} and dollar with less than {@code $foo &lt; $bar}'
    }

    @Test
    void testEncodeAngleBracketsInTagBodyLeavesSpecialChars() {
        def text = 'text with illegal group ref {@code $3 \\$}'
        def regex = SimpleGroovyClassDoc.CODE_REGEX
        def encodedText = SimpleGroovyClassDoc.encodeAngleBracketsInTagBody(text, regex)
        assert encodedText == text
    }

    @Test
    void testEncodeAngleBrackets() {
        def text = 'text with <tag1> and <tag2>'

        def encodedText = SimpleGroovyClassDoc.encodeAngleBrackets(text)

        assert encodedText == 'text with &lt;tag1&gt; and &lt;tag2&gt;'
    }

    @Test
    void testExpandDocRootTag() {
        def text = "<img src='{@docRoot}/logo.jpg'><img src='{@docRoot}logo.jpg'>"
        def doc = classDoc
        doc.fullPathName = 'DUMMY/PATH/NAME'
        def result = doc.replaceTags(text)
        assert result == "<img src='../../logo.jpg\'><img src='../../logo.jpg'>"
    }

    private getClassDoc() {
        def classDoc = new SimpleGroovyClassDoc([], 'Foo')
        classDoc.fullPathName = ''
        classDoc
    }
}
