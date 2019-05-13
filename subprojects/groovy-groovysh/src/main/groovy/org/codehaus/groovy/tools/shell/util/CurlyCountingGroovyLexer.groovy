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
package org.codehaus.groovy.tools.shell.util

import org.codehaus.groovy.antlr.GroovySourceToken
import org.codehaus.groovy.antlr.SourceBuffer
import org.codehaus.groovy.antlr.UnicodeEscapingReader
import org.codehaus.groovy.antlr.parser.GroovyLexer
import org.codehaus.groovy.antlr.parser.GroovyTokenTypes

/**
 * patching GroovyLexer to get access to Paren level
 */
@Deprecated
class CurlyCountingGroovyLexer extends GroovyLexer {

    private endReached = false

    protected CurlyCountingGroovyLexer(Reader reader) {
        super(reader)
    }

    static CurlyCountingGroovyLexer createGroovyLexer(String src) {
        Reader unicodeReader = new UnicodeEscapingReader(new StringReader(src.toString()), new SourceBuffer())
        CurlyCountingGroovyLexer lexer = new CurlyCountingGroovyLexer(unicodeReader)
        unicodeReader.setLexer(lexer)
        return lexer
    }

    int getParenLevel() {
        return parenLevelStack.size()
    }

    // called by nextToken()
    @Override
    void uponEOF() {
        super.uponEOF()
        endReached = true
    }

    List<GroovySourceToken> toList() {
        List<GroovySourceToken> tokens = []
        GroovySourceToken token
        while (! endReached) {
            token = nextToken() as GroovySourceToken
            tokens.add(token)
            if (token.type == GroovyTokenTypes.EOF) {
                break
            }
        }
        return tokens
    }
}
