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
import org.apache.groovy.parser.antlr4.util.PositionConfigureUtils
import org.codehaus.groovy.ast.ASTNode
import org.junit.jupiter.api.Test

/**
 * Position calculations for tokens whose {@link Token#getText()} is null
 * (typical of an unset EOF / imaginary token).
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
    void configureASTWithNullTokenTextUsesStartColumn() {
        CommonToken token = new CommonToken(Token.EOF)
        token.setLine(3)
        token.setCharPositionInLine(4)
        assert token.text == null

        def node = PositionConfigureUtils.configureAST(new ASTNode(), token)
        assert node.lineNumber == 3
        assert node.columnNumber == 5
        assert node.lastLineNumber == 3
        assert node.lastColumnNumber == 5
    }
}
