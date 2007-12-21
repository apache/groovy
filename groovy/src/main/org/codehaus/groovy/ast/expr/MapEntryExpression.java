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

import org.codehaus.groovy.ast.GroovyCodeVisitor;


/**
 * Represents an entry inside a map expression such as 1 : 2.
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class MapEntryExpression extends Expression {
    private Expression keyExpression;
    private Expression valueExpression;

    public MapEntryExpression(Expression keyExpression, Expression valueExpression) {
        this.keyExpression = keyExpression;
        this.valueExpression = valueExpression;
    }

    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitMapEntryExpression(this);
    }

    public Expression transformExpression(ExpressionTransformer transformer) {
        Expression ret = new MapEntryExpression(transformer.transform(keyExpression), transformer.transform(valueExpression));
        ret.setSourcePosition(this);
        return ret;        
    }

    public String toString() {
        return super.toString() + "(key: " + keyExpression + ", value: " + valueExpression + ")";
    }

    public Expression getKeyExpression() {
        return keyExpression;
    }

    public Expression getValueExpression() {
        return valueExpression;
    }

}
