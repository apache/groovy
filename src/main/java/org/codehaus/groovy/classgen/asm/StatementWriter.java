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
package org.codehaus.groovy.classgen.asm;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ClosureListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.EmptyExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.stmt.AssertStatement;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.BreakStatement;
import org.codehaus.groovy.ast.stmt.CaseStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ContinueStatement;
import org.codehaus.groovy.ast.stmt.DoWhileStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.ast.stmt.SynchronizedStatement;
import org.codehaus.groovy.ast.stmt.ThrowStatement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.codehaus.groovy.ast.stmt.WhileStatement;
import org.codehaus.groovy.classgen.asm.CompileStack.BlockRecorder;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.Iterator;
import java.util.List;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.MONITORENTER;
import static org.objectweb.asm.Opcodes.MONITOREXIT;
import static org.objectweb.asm.Opcodes.NOP;
import static org.objectweb.asm.Opcodes.RETURN;

public class StatementWriter {
    // iterator
    private static final MethodCaller iteratorNextMethod = MethodCaller.newInterface(Iterator.class, "next");
    private static final MethodCaller iteratorHasNextMethod = MethodCaller.newInterface(Iterator.class, "hasNext");

    private final WriterController controller;

    public StatementWriter(WriterController controller) {
        this.controller = controller;
    }

    protected void writeStatementLabel(Statement statement) {
        String name = statement.getStatementLabel();
        if (name != null) {
            Label label = controller.getCompileStack().createLocalLabel(name);
            controller.getMethodVisitor().visitLabel(label);
        }
    }

    public void writeBlockStatement(BlockStatement block) {
        writeStatementLabel(block);

        int mark = controller.getOperandStack().getStackLength();
        CompileStack compileStack = controller.getCompileStack();
        compileStack.pushVariableScope(block.getVariableScope());
        List<Statement> statementList = block.getStatements();
        for (Statement statement : statementList) {
            statement.visit(controller.getAcg());
        }
        compileStack.pop();

        // GROOVY-7647
        if (block.getLastLineNumber() > 0
                && !methodOrConstructorsLastReturnStatementExists(block) // GROOVY-9126
        ) {
            MethodVisitor mv = controller.getMethodVisitor();
            Label blockEnd = new Label();
            mv.visitLabel(blockEnd);
            mv.visitLineNumber(block.getLastLineNumber(), blockEnd);
        }

        controller.getOperandStack().popDownTo(mark);
    }

    private boolean methodOrConstructorsLastReturnStatementExists(BlockStatement block) {
        MethodNode methodNode = controller.getMethodNode();
        if (null == methodNode) {
            methodNode = controller.getConstructorNode();
        }

        if (null == methodNode || block != methodNode.getCode()) { // check if the block is method/constructor's code
            return false;
        }

        List<Statement> statementList = block.getStatements();
        final int size = statementList.size();
        return size > 0 && statementList.get(size - 1) instanceof ReturnStatement;
    }

    public void writeForStatement(ForStatement loop) {
        Parameter loopVar = loop.getVariable();
        if (loopVar == ForStatement.FOR_LOOP_DUMMY) {
            writeForLoopWithClosureList(loop);
        } else {
            writeForInLoop(loop);
        }
    }

    protected void writeIteratorHasNext(MethodVisitor mv) {
        iteratorHasNextMethod.call(mv);
    }

    protected void writeIteratorNext(MethodVisitor mv) {
        iteratorNextMethod.call(mv);
    }

    protected void writeForInLoop(ForStatement loop) {
        controller.getAcg().onLineNumber(loop, "visitForLoop");
        writeStatementLabel(loop);

        CompileStack compileStack = controller.getCompileStack();
        MethodVisitor mv = controller.getMethodVisitor();
        OperandStack operandStack = controller.getOperandStack();

        compileStack.pushLoop(loop.getVariableScope(), loop.getStatementLabels());

        // Declare the loop counter.
        BytecodeVariable variable = compileStack.defineVariable(loop.getVariable(), false);

        // Then get the iterator and generate the loop control
        MethodCallExpression iterator = new MethodCallExpression(loop.getCollectionExpression(), "iterator", new ArgumentListExpression());
        iterator.visit(controller.getAcg());
        operandStack.doGroovyCast(ClassHelper.Iterator_TYPE);

        final int iteratorIdx = compileStack.defineTemporaryVariable("iterator", ClassHelper.Iterator_TYPE, true);

        Label continueLabel = compileStack.getContinueLabel();
        Label breakLabel = compileStack.getBreakLabel();

        mv.visitLabel(continueLabel);
        mv.visitVarInsn(ALOAD, iteratorIdx);
        writeIteratorHasNext(mv);
        // note: ifeq tests for ==0, a boolean is 0 if it is false
        mv.visitJumpInsn(IFEQ, breakLabel);

        mv.visitVarInsn(ALOAD, iteratorIdx);
        writeIteratorNext(mv);
        operandStack.push(ClassHelper.OBJECT_TYPE);
        operandStack.storeVar(variable);

        // Generate the loop body
        loop.getLoopBlock().visit(controller.getAcg());

        mv.visitJumpInsn(GOTO, continueLabel);
        mv.visitLabel(breakLabel);

        compileStack.removeVar(iteratorIdx);
        compileStack.pop();
    }

