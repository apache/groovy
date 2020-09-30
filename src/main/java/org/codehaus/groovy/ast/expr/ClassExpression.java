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
 * Represents access to a Java/Groovy class in an expression, such
 * as when invoking a static method or accessing a static type
 */
public class ClassExpression extends Expression {

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

    public String toString() {
       return super.toString() + "[type: " + getType().getName() + "]";
    }
}
