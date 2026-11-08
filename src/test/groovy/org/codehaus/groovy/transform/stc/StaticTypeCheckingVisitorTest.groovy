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
package org.codehaus.groovy.transform.stc

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.SwitchExpression
import org.codehaus.groovy.ast.stmt.CaseStatement
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.codehaus.groovy.ast.stmt.SwitchStatement
import org.codehaus.groovy.control.SourceUnit
import org.junit.jupiter.api.Test

import java.lang.reflect.Modifier

import static org.codehaus.groovy.ast.tools.GeneralUtils.classX
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX
import static org.junit.jupiter.api.Assertions.assertFalse

/**
 * Cases for resolving the enclosing switch while typing a case label.
 */
final class StaticTypeCheckingVisitorTest {

    @Test
    void testReplacedSwitchSelectorDoesNotThrow() {
        StaticTypeCheckingVisitor visitor = visitor()
        Expression original = varX('x')
        SwitchStatement statement = new SwitchStatement(original)
        statement.addCase(kase(constX(1)))
        visitor.typeCheckingContext.pushEnclosingSwitchStatement(statement)
        statement.expression = varX('y')

        visitor.visitCaseStatement(statement.caseStatements[0])

        assertFalse(visitor.sourceUnit.errorCollector.hasErrors())
    }

    @Test
    void testUnrelatedEnclosingSwitchExpressionDoesNotThrow() {
        StaticTypeCheckingVisitor visitor = visitor()
        SwitchExpression outer = new SwitchExpression(varX('outer'))
        visitor.typeCheckingContext.pushEnclosingSwitchExpression(outer)
        Expression original = varX('x')
        SwitchStatement statement = new SwitchStatement(original)
        statement.addCase(kase(constX(1)))
        visitor.typeCheckingContext.pushEnclosingSwitchStatement(statement)
        statement.expression = varX('y')

        visitor.visitCaseStatement(statement.caseStatements[0])

        assertFalse(visitor.sourceUnit.errorCollector.hasErrors())
    }

    @Test
    void testCaseMissingFromOwningSwitchDoesNotThrow() {
        StaticTypeCheckingVisitor visitor = visitor()
        SwitchStatement statement = new SwitchStatement(varX('x'))
        visitor.typeCheckingContext.pushEnclosingSwitchStatement(statement)

        visitor.visitCaseStatement(kase(constX(1)))

        assertFalse(visitor.sourceUnit.errorCollector.hasErrors())
    }

    @Test
    void testLastCaseOfSwitchStatementHasNoNextLabel() {
        StaticTypeCheckingVisitor visitor = visitor()
        SwitchStatement statement = new SwitchStatement(varX('x'))
        statement.addCase(kase(constX(1)))
        visitor.typeCheckingContext.pushEnclosingSwitchStatement(statement)

        visitor.visitCaseStatement(statement.caseStatements[0])

        assertFalse(visitor.sourceUnit.errorCollector.hasErrors())
    }

    @Test
    void testSwitchStatementFallThroughReadsNextLabel() {
        StaticTypeCheckingVisitor visitor = visitor()
        SwitchStatement statement = new SwitchStatement(varX('x'))
        statement.addCase(kase(constX(1)))
        statement.addCase(kase(classX(ClassHelper.Integer_TYPE)))
        visitor.typeCheckingContext.pushEnclosingSwitchStatement(statement)

        visitor.visitCaseStatement(statement.caseStatements[0])

        assertFalse(visitor.sourceUnit.errorCollector.hasErrors())
    }

    @Test
    void testSwitchExpressionWithoutEnclosingStatementReadsItsOwnCases() {
        StaticTypeCheckingVisitor visitor = visitor()
        SwitchExpression expression = new SwitchExpression(varX('x'))
        expression.addCase(kase(constX(1)))
        expression.addCase(kase(constX(2)))
        visitor.typeCheckingContext.pushEnclosingSwitchExpression(expression)

        visitor.visitCaseStatement(expression.caseStatements[0])
        visitor.visitCaseStatement(expression.caseStatements[1])

        assertFalse(visitor.sourceUnit.errorCollector.hasErrors())
    }

    private static StaticTypeCheckingVisitor visitor() {
        SourceUnit source = SourceUnit.create('C.groovy', 'class C {}')
        ClassNode classNode = new ClassNode('C', Modifier.PUBLIC, ClassHelper.OBJECT_TYPE)
        new StaticTypeCheckingVisitor(source, classNode)
    }

    private static CaseStatement kase(Expression label) {
        new CaseStatement(label, EmptyStatement.INSTANCE)
    }
}
