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

import org.codehaus.groovy.ast.GroovyCodeVisitor;

/**
 * Represents a property access such as the expression "foo.bar".
 */
public class PropertyExpression extends Expression {

    private Expression objectExpression;
    private final Expression property;
    private boolean spreadSafe = false;
    private boolean safe = false;
    private boolean isStatic = false;

    private boolean implicitThis = false;

    public boolean isStatic() {
        return isStatic;
    }

    public PropertyExpression(Expression objectExpression, String property) {
        this(objectExpression, new ConstantExpression(property), false);
    }
    
    public PropertyExpression(Expression objectExpression, Expression property) {
        this(objectExpression, property, false);
    }

    public PropertyExpression(Expression objectExpression, Expression property, boolean safe) {
        this.objectExpression = objectExpression;
        this.property = property;
        this.safe = safe;
    }

    public void accept(GroovyCodeVisitor visitor) {
        visitor.visitPropertyExpression(this);
    }

    public boolean isDynamic() {
        return true;
    }

    public Expression transformExpression(ExpressionTransformer transformer) {
        PropertyExpression ret = new PropertyExpression(transformer.transform(objectExpression),
                transformer.transform(property), safe);
        ret.setSpreadSafe(spreadSafe);
        ret.setStatic(isStatic);
        ret.setImplicitThis(implicitThis);
        ret.setSourcePosition(this);
        ret.copyNodeMetaData(this);
        return ret;
    }

    public Expression getObjectExpression() {
        return objectExpression;
    }

    public void setObjectExpression(Expression exp) {
        objectExpression=exp;
    }    
    
    public Expression getProperty() {
        return property;
    }
    
    public String getPropertyAsString() {
        if (property==null) return null;
        if (! (property instanceof ConstantExpression)) return null;
        ConstantExpression constant = (ConstantExpression) property;
        return constant.getText();
    }

    public String getText() {
        String object = objectExpression.getText();
        String text = property.getText();
        String spread = isSpreadSafe() ? "*" : "";
        String safe = isSafe() ? "?" : "";
        return object + spread + safe + "." + text;
    }

    /**
     * @return is this a safe navigation, i.e. if true then if the source object is null
     * then this navigation will return null
     */
    public boolean isSafe() {
        return safe;
    }

    public boolean isSpreadSafe() {
        return spreadSafe;
    }

    public void setSpreadSafe(boolean value) {
        spreadSafe = value;
    }

    public String toString() {
        return super.toString() + "[object: " + objectExpression + " property: " + property + "]";
    }

    public void setStatic(boolean aStatic) {
        this.isStatic = aStatic;
    }
    
    public boolean isImplicitThis(){
        return implicitThis;
    }
    
    public void setImplicitThis(boolean it) {
        implicitThis  = it;
    }
}