    private void visitExpressionOfLoopStatement(Expression expression) {
        if (expression instanceof ClosureListExpression) {
            ((ClosureListExpression) expression).getExpressions().forEach(this::visitExpressionOrStatement);
        } else {
            visitExpressionOrStatement(expression);
        }
    }

    protected void writeForLoopWithClosureList(ForStatement loop) {
        controller.getAcg().onLineNumber(loop, "visitForLoop");
        writeStatementLabel(loop);

        MethodVisitor mv = controller.getMethodVisitor();
        controller.getCompileStack().pushLoop(loop.getVariableScope(), loop.getStatementLabels());

        ClosureListExpression clExpr = (ClosureListExpression) loop.getCollectionExpression();
        controller.getCompileStack().pushVariableScope(clExpr.getVariableScope());

        List<Expression> expressions = clExpr.getExpressions();
        int size = expressions.size();

        // middle element is condition, lower half is init, higher half is increment
        int condIndex = (size - 1) / 2;

        // visit init
        for (int i = 0; i < condIndex; i++) {
            visitExpressionOfLoopStatement(expressions.get(i));
        }

        Label continueLabel = controller.getCompileStack().getContinueLabel();
        Label breakLabel = controller.getCompileStack().getBreakLabel();

        Label cond = new Label();
        mv.visitLabel(cond);
        // visit condition leave boolean on stack
        {
            Expression condExpr = expressions.get(condIndex);
            int mark = controller.getOperandStack().getStackLength();
            condExpr.visit(controller.getAcg());
            controller.getOperandStack().castToBool(mark, true);
        }
        // jump if we don't want to continue
        // note: ifeq tests for ==0, a boolean is 0 if it is false
        controller.getOperandStack().jump(IFEQ, breakLabel);

        // Generate the loop body
        loop.getLoopBlock().visit(controller.getAcg());

        // visit increment
        mv.visitLabel(continueLabel);
        // fix for being on the wrong line when debugging for loop
        controller.getAcg().onLineNumber(loop, "increment condition");
        for (int i = condIndex + 1; i < size; i += 1) {
            visitExpressionOfLoopStatement(expressions.get(i));
        }

        // jump to test the condition again
        mv.visitJumpInsn(GOTO, cond);

        // loop end
        mv.visitLabel(breakLabel);

        controller.getCompileStack().pop();
        controller.getCompileStack().pop();
    }

    private void visitExpressionOrStatement(Object o) {
        if (o instanceof EmptyExpression) return;
        if (o instanceof Expression) {
            Expression expr = (Expression) o;
            int mark = controller.getOperandStack().getStackLength();
            expr.visit(controller.getAcg());
            controller.getOperandStack().popDownTo(mark);
        } else {
            ((Statement) o).visit(controller.getAcg());
        }
    }

    private void visitConditionOfLoopingStatement(BooleanExpression bool, Label breakLabel, MethodVisitor mv) {
        boolean boolHandled = false;
        if (bool.getExpression() instanceof ConstantExpression) {
            ConstantExpression constant = (ConstantExpression) bool.getExpression();
            if (constant.getValue() == Boolean.TRUE) {
                boolHandled = true;
                // do nothing
            } else if (constant.getValue() == Boolean.FALSE) {
                boolHandled = true;
                mv.visitJumpInsn(GOTO, breakLabel);
            }
        }

        if (!boolHandled) {
            bool.visit(controller.getAcg());
            controller.getOperandStack().jump(IFEQ, breakLabel);
        }
    }

