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
package org.codehaus.groovy.transform;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;

import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

/**
 * Shared AST utilities for the {@code async}/{@code await} language feature.
 * <p>
 * Both the ANTLR4 parser ({@code AstBuilder}) and the annotation-driven
 * {@link AsyncASTTransformation} need to detect and rewrite
 * {@code yield return} and {@code defer} statements inside async bodies.
 * This helper class centralises those operations so that the two code paths
 * stay in sync and no logic is duplicated.
 * <p>
 * <b>Yield-return rewriting</b> works in two steps:
 * <ol>
 *   <li>{@link #containsYieldReturn(Statement)} scans a statement tree for
 *       {@code AsyncSupport.yieldReturn(expr)} calls to determine whether
 *       the body is an async generator.</li>
 *   <li>{@link #injectGenParamIntoYieldReturnCalls(Statement, Parameter)}
 *       rewrites each {@code yieldReturn(expr)} call to
 *       {@code yieldReturn($__asyncGen__, expr)}, injecting the synthetic
 *       generator parameter so that the generator instance is available
 *       at runtime without {@code ThreadLocal}.</li>
 * </ol>
 * <p>
 * <b>Defer scope injection</b> is handled by:
 * <ul>
 *   <li>{@link #containsDefer(Statement)} — scans for
 *       {@code AsyncSupport.defer(...)} calls.</li>
 *   <li>{@link #wrapWithDeferScope(Statement)} — wraps the body in
 *       {@code def $__deferScope__ = createDeferScope(); try { body } finally { executeDeferScope($__deferScope__) }}</li>
 * </ul>
 * <p>
 * Neither the yield-return nor defer scanners descend into nested
 * {@link ClosureExpression}s because each nested {@code async { ... }}
 * closure manages its own generator and defer scope independently.
 *
 * @see AsyncASTTransformation
 * @since 6.0.0
 */
public final class AsyncTransformHelper {

    /** Fully-qualified class name of the internal runtime support class. */
    public static final String ASYNC_SUPPORT_CLASS = "org.apache.groovy.runtime.async.AsyncSupport";

    /** {@link ClassNode} for {@code AsyncSupport}, usable in AST construction. */
    public static final ClassNode ASYNC_SUPPORT_TYPE = ClassHelper.make(ASYNC_SUPPORT_CLASS);

    /** Name of the synthetic parameter injected into async generator closures. */
    public static final String ASYNC_GEN_PARAM_NAME = "$__asyncGen__";

    /** Runtime method name for awaiting a value. */
    public static final String AWAIT_METHOD = "await";

    /** Runtime method name for yielding a value from a generator. */
    public static final String YIELD_RETURN_METHOD = "yieldReturn";

    /** Runtime method name for wrapping a closure as an async factory. */
    public static final String WRAP_ASYNC_METHOD = "wrapAsync";

    /** Runtime method name for wrapping a generator closure as a stream factory. */
    public static final String WRAP_ASYNC_GENERATOR_METHOD = "wrapAsyncGenerator";

    /** Runtime method name for directly generating an async stream. */
    public static final String GENERATE_ASYNC_STREAM_METHOD = "generateAsyncStream";

    /** Runtime method name for executing a closure asynchronously. */
    public static final String EXECUTE_ASYNC_METHOD = "executeAsync";

    /** Runtime method name for executing a void closure asynchronously. */
    public static final String EXECUTE_ASYNC_VOID_METHOD = "executeAsyncVoid";

    /** Runtime method name for converting a source to an async stream. */
    public static final String TO_ASYNC_STREAM_METHOD = "toAsyncStream";

    /** Runtime method name for creating a defer scope. */
    public static final String CREATE_DEFER_SCOPE_METHOD = "createDeferScope";

    /** Runtime method name for registering a deferred action. */
    public static final String DEFER_METHOD = "defer";

    /** Runtime method name for executing all deferred actions. */
    public static final String EXECUTE_DEFER_SCOPE_METHOD = "executeDeferScope";

    /** Name of the synthetic variable holding the defer scope. */
    public static final String DEFER_SCOPE_VAR = "$__deferScope__";

    private AsyncTransformHelper() { }

    /**
     * Checks whether the given statement tree contains any
     * {@code AsyncSupport.yieldReturn()} calls, indicating that the enclosing
     * body is an async generator (which should produce an
     * {@link groovy.concurrent.AsyncStream AsyncStream} rather than an
     * {@link groovy.concurrent.Awaitable Awaitable}).
     * <p>
     * The scan does <em>not</em> descend into nested {@link ClosureExpression}s
     * because each nested {@code async { ... }} closure handles its own
     * generator wrapping independently.
     *
     * @param code the statement tree to scan
     * @return {@code true} if at least one {@code yieldReturn} call is found
     */
    public static boolean containsYieldReturn(Statement code) {
        boolean[] found = {false};
        code.visit(new CodeVisitorSupport() {
            @Override
            public void visitStaticMethodCallExpression(StaticMethodCallExpression call) {
                if (YIELD_RETURN_METHOD.equals(call.getMethod())
                        && ASYNC_SUPPORT_CLASS.equals(call.getOwnerType().getName())) {
                    found[0] = true;
                }
                if (!found[0]) {
                    super.visitStaticMethodCallExpression(call);
                }
            }

            @Override
            public void visitClosureExpression(ClosureExpression expression) {
                // Do not descend into nested closures — each manages its own generator
            }
        });
        return found[0];
    }

