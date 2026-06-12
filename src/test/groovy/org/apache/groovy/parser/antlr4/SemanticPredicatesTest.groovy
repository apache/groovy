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
package org.apache.groovy.parser.antlr4

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.TokenStream
import org.junit.jupiter.api.Test

final class SemanticPredicatesTest {

    @Test
    void 'following arguments or closure detects postfix argument tails'() {
        assert SemanticPredicates.isFollowingArgumentsOrClosure(parseExpression('call()'))
        assert SemanticPredicates.isFollowingArgumentsOrClosure(parseExpression('call { 1 }'))
        assert !SemanticPredicates.isFollowingArgumentsOrClosure(parseExpression('call'))
        assert !SemanticPredicates.isFollowingArgumentsOrClosure(parseExpression('call[0]'))
    }

    @Test
    void 'invalid method declaration only rejects call-like script statements'() {
        assert SemanticPredicates.isInvalidMethodDeclaration(tokens('foo()'))
        assert SemanticPredicates.isInvalidMethodDeclaration(tokens('Foo()'))
        assert SemanticPredicates.isInvalidMethodDeclaration(tokens('"foo"()'))
        assert SemanticPredicates.isInvalidMethodDeclaration(tokens('yield()'))

        assert !SemanticPredicates.isInvalidMethodDeclaration(tokens('int foo() {}'))
        assert !SemanticPredicates.isInvalidMethodDeclaration(tokens('foo + 1'))
    }

    @Test
    void 'invalid local variable declaration distinguishes declarations calls and annotated loops'() {
        assert !SemanticPredicates.isInvalidLocalVariableDeclaration(tokens('String name'))
        assert !SemanticPredicates.isInvalidLocalVariableDeclaration(tokens('java.lang.String name'))
        assert !SemanticPredicates.isInvalidLocalVariableDeclaration(tokens('java.util.List<String> names'))
        assert !SemanticPredicates.isInvalidLocalVariableDeclaration(tokens('@Deprecated String name'))

        assert SemanticPredicates.isInvalidLocalVariableDeclaration(tokens('foo bar'))
        assert SemanticPredicates.isInvalidLocalVariableDeclaration(tokens('foo.bar baz'))
        assert SemanticPredicates.isInvalidLocalVariableDeclaration(tokens('@Deprecated for (item in items) { }'))
    }

    @Test
    void 'annotated loop statement skips qualified annotations and nested arguments'() {
        assert SemanticPredicates.isAnnotatedLoopStatement(tokens('@java.lang.Deprecated while (flag) { break }'))
        assert SemanticPredicates.isAnnotatedLoopStatement(tokens('@Anno(value = ((1 + 2))) do { work() } while (ready)'))
        assert !SemanticPredicates.isAnnotatedLoopStatement(tokens('@java.lang.Deprecated String name'))
    }

    private static GroovyParser.ExpressionContext parseExpression(String source) {
        GroovyLangParser parser = parser(source)
        GroovyParser.ExpressionContext expression = parser.expression()

        assert 0 == parser.numberOfSyntaxErrors: "Failed to parse `${source}`"
        assert Token.EOF == parser.currentToken.type: "Unconsumed tokens remain after parsing `${source}`"

        return expression
    }

    private static TokenStream tokens(String source) {
        CommonTokenStream tokenStream = new CommonTokenStream(new GroovyLangLexer(CharStreams.fromString(source)))
        tokenStream.fill()
        return tokenStream
    }

    private static GroovyLangParser parser(String source) {
        return new GroovyLangParser(tokens(source))
    }
}
