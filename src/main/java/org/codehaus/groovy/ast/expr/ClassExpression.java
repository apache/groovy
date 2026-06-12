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
package org.codehaus.groovy.ast.expr;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;

/**
 * Represents access to a Java or Groovy class object as an expression.
 * This is used when referring to a class literal (e.g., {@code String.class}, {@code MyClass})
 * in contexts such as invoking static methods, accessing static members, or passing a class as an argument.
 * The type of this expression is the {@link ClassNode} being accessed.
 * 
 * @see {@link VariableExpression} for variable references
 * @see {@link MethodCallExpression} for static method invocation
 */
public class ClassExpression extends Expression {

    /**
     * Creates a class expression for the specified type.
     * 
     * @param type the class being accessed as an expression (non-null)
     */
    public ClassExpression(ClassNode type) {
        super.setType(type);
    }

    @Override
    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitClassExpression(this);
    }

    @Override
    public Expression transformExpression(ExpressionTransformer transformer) {
        return this;
    }

    @Override
    public String getText() {
        return getType().getName();
    }

    @Override
    public String toString() {
       return super.toString() + "[type: " + getType().getName() + "]";
    }
}
