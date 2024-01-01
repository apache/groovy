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
package org.codehaus.groovy.classgen;

import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.BreakStatement;
import org.codehaus.groovy.ast.stmt.ContinueStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.ThrowStatement;
import org.codehaus.groovy.control.SourceUnit;

/**
 * Analyze AST for dead code
 *
 * @since 5.0.0
 */
public class DeadCodeAnalyzer extends ClassCodeVisitorSupport {

    private final SourceUnit sourceUnit;

    public DeadCodeAnalyzer(final SourceUnit sourceUnit) {
        this.sourceUnit = sourceUnit;
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return sourceUnit;
    }
    @Override
    public void visitBlockStatement(BlockStatement statement) {
        analyzeDeadCode(statement);
        super.visitBlockStatement(statement);
    }

    private void analyzeDeadCode(BlockStatement block) {
        int foundCnt = 0;
        for (Statement statement : block.getStatements()) {
            if (statement instanceof ReturnStatement
                || statement instanceof BreakStatement
                || statement instanceof ContinueStatement
                || statement instanceof ThrowStatement) {
                foundCnt++;
                if (1 == foundCnt) continue;
            }

            if (foundCnt > 0) {
                addError("Unreachable statement found", statement);
            }
        }
    }
}