    public void writeWhileLoop(WhileStatement loop) {
        controller.getAcg().onLineNumber(loop, "visitWhileLoop");
        writeStatementLabel(loop);

        MethodVisitor mv = controller.getMethodVisitor();

        controller.getCompileStack().pushLoop(loop.getStatementLabels());
        Label continueLabel = controller.getCompileStack().getContinueLabel();
        Label breakLabel = controller.getCompileStack().getBreakLabel();

        mv.visitLabel(continueLabel);

        this.visitConditionOfLoopingStatement(loop.getBooleanExpression(), breakLabel, mv);
        loop.getLoopBlock().visit(controller.getAcg());

        mv.visitJumpInsn(GOTO, continueLabel);
        mv.visitLabel(breakLabel);

        controller.getCompileStack().pop();
    }

    public void writeDoWhileLoop(DoWhileStatement loop) {
        controller.getAcg().onLineNumber(loop, "visitDoWhileLoop");
        writeStatementLabel(loop);

        MethodVisitor mv = controller.getMethodVisitor();

        controller.getCompileStack().pushLoop(loop.getStatementLabels());
        Label continueLabel = controller.getCompileStack().getContinueLabel();
        Label breakLabel = controller.getCompileStack().getBreakLabel();

        mv.visitLabel(continueLabel);

        loop.getLoopBlock().visit(controller.getAcg());
        this.visitConditionOfLoopingStatement(loop.getBooleanExpression(), breakLabel, mv);

        mv.visitJumpInsn(GOTO, continueLabel);
        mv.visitLabel(breakLabel);

        controller.getCompileStack().pop();
    }

    public void writeIfElse(IfStatement ifElse) {
        controller.getAcg().onLineNumber(ifElse, "visitIfElse");
        writeStatementLabel(ifElse);

        MethodVisitor mv = controller.getMethodVisitor();

        ifElse.getBooleanExpression().visit(controller.getAcg());
        Label l0 = controller.getOperandStack().jump(IFEQ);

        // if-else is here handled as a special version
        // of a boolean expression
        controller.getCompileStack().pushBooleanExpression();
        ifElse.getIfBlock().visit(controller.getAcg());
        controller.getCompileStack().pop();

        if (ifElse.getElseBlock() instanceof EmptyStatement) {
            mv.visitLabel(l0);
        } else {
            Label l1 = new Label();
            mv.visitJumpInsn(GOTO, l1);
            mv.visitLabel(l0);

            controller.getCompileStack().pushBooleanExpression();
            ifElse.getElseBlock().visit(controller.getAcg());
            controller.getCompileStack().pop();

            mv.visitLabel(l1);
        }
    }

    public void writeTryCatchFinally(TryCatchStatement statement) {
        controller.getAcg().onLineNumber(statement, "visitTryCatchFinally");
        writeStatementLabel(statement);

        MethodVisitor mv = controller.getMethodVisitor();
        CompileStack compileStack = controller.getCompileStack();
        OperandStack operandStack = controller.getOperandStack();

        Statement tryStatement = statement.getTryStatement();
        Statement finallyStatement = statement.getFinallyStatement();

        // start try block, label needed for exception table
        Label tryStart = new Label();
        mv.visitLabel(tryStart);
        BlockRecorder tryBlock = makeBlockRecorder(finallyStatement);
        tryBlock.startRange(tryStart);

        tryStatement.visit(controller.getAcg());

        // goto finally part
        Label finallyStart = new Label();
        mv.visitJumpInsn(GOTO, finallyStart);

        Label tryEnd = new Label();
        mv.visitLabel(tryEnd);
        tryBlock.closeRange(tryEnd);
        // pop for "makeBlockRecorder(finallyStatement)"
        controller.getCompileStack().pop();

        BlockRecorder catches = makeBlockRecorder(finallyStatement);
        for (CatchStatement catchStatement : statement.getCatchStatements()) {
            ClassNode exceptionType = catchStatement.getExceptionType();
            String exceptionTypeInternalName = BytecodeHelper.getClassInternalName(exceptionType);

            // start catch block, label needed for exception table
            Label catchStart = new Label();
            mv.visitLabel(catchStart);
            catches.startRange(catchStart);

            // create exception variable and store the exception
            Parameter exceptionVariable = catchStatement.getVariable();
            compileStack.pushState();
            compileStack.defineVariable(exceptionVariable, true);
            // handle catch body
            catchStatement.visit(controller.getAcg());
            // place holder to avoid problems with empty catch blocks
            mv.visitInsn(NOP);
            // pop for the variable
            controller.getCompileStack().pop();

            // end of catch
            Label catchEnd = new Label();
            mv.visitLabel(catchEnd);
            catches.closeRange(catchEnd);

            // goto finally start
            mv.visitJumpInsn(GOTO, finallyStart);
            compileStack.writeExceptionTable(tryBlock, catchStart, exceptionTypeInternalName);
        }

        // used to handle exceptions in catches and regularly visited finals
        Label catchAny = new Label();

        // add "catch any" block to exception table for try part we do this
        // after the exception blocks, because else this one would supersede
        // any of those otherwise
        compileStack.writeExceptionTable(tryBlock, catchAny, null);
        // same for the catch parts
        compileStack.writeExceptionTable(catches, catchAny, null);

        // pop for "makeBlockRecorder(catches)"
        compileStack.pop();

        // start finally
        mv.visitLabel(finallyStart);
        finallyStatement.visit(controller.getAcg());

        // goto after all-catching block
        Label skipCatchAll = new Label();
        mv.visitJumpInsn(GOTO, skipCatchAll);

        // start a block catching any Exception
        mv.visitLabel(catchAny);
        // store exception
        // TODO: maybe define a Throwable and use it here instead of Object
        operandStack.push(ClassHelper.OBJECT_TYPE);
        int anyExceptionIndex = compileStack.defineTemporaryVariable("exception", true);

        // GROOVY-9199
        controller.resetLineNumber();
        int line = finallyStatement.getLineNumber();
        if (line > 0) mv.visitLineNumber(line, catchAny);

        finallyStatement.visit(controller.getAcg());

        // load the exception and rethrow it
        mv.visitVarInsn(ALOAD, anyExceptionIndex);
        mv.visitInsn(ATHROW);

        mv.visitLabel(skipCatchAll);
        compileStack.removeVar(anyExceptionIndex);
    }

