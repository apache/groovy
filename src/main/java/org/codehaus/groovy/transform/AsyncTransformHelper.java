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

import groovy.concurrent.Awaitable;
import org.apache.groovy.runtime.async.AsyncSupport;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.stmt.Statement;

import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.castX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.declS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.tryCatchS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

/**
 * Shared AST utilities for the {@code async}/{@code await}/{@code defer} language features.
 * <p>
 * Centralises AST node construction for the parser ({@code AstBuilder}).
 *
 * @since 6.0.0
 */
public final class AsyncTransformHelper {

    private static final ClassNode ASYNC_SUPPORT_TYPE = ClassHelper.makeWithoutCaching(AsyncSupport.class, false);
    private static final ClassNode AWAITABLE_TYPE = ClassHelper.makeWithoutCaching(Awaitable.class, false);
    private static final String DEFER_SCOPE_VAR = "$__deferScope__";

    private static final String ASYNC_GEN_PARAM_NAME = "$__asyncGen__";
    private static final String AWAIT_METHOD = "await";
    private static final String YIELD_RETURN_METHOD = "yieldReturn";
    private static final String ASYNC_METHOD = "async";
    private static final String ASYNC_GENERATOR_METHOD = "asyncGenerator";
    private static final String TO_BLOCKING_ITERABLE_METHOD = "toBlockingIterable";
    private static final String CLOSE_ITERABLE_METHOD = "closeIterable";
    private static final String CREATE_DEFER_SCOPE_METHOD = "createDeferScope";
    private static final String DEFER_METHOD = "defer";
    private static final String EXECUTE_DEFER_SCOPE_METHOD = "executeDeferScope";

    private AsyncTransformHelper() { }

    private static Expression ensureArgs(Expression expr) {
        return expr instanceof ArgumentListExpression ? expr : new ArgumentListExpression(expr);
    }

    /**
     * Builds {@code AsyncSupport.await(arg)} or for multi-arg:
     * {@code AsyncSupport.await(Awaitable.all(arg1, arg2, ...))}.
     */
    public static Expression buildAwaitCall(Expression arg) {
        if (arg instanceof ArgumentListExpression args && args.getExpressions().size() > 1) {
            Expression allCall = callX(classX(AWAITABLE_TYPE), "all", args);
            return callX(ASYNC_SUPPORT_TYPE, AWAIT_METHOD, new ArgumentListExpression(allCall));
        }
        // Cast to Object to force dynamic dispatch to the await(Object) overload,
        // avoiding ambiguity when the argument implements multiple async interfaces
        // (e.g., CompletableFuture implements both CompletionStage and Future)
        return callX(ASYNC_SUPPORT_TYPE, AWAIT_METHOD,
                new ArgumentListExpression(castX(ClassHelper.OBJECT_TYPE, arg)));
    }

    /**
     * Builds {@code AsyncSupport.defer($__deferScope__, action)}.
     */
    public static Expression buildDeferCall(Expression action) {
        return callX(ASYNC_SUPPORT_TYPE, DEFER_METHOD,
                args(varX(DEFER_SCOPE_VAR), action));
    }

    /**
     * Builds {@code AsyncSupport.yieldReturn($__asyncGen__, expr)}.
     */
    public static Expression buildYieldReturnCall(Expression arg) {
        return callX(ASYNC_SUPPORT_TYPE, YIELD_RETURN_METHOD,
                args(varX(ASYNC_GEN_PARAM_NAME), arg));
    }

    /**
     * Builds {@code AsyncSupport.async(closure)} — starts immediately, returns Awaitable.
     */
    public static Expression buildAsyncCall(Expression closure) {
        return callX(ASYNC_SUPPORT_TYPE, ASYNC_METHOD, ensureArgs(closure));
    }

    /**
     * Builds {@code AsyncSupport.asyncGenerator(closure)} — starts immediately, returns Iterable.
     */
    public static Expression buildAsyncGeneratorCall(Expression closure) {
        return callX(ASYNC_SUPPORT_TYPE, ASYNC_GENERATOR_METHOD, ensureArgs(closure));
    }

