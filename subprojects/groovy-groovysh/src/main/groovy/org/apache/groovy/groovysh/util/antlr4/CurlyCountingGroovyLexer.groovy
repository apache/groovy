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
package org.apache.groovy.groovysh.util.antlr4

import groovy.transform.CompileStatic
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.Token
import org.apache.groovy.parser.antlr4.GroovyLangLexer

/**
 * patching GroovyLangLexer to get access to Paren level
 */
@CompileStatic
class CurlyCountingGroovyLexer extends GroovyLangLexer {

    protected CurlyCountingGroovyLexer(Reader reader) {
        super(CharStreams.fromReader(reader))
    }

    static CurlyCountingGroovyLexer createGroovyLexer(String src) {
        new CurlyCountingGroovyLexer(new StringReader(src))
    }

    private int curlyLevel
    private List<Token> tokens = null

    int getCurlyLevel() {
        return curlyLevel
    }

    int countCurlyLevel() {
        CommonTokenStream tokenStream = new CommonTokenStream(this)
        try {
            tokenStream.fill()
            tokens = tokenStream.tokens
        } catch (ignore) {
        }

        return curlyLevel
    }

    List<Token> toList() {
        if (tokens == null) countCurlyLevel()
        tokens
    }

    @Override
    protected void enterParenCallback(String text) {
        if ("{" == text) {
            curlyLevel++
        }
    }

    @Override
    protected void exitParenCallback(String text) {
        if ("}" == text) {
            curlyLevel--
        }
    }
}
