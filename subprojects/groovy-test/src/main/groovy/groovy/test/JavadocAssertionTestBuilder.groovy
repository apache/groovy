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
package groovy.test

import java.util.regex.Pattern

/**
 * <code>JavadocAssertionTestBuilder</code> dynamically creates test cases from Groovy assertions placed within
 * the doc comments of a source file. Four authoring forms are recognised:
 *
 * <ol>
 * <li>Traditional Javadoc with an HTML wrapper carrying <code>class="groovyTestCase"</code>:
 * <pre>/&#42;&#42; &lt;pre class="groovyTestCase"&gt; assert "example".size() == 7 &lt;/pre&gt; &#42;/</pre></li>
 * <li>JEP 467 Markdown doc comments (triple-slash <code>///</code> runs) with the same HTML wrapper — Markdown
 * passes inline HTML through, so the existing convention works unchanged:
 * <pre>/// &lt;pre class="groovyTestCase"&gt; assert 1 + 1 == 2 &lt;/pre&gt;</pre></li>
 * <li>Fenced Markdown code block inside a <code>///</code> run whose infostring is <code>groovy groovyTestCase</code>.
 * CommonMark uses the first word as the language (so Prism/highlight.js still highlight the body as Groovy); the
 * second word is the test marker recognised by this builder when it scans the source:
 * <pre>
 * /// ```groovy groovyTestCase
 * /// assert 2 + 2 == 4
 * /// ```
 * </pre></li>
 * <li>JEP 413 <code>{@snippet}</code> tag with an <code>id</code> attribute of <code>groovyTestCase</code>.
 * The body is brace-balanced per JEP 413, so the containing assertion may use literal <code>&lt;</code> /
 * <code>&gt;</code> without HTML escaping. Any <code>{</code> / <code>}</code> pairs inside the body must still
 * balance (groovydoc's snippet parser does dumb brace counting with no string-awareness; the same rule applies here).
 * Uses <code>id=</code> rather than <code>class=</code> because JEP 413 interprets <code>class=</code> as a
 * reference to a source file on the snippet path — javadoc warns when that file isn't found.
 * <pre>/&#42;&#42; {&#64;snippet lang="groovy" id="groovyTestCase" :
 *   assert "a&lt;b&gt;c".size() == 5
 * } &#42;/</pre></li>
 * </ol>
 *
 * When extracting the code for an HTML-wrapper test, single-line snippets of code within a {{@code @code} ...}
 * tag have the javadoc {@code code} tag stripped, and HTML entities are converted back so that {@code &lt;} and
 * {@code &gt;} become {@code <} and {@code >}. Fenced-block and {@code {@snippet}} extractions pass through
 * without HTML decoding because those forms hold raw source already.
 */
