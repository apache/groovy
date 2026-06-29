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
package org.codehaus.groovy.ast.query;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;

import java.util.List;

/**
 * The enclosing context of a candidate node during an {@link AstQuery} traversal.
 *
 * <p>A {@code AstContext} is supplied to the contextual {@code where}/{@code forEach}
 * overloads so a predicate or consumer can take the surrounding structure of a node into
 * account (for example, "a {@code VariableExpression} that occurs inside a static method").
 * The information is valid only for the duration of the callback; do not retain it.
 *
 * @since 6.0.0
 */
public interface AstContext {

    /**
     * Returns the immediate parent of the candidate node, or {@code null} if the candidate is the
     * query root.
     *
     * @return the parent node or {@code null}
     */
    ASTNode parent();

    /**
     * Returns the ancestors of the candidate node, nearest first (the immediate parent is the first
     * element). The candidate node itself is not included.
     *
     * @return an immutable snapshot of the ancestor chain
     */
    List<ASTNode> ancestors();

    /**
     * Returns the nearest enclosing method or constructor, or {@code null} if the candidate is not
     * inside one.
     *
     * @return the enclosing {@link MethodNode} or {@code null}
     */
    MethodNode enclosingMethod();

    /**
     * Returns the nearest enclosing class, or {@code null} if the candidate is not inside one.
     *
     * @return the enclosing {@link ClassNode} or {@code null}
     */
    ClassNode enclosingClass();
}
