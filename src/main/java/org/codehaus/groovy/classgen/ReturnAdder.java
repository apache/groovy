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

/**
 * Utility class to add return statements.
 * Extracted from Verifier as it can be useful for some AST transformations
 */
public class ReturnAdder {

    private static final ReturnStatementListener DEFAULT_LISTENER = returnStatement -> {
    };

    /**
     * If set to 'true', then returns are effectively added. This is useful whenever you just want
     * to check what returns are produced without eventually adding them.
     */
    private final boolean doAdd;

    private final ReturnStatementListener listener;

    public ReturnAdder() {
        doAdd = true;
        listener = DEFAULT_LISTENER;
    }

    public ReturnAdder(ReturnStatementListener listener) {
        this.listener = listener;
        this.doAdd = false;
    }

    /**
     * Adds return statements in method code whenever an implicit return is detected.
     * @param node the method node where to add return statements
     * @deprecated Use {@link #visitMethod(org.codehaus.groovy.ast.MethodNode)} instead
     */
    @Deprecated
    public static void addReturnIfNeeded(MethodNode node) {
        ReturnAdder adder = new ReturnAdder();
        adder.visitMethod(node);
    }

    public void visitMethod(MethodNode node) {
        Statement statement = node.getCode();
        if (!node.isVoidMethod()) {
            if (statement != null) // it happens with @interface methods
            {
                final Statement code = addReturnsIfNeeded(statement, node.getVariableScope());
                if (doAdd) node.setCode(code);
            }
        } else if (!node.isAbstract() && node.getReturnType().redirect()!=ClassHelper.VOID_TYPE) {
            if (!(statement instanceof BytecodeSequence)) {
                BlockStatement newBlock = new BlockStatement();
                Statement code = node.getCode();
                if (code instanceof BlockStatement) {
                    newBlock.setVariableScope(((BlockStatement) code).getVariableScope());
                }
                if (statement instanceof BlockStatement) {
                    newBlock.addStatements(((BlockStatement)statement).getStatements());
                } else {
                    newBlock.addStatement(statement);
                }
                final ReturnStatement returnStatement = ReturnStatement.RETURN_NULL_OR_VOID;
                listener.returnStatementAdded(returnStatement);
                newBlock.addStatement(returnStatement);
                newBlock.setSourcePosition(statement);
                if (doAdd) node.setCode(newBlock);
            }
        }
    }