    /**
     * Builds {@code AsyncSupport.toBlockingIterable(source)}.
     */
    public static Expression buildToBlockingIterableCall(Expression source) {
        return callX(ASYNC_SUPPORT_TYPE, TO_BLOCKING_ITERABLE_METHOD, ensureArgs(source));
    }

    /**
     * Builds {@code AsyncSupport.closeIterable(source)}.
     */
    public static Expression buildCloseIterableCall(Expression source) {
        return callX(ASYNC_SUPPORT_TYPE, CLOSE_ITERABLE_METHOD, ensureArgs(source));
    }

    /** Creates the synthetic generator parameter {@code $__asyncGen__}. */
    public static Parameter createGenParam() {
        return new Parameter(ClassHelper.OBJECT_TYPE, ASYNC_GEN_PARAM_NAME);
    }

    /**
     * Returns {@code true} if the statement tree contains a {@code yield return}
     * call, without descending into nested closures.
     */
    public static boolean containsYieldReturn(Statement stmt) {
        boolean[] found = {false};
        stmt.visit(new CodeVisitorSupport() {
            @Override
            public void visitStaticMethodCallExpression(StaticMethodCallExpression call) {
                if (YIELD_RETURN_METHOD.equals(call.getMethod())
                        && AsyncSupport.class.getName().equals(call.getOwnerType().getName())) {
                    found[0] = true;
                }
                if (!found[0]) super.visitStaticMethodCallExpression(call);
            }
            @Override
            public void visitClosureExpression(ClosureExpression expression) {
                // Don't descend into nested closures
            }
        });
        return found[0];
    }

    /**
     * Rewrites {@code yieldReturn(expr)} calls to {@code yieldReturn($__asyncGen__, expr)}
     * by injecting the generator parameter reference.
     */
    public static void injectGenParamIntoYieldReturnCalls(Statement stmt, Parameter genParam) {
        stmt.visit(new CodeVisitorSupport() {
            @Override
            public void visitStaticMethodCallExpression(StaticMethodCallExpression call) {
                if (YIELD_RETURN_METHOD.equals(call.getMethod())
                        && AsyncSupport.class.getName().equals(call.getOwnerType().getName())) {
                    // Already built with gen param by buildYieldReturnCall — no action needed
                    // This method is a hook point for future transformations
                }
                super.visitStaticMethodCallExpression(call);
            }
            @Override
            public void visitClosureExpression(ClosureExpression expression) {
                // Don't descend into nested closures
            }
        });
    }

    /**
     * Returns {@code true} if the statement tree contains a {@code defer} call,
     * without descending into nested closures.
     */
    public static boolean containsDefer(Statement stmt) {
        boolean[] found = {false};
        stmt.visit(new CodeVisitorSupport() {
            @Override
            public void visitStaticMethodCallExpression(StaticMethodCallExpression call) {
                if (DEFER_METHOD.equals(call.getMethod())
                        && AsyncSupport.class.getName().equals(call.getOwnerType().getName())) {
                    found[0] = true;
                }
                if (!found[0]) super.visitStaticMethodCallExpression(call);
            }
            @Override
            public void visitClosureExpression(ClosureExpression expression) {
                // Don't descend into nested closures
            }
        });
        return found[0];
    }

    /**
     * Wraps a statement block with defer scope management:
     * <pre>
     * var $__deferScope__ = AsyncSupport.createDeferScope()
     * try { original body }
     * finally { AsyncSupport.executeDeferScope($__deferScope__) }
     * </pre>
     */
    public static Statement wrapWithDeferScope(Statement body) {
        // var $__deferScope__ = AsyncSupport.createDeferScope()
        Statement declStmt = declS(varX(DEFER_SCOPE_VAR),
                callX(ASYNC_SUPPORT_TYPE, CREATE_DEFER_SCOPE_METHOD));

        // try { body } finally { AsyncSupport.executeDeferScope($__deferScope__) }
        Statement finallyStmt = stmt(callX(ASYNC_SUPPORT_TYPE, EXECUTE_DEFER_SCOPE_METHOD,
                args(varX(DEFER_SCOPE_VAR))));

        return block(declStmt, tryCatchS(body, finallyStmt));
    }
}