    private BlockRecorder makeBlockRecorder(Statement finallyStatement) {
        final BlockRecorder block = new BlockRecorder();
        block.excludedStatement = () -> {
            controller.getCompileStack().pushBlockRecorderVisit(block);
            finallyStatement.visit(controller.getAcg());
            controller.getCompileStack().popBlockRecorderVisit(block);
        };
        controller.getCompileStack().pushBlockRecorder(block);
        return block;
    }

    public void writeSwitch(SwitchStatement statement) {
        controller.getAcg().onLineNumber(statement, "visitSwitch");
        writeStatementLabel(statement);

        statement.getExpression().visit(controller.getAcg());

        // switch does not have a continue label. use its parent's for continue
        Label breakLabel = controller.getCompileStack().pushSwitch();

        final int switchVariableIndex = controller.getCompileStack().defineTemporaryVariable("switch", true);

        List caseStatements = statement.getCaseStatements();
        int caseCount = caseStatements.size();
        Label[] labels = new Label[caseCount + 1];
        for (int i = 0; i < caseCount; i++) {
            labels[i] = new Label();
        }

        int i = 0;
        for (Iterator iter = caseStatements.iterator(); iter.hasNext(); i++) {
            CaseStatement caseStatement = (CaseStatement) iter.next();
            writeCaseStatement(caseStatement, switchVariableIndex, labels[i], labels[i + 1]);
        }

        statement.getDefaultStatement().visit(controller.getAcg());

        controller.getMethodVisitor().visitLabel(breakLabel);

        controller.getCompileStack().removeVar(switchVariableIndex);
        controller.getCompileStack().pop();
    }

    protected void writeCaseStatement(
            CaseStatement statement, int switchVariableIndex,
            Label thisLabel, Label nextLabel) {
        controller.getAcg().onLineNumber(statement, "visitCaseStatement");
        MethodVisitor mv = controller.getMethodVisitor();
        OperandStack operandStack = controller.getOperandStack();

        mv.visitVarInsn(ALOAD, switchVariableIndex);

        statement.getExpression().visit(controller.getAcg());
        operandStack.box();
        controller.getBinaryExpressionHelper().getIsCaseMethod().call(mv);
        operandStack.replace(ClassHelper.boolean_TYPE);

        Label l0 = controller.getOperandStack().jump(IFEQ);

        mv.visitLabel(thisLabel);

        statement.getCode().visit(controller.getAcg());

        // now if we don't finish with a break we need to jump past
        // the next comparison
        if (nextLabel != null) {
            mv.visitJumpInsn(GOTO, nextLabel);
        }

        mv.visitLabel(l0);
    }

    public void writeBreak(BreakStatement statement) {
        controller.getAcg().onLineNumber(statement, "visitBreakStatement");
        writeStatementLabel(statement);

        String name = statement.getLabel();
        Label breakLabel = controller.getCompileStack().getNamedBreakLabel(name);
        controller.getCompileStack().applyFinallyBlocks(breakLabel, true);

        controller.getMethodVisitor().visitJumpInsn(GOTO, breakLabel);
    }

