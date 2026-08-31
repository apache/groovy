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

import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.apache.groovy.parser.antlr4.GroovyLangParser;

/**
 * Manage ATN for parser to avoid memory leak
 */
public class ParserAtnManager extends AtnManager {
    public static final ParserAtnManager INSTANCE = new ParserAtnManager();

    @Override
    protected AtnWrapper createAtnWrapper() {
        // The wrapper must own a private ATN: its DFA/context caches are released by the
        // wrapper becoming unreachable, which the generated parser's static ATN never is.
        // When dropping is disabled the static ATN is used directly so its caches persist
        // for the life of the class (the documented "never drop" behaviour).
        return new AtnWrapper(droppingEnabled()
                ? new ATNDeserializer().deserialize(GroovyLangParser._serializedATN)
                : GroovyLangParser._ATN);
    }

    @Override
    protected boolean shouldClearDfaCache() {
        return true;
    }

    private ParserAtnManager() {}
}
