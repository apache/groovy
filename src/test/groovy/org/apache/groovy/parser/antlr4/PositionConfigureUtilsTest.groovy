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

import org.antlr.v4.runtime.CommonToken
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.tree.TerminalNodeImpl
import org.apache.groovy.parser.antlr4.util.PositionConfigureUtils
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.EmptyExpression
import org.junit.jupiter.api.Test

/**
 * Position calculations for tokens whose {@link Token#getText()} is null
 * (typical of an unset EOF / imaginary token), and the
 * {@code initialStop} / {@code start,stop} overloads that must ignore
 * synthetic nodes whose end is unset.
 */
final class PositionConfigureUtilsTest {

    @Test
    void endPositionWithNullTokenTextUsesStartColumn() {
        CommonToken token = new CommonToken(Token.EOF)
        token.setLine(5)
        token.setCharPositionInLine(7)
        assert token.text == null

        def (lastLine, lastColumn) = PositionConfigureUtils.endPosition(token)
        assert lastLine == 5
        // 0-based start column 7 → 1-based exclusive end column 8 for empty text
        assert lastColumn == 8
    }

    @Test
    void endPositionWithEmptyTextUsesStartColumn() {
        CommonToken token = token('', 4, 2)
        def (lastLine, lastColumn) = PositionConfigureUtils.endPosition(token)
        assert lastLine == 4
        assert lastColumn == 3
    }

    @Test
    void endPositionWithoutNewlineUsesCodePointLength() {
        CommonToken token = token('int', 1, 8)
        def (lastLine, lastColumn) = PositionConfigureUtils.endPosition(token)
        assert lastLine == 1
        assert lastColumn == 12
    }

    @Test
    void endPositionWithNewlineUsesTrailingLine() {
        CommonToken token = token('a\nb', 5, 2)
        def (lastLine, lastColumn) = PositionConfigureUtils.endPosition(token)
        assert lastLine == 6
        // trailing "\nb" measured in code points from the last newline
        assert lastColumn == 2
    }

    @Test
    void configureASTWithNullTokenTextUsesStartColumn() {
        CommonToken token = new CommonToken(Token.EOF)
        token.setLine(3)
        token.setCharPositionInLine(4)
        assert token.text == null

        def node = PositionConfigureUtils.configureAST(new ASTNode(), token)
        assertPos node, 3, 5, 3, 5
    }

    @Test
    void configureASTWithTokenTextUsesCodePointLength() {
        def node = PositionConfigureUtils.configureAST(new ASTNode(), token('foo', 2, 0))
        assertPos node, 2, 1, 2, 4
    }

    @Test
    void configureASTWithTerminalNodeMatchesToken() {
        def node = PositionConfigureUtils.configureAST(new ASTNode(), new TerminalNodeImpl(token('x', 9, 3)))
        assertPos node, 9, 4, 9, 5
    }

    @Test
    void configureASTFromSourceCopiesAllFour() {
        def source = positioned(4, 2, 5, 8)
        def node = PositionConfigureUtils.configureAST(new ASTNode(), source)
        assertPos node, 4, 2, 5, 8
    }

    @Test
    void configureASTFromContextUsesStartAndStopTokens() {
        def ctx = ctx(token('int', 2, 4), token(';', 2, 20))
        def node = PositionConfigureUtils.configureAST(new ASTNode(), ctx)
        assertPos node, 2, 5, 2, 22
    }

    @Test
    void configureEndPositionUsesStopToken() {
        def node = new ASTNode()
        PositionConfigureUtils.configureEndPosition(node, token(';', 7, 10))
        assert node.lastLineNumber == 7
        assert node.lastColumnNumber == 12
    }

    @Test
    void configureASTContextInitialStopUsesAuthoredEnd() {
        def ctx = ctx(token('int', 2, 4), token(';', 2, 20))
        def initial = positioned(2, 18, 2, 19)
        def node = PositionConfigureUtils.configureAST(new ASTNode(), ctx, initial)
        assertPos node, 2, 5, 2, 19
    }

