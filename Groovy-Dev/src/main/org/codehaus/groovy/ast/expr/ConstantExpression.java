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

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.GroovyCodeVisitor;

/**
 * Represents a constant expression such as null, true, false
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class ConstantExpression extends Expression {
    // The following fields are only used internally; every occurrence of a user-defined expression of the same kind
    // has its own instance so as to preserve line information. Consequently, to test for such an expression, don't
    // compare against the field but call isXXXExpression() instead.
    public static final ConstantExpression NULL = new ConstantExpression(null);
    public static final ConstantExpression TRUE = new ConstantExpression(Boolean.TRUE);
    public static final ConstantExpression FALSE = new ConstantExpression(Boolean.FALSE);
    public static final ConstantExpression EMPTY_STRING = new ConstantExpression("");
    //public static final Expression EMPTY_ARRAY = new PropertyExpression(new ClassExpression(ArgumentListExpression.class.getName()), "EMPTY_ARRAY");

    // the following fields are only used internally; there are no user-defined expressions of the same kind
    public static final ConstantExpression VOID = new ConstantExpression(Void.class);
    public static final ConstantExpression EMTPY_EXPRESSION = new ConstantExpression(null);
    
    private Object value;
    private String constantName;
    
    public ConstantExpression(Object value) {
        this.value = value;
        if (this.value != null)
            setType(ClassHelper.make(value.getClass()));
    }

    public String toString() {
        return "ConstantExpression[" + value + "]";
    }

    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitConstantExpression(this);
    }


    public Expression transformExpression(ExpressionTransformer transformer) {
        return this;
    }

    /**
     * @return the value of this constant expression
     */    
    public Object getValue() {
        return value;
    }

    public String getText() {
        return (value == null) ? "null" : value.toString();
    }

    public String getConstantName() {
        return constantName;
    }

    public void setConstantName(String constantName) {
        this.constantName = constantName;
    }

    public boolean isNullExpression() {
        return value == null;
    }

    public boolean isTrueExpression() {
        return Boolean.TRUE.equals(value);
    }

    public boolean isFalseExpression() {
        return Boolean.FALSE.equals(value);
    }

    public boolean isEmptyStringExpression() {
        return "".equals(value);
    }
}