    public void writeContinue(ContinueStatement statement) {
        controller.getAcg().onLineNumber(statement, "visitContinueStatement");
        writeStatementLabel(statement);

        String name = statement.getLabel();
        Label continueLabel = controller.getCompileStack().getContinueLabel();
        if (name != null) continueLabel = controller.getCompileStack().getNamedContinueLabel(name);
        controller.getCompileStack().applyFinallyBlocks(continueLabel, false);
        controller.getMethodVisitor().visitJumpInsn(GOTO, continueLabel);
    }

    public void writeSynchronized(SynchronizedStatement statement) {
        controller.getAcg().onLineNumber(statement, "visitSynchronizedStatement");
        writeStatementLabel(statement);
        final MethodVisitor mv = controller.getMethodVisitor();
        CompileStack compileStack = controller.getCompileStack();

        statement.getExpression().visit(controller.getAcg());
        controller.getOperandStack().box();
        final int index = compileStack.defineTemporaryVariable("synchronized", ClassHelper.OBJECT_TYPE, true);

        final Label synchronizedStart = new Label();
        final Label synchronizedEnd = new Label();
        final Label catchAll = new Label();

        mv.visitVarInsn(ALOAD, index);
        mv.visitInsn(MONITORENTER);
        mv.visitLabel(synchronizedStart);
        // place holder for "empty" synchronized blocks, for example
        // if there is only a break/continue.
        mv.visitInsn(NOP);

        Runnable finallyPart = () -> {
            mv.visitVarInsn(ALOAD, index);
            mv.visitInsn(MONITOREXIT);
        };
        BlockRecorder fb = new BlockRecorder(finallyPart);
        fb.startRange(synchronizedStart);
        compileStack.pushBlockRecorder(fb);
        statement.getCode().visit(controller.getAcg());

        fb.closeRange(catchAll);
        compileStack.writeExceptionTable(fb, catchAll, null);
        compileStack.pop(); //pop fb

        finallyPart.run();
        mv.visitJumpInsn(GOTO, synchronizedEnd);
        mv.visitLabel(catchAll);
        finallyPart.run();
        mv.visitInsn(ATHROW);

        mv.visitLabel(synchronizedEnd);
        compileStack.removeVar(index);
    }

    public void writeAssert(AssertStatement statement) {
        controller.getAcg().onLineNumber(statement, "visitAssertStatement");
        writeStatementLabel(statement);
        controller.getAssertionWriter().writeAssertStatement(statement);
    }

    public void writeThrow(ThrowStatement statement) {
        controller.getAcg().onLineNumber(statement, "visitThrowStatement");
        writeStatementLabel(statement);
        MethodVisitor mv = controller.getMethodVisitor();

        statement.getExpression().visit(controller.getAcg());

        // we should infer the type of the exception from the expression
        mv.visitTypeInsn(CHECKCAST, "java/lang/Throwable");
        mv.visitInsn(ATHROW);

        controller.getOperandStack().remove(1);
    }

    public void writeReturn(ReturnStatement statement) {
        controller.getAcg().onLineNumber(statement, "visitReturnStatement");
        writeStatementLabel(statement);
        MethodVisitor mv = controller.getMethodVisitor();
        OperandStack operandStack = controller.getOperandStack();
        ClassNode returnType = controller.getReturnType();

        if (returnType == ClassHelper.VOID_TYPE) {
            if (!(statement.isReturningNullOrVoid())) {
                //TODO: move to Verifier
                controller.getAcg().throwException("Cannot use return statement with an expression on a method that returns void");
            }
            controller.getCompileStack().applyBlockRecorder();
            mv.visitInsn(RETURN);
            return;
        }

        Expression expression = statement.getExpression();
        expression.visit(controller.getAcg());

        operandStack.doGroovyCast(returnType);

        if (controller.getCompileStack().hasBlockRecorder()) {
            ClassNode type = operandStack.getTopOperand();
            int returnValueIdx = controller.getCompileStack().defineTemporaryVariable("returnValue", returnType, true);
            controller.getCompileStack().applyBlockRecorder();
            operandStack.load(type, returnValueIdx);
            controller.getCompileStack().removeVar(returnValueIdx);
        }

        BytecodeHelper.doReturn(mv, returnType);
        operandStack.remove(1);
    }

    public void writeExpressionStatement(ExpressionStatement statement) {
        controller.getAcg().onLineNumber(statement, "visitExpressionStatement: " + statement.getExpression().getClass().getName());
        writeStatementLabel(statement);

        Expression expression = statement.getExpression();

        int mark = controller.getOperandStack().getStackLength();
        expression.visit(controller.getAcg());
        controller.getOperandStack().popDownTo(mark);
    }
}
