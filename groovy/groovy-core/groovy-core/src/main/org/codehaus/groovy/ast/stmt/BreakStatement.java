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
package org.codehaus.groovy.ast.stmt;

import org.codehaus.groovy.ast.GroovyCodeVisitor;


/**
 * Represents a break statement in a switch or loop statement
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class BreakStatement extends Statement {

    private String label;
    
    public BreakStatement() {
        this(null);
    }
    
    public BreakStatement(String label) {
        this.label = label;
    }
    
    public String getLabel() {
        return label;
    }

    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitBreakStatement(this);
    }
}
