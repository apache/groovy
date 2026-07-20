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
package org.codehaus.groovy.transform

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.CastExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ForStatement
import org.codehaus.groovy.ast.tools.GeneralUtils
import org.junit.jupiter.api.Test

/**
 * Unit tests for {@link AsyncTransformHelper} AST construction helpers.
 */
final class AsyncTransformHelperTest {

    @Test
    void testBuildAwaitCallSingleArgUsesAwaitWithObjectCast() {
        def expr = AsyncTransformHelper.buildAwaitCall(new ConstantExpression(1))
        assert methodName(expr) == 'await'
        assert ownerName(expr).endsWith('AsyncSupport')
        def arg0 = ((StaticMethodCallExpression) expr).arguments.expressions[0]
        assert arg0 instanceof CastExpression
        assert arg0.type == ClassHelper.OBJECT_TYPE
    }

    @Test
    void testBuildAwaitCallMultiArgUsesAwaitableAllThenAwait() {
        def args = new ArgumentListExpression(
                new ConstantExpression(1),
                new ConstantExpression(2))
        def expr = AsyncTransformHelper.buildAwaitCall(args)
        assert methodName(expr) == 'await'
        def arg0 = ((StaticMethodCallExpression) expr).arguments.expressions[0]
        assert methodName(arg0) == 'all'
        assert ownerName(arg0).endsWith('Awaitable')
    }

    @Test
    void testContainsYieldReturnAndDeferDoNotDescendIntoNestedClosures() {
        def yieldCall = AsyncTransformHelper.buildYieldReturnCall(new ConstantExpression(1))
        def deferCall = AsyncTransformHelper.buildDeferCall(
                new ClosureExpression(Parameter.EMPTY_ARRAY, EmptyStatement.INSTANCE))

        def outerWithYield = new BlockStatement()
        outerWithYield.addStatement(new ExpressionStatement(yieldCall))
        assert AsyncTransformHelper.containsYieldReturn(outerWithYield)
        assert !AsyncTransformHelper.containsDefer(outerWithYield)

        def outerWithDefer = new BlockStatement()
        outerWithDefer.addStatement(new ExpressionStatement(deferCall))
        assert AsyncTransformHelper.containsDefer(outerWithDefer)
        assert !AsyncTransformHelper.containsYieldReturn(outerWithDefer)

        // Nested inside a child closure — must be ignored
        def nested = new ClosureExpression(Parameter.EMPTY_ARRAY, outerWithYield)
        def outerOnlyNested = new BlockStatement()
        outerOnlyNested.addStatement(new ExpressionStatement(nested))
        assert !AsyncTransformHelper.containsYieldReturn(outerOnlyNested)
    }

    @Test
    void testWrapWithDeferScopeStructure() {
        def body = EmptyStatement.INSTANCE
        def wrapped = AsyncTransformHelper.wrapWithDeferScope(body)
        assert wrapped instanceof BlockStatement
        assert wrapped.statements.size() == 2
    }

    @Test
    void testWrapForAwaitLoopUsesUniqueSyntheticNames() {
        def collection = new ConstantExpression([1, 2, 3])
        def loop1 = new ForStatement(
                new Parameter(org.codehaus.groovy.ast.ClassHelper.OBJECT_TYPE, 'item'),
                collection,
                EmptyStatement.INSTANCE)
        def loop2 = new ForStatement(
                new Parameter(org.codehaus.groovy.ast.ClassHelper.OBJECT_TYPE, 'item'),
                collection,
                EmptyStatement.INSTANCE)

        def block1 = AsyncTransformHelper.wrapForAwaitLoop(loop1) as BlockStatement
        def block2 = AsyncTransformHelper.wrapForAwaitLoop(loop2) as BlockStatement

        def name1 = extractDeclaredName(block1)
        def name2 = extractDeclaredName(block2)
        assert name1.startsWith('$__forAwaitSource_')
        assert name2.startsWith('$__forAwaitSource_')
        assert name1 != name2
    }

    @Test
    void testTransformAsyncClosurePlainAndGenerator() {
        def plain = new ClosureExpression(Parameter.EMPTY_ARRAY, EmptyStatement.INSTANCE)
        def plainCall = AsyncTransformHelper.transformAsyncClosure(plain)
        assert methodName(plainCall) == 'async'

        def yieldCall = AsyncTransformHelper.buildYieldReturnCall(new ConstantExpression(1))
        def yieldBody = new BlockStatement()
        yieldBody.addStatement(new ExpressionStatement(yieldCall))
        def genClosure = new ClosureExpression(Parameter.EMPTY_ARRAY, yieldBody)
        def genCall = AsyncTransformHelper.transformAsyncClosure(genClosure)
        assert methodName(genCall) == 'asyncGenerator'
    }

    @Test
    void testTransformAsyncClosureWithDefer() {
        def deferCall = AsyncTransformHelper.buildDeferCall(
                new ClosureExpression(Parameter.EMPTY_ARRAY, EmptyStatement.INSTANCE))
        def body = new BlockStatement()
        body.addStatement(new ExpressionStatement(deferCall))
        def closure = new ClosureExpression(Parameter.EMPTY_ARRAY, body)
        def call = AsyncTransformHelper.transformAsyncClosure(closure)
        assert methodName(call) == 'async'
    }

    @Test
    void testCreateGenParamAndBuilders() {
        def p = AsyncTransformHelper.createGenParam()
        assert p.name == '$__asyncGen__'

        assert methodName(AsyncTransformHelper.buildAsyncCall(
                new ClosureExpression(Parameter.EMPTY_ARRAY, EmptyStatement.INSTANCE))) == 'async'
        assert methodName(AsyncTransformHelper.buildAsyncGeneratorCall(
                new ClosureExpression(Parameter.EMPTY_ARRAY, EmptyStatement.INSTANCE))) == 'asyncGenerator'
        assert methodName(AsyncTransformHelper.buildToIterableCall(new ConstantExpression([]))) == 'toIterable'
        assert methodName(AsyncTransformHelper.buildCloseIterableCall(GeneralUtils.varX('x'))) == 'closeIterable'
    }

    private static String methodName(Object expr) {
        if (expr instanceof StaticMethodCallExpression) {
            return expr.method
        }
        if (expr instanceof MethodCallExpression) {
            return expr.methodAsString
        }
        throw new AssertionError("unexpected expression: ${expr?.getClass()?.name}")
    }

    private static String ownerName(Object expr) {
        if (expr instanceof StaticMethodCallExpression) {
            return expr.ownerType.name
        }
        if (expr instanceof MethodCallExpression) {
            return expr.objectExpression.type.name
        }
        throw new AssertionError("unexpected expression: ${expr?.getClass()?.name}")
    }

    private static String extractDeclaredName(BlockStatement block) {
        def declStmt = block.statements[0] as ExpressionStatement
        def decl = declStmt.expression
        def left = decl.leftExpression
        if (left instanceof VariableExpression) {
            return left.name
        }
        return decl.variableExpression.name
    }
}
