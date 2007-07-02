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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;

/**
 * Represents an array object construction either using a fixed size
 * or an initializer expression
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class ArrayExpression extends Expression {
    private List expressions;
    private List sizeExpression;

    private ClassNode elementType;
    
    private static ClassNode makeArray(ClassNode base, List sizeExpression) {
    	ClassNode ret = base.makeArray();
    	if (sizeExpression==null) return ret;
    	int size = sizeExpression.size();
    	for (int i=1; i<size; i++) {
    		ret = ret.makeArray();
    	}
    	return ret;
    }
    
    public ArrayExpression(ClassNode elementType, List expressions, List sizeExpression) {
        //expect to get the elementType
        super.setType(makeArray(elementType,sizeExpression));
        if (expressions==null) expressions=Collections.EMPTY_LIST;
        this.elementType = elementType;
        this.expressions = expressions;
        this.sizeExpression = sizeExpression;
        
        for (Iterator iter = expressions.iterator(); iter.hasNext();) {
            Object item = iter.next();
            if (item!=null && !(item instanceof Expression)) {
                throw new ClassCastException("Item: " + item + " is not an Expression");
            }
        }
        if (sizeExpression!=null) {
	        for (Iterator iter = sizeExpression.iterator(); iter.hasNext();) {
	            Object item = iter.next();
	            if (!(item instanceof Expression)) {
	                throw new ClassCastException("Item: " + item + " is not an Expression");
	            }
	        }
        }
    }
    
    
    /**
     * Creates an array using an initializer expression
     */
    public ArrayExpression(ClassNode elementType, List expressions) {
        this(elementType,expressions,null);
    }

    public void addExpression(Expression expression) {
        expressions.add(expression);
    }

    public List getExpressions() {
        return expressions;
    }

    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitArrayExpression(this);
    }

    public boolean isDynamic() {
        return false;
    }

    public Expression transformExpression(ExpressionTransformer transformer) {
    	List exprList = transformExpressions(expressions, transformer);
    	List sizes = null;
    	if (sizeExpression!=null) sizes = transformExpressions(sizeExpression,transformer);
        Expression ret = new ArrayExpression(elementType, exprList, sizes);
        ret.setSourcePosition(this);
        return ret;
    }

    public Expression getExpression(int i) {
        Object object = expressions.get(i);
        return (Expression) object;
    }

    public ClassNode getElementType() {
        return elementType;
    }
    
    public String getText() {
        StringBuffer buffer = new StringBuffer("[");
        boolean first = true;
        for (Iterator iter = expressions.iterator(); iter.hasNext();) {
            if (first) {
                first = false;
            }
            else {
                buffer.append(", ");
            }

            buffer.append(((Expression) iter.next()).getText());
        }
        buffer.append("]");
        return buffer.toString();
    }

    public List getSizeExpression() {
        return sizeExpression;
    }

    public String toString() {
        return super.toString() + expressions;
    }
}
