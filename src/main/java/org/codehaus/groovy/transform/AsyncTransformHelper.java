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
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.query.AstQuery;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;

import java.util.concurrent.atomic.AtomicLong;

import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.castX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.declS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.tryCatchS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

/**
 * Shared AST utilities for the {@code async}/{@code await}/{@code defer} language features.
 * <p>
 * Centralises AST node construction and higher-level rewrites used by the
 * parser ({@code AstBuilder}) so feature logic does not sprawl across the
 * visitor.
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
    private static final String TO_ITERABLE_METHOD = "toIterable";
    private static final String CLOSE_ITERABLE_METHOD = "closeIterable";
    private static final String CREATE_DEFER_SCOPE_METHOD = "createDeferScope";
    private static final String DEFER_METHOD = "defer";
    private static final String EXECUTE_DEFER_SCOPE_METHOD = "executeDeferScope";

    /** Monotonic suffix for synthetic {@code for await} source variables. */
    private static final AtomicLong FOR_AWAIT_SEQ = new AtomicLong();

    private AsyncTransformHelper() { }

    private static Expression ensureArgs(Expression expr) {
        return expr instanceof ArgumentListExpression ? expr : new ArgumentListExpression(expr);
    }

    /**
     * Builds {@code AsyncSupport.await(arg)} or for multi-arg:
     * {@code AsyncSupport.await(Awaitable.all(arg1, arg2, ...))}.
     * <p>
     * Single-arg form casts to {@link Object} so static/dynamic overload
     * selection targets {@link AsyncSupport#await(Object)}, avoiding
     * ambiguity when the argument implements multiple async interfaces
     * (e.g. {@link java.util.concurrent.CompletableFuture} implements both
     * {@link java.util.concurrent.CompletionStage} and
     * {@link java.util.concurrent.Future}).
     */
    public static Expression buildAwaitCall(Expression arg) {
        if (arg instanceof ArgumentListExpression args && args.getExpressions().size() > 1) {
            Expression allCall = callX(AWAITABLE_TYPE, "all", args);
            return callX(ASYNC_SUPPORT_TYPE, AWAIT_METHOD, new ArgumentListExpression(allCall));
        }
        Expression source = arg;
        if (arg instanceof ArgumentListExpression args && !args.getExpressions().isEmpty()) {
            source = args.getExpression(0);
        }
        return callX(ASYNC_SUPPORT_TYPE, AWAIT_METHOD,
                new ArgumentListExpression(castX(ClassHelper.OBJECT_TYPE, source)));
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
     * Builds {@code AsyncSupport.toIterable(source)}.
     */
    public static Expression buildToIterableCall(Expression source) {
        return callX(ASYNC_SUPPORT_TYPE, TO_ITERABLE_METHOD, ensureArgs(source));
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
        return AstQuery.from(stmt)
                .descendants(StaticMethodCallExpression.class)
                .notInto(ClosureExpression.class)
                .where(call -> YIELD_RETURN_METHOD.equals(call.getMethod())
                        && AsyncSupport.class.getName().equals(call.getOwnerType().getName()))
                .any();
    }

    /**
     * Returns {@code true} if the statement tree contains a {@code defer} call,
     * without descending into nested closures.
     */
    public static boolean containsDefer(Statement stmt) {
        return AstQuery.from(stmt)
                .descendants(StaticMethodCallExpression.class)
                .notInto(ClosureExpression.class)
                .where(call -> DEFER_METHOD.equals(call.getMethod())
                        && AsyncSupport.class.getName().equals(call.getOwnerType().getName()))
                .any();
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
        Statement declStmt = declS(varX(DEFER_SCOPE_VAR),
                callX(ASYNC_SUPPORT_TYPE, CREATE_DEFER_SCOPE_METHOD));

        Statement finallyStmt = stmt(callX(ASYNC_SUPPORT_TYPE, EXECUTE_DEFER_SCOPE_METHOD,
                args(varX(DEFER_SCOPE_VAR))));

        return block(declStmt, tryCatchS(body, finallyStmt));
    }

    /**
     * Rewrites a {@code for await} loop into a block that materialises the
     * source iterable, runs the loop, and closes the source in a finally block:
     * <pre>
     * var $__forAwaitSource_N = AsyncSupport.toIterable(collection)
     * try {
     *     for (... in $__forAwaitSource_N) { body }
     * } finally {
     *     AsyncSupport.closeIterable($__forAwaitSource_N)
     * }
     * </pre>
     *
     * @param forStatement the for statement whose collection is the await source
     * @return the rewritten statement block
     */
    public static Statement wrapForAwaitLoop(ForStatement forStatement) {
        Expression original = forStatement.getCollectionExpression();
        String tempVar = "$__forAwaitSource_" + FOR_AWAIT_SEQ.incrementAndGet();
        Expression toIterableCall = buildToIterableCall(original);

        Statement declStmt = declS(varX(tempVar), toIterableCall);
        forStatement.setCollectionExpression(varX(tempVar));

        Statement finallyStmt = stmt(buildCloseIterableCall(varX(tempVar)));
        TryCatchStatement tryCatch = new TryCatchStatement(forStatement, finallyStmt);
        return block(declStmt, tryCatch);
    }

    /**
     * Applies defer-scope wrapping and generator parameter injection to an
     * {@code async { ... }} closure, then builds the runtime call
     * ({@code async} or {@code asyncGenerator}).
     *
     * @param closure the parsed async closure expression
     * @return the desugared runtime call expression
     */
    public static Expression transformAsyncClosure(ClosureExpression closure) {
        boolean hasYieldReturn = containsYieldReturn(closure.getCode());
        boolean hasDefer = containsDefer(closure.getCode());

        if (hasDefer) {
            Statement wrappedBody = wrapWithDeferScope(closure.getCode());
            ClosureExpression newClosure = new ClosureExpression(closure.getParameters(), wrappedBody);
            newClosure.setVariableScope(closure.getVariableScope());
            newClosure.setSourcePosition(closure);
            closure = newClosure;
        }

        if (hasYieldReturn) {
            Parameter genParam = createGenParam();
            Parameter[] existingParams = closure.getParameters();
            boolean hasUserParams = existingParams != null && existingParams.length > 0;
            Parameter[] newParams;
            if (hasUserParams) {
                newParams = new Parameter[existingParams.length + 1];
                newParams[0] = genParam;
                System.arraycopy(existingParams, 0, newParams, 1, existingParams.length);
            } else {
                newParams = new Parameter[]{genParam};
            }
            ClosureExpression genClosure = new ClosureExpression(newParams, closure.getCode());
            genClosure.setVariableScope(closure.getVariableScope());
            genClosure.setSourcePosition(closure);
            return buildAsyncGeneratorCall(new ArgumentListExpression(genClosure));
        }
        return buildAsyncCall(new ArgumentListExpression(closure));
    }
}
