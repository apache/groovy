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

import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.control.CompilePhase
import org.junit.jupiter.api.Test

/**
 * GROOVY-12085: AST column numbers are code-point based (the lexer reads an ANTLR
 * CodePointCharStream), so a supplementary character (e.g. an emoji) must advance
 * the column by one code point, not by its two UTF-16 units. This covers the
 * multi-line-token path in {@code PositionConfigureUtils.endPosition}, whose end
 * column is computed from the text after the last newline.
 */
final class Groovy12085Test {

    @Test
    void testMultiLineTokenEndColumnIsCodePointBased() {
        // last line is: end🥤"""  -- 7 code points, so the literal ends at column 8
        Expression withEmoji = literalOf('def a = """line1\nend🥤"""')
        // control without the supplementary character: endz"""
        Expression withAscii = literalOf('def a = """line1\nendz"""')

        assert [withEmoji.lastLineNumber, withEmoji.lastColumnNumber] == [2, 8]
        assert [withAscii.lastLineNumber, withAscii.lastColumnNumber] == [2, 8]

        // the emoji (1 code point) must occupy the same column span as a single ASCII char
        assert withEmoji.lastColumnNumber == withAscii.lastColumnNumber
    }

    private static Expression literalOf(String src) {
        def nodes = new AstBuilder().buildFromString(CompilePhase.CONVERSION, false, src)
        nodes[0].statements[0].expression.rightExpression
    }
}
