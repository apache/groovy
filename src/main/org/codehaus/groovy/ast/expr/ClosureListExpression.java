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

import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.VariableScope;

/**
 * This class rerpresents a list of expressions used to 
 * create closures. Example:
 * <code>
 * def foo = (1;2;;)
 * </code>
 * The right side is a ClosureListExpression consisting of
 * two ConstantExpressions for the values 1 and 2, and two
 * EmptyStatement entries. The ClosureListExpression defines a new 
 * variable scope. All created Closures share this scope.
 * 
 * 
 * @author Jochen Theodorou
 */
public class ClosureListExpression extends ListExpression {

    private VariableScope scope;
    
    public ClosureListExpression(List expressions) {
        super(expressions);
        scope = new VariableScope();
    }
    
    public ClosureListExpression() {
        this(new ArrayList(3));
    }
    
    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitClosureListExpression(this);
    }
    
    public Expression transformExpression(ExpressionTransformer transformer) {
        Expression ret = new ClosureListExpression(transformExpressions(getExpressions(), transformer));
        ret.setSourcePosition(this);
        return ret;       
    }
    
    public void setVariableScope(VariableScope scope) {
        this.scope = scope;
    }
    
    public VariableScope getVariableScope() {
        return scope;
    }
    
    public String getText() {
        StringBuffer buffer = new StringBuffer("(");
        boolean first = true;
        for (Iterator iter = getExpressions().iterator(); iter.hasNext(); ) {
            if (first) {
                first = false;
            } else {
                buffer.append("; ");
            }
            
            buffer.append(((Expression)iter.next()).getText());
        }
        buffer.append(")");
        return buffer.toString();
    }
}
