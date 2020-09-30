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

import groovy.lang.MetaMethod;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;

/**
 * A static method call on a class
 */
public class StaticMethodCallExpression extends Expression implements MethodCall {

    private ClassNode ownerType;
    private final String method;
    private final Expression arguments;
    private MetaMethod metaMethod = null;

    public StaticMethodCallExpression(ClassNode type, String method, Expression arguments) {
        ownerType = type;
        this.method = method;
        this.arguments = arguments;
    }

    @Override
    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitStaticMethodCallExpression(this);
    }

    @Override
    public Expression transformExpression(ExpressionTransformer transformer) {
        Expression ret = new StaticMethodCallExpression(getOwnerType(), method, transformer.transform(arguments));
        ret.setSourcePosition(this);
        ret.copyNodeMetaData(this);
        return ret;
    }

    @Override
    public ASTNode getReceiver() {
        return ownerType;
    }

    @Override
    public String getMethodAsString() {
        return method;
    }

    @Override
    public Expression getArguments() {
        return arguments;
    }

    public String getMethod() {
        return method;
    }

    @Override
    public String getText() {
        return getOwnerType().getName() + "." + method + arguments.getText();
    }

    public String toString() {
        return super.toString() + "[" + getOwnerType().getName() + "#" + method + " arguments: " + arguments + "]";
    }

    public ClassNode getOwnerType() {
        return ownerType;
    }

    public void setOwnerType(ClassNode ownerType) {
        this.ownerType = ownerType;
    }

    public void setMetaMethod(MetaMethod metaMethod) {
        this.metaMethod = metaMethod;
    }

    public MetaMethod getMetaMethod() {
        return metaMethod;
    }

}
