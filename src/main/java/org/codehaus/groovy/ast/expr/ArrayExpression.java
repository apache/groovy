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
 * Represents an array object construction.
 * One of:
 * <ul>
 *     <li>a fixed size array (e.g. {@code new String[3]} or {@code new Integer[2][3])}</li>
 *     <li>an array with an explicit initializer (e.g. {@code new String[]&#123; "foo", "bar" &#125;})</li>
 * </ul>
 */
public class ArrayExpression extends Expression {

    private final ClassNode elementType;
    private final List<Expression> initExpressions;
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
     * Creates an array using an initializer (list of expressions corresponding to array elements).
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

    public ClassNode getElementType() {
        return elementType;
    }

    /**
     * Gets the initializer expressions.
     */
    public List<Expression> getExpressions() {
        return initExpressions;
    }

    /**
     * Gets a specific initializer expression.
     */
    public Expression getExpression(final int i) {
        return initExpressions.get(i);
    }

    /**
     * Adds another element to the initializer expressions.
     */
    public void addExpression(final Expression initExpression) {
        initExpressions.add(initExpression);
    }

    /**
     * @return a list with elements corresponding to the array's dimensions
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
     * @return true if the array expression is defined by an explicit initializer
     */
    public boolean hasInitializer() {
        return (sizeExpressions == null);
    }

    private String formatInitExpressions() {
        return initExpressions.stream().map(e -> e.getText()).collect(Collectors.joining(", ", "{", "}"));
    }

    private String formatSizeExpressions() {
        return sizeExpressions.stream().map(e -> "[" + e.getText() + "]").collect(Collectors.joining());
    }
}
