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
package org.apache.groovy.lsp.internal.position

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.eclipse.lsp4j.Position
import org.junit.jupiter.api.Test

final class PositionsTest {

    @Test
    void utf16SupplementaryCharacterOccupiesTwoUnits() {
        String line = 'a🥤b'
        // Groovy columns: a=1, emoji=2, b=3, exclusive end of b=4
        assert Positions.groovyColumnToCharacter(line, 1, PositionEncoding.UTF16) == 0
        assert Positions.groovyColumnToCharacter(line, 2, PositionEncoding.UTF16) == 1
        assert Positions.groovyColumnToCharacter(line, 3, PositionEncoding.UTF16) == 3
        assert Positions.groovyColumnToCharacter(line, 4, PositionEncoding.UTF16) == 4
        assert Positions.characterToGroovyColumn(line, 3, PositionEncoding.UTF16) == 3
    }

    @Test
    void utf32MatchesCodePoints() {
        String line = 'a🥤b'
        assert Positions.groovyColumnToCharacter(line, 3, PositionEncoding.UTF32) == 2
        assert Positions.characterToGroovyColumn(line, 2, PositionEncoding.UTF32) == 3
    }

    @Test
    void utf8CountsBytes() {
        String line = 'a🥤b'
        int emojiBytes = '🥤'.getBytes('UTF-8').length
        assert Positions.groovyColumnToCharacter(line, 2, PositionEncoding.UTF8) == 1
        assert Positions.groovyColumnToCharacter(line, 3, PositionEncoding.UTF8) == 1 + emojiBytes
        assert Positions.characterToGroovyColumn(line, 1 + emojiBytes, PositionEncoding.UTF8) == 3
    }

    @Test
    void clampsPastEndOfLine() {
        assert Positions.groovyColumnToCharacter('ab', 99, PositionEncoding.UTF16) == 2
        assert Positions.characterToGroovyColumn('ab', 99, PositionEncoding.UTF16) == 3
    }

    @Test
    void lineTextSplitsOnAllEol() {
        assert Positions.lineText('a\nb\r\nc\rd', 1) == 'a'
        assert Positions.lineText('a\nb\r\nc\rd', 2) == 'b'
        assert Positions.lineText('a\nb\r\nc\rd', 3) == 'c'
        assert Positions.lineText('a\nb\r\nc\rd', 4) == 'd'
        assert Positions.lineText('a\nb\r\nc\rd', 5) == ''
        assert Positions.lineText(null, 1) == ''
    }

    @Test
    void toLspAndContains() {
        def pos = Positions.toLsp(2, 3, 'abc', PositionEncoding.UTF16)
        assert pos.line == 1
        assert pos.character == 2
        def node = new ASTNode()
        node.lineNumber = 1
        node.columnNumber = 1
        node.lastLineNumber = 1
        node.lastColumnNumber = 4
        assert Positions.contains(node, 1, 1)
        assert Positions.contains(node, 1, 3)
        assert !Positions.contains(node, 1, 4)
        assert !Positions.contains(node, 2, 1)
        assert !Positions.contains(null, 1, 1)
        assert Positions.innerScore(null) == Long.MIN_VALUE
        assert Positions.innerScore(node) > Long.MIN_VALUE
    }

    @Test
    void innerScorePrefersLaterStartOnTheSameLine() {
        def outer = new ASTNode()
        outer.lineNumber = 1
        outer.columnNumber = 1
        outer.lastLineNumber = 1
        outer.lastColumnNumber = 40
        def inner = new ASTNode()
        inner.lineNumber = 1
        inner.columnNumber = 12
        inner.lastLineNumber = 1
        inner.lastColumnNumber = 40
        assert Positions.innerScore(inner) > Positions.innerScore(outer)
    }

    @Test
    void identifierRangeUsesCallNameNotArgumentList() {
        def call = new MethodCallExpression(
                new VariableExpression('this'),
                'foo',
                new ArgumentListExpression())
        call.lineNumber = 1
        call.columnNumber = 1
        call.lastLineNumber = 1
        call.lastColumnNumber = 10
        call.method.lineNumber = 1
        call.method.columnNumber = 1
        call.method.lastLineNumber = 1
        call.method.lastColumnNumber = 4
        def range = Positions.toIdentifierRange(call, 'foo(bar)', PositionEncoding.UTF16)
        assert range.end.character == 3
    }

    @Test
    void nameRangeFindsIdentifierInSourceWithoutCoreNameSpans() {
        def node = new ClassNode('Hello', 0, ClassHelper.OBJECT_TYPE)
        node.lineNumber = 1
        node.columnNumber = 1
        node.lastLineNumber = 1
        node.lastColumnNumber = 12
        def range = Positions.toNameRange(node, 'class Hello', PositionEncoding.UTF16)
        assert range.start.character == 6
        assert range.end.character == 11
        def longer = new ClassNode('Hello', 0, ClassHelper.OBJECT_TYPE)
        longer.lineNumber = 1
        longer.columnNumber = 1
        longer.lastLineNumber = 1
        longer.lastColumnNumber = 18
        def skipped = Positions.toNameRange(longer, 'class HelloWorld', PositionEncoding.UTF16)
        assert skipped == null
        assert Positions.toNameRange(null, '', PositionEncoding.UTF16) == null
        assert Positions.toRange(null, '', PositionEncoding.UTF16) == null
    }

    @Test
    void toGroovyColumnUsesPosition() {
        assert Positions.toGroovyColumn(new Position(0, 2), 'abc', PositionEncoding.UTF16) == 3
    }

    @Test
    void negotiateDefaultsToUtf16() {
        assert PositionEncoding.negotiate(null) == PositionEncoding.UTF16
        assert PositionEncoding.negotiate([]) == PositionEncoding.UTF16
        assert PositionEncoding.negotiate(['utf-32', 'utf-16']) == PositionEncoding.UTF32
        assert PositionEncoding.negotiate(['nope']) == PositionEncoding.UTF16
        assert PositionEncoding.fromProtocol('UTF-8') == PositionEncoding.UTF8
        assert PositionEncoding.fromProtocol(null) == null
        assert PositionEncoding.UTF16.protocolName() == 'utf-16'
    }
}
