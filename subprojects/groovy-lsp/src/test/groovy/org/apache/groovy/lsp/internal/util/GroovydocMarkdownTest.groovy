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
package org.apache.groovy.lsp.internal.util

import org.junit.jupiter.api.Test

final class GroovydocMarkdownTest {

    @Test
    void formatsTagsAndHtml() {
        assert GroovydocMarkdown.format(null) == ''
        assert GroovydocMarkdown.format('  ') == ''
        assert GroovydocMarkdown.of(null) == ''
        def markdown = GroovydocMarkdown.format('''\
            /**
             * Says {@code hello} to {@link demo.Person#name}.
             * <p>Uses <b>bold</b> and <i>italic</i>.
             * @param name the person
             * @return greeting
             * @throws IllegalArgumentException when blank
             * @see demo.Other
             */
            '''.stripIndent())
        assert markdown.contains('`hello`')
        assert markdown.contains('`demo.Person.name`') || markdown.contains('`Person.name`')
        assert markdown.contains('**bold**')
        assert markdown.contains('*italic*')
        assert markdown.contains('**@param** `name` the person')
        assert markdown.contains('**@return** greeting')
        assert markdown.contains('**@throws** `IllegalArgumentException` when blank')
        assert markdown.contains('**@see** demo.Other')
        assert GroovydocMarkdown.format('{@linkplain String#isBlank()}').contains('`String.isBlank`')
        assert GroovydocMarkdown.format('{@literal *}').contains('*')
        assert GroovydocMarkdown.format('plain <unknown>tag</unknown>').contains('plain &lt;unknown&gt;tag&lt;/unknown&gt;')
        assert !GroovydocMarkdown.format('plain <unknown>tag</unknown>').contains('plain tag<')
    }

    @Test
    void formatsBareTagsAndUnclosedMarkup() {
        assert GroovydocMarkdown.format('{@link java.lang.String#foo bar}').contains('java.lang.String.foo')
        assert GroovydocMarkdown.format('{@link java.lang.String}').contains('`String`')
        assert GroovydocMarkdown.format('keep <unclosed').contains('&lt;unclosed')
        assert GroovydocMarkdown.format('@exception Only').contains('**@exception** `Only`')
        assert GroovydocMarkdown.format('@param name').contains('**@param** `name`')
    }

    @Test
    void keepsGenericsAndComparisons() {
        assert GroovydocMarkdown.format('List<String>') == 'List&lt;String&gt;'
        def coded = GroovydocMarkdown.format('{@code List<String>}')
        assert coded.contains('`List<String>`')
        assert !coded.contains('&lt;')
        assert GroovydocMarkdown.format('{@literal List<String>}').contains('List&lt;String&gt;')
        def compared = GroovydocMarkdown.format('x < y && y > z')
        assert compared.contains('x &lt; y && y &gt; z')
        assert compared.contains('y')
    }

    @Test
    void stripsKnownHtmlAndKeepsContents() {
        def pre = GroovydocMarkdown.format('<pre>List<String></pre>')
        assert pre.contains('List&lt;String&gt;')
        assert !pre.contains('<pre>')
        def list = GroovydocMarkdown.format('<ul><li>item</li></ul>')
        assert list.contains('item')
        assert !list.contains('<li>')
        assert GroovydocMarkdown.format('<b>bold</b>').contains('**bold**')
        assert GroovydocMarkdown.format('<code>List<String></code>').contains('`List<String>`')
        assert GroovydocMarkdown.format('a<br>b').contains('a\nb')
        assert GroovydocMarkdown.format('a<br/>b').contains('a\nb')
        assert GroovydocMarkdown.format('a<br />b').contains('a\nb')
        assert GroovydocMarkdown.format('<p class="x">body</p>').contains('body')
        assert GroovydocMarkdown.format('<P>Upper</P>').contains('Upper')
    }

    @Test
    void keepsTypeArgumentsThatShareHtmlNames() {
        assert GroovydocMarkdown.format('List<U>') == 'List&lt;U&gt;'
        assert GroovydocMarkdown.format('List<P>') == 'List&lt;P&gt;'
        assert GroovydocMarkdown.format('List<B>') == 'List&lt;B&gt;'
        assert GroovydocMarkdown.format('Tuple2<A, B>') == 'Tuple2&lt;A, B&gt;'
        assert GroovydocMarkdown.format('{@code List<P>}') == '`List<P>`'
        assert GroovydocMarkdown.format('<code>List<P></code>') == '`List<P>`'
        assert GroovydocMarkdown.format('<code><b>x</b></code>') == '`<b>x</b>`'
        def param = GroovydocMarkdown.format('@param <U> the type')
        assert param.contains('&lt;U&gt;')
        assert param.contains('the type')
        assert !param.contains('**@param** `the`')
    }

    @Test
    void privateConstructorCanBeInvoked() {
        [GroovydocMarkdown].each { Class type ->
            def ctor = type.getDeclaredConstructor()
            ctor.accessible = true
            ctor.newInstance()
        }
    }

}