class JavadocAssertionTestBuilder {
    // TODO write tests for this classes functionality
    // Match both traditional `/&#42;&#42; ... &#42;/` Javadoc and contiguous `///` Markdown doc-comment runs.
    private static final Pattern javadocPattern =
        Pattern.compile( /(?ims)\/\*\*.*?\*\/|(?-s:(?:^[ \t]*\/\/\/[^\n]*\n?)+)/ )
    // Three authoring forms for the assertion inside a doc comment:
    //   - HTML wrapper:   <pre class="groovyTestCase">...</pre>  (or <code>, <samp>, ...).
    //     Other classes may be present alongside groovyTestCase — e.g.
    //     <pre class="language-groovy groovyTestCase"> combines Prism highlighting
    //     with test extraction.
    //   - Fenced Markdown: ```groovy groovyTestCase ... ``` — only recognised within `///` runs
    //     (Javadoc `/** */` is not Markdown, so there's no ambiguity).
    //   - JEP 413 {@snippet}: {@snippet lang="groovy" id="groovyTestCase" : body}
    //     The body is brace-balanced per JEP 413, so regex can't capture it
    //     directly. The opener regex below locates the tag; the body is then
    //     extracted programmatically by walking braces in findSnippetTestCaseTags.
    //     Uses id= (not class=) so javadoc doesn't treat groovyTestCase as a
    //     reference to a source file on its snippet path.
    private static final Pattern assertionPattern =
        Pattern.compile(
            /(?ims)<([a-z]+)\s+class\s*=\s*['"][^'"]*\bgroovyTestCase\b[^'"]*['"]\s*>.*?<\s*\/\s*\1>/
            + /|(?m:^[ \t]*\/\/\/[ \t]*```groovy[ \t]+groovyTestCase\b[^\n]*\n(?s:.*?)^[ \t]*\/\/\/[ \t]*```)/
        )
    // {@snippet ... id="groovyTestCase" ... :   — opener up to the body-introducing colon.
    // [^:{}]* keeps the match inside a single tag; attribute values don't
    // contain braces or colons in practice.
    private static final Pattern snippetOpenerPattern =
        Pattern.compile(
            /(?s)\{@snippet\b[^:{}]*\bid\s*=\s*['"]\s*groovyTestCase\s*['"][^:{}]*:/
        )

    /**
     * Builds a synthetic test class from assertion snippets found in the supplied source text.
     *
     * @param filename the source file name used to derive the generated test class name
     * @param code the source text to scan for assertion snippets
     * @return the generated test class, or {@code null} when no assertion snippets are present
     */
    Class buildTest(String filename, String code) {
        Class test = null

        List assertionTags = getAssertionTags(code)
        if (assertionTags) {
            String testName = getTestName(filename)

            Map lineNumberToAssertions = getLineNumberToAssertionsMap(code, assertionTags)
            List testMethods = getTestMethods(lineNumberToAssertions, filename)
            String testCode = getTestCode(testName, testMethods)

            test = createClass(testCode)
        }

        return test
    }

    private List getAssertionTags(String code) {
        List assertions = new ArrayList()

        code.eachMatch(javadocPattern) { javadoc ->
            assertions.addAll(javadoc.findAll(assertionPattern))
            assertions.addAll(findSnippetTestCaseTags(javadoc))
        }

        return assertions
    }

    // Scan {@snippet ... id="groovyTestCase" : body } tags and return
    // each full tag string from '{@snippet' to the matching close brace.
    // Body brace-balancing is done programmatically because regex can't count
    // nested braces — this mirrors the parser in
    // org.codehaus.groovy.tools.groovydoc.TagRenderer which does the same dumb
    // counting (no string/comment awareness) so the semantics stay consistent
    // between rendered output and test extraction.
    private static List<String> findSnippetTestCaseTags(String javadoc) {
        List<String> results = []
        def m = snippetOpenerPattern.matcher(javadoc)
        while (m.find()) {
            int start = m.start()
            int bodyStart = m.end()  // immediately after the `:` that opens the body
            int depth = 1
            int j = bodyStart
            int n = javadoc.length()
            while (j < n && depth > 0) {
                char c = javadoc.charAt(j)
                if (c == (char) '{') depth++
                else if (c == (char) '}') {
                    depth--
                    if (depth == 0) break
                }
                j++
            }
            if (depth != 0) continue  // unterminated tag; skip
            results << javadoc.substring(start, j + 1)
        }
        return results
    }

    private String getTestName(String filename) {
        String filenameWithoutPath = new File(filename).name
        String testName = filenameWithoutPath.substring(0, filenameWithoutPath.lastIndexOf(".")) +
            "JavadocAssertionTest"

        return testName
    }

    private Map getLineNumberToAssertionsMap(String code, List assertionTags) {
        Map lineNumberToAssertions = [:] as LinkedHashMap

        int codeIndex = 0
        assertionTags.each { tag ->
            codeIndex = code.indexOf(tag, codeIndex)
            // Counting newlines (+1 for the starting line) rather than matching
            // `(?m)^` avoids the end-of-input edge case where a match that
            // anchors at a line start lands on the first character right after
            // the substring's trailing newline — Java's (?m)^ does not match
            // at end-of-input, which would otherwise under-count by one.
            int lineNumber = code.substring(0, codeIndex).count('\n') + 1
            codeIndex += tag.size()

            String assertion = getAssertion(tag)

            lineNumberToAssertions.get(lineNumber, []) << assertion
        }

        return lineNumberToAssertions
    }

    private String getAssertion(String tag) {
        // Dispatch on the leading non-whitespace character.
        String trimmed = tag.trim()
        if (trimmed.startsWith('<')) return getHtmlWrapperAssertion(tag)
        if (trimmed.startsWith('{@snippet')) return getSnippetAssertion(tag)
        return getFencedAssertion(tag)
    }

    // Extract the body of a {@snippet ... id="groovyTestCase" : body }
    // tag. The body is the text between the body-opener `:` and the matching
    // closing `}` (tag was pre-sliced by findSnippetTestCaseTags). Line
    // prefixes (`*` for /** */ comments, `///` for Markdown doc comments) are
    // stripped. No HTML-entity decoding — snippet bodies are verbatim per JEP 413.
    private String getSnippetAssertion(String tag) {
        int colon = findSnippetBodyColon(tag)
        int bodyEnd = tag.lastIndexOf('}')
        if (colon < 0 || bodyEnd <= colon) return ''
        String body = tag.substring(colon + 1, bodyEnd)
        return body.replaceAll("(?m)^\\s*(?:\\*|\\/\\/\\/)\\s?", "")
    }

    // Find the `:` that introduces the body, skipping any `:` that appears
    // inside a quoted attribute value. Returns -1 if not found.
    private static int findSnippetBodyColon(String tag) {
        int i = '{@snippet'.length()
        int n = tag.length()
        while (i < n) {
            char c = tag.charAt(i)
            if (c == (char) '"' || c == (char) "'") {
                char q = c
                i++
                while (i < n && tag.charAt(i) != q) i++
                if (i < n) i++  // consume closing quote
            } else if (c == (char) ':') {
                return i
            } else {
                i++
            }
        }
        return -1
    }

    private String getHtmlWrapperAssertion(String tag) {
        String tagInner = tag.substring(tag.indexOf(">")+1, tag.lastIndexOf("<"))
        // Strip `* ` (traditional Javadoc) OR `/// ` (JEP 467 Markdown) line prefixes.
        String htmlAssertion = tagInner.replaceAll("(?m)^\\s*(?:\\*|\\/\\/\\/)\\s?", "")
        String assertion = htmlAssertion
        // TODO improve on this
        [nbsp:' ', gt:'>', lt:'<', quot:'"', apos:"'", at:'@', '#64':'@', ndash:'-', amp:'&'].each { key, value ->
            assertion = assertion.replaceAll("(?i)&$key;", value)
        }
        assertion = assertion.replaceAll(/(?i)\{@code ([^}]*)\}/, '$1')
        return assertion
    }

    // Extract the body of a fenced `` ```groovy groovyTestCase ... ``` `` block
    // embedded in a `///` run. Strip the opening and closing fence lines, and
    // remove the `///` line prefix (plus one optional space) from each body line.
    // No HTML-entity decoding — fenced code holds raw source already.
    private String getFencedAssertion(String tag) {
        def lines = tag.split('\n', -1)
        if (lines.size() < 2) return ''
        // Drop the opening fence line. Also drop the closing fence line if the
        // last line is one; otherwise keep everything after the opener.
        int start = 1
        int end = lines.size()
        if (lines[-1].trim().matches(/\/\/\/\s*```.*/)) end = lines.size() - 1
        StringBuilder sb = new StringBuilder()
        for (int k = start; k < end; k++) {
            if (sb.length() > 0) sb.append('\n')
            sb.append(lines[k].replaceFirst(/^\s*\/\/\/\s?/, ''))
        }
        return sb.toString()
    }

    private List getTestMethods(Map lineNumberToAssertions, String filename) {
        List testMethods = lineNumberToAssertions.collect { lineNumber, assertions ->
            Character differentiator = 'a'
            assertions.collect { assertion ->
                String suffix = (assertions.size() > 1 ? "$lineNumber$differentiator" : lineNumber)
                differentiator++
                getTestMethodCodeForAssertion(suffix, assertion, basename(filename))
            }
        }.flatten()

        return testMethods
    }

    private String basename(String fullPath) {
        def path = new File(fullPath)
        def fullName = path.name
        fullName.substring(0, fullName.lastIndexOf("."))
    }

    private String getTestMethodCodeForAssertion(String suffix, String assertion, String basename) {
        return """
            public void testAssertionFrom${basename}Line$suffix() {
                byte[] bytes = [ ${(assertion.getBytes("UTF-8") as List).join(", ")} ] as byte[]
                Eval.me(new String(bytes, "UTF-8"))
            }
        """
    }

    private String getTestCode(String testName, List testMethods) {
        return """
            class $testName extends junit.framework.TestCase {
                """+testMethods.join("\n")+"""
            }
        """
    }

    private Class createClass(String testCode) {
        return new GroovyClassLoader().parseClass(testCode)
    }
}
