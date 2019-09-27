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
package org.apache.groovy.groovysh.completion

import groovy.test.GroovyTestCase

import static org.apache.groovy.groovysh.completion.TokenUtilTest.tokenList
import static org.apache.groovy.groovysh.completion.TokenUtilTest.tokensString
import static org.apache.groovy.groovysh.completion.antlr4.ReflectionCompleter.getFieldnameForAccessor
import static org.apache.groovy.groovysh.completion.antlr4.ReflectionCompleter.getInvokerTokens
import static org.apache.groovy.groovysh.completion.antlr4.ReflectionCompleter.tokenListToEvalString

class InvokerParsingTest extends GroovyTestCase {

    void testTokenListToEvalString() {
        assert '' == tokenListToEvalString(tokenList(''))
        assert '1' == tokenListToEvalString(tokenList('1'))
        assert '1.' == tokenListToEvalString(tokenList('1.'))
        assert 'foo' == tokenListToEvalString(tokenList('foo'))
        assert '\'\'foo\'\'' == tokenListToEvalString(tokenList("'foo'"))
    }

    // FIXME re-enable commented out lines
    void testGetInvokerTokens() {
        // must make sure no token list is returned that could be evaluated with side effects.
        assert null == tokensString(getInvokerTokens(tokenList('')))
        assert 'foo' == tokensString(getInvokerTokens(tokenList('foo')))
        assert 'bar.foo' == tokensString(getInvokerTokens(tokenList('bar.foo')))
        assert 'bar.&foo' == tokensString(getInvokerTokens(tokenList('bar.&foo')))
        // literal (simple join of token text forgets hyphens)
        assert '"foo"' == tokensString(getInvokerTokens(tokenList('"foo"')))
//        assert '1' == tokensString(getInvokerTokens(tokenList('1')))
        // operator
        assert '"foo"' == tokensString(getInvokerTokens(tokenList('1+"foo"')))
        assert 'foo' == tokensString(getInvokerTokens(tokenList('bar == foo')))
        assert 'foo' == tokensString(getInvokerTokens(tokenList('bar = foo')))
        assert 'foo' == tokensString(getInvokerTokens(tokenList('1+foo')))
        // begin
        assert 'foo' == tokensString(getInvokerTokens(tokenList(';foo')))
        assert 'foo' == tokensString(getInvokerTokens(tokenList('(foo')))
//        assert '[foo[][2]].bar' == tokensString(getInvokerTokens(tokenList('[[foo[][2]].bar')))
        assert 'foo' == tokensString(getInvokerTokens(tokenList('${foo')))
        assert 'foo' == tokensString(getInvokerTokens(tokenList('"$foo')))
        assert 'foo' == tokensString(getInvokerTokens(tokenList('[1,foo')))
        assert 'foo' == tokensString(getInvokerTokens(tokenList('[1: foo')))
        // Collections
//        assert '[1+2]' == tokensString(getInvokerTokens(tokenList('[1+2]')))
//        assert '[1..2]' == tokensString(getInvokerTokens(tokenList('[1..2]')))
//        assert '[1,2]' == tokensString(getInvokerTokens(tokenList('[1, 2]')))
//        assert '[1:2]' == tokensString(getInvokerTokens(tokenList('[1: 2]')))
        // allowed Parens
        assert '((Foo)foo).' == tokensString(getInvokerTokens(tokenList('((Foo) foo).')))
//        assert '((1+2>4)==(a&&b)).' == tokensString(getInvokerTokens(tokenList('((1 + 2 > 4) == (a && b)).')))
        // not allowed
        assert null == tokensString(getInvokerTokens(tokenList('foo()')))
        assert null == tokensString(getInvokerTokens(tokenList('foo each')))
        assert null == tokensString(getInvokerTokens(tokenList('New Foo().')))
        assert null == tokensString(getInvokerTokens(tokenList('foo.each bar')))
        assert null == tokensString(getInvokerTokens(tokenList('foo++')))
    }

    void testGetFieldnameForAccessor() {
        assert 'foo' == getFieldnameForAccessor('getFoo', 0)
        assert 'foo' == getFieldnameForAccessor('setFoo', 1)
        assert 'foo' == getFieldnameForAccessor('isFoo', 0)

        assert null == getFieldnameForAccessor('getFoo', 1)
        assert null == getFieldnameForAccessor('setFoo', 0)
        assert null == getFieldnameForAccessor('isFoo', 1)
    }
}