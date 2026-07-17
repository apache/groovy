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
package org.apache.groovy.parser.antlr4.internal

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonToken
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.misc.IntervalSet
import org.apache.groovy.parser.antlr4.GroovyLangLexer
import org.apache.groovy.parser.antlr4.GroovyParser
import org.junit.jupiter.api.Test

import java.lang.reflect.Method

import static org.apache.groovy.parser.antlr4.GroovyParser.AT
import static org.apache.groovy.parser.antlr4.GroovyParser.BITAND
import static org.apache.groovy.parser.antlr4.GroovyParser.BuiltInPrimitiveType
import static org.apache.groovy.parser.antlr4.GroovyParser.COMMA
import static org.apache.groovy.parser.antlr4.GroovyParser.CapitalizedIdentifier
import static org.apache.groovy.parser.antlr4.GroovyParser.DOT
import static org.apache.groovy.parser.antlr4.GroovyParser.GT
import static org.apache.groovy.parser.antlr4.GroovyParser.Identifier
import static org.apache.groovy.parser.antlr4.GroovyParser.IntegerLiteral
import static org.apache.groovy.parser.antlr4.GroovyParser.LBRACE
import static org.apache.groovy.parser.antlr4.GroovyParser.LBRACK
import static org.apache.groovy.parser.antlr4.GroovyParser.LPAREN
import static org.apache.groovy.parser.antlr4.GroovyParser.LT
import static org.apache.groovy.parser.antlr4.GroovyParser.RBRACE
import static org.apache.groovy.parser.antlr4.GroovyParser.RBRACK
import static org.apache.groovy.parser.antlr4.GroovyParser.RPAREN
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * Branch-level coverage for {@link MissingDelimiterDiagnostic} (GROOVY-12169).
 * Complements the end-to-end cases in {@code SyntaxErrorTest} by exercising
 * edge paths that are hard to reach through full compilation alone.
 */
final class MissingDelimiterDiagnosticTest {

    //--------------------------------------------------------------------------
    // locate() entry points
    //--------------------------------------------------------------------------

    @Test
    void 'locate returns null for null token stream'() {
        assertNull MissingDelimiterDiagnostic.locate(null, null)
    }

    @Test
    void 'locate returns null when token collection fails'() {
        // get(i) throws → empty collection → null
        assertNull MissingDelimiterDiagnostic.locate(new ThrowingTokenStream(), null)
    }

    @Test
    void 'locate with null exception still finds unclosed paren via stack'() {
        def tokens = tokenStream('foo(1')
        def hit = MissingDelimiterDiagnostic.locate(tokens, null)
        assertNotNull hit
        assertEquals "Missing ')'", hit.message
    }

    @Test
    void 'private constructor is callable for utility-class coverage'() {
        def ctor = MissingDelimiterDiagnostic.getDeclaredConstructor()
        ctor.accessible = true
        assertNotNull ctor.newInstance()
    }

    //--------------------------------------------------------------------------
    // Strategy 1 — sole expected closer
    //--------------------------------------------------------------------------

    @Test
    void 'sole expected RPAREN with open depth reports Missing paren'() {
        def tokens = tokenStream('m(x')
        def eof = lastToken(tokens)
        def hit = MissingDelimiterDiagnostic.locate(tokens, stubException(setOf(RPAREN), eof))
        assertNotNull hit
        assertEquals "Missing ')'", hit.message
    }

    @Test
    void 'sole expected RBRACK with open depth reports Missing bracket'() {
        def tokens = tokenStream('[1')
        def eof = lastToken(tokens)
        def hit = MissingDelimiterDiagnostic.locate(tokens, stubException(setOf(RBRACK), eof))
        assertNotNull hit
        assertEquals "Missing ']'", hit.message
    }

    @Test
    void 'sole expected RBRACE with open depth reports Missing brace'() {
        def tokens = tokenStream('{1')
        def eof = lastToken(tokens)
        def hit = MissingDelimiterDiagnostic.locate(tokens, stubException(setOf(RBRACE), eof))
        assertNotNull hit
        assertEquals "Missing '}'", hit.message
    }

