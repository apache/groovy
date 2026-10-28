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

import groovy.lang.GroovyShell
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.Token
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.ParserPlugin
import org.junit.jupiter.api.Test

/**
 * A bare carriage return is a line ending. Antlr's lexer only advances the
 * line on {@code \n}, so a source file that uses {@code \r} by itself (or
 * mixes one into a file of {@code \r\n}) reports import, statement, syntax
 * error, and stack-trace lines that do not match the editor gutter.
 * {@code \r\n} and {@code \n} stay one line break each.
 */
final class Groovy12435 {

    /**
     * Attachment {@code LineEndings.groovy} from the ticket: one bare
     * {@code \r} after {@code package foo}, {@code \r\n} everywhere else.
     * The import is line 3 and the println is line 7.
     */
    private static final String ATTACHMENT =
            "package foo\r\n\rimport java.nio.*\r\n\r\n// single-line comment\r\n\r\nprintln 'this is line 7'"

    @Test
    void attachmentImportAndStatementUseEditorLines() {
        ModuleNode module = module(ATTACHMENT)

        assert [module.package.lineNumber, module.package.columnNumber] == [1, 1]

        def star = module.starImports.find { it.packageName == 'java.nio.' }
        assert star != null
        assert [star.lineNumber, star.columnNumber] == [3, 1]
        assert [star.lastLineNumber, star.lastColumnNumber] == [3, 18]

        def printlnStmt = module.statementBlock.statements[0]
        assert [printlnStmt.lineNumber, printlnStmt.columnNumber] == [7, 1]
    }

    @Test
    void allCarriageReturnStatementsResetLineAndColumn() {
        // def a = 1 / def b = 2 / <blank> / def c = 3
        def positions = statementPositions('def a = 1\rdef b = 2\r\rdef c = 3')
        assert positions == [[1, 1], [2, 1], [4, 1]]
    }

    @Test
    void statementAfterBlockCommentWithBareCarriageReturns() {
        def positions = statementPositions('/*a\rb\rc*/\rprintln 1')
        assert positions == [[4, 1]]
    }

    @Test
    void columnAfterBareCarriageReturnCountsSpacesFromColumnOne() {
        def positions = statementPositions('\r    x = 1')
        assert positions == [[2, 5]]
    }

    @Test
    void tripleQuotedStringContainingBareCarriageReturn() {
        ModuleNode module = module("def s = '''a\rb'''\rnext = 1")
        def declaration = declaration(module, 0)
        def literal = declaration.rightExpression

        // "def s = " is 8 characters; the literal starts at column 9.
        // Line 2 is "b'''" (the break character counts toward the end column).
        assert [literal.lineNumber, literal.columnNumber, literal.lastLineNumber, literal.lastColumnNumber] == [1, 9, 2, 5]
        assert positionsOf(module) == [[1, 1], [3, 1]]
    }

    @Test
    void gstringPartsContainingBareCarriageReturn() {
        ModuleNode module = module('def s = """a\r${1}b"""\rnext = 1')
        assert positionsOf(module) == [[1, 1], [3, 1]]

        def gstring = declaration(module, 0).rightExpression
        assert [gstring.lineNumber, gstring.lastLineNumber] == [1, 2]
    }

    @Test
    void slashyStringContainingBareCarriageReturn() {
        ModuleNode module = module('def s = /a\rb/\rnext = 1')
        def literal = declaration(module, 0).rightExpression
        assert [literal.lineNumber, literal.lastLineNumber] == [1, 2]
        assert positionsOf(module) == [[1, 1], [3, 1]]
    }

    @Test
    void syntaxErrorAfterBareCarriageReturn() {
        // Same caret as a LF file. The '?' continues the previous expression,
        // so the reported column is the second '?', not column 1.
        def cr = syntaxError('a = 1\r???')
        def lf = syntaxError('a = 1\n???')
        assert [cr.startLine, cr.startColumn] == [lf.startLine, lf.startColumn]
        assert cr.startLine == 2
    }

    @Test
    void syntaxErrorAfterCommentWithBareCarriageReturns() {
        def cr = syntaxError('/*a\rb*/\r???')
        def lf = syntaxError('/*a\nb*/\n???')
        assert [cr.startLine, cr.startColumn] == [lf.startLine, lf.startColumn]
        assert [cr.startLine, cr.startColumn] == [3, 1]
    }

