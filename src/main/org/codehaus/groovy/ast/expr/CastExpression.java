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

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;

/**
 * Represents a type cast expression
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class CastExpression extends Expression {
    
    private final Expression expression;
    private boolean ignoreAutoboxing=false;
    private boolean coerce = false;

    public static CastExpression asExpression(ClassNode type, Expression expression) {
        CastExpression answer = new CastExpression(type, expression);
        answer.setCoerce(true);
        return answer;
    }

    public CastExpression(ClassNode type, Expression expression) {
        this(type,expression,false);
    }

    public CastExpression(ClassNode type, Expression expression, boolean ignoreAutoboxing) {
        super.setType(type);
        this.expression = expression;
        this.ignoreAutoboxing = ignoreAutoboxing;
    }
    
    public boolean isIgnoringAutoboxing(){
        return ignoreAutoboxing;
    }

    public boolean isCoerce() {
        return coerce;
    }

    public void setCoerce(boolean coerce) {
        this.coerce = coerce;
    }

    public String toString() {
        return super.toString() +"[(" + getType().getName() + ") " + expression + "]";
    }

    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitCastExpression(this);
    }

    public Expression transformExpression(ExpressionTransformer transformer) {
        CastExpression ret =  new CastExpression(getType(), transformer.transform(expression));
        ret.setSourcePosition(this);
        ret.setCoerce(this.isCoerce());
        return ret;
    }
    
    public String getText() {
        return "(" + getType() + ") " + expression.getText();
    }
 
    public Expression getExpression() {
        return expression;
    }
    
    public void setType(ClassNode t) {
        super.setType(t);
    }

}