    @Test
    void configureASTContextInitialStopIgnoresSyntheticConstant() {
        def ctx = ctx(token('int', 2, 4), token(';', 2, 20))
        def synthetic = new ConstantExpression(0, true)
        assert synthetic.lastLineNumber == -1

        def node = PositionConfigureUtils.configureAST(new ASTNode(), ctx, synthetic)
        assertPos node, 2, 5, 2, 22
    }

    @Test
    void configureASTContextInitialStopIgnoresEmptyExpressionSingleton() {
        def ctx = ctx(token('int', 2, 4), token(';', 2, 20))
        def node = PositionConfigureUtils.configureAST(new ASTNode(), ctx, EmptyExpression.INSTANCE)
        assertPos node, 2, 5, 2, 22
    }

    @Test
    void configureASTContextInitialStopNullFallsBackToContext() {
        def ctx = ctx(token('int', 2, 4), token(';', 2, 20))
        def node = PositionConfigureUtils.configureAST(new ASTNode(), ctx, null)
        assertPos node, 2, 5, 2, 22
    }

    @Test
    void configureASTContextInitialStopZeroLastLineFallsBackToContext() {
        def ctx = ctx(token('int', 2, 4), token(';', 2, 20))
        def zero = positioned(1, 1, 0, 1)
        def node = PositionConfigureUtils.configureAST(new ASTNode(), ctx, zero)
        assertPos node, 2, 5, 2, 22
    }

    @Test
    void configureASTStartStopUsesAuthoredEnd() {
        def start = positioned(3, 2, 3, 5)
        def stop = positioned(3, 8, 3, 12)
        def node = PositionConfigureUtils.configureAST(new ASTNode(), start, stop)
        assertPos node, 3, 2, 3, 12
    }

    @Test
    void configureASTStartStopIgnoresUnpositionedStop() {
        def start = positioned(3, 2, 3, 5)
        def node = PositionConfigureUtils.configureAST(new ASTNode(), start, new ASTNode())
        assertPos node, 3, 2, 3, 5
    }

    @Test
    void configureASTStartStopNullStopUsesStartEnd() {
        def start = positioned(3, 2, 3, 5)
        def node = PositionConfigureUtils.configureAST(new ASTNode(), start, null)
        assertPos node, 3, 2, 3, 5
    }

    @Test
    void configureASTStartStopIgnoresEmptyExpressionSingleton() {
        def start = positioned(3, 2, 3, 5)
        def node = PositionConfigureUtils.configureAST(new ASTNode(), start, EmptyExpression.INSTANCE)
        assertPos node, 3, 2, 3, 5
    }

    @Test
    void configureASTStartStopIgnoresSyntheticConstant() {
        def start = positioned(3, 2, 3, 5)
        def node = PositionConfigureUtils.configureAST(new ASTNode(), start, new ConstantExpression(0, true))
        assertPos node, 3, 2, 3, 5
    }

    @Test
    void configureASTStartStopZeroLastLineUsesStartEnd() {
        def start = positioned(3, 2, 3, 5)
        def zero = positioned(3, 8, 0, 9)
        def node = PositionConfigureUtils.configureAST(new ASTNode(), start, zero)
        assertPos node, 3, 2, 3, 5
    }

    private static CommonToken token(String text, int line, int charPos) {
        CommonToken token = new CommonToken(0, text)
        token.setLine(line)
        token.setCharPositionInLine(charPos)
        return token
    }

    private static GroovyParser.GroovyParserRuleContext ctx(Token start, Token stop) {
        def ctx = new GroovyParser.GroovyParserRuleContext()
        ctx.start = start
        ctx.stop = stop
        return ctx
    }

    private static ASTNode positioned(int line, int column, int lastLine, int lastColumn) {
        def node = new ASTNode()
        node.lineNumber = line
        node.columnNumber = column
        node.lastLineNumber = lastLine
        node.lastColumnNumber = lastColumn
        return node
    }

    private static void assertPos(ASTNode node, int line, int column, int lastLine, int lastColumn) {
        assert [node.lineNumber, node.columnNumber, node.lastLineNumber, node.lastColumnNumber] == [line, column, lastLine, lastColumn]
    }
}
