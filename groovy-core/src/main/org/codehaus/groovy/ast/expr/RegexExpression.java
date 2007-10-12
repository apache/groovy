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
 * Represents a regular expression of the form ~<double quoted string> which creates
 * a regular expression. 
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class RegexExpression extends Expression {
    
    private final Expression string;
    
    public RegexExpression(Expression string) {
        this.string = string;
        super.setType(ClassHelper.PATTERN_TYPE);
    }
    
    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitRegexExpression(this);
    }

    public Expression transformExpression(ExpressionTransformer transformer) {
        Expression ret = new RegexExpression(transformer.transform(string)); 
        ret.setSourcePosition(this);
        return ret;       
    }

    public Expression getRegex() {
        return string;
    }

}
