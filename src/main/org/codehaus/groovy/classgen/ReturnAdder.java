/*
 * Copyright 2003-2008 the original author or authors.
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
package org.codehaus.groovy.classgen;

import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.*;

import java.util.List;
import java.util.ArrayList;

/**
 * Utility class to add return statements.
 * Extracted from Verifier as it can be useful for some AST transformations
 */
public class ReturnAdder {
    public static void addReturnIfNeeded(MethodNode node) {
        Statement statement = node.getCode();
        if (!node.isVoidMethod()) {
            if (statement != null) // it happens with @interface methods
              node.setCode(addReturnsIfNeeded(statement, node.getVariableScope()));
        }
        else if (!node.isAbstract()) {
            if (!(statement instanceof BytecodeSequence)) {
                BlockStatement newBlock = new BlockStatement();
                newBlock.setVariableScope(node.getVariableScope());
                if (statement instanceof BlockStatement) {
                    newBlock.addStatements(((BlockStatement)statement).getStatements());
                } else {
                    newBlock.addStatement(statement);
                }
                newBlock.addStatement(ReturnStatement.RETURN_NULL_OR_VOID);
                newBlock.setSourcePosition(statement);
                node.setCode(newBlock);
            }
        }
    }

    private static Statement addReturnsIfNeeded(Statement statement, VariableScope scope) {
        if (  statement instanceof ReturnStatement
           || statement instanceof BytecodeSequence
           || statement instanceof ThrowStatement)
        {
            return statement;
        }

        if (statement instanceof EmptyStatement) {
            return new ReturnStatement(ConstantExpression.NULL);
        }

        if (statement instanceof ExpressionStatement) {
            ExpressionStatement expStmt = (ExpressionStatement) statement;
            Expression expr = expStmt.getExpression();
            ReturnStatement ret = new ReturnStatement(expr);
            ret.setSourcePosition(expr);
            ret.setStatementLabel(statement.getStatementLabel());
            return ret;
        }

        if (statement instanceof SynchronizedStatement) {
            SynchronizedStatement sync = (SynchronizedStatement) statement;
            sync.setCode(addReturnsIfNeeded(sync.getCode(), scope));
            return sync;
        }

        if (statement instanceof IfStatement) {
            IfStatement ifs = (IfStatement) statement;
            ifs.setIfBlock(addReturnsIfNeeded(ifs.getIfBlock(), scope));
            ifs.setElseBlock(addReturnsIfNeeded(ifs.getElseBlock(), scope));
            return ifs;
        }

        if (statement instanceof SwitchStatement) {
            SwitchStatement swi = (SwitchStatement) statement;
            for (CaseStatement caseStatement : swi.getCaseStatements()) {
                caseStatement.setCode(adjustSwitchCaseCode(caseStatement.getCode(), scope));
            }
            swi.setDefaultStatement(adjustSwitchCaseCode(swi.getDefaultStatement(), scope)); 
            return swi;
        }

        if (statement instanceof TryCatchStatement) {
            TryCatchStatement trys = (TryCatchStatement) statement;
            trys.setTryStatement(addReturnsIfNeeded(trys.getTryStatement(), scope));
            final int len = trys.getCatchStatements().size();
            for (int i = 0; i != len; ++i) {
                final CatchStatement catchStatement = trys.getCatchStatement(i);
                catchStatement.setCode(addReturnsIfNeeded(catchStatement.getCode(), scope));
            }
            return trys;
        }

        if (statement instanceof BlockStatement) {
            BlockStatement block = (BlockStatement) statement;

            final List list = block.getStatements();
            if (!list.isEmpty()) {
                int idx = list.size() - 1;
                Statement last = addReturnsIfNeeded((Statement) list.get(idx), block.getVariableScope());
                list.set(idx, last);
                if (!statementReturns(last)) {
                    list.add(new ReturnStatement(ConstantExpression.NULL));
                }
            }
            else {
                ReturnStatement ret = new ReturnStatement(ConstantExpression.NULL);
                ret.setSourcePosition(block);
                return ret;
            }

            return new BlockStatement(list,block.getVariableScope());
        }

        if (statement == null)
          return new ReturnStatement(ConstantExpression.NULL);
        else {
            final List list = new ArrayList();
            list.add(statement);
            list.add(new ReturnStatement(ConstantExpression.NULL));
            return new BlockStatement(list,new VariableScope(scope));
        }
    }

    private static Statement adjustSwitchCaseCode(Statement statement, VariableScope scope) {
        if(statement instanceof BlockStatement) {
            final List list = ((BlockStatement)statement).getStatements();
            if (!list.isEmpty()) {
                int idx = list.size() - 1;
                Statement last = (Statement) list.get(idx);
                if(last instanceof BreakStatement) {
                    list.remove(idx);
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
}
