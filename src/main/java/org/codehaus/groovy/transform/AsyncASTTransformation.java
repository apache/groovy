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
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
import static org.codehaus.groovy.ast.tools.GenericsUtils.makeClassSafeWithGenerics;

/**
 * Handles code generation for the {@link Async @Async} annotation.
 * <p>
 * Transforms the annotated method so that:
 * <ol>
 *   <li>{@code await(expr)} calls within the method body are redirected to
 *       the runtime's {@code await()} via {@link AsyncTransformHelper}</li>
 *   <li>The method body is executed asynchronously — or as an async generator
 *       if it contains {@code yield return} — via factory methods on
 *       {@link AsyncTransformHelper}</li>
 *   <li>Methods containing {@code defer} statements are wrapped in a
 *       try-finally block that executes deferred actions in LIFO order</li>
 *   <li>The return type becomes {@code Awaitable<T>}
 *       (or {@code AsyncStream<T>} for generators)</li>
 * </ol>
 * <p>
 * All AST node construction for async runtime calls is delegated to
 * {@link AsyncTransformHelper}, which encapsulates the internal method
 * names and class references. This class focuses on method-level
 * validation and structural transformation.
 * <p>
 * This transformation runs during the {@link CompilePhase#CANONICALIZATION}
 * phase — before type resolution, which allows the modified return types to
 * participate in normal type checking.
 *
 * @see Async
 * @see AsyncTransformHelper
 * @since 6.0.0
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class AsyncASTTransformation extends AbstractASTTransformation {

    private static final Class<?> MY_CLASS = Async.class;
    private static final ClassNode MY_TYPE = ClassHelper.make(MY_CLASS);
    private static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();
    private static final ClassNode AWAITABLE_TYPE = ClassHelper.make(Awaitable.class);
    private static final ClassNode ASYNC_STREAM_TYPE = ClassHelper.make(AsyncStream.class);

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
            executorExpr = AsyncTransformHelper.buildGetExecutorCall();
        }

        // Step 1: Transform await() method calls to AsyncSupport.await()
        new AwaitCallTransformer(source).visitMethod(mNode);

        // Step 2: Check if body contains yield return (AsyncSupport.yieldReturn) calls
        Statement transformedBody = mNode.getCode();
        boolean hasYieldReturn = AsyncTransformHelper.containsYieldReturn(transformedBody);
        boolean hasDefer = AsyncTransformHelper.containsDefer(transformedBody);

        // Step 2a: If defer is present, wrap body in try-finally with defer scope
        if (hasDefer) {
            transformedBody = AsyncTransformHelper.wrapWithDeferScope(transformedBody);
        }

        Parameter[] closureParams;
        if (hasYieldReturn) {
            closureParams = new Parameter[]{ AsyncTransformHelper.createGenParam() };
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
            AsyncTransformHelper.injectGenParamIntoYieldReturnCalls(transformedBody, closureParams[0]);

            // Async generator: wrap body in AsyncSupport.generateAsyncStream { ... }
            Expression genCall = AsyncTransformHelper.buildGenerateAsyncStreamCall(closure);
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
            boolean isVoid = ClassHelper.isPrimitiveVoid(originalReturnType);
            Expression asyncCall;
            if (isVoid) {
                asyncCall = AsyncTransformHelper.buildExecuteAsyncVoidCall(closure, executorExpr);
            } else {
                asyncCall = AsyncTransformHelper.buildExecuteAsyncCall(closure, executorExpr);
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
     * Walks the method body and transforms {@code await(expr)} method calls
     * into {@code AsyncSupport.await(expr)} static calls.  With the native
     * grammar ({@code await expr}), most calls are already compiled to
     * {@code AsyncSupport.await()} by the parser; this transformer handles
     * the {@code await(expr)} call-style used in {@link Async @Async}-annotated
     * methods where {@code await} is parsed as a regular method call.
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
                    && AsyncTransformHelper.isAwaitMethodName(mce.getMethodAsString())
                    && mce.isImplicitThis()) {
                Expression transformedArgs = mce.getArguments().transformExpression(this);
                Expression replacement = AsyncTransformHelper.buildAwaitCall(transformedArgs);
                replacement.setSourcePosition(expr);
                return replacement;
            }

            return expr.transformExpression(this);
        }
    }
}
