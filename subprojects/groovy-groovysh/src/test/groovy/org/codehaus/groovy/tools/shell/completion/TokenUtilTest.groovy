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
package org.codehaus.groovy.tools.shell.completion

import org.codehaus.groovy.antlr.GroovySourceToken
import org.codehaus.groovy.tools.shell.util.CurlyCountingGroovyLexer

/**
 * Defines method tokenList for other Unit tests and tests it
 */
class TokenUtilTest extends GroovyTestCase {

    /**
     * return token list without EOF
     */
    static List<GroovySourceToken> tokenList(String src) {
        CurlyCountingGroovyLexer lexer = CurlyCountingGroovyLexer.createGroovyLexer(src)
        def result = lexer.toList()
        if (result && result.size() > 1) {
           return result[0..-2]
        }
        return []
    }

    void testTokenList() {
        assert [] == tokenList('')
        assert 'foo' == tokenList('foo')[0].text
        assert ['foo'] == tokenList('foo')*.text
        assert ['foo', '{', 'bar'] == tokenList('foo{bar')*.text
        assert ['1', '..', '2'] == tokenList('1..2')*.text
    }

    static tokensString(List<GroovySourceToken> tokens) {
        if (tokens == null || tokens.size() == 0) {
            return null
        }
        return tokens*.text.join()
    }

}
