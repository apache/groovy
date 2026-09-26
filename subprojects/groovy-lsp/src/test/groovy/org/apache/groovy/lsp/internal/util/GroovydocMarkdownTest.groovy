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
        assert GroovydocMarkdown.format('plain <unknown>tag</unknown>').contains('plain tag')
    }

    @Test
    void formatsBareTagsAndUnclosedMarkup() {
        assert GroovydocMarkdown.format('{@link java.lang.String#foo bar}').contains('java.lang.String.foo')
        assert GroovydocMarkdown.format('{@link java.lang.String}').contains('`String`')
        assert GroovydocMarkdown.format('keep <unclosed').contains('<unclosed')
        assert GroovydocMarkdown.format('@exception Only').contains('**@exception** `Only`')
        assert GroovydocMarkdown.format('@param name').contains('**@param** `name`')
    }
}
