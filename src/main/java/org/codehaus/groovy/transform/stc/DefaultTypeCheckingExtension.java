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
package org.codehaus.groovy.transform.stc;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCall;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.ReturnStatement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * The default type checking handler is used by the standard type checker and doesn't handle
 * any of the type checking errors by default. This just means that whenever a type checking
 * error is detected, there's no additional information available to the type checker that
 * could help it.
 *
 * The default handler is also capable of handling a collection of delegate handlers. If a list
 * of delegates is set, then the type checker will try all the delegates until one is capable
 * of handling an error.
 *
 * @since 2.1.0
 */
public class DefaultTypeCheckingExtension extends TypeCheckingExtension {
    protected final List<TypeCheckingExtension> handlers = new LinkedList<TypeCheckingExtension>();

    public DefaultTypeCheckingExtension(final StaticTypeCheckingVisitor typeCheckingVisitor) {
        super(typeCheckingVisitor);
    }

    public void addHandler(TypeCheckingExtension handler) {
        handlers.add(handler);
    }

    public void removeHandler(TypeCheckingExtension handler) {
        handlers.remove(handler);
    }

    public boolean handleUnresolvedVariableExpression(VariableExpression vexp) {
        for (TypeCheckingExtension handler : handlers) {
            if (handler.handleUnresolvedVariableExpression(vexp)) return true;
        }
        return false;
    }

    public boolean handleUnresolvedProperty(final PropertyExpression pexp) {
        for (TypeCheckingExtension handler : handlers) {
            if (handler.handleUnresolvedProperty(pexp)) return true;
        }
        return false;
    }

    public boolean handleUnresolvedAttribute(final AttributeExpression aexp) {
        for (TypeCheckingExtension handler : handlers) {
            if (handler.handleUnresolvedAttribute(aexp)) return true;
        }
        return false;
    }

    @Override
    public boolean handleIncompatibleAssignment(final ClassNode lhsType, final ClassNode rhsType, final Expression assignmentExpression) {
        for (TypeCheckingExtension handler : handlers) {
            if (handler.handleIncompatibleAssignment(lhsType, rhsType, assignmentExpression)) return true;
        }
        return false;
    }

    @Override
    public boolean handleIncompatibleReturnType(ReturnStatement returnStatement, ClassNode inferredReturnType) {
        for (TypeCheckingExtension handler : handlers) {
            if (handler.handleIncompatibleReturnType(returnStatement, inferredReturnType)) return true;
        }
        return false;
    }

    @Override
    public List<MethodNode> handleAmbiguousMethods(final List<MethodNode> nodes, final Expression origin) {
        List<MethodNode> result = nodes;
        Iterator<TypeCheckingExtension> it = handlers.iterator();
        while (result.size()>1 && it.hasNext()) {
            result = it.next().handleAmbiguousMethods(result, origin);
        }
        return result;
    }

    public List<MethodNode> handleMissingMethod(final ClassNode receiver, final String name, final ArgumentListExpression argumentList, final ClassNode[] argumentTypes, final MethodCall call) {
        List<MethodNode> result = new LinkedList<MethodNode>();
        for (TypeCheckingExtension handler : handlers) {
            List<MethodNode> handlerResult = handler.handleMissingMethod(receiver, name, argumentList, argumentTypes, call);
            for (MethodNode mn : handlerResult) {
                if (mn.getDeclaringClass()==null) {
                    mn.setDeclaringClass(ClassHelper.OBJECT_TYPE);
                }
            }
            result.addAll(handlerResult);
        }
        return result;
    }

    @Override
    public void afterVisitMethod(final MethodNode node) {
        for (TypeCheckingExtension handler : handlers) {
            handler.afterVisitMethod(node);
        }
    }

    @Override
    public boolean beforeVisitMethod(final MethodNode node) {
        for (TypeCheckingExtension handler : handlers) {
            if (handler.beforeVisitMethod(node)) return true;
        }
        return false;
    }

    @Override
    public void afterVisitClass(final ClassNode node) {
        for (TypeCheckingExtension handler : handlers) {
            handler.afterVisitClass(node);
        }
    }

    @Override
    public boolean beforeVisitClass(final ClassNode node) {
        for (TypeCheckingExtension handler : handlers) {
            if (handler.beforeVisitClass(node)) return true;
        }
        return false;
    }

    @Override
    public void afterMethodCall(final MethodCall call) {
        for (TypeCheckingExtension handler : handlers) {
            handler.afterMethodCall(call);
        }

    }

    @Override
    public boolean beforeMethodCall(final MethodCall call) {
        for (TypeCheckingExtension handler : handlers) {
            if (handler.beforeMethodCall(call)) return true;
        }
        return false;
    }

    @Override
    public void onMethodSelection(final Expression expression, final MethodNode target) {
        for (TypeCheckingExtension handler : handlers) {
            handler.onMethodSelection(expression, target);
        }
    }

    @Override
    public void setup() {
        ArrayList<TypeCheckingExtension> copy = new ArrayList<TypeCheckingExtension>(handlers);
        // we're using a copy here because new extensions can be added during the "setup" phase
        for (TypeCheckingExtension handler : copy) {
            handler.setup();
        }
    }

    @Override
    public void finish() {
        for (TypeCheckingExtension handler : handlers) {
            handler.finish();
        }
    }
}