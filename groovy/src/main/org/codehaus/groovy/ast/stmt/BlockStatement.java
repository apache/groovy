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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.VariableScope;

/**
 * A list of statements
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class BlockStatement extends Statement {

    private List statements = new ArrayList();
    private VariableScope scope;
    
    public BlockStatement() {
        this(new ArrayList(), new VariableScope());
    }
    
    public BlockStatement(List statements, VariableScope scope) {
        this.statements = statements;
        this.scope = scope;
    }
    
    public BlockStatement(Statement[] statements, VariableScope scope) {
        this.statements.addAll(Arrays.asList(statements));
        this.scope = scope;
    }

    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitBlockStatement(this);
    }

    public List getStatements() {
        return statements;
    }

    public void addStatement(Statement statement) {
        statements.add(statement);
    }

    public void addStatements(List listOfStatements) {
        statements.addAll(listOfStatements);
    }

    public String toString() {
        return super.toString() + statements;
    }

    public String getText() {
        StringBuffer buffer = new StringBuffer("{ ");
        boolean first = true;
        for (Iterator iter = statements.iterator(); iter.hasNext(); ) {
            if (first) {
                first = false;
            }
            else {
                buffer.append("; ");
            }
            Statement statement = (Statement) iter.next();
            buffer.append(statement.getText());
        }
        buffer.append(" }");
        return buffer.toString();
    }

    public boolean isEmpty() {
        return statements.isEmpty();
    }

    public void setVariableScope(VariableScope scope) {
        this.scope = scope;
    }
    
    public VariableScope getVariableScope() {
        return scope;
    }
}
