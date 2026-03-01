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

import groovy.concurrent.AsyncStream;
import groovy.concurrent.Awaitable;
import groovy.transform.Async;
import org.apache.groovy.runtime.async.AsyncSupport;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
import static org.codehaus.groovy.ast.tools.GenericsUtils.makeClassSafeWithGenerics;

/**
 * Handles code generation for the {@link Async @Async} annotation.
 * <p>
 * Transforms the annotated method so that:
 * <ol>
 *   <li>{@code await(expr)} calls within the method body are redirected to
 *       {@link AsyncSupport#await(Object) AsyncSupport.await()}</li>
 *   <li>The method body is executed asynchronously via
 *       {@link AsyncSupport#executeAsync AsyncSupport.executeAsync}
 *       (or {@link AsyncSupport#executeAsyncVoid AsyncSupport.executeAsyncVoid}
 *       for {@code void} methods)</li>
 *   <li>Generator methods (containing {@code yield return}) are transformed to
 *       use {@link AsyncSupport#generateAsyncStream AsyncSupport.generateAsyncStream},
 *       returning an {@link AsyncStream}{@code <T>}</li>
 *   <li>The return type becomes {@link Awaitable}{@code <T>}
 *       (or {@link AsyncStream}{@code <T>} for generators)</li>
 * </ol>
 * <p>
 * This transformation runs during the {@link CompilePhase#CANONICALIZATION}
 * phase — before type resolution, which allows the modified return types to
 * participate in normal type checking.
 *
 * @see Async
 * @see AsyncSupport
 * @since 6.0.0
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class AsyncASTTransformation extends AbstractASTTransformation {

    private static final Class<?> MY_CLASS = Async.class;
    private static final ClassNode MY_TYPE = ClassHelper.make(MY_CLASS);
    private static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();
    private static final ClassNode AWAITABLE_TYPE = ClassHelper.make(Awaitable.class);
    private static final ClassNode ASYNC_STREAM_TYPE = ClassHelper.make(AsyncStream.class);
    private static final ClassNode ASYNC_UTILS_TYPE = ClassHelper.make(AsyncSupport.class);

    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode anno = (AnnotationNode) nodes[0];
        if (!MY_TYPE.equals(anno.getClassNode())) return;

        if (!(parent instanceof MethodNode mNode)) return;

        // Validate
        if (mNode.isAbstract()) {
            addError(MY_TYPE_NAME + " cannot be applied to abstract method '" + mNode.getName() + "'", mNode);
            return;
        }
        if ("<init>".equals(mNode.getName()) || "<clinit>".equals(mNode.getName())) {
            addError(MY_TYPE_NAME + " cannot be applied to constructors", mNode);
            return;
        }
        ClassNode originalReturnType = mNode.getReturnType();
        if (AWAITABLE_TYPE.getName().equals(originalReturnType.getName())
                || ASYNC_STREAM_TYPE.getName().equals(originalReturnType.getName())
                || "java.util.concurrent.CompletableFuture".equals(originalReturnType.getName())) {
            addError(MY_TYPE_NAME + " cannot be applied to a method that already returns an async type", mNode);
            return;
        }
        Statement originalBody = mNode.getCode();
        if (originalBody == null) return;

        ClassNode cNode = mNode.getDeclaringClass();

        // Resolve executor expression
        String executorFieldName = getMemberStringValue(anno, "executor");
        Expression executorExpr;
        if (executorFieldName != null && !executorFieldName.isEmpty()) {
            FieldNode field = cNode.getDeclaredField(executorFieldName);
            if (field == null) {
                addError(MY_TYPE_NAME + ": executor field '" + executorFieldName
                        + "' not found in class " + cNode.getName(), mNode);
                return;
            }
            if (mNode.isStatic() && !field.isStatic()) {
                addError(MY_TYPE_NAME + ": executor field '" + executorFieldName
                        + "' must be static for static method '" + mNode.getName() + "'", mNode);
                return;
            }
            executorExpr = varX(executorFieldName);
        } else {
            executorExpr = callX(ASYNC_UTILS_TYPE, "getExecutor", new org.codehaus.groovy.ast.expr.ArgumentListExpression());
        }

        // Step 1: Transform await() method calls to AsyncSupport.await()
        new AwaitCallTransformer(source).visitMethod(mNode);

        // Step 2: Check if body contains yield return (AsyncSupport.yieldReturn) calls
        Statement transformedBody = mNode.getCode();
        boolean hasYieldReturn = containsYieldReturn(transformedBody, source);

        Parameter[] closureParams;
        if (hasYieldReturn) {
            // Generator: inject $__asyncGen__ as closure parameter so yield return
            // can reference it directly — no ThreadLocal needed
            closureParams = new Parameter[]{ new Parameter(ClassHelper.DYNAMIC_TYPE, "$__asyncGen__") };
        } else {
            closureParams = Parameter.EMPTY_ARRAY;
        }
        ClosureExpression closure = new ClosureExpression(closureParams, transformedBody);
        VariableScope closureScope = new VariableScope(mNode.getVariableScope());
        for (Parameter p : mNode.getParameters()) {
            p.setClosureSharedVariable(true);
            closureScope.putReferencedLocalVariable(p);
        }
        closure.setVariableScope(closureScope);

        if (hasYieldReturn) {
            // Transform yieldReturn(expr) → yieldReturn($__asyncGen__, expr) in closure body
            injectGenParamIntoYieldReturnCalls(transformedBody, closureParams[0]);

            // Async generator: wrap body in AsyncSupport.generateAsyncStream { ... }
            Expression genCall = callX(ASYNC_UTILS_TYPE, "generateAsyncStream", args(closure));
            mNode.setCode(block(returnS(genCall)));

            // Return type: AsyncStream<T>
            ClassNode innerType;
            if (ClassHelper.isPrimitiveVoid(originalReturnType) || ClassHelper.OBJECT_TYPE.equals(originalReturnType)) {
                innerType = ClassHelper.OBJECT_TYPE;
            } else if (ClassHelper.isPrimitiveType(originalReturnType)) {
                innerType = ClassHelper.getWrapper(originalReturnType);
            } else {
                innerType = originalReturnType;
            }
            ClassNode streamReturnType = makeClassSafeWithGenerics(ASYNC_STREAM_TYPE, new GenericsType(innerType));
            mNode.setReturnType(streamReturnType);
        } else {
            // Regular async: delegate to AsyncSupport.executeAsync/executeAsyncVoid
            // This ensures identical exception handling with async closures/lambdas
            boolean isVoid = ClassHelper.isPrimitiveVoid(originalReturnType);
            Expression asyncCall;
            if (isVoid) {
                asyncCall = callX(ASYNC_UTILS_TYPE, "executeAsyncVoid", args(closure, executorExpr));
            } else {
                asyncCall = callX(ASYNC_UTILS_TYPE, "executeAsync", args(closure, executorExpr));
            }
            mNode.setCode(block(returnS(asyncCall)));

            // Return type: Awaitable<T>
            ClassNode innerType;
            if (isVoid) {
                innerType = ClassHelper.void_WRAPPER_TYPE;
            } else if (ClassHelper.isPrimitiveType(originalReturnType)) {
                innerType = ClassHelper.getWrapper(originalReturnType);
            } else {
                innerType = originalReturnType;
            }
            ClassNode awaitableReturnType = makeClassSafeWithGenerics(AWAITABLE_TYPE, new GenericsType(innerType));
            mNode.setReturnType(awaitableReturnType);
        }
    }

    /**
     * Walks the statement tree and transforms
     * {@code AsyncSupport.yieldReturn(expr)} calls to
     * {@code AsyncSupport.yieldReturn($__asyncGen__, expr)}, injecting the
     * generator parameter as the first argument so that the generator instance
     * is available without {@code ThreadLocal}.  Does not descend into nested
     * closures, since each nested async closure handles its own generator.
     */
    private static void injectGenParamIntoYieldReturnCalls(Statement code, Parameter genParam) {
        code.visit(new CodeVisitorSupport() {
            @Override
            public void visitExpressionStatement(ExpressionStatement stmt) {
                Expression expr = stmt.getExpression();
                if (expr instanceof StaticMethodCallExpression smce
                        && "yieldReturn".equals(smce.getMethod())
                        && "org.apache.groovy.runtime.async.AsyncSupport".equals(smce.getOwnerType().getName())) {
                    VariableExpression genRef = varX("$__asyncGen__");
                    genRef.setAccessedVariable(genParam);
                    ArgumentListExpression newArgs = new ArgumentListExpression();
                    newArgs.addExpression(genRef);
                    if (smce.getArguments() instanceof ArgumentListExpression argList) {
                        for (Expression arg : argList.getExpressions()) {
                            newArgs.addExpression(arg);
                        }
                    }
                    StaticMethodCallExpression replacement =
                            new StaticMethodCallExpression(smce.getOwnerType(), "yieldReturn", newArgs);
                    replacement.setSourcePosition(smce);
                    stmt.setExpression(replacement);
                }
                super.visitExpressionStatement(stmt);
            }
            @Override
            public void visitClosureExpression(ClosureExpression expression) {
                // Don't descend into nested closures
            }
        });
    }

    /**
     * Checks whether the given statement tree contains any
     * {@code AsyncSupport.yieldReturn()} calls, indicating that this is an
     * async generator method (which should return {@link AsyncStream} rather
     * than {@link Awaitable}).  Does not descend into nested closures.
     */
    private static boolean containsYieldReturn(Statement body, SourceUnit source) {
        boolean[] found = {false};
        new ClassCodeVisitorSupport() {
            @Override
            protected SourceUnit getSourceUnit() { return source; }

            @Override
            public void visitExpressionStatement(ExpressionStatement stmt) {
                Expression expr = stmt.getExpression();
                if (expr instanceof StaticMethodCallExpression smce
                        && "yieldReturn".equals(smce.getMethod())
                        && "org.apache.groovy.runtime.async.AsyncSupport".equals(smce.getOwnerType().getName())) {
                    found[0] = true;
                }
                super.visitExpressionStatement(stmt);
            }

            @Override
            public void visitClosureExpression(ClosureExpression expr) {
                // Do NOT visit into nested closures — yield return inside
                // an async { ... } sub-closure is handled by its own wrapping
            }
        }.visitMethod(new MethodNode("$check", 0, ClassHelper.VOID_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, body));
        return found[0];
    }

    /**
     * Walks the method body and transforms {@code await(expr)} method calls
     * into {@code AsyncSupport.await(expr)} static calls.  With the native
     * grammar ({@code await expr}), most calls are already compiled to
     * {@code AsyncSupport.await()} by the parser; this transformer handles
     * the legacy {@code await(expr)} call-style used in
     * {@link Async @Async}-annotated methods.
     */
    private static class AwaitCallTransformer extends ClassCodeExpressionTransformer {

        private final SourceUnit sourceUnit;

        AwaitCallTransformer(SourceUnit sourceUnit) {
            this.sourceUnit = sourceUnit;
        }

        @Override
        protected SourceUnit getSourceUnit() {
            return sourceUnit;
        }

        @Override
        public Expression transform(Expression expr) {
            if (expr == null) return null;

            if (expr instanceof ClosureExpression ce) {
                ce.getCode().visit(this);
                return ce;
            }

            if (expr instanceof MethodCallExpression mce
                    && "await".equals(mce.getMethodAsString())
                    && mce.isImplicitThis()) {
                Expression transformedArgs = mce.getArguments().transformExpression(this);
                StaticMethodCallExpression replacement = callX(ASYNC_UTILS_TYPE, "await", transformedArgs);
                replacement.setSourcePosition(expr);
                return replacement;
            }

            return expr.transformExpression(this);
        }
    }
}
