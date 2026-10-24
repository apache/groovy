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
package org.apache.groovy.lsp.internal.feature

import org.apache.groovy.lsp.internal.compile.CompiledDocument
import org.apache.groovy.lsp.internal.position.PositionEncoding
import org.junit.jupiter.api.Test

import java.net.URI

final class SemanticTokensTest {

    private static final PositionEncoding UTF16 = PositionEncoding.UTF16

    @Test
    void absoluteSpansEncodeAsDeltasAndSkipARegression() {
        def service = new SemanticTokensService()
        assert service.tokenSpans(null, UTF16).isEmpty()
        assert SemanticTokensService.encode(null).data.isEmpty()
        assert SemanticTokensService.encode([]).data.isEmpty()

        def compiled = new CompiledDocument(URI.create('file:///T.groovy'), 1, 'class C {}\n', null, null, null)
        def spans = service.tokenSpans(compiled, UTF16)
        assert spans
        assert spans.every { it.line() >= 0 && it.character() >= 0 && it.length() > 0 }
        def encoded = SemanticTokensService.encode(spans)
        assert encoded.data.size() == spans.size() * 5
        assert encoded.data[0] == spans[0].line()
        assert encoded.data[1] == spans[0].character()
        assert encoded.data[2] == spans[0].length()

        def backwards = SemanticTokensService.encode([
                new SemanticTokensService.TokenSpan(1, 4, 1, 8, 0),
                new SemanticTokensService.TokenSpan(0, 0, 3, 8, 0),
                new SemanticTokensService.TokenSpan(1, 1, 1, 8, 0),
                new SemanticTokensService.TokenSpan(2, 0, 2, 9, 0)
        ])
        assert backwards.data == [1, 4, 1, 8, 0, 1, 0, 2, 9, 0]
    }

    @Test
    void lexerKeywordAfterASupplementaryCharacterKeepsItsColumn() {
        def service = new SemanticTokensService()
        def text = 'def s = "\uD83D\uDE00"; def n = 1\n'
        def compiled = new CompiledDocument(URI.create('file:///Emoji.groovy'), 1, text, null, null, null)
        def spans = service.tokenSpans(compiled, UTF16)
        int keyword = SemanticTokensService.TOKEN_TYPES.indexOf('keyword')
        int at = text.indexOf('; def') + 2
        assert spans.any { it.line() == 0 && it.character() == at && it.length() == 3 && it.type() == keyword }
    }

    @Test
    void lexicalTokenSpansStayLenientAndPaintIdentifiersAsVariable() {
        assert SemanticTokensService.lexicalTokenSpans(null, UTF16).isEmpty()
        assert SemanticTokensService.lexicalTokenSpans('', UTF16).isEmpty()
        def incomplete = SemanticTokensService.lexicalTokenSpans('def x =', UTF16)
        int keyword = SemanticTokensService.TOKEN_TYPES.indexOf('keyword')
        int variable = SemanticTokensService.TOKEN_TYPES.indexOf('variable')
        assert incomplete.any { it.type() == keyword }
        assert incomplete.any { it.type() == variable }
        def brokenNumeric = SemanticTokensService.lexicalTokenSpans('1_', UTF16)
        assert brokenNumeric.any { it.length() > 0 }
        def mixed = SemanticTokensService.lexicalTokenSpans('def n = 1 // c\ndef s = "x"\ndef t = /z/\n!in\n@Foo x, y;\n', UTF16)
        int string = SemanticTokensService.TOKEN_TYPES.indexOf('string')
        int number = SemanticTokensService.TOKEN_TYPES.indexOf('number')
        int comment = SemanticTokensService.TOKEN_TYPES.indexOf('comment')
        int decorator = SemanticTokensService.TOKEN_TYPES.indexOf('decorator')
        int operator = SemanticTokensService.TOKEN_TYPES.indexOf('operator')
        assert mixed.any { it.type() == string }
        assert mixed.any { it.type() == number }
        assert mixed.any { it.type() == comment }
        assert mixed.any { it.type() == decorator }
        assert mixed.any { it.type() == operator }
        def block = SemanticTokensService.lexicalTokenSpans('/* a\nb */\n', UTF16)
        assert block.any { it.type() == comment }
        assert SemanticTokensService.lexicalTokenSpans('def x', null, UTF16).isEmpty()
        assert SemanticTokensService.TOKEN_TYPES.indexOf('operator') == 12
        assert SemanticTokensService.TOKEN_TYPES.indexOf('decorator') == 13
        def compiled = new CompiledDocument(URI.create('file:///C.groovy'), 1, 'class C {}\n',
                null, null, null)
        // no AST: C is a lexical variable. With a module, declaration overlay wins.
        def bare = new SemanticTokensService().tokenSpans(compiled, UTF16)
        assert bare.any { it.type() == variable }
    }

    @Test
    void stringModifiersFollowTheOpeningDelimiterAndSkipTheGStringDollar() {
        int string = SemanticTokensService.TOKEN_TYPES.indexOf('string')
        assert SemanticTokensService.TOKEN_TYPES.indexOf('delimiter') == 14
        def brackets = SemanticTokensService.lexicalTokenSpans('def c = { x[0] }\n', UTF16)
        int delimiter = 14
        assert brackets.findAll { it.type() == delimiter }.size() == 4
        def safe = SemanticTokensService.lexicalTokenSpans('def x = a?[0]\n', UTF16)
        assert safe.any { it.type() == delimiter && it.length() == 2 }
        def quoted = SemanticTokensService.lexicalTokenSpans("def s = '{'\n", UTF16)
        assert quoted.every { it.type() != delimiter }
        assert SemanticTokensService.TOKEN_MODIFIERS.indexOf('slashy') == 3
        assert SemanticTokensService.TOKEN_MODIFIERS.indexOf('singleQuoted') == 4

        def single = stringSpans("def s = 'ab'\n", string)
        assert single.every { it.modifiers() == 16 }
        def quotes = stringSpans('def s = "ab"\n', string)
        assert quotes.every { it.modifiers() == 0 }
        def slashy = stringSpans('def s = /ab/\n', string)
        assert slashy.every { it.modifiers() == 8 }
        def dollarSlashy = stringSpans('def s = $/ab/$\n', string)
        assert dollarSlashy.every { it.modifiers() == 8 }

        def gstring = '"${x}${y}"'
        def parts = stringSpans(gstring, string)
        int secondDollar = gstring.lastIndexOf('$')
        assert parts.every { it.length() > 0 && it.modifiers() == 0 }
        assert !parts.any { it.line() == 0 && it.character() <= secondDollar && secondDollar < it.character() + it.length() }

        def text = 'def n = 1\n'
        def service = new SemanticTokensService()
        def overlay = new CompiledDocument(URI.create('file:///N.groovy'), 1, text, null, null, null)
        assert service.semanticTokens(text, overlay, UTF16).data
        assert service.semanticTokens('def n = 2\n', overlay, UTF16).data
        assert service.semanticTokens(text, null, UTF16).data
        assert service.semanticTokens(null, overlay, UTF16).data.isEmpty()
    }

    private static List stringSpans(String text, int string) {
        SemanticTokensService.lexicalTokenSpans(text, UTF16).findAll { it.type() == string }
    }
}
