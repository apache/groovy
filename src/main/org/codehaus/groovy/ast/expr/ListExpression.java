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
import java.util.Iterator;
import java.util.List;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.GroovyCodeVisitor;

/**
 * Represents a list expression [1, 2, 3] which creates a mutable List
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class ListExpression extends Expression {
    private List expressions;
    private boolean wrapped = false;

    public ListExpression() {
        this(new ArrayList());
    }
    
    public ListExpression(List expressions) {
        this.expressions = expressions;
        //TODO: get the type's of the expressions to specify the
        // list type to List<X> if possible.
        setType(ClassHelper.LIST_TYPE);
    }
    
    public void addExpression(Expression expression) {
        expressions.add(expression);
    }
    
    public List getExpressions() {
        return expressions;
    }

    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitListExpression(this);
    }

    public Expression transformExpression(ExpressionTransformer transformer) {
        Expression ret = new ListExpression(transformExpressions(getExpressions(), transformer));
        ret.setSourcePosition(this);
        return ret;       
    }

    public Expression getExpression(int i) {
        return (Expression) expressions.get(i);
    }

    public void setWrapped(boolean value) {
    	wrapped = value;
    }
    
    public boolean isWrapped() {
    	return wrapped;
    }

    public String getText() {
        StringBuffer buffer = new StringBuffer("[");
        boolean first = true;
        for (Iterator iter = expressions.iterator(); iter.hasNext(); ) {
            if (first) {
                first = false;
            }
            else {
                buffer.append(", ");
            }
            
            buffer.append(((Expression)iter.next()).getText());
        }
        buffer.append("]");
        return buffer.toString();
    }

    public String toString() {
        return super.toString() + expressions;
    }
}
