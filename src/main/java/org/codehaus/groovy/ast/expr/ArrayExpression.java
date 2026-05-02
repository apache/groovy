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

import static org.apache.groovy.ast.tools.ClassNodeUtils.formatTypeName;

/**
 * Represents an array literal or array construction expression.
 * Supports both fixed-size array construction (e.g., {@code new String[3]} or {@code new Integer[2][3]})
 * and array initialization with explicit elements (e.g., {@code new String[] { "foo", "bar" }}).
 * The expression may be either an initializer-based array or a size-based array, but not both.
 * 
 * @see {@link ClassNode} for array type representation
 * @see {@link ConstantExpression} for element expressions
 */
public class ArrayExpression extends Expression {

    /**
     * The element type of the array (the base type before applying array dimensions).
     */
    private final ClassNode elementType;
    /**
     * The initializer expressions for array elements (non-empty only for initializer-based arrays).
     */
    private final List<Expression> initExpressions;
    /**
     * The size expressions for each dimension (non-empty only for size-based arrays).
     */
    private final List<Expression> sizeExpressions;

    private static ClassNode makeArray(final ClassNode base, final List<Expression> sizeExpressions) {
        ClassNode ret = base.makeArray();
        if (sizeExpressions == null) return ret;
        int size = sizeExpressions.size();
        for (int i = 1; i < size; i++) {
            ret = ret.makeArray();
        }
        return ret;
    }

    /**
     * Constructs an array expression with either size expressions or initializer expressions.
     * 
     * @param elementType the base element type of the array (non-null)
     * @param initExpressions the list of initializer expressions for array elements, or null for size-based construction
     * @param sizeExpressions the list of size expressions (one per dimension) for fixed-size arrays, or null for initializer-based construction
     * @throws IllegalArgumentException if both initExpressions and sizeExpressions are provided or both are null/empty
     * @throws IllegalArgumentException if any initializer is not an {@link Expression}
     */
    public ArrayExpression(final ClassNode elementType, final List<Expression> initExpressions, final List<Expression> sizeExpressions) {
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
     * Creates an array using an initializer list of expressions corresponding to array elements.
     * 
     * @param elementType the base element type of the array (non-null)
     * @param initExpressions the list of initializer expressions for array elements (non-null)
     */
    public ArrayExpression(final ClassNode elementType, final List<Expression> initExpressions) {
        this(elementType, initExpressions, null);
    }

    @Override
    public Expression transformExpression(final ExpressionTransformer transformer) {
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

    @Override
    public void visit(final GroovyCodeVisitor visitor) {
        visitor.visitArrayExpression(this);
    }

    //--------------------------------------------------------------------------

    /**
     * Returns the base element type of the array (before array dimensions are applied).
     * 
     * @return the element type
     */
    public ClassNode getElementType() {
        return elementType;
    }

    /**
     * Returns the list of initializer expressions for array elements.
     * 
     * @return a list of initializer expressions (non-null but may be empty for size-based arrays)
     */
    public List<Expression> getExpressions() {
        return initExpressions;
    }

    /**
     * Returns the initializer expression at the specified index.
     * 
     * @param i the index of the element
     * @return the expression at the specified index
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public Expression getExpression(final int i) {
        return initExpressions.get(i);
    }

    /**
     * Adds an element to the initializer expressions.
     * 
     * @param initExpression the expression to add (non-null)
     */
    public void addExpression(final Expression initExpression) {
        initExpressions.add(initExpression);
    }

    /**
     * Returns the size expressions for each dimension of the array.
     * 
     * @return a list with one expression per array dimension (non-null for size-based arrays, null for initializer-based)
     */
    public List<Expression> getSizeExpression() {
        return sizeExpressions;
    }

    @Override
    public String getText() {
        if (hasInitializer()) {
            return "new " + formatTypeName(getType()) + formatInitExpressions();
        } else {
            ClassNode basicType = getElementType();
            while (basicType.isArray()) basicType= basicType.getComponentType();
            return "new " + formatTypeName(basicType) + formatSizeExpressions();
        }
    }

    @Override
    public String toString() {
        if (hasInitializer()) {
            return super.toString() + "[type: " + formatTypeName(getType()) + ", init: " + formatInitExpressions() + "]";
        } else {
            return super.toString() + "[type: " + formatTypeName(getType()) + ", size: " + formatSizeExpressions() + "]";
        }
    }

    public boolean isDynamic() {
        return false;
    }

    /**
     * Indicates whether this array is defined by an explicit initializer or by size expressions.
     * 
     * @return true if the array has an explicit initializer, false if defined by size expressions
     */
    public boolean hasInitializer() {
        return (sizeExpressions == null);
    }

    private String formatInitExpressions() {
        return initExpressions.stream().map(e -> e.getText()).collect(Collectors.joining(", ", "{", "}"));
    }

    private String formatSizeExpressions() {
        return sizeExpressions.stream().map(e -> "[" + (e == ConstantExpression.EMPTY_EXPRESSION ? "" : e.getText()) + "]").collect(Collectors.joining());
    }
}
