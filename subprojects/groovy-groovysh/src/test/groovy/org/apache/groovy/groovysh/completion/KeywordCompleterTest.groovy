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
package org.apache.groovy.groovysh.completion

import groovy.test.GroovyTestCase

import static org.apache.groovy.groovysh.completion.TokenUtilTest.tokenList

class KeywordCompleterTest extends GroovyTestCase {

    void testKeywordModifier() {
        KeywordSyntaxCompleter completer = new KeywordSyntaxCompleter()
        def candidates = []
        String buffer = 'pub'
        assert completer.complete(tokenList(buffer), candidates)
        assert ['public '] == candidates
    }

    void testInfixKeywordNotCompleted() {
        // extends, implements, instanceof are not to kbe completed when at start of line
        KeywordSyntaxCompleter completer = new KeywordSyntaxCompleter()
        def candidates = []
        String buffer = 'ext'
        assert !completer.complete(tokenList(buffer), candidates)
        buffer = 'imple'
        assert !completer.complete(tokenList(buffer), candidates)
        buffer = 'inst'
        assert !completer.complete(tokenList(buffer), candidates)
    }

    void testKeywordModifierSecond() {
        KeywordSyntaxCompleter completer = new KeywordSyntaxCompleter()
        def candidates = []
        String buffer = 'public sta'
        assert completer.complete(tokenList(buffer), candidates)
        assert ['static '] == candidates
        candidates = []
        buffer = 'public swi' // don't suggest switch keyword here
        assert completer.complete(tokenList(buffer), candidates)
        assert ['switch ('] == candidates
    }

    void testKeywordModifierThird() {
        KeywordSyntaxCompleter completer = new KeywordSyntaxCompleter()
        def candidates = []
        String buffer = 'public static inter'
        assert completer.complete(tokenList(buffer), candidates)
        assert ['interface '] == candidates
    }

    void testKeywordModifierFor() {
        KeywordSyntaxCompleter completer = new KeywordSyntaxCompleter()
        def candidates = []
        String buffer = 'fo'
        assert completer.complete(tokenList(buffer), candidates)
        assert ['for ('] == candidates
    }
}
