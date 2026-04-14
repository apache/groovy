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

import groovy.concurrent.Actor;
import groovy.concurrent.Awaitable;
import groovy.transform.ActiveMethod;
import groovy.transform.ActiveObject;
import org.apache.groovy.runtime.async.AsyncSupport;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.fieldX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

/**
 * Handles the {@link ActiveObject} annotation, transforming
 * {@link ActiveMethod}-annotated methods to route through an
 * internal actor for serialised execution.
 * <p>
 * Inspired by GPars' {@code ActiveObjectASTTransformation},
 * adapted for Groovy's built-in {@link Actor} infrastructure.
 *
 * @see ActiveObject
 * @see ActiveMethod
 * @since 6.0.0
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class ActiveObjectASTTransformation extends AbstractASTTransformation {

    private static final ClassNode ACTOR_TYPE = ClassHelper.makeWithoutCaching(Actor.class, false);
    private static final ClassNode AWAITABLE_TYPE = ClassHelper.makeWithoutCaching(Awaitable.class, false);
    private static final ClassNode ASYNC_SUPPORT_TYPE = ClassHelper.makeWithoutCaching(AsyncSupport.class, false);
    private static final ClassNode INVOKER_HELPER_TYPE = ClassHelper.makeWithoutCaching(InvokerHelper.class, false);
    private static final ClassNode ACTIVE_METHOD_TYPE = ClassHelper.makeWithoutCaching(ActiveMethod.class, false);

    private static final String METHOD_NAME_PREFIX = "activeObject_";

    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotationNode annotation = (AnnotationNode) nodes[0];
        ClassNode classNode = (ClassNode) nodes[1];

        String actorFieldName = getMemberStringValue(annotation, "actorName", "internalActiveObjectActor");

        // Add actor field if not already present
        FieldNode actorField = classNode.getDeclaredField(actorFieldName);
        if (actorField == null) {
            actorField = addActorField(classNode, actorFieldName);
        }

        // Transform all @ActiveMethod methods
        for (MethodNode method : new ArrayList<>(classNode.getMethods())) {
            List<AnnotationNode> activeAnnotations = method.getAnnotations(ACTIVE_METHOD_TYPE);
            if (!activeAnnotations.isEmpty()) {
                if (method.isStatic()) {
                    addError("@ActiveMethod cannot be applied to static methods: " + method.getName(), method);
                    continue;
                }
                boolean blocking = memberHasValue(activeAnnotations.get(0), "blocking", false)
                        ? false : true;  // default is true
                transformMethod(classNode, method, actorField, blocking);
            }
        }
    }

    /**
     * Adds the actor field to the class, initialised with a reactor
     * that dispatches messages via InvokerHelper.
     */
    private FieldNode addActorField(ClassNode classNode, String fieldName) {
        // Actor.reactor { msg -> InvokerHelper.invokeMethod(msg[0], msg[1], msg[2]) }
        // The reactor receives [target, methodName, argsArray] and invokes the renamed method
        Expression initializer = callX(
                classX(ACTOR_TYPE),
                "reactor",
                args(buildDispatchLambda()));

        return classNode.addField(fieldName,
                Modifier.FINAL | Modifier.TRANSIENT | Modifier.PRIVATE,
                ACTOR_TYPE,
                initializer);
    }

    /**
     * Builds the dispatch function:
     * { msg -> InvokerHelper.invokeMethod(msg[0], "activeObject_" + msg[1], msg[2]) }
     */
    private Expression buildDispatchLambda() {
        // We build a closure that:
        // 1. Extracts target = msg[0], methodName = msg[1], args = msg[2]
        // 2. Calls InvokerHelper.invokeMethod(target, "activeObject_" + methodName, args)
        Parameter msgParam = new Parameter(ClassHelper.OBJECT_TYPE, "msg");
        Expression msgVar = varX(msgParam);

        // msg[0] = target, msg[1] = method name, msg[2] = args array
        Expression target = callX(msgVar, "getAt", constX(0));
        Expression methodName = callX(
                constX(METHOD_NAME_PREFIX), "plus",
                callX(msgVar, "getAt", constX(1)));
        Expression argsExpr = callX(msgVar, "getAt", constX(2));

        Expression invokeCall = callX(
                classX(INVOKER_HELPER_TYPE),
                "invokeMethod",
                args(target, methodName, argsExpr));

        Statement body = returnS(invokeCall);

        org.codehaus.groovy.ast.expr.ClosureExpression closure =
                new org.codehaus.groovy.ast.expr.ClosureExpression(
                        new Parameter[]{msgParam}, body);
        closure.setVariableScope(new org.codehaus.groovy.ast.VariableScope());

        return closure;
    }

    /**
     * Transforms a single method:
     * 1. Renames original to activeObject_methodName (private)
     * 2. Replaces body with actor.sendAndGet([this, name, args]) dispatch
     */
    private void transformMethod(ClassNode classNode, MethodNode method,
                                  FieldNode actorField, boolean blocking) {
        String originalName = method.getName();
        String renamedName = findUniqueName(classNode, originalName);

        // Create the renamed private method with the original body
        MethodNode renamed = new MethodNode(
                renamedName,
                Modifier.PRIVATE | Modifier.FINAL,
                method.getReturnType(),
                method.getParameters(),
                method.getExceptions(),
                method.getCode());
        renamed.setSourcePosition(method);
        classNode.addMethod(renamed);

        // Build the message: [this, "originalName", [param1, param2, ...]]
        List<Expression> argsList = new ArrayList<>();
        for (Parameter p : method.getParameters()) {
            argsList.add(varX(p.getName()));
        }
        Expression argsArray = new ArrayExpression(
                ClassHelper.OBJECT_TYPE, argsList);

        List<Expression> messageElements = new ArrayList<>();
        messageElements.add(varX("this"));
        messageElements.add(constX(originalName));
        messageElements.add(argsArray);
        Expression message = new org.codehaus.groovy.ast.expr.ListExpression(messageElements);

        // actor.sendAndGet(message)
        Expression sendCall = callX(
                fieldX(actorField),
                "sendAndGet",
                args(message));

        Statement newBody;
        if (blocking) {
            // AsyncSupport.await(actor.sendAndGet(message))
            Expression awaitCall = callX(
                    classX(ASYNC_SUPPORT_TYPE),
                    "await",
                    args(sendCall));

            if (method.getReturnType().equals(ClassHelper.VOID_TYPE)) {
                newBody = stmt(awaitCall);
            } else {
                newBody = returnS(awaitCall);
            }
        } else {
            // return actor.sendAndGet(message)  — returns Awaitable
            ClassNode returnType = method.getReturnType();
            if (!returnType.equals(ClassHelper.VOID_TYPE)
                    && !returnType.equals(AWAITABLE_TYPE)
                    && !returnType.equals(ClassHelper.OBJECT_TYPE)) {
                addError("@ActiveMethod with blocking=false requires a return type of void, Object, or "
                        + Awaitable.class.getName() + "; got " + returnType.getName(), method);
                return;
            }
            if (returnType.equals(ClassHelper.VOID_TYPE)) {
                newBody = stmt(sendCall);
            } else {
                newBody = returnS(sendCall);
            }
        }

        method.setCode(newBody);
    }

    private String findUniqueName(ClassNode classNode, String originalName) {
        String candidate = METHOD_NAME_PREFIX + originalName;
        int counter = 0;
        // Check all overloads, not just the no-arg variant, so two
        // @ActiveMethod methods with the same name don't collide.
        while (!classNode.getDeclaredMethods(candidate).isEmpty()) {
            candidate = METHOD_NAME_PREFIX + originalName + "_" + (++counter);
        }
        return candidate;
    }
}
