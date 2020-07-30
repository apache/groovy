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
 * Represents an attribute access (accessing the field of a class) such as the expression "foo.@bar".
 */
public class AttributeExpression extends PropertyExpression {

    public AttributeExpression(final Expression objectExpression, final Expression property) {
        super(objectExpression, property, false);
    }

    public AttributeExpression(final Expression objectExpression, final Expression property, final boolean safe) {
        super(objectExpression, property, safe);
    }

    @Override
    public String getText() {
        StringBuilder sb = new StringBuilder(getObjectExpression().getText());
        if (isSpreadSafe()) sb.append('*');
        if (isSafe()) sb.append('?');
        sb.append(".@");

        return sb.append(getProperty().getText()).toString();
    }

    @Override
    public Expression transformExpression(final ExpressionTransformer transformer) {
        AttributeExpression ret = new AttributeExpression(transformer.transform(getObjectExpression()), transformer.transform(getProperty()), isSafe());
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
        visitor.visitAttributeExpression(this);
    }
}
