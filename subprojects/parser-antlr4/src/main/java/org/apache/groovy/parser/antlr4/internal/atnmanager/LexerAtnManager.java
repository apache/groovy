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
package org.apache.groovy.parser.antlr4.internal.atnmanager;

import org.antlr.v4.runtime.atn.ATN;
import org.apache.groovy.parser.antlr4.GroovyLangLexer;
import org.apache.groovy.util.SystemUtil;

/**
 * Manage ATN for lexer to avoid memory leak
 */
public class LexerAtnManager extends AtnManager {
    private static final String GROOVY_CLEAR_LEXER_DFA_CACHE = "groovy.clear.lexer.dfa.cache";
    private static final boolean TO_CLEAR_LEXER_DFA_CACHE;
    private final AtnWrapper lexerAtnWrapper = new AtnManager.AtnWrapper(GroovyLangLexer._ATN);
    public static final LexerAtnManager INSTANCE = new LexerAtnManager();

    static {
        TO_CLEAR_LEXER_DFA_CACHE = SystemUtil.getBooleanSafe(GROOVY_CLEAR_LEXER_DFA_CACHE);
    }

    @Override
    public ATN getATN() {
        return lexerAtnWrapper.checkAndClear();
    }

    @Override
    protected boolean shouldClearDfaCache() {
        return TO_CLEAR_LEXER_DFA_CACHE;
    }

    private LexerAtnManager() {}
}
