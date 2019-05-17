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

import groovy.test.GroovyTestCase

class SimpleGroovyClassDocTests extends GroovyTestCase {

    void testReplaceTags_link() {
        def text = 'Use the {@link #getComponentAt(int, int) getComponentAt} method.'

        def result = classDoc.replaceTags(text)

        assert result == "Use the <a href='#getComponentAt(int, int)'>getComponentAt</a> method."
    }

    void testReplaceTags_literal() {
        def text = 'text with literal {@literal A<B>C} tag'

        def result = classDoc.replaceTags(text)

        assert result == 'text with literal A&lt;B&gt;C tag'
    }

    void testReplaceTags_code() {
        def text = 'text with code {@code A<B>C} tag'

        def result = classDoc.replaceTags(text)

        assert result == 'text with code <CODE>A&lt;B&gt;C</CODE> tag'
    }

    void testEncodeAngleBracketsInTagBody() {
        def text = 'text with <tag1> outside and {@code <tag2> inside} a @code tag'
        def regex = SimpleGroovyClassDoc.CODE_REGEX
        def encodedText = SimpleGroovyClassDoc.encodeAngleBracketsInTagBody(text, regex)
        assert encodedText == 'text with <tag1> outside and {@code &lt;tag2&gt; inside} a @code tag'
    }

    void testEncodeAngleBracketsInTagBodyWithDollar() {
        def text = 'text with dollar {@code $foo.bar} and dollar with less than {@code $foo < $bar}'
        def regex = SimpleGroovyClassDoc.CODE_REGEX
        def encodedText = SimpleGroovyClassDoc.encodeAngleBracketsInTagBody(text, regex)
        assert encodedText == 'text with dollar {@code $foo.bar} and dollar with less than {@code $foo &lt; $bar}'
    }

    void testEncodeAngleBracketsInTagBodyLeavesSpecialChars() {
        def text = 'text with illegal group ref {@code $3 \\$}'
        def regex = SimpleGroovyClassDoc.CODE_REGEX
        def encodedText = SimpleGroovyClassDoc.encodeAngleBracketsInTagBody(text, regex)
        assert encodedText == text
    }

    void testEncodeAngleBrackets() {
        def text = 'text with <tag1> and <tag2>'

        def encodedText = SimpleGroovyClassDoc.encodeAngleBrackets(text)

        assert encodedText == 'text with &lt;tag1&gt; and &lt;tag2&gt;'
    }

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