    @Test
    void stackTraceLineFollowsBareCarriageReturns() {
        def source = "def boom() {\rthrow new RuntimeException('boom')\r}\rboom()\r"
        try {
            new GroovyShell().evaluate(source, 'LineEndings.groovy')
            throw new AssertionError('expected the script to throw')
        } catch (RuntimeException e) {
            def frame = e.stackTrace.find { it.methodName == 'boom' }
            assert frame != null
            assert frame.lineNumber == 2
        }
    }

    @Test
    void tripleQuotedStringMixesCrlfLoneLineFeedAndBareCarriageReturn() {
        // One token, so correction walks a CRLF, a lone LF, and a bare CR.
        ModuleNode module = module("def s = '''a\r\nb\nc\rd'''")
        def literal = declaration(module, 0).rightExpression
        assert [literal.lineNumber, literal.columnNumber, literal.lastLineNumber, literal.lastColumnNumber] == [1, 9, 4, 5]
    }

    @Test
    void gstringMoreFragmentSplitsCarriageReturnFromFollowingLineFeed() {
        // After ${1} each character is its own match. The CR's match ends
        // with LF still outside the token, and a later bare CR forces correction.
        ModuleNode module = module('def s = """x${1}\r\nb\rc"""\nnext = 1')
        def gstring = declaration(module, 0).rightExpression
        assert [gstring.lineNumber, gstring.lastLineNumber] == [1, 3]
        assert positionsOf(module) == [[1, 1], [4, 1]]
    }

    @Test
    void consumeBareCarriageReturnOutsideDfaLoopEndsTheLine() {
        // consume(CharStream) is the rollback and speculative-predicate entry.
        // The DFA loop does not call it. A bare CR ends the line; CR before LF does not.
        def bare = CharStreams.fromString('\rx')
        def bareLexer = new GroovyLangLexer(bare)
        bareLexer.interpreter.consume(bare)
        assert [bareLexer.interpreter.line, bareLexer.interpreter.charPositionInLine] == [2, 0]
        assert bare.LA(1) == 'x'.codePointAt(0)

        def paired = CharStreams.fromString('\r\n')
        def pairedLexer = new GroovyLangLexer(paired)
        pairedLexer.interpreter.consume(paired)
        assert [pairedLexer.interpreter.line, pairedLexer.interpreter.charPositionInLine] == [1, 1]
        assert paired.LA(1) == '\n'.codePointAt(0)

        def plain = CharStreams.fromString('ab')
        def plainLexer = new GroovyLangLexer(plain)
        plainLexer.interpreter.consume(plain)
        assert [plainLexer.interpreter.line, plainLexer.interpreter.charPositionInLine] == [1, 1]
        assert plain.LA(1) == 'b'.codePointAt(0)
    }

    @Test
    void emptyMatchOnBareCarriageReturnFileDoesNotMoveTheInput() {
        def lexer = new GroovyLangLexer(CharStreams.fromString('a\rb'))
        while (lexer.nextToken().type != Token.EOF) {
        }
        def input = lexer.inputStream
        assert input.LA(1) == Token.EOF
        int atEnd = input.index()
        assert lexer.interpreter.match(input, 0) == Token.EOF
        assert input.index() == atEnd
    }

    @Test
    void crlfLineEndingsStayOneBreakPerPair() {
        ModuleNode module = module("package foo\r\nimport java.nio.*\r\n\r\nprintln 'x'")
        def star = module.starImports.find { it.packageName == 'java.nio.' }
        assert [star.lineNumber, star.columnNumber] == [2, 1]
        assert positionsOf(module) == [[4, 1]]
    }

    private static ModuleNode module(String source) {
        ParserPlugin.buildAST(source, null, null, null)
    }

    private static List<List<Integer>> statementPositions(String source) {
        positionsOf(module(source))
    }

    private static List<List<Integer>> positionsOf(ModuleNode module) {
        module.statementBlock.statements.collect { ASTNode node ->
            [node.lineNumber, node.columnNumber]
        }
    }

    private static DeclarationExpression declaration(ModuleNode module, int index) {
        module.statementBlock.statements[index].expression
    }

    private static syntaxError(String source) {
        try {
            new GroovyShell().parse(source, 'LineEndings.groovy')
            throw new AssertionError('expected a syntax error')
        } catch (MultipleCompilationErrorsException e) {
            return e.errorCollector.getSyntaxError(0)
        }
    }
}
