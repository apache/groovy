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
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.AssertStatement;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.DoWhileStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.WhileStatement;
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

    /**
     * Rewrites the target return statement so it stores the result value, executes the assertion block,
     * and then returns the stored result.
     *
     * @param statement the surrounding block to rewrite
     * @param returnType the declared return type
     * @param returnStatement the return statement to replace
     * @param assertionCallStatement the assertion block to insert
     */
    public static void injectResultVariableReturnStatementAndAssertionCallStatement(BlockStatement statement, ClassNode returnType, ReturnStatement returnStatement, BlockStatement assertionCallStatement) {
        ensureReturnStatementInBlock(statement, returnStatement);
        final AddResultReturnStatementVisitor addResultReturnStatementVisitor = new AddResultReturnStatementVisitor(returnStatement, returnType, assertionCallStatement);
        addResultReturnStatementVisitor.visitBlockStatement(statement);
    }

    /**
     * Rewrites the target return statement so it evaluates the return expression once, executes the
     * assertion call, and finally returns the stored value.
     *
     * @param statement the surrounding block to rewrite
     * @param returnStatement the return statement to replace
     * @param assertionCallStatement the assertion statement to insert
     */
    public static void addAssertionCallStatementToReturnStatement(BlockStatement statement, ReturnStatement returnStatement, Statement assertionCallStatement) {
        ensureReturnStatementInBlock(statement, returnStatement);
        final AddAssertionCallStatementToReturnStatementVisitor addAssertionCallStatementToReturnStatementVisitor = new AddAssertionCallStatementToReturnStatementVisitor(returnStatement, assertionCallStatement);
        addAssertionCallStatementToReturnStatementVisitor.visitBlockStatement(statement);
    }

    /**
     * Ensures the target {@code returnStatement} is a direct child of a {@link BlockStatement} so the
     * rewriting visitors below can find and replace it.
     * <p>
     * A single-statement {@code if}/{@code else} or loop branch (e.g. {@code if (n <= 1) return acc})
     * holds the {@link ReturnStatement} directly rather than inside a block. The rewriters only match
     * returns that are members of a {@link BlockStatement}, so without this normalization the
     * postcondition (and class-invariant) assertion is silently skipped for such returns. The gap is
     * usually masked in recursive methods by a sibling {@code return <recursiveCall>} that is a block
     * member, but {@code @TailRecursive} converts that call into a {@code continue}, leaving only the
     * braceless branch return (GROOVY-12079).
     *
     * @param root            the method body to scan
     * @param returnStatement the return statement that must end up inside a block
     */
    private static void ensureReturnStatementInBlock(BlockStatement root, final ReturnStatement returnStatement) {
        final VariableScope scope = root.getVariableScope();
        ClassCodeVisitorSupport normalizer = new ClassCodeVisitorSupport() {
            @Override
            protected SourceUnit getSourceUnit() {
                return null;
            }

            private Statement wrapIfTarget(Statement branch) {
                return branch == returnStatement ? block(new VariableScope(scope), returnStatement) : branch;
            }

            @Override
            public void visitIfElse(IfStatement ifElse) {
                ifElse.setIfBlock(wrapIfTarget(ifElse.getIfBlock()));
                ifElse.setElseBlock(wrapIfTarget(ifElse.getElseBlock()));
                super.visitIfElse(ifElse);
            }

            @Override
            public void visitWhileLoop(WhileStatement loop) {
                loop.setLoopBlock(wrapIfTarget(loop.getLoopBlock()));
                super.visitWhileLoop(loop);
            }

            @Override
            public void visitForLoop(ForStatement loop) {
                loop.setLoopBlock(wrapIfTarget(loop.getLoopBlock()));
                super.visitForLoop(loop);
            }

            @Override
            public void visitDoWhileLoop(DoWhileStatement loop) {
                loop.setLoopBlock(wrapIfTarget(loop.getLoopBlock()));
                super.visitDoWhileLoop(loop);
            }

            @Override
            public void visitClosureExpression(ClosureExpression expression) {
                // returns inside closures belong to the closure, not the surrounding method
            }
        };
        normalizer.visitBlockStatement(root);
    }

    /**
     * Collects all {@link ReturnStatement} instances from a given code block.
     */
    public static class ReturnStatementVisitor extends ClassCodeVisitorSupport {

        private final List<ReturnStatement> returnStatements = new ArrayList<>();

        /**
         * This visitor is source-independent.
         *
         * @return {@code null}
         */
        @Override
        protected SourceUnit getSourceUnit() {
            return null;
        }

        /**
         * Records one visited return statement.
         *
         * @param statement the return statement to collect
         */
        @Override
        public void visitReturnStatement(ReturnStatement statement) {
            returnStatements.add(statement);
        }

        /**
         * Skips nested closures so only returns from the surrounding method are collected.
         *
         * @param expression the closure expression to ignore
         */
        @Override
        public void visitClosureExpression(ClosureExpression expression) {
            // do nothing to prevent getting return statements from closures
        }

        /**
         * Returns the collected return statements.
         *
         * @return the collected return statements
         */
        public List<ReturnStatement> getReturnStatements() {
            return returnStatements;
        }
    }

    /**
     * Replaces a given {@link ReturnStatement} with the appropriate assertion call statement and returns a result variable expression.
     */
    public static class AddResultReturnStatementVisitor extends ClassCodeVisitorSupport {

        /**
         * This visitor is source-independent.
         *
         * @return {@code null}
         */
        @Override
        protected SourceUnit getSourceUnit() {
            return null;
        }

        private final ReturnStatement returnStatement;
        private final ClassNode returnType;
        private final BlockStatement assertionCallBlock;

        /**
         * Creates a visitor that rewrites one return statement to expose the {@code result} variable.
         *
         * @param returnStatement the return statement to replace
         * @param returnType the declared return type
         * @param assertionCallBlock the assertion block to inject before returning
         */
        public AddResultReturnStatementVisitor(ReturnStatement returnStatement, ClassNode returnType, BlockStatement assertionCallBlock) {
            this.returnStatement = returnStatement;
            this.returnType = returnType;
            this.assertionCallBlock = assertionCallBlock;
        }

        /**
         * Rewrites the block containing the target return statement.
         *
         * @param block the block being visited
         */
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

        /**
         * This visitor is source-independent.
         *
         * @return {@code null}
         */
        @Override
        protected SourceUnit getSourceUnit() {
            return null;
        }

        private final ReturnStatement returnStatement;
        private final Statement assertionCallStatement;

        /**
         * Creates a visitor that rewrites one return statement to execute an assertion call before returning.
         *
         * @param returnStatement the return statement to replace
         * @param assertionCallStatement the assertion statement to insert
         */
        public AddAssertionCallStatementToReturnStatementVisitor(ReturnStatement returnStatement, Statement assertionCallStatement) {
            this.returnStatement = returnStatement;
            this.assertionCallStatement = assertionCallStatement;
        }

        /**
         * Rewrites the block containing the target return statement.
         *
         * @param block the block being visited
         */
        @Override
        public void visitBlockStatement(BlockStatement block) {
            List<Statement> blockStatementsCopy = new ArrayList<>(block.getStatements());

            for (Statement statement : blockStatementsCopy) {
                if (statement == returnStatement) {
                    block.getStatements().remove(statement);

                    final VariableExpression gcResult = localVarX("$_gc_result", ClassHelper.OBJECT_TYPE);
                    block.addStatement(declS(gcResult, returnStatement.getExpression()));
                    block.addStatement(assertionCallStatement);

                    final Statement gcResultReturn = returnS(gcResult);
                    gcResultReturn.setSourcePosition(returnStatement);
                    block.addStatement(gcResultReturn);
                    return; // we found the return statement under target, let's cancel tree traversal
                }
            }

            super.visitBlockStatement(block);
        }
    }
}
