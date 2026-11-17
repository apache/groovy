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
package org.codehaus.groovy.ast;

import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;

/**
 * Represents a constructor declaration
 */
public class ConstructorNode extends MethodNode {
    /**
     * Leaves the constructor unset so a subclass can fill the declaration
     * in later. The name stays null and no exceptions are recorded.
     */
    protected ConstructorNode() {}

    /**
     * Creates a constructor with no parameters and no checked exceptions.
     *
     * @param modifiers JVM access flags for the constructor
     * @param code constructor body
     */
    public ConstructorNode(int modifiers, Statement code) {
        this(modifiers, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, code);
    }

    /**
     * Creates a constructor named {@literal <init>} whose return type is
     * {@code void}.
     *
     * @param modifiers JVM access flags for the constructor
     * @param parameters constructor parameters
     * @param exceptions checked exceptions declared on the constructor
     * @param code constructor body
     */
    public ConstructorNode(int modifiers, Parameter[] parameters, ClassNode[] exceptions, Statement code) {
        super("<init>", modifiers, ClassHelper.VOID_TYPE, parameters, exceptions, code);
    }

    /**
     * Reports whether the first statement is a {@code this(...)} or
     * {@code super(...)} call.
     *
     * @return {@code true} when that statement is a special constructor call
     */
    public boolean firstStatementIsSpecialConstructorCall() {
        Statement code = getFirstStatement();
        if (!(code instanceof ExpressionStatement)) return false;

        Expression expression = ((ExpressionStatement) code).getExpression();
        if (!(expression instanceof ConstructorCallExpression cce)) return false;
        return cce.isSpecialCall();
    }

}
