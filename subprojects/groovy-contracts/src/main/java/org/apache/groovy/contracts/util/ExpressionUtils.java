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
package org.apache.groovy.contracts.util;

import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.AssertStatement;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.codehaus.groovy.ast.tools.GeneralUtils.AND;
import static org.codehaus.groovy.ast.tools.GeneralUtils.binX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.boolX;

/**
 * <p>Internal utility class for extracting a boolean expression from the given expression or statement.</p>
 *
 * @see ClosureExpression
 * @see BooleanExpression
 */
public class ExpressionUtils {

    /**
     * Returns all {@link BooleanExpression} instances found in the given {@link ClosureExpression}.
     */
    public static List<BooleanExpression> getBooleanExpression(ClosureExpression closureExpression) {
        if (closureExpression == null) return null;

        final BlockStatement closureBlockStatement = (BlockStatement) closureExpression.getCode();
        return getBooleanExpressions(closureBlockStatement);
    }

    /**
     * Returns all {@link BooleanExpression} instances found in the given {@link BlockStatement}.
     */
    private static List<BooleanExpression> getBooleanExpressions(BlockStatement closureBlockStatement) {
        final List<Statement> statementList = closureBlockStatement.getStatements();

        List<BooleanExpression> booleanExpressions = new ArrayList<BooleanExpression>();

        for (Statement stmt : statementList) {
            BooleanExpression tmp = null;

            if (stmt instanceof ExpressionStatement && ((ExpressionStatement) stmt).getExpression() instanceof BooleanExpression) {
                tmp = (BooleanExpression) ((ExpressionStatement) stmt).getExpression();
                tmp.setNodeMetaData("statementLabel", stmt.getStatementLabel());
            } else if (stmt instanceof ExpressionStatement) {
                Expression expression = ((ExpressionStatement) stmt).getExpression();
                tmp = boolX(expression);
                tmp.setSourcePosition(expression);
                tmp.setNodeMetaData("statementLabel", stmt.getStatementLabel());
            }

            booleanExpressions.add(tmp);
        }

        return booleanExpressions;
    }

    /**
     * Returns all {@link BooleanExpression} instances found in the given {@link BlockStatement}.
     */
    public static List<BooleanExpression> getBooleanExpressionsFromAssertionStatements(BlockStatement blockStatement) {
        AssertStatementCollector collector = new AssertStatementCollector();
        collector.visitBlockStatement(blockStatement);

        List<AssertStatement> assertStatements = collector.assertStatements;
        if (assertStatements.isEmpty()) return Collections.emptyList();

        List<BooleanExpression> booleanExpressions = new ArrayList<BooleanExpression>();
        for (AssertStatement assertStatement : assertStatements) {
            booleanExpressions.add(assertStatement.getBooleanExpression());
        }

        return booleanExpressions;
    }

    public static BooleanExpression getBooleanExpression(List<BooleanExpression> booleanExpressions) {
        if (booleanExpressions == null || booleanExpressions.isEmpty())
            return boolX(ConstantExpression.TRUE);

        BooleanExpression result = null;
        for (BooleanExpression booleanExpression : booleanExpressions) {
            if (result == null) {
                result = booleanExpression;
            } else {
                result = boolX(binX(result, AND, booleanExpression));
            }
        }

        return result;
    }

    static class AssertStatementCollector extends ClassCodeVisitorSupport implements Opcodes {

        public List<AssertStatement> assertStatements = new ArrayList<AssertStatement>();

        @Override
        public void visitAssertStatement(AssertStatement statement) {
            assertStatements.add(statement);
        }

        @Override
        protected SourceUnit getSourceUnit() {
            return null;
        }
    }
}
