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

    @Test
    void 'identifier assign distinguishes named annotation pairs from single values'() {
        assert SemanticPredicates.isIdentifierAssign(tokens('a = 1'))
        assert SemanticPredicates.isIdentifierAssign(tokens('value = 1'))
        assert SemanticPredicates.isIdentifierAssign(tokens('class = 1'))
        assert !SemanticPredicates.isIdentifierAssign(tokens('1'))
        assert !SemanticPredicates.isIdentifierAssign(tokens('a + 1'))
        assert !SemanticPredicates.isIdentifierAssign(tokens('a'))
        assert !SemanticPredicates.isIdentifierAssign(tokens('[1]'))
        assert !SemanticPredicates.isIdentifierAssign(tokens('@Bar'))
        assert !SemanticPredicates.isIdentifierAssign(tokens(''))
        assert !SemanticPredicates.isIdentifierAssign(tokens('(a = 1)'))
    }

    @Test
    void 'identifier assign accepts every elementValuePairName token'() {
        // identifier extras + keywords, including MODULE which is not in keywords.
        [
                'a', 'Foo', 'as', 'async', 'await', 'defer', 'in', 'module', 'permits',
                'record', 'sealed', 'trait', 'val', 'var', 'yield',
                'abstract', 'assert', 'break', 'case', 'catch', 'class', 'const',
                'continue', 'def', 'default', 'do', 'else', 'enum', 'extends', 'final',
                'finally', 'for', 'goto', 'if', 'implements', 'import', 'instanceof',
                'interface', 'native', 'new', 'non-sealed', 'package', 'return',
                'static', 'strictfp', 'super', 'switch', 'synchronized', 'this',
                'throw', 'throws', 'transient', 'threadsafe', 'try', 'volatile',
                'while', 'null', 'true', 'false', 'int', 'void', 'public', 'protected',
                'private'
        ].each { name ->
            assert SemanticPredicates.isIdentifierAssign(tokens("${name} = 1")):
                    "expected ${name} = 1 to be a named annotation pair"
        }
    }

    @Test
    void 'followed by java letter in GString is a char-class check'() {
        assert SemanticPredicates.isFollowedByJavaLetterInGString(CharStreams.fromString('name'))
        assert SemanticPredicates.isFollowedByJavaLetterInGString(CharStreams.fromString('{x}'))
        assert SemanticPredicates.isFollowedByJavaLetterInGString(CharStreams.fromString('_x'))
        assert SemanticPredicates.isFollowedByJavaLetterInGString(CharStreams.fromString('Ä'))
        assert SemanticPredicates.isFollowedByJavaLetterInGString(CharStreams.fromString('\uD801\uDC28')) // Deseret small
        assert !SemanticPredicates.isFollowedByJavaLetterInGString(CharStreams.fromString('$x'))
        assert !SemanticPredicates.isFollowedByJavaLetterInGString(CharStreams.fromString('1x'))
        assert !SemanticPredicates.isFollowedByJavaLetterInGString(CharStreams.fromString(''))
        assert !SemanticPredicates.isFollowedByJavaLetterInGString(CharStreams.fromString(' '))
        assert !SemanticPredicates.isFollowedByJavaLetterInGString(CharStreams.fromString('\uDC00')) // lone low surrogate
        assert !SemanticPredicates.isFollowedByJavaLetterInGString(CharStreams.fromString('\uD801')) // high surrogate at EOF
    }

    @Test
    void 'followed by whitespaces ignores only ASCII horizontal whitespace'() {
        assert SemanticPredicates.isFollowedByWhiteSpaces(CharStreams.fromString(''))
        assert SemanticPredicates.isFollowedByWhiteSpaces(CharStreams.fromString(' \t\f'))
        assert SemanticPredicates.isFollowedByWhiteSpaces(CharStreams.fromString('\u000B'))
        assert SemanticPredicates.isFollowedByWhiteSpaces(CharStreams.fromString('  \ncode'))
        assert !SemanticPredicates.isFollowedByWhiteSpaces(CharStreams.fromString(' x'))
        assert !SemanticPredicates.isFollowedByWhiteSpaces(CharStreams.fromString('\u00A0'))
    }

    @Test
    void 'followed by matches any listed character'() {
        def cs = CharStreams.fromString('[x]')
        assert SemanticPredicates.isFollowedBy(cs, ' ' as char, '\t' as char, '[' as char)
        assert !SemanticPredicates.isFollowedBy(cs, ' ' as char, '\t' as char, '(' as char)
        assert !SemanticPredicates.isFollowedBy(CharStreams.fromString(''), 'a' as char)
    }

    @Test
    void 'following arguments or closure is false for non-postfix expressions'() {
        assert !SemanticPredicates.isFollowingArgumentsOrClosure(parseExpression('1 + 2'))
    }

    @Test
    void 'invalid local variable declaration uses the identifier code point'() {
        // supplementary uppercase: typed declaration, not a command
        assert !SemanticPredicates.isInvalidLocalVariableDeclaration(tokens('\uD801\uDC00 x = 1'))
        // supplementary lowercase: command expression
        assert SemanticPredicates.isInvalidLocalVariableDeclaration(tokens('\uD801\uDC28 x'))
    }

    @Test
    void 'named and single-element annotations still parse'() {
        ['@Foo class C {}',
         '@Foo() class C {}',
         '@Foo(1) class C {}',
         '@Foo(a = 1) class C {}',
         '@Foo(value = 1, other = 2) class C {}',
         '@Foo(a + 1) class C {}',
         '@Foo(class = 1) class C {}',
         '@Foo(module = 1) class C {}',
         '@Foo(non-sealed = 1) class C {}',
         '@Foo(int = 1) class C {}',
         '@Foo([1, 2]) class C {}',
         '@Foo(@Bar) class C {}',
         '@Foo(null) class C {}'
        ].each { src ->
            GroovyLangParser p = parser(src)
            assert p.compilationUnit() != null
            assert 0 == p.numberOfSyntaxErrors: "Failed to parse `${src}`"
        }
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
