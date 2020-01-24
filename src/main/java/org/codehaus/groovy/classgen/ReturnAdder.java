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

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.BreakStatement;
import org.codehaus.groovy.ast.stmt.CaseStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.ast.stmt.SynchronizedStatement;
import org.codehaus.groovy.ast.stmt.ThrowStatement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Utility class to add return statements.
 * <p>
 * Extracted from Verifier as it can be useful for some AST transformations.
 */
public class ReturnAdder {

    @FunctionalInterface
    public interface ReturnStatementListener {
        /**
         * Implement this method in order to be notified whenever a return statement is generated.
         */
        void returnStatementAdded(ReturnStatement returnStatement);
    }

    private static final ReturnStatementListener DEFAULT_LISTENER = returnStatement -> {
    };

    private final ReturnStatementListener listener;

    /**
     * If set to 'true', then returns are effectively added. This is useful whenever you just want
     * to check what returns are produced without eventually adding them.
     */
    private final boolean doAdd;

    public ReturnAdder() {
        this.listener = DEFAULT_LISTENER;
        this.doAdd = true;
    }

    public ReturnAdder(final ReturnStatementListener listener) {
        this.listener = Objects.requireNonNull(listener);
        this.doAdd = false;
    }

    /**
     * Adds return statements in method code whenever an implicit return is detected.
     * @param node the method node where to add return statements
     * @deprecated Use {@link #visitMethod(org.codehaus.groovy.ast.MethodNode)} instead
     */
    @Deprecated
    public static void addReturnIfNeeded(final MethodNode node) {
        ReturnAdder adder = new ReturnAdder();
        adder.visitMethod(node);
    }

    public void visitMethod(final MethodNode node) {
        Statement statement = node.getCode();
        if (!node.isVoidMethod()) {
            if (statement != null) { // happens with @interface methods
                Statement code = addReturnsIfNeeded(statement, node.getVariableScope());
                if (doAdd) node.setCode(code);
            }
        } else if (!node.isAbstract() && node.getReturnType().redirect() != ClassHelper.VOID_TYPE) {
            if (!(statement instanceof BytecodeSequence)) {
                BlockStatement newBlock = new BlockStatement();
                Statement code = node.getCode();
                if (code instanceof BlockStatement) {
                    newBlock.setVariableScope(((BlockStatement) code).getVariableScope());
                }
                if (statement instanceof BlockStatement) {
                    newBlock.addStatements(((BlockStatement) statement).getStatements());
                } else {
                    newBlock.addStatement(statement);
                }
                ReturnStatement returnStatement = ReturnStatement.RETURN_NULL_OR_VOID;
                listener.returnStatementAdded(returnStatement);
                newBlock.addStatement(returnStatement);
                newBlock.setSourcePosition(statement);
                if (doAdd) node.setCode(newBlock);
            }
        }
    }

