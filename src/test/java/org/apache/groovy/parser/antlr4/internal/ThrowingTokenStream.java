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
package org.apache.groovy.parser.antlr4.internal;

import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.Interval;

/**
 * Token stream that fails on every access — used to exercise defensive
 * catch paths in {@link MissingDelimiterDiagnostic}.
 */
final class ThrowingTokenStream implements TokenStream {
    @Override
    public Token LT(int k) {
        throw new IndexOutOfBoundsException("test");
    }

    @Override
    public Token get(int index) {
        throw new IndexOutOfBoundsException("test");
    }

    @Override
    public TokenSource getTokenSource() {
        return null;
    }

    @Override
    public String getText(Interval interval) {
        return "";
    }

    @Override
    public String getText() {
        return "";
    }

    @Override
    public String getText(RuleContext ctx) {
        return "";
    }

    @Override
    public String getText(Object start, Object stop) {
        return "";
    }

    @Override
    public void consume() {
    }

    @Override
    public int LA(int i) {
        return Token.EOF;
    }

    @Override
    public int mark() {
        return 0;
    }

    @Override
    public void release(int marker) {
    }

    @Override
    public int index() {
        return 0;
    }

    @Override
    public void seek(int index) {
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public String getSourceName() {
        return "throwing";
    }
}
