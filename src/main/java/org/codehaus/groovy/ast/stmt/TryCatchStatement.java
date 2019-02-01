/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.ast.stmt;

import org.codehaus.groovy.ast.GroovyCodeVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a try { ... } catch () finally {} statement in Groovy
 */
public class TryCatchStatement extends Statement {

    private Statement tryStatement;
    private List<CatchStatement> catchStatements = new ArrayList<CatchStatement>();
    private Statement finallyStatement;
    

    public TryCatchStatement(Statement tryStatement, Statement finallyStatement) {
        this.tryStatement = tryStatement;
        this.finallyStatement = finallyStatement;
    }
    
    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitTryCatchFinally(this);
    }
    
    public List<CatchStatement> getCatchStatements() {
        return catchStatements;
    }

    public Statement getFinallyStatement() {
        return finallyStatement;
    }

    public Statement getTryStatement() {
        return tryStatement;
    }

    public void addCatch(CatchStatement catchStatement) {
        catchStatements.add(catchStatement);
    }

    /**
     * @return the catch statement of the given index or null
     */
    public CatchStatement getCatchStatement(int idx) {
        if (idx >= 0 && idx < catchStatements.size()) {
            return catchStatements.get(idx);
        }
        return null;
    }

    public void setTryStatement(Statement tryStatement) {
        this.tryStatement = tryStatement;
    }

    public void setCatchStatement(int idx, CatchStatement catchStatement) {
        catchStatements.set(idx, catchStatement);
    }

    public void setFinallyStatement(Statement finallyStatement) {
        this.finallyStatement = finallyStatement;
    }
}