    private Statement addReturnsIfNeeded(final Statement statement, final VariableScope scope) {
        if (statement instanceof ReturnStatement || statement instanceof ThrowStatement
                || statement instanceof BytecodeSequence) {
            return statement;
        }

        if (statement instanceof EmptyStatement || statement == null) {
            ReturnStatement returnStatement = new ReturnStatement(ConstantExpression.NULL);
            listener.returnStatementAdded(returnStatement);
            return returnStatement;
        }

        if (statement instanceof ExpressionStatement) {
            Expression expression = ((ExpressionStatement) statement).getExpression();
            ReturnStatement returnStatement = new ReturnStatement(expression);
            addLabelsToReturnStatement(statement, returnStatement);
            returnStatement.setSourcePosition(statement);
            listener.returnStatementAdded(returnStatement);
            return returnStatement;
        }

        if (statement instanceof SynchronizedStatement) {
            SynchronizedStatement syncStatement = (SynchronizedStatement) statement;
            Statement code = addReturnsIfNeeded(syncStatement.getCode(), scope);
            if (doAdd) syncStatement.setCode(code);
            return syncStatement;
        }

        if (statement instanceof IfStatement) {
            IfStatement ifElseStatement = (IfStatement) statement;
            Statement ifBlock = addReturnsIfNeeded(ifElseStatement.getIfBlock(), scope);
            Statement elseBlock = addReturnsIfNeeded(ifElseStatement.getElseBlock(), scope);
            if (doAdd) {
                ifElseStatement.setIfBlock(ifBlock);
                ifElseStatement.setElseBlock(elseBlock);
            }
            return ifElseStatement;
        }

        if (statement instanceof SwitchStatement) {
            SwitchStatement switchStatement = (SwitchStatement) statement;
            for (CaseStatement caseStatement : switchStatement.getCaseStatements()) {
                Statement code = adjustSwitchCaseCode(caseStatement.getCode(), scope, false);
                if (doAdd) caseStatement.setCode(code);
            }
            Statement defaultStatement = adjustSwitchCaseCode(switchStatement.getDefaultStatement(), scope, true);
            if (doAdd) switchStatement.setDefaultStatement(defaultStatement);
            return switchStatement;
        }

        if (statement instanceof TryCatchStatement) {
            TryCatchStatement tryCatchFinally = (TryCatchStatement) statement;
            boolean[] missesReturn = new boolean[1];
            new ReturnAdder(returnStatement -> missesReturn[0] = true)
                    .addReturnsIfNeeded(tryCatchFinally.getFinallyStatement(), scope);
            boolean hasFinally = !(tryCatchFinally.getFinallyStatement() instanceof EmptyStatement);

            // if there is no missing return in the finally block and the block exists
            // there is nothing to do
            if (hasFinally && !missesReturn[0]) return tryCatchFinally;

            // add returns to try and catch blocks
            Statement tryStatement = addReturnsIfNeeded(tryCatchFinally.getTryStatement(), scope);
            if (doAdd) tryCatchFinally.setTryStatement(tryStatement);
            for (CatchStatement catchStatement : tryCatchFinally.getCatchStatements()) {
                Statement code = addReturnsIfNeeded(catchStatement.getCode(), scope);
                if (doAdd) catchStatement.setCode(code);
            }
            return tryCatchFinally;
        }

        if (statement instanceof BlockStatement) {
            BlockStatement blockStatement = (BlockStatement) statement;
            if (blockStatement.isEmpty()) {
                ReturnStatement returnStatement = new ReturnStatement(ConstantExpression.NULL);
                addLabelsToReturnStatement(blockStatement, returnStatement);
                returnStatement.setSourcePosition(blockStatement);
                listener.returnStatementAdded(returnStatement);
                return returnStatement;
            } else {
                List<Statement> statements = blockStatement.getStatements();
                int lastIndex = statements.size() - 1;
                Statement last = addReturnsIfNeeded(statements.get(lastIndex), blockStatement.getVariableScope());
                if (doAdd) statements.set(lastIndex, last);
                if (!statementReturns(last)) {
                    ReturnStatement returnStatement = new ReturnStatement(ConstantExpression.NULL);
                    listener.returnStatementAdded(returnStatement);
                    if (doAdd) statements.add(returnStatement);
                }
                return blockStatement;
            }
        }

        List<Statement> statements = new ArrayList<>(2);
        statements.add(statement);

        ReturnStatement returnStatement = new ReturnStatement(ConstantExpression.NULL);
        listener.returnStatementAdded(returnStatement);
        statements.add(returnStatement);

        BlockStatement blockStatement = new BlockStatement(statements, new VariableScope(scope));
        blockStatement.setSourcePosition(statement);
        return blockStatement;
    }

    private void addLabelsToReturnStatement(Statement statement, ReturnStatement returnStatement) {
        Optional.ofNullable(statement.getStatementLabels())
                .ifPresent(labels -> labels.forEach(returnStatement::addStatementLabel));
    }

    private Statement adjustSwitchCaseCode(final Statement statement, final VariableScope scope, final boolean defaultCase) {
        if (statement instanceof BlockStatement) {
            List<Statement> statements = ((BlockStatement) statement).getStatements();
            if (!statements.isEmpty()) {
                int lastIndex = statements.size() - 1;
                Statement last = statements.get(lastIndex);
                if (last instanceof BreakStatement) {
                    if (doAdd) {
                        statements.remove(lastIndex);
                        return addReturnsIfNeeded(statement, scope);
                    } else {
                        BlockStatement newBlock = new BlockStatement();
                        for (int i = 0; i < lastIndex; i += 1) {
                            newBlock.addStatement(statements.get(i));
                        }
                        return addReturnsIfNeeded(newBlock, scope);
                    }
                } else if (defaultCase) {
                    return addReturnsIfNeeded(statement, scope);
                }
            }
        }
        return statement;
    }

    private static boolean statementReturns(final Statement last) {
        return last instanceof ReturnStatement
            || last instanceof BlockStatement
            || last instanceof IfStatement
            || last instanceof ExpressionStatement
            || last instanceof EmptyStatement
            || last instanceof TryCatchStatement
            || last instanceof ThrowStatement
            || last instanceof SynchronizedStatement
            || last instanceof BytecodeSequence;
    }
}
