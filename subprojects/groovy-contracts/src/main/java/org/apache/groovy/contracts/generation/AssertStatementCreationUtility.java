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
package org.apache.groovy.contracts.generation;

import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.AssertStatement;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.SourceUnit;

import java.util.ArrayList;
import java.util.List;

import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.declS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.localVarX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;

/**
 * Central place to create {@link org.codehaus.groovy.ast.stmt.AssertStatement} instances in groovy-contracts.
 * Utilized to centralize {@link AssertionError} message generation.
 *
 * @see org.codehaus.groovy.ast.stmt.AssertStatement
 * @see AssertionError
 */
public final class AssertStatementCreationUtility {

    /**
     * Reusable method for creating assert statements for the given <tt>booleanExpression</tt>.
     *
     * @param booleanExpressions the assertion's {@link org.codehaus.groovy.ast.expr.BooleanExpression} instances
     * @return a newly created {@link org.codehaus.groovy.ast.stmt.AssertStatement}
     */
    public static BlockStatement getAssertionStatements(final List<BooleanExpression> booleanExpressions) {

        List<Statement> assertStatements = new ArrayList<>();
        for (BooleanExpression booleanExpression : booleanExpressions) {
            assertStatements.add(getAssertionStatement(booleanExpression));
        }

        final BlockStatement blockStatement = block();
        blockStatement.getStatements().addAll(assertStatements);

        return blockStatement;
    }

    /**
     * Reusable method for creating assert statements for the given <tt>booleanExpression</tt>.
     *
     * @param booleanExpression the assertion's {@link org.codehaus.groovy.ast.expr.BooleanExpression}
     * @return a newly created {@link org.codehaus.groovy.ast.stmt.AssertStatement}
     */
    public static AssertStatement getAssertionStatement(final BooleanExpression booleanExpression) {

        final AssertStatement assertStatement = new AssertStatement(booleanExpression);
        assertStatement.setStatementLabel(booleanExpression.getNodeMetaData("statementLabel"));
        assertStatement.setSourcePosition(booleanExpression);

        return assertStatement;
    }

    /**
     * Gets a list of {@link org.codehaus.groovy.ast.stmt.ReturnStatement} instances from the given {@link MethodNode}.
     *
     * @param method the {@link org.codehaus.groovy.ast.MethodNode} that holds the given <tt>lastStatement</tt>
     * @return a {@link org.codehaus.groovy.ast.stmt.ReturnStatement} or <tt>null</tt>
     */
    public static List<ReturnStatement> getReturnStatements(MethodNode method) {

        final ReturnStatementVisitor returnStatementVisitor = new ReturnStatementVisitor();
        returnStatementVisitor.visitMethod(method);

        final List<ReturnStatement> returnStatements = returnStatementVisitor.getReturnStatements();
        final BlockStatement blockStatement = (BlockStatement) method.getCode();

        if (returnStatements.isEmpty()) {
            final int statementCount = blockStatement.getStatements().size();
            if (statementCount > 0) {
                final Statement lastStatement = blockStatement.getStatements().get(statementCount - 1);
                if (lastStatement instanceof ExpressionStatement) {
                    final ReturnStatement returnStatement = new ReturnStatement((ExpressionStatement) lastStatement);
                    returnStatement.setSourcePosition(lastStatement);
                    blockStatement.getStatements().remove(lastStatement);
                    blockStatement.addStatement(returnStatement);
                    returnStatements.add(returnStatement);
                }
            }
        }

        return returnStatements;
    }

    public static void injectResultVariableReturnStatementAndAssertionCallStatement(BlockStatement statement, ClassNode returnType, ReturnStatement returnStatement, BlockStatement assertionCallStatement) {
        final AddResultReturnStatementVisitor addResultReturnStatementVisitor = new AddResultReturnStatementVisitor(returnStatement, returnType, assertionCallStatement);
        addResultReturnStatementVisitor.visitBlockStatement(statement);
    }

