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
package org.codehaus.groovy.ant

import org.junit.jupiter.api.Test

/**
 * Exercises the {@code preLanguage} post-pass on the Ant Groovydoc task —
 * bare {@code <pre>} opening tags get a {@code class="language-xxx"}
 * attribute injected, while tags that already carry any attribute are
 * left alone.
 */
class GroovydocPreLanguageTest {

    @Test
    void testBarePreIsRewritten() {
        String input = "<p>hi</p><pre>assert 1 == 1</pre>"
        String out = Groovydoc.rewritePreTags(input, 'groovy')
        assert out == "<p>hi</p><pre class=\"language-groovy\">assert 1 == 1</pre>"
    }

    @Test
    void testPreWithWhitespaceBeforeCloseIsRewritten() {
        String input = "<pre   >code</pre>"
        String out = Groovydoc.rewritePreTags(input, 'groovy')
        assert out.startsWith('<pre class="language-groovy">')
    }

    @Test
    void testPreWithExistingClassIsNotRewritten() {
        String input = '<pre class="groovyTestCase">assert 2 == 2</pre>'
        String out = Groovydoc.rewritePreTags(input, 'groovy')
        assert out == input
    }

    @Test
    void testPreWithExistingLanguageClassIsNotRewritten() {
        String input = '<pre class="language-sql">select 1</pre>'
        String out = Groovydoc.rewritePreTags(input, 'groovy')
        assert out == input
    }

    @Test
    void testPreWithIdAttributeIsNotRewritten() {
        // A `<pre>` with any attribute is considered deliberate and left alone.
        String input = '<pre id="sample">x</pre>'
        String out = Groovydoc.rewritePreTags(input, 'groovy')
        assert out == input
    }

    @Test
    void testMultipleBlocksInSameFile() {
        String input = '<pre>first</pre> and <pre class="groovyTestCase">second</pre> and <pre>third</pre>'
        String out = Groovydoc.rewritePreTags(input, 'groovy')
        int count = (out =~ /<pre class="language-groovy">/).count
        assert count == 2
        assert out.contains('<pre class="groovyTestCase">second</pre>')
    }
}
