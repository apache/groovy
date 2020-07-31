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
import java.util.stream.Collectors;

/**
 * Represents an array object construction.
 * One of:
 * <ul>
 *     <li>a fixed size array (e.g. {@code new String[3]} or {@code new Integer[2][3])}</li>
 *     <li>an array with an explicit initializer (e.g. {@code new String[]{ "foo", "bar" &#125;})</li>
 * </ul>
 */
public class ArrayExpression extends Expression {
    private final List<Expression> initExpressions;
    private final List<Expression> sizeExpressions;

    private final ClassNode elementType;

    private static ClassNode makeArray(ClassNode base, List<Expression> sizeExpressions) {
        ClassNode ret = base.makeArray();
        if (sizeExpressions == null) return ret;
        int size = sizeExpressions.size();
        for (int i = 1; i < size; i++) {
            ret = ret.makeArray();
        }
        return ret;
    }

    public ArrayExpression(ClassNode elementType, List<Expression> initExpressions, List<Expression> sizeExpressions) {
        super.setType(makeArray(elementType, sizeExpressions));
        this.elementType = elementType;
        this.sizeExpressions = sizeExpressions;
        this.initExpressions = initExpressions == null ? Collections.emptyList() : initExpressions;
        if (initExpressions == null) {
            if (sizeExpressions == null || sizeExpressions.isEmpty()) {
                throw new IllegalArgumentException("Either an initializer or defined size must be given");
            }
        }
        if (!this.initExpressions.isEmpty() && sizeExpressions != null && !sizeExpressions.isEmpty()) {
            throw new IllegalArgumentException("Both an initializer (" + formatInitExpressions() +
                    ") and a defined size (" + formatSizeExpressions() + ") cannot be given");
        }
        for (Object item : this.initExpressions) {
            if (item != null && !(item instanceof Expression)) {
                throw new ClassCastException("Item: " + item + " is not an Expression");
            }
        }
        if (!hasInitializer()) {
            for (Object item : sizeExpressions) {
                if (!(item instanceof Expression)) {
                    throw new ClassCastException("Item: " + item + " is not an Expression");
                }
            }
        }
    }

    /**
     * Creates an array using an initializer (list of expressions corresponding to array elements)
     */
    public ArrayExpression(ClassNode elementType, List<Expression> initExpressions) {
        this(elementType, initExpressions, null);
    }

    /**
     * Add another element to the initializer expressions
     */
    public void addExpression(Expression initExpression) {
        initExpressions.add(initExpression);
    }

    /**
     * Get the initializer expressions
     */
    public List<Expression> getExpressions() {
        return initExpressions;
    }

    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitArrayExpression(this);
    }

    public boolean isDynamic() {
        return false;
    }

    public Expression transformExpression(ExpressionTransformer transformer) {
        List<Expression> exprList = transformExpressions(initExpressions, transformer);
        List<Expression> sizes = null;
        if (!hasInitializer()) {
            sizes = transformExpressions(sizeExpressions, transformer);
        }
        Expression ret = new ArrayExpression(elementType, exprList, sizes);
        ret.setSourcePosition(this);
        ret.copyNodeMetaData(this);
        return ret;
    }

    /**
     * Get a particular initializer expression
     */
    public Expression getExpression(int i) {
        return initExpressions.get(i);
    }

    public ClassNode getElementType() {
        return elementType;
    }

    public String getText() {
        return "[" + formatInitExpressions() + "]";
    }

    private String formatInitExpressions() {
        return "{" + initExpressions.stream().map(Expression::getText).collect(Collectors.joining(", ")) + "}";
    }

    private String formatSizeExpressions() {
        return sizeExpressions.stream().map(e -> "[" + e.getText() + "]").collect(Collectors.joining());
    }

    /**
     * @return true if the array expression is defined by an explicit initializer
     */
    public boolean hasInitializer() {
        return sizeExpressions == null;
    }

    /**
     * @return a list with elements corresponding to the array's dimensions
     */
    public List<Expression> getSizeExpression() {
        return sizeExpressions;
    }

    public String toString() {
        if (hasInitializer()) {
            return super.toString() + "[elementType: " + getElementType() + ", init: {" + formatInitExpressions() + "}]";
        }
        return super.toString() + "[elementType: " + getElementType() + ", size: " + formatSizeExpressions() + "]";
    }
}