    /**
     * Rewrites {@code AsyncSupport.yieldReturn(expr)} calls to
     * {@code AsyncSupport.yieldReturn($__asyncGen__, expr)}, injecting the
     * synthetic generator parameter as the first argument.
     * <p>
     * This enables ThreadLocal-free yield semantics: the
     * {@link org.apache.groovy.runtime.async.AsyncStreamGenerator} instance is
     * passed explicitly through the closure parameter rather than being
     * stored in a thread-local variable.
     * <p>
     * Does <em>not</em> descend into nested {@link ClosureExpression}s.
     *
     * @param code     the statement tree to transform
     * @param genParam the synthetic {@code $__asyncGen__} parameter whose
     *                 variable reference will be injected
     */
    public static void injectGenParamIntoYieldReturnCalls(Statement code, Parameter genParam) {
        code.visit(new CodeVisitorSupport() {
            @Override
            public void visitExpressionStatement(ExpressionStatement stmt) {
                Expression expr = stmt.getExpression();
                if (expr instanceof StaticMethodCallExpression smce
                        && YIELD_RETURN_METHOD.equals(smce.getMethod())
                        && ASYNC_SUPPORT_CLASS.equals(smce.getOwnerType().getName())) {
                    VariableExpression genRef = varX(ASYNC_GEN_PARAM_NAME);
                    genRef.setAccessedVariable(genParam);
                    ArgumentListExpression newArgs = new ArgumentListExpression();
                    newArgs.addExpression(genRef);
                    if (smce.getArguments() instanceof ArgumentListExpression argList) {
                        for (Expression arg : argList.getExpressions()) {
                            newArgs.addExpression(arg);
                        }
                    }
                    StaticMethodCallExpression replacement =
                            new StaticMethodCallExpression(smce.getOwnerType(), YIELD_RETURN_METHOD, newArgs);
                    replacement.setSourcePosition(smce);
                    stmt.setExpression(replacement);
                }
                super.visitExpressionStatement(stmt);
            }

            @Override
            public void visitClosureExpression(ClosureExpression expression) {
                // Do not descend into nested closures
            }
        });
    }

    /**
     * Creates the synthetic {@code $__asyncGen__} parameter used by async
     * generator closures.  The parameter is dynamically typed.
     *
     * @return a new {@link Parameter} instance
     */
    public static Parameter createGenParam() {
        return new Parameter(ClassHelper.OBJECT_TYPE, ASYNC_GEN_PARAM_NAME);
    }

    /**
     * Checks whether the given statement tree contains any
     * {@code AsyncSupport.defer()} calls, indicating that the enclosing
     * method uses the {@code defer} statement and needs a try-finally
     * wrapper to execute deferred actions on exit.
     * <p>
     * Does <em>not</em> descend into nested {@link ClosureExpression}s.
     *
     * @param code the statement tree to scan
     * @return {@code true} if at least one {@code defer} call is found
     */
    public static boolean containsDefer(Statement code) {
        boolean[] found = {false};
        code.visit(new CodeVisitorSupport() {
            @Override
            public void visitStaticMethodCallExpression(StaticMethodCallExpression call) {
                if (DEFER_METHOD.equals(call.getMethod())
                        && ASYNC_SUPPORT_CLASS.equals(call.getOwnerType().getName())) {
                    found[0] = true;
                }
                if (!found[0]) {
                    super.visitStaticMethodCallExpression(call);
                }
            }

            @Override
            public void visitClosureExpression(ClosureExpression expression) {
                // Do not descend into nested closures
            }
        });
        return found[0];
    }

    /**
     * Wraps the given statement body with defer scope management.
     * Generates the equivalent of:
     * <pre>{@code
     * def $__deferScope__ = AsyncSupport.createDeferScope()
     * try {
     *     <originalBody>
     * } finally {
     *     AsyncSupport.executeDeferScope($__deferScope__)
     * }
     * }</pre>
     *
     * @param body the original method or closure body
     * @return a new block statement with defer scope initialization and try-finally
     */
    public static Statement wrapWithDeferScope(Statement body) {
        // def $__deferScope__ = AsyncSupport.createDeferScope()
        Expression createCall = callX(ASYNC_SUPPORT_TYPE,
                CREATE_DEFER_SCOPE_METHOD, ArgumentListExpression.EMPTY_ARGUMENTS);
        ExpressionStatement initStmt = new ExpressionStatement(
                new DeclarationExpression(
                        varX(DEFER_SCOPE_VAR),
                        Token.newSymbol(Types.ASSIGN, -1, -1),
                        createCall));

        // finally { AsyncSupport.executeDeferScope($__deferScope__) }
        Expression executeCall = callX(ASYNC_SUPPORT_TYPE,
                EXECUTE_DEFER_SCOPE_METHOD,
                args(varX(DEFER_SCOPE_VAR)));
        TryCatchStatement tryCatch = new TryCatchStatement(
                body,
                block(new ExpressionStatement(executeCall)));

        return block(initStmt, tryCatch);
    }
}
