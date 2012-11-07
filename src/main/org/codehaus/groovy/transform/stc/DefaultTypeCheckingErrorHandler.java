/*
 * Copyright 2003-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.transform.stc;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.*;

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
 * @author Cedric Champeau
 * @since 2.1.0
 */
public class DefaultTypeCheckingErrorHandler implements TypeCheckingErrorHandler {
    protected final StaticTypeCheckingVisitor typeCheckingVisitor;
    protected final List<TypeCheckingErrorHandler> handlers = new LinkedList<TypeCheckingErrorHandler>();

    public DefaultTypeCheckingErrorHandler(final StaticTypeCheckingVisitor typeCheckingVisitor) {
        this.typeCheckingVisitor = typeCheckingVisitor;
    }

    public void addHandler(TypeCheckingErrorHandler handler) {
        handlers.add(handler);
    }

    public void removeHandler(TypeCheckingErrorHandler handler) {
        handlers.remove(handler);
    }

    public boolean handleUnresolvedVariableExpression(VariableExpression vexp) {
        for (TypeCheckingErrorHandler handler : handlers) {
            if (handler.handleUnresolvedVariableExpression(vexp)) return true;
        }
        return false;
    }

    public boolean handleUnresolvedProperty(final PropertyExpression pexp) {
        for (TypeCheckingErrorHandler handler : handlers) {
            if (handler.handleUnresolvedProperty(pexp)) return true;
        }
        return false;
    }

    public boolean handleUnresolvedAttribute(final AttributeExpression aexp) {
        for (TypeCheckingErrorHandler handler : handlers) {
            if (handler.handleUnresolvedAttribute(aexp)) return true;
        }
        return false;
    }

    public List<MethodNode> handleMissingMethod(final ClassNode receiver, final String name, final ArgumentListExpression argumentList, final ClassNode[] argumentTypes, final MethodCallExpression call) {
        List<MethodNode> result = new LinkedList<MethodNode>();
        for (TypeCheckingErrorHandler handler : handlers) {
            result.addAll(handler.handleMissingMethod(receiver, name, argumentList, argumentTypes, call));
        }
        return result;
    }
}