    private Statement addReturnsIfNeeded(Statement statement, VariableScope scope) {
        if (  statement instanceof ReturnStatement
           || statement instanceof BytecodeSequence
           || statement instanceof ThrowStatement)
        {
            return statement;
        }

        if (statement instanceof EmptyStatement) {
            final ReturnStatement returnStatement = new ReturnStatement(ConstantExpression.NULL);
            listener.returnStatementAdded(returnStatement);
            return returnStatement;
        }

        if (statement instanceof ExpressionStatement) {
            ExpressionStatement expStmt = (ExpressionStatement) statement;
            Expression expr = expStmt.getExpression();
            ReturnStatement ret = new ReturnStatement(expr);
            ret.setSourcePosition(expr);
            ret.setStatementLabel(statement.getStatementLabel());
            listener.returnStatementAdded(ret);
            return ret;
        }

        if (statement instanceof SynchronizedStatement) {
            SynchronizedStatement sync = (SynchronizedStatement) statement;
            final Statement code = addReturnsIfNeeded(sync.getCode(), scope);
            if (doAdd) sync.setCode(code);
            return sync;
        }

        if (statement instanceof IfStatement) {
            IfStatement ifs = (IfStatement) statement;
            final Statement ifBlock = addReturnsIfNeeded(ifs.getIfBlock(), scope);
            final Statement elseBlock = addReturnsIfNeeded(ifs.getElseBlock(), scope);
            if (doAdd) {
                ifs.setIfBlock(ifBlock);
                ifs.setElseBlock(elseBlock);
            }
            return ifs;
        }

        if (statement instanceof SwitchStatement) {
            SwitchStatement swi = (SwitchStatement) statement;
            for (CaseStatement caseStatement : swi.getCaseStatements()) {
                final Statement code = adjustSwitchCaseCode(caseStatement.getCode(), scope, false);
                if (doAdd) caseStatement.setCode(code);
            }
            final Statement defaultStatement = adjustSwitchCaseCode(swi.getDefaultStatement(), scope, true);
            if (doAdd) swi.setDefaultStatement(defaultStatement);
            return swi;
        }

        if (statement instanceof TryCatchStatement) {
            TryCatchStatement trys = (TryCatchStatement) statement;
            final boolean[] missesReturn = new boolean[1];
            new ReturnAdder(returnStatement -> missesReturn[0] = true).addReturnsIfNeeded(trys.getFinallyStatement(), scope);
            boolean hasFinally = !(trys.getFinallyStatement() instanceof EmptyStatement);

            // if there is no missing return in the finally block and the block exists
            // there is nothing to do
            if (hasFinally && !missesReturn[0]) return trys;

            // add returns to try and catch blocks
            final Statement tryStatement = addReturnsIfNeeded(trys.getTryStatement(), scope);
            if (doAdd) trys.setTryStatement(tryStatement);
            final int len = trys.getCatchStatements().size();
            for (int i = 0; i != len; ++i) {
                final CatchStatement catchStatement = trys.getCatchStatement(i);
                final Statement code = addReturnsIfNeeded(catchStatement.getCode(), scope);
                if (doAdd) catchStatement.setCode(code);
            }
            return trys;
        }

        if (statement instanceof BlockStatement) {
            BlockStatement block = (BlockStatement) statement;

            final List list = block.getStatements();
            if (!list.isEmpty()) {
                int idx = list.size() - 1;
                Statement last = addReturnsIfNeeded((Statement) list.get(idx), block.getVariableScope());
                if (doAdd) list.set(idx, last);
                if (!statementReturns(last)) {
                    final ReturnStatement returnStatement = new ReturnStatement(ConstantExpression.NULL);
                    listener.returnStatementAdded(returnStatement);
                    if (doAdd) list.add(returnStatement);
                }
            } else {
                ReturnStatement ret = new ReturnStatement(ConstantExpression.NULL);
                ret.setSourcePosition(block);
                listener.returnStatementAdded(ret);
                return ret;
            }

            BlockStatement newBlock = new BlockStatement(list, block.getVariableScope());
            newBlock.setSourcePosition(block);
            return newBlock;
        }

        if (statement == null) {
            final ReturnStatement returnStatement = new ReturnStatement(ConstantExpression.NULL);
            listener.returnStatementAdded(returnStatement);
            return returnStatement;
        } else {
            final List list = new ArrayList();
            list.add(statement);
            final ReturnStatement returnStatement = new ReturnStatement(ConstantExpression.NULL);
            listener.returnStatementAdded(returnStatement);
            list.add(returnStatement);

            BlockStatement newBlock = new BlockStatement(list, new VariableScope(scope));
            newBlock.setSourcePosition(statement);
            return newBlock;
        }
    }

    private Statement adjustSwitchCaseCode(Statement statement, VariableScope scope, boolean defaultCase) {
        if(statement instanceof BlockStatement) {
            final List list = ((BlockStatement)statement).getStatements();
            if (!list.isEmpty()) {
                int idx = list.size() - 1;
                Statement last = (Statement) list.get(idx);
                if(last instanceof BreakStatement) {
                    if (doAdd) {
                        list.remove(idx);
                        return addReturnsIfNeeded(statement, scope);
                    } else {
                        BlockStatement newStmt = new BlockStatement();
                        for (int i=0;i<idx; i++) {
                            newStmt.addStatement((Statement) list.get(i));
                        }
                        return addReturnsIfNeeded(newStmt, scope);
                    }
                } else if(defaultCase) {
                    return addReturnsIfNeeded(statement, scope);
                }
            }
        }
        return statement;
    }

    private static boolean statementReturns(Statement last) {
        return (
                last instanceof ReturnStatement ||
                last instanceof BlockStatement ||
                last instanceof IfStatement ||
                last instanceof ExpressionStatement ||
                last instanceof EmptyStatement ||
                last instanceof TryCatchStatement ||
                last instanceof BytecodeSequence ||
                last instanceof ThrowStatement ||
                last instanceof SynchronizedStatement
                );
    }

    /**
     * Implement this method in order to be notified whenever a return statement is generated.
     */
    public interface ReturnStatementListener {
        void returnStatementAdded(ReturnStatement returnStatement);
    }
}
