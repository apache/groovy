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

import java.util.Collections;
import java.util.List;

/**
 * Represents an array object construction either using a fixed size
 * or an initializer expression
 */
public class ArrayExpression extends Expression {
    private final List<Expression> expressions;
    private final List<Expression> sizeExpression;

    private final ClassNode elementType;

    private static ClassNode makeArray(ClassNode base, List<Expression> sizeExpression) {
        ClassNode ret = base.makeArray();
        if (sizeExpression == null) return ret;
        int size = sizeExpression.size();
        for (int i = 1; i < size; i++) {
            ret = ret.makeArray();
        }
        return ret;
    }

    public ArrayExpression(ClassNode elementType, List<Expression> expressions, List<Expression> sizeExpression) {
        //expect to get the elementType
        super.setType(makeArray(elementType, sizeExpression));
        if (expressions == null) expressions = Collections.emptyList();
        this.elementType = elementType;
        this.expressions = expressions;
        this.sizeExpression = sizeExpression;

        for (Object item : expressions) {
            if (item != null && !(item instanceof Expression)) {
                throw new ClassCastException("Item: " + item + " is not an Expression");
            }
        }
        if (sizeExpression != null) {
            for (Object item : sizeExpression) {
                if (!(item instanceof Expression)) {
                    throw new ClassCastException("Item: " + item + " is not an Expression");
                }
            }
        }
    }


    /**
     * Creates an array using an initializer expression
     */
    public ArrayExpression(ClassNode elementType, List<Expression> expressions) {
        this(elementType, expressions, null);
    }

    public void addExpression(Expression expression) {
        expressions.add(expression);
    }

    public List<Expression> getExpressions() {
        return expressions;
    }

    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitArrayExpression(this);
    }

    public boolean isDynamic() {
        return false;
    }

    public Expression transformExpression(ExpressionTransformer transformer) {
        List<Expression> exprList = transformExpressions(expressions, transformer);
        List<Expression> sizes = null;
        if (sizeExpression != null) sizes = transformExpressions(sizeExpression, transformer);
        Expression ret = new ArrayExpression(elementType, exprList, sizes);
        ret.setSourcePosition(this);
        ret.copyNodeMetaData(this);
        return ret;
    }

    public Expression getExpression(int i) {
        return expressions.get(i);
    }

    public ClassNode getElementType() {
        return elementType;
    }

    public String getText() {
        StringBuilder buffer = new StringBuilder("[");
        boolean first = true;
        for (Expression expression : expressions) {
            if (first) {
                first = false;
            } else {
                buffer.append(", ");
            }

            buffer.append(expression.getText());
        }
        buffer.append("]");
        return buffer.toString();
    }

    public List<Expression> getSizeExpression() {
        return sizeExpression;
    }

    public String toString() {
        return super.toString() + expressions;
    }
}