    @Test
    void 'sole expected closer ignored when delimiter family is balanced'() {
        def tokens = tokenStream('foo(1;2;3)')
        // Parser might claim it wanted only RPAREN at ';', but parens are balanced overall
        def semi = findFirst(tokens) { it.text == ';' }
        def hit = MissingDelimiterDiagnostic.locate(tokens, stubException(setOf(RPAREN), semi))
        // depth of parens is 0 after fill → strategy 1 returns null; stack is also balanced
        assertNull hit
    }

    @Test
    void 'sole expected set that is not a single closer is ignored'() {
        def tokens = tokenStream('foo(1')
        def eof = lastToken(tokens)
        def hit = MissingDelimiterDiagnostic.locate(tokens, stubException(setOf(COMMA), eof))
        // falls through to cast/stack — still reports missing ')' via stack
        assertNotNull hit
        assertEquals "Missing ')'", hit.message
    }

    @Test
    void 'sole expected multi-token set is ignored by strategy 1'() {
        def tokens = tokenStream('foo(1')
        def eof = lastToken(tokens)
        def multi = new IntervalSet()
        multi.add(RPAREN)
        multi.add(COMMA)
        // strategy 1 skips (size != 1); stack still finds unclosed '('
        def hit = MissingDelimiterDiagnostic.locate(tokens, stubException(multi, eof))
        assertNotNull hit
        assertEquals "Missing ')'", hit.message
    }

    @Test
    void 'sole expected with null IntervalSet falls through'() {
        def tokens = tokenStream('[1')
        def hit = MissingDelimiterDiagnostic.locate(tokens, stubException(null, lastToken(tokens)))
        assertNotNull hit
        assertEquals "Missing ']'", hit.message
    }

    @Test
    void 'sole expected with null offending token falls through'() {
        def tokens = tokenStream('m(a')
        def hit = MissingDelimiterDiagnostic.locate(tokens, stubException(setOf(RPAREN), null))
        assertNotNull hit
        assertEquals "Missing ')'", hit.message
    }

    @Test
    void 'sole expected EOF uses insertion point after last real token'() {
        def tokens = tokenStream('def f(int x')
        def eof = lastToken(tokens)
        assertEquals Token.EOF, eof.type
        def hit = MissingDelimiterDiagnostic.locate(tokens, stubException(setOf(RPAREN), eof))
        assertNotNull hit
        assertEquals "Missing ')'", hit.message
        // caret after 'x', not on the following empty line of EOF
        assertEquals 1, hit.at.line
        assertTrue hit.at.charPositionInLine > 0
    }

    @Test
    void 'sole expected non-EOF offending token keeps that token as caret'() {
        def tokens = tokenStream('def m( { }')
        def lbrace = findFirst(tokens) { it.type == LBRACE }
        def hit = MissingDelimiterDiagnostic.locate(tokens, stubException(setOf(RPAREN), lbrace))
        assertNotNull hit
        assertEquals "Missing ')'", hit.message
        assertEquals LBRACE, /* token type may not be on Hit.at if synthetic */ lbrace.type
        // when non-EOF offending is used, caret is the brace token
        assertEquals lbrace.line, hit.at.line
        assertEquals lbrace.charPositionInLine, hit.at.charPositionInLine
    }

    //--------------------------------------------------------------------------
    // Strategy 2 — cast patterns
    //--------------------------------------------------------------------------

    @Test
    void 'cast pattern primitive then literal'() {
        def hit = MissingDelimiterDiagnostic.locate(tokenStream('(int 123'), null)
        assertNotNull hit
        assertEquals "Missing ')'", hit.message
    }

    @Test
    void 'cast pattern void then literal'() {
        // void is not a realistic cast target but exercises VOID branch in isPrimitiveOrVoid
        def hit = MissingDelimiterDiagnostic.locate(tokenStream('(void 1'), null)
        assertNotNull hit
        assertEquals "Missing ')'", hit.message
    }

    @Test
    void 'cast pattern CapitalizedIdentifier then literal'() {
        def hit = MissingDelimiterDiagnostic.locate(tokenStream('(Foo 1'), null)
        // Foo is CapitalizedIdentifier; operand 1 is literal → high-confidence cast miss
        assertNotNull hit
        assertEquals "Missing ')'", hit.message
    }

    @Test
    void 'cast pattern qualified type then literal'() {
        def hit = MissingDelimiterDiagnostic.locate(tokenStream('(java.lang.Integer 1'), null)
        assertNotNull hit
        assertEquals "Missing ')'", hit.message
    }

