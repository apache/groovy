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
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;

/**
 * Represents a field access such as the expression "this.foo".
 */
public class FieldExpression extends Expression {

    private final FieldNode field;
    private boolean useRef;
    
    public FieldExpression(FieldNode field) {
        this.field = field;
    }
    
    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitFieldExpression(this);
    }

    public Expression transformExpression(ExpressionTransformer transformer) {
        return this;
    }
    
    public String getFieldName() {
        return field.getName();
    }

    public FieldNode getField() {
        return field;
    }

    public String getText() {
        return "this." + field.getName();
    }

    public boolean isDynamicTyped() {
        return field.isDynamicTyped();
    }

    public void setType(ClassNode type) {
        super.setType(type);
        field.setType(type);
    }
    
    public ClassNode getType() {
        return field.getType();
    }
    
    public void setUseReferenceDirectly(boolean useRef) {
        this.useRef = useRef;        
    }
    
    public boolean isUseReferenceDirectly() {
        return useRef;
    }
    
    public String toString() {
        return "field("+getType()+" "+getFieldName()+")";
    }
}
