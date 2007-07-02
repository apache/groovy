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

import java.util.Iterator;
import java.util.Map;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;


/**
 * Represents an annotation "constant" that may appear in annotation attributes 
 * (mainly used as a marker).
 * 
 * @author <a href='mailto:the[dot]mindstorm[at]gmail[dot]com'>Alex Popescu</a>
 * @version $Revision: 3264 $
 */
public class AnnotationConstantExpression extends ConstantExpression {
    public AnnotationConstantExpression(AnnotationNode node) {
        super(node);
        setType(node.getClassNode());
    }
    
    public void visit(GroovyCodeVisitor visitor) {
        AnnotationNode node = (AnnotationNode) getValue();
        Map attrs = node.getMembers();
        for(Iterator it = attrs.values().iterator(); it.hasNext(); ) {
            ((Expression) it.next()).visit(visitor);
        }
    }
}
