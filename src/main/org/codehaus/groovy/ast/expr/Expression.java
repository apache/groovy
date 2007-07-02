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

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;

/**
 * Represents a base class for expressions which evaluate as an object
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public abstract class Expression extends ASTNode {

    private ClassNode type=ClassHelper.DYNAMIC_TYPE;
    
    /**
     * Return a copy of the expression calling the transformer on any nested expressions 
     * @param transformer
     */
    public abstract Expression transformExpression(ExpressionTransformer transformer);

    /**
     * Transforms the list of expressions
     * @return a new list of transformed expressions
     */
    protected List transformExpressions(List expressions, ExpressionTransformer transformer) {
        List list = new ArrayList(expressions.size());
        for (Iterator iter = expressions.iterator(); iter.hasNext(); ) {
            list.add(transformer.transform((Expression) iter.next()));
        }
        return list;
    }
    
    public ClassNode getType() {
        return type;
    }
    
    public void setType(ClassNode t) {
        type=t;
    }
}