    @Test
    void 'cast pattern generic type then literal'() {
        def hit = MissingDelimiterDiagnostic.locate(tokenStream('(List<String> 1'), null)
        assertNotNull hit
        assertEquals "Missing ')'", hit.message
    }

    @Test
    void 'cast pattern array type then literal'() {
        def hit = MissingDelimiterDiagnostic.locate(tokenStream('(int[] 1'), null)
        assertNotNull hit
        assertEquals "Missing ')'", hit.message
    }

    @Test
    void 'cast pattern annotated primitive then literal'() {
        def hit = MissingDelimiterDiagnostic.locate(tokenStream('(@Deprecated int 1'), null)
        assertNotNull hit
        assertEquals "Missing ')'", hit.message
    }

    @Test
    void 'cast pattern annotation with args then literal'() {
        def hit = MissingDelimiterDiagnostic.locate(tokenStream('(@Anno(1) int 2'), null)
        assertNotNull hit
        assertEquals "Missing ')'", hit.message
    }

    @Test
    void 'cast pattern floating point operand'() {
        def hit = MissingDelimiterDiagnostic.locate(tokenStream('(int 1.5'), null)
        assertNotNull hit
        assertEquals "Missing ')'", hit.message
    }

    @Test
    void 'cast pattern string operand'() {
        def hit = MissingDelimiterDiagnostic.locate(tokenStream('(int "x"'), null)
        assertNotNull hit
        assertEquals "Missing ')'", hit.message
    }

    @Test
    void 'cast pattern boolean operand'() {
        def hit = MissingDelimiterDiagnostic.locate(tokenStream('(int true'), null)
        assertNotNull hit
        assertEquals "Missing ')'", hit.message
    }

    @Test
    void 'cast pattern nested paren operand'() {
        def hit = MissingDelimiterDiagnostic.locate(tokenStream('(int (1'), null)
        assertNotNull hit
        assertEquals "Missing ')'", hit.message
    }

    @Test
    void 'cast pattern type only at EOF'() {
        def hit = MissingDelimiterDiagnostic.locate(tokenStream('(int'), null)
        assertNotNull hit
        assertEquals "Missing ')'", hit.message
    }

    @Test
    void 'well-formed cast does not fire cast strategy alone when parens close'() {
        // balanced (int)1 — no missing delimiter
        assertNull MissingDelimiterDiagnostic.locate(tokenStream('(int)1'), null)
    }

    @Test
    void 'intersection-type cast continues past BITAND without false positive'() {
        // (A & B) x  with missing outer — still has well-formed structure for first type+BITAND
        // Use incomplete: (A &  so after BITAND we need another type
        def hit = MissingDelimiterDiagnostic.locate(tokenStream('(A & B 1'), null)
        // (A is type CapitalizedIdentifier, next is BITAND → continue cast scan;
        // may still report via stack or later cast of incomplete form
        assertNotNull hit
        assertEquals "Missing ')'", hit.message
    }

    @Test
    void 'parenthesised command-like form does not use low-confidence cast'() {
        // (foo bar) incomplete: (foo bar — identifier after identifier is low confidence for cast
        def hit = MissingDelimiterDiagnostic.locate(tokenStream('(foo bar'), null)
        // stack still reports missing ')'
        assertNotNull hit
        assertEquals "Missing ')'", hit.message
    }

    @Test
    void 'malformed annotation after LPAREN skips cast type match'() {
        // (@ alone — skipAnnotations returns -1
        def hit = MissingDelimiterDiagnostic.locate(tokenStream('(@'), null)
        assertNotNull hit
        assertEquals "Missing ')'", hit.message // via stack
    }

    @Test
    void 'cast pattern continues past DOT after primitive without false cast hit on DOT'() {
        // afterType lands on DOT → continue in cast scan; still unclosed via stack
        def hit = MissingDelimiterDiagnostic.locate(tokenStream('(int .x'), null)
        assertNotNull hit
        assertEquals "Missing ')'", hit.message
    }

    @Test
    void 'cast pattern continues past LT fragment without treating as cast operand'() {
        def hit = MissingDelimiterDiagnostic.locate(tokenStream('(Foo<'), null)
        assertNotNull hit
        assertEquals "Missing ')'", hit.message
    }

