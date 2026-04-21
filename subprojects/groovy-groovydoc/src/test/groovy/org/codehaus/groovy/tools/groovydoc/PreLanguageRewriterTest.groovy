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

class PreLanguageRewriterTest {

    @Test
    void testBarePreIsWrappedInCodeForPrism() {
        // Prism only highlights <code> descendants of language-classed elements,
        // so we must insert a <code> wrapper around bare <pre> bodies.
        String input = "<p>hi</p><pre>assert 1 == 1</pre>"
        String out = PreLanguageRewriter.rewriteTags(input, 'groovy')
        assert out == '<p>hi</p><pre class="language-groovy"><code>assert 1 == 1</code></pre>'
    }

    @Test
    void testPreWithWhitespaceBeforeCloseIsWrappedInCode() {
        String input = "<pre   >code</pre>"
        String out = PreLanguageRewriter.rewriteTags(input, 'groovy')
        assert out == '<pre class="language-groovy"><code>code</code></pre>'
    }

    @Test
    void testPreWithExistingInnerCodeOnlyGetsClassOnPre() {
        // {@snippet} emits <pre><code class="language-xxx">...</code></pre>.
        // Don't double-wrap; just add the class to the outer <pre>.
        String input = '<pre><code class="language-groovy">assert 1 == 1</code></pre>'
        String out = PreLanguageRewriter.rewriteTags(input, 'groovy')
        assert out == '<pre class="language-groovy"><code class="language-groovy">assert 1 == 1</code></pre>'
    }

    @Test
    void testPreWithExistingClassIsNotRewritten() {
        String input = '<pre class="groovyTestCase">assert 2 == 2</pre>'
        String out = PreLanguageRewriter.rewriteTags(input, 'groovy')
        assert out == input
    }

    @Test
    void testPreWithExistingLanguageClassIsNotRewritten() {
        String input = '<pre class="language-sql">select 1</pre>'
        String out = PreLanguageRewriter.rewriteTags(input, 'groovy')
        assert out == input
    }

    @Test
    void testPreWithIdAttributeIsNotRewritten() {
        String input = '<pre id="sample">x</pre>'
        String out = PreLanguageRewriter.rewriteTags(input, 'groovy')
        assert out == input
    }

    @Test
    void testMultipleBlocksInSameFile() {
        String input = '<pre>first</pre> and <pre class="groovyTestCase">second</pre> and <pre>third</pre>'
        String out = PreLanguageRewriter.rewriteTags(input, 'groovy')
        int count = (out =~ /<pre class="language-groovy"><code>/).count
        assert count == 2
        assert out.contains('<pre class="groovyTestCase">second</pre>')
    }

    @Test
    void testEmptyPreLanguageIsNoOp() {
        String input = '<pre>x</pre>'
        assert PreLanguageRewriter.rewriteTags(input, '') == input
        assert PreLanguageRewriter.rewriteTags(input, null) == input
    }
}
