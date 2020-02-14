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
    private boolean safe;
    private boolean spreadSafe;
    private boolean isStatic;
    private boolean implicitThis;

    public PropertyExpression(final Expression objectExpression, final String propertyName) {
        this(objectExpression, new ConstantExpression(propertyName), false);
    }

    public PropertyExpression(final Expression objectExpression, final Expression property) {
        this(objectExpression, property, false);
    }

    public PropertyExpression(final Expression objectExpression, final Expression property, final boolean safe) {
        this.objectExpression = objectExpression;
        this.property = property;
        this.safe = safe;
    }

    public void visit(final GroovyCodeVisitor visitor) {
        visitor.visitPropertyExpression(this);
    }

    public Expression transformExpression(final ExpressionTransformer transformer) {
        PropertyExpression ret = new PropertyExpression(
                transformer.transform(objectExpression),
                transformer.transform(property),
                safe
        );
        ret.setImplicitThis(implicitThis);
        ret.setSpreadSafe(spreadSafe);
        ret.setStatic(isStatic);
        ret.setSourcePosition(this);
        ret.copyNodeMetaData(this);
        return ret;
    }

    public Expression getObjectExpression() {
        return objectExpression;
    }

    public void setObjectExpression(final Expression objectExpression) {
        this.objectExpression = objectExpression;
    }

    public Expression getProperty() {
        return property;
    }

    public String getPropertyAsString() {
        if (!(property instanceof ConstantExpression)) return null;
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

    public boolean isDynamic() {
        return true;
    }

    public boolean isImplicitThis() {
        return implicitThis;
    }

    public void setImplicitThis(final boolean implicitThis) {
        this.implicitThis  = implicitThis;
    }

    /**
     * @return is this a safe navigation, i.e. if true then if the source object
     * is null then this navigation will return null
     */
    public boolean isSafe() {
        return safe;
    }

    public boolean isSpreadSafe() {
        return spreadSafe;
    }

    public void setSpreadSafe(final boolean spreadSafe) {
        this.spreadSafe = spreadSafe;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public void setStatic(final boolean isStatic) {
        this.isStatic = isStatic;
    }

    public String toString() {
        return super.toString() + "[object: " + objectExpression + " property: " + property + "]";
    }
}
