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
import org.antlr.v4.runtime.atn.StarLoopEntryState;
import org.apache.groovy.parser.antlr4.GroovyLangParser;

import static org.apache.groovy.parser.antlr4.GroovyParser.RULE_nls;

/**
 * Manage ATN for parser to avoid memory leak
 */
public class ParserAtnManager extends AtnManager {
    private final AtnWrapper parserAtnWrapper = new AtnManager.AtnWrapper(GroovyLangParser._ATN);
    public static final ParserAtnManager INSTANCE = new ParserAtnManager();

    static {
        GroovyLangParser._ATN.states.stream()
                .filter(e -> (e instanceof StarLoopEntryState) && (RULE_nls == ((StarLoopEntryState) e).ruleIndex))
                .forEach(e -> ((StarLoopEntryState) e).sll = true);

    }

    @Override
    public ATN getATN() {
        return parserAtnWrapper.checkAndClear();
    }

    @Override
    protected boolean shouldClearDfaCache() {
        return true;
    }

    private ParserAtnManager() {}
}