    @Test
    void 'unbalanced annotation arguments in cast position'() {
        def hit = MissingDelimiterDiagnostic.locate(tokenStream('(@Anno(1 int 2'), null)
        assertNotNull hit
        assertEquals "Missing ')'", hit.message
    }

    @Test
    void 'unbalanced generics in cast type position'() {
        def hit = MissingDelimiterDiagnostic.locate(tokenStream('(List<String 1'), null)
        assertNotNull hit
        assertEquals "Missing ')'", hit.message
    }

    @Test
    void 'bracket depth counts nested open and close before trailing unclosed'() {
        // [[1]  → RBRACK decrements depth then one open remains (strategy 1 or stack)
        def tokens = tokenStream('[[1]')
        def hit = MissingDelimiterDiagnostic.locate(tokens, stubException(setOf(RBRACK), lastToken(tokens)))
        assertNotNull hit
        assertEquals "Missing ']'", hit.message
    }

    @Test
    void 'brace depth counts close then still unclosed outer'() {
        def tokens = tokenStream('{{1}')
        def hit = MissingDelimiterDiagnostic.locate(tokens, stubException(setOf(RBRACE), lastToken(tokens)))
        assertNotNull hit
        assertEquals "Missing '}'", hit.message
    }

    @Test
    void 'paren depth counts close then still unclosed outer'() {
        def tokens = tokenStream('((1)')
        def hit = MissingDelimiterDiagnostic.locate(tokens, stubException(setOf(RPAREN), lastToken(tokens)))
        assertNotNull hit
        assertEquals "Missing ')'", hit.message
    }

    //--------------------------------------------------------------------------
    // Strategy 3 — delimiter stack
    //--------------------------------------------------------------------------

    @Test
    void 'extra closer alone is not reported as missing'() {
        assertNull MissingDelimiterDiagnostic.locate(tokenStream(')'), null)
        assertNull MissingDelimiterDiagnostic.locate(tokenStream(']'), null)
        assertNull MissingDelimiterDiagnostic.locate(tokenStream('}'), null)
    }

    @Test
    void 'mismatched closer prefers innermost opener'() {
        def hit = MissingDelimiterDiagnostic.locate(tokenStream('foo([1, 2)'), null)
        assertNotNull hit
        assertEquals "Missing ']'", hit.message
    }

    @Test
    void 'mismatched brace against open paren'() {
        def hit = MissingDelimiterDiagnostic.locate(tokenStream('m(}'), null)
        assertNotNull hit
        assertEquals "Missing ')'", hit.message
    }

    @Test
    void 'opener with no interior tokens at EOF'() {
        def hit = MissingDelimiterDiagnostic.locate(tokenStream('('), null)
        assertNotNull hit
        assertEquals "Missing ')'", hit.message
    }

    @Test
    void 'opener with no interior tokens and mismatched closer'() {
        def hit = MissingDelimiterDiagnostic.locate(tokenStream('(]'), null)
        assertNotNull hit
        assertEquals "Missing ')'", hit.message
    }

    @Test
    void 'safe-index unclosed reports Missing bracket'() {
        def hit = MissingDelimiterDiagnostic.locate(tokenStream('a?[0'), null)
        assertNotNull hit
        assertEquals "Missing ']'", hit.message
    }

    @Test
    void 'newlines inside braces do not move caret past last statement token'() {
        def hit = MissingDelimiterDiagnostic.locate(tokenStream('def m() {\n  println 1\n'), null)
        assertNotNull hit
        assertEquals "Missing '}'", hit.message
        assertEquals 2, hit.at.line
    }

    @Test
    void 'balanced nested delimiters yield null'() {
        assertNull MissingDelimiterDiagnostic.locate(tokenStream('foo([1, 2]) { x }'), null)
    }

    //--------------------------------------------------------------------------
    // insertionPointAfter / collectDefaultChannelTokens edge cases
    //--------------------------------------------------------------------------

    @Test
    void 'insertion point after multi-code-point token'() {
        // emoji as identifier content is not valid, but insertionPointAfter uses codePointCount
        // Exercise via unclosed list ending with a multi-byte string literal
        def hit = MissingDelimiterDiagnostic.locate(tokenStream('["\uD83D\uDE00"'), null)
        assertNotNull hit
        assertEquals "Missing ']'", hit.message
    }

