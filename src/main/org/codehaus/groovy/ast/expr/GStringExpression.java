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

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.GroovyCodeVisitor;

/**
 * Represents a String expression which contains embedded values inside
 * it such as "hello there ${user} how are you" which is expanded lazily
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class GStringExpression extends Expression {

    private String verbatimText;
    private List<ConstantExpression> strings = new ArrayList<ConstantExpression>();
    private List<Expression> values = new ArrayList<Expression>();
    
    public GStringExpression(String verbatimText) {
        this.verbatimText = verbatimText;
        super.setType(ClassHelper.GSTRING_TYPE);
    }

    public GStringExpression(String verbatimText, List<ConstantExpression> strings, List<Expression> values) {
        this.verbatimText = verbatimText;
        this.strings = strings;
        this.values = values;
        super.setType(ClassHelper.GSTRING_TYPE);
    }

    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitGStringExpression(this);
    }

    public Expression transformExpression(ExpressionTransformer transformer) {
        Expression ret = new GStringExpression(
                verbatimText,
                transformExpressions(strings, transformer, ConstantExpression.class),
                transformExpressions(values, transformer));
        ret.setSourcePosition(this);
        ret.copyNodeMetaData(this);
        return ret;        
    }

    public String toString() {
        return super.toString() + "[strings: " + strings + " values: " + values + "]";
    }

    public String getText() {
        return verbatimText;
    }

    public List<ConstantExpression> getStrings() {
        return strings;
    }

    public List<Expression> getValues() {
        return values;
    }

    public void addString(ConstantExpression text) {
        if (text == null) {
            throw new NullPointerException("Cannot add a null text expression");
        }
        strings.add(text);
    }

    public void addValue(Expression value) {
        // If the first thing is an value, then we need a dummy empty string in front of it so that when we
        // toString it they come out in the correct order.
        if (strings.size() == 0)
            strings.add(ConstantExpression.EMPTY_STRING);
        values.add(value);
    }

    public Expression getValue(int idx) {
        return values.get(idx);
    }

    public boolean isConstantString() {
        return values.isEmpty();
    }

    public Expression asConstantString() {
        StringBuffer buffer = new StringBuffer();
        for (ConstantExpression expression : strings) {
            Object value = expression.getValue();
            if (value != null) {
                buffer.append(value);
            }
        }
        return new ConstantExpression(buffer.toString());
    }
}
