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

    /**
     * Builds a property access that is not safe, with a constant property name.
     *
     * @param objectExpression receiver of the access
     * @param propertyName name stored as a {@link ConstantExpression}
     */
    public PropertyExpression(final Expression objectExpression, final String propertyName) {
        this(objectExpression, new ConstantExpression(propertyName), false);
    }

    /**
     * Builds a property access that is not safe.
     *
     * @param objectExpression receiver of the access
     * @param property expression that yields the property name
     */
    public PropertyExpression(final Expression objectExpression, final Expression property) {
        this(objectExpression, property, false);
    }

    /**
     * Builds a property access, optionally with safe navigation.
     *
     * @param objectExpression receiver of the access
     * @param property expression that yields the property name
     * @param safe {@code true} for {@code ?.} navigation, which yields {@code null}
     *             when the receiver is {@code null}
     */
    public PropertyExpression(final Expression objectExpression, final Expression property, final boolean safe) {
        this.objectExpression = objectExpression;
        this.property = property;
        this.safe = safe;
    }

    @Override
    public Expression transformExpression(final ExpressionTransformer transformer) {
        PropertyExpression ret = new PropertyExpression(transformer.transform(getObjectExpression()), transformer.transform(getProperty()), isSafe());
        ret.setImplicitThis(this.isImplicitThis());
        ret.setSpreadSafe(this.isSpreadSafe());
        ret.setStatic(this.isStatic());
        ret.setType(this.getType());
        ret.setSourcePosition(this);
        ret.copyNodeMetaData(this);
        return ret;
    }

    @Override
    public void visit(final GroovyCodeVisitor visitor) {
        visitor.visitPropertyExpression(this);
    }

    /**
     * Returns the receiver of this property access.
     *
     * @return the receiver expression
     */
    public Expression getObjectExpression() {
        return objectExpression;
    }

    /**
     * Replaces the receiver of this property access.
     *
     * @param objectExpression the receiver to store
     */
    public void setObjectExpression(final Expression objectExpression) {
        this.objectExpression = objectExpression;
    }

    /**
     * Returns the expression that names the property.
     *
     * @return the property-name expression
     */
    public Expression getProperty() {
        return property;
    }

    /**
     * Returns the property name when it is a constant. A non-constant property has no
     * fixed name at this node.
     *
     * @return {@link ConstantExpression#getText()} of the property, or {@code null}
     *         when the property is not a {@link ConstantExpression}
     */
    public String getPropertyAsString() {
        return getProperty() instanceof ConstantExpression ? getProperty().getText() : null;
    }

    @Override
    public String getText() {
        StringBuilder sb = new StringBuilder(getObjectExpression().getText());
        if (isSpreadSafe()) sb.append('*');
        if (isSafe()) sb.append('?');
        sb.append('.');

        return sb.append(getProperty().getText()).toString();
    }

    /**
     * Reports this property access as dynamic. This implementation always returns {@code true}.
     *
     * @return {@code true}
     */
    public boolean isDynamic() {
        return true;
    }

    /**
     * Indicates whether the receiver stands for an implicit {@code this} rather than an
     * expression written at this property. The flag starts {@code false}.
     *
     * @return {@code true} when the receiver was inserted for an implicit {@code this}
     */
    public boolean isImplicitThis() {
        return implicitThis;
    }

    /**
     * Records whether the receiver stands for an implicit {@code this}.
     *
     * @param implicitThis {@code true} when the receiver was not written at this property
     */
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

    /**
     * Indicates whether this access uses spread-dot navigation ({@code *.}).
     * The flag starts {@code false}.
     *
     * @return {@code true} when the access is spread-safe
     */
    public boolean isSpreadSafe() {
        return spreadSafe;
    }

    /**
     * Records whether this access uses spread-dot navigation ({@code *.}).
     *
     * @param spreadSafe {@code true} to mark the access as spread-safe
     */
    public void setSpreadSafe(final boolean spreadSafe) {
        this.spreadSafe = spreadSafe;
    }

    /**
     * Returns the static-access flag. It starts {@code false} and is copied by
     * {@link #transformExpression(ExpressionTransformer)}.
     *
     * @return {@code true} when the access is marked static
     */
    public boolean isStatic() {
        return isStatic;
    }

    /**
     * Records whether this access targets a static member.
     *
     * @param isStatic {@code true} to mark the access as static
     */
    public void setStatic(final boolean isStatic) {
        this.isStatic = isStatic;
    }

    @Override
    public String toString() {
        return super.toString() + "[object: " + getObjectExpression() + " property: " + getProperty() + "]";
    }
}
