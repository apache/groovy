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

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.PropertyNode
import org.codehaus.groovy.ast.VariableScope
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ClosureListExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MethodPointerExpression
import org.codehaus.groovy.ast.expr.MethodReferenceExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.expr.SwitchExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.CaseStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ForStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.junit.jupiter.api.Test

import java.lang.reflect.Modifier

import static org.codehaus.groovy.ast.ClassHelper.OBJECT_TYPE
import static org.codehaus.groovy.ast.ClassHelper.VOID_TYPE
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertNotSame
import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertSame
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * The bugs suite covers compilation. These tests cover the contracts that
 * compilation does not show: members without a sentinel keep their nodes,
 * and an implicit method pointer is not turned into the shared {@code this}.
 */
final class SharedVariableExpressionRewriterTest {

    private static final Expression THIS = VariableExpression.THIS_EXPRESSION
    private static final Expression SUPER = VariableExpression.SUPER_EXPRESSION

    // GROOVY-12441
    @Test
    void testNullNodesAndUntouchedMembersKeepIdentity() {
        SharedVariableExpressionRewriter.rewrite((MethodNode) null)
        SharedVariableExpressionRewriter.rewrite((FieldNode) null)
        SharedVariableExpressionRewriter.rewrite((PropertyNode) null)
        SharedVariableExpressionRewriter.rewriteInitializers(null)

        ConstantExpression keep = new ConstantExpression('k')
        MethodPointerExpression implicit = new MethodPointerExpression(null, new ConstantExpression('m'))
        MethodNode bare = method('bare', new BlockStatement([
                new ExpressionStatement(keep),
                new ExpressionStatement(implicit)
        ], new VariableScope()))
        SharedVariableExpressionRewriter.rewrite(bare)
        assertSame(keep, ((ExpressionStatement) ((BlockStatement) bare.code).statements[0]).expression)
        assertNull(implicit.@expression)

        ClassNode owner = new ClassNode('C', Modifier.PUBLIC, OBJECT_TYPE)
        FieldNode sameInit = new FieldNode('b', Modifier.PUBLIC, OBJECT_TYPE, owner, keep)
        SharedVariableExpressionRewriter.rewrite(sameInit)
        assertSame(keep, sameInit.initialExpression)

        BlockStatement getter = new BlockStatement([new ExpressionStatement(keep)], new VariableScope())
        SharedVariableExpressionRewriter.rewrite(new PropertyNode('g', Modifier.PUBLIC, OBJECT_TYPE, owner, null, getter, null))
        assertSame(keep, ((ExpressionStatement) getter.statements[0]).expression)
    }

    // GROOVY-12441
    @Test
    void testSentinelAtEachHookIsCopied() {
        Parameter withDefault = new Parameter(OBJECT_TYPE, 'b', THIS)
        ClosureExpression closure = new ClosureExpression(null, new ReturnStatement(THIS))
        SwitchExpression switchExpression = new SwitchExpression(new ConstantExpression(1),
                [new CaseStatement(new ConstantExpression(1), new ReturnStatement(SUPER))], null)
        MethodPointerExpression explicit = new MethodPointerExpression(THIS, new ConstantExpression('m'))
        MethodPointerExpression implicitNamed = new MethodPointerExpression(null, SUPER)
        MethodReferenceExpression implicitReference = new MethodReferenceExpression(null, new ConstantExpression('m'))
        VariableScope loopScope = new VariableScope()
        ClosureListExpression loop = new ClosureListExpression([THIS, new ConstantExpression(true), new ConstantExpression(1)])
        loop.variableScope = loopScope

        MethodNode method = new MethodNode('m', Modifier.PUBLIC, VOID_TYPE, [withDefault] as Parameter[], ClassNode.EMPTY_ARRAY,
                new BlockStatement([
                        new ExpressionStatement(closure),
                        new ExpressionStatement(switchExpression),
                        new ExpressionStatement(explicit),
                        new ExpressionStatement(implicitNamed),
                        new ExpressionStatement(implicitReference),
                        new ForStatement(loop, new ExpressionStatement(new ConstantExpression(0)))
                ], new VariableScope()))
        SharedVariableExpressionRewriter.rewrite(method)

        BlockStatement body = (BlockStatement) method.code
        assertCopiedThis(withDefault.initialExpression)
        assertSame(closure, ((ExpressionStatement) body.statements[0]).expression)
        assertCopiedThis(((ReturnStatement) closure.code).expression)
        assertCopiedSuper(((ReturnStatement) switchExpression.caseStatements[0].code).expression)
        assertNull(switchExpression.defaultStatement)
        MethodPointerExpression rewrittenExplicit = (MethodPointerExpression) ((ExpressionStatement) body.statements[2]).expression
        assertCopiedThis(rewrittenExplicit.@expression)
        MethodPointerExpression rewrittenNamed = (MethodPointerExpression) ((ExpressionStatement) body.statements[3]).expression
        assertNull(rewrittenNamed.@expression)
        assertCopiedSuper(rewrittenNamed.methodName)
        MethodReferenceExpression rewrittenReference = (MethodReferenceExpression) ((ExpressionStatement) body.statements[4]).expression
        assertNull(rewrittenReference.@expression)
        ClosureListExpression rewrittenLoop = (ClosureListExpression) ((ForStatement) body.statements[5]).collectionExpression
        assertSame(loopScope, rewrittenLoop.variableScope)
        assertCopiedThis(rewrittenLoop.expressions[0])

        ClassNode owner = new ClassNode('C', Modifier.PUBLIC, OBJECT_TYPE)
        FieldNode sharedInit = new FieldNode('c', Modifier.PUBLIC, OBJECT_TYPE, owner, SUPER)
        SharedVariableExpressionRewriter.rewrite(sharedInit)
        assertCopiedSuper(sharedInit.initialExpression)

        BlockStatement getter = new BlockStatement([new ExpressionStatement(THIS)], new VariableScope())
        BlockStatement setter = new BlockStatement([new ReturnStatement(SUPER)], new VariableScope())
        SharedVariableExpressionRewriter.rewrite(new PropertyNode('both', Modifier.PUBLIC, OBJECT_TYPE, owner, null, getter, setter))
        assertCopiedThis(((ExpressionStatement) getter.statements[0]).expression)
        assertCopiedSuper(((ReturnStatement) setter.statements[0]).expression)

        ExpressionStatement initializer = new ExpressionStatement(THIS)
        owner.objectInitializerStatements << initializer
        SharedVariableExpressionRewriter.rewriteInitializers(owner)
        assertCopiedThis(initializer.expression)
    }

    private static MethodNode method(String name, Statement code) {
        new MethodNode(name, Modifier.PUBLIC, VOID_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, code)
    }

    private static void assertCopiedThis(Expression expression) {
        assertTrue(expression instanceof VariableExpression)
        assertNotSame(THIS, expression)
        assertTrue(((VariableExpression) expression).thisExpression)
        assertFalse(((VariableExpression) expression).superExpression)
    }

    private static void assertCopiedSuper(Expression expression) {
        assertTrue(expression instanceof VariableExpression)
        assertNotSame(SUPER, expression)
        assertTrue(((VariableExpression) expression).superExpression)
    }
}
