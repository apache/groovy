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

import org.apache.groovy.parser.antlr4.GroovyLexer
import org.junit.jupiter.api.Test

final class GroovyKeywordsTest {

    @Test
    void keywordsComeFromTheLexerVocabulary() {
        def words = GroovyKeywords.KEYWORDS
        assert words.containsAll(['as', 'def', 'trait', 'record', 'sealed', 'permits',
                'yield', 'var', 'val', 'async', 'await', 'defer', 'threadsafe',
                'non-sealed', 'module', 'class', 'interface'])
        assert words.containsAll(['abstract', 'public', 'static', 'boolean', 'int', 'void',
                'true', 'false', 'null'])
        assert words == words.toSorted()
        assert words.size() == words.toSet().size()
        assert !words.contains('+')
        assert !words.contains('==')
        assert !words.contains('!in')
        assert GroovyLexer.VOCABULARY.getLiteralName(GroovyLexer.AS).contains('as')
    }

    @Test
    void modifiersAreLexerSpellingsAndASubsetOfKeywords() {
        assert GroovyKeywords.MODIFIERS.containsAll(['abstract', 'public', 'static', 'final'])
        assert GroovyKeywords.KEYWORDS.containsAll(GroovyKeywords.MODIFIERS)
        assert GroovyKeywords.MODIFIERS == GroovyKeywords.MODIFIERS.toSorted()
    }

    @Test
    void wordKeywordsRejectPunctuation() {
        def word = GroovyKeywords.getDeclaredMethod('isWordKeyword', String)
        word.accessible = true
        assert word.invoke(null, 'a!') == false
        assert word.invoke(null, 'non-sealed') == true
    }
}
