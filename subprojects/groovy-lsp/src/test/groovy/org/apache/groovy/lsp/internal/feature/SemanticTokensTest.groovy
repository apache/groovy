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
}