    //--------------------------------------------------------------------------
    // DescriptiveErrorStrategy integration (fallback path)
    //--------------------------------------------------------------------------

    @Test
    void 'balanced illegal construct does not force Missing delimiter message'() {
        // end-to-end: foo(1;2;3) must not become Missing ')'
        def err = compileError('foo(1;2;3)')
        assertTrue !err.contains("Missing ')'"), err
    }

    @Test
    void 'end-to-end sole expected RBRACE style unclosed class'() {
        def err = compileError('class C {\n  def x\n')
        assertTrue err.contains("Missing '}'"), err
    }

    @Test
    void 'end-to-end void cast missing paren'() {
        // may surface as Missing ')' via cast or stack
        def err = compileError('def x = (int true')
        assertTrue err.contains("Missing ')'"), err
    }

    @Test
    void 'end-to-end generic cast missing paren'() {
        def err = compileError('def x = (List<String> 1')
        assertTrue err.contains("Missing ')'"), err
    }

    @Test
    void 'end-to-end annotation cast missing paren'() {
        def err = compileError('def x = (@Deprecated int 1')
        assertTrue err.contains("Missing ')'"), err
    }

    @Test
    void 'end-to-end empty open paren'() {
        def err = compileError('(')
        assertTrue err.contains("Missing ')'"), err
    }

    @Test
    void 'end-to-end mismatched paren bracket'() {
        def err = compileError('(]')
        assertTrue err.contains("Missing ')'") || err.contains("Missing ']'"), err
    }

    //--------------------------------------------------------------------------
    // Reflection-based branch coverage for defensive / hard-to-reach paths
    //--------------------------------------------------------------------------

    @Test
    void 'fromCastPattern typeStart negative continues'() {
        // LPAREN + bare AT (no annotation name) → skipAnnotations returns -1 → continue
        def tokens = [tok(LPAREN, '('), tok(AT, '@')]
        assertEquals (-1), invoke('skipAnnotations', tokens, 1)
        assertNull invoke('fromCastPattern', tokens)

        // also with EOF sentinel
        def withEof = [tok(LPAREN, '('), tok(AT, '@'), tok(Token.EOF, null)]
        assertEquals (-1), invoke('skipAnnotations', withEof, 1)
        assertNull invoke('fromCastPattern', withEof)
    }

    @Test
    void 'fromCastPattern typeStart past end continues'() {
        // skipAnnotations returns index == n (empty type region)
        def tokens = [tok(LPAREN, '(')]
        assertEquals 1, invoke('skipAnnotations', tokens, 1) // start==n
        assertNull invoke('fromCastPattern', tokens)
    }

    @Test
    void 'fromCastPattern afterType at end of list without EOF sentinel'() {
        // No room for operand token → afterType >= n
        def tokens = [tok(LPAREN, '('), tok(BuiltInPrimitiveType, 'int')]
        def hit = (MissingDelimiterDiagnostic.Hit) invoke('fromCastPattern', tokens)
        assertNotNull hit
        assertEquals "Missing ')'", hit.message
    }

    @Test
    void 'fromCastPattern continues when next is DOT LT or LBRACK'() {
        def base = [tok(LPAREN, '('), tok(BuiltInPrimitiveType, 'int')]
        for (def next : [[tok(DOT, '.'), tok(Identifier, 'x')],
                         [tok(LT, '<'), tok(CapitalizedIdentifier, 'T')],
                         [tok(LBRACK, '['), tok(IntegerLiteral, '0')]]) {
            def tokens = base + next
            // continues cast loop; no high-confidence hit → null from cast alone
            assertNull invoke('fromCastPattern', tokens)
        }
    }

    @Test
    void 'fromCastPattern well-formed cast and intersection continue'() {
        assertNull invoke('fromCastPattern', [
            tok(LPAREN, '('), tok(BuiltInPrimitiveType, 'int'), tok(RPAREN, ')'), tok(IntegerLiteral, '1')
        ])
        assertNull invoke('fromCastPattern', [
            tok(LPAREN, '('), tok(CapitalizedIdentifier, 'A'), tok(BITAND, '&'), tok(CapitalizedIdentifier, 'B'),
            tok(RPAREN, ')'), tok(IntegerLiteral, '1')
        ])
    }

