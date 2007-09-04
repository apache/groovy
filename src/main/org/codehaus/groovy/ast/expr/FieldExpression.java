/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.ast.expr;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;

/**
 * Represents a field access such as the expression "this.foo".
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class FieldExpression extends Expression {

    private final FieldNode field;
    
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
}
