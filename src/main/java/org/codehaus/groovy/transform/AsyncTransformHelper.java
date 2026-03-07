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

import org.apache.groovy.runtime.async.AsyncSupport;
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
 * {@link AsyncASTTransformation} need to detect, rewrite, and generate AST
 * nodes for {@code await}, {@code yield return}, {@code defer}, and other
 * async constructs. This helper class centralises those operations behind
 * a clean API so that:
 * <ul>
 *   <li>Internal constants (method names, variable names, type references)
 *       remain encapsulated — callers never reference them directly.</li>
 *   <li>The two code paths (parser and annotation transform) stay in sync
 *       with no duplicated logic.</li>
 * </ul>
 * <p>
 * <b>AST factory methods</b> (e.g.
 * {@link #buildAwaitCall(Expression)},
 * {@link #buildYieldReturnCall(Expression)},
 * {@link #buildDeferCall(Expression)})
 * construct fully-formed AST nodes that reference the internal
 * {@code AsyncSupport} runtime class, shielding callers from the underlying
 * method names and class references.
 * <p>
 * <b>Statement scanners</b> ({@link #containsYieldReturn(Statement)},
 * {@link #containsDefer(Statement)}) detect the presence of generator and
 * defer constructs without descending into nested
 * {@link ClosureExpression}s, since each nested {@code async { ... }} closure
 * manages its own scope independently.
 * <p>
 * <b>Rewriting utilities</b>
 * ({@link #injectGenParamIntoYieldReturnCalls(Statement, Parameter)},
 * {@link #wrapWithDeferScope(Statement)}) perform targeted AST mutations
 * that transform high-level syntax into the lower-level runtime calls.  The
 * generated runtime calls now also inherit async-local context automatically,
 * so compiler-generated code retains request/trace metadata across thread hops
 * without special-case AST handling.
 *
 * @see AsyncASTTransformation
 * @since 6.0.0
 */
public final class AsyncTransformHelper {

    // ---- internal constants (all private) --------------------------------

    private static final String ASYNC_SUPPORT_CLASS = AsyncSupport.class.getName();
    private static final ClassNode ASYNC_SUPPORT_TYPE = ClassHelper.makeWithoutCaching(AsyncSupport.class, false);
    private static final String ASYNC_GEN_PARAM_NAME = "$__asyncGen__";
    private static final String DEFER_SCOPE_VAR = "$__deferScope__";

    private static final String AWAIT_METHOD = "await";
    private static final String YIELD_RETURN_METHOD = "yieldReturn";
    private static final String WRAP_ASYNC_METHOD = "wrapAsync";
    private static final String WRAP_ASYNC_GENERATOR_METHOD = "wrapAsyncGenerator";
    private static final String GENERATE_ASYNC_STREAM_METHOD = "generateAsyncStream";
    private static final String EXECUTE_ASYNC_METHOD = "executeAsync";
    private static final String EXECUTE_ASYNC_VOID_METHOD = "executeAsyncVoid";
    private static final String TO_ASYNC_STREAM_METHOD = "toAsyncStream";
    private static final String CLOSE_STREAM_METHOD = "closeStream";
    private static final String CREATE_DEFER_SCOPE_METHOD = "createDeferScope";
    private static final String DEFER_METHOD = "defer";
    private static final String EXECUTE_DEFER_SCOPE_METHOD = "executeDeferScope";

    private AsyncTransformHelper() { }

    // ---- AST factory methods --------------------------------------------

    /**
     * Ensures the expression is an {@link ArgumentListExpression}.
     * If it already is one, returns it as-is; otherwise wraps it.
     */
    private static Expression ensureArgs(Expression expr) {
        return expr instanceof ArgumentListExpression ? expr : new ArgumentListExpression(expr);
    }

    /**
     * Builds {@code AsyncSupport.await(arg)}.
     * Accepts either a single expression or a pre-assembled
     * {@link ArgumentListExpression}.
     *
     * @param arg the expression to await
     * @return an AST node representing the static call
     */
    public static Expression buildAwaitCall(Expression arg) {
        return callX(ASYNC_SUPPORT_TYPE, AWAIT_METHOD, ensureArgs(arg));
    }

    /**
     * Returns {@code true} if the given method name is the {@code await}
     * keyword — used by the {@code @Async} annotation's
     * {@link AsyncASTTransformation.AwaitCallTransformer AwaitCallTransformer}
     * to identify {@code await(expr)} method calls that need rewriting.
     *
     * @param name the method name to check
     * @return {@code true} if the name is {@code "await"}
     */
    public static boolean isAwaitMethodName(String name) {
        return AWAIT_METHOD.equals(name);
    }

    /**
     * Builds {@code AsyncSupport.yieldReturn(expr)}.
     *
     * @param arg the expression to yield
     * @return an AST node representing the static call
     */
    public static Expression buildYieldReturnCall(Expression arg) {
        return callX(ASYNC_SUPPORT_TYPE, YIELD_RETURN_METHOD, ensureArgs(arg));
    }

    /**
     * Builds {@code AsyncSupport.defer($__deferScope__, action)}.
     * The synthetic defer-scope variable is injected by
     * {@link #wrapWithDeferScope(Statement)} during AST transformation.
     * <p>
     * The parser validates that {@code defer} only appears inside an async
     * context (async method or async closure); using it elsewhere is a
     * compile-time error.
     *
     * @param action the deferred action expression (typically a closure)
     * @return an AST node representing the static call
     */
    public static Expression buildDeferCall(Expression action) {
        return callX(ASYNC_SUPPORT_TYPE, DEFER_METHOD,
                args(varX(DEFER_SCOPE_VAR), action));
    }

    /**
     * Builds {@code AsyncSupport.toAsyncStream(source)}.
     *
     * @param source the expression to convert to an async stream
     * @return an AST node representing the static call
     */
    public static Expression buildToAsyncStreamCall(Expression source) {
        return callX(ASYNC_SUPPORT_TYPE, TO_ASYNC_STREAM_METHOD, ensureArgs(source));
    }

    /**
     * Builds {@code AsyncSupport.closeStream(stream)}.
     *
     * @param stream the stream expression to close
     * @return an AST node representing the static call
     */
    public static Expression buildCloseStreamCall(Expression stream) {
        return callX(ASYNC_SUPPORT_TYPE, CLOSE_STREAM_METHOD, ensureArgs(stream));
    }

    /**
     * Builds {@code AsyncSupport.generateAsyncStream(closure)}.
     *
     * @param closure the generator closure expression
     * @return an AST node representing the static call
     */
    public static Expression buildGenerateAsyncStreamCall(Expression closure) {
        return callX(ASYNC_SUPPORT_TYPE, GENERATE_ASYNC_STREAM_METHOD, ensureArgs(closure));
    }

    /**
     * Builds {@code AsyncSupport.wrapAsync(closure)}.
     *
     * @param closure the async closure expression
     * @return an AST node representing the static call
     */
    public static Expression buildWrapAsyncCall(Expression closure) {
        return callX(ASYNC_SUPPORT_TYPE, WRAP_ASYNC_METHOD, ensureArgs(closure));
    }

    /**
     * Builds {@code AsyncSupport.wrapAsyncGenerator(closure)}.
     *
     * @param closure the generator closure expression
     * @return an AST node representing the static call
     */
    public static Expression buildWrapAsyncGeneratorCall(Expression closure) {
        return callX(ASYNC_SUPPORT_TYPE, WRAP_ASYNC_GENERATOR_METHOD, ensureArgs(closure));
    }

    /**
     * Builds {@code AsyncSupport.executeAsync(closure, executor)}.
     *
     * @param closure  the async closure expression
     * @param executor the executor expression
     * @return an AST node representing the static call
     */
    public static Expression buildExecuteAsyncCall(Expression closure, Expression executor) {
        return callX(ASYNC_SUPPORT_TYPE, EXECUTE_ASYNC_METHOD, args(closure, executor));
    }

    /**
     * Builds {@code AsyncSupport.executeAsyncVoid(closure, executor)}.
     *
     * @param closure  the async void closure expression
     * @param executor the executor expression
     * @return an AST node representing the static call
     */
    public static Expression buildExecuteAsyncVoidCall(Expression closure, Expression executor) {
        return callX(ASYNC_SUPPORT_TYPE, EXECUTE_ASYNC_VOID_METHOD, args(closure, executor));
    }

    /**
     * Builds {@code AsyncSupport.getExecutor()}.
     *
     * @return an AST node representing the static call
     */
    public static Expression buildGetExecutorCall() {
        return callX(ASYNC_SUPPORT_TYPE, "getExecutor", ArgumentListExpression.EMPTY_ARGUMENTS);
    }

    // ---- statement scanners ---------------------------------------------

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
        return containsStaticCall(code, YIELD_RETURN_METHOD);
    }

    // ---- rewriting utilities --------------------------------------------

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
        return containsStaticCall(code, DEFER_METHOD);
    }

    /**
     * Scans the given statement tree for a static method call to
     * {@code AsyncSupport.<methodName>()}.  Does <em>not</em> descend
     * into nested {@link ClosureExpression}s, since each nested closure
     * manages its own transformation independently.
     *
     * @param code       the statement tree to scan
     * @param methodName the method name to look for on {@code AsyncSupport}
     * @return {@code true} if at least one matching call is found
     */
    private static boolean containsStaticCall(Statement code, String methodName) {
        boolean[] found = {false};
        code.visit(new CodeVisitorSupport() {
            @Override
            public void visitStaticMethodCallExpression(StaticMethodCallExpression call) {
                if (methodName.equals(call.getMethod())
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