    @Test
    void 'hitForOpen lastInside null branches'() {
        def open = tok(LPAREN, '(')
        def closer = tok(RBRACK, ']')
        // lastInside null + mismatchCloser
        def hit1 = (MissingDelimiterDiagnostic.Hit) invoke('hitForOpen', open, null, closer)
        assertEquals "Missing ')'", hit1.message
        assertEquals closer, hit1.at
        // lastInside null + no mismatch → caret on open
        def hit2 = (MissingDelimiterDiagnostic.Hit) invoke('hitForOpen', open, null, null)
        assertEquals open, hit2.at
    }

    @Test
    void 'closes returns false for unknown open type'() {
        assertEquals Boolean.FALSE, invoke('closes', -999, RPAREN)
    }

    @Test
    void 'matchType returns start when annotations fail or input exhausted'() {
        // annotations fail → start
        assertEquals 0, invoke('matchType', [tok(AT, '@')], 0)
        // start past end
        assertEquals 1, invoke('matchType', [tok(LPAREN, '(')], 1)
    }

    @Test
    void 'skipBalanced returns -1 when unclosed through end of list'() {
        def tokens = [tok(LT, '<'), tok(CapitalizedIdentifier, 'T')]
        assertEquals (-1), invoke('skipBalanced', tokens, 0, LT, GT)
    }

    @Test
    void 'skipBalanced returns -1 on EOF token in stream'() {
        def tokens = [tok(LPAREN, '('), tok(Token.EOF, null)]
        assertEquals (-1), invoke('skipBalanced', tokens, 0, LPAREN, RPAREN)
    }

    @Test
    void 'lastNonEof returns null when only EOF present'() {
        assertNull invoke('lastNonEof', [tok(Token.EOF, null)])
    }

    @Test
    void 'insertionPointAfter null text uses zero length'() {
        def t = tok(Identifier, null)
        t.text = null
        def point = (Token) invoke('insertionPointAfter', t)
        assertEquals t.line, point.line
        assertEquals t.charPositionInLine, point.charPositionInLine
    }

    //--------------------------------------------------------------------------
    // helpers
    //--------------------------------------------------------------------------

    private static CommonTokenStream tokenStream(String source) {
        def lexer = new GroovyLangLexer(CharStreams.fromString(source))
        def tokens = new CommonTokenStream(lexer)
        tokens.fill()
        return tokens
    }

    private static CommonToken tok(int type, String text) {
        def t = new CommonToken(type, text)
        t.line = 1
        t.charPositionInLine = 0
        return t
    }

    private static Object invoke(String name, Object... args) {
        Method match = null
        for (Method m : MissingDelimiterDiagnostic.declaredMethods) {
            if (m.name == name && m.parameterCount == args.length) {
                match = m
                break
            }
        }
        assertNotNull match, "method $name with ${args.length} args"
        match.accessible = true
        return match.invoke(null, args)
    }

    private static Token lastToken(CommonTokenStream tokens) {
        tokens.get(tokens.size() - 1)
    }

    private static Token findFirst(CommonTokenStream tokens, Closure pred) {
        for (int i = 0; i < tokens.size(); i++) {
            def t = tokens.get(i)
            if (pred(t)) return t
        }
        return null
    }

    private static IntervalSet setOf(int... types) {
        def set = new IntervalSet()
        types.each { set.add(it) }
        return set
    }

    private static RecognitionException stubException(IntervalSet expected, Token offending) {
        new StubRecognitionException(expected, offending)
    }

    private static String compileError(String source) {
        try {
            def cu = new org.codehaus.groovy.control.CompilationUnit()
            cu.addSource('t.groovy', source)
            cu.compile(org.codehaus.groovy.control.Phases.CONVERSION)
            return ''
        } catch (Throwable e) {
            return e.message ?: ''
        }
    }

    /**
     * Minimal RecognitionException stand-in that returns a fixed expected set
     * and offending token (package-local unit tests only).
     */
    private static final class StubRecognitionException extends RecognitionException {
        private final IntervalSet expected
        private final Token offending

        StubRecognitionException(IntervalSet expected, Token offending) {
            super(null, null, null)
            this.expected = expected
            this.offending = offending
        }

        @Override
        IntervalSet getExpectedTokens() {
            return expected
        }

        @Override
        Token getOffendingToken() {
            return offending
        }
    }

}
