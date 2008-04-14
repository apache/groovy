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
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;

/**
 * Represents a ternary expression (booleanExpression) ? expression : expression
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class TernaryExpression extends Expression {

    private BooleanExpression booleanExpression;
    private Expression trueExpression;
    private Expression falseExpression;

    public TernaryExpression(
        BooleanExpression booleanExpression,
        Expression trueExpression,
        Expression falseExpression) {
        this.booleanExpression = booleanExpression;
        this.trueExpression = trueExpression;
        this.falseExpression = falseExpression;
    }
    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitTernaryExpression(this);
    }

    public Expression transformExpression(ExpressionTransformer transformer) {
        Expression ret = new TernaryExpression(
                (BooleanExpression) transformer.transform(booleanExpression),
                transformer.transform(trueExpression),
                transformer.transform(falseExpression)); 
        ret.setSourcePosition(this);
        return ret; 
    }

    public String toString() {
        return super.toString() +"[" + booleanExpression + " ? " + trueExpression + " : " + falseExpression + "]";
    }
    
    public BooleanExpression getBooleanExpression() {
        return booleanExpression;
    }

    public Expression getFalseExpression() {
        return falseExpression;
    }

    public Expression getTrueExpression() {
        return trueExpression;
    }

    public String getText() {
        return "("
            + booleanExpression.getText()
            + ") ? "
            + trueExpression.getText()
            + " : "
            + falseExpression.getText();
    }

    public ClassNode getType() {
        return ClassHelper.OBJECT_TYPE;
    }
}
