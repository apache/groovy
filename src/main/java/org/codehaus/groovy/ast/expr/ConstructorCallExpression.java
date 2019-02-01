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

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;

/**
 * A constructor call
 */
public class ConstructorCallExpression extends Expression implements MethodCall {

    private final Expression arguments;
    private boolean usesAnonymousInnerClass;

    public ConstructorCallExpression(ClassNode type, Expression arguments) {
        super.setType(type);
        if (!(arguments instanceof TupleExpression)) {
            this.arguments = new TupleExpression(arguments);
        } else {
            this.arguments = arguments;
        }
    }

    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitConstructorCallExpression(this);
    }

    public Expression transformExpression(ExpressionTransformer transformer) {
        Expression args = transformer.transform(arguments);
        ConstructorCallExpression ret = new ConstructorCallExpression(getType(), args);
        ret.setSourcePosition(this);
        ret.setUsingAnonymousInnerClass(isUsingAnonymousInnerClass());
        ret.copyNodeMetaData(this);
        return ret;
    }

    public ASTNode getReceiver() {
        return null;
    }

    public String getMethodAsString() {
        return "<init>";
    }

    public Expression getArguments() {
        return arguments;
    }

    public String getText() {
        String text = null;
        if (isSuperCall()) {
            text = "super ";
        } else if (isThisCall()) {
            text = "this ";
        } else {
            text = "new " + getType().getName();
        }
        return text + arguments.getText();
    }

    public String toString() {
        return super.toString() + "[type: " + getType() + " arguments: " + arguments + "]";
    }

    public boolean isSuperCall() {
        return getType() == ClassNode.SUPER;
    }

    public boolean isSpecialCall() {
        return isThisCall() || isSuperCall();
    }

    public boolean isThisCall() {
        return getType() == ClassNode.THIS;
    }

    public void setUsingAnonymousInnerClass(boolean usage) {
        this.usesAnonymousInnerClass = usage;
    }

    public boolean isUsingAnonymousInnerClass() {
        return usesAnonymousInnerClass;
    }
}
