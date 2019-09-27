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
package org.apache.groovy.groovysh.util

import groovy.transform.CompileStatic
import org.antlr.v4.runtime.CommonTokenStream
import org.apache.groovy.parser.antlr4.GroovyLangLexer

/**
 * patching GroovyLangLexer to get access to Paren level
 */
@CompileStatic
class CurlyLevelCountingGroovyLexer extends GroovyLangLexer {
    static CurlyLevelCountingGroovyLexer createGroovyLexer(String text) {
        return new CurlyLevelCountingGroovyLexer(new StringReader(text))
    }

    CurlyLevelCountingGroovyLexer(Reader reader) throws IOException {
        super(reader)
    }

    private int curlyLevel

    int getCurlyLevel() {
        return curlyLevel
    }

    int countCurlyLevel() {
        CommonTokenStream tokenStream = new CommonTokenStream(this)
        try {
            tokenStream.fill()
        } catch (e) {
        }

        return curlyLevel
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