    public static void addAssertionCallStatementToReturnStatement(BlockStatement statement, ReturnStatement returnStatement, Statement assertionCallStatement) {
        final AddAssertionCallStatementToReturnStatementVisitor addAssertionCallStatementToReturnStatementVisitor = new AddAssertionCallStatementToReturnStatementVisitor(returnStatement, assertionCallStatement);
        addAssertionCallStatementToReturnStatementVisitor.visitBlockStatement(statement);
    }

    /**
     * Collects all {@link ReturnStatement} instances from a given code block.
     */
    public static class ReturnStatementVisitor extends ClassCodeVisitorSupport {

        private final List<ReturnStatement> returnStatements = new ArrayList<>();

        @Override
        protected SourceUnit getSourceUnit() {
            return null;
        }

        @Override
        public void visitReturnStatement(ReturnStatement statement) {
            returnStatements.add(statement);
        }

        @Override
        public void visitClosureExpression(ClosureExpression expression) {
            // do nothing to prevent getting return statements from closures
        }

        public List<ReturnStatement> getReturnStatements() {
            return returnStatements;
        }
    }

    /**
     * Replaces a given {@link ReturnStatement} with the appropriate assertion call statement and returns a result variable expression.
     */
    public static class AddResultReturnStatementVisitor extends ClassCodeVisitorSupport {

        @Override
        protected SourceUnit getSourceUnit() {
            return null;
        }

        private final ReturnStatement returnStatement;
        private final ClassNode returnType;
        private final BlockStatement assertionCallBlock;

        public AddResultReturnStatementVisitor(ReturnStatement returnStatement, ClassNode returnType, BlockStatement assertionCallBlock) {
            this.returnStatement = returnStatement;
            this.returnType = returnType;
            this.assertionCallBlock = assertionCallBlock;
        }

        @Override
        public void visitBlockStatement(BlockStatement block) {

            List<Statement> blockStatementsCopy = new ArrayList<>(block.getStatements());

            for (Statement statement : blockStatementsCopy) {
                if (statement == returnStatement) {
                    block.getStatements().remove(statement);
                    block.addStatements(assertionCallBlock.getStatements());

                    VariableExpression variableExpression = localVarX("result", returnType);

                    block.addStatement(returnS(variableExpression));
                    return; // we found the return statement under target, let's cancel tree traversal
                }
            }

            super.visitBlockStatement(block);
        }
    }

    /**
     * Replaces a given {@link ReturnStatement} with the appropriate assertion call statement and returns a result variable expression.
     */
    public static class AddAssertionCallStatementToReturnStatementVisitor extends ClassCodeVisitorSupport {

        @Override
        protected SourceUnit getSourceUnit() {
            return null;
        }

        private final ReturnStatement returnStatement;
        private final Statement assertionCallStatement;

        public AddAssertionCallStatementToReturnStatementVisitor(ReturnStatement returnStatement, Statement assertionCallStatement) {
            this.returnStatement = returnStatement;
            this.assertionCallStatement = assertionCallStatement;
        }

        @Override
        public void visitBlockStatement(BlockStatement block) {
            List<Statement> blockStatementsCopy = new ArrayList<>(block.getStatements());

            for (Statement statement : blockStatementsCopy) {
                if (statement == returnStatement) {
                    block.getStatements().remove(statement);

                    final VariableExpression $_gc_result = localVarX("$_gc_result", ClassHelper.DYNAMIC_TYPE);
                    block.addStatement(declS($_gc_result, returnStatement.getExpression()));
                    block.addStatement(assertionCallStatement);

                    final Statement gcResultReturn = returnS($_gc_result);
                    gcResultReturn.setSourcePosition(returnStatement);
                    block.addStatement(gcResultReturn);
                    return; // we found the return statement under target, let's cancel tree traversal
                }
            }

            super.visitBlockStatement(block);
        }
    }
}
