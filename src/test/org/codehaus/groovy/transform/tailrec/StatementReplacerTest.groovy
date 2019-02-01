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
package org.codehaus.groovy.transform.tailrec

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.expr.BooleanExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.stmt.*
import org.junit.Before
import org.junit.Test

class StatementReplacerTest {

    StatementReplacer replacer
    def replacements = [:]
    Closure<Boolean> when = { Statement node -> replacements.containsKey node }
    Closure<ASTNode> replaceWith = { Statement node -> replacements[node] }

    @Before
    void init() {
        replacer = new StatementReplacer(when: when, replaceWith: replaceWith)
    }

    @Test
    void replaceSingleStatementInBlock() {
        def toReplace = aReturnStatement("old")
        def replacement = aReturnStatement("new")
        def block = new BlockStatement()
        block.addStatement(aReturnStatement("before"))
        block.addStatement(toReplace)
        block.addStatement(aReturnStatement("after"))

        replacements[toReplace] = replacement
        replacer.replaceIn(block)

        assert block.statements[1] == replacement
        assert block.statements.size() == 3
    }

    @Test
    void replacingElementCopiesSourcePosition() {
        def toReplace = aReturnStatement("old")
        toReplace.lineNumber = 42
        def replacement = aReturnStatement("new")
        def block = new BlockStatement()
        block.addStatement(toReplace)

        replacements[toReplace] = replacement
        replacer.replaceIn(block)

        assert block.statements[0] == replacement
        assert replacement.lineNumber == toReplace.lineNumber
    }

    @Test
    void replaceByCondition() {
        def toReplace = aReturnStatement("old")
        def replacement = aReturnStatement("new")
        def block = new BlockStatement()
        block.addStatement(aReturnStatement("before"))
        block.addStatement(toReplace)
        block.addStatement(aReturnStatement("after"))

        replacer = new StatementReplacer(when: { it == toReplace }, replaceWith: {
            assert it == toReplace
            replacement
        })
        replacer.replaceIn(block)

        assert block.statements[1] == replacement
        assert block.statements.size() == 3
    }

    @Test
    void replaceTwoStatementsInBlock() {
        def toReplace1 = aReturnStatement("old1")
        def replacement1 = aReturnStatement("new1")
        def toReplace2 = aReturnStatement("old2")
        def replacement2 = aReturnStatement("new2")
        def block = new BlockStatement()
        block.addStatement(aReturnStatement("before"))
        block.addStatement(toReplace1)
        block.addStatement(toReplace2)
        block.addStatement(aReturnStatement("after"))

        replacements = [(toReplace1): replacement1, (toReplace2): replacement2]
        replacer.replaceIn(block)

        assert block.statements[1] == replacement1
        assert block.statements[2] == replacement2
        assert block.statements.size() == 4
    }

    @Test
    void replaceIfBlock() {
        def toReplace = aReturnStatement("old")
        def replacement = aReturnStatement("new")
        def ifStatement = new IfStatement(aBooleanExpression(true), toReplace, EmptyStatement.INSTANCE)

        replacements[toReplace] = replacement
        replacer.replaceIn(ifStatement)

        assert ifStatement.ifBlock == replacement
    }

    @Test
    void replaceElseBlock() {
        def toReplace = aReturnStatement("old")
        def replacement = aReturnStatement("new")
        def ifStatement = new IfStatement(aBooleanExpression(true), EmptyStatement.INSTANCE, toReplace)

        replacements[toReplace] = replacement
        replacer.replaceIn(ifStatement)

        assert ifStatement.elseBlock == replacement
    }

    @Test
    void replaceForLoopBlock() {
        def toReplace = aReturnStatement("old")
        def replacement = aReturnStatement("new")
        def forLoop = new ForStatement(new Parameter(ClassHelper.int_TYPE, "a"), aConstant(0), toReplace)

        replacements[toReplace] = replacement
        replacer.replaceIn(forLoop)

        assert forLoop.loopBlock == replacement
    }

    @Test
    void replaceWhileLoopBlock() {
        def toReplace = aReturnStatement("old")
        def replacement = aReturnStatement("new")
        def whileLoop = new WhileStatement(aBooleanExpression(true), toReplace)

        replacements[toReplace] = replacement
        replacer.replaceIn(whileLoop)

        assert whileLoop.loopBlock == replacement
    }

    @Test
    void replaceDoWhileLoopBlock() {
        def toReplace = aReturnStatement("old")
        def replacement = aReturnStatement("new")
        def doWhileLoop = new DoWhileStatement(aBooleanExpression(true), toReplace)

        replacements[toReplace] = replacement
        replacer.replaceIn(doWhileLoop)

        assert doWhileLoop.loopBlock == replacement
    }

    @Test
    void inClosureAttributeIsTrueInClosure() {
        ClosureExpression closure = new AstBuilder().buildFromSpec {
            closure {
                parameters {
                    parameter 'parm': Object.class
                }
                block {
                    expression {
                        constant 'old'
                    }
                }
            }
        }[0]

        def replacer = new StatementReplacer(
                when: { node, inClosure -> inClosure && node instanceof ExpressionStatement },
                replaceWith: { new ExpressionStatement(aConstant('new')) }
        )
        replacer.replaceIn(closure)

        assert closure.code.statements[0].expression.text == 'new'
    }

    @Test
    void inClosureAttributeIsFalseOutsideClosure() {
        BlockStatement block = new AstBuilder().buildFromSpec {
            block {
                expression {
                    constant 'old'
                }
            }
        }[0]

        def replacer = new StatementReplacer(
                when: { node, inClosure -> inClosure && node instanceof ExpressionStatement },
                replaceWith: { assert false, 'Must not get here' }
        )
        replacer.replaceIn(block)
    }

    def aReturnStatement(value) {
        new ReturnStatement(aConstant(value))
    }

    def aConstant(value) {
        new ConstantExpression(value)
    }

    def aBooleanExpression(value) {
        new BooleanExpression(new ConstantExpression(value))
    }
}
