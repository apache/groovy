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
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ClosureListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.EmptyExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCall;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.NotExpression;
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
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.classgen.asm.CompileStack.BlockRecorder;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.apache.groovy.ast.tools.ExpressionUtils.isNullConstant;
import static org.codehaus.groovy.ast.tools.GeneralUtils.*;
import static org.objectweb.asm.Opcodes.*;

public class StatementWriter {

    private static final MethodCaller iteratorHasNextMethod = MethodCaller.newInterface(Iterator.class, "hasNext");
    private static final MethodCaller iteratorNextMethod = MethodCaller.newInterface(Iterator.class, "next");

    protected final WriterController controller;

    public StatementWriter(final WriterController controller) {
        this.controller = controller;
    }

    protected void writeStatementLabel(final Statement statement) {
        List<String> labels = statement.getStatementLabels();
        if (labels != null) {
            CompileStack  cs = controller.getCompileStack ();
            MethodVisitor mv = controller.getMethodVisitor();
            for (String label : labels) {
                mv.visitLabel(cs.createLocalLabel(label));
            }
        }
    }

    public void writeBlockStatement(final BlockStatement block) {
        writeStatementLabel(block);

        CompileStack compileStack = controller.getCompileStack();
        OperandStack operandStack = controller.getOperandStack();

        int mark = operandStack.getStackLength();
        compileStack.pushVariableScope(block.getVariableScope());
        List<String> labels = block.getStatementLabels(); // GROOVY-6844
        Label end = (labels != null && !labels.isEmpty()) ? compileStack.pushBreakable(labels) : null;

        for (Statement statement : block.getStatements()) {
            statement.visit(controller.getAcg());
        }

        if (end != null) {
            controller.getMethodVisitor().visitLabel(end);
            compileStack.pop();
        }
        compileStack.pop();
        operandStack.popDownTo(mark);
    }

    public void writeForStatement(final ForStatement statement) {
        if (statement.getCollectionExpression() instanceof ClosureListExpression) {
            writeForLoopWithClosureList(statement);
        } else {
            writeForInLoop(statement);
        }
    }

    protected void writeForInLoop(final ForStatement statement) {
        controller.getAcg().onLineNumber(statement, "visitForLoop");
        writeStatementLabel(statement);

        CompileStack compileStack = controller.getCompileStack();
        compileStack.pushLoop(statement.getVariableScope(), statement.getStatementLabels());

        // then get the iterator and generate the loop control
        Expression iterator = callX(statement.getCollectionExpression(), "iterator");
        ((MethodCallExpression) iterator).setImplicitThis(false);
        iterator = castX(ClassHelper.Iterator_TYPE, iterator);
        iterator.visit(controller.getAcg());

        writeForInLoopControlAndBlock(statement);
        compileStack.pop();
    }

    protected void writeForInLoopControlAndBlock(ForStatement statement) {
        CompileStack compileStack = controller.getCompileStack();
        MethodVisitor mv = controller.getMethodVisitor();
        OperandStack operandStack = controller.getOperandStack();

        // declare the loop index and value variables
        BytecodeVariable indexVariable = Optional.ofNullable(statement.getIndexVariable()).map(iv -> {
            mv.visitInsn(ICONST_M1); // initialize to -1 so increment can pair with next()
            return compileStack.defineVariable(iv, true);
        }).orElse(null);
        BytecodeVariable valueVariable = compileStack.defineVariable(statement.getValueVariable(), false);

        // get the iterator and generate the loop control
        int iterator = compileStack.defineTemporaryVariable("iterator", ClassHelper.Iterator_TYPE, true);
        Label breakLabel = compileStack.getBreakLabel(), continueLabel = compileStack.getContinueLabel();

        mv.visitVarInsn(ALOAD, iterator);
        mv.visitJumpInsn(IFNULL, breakLabel);

        mv.visitLabel(continueLabel);

        mv.visitVarInsn(ALOAD, iterator);
        writeIteratorHasNext(mv);
        mv.visitJumpInsn(IFEQ, breakLabel); // jump if zero (aka false)

        mv.visitVarInsn(ALOAD, iterator);
        writeIteratorNext(mv);
        operandStack.push(ClassHelper.OBJECT_TYPE);
        operandStack.storeVar(valueVariable);
        if (indexVariable != null) {
            if (!indexVariable.isHolder()) {
                mv.visitIincInsn(indexVariable.getIndex(), 1);
            } else { // GROOVY-11751: shared variable reference
                mv.visitVarInsn(ALOAD, indexVariable.getIndex());
                mv.visitMethodInsn(INVOKEVIRTUAL, "groovy/lang/Reference", "get", "()Ljava/lang/Object;", false);
                mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
                mv.visitInsn(ICONST_1);
                mv.visitInsn(IADD);
                operandStack.push(ClassHelper.int_TYPE);
                operandStack.storeVar(indexVariable);
            }
        }

        // generate the loop body
        statement.getLoopBlock().visit(controller.getAcg());
        mv.visitJumpInsn(GOTO, continueLabel);

        mv.visitLabel(breakLabel);
        compileStack.removeVar(iterator);
    }

    protected void writeIteratorHasNext(final MethodVisitor mv) {
        iteratorHasNextMethod.call(mv);
    }

    protected void writeIteratorNext(final MethodVisitor mv) {
        iteratorNextMethod.call(mv);
    }

    protected void writeForLoopWithClosureList(final ForStatement statement) {
        controller.getAcg().onLineNumber(statement, "visitForLoop");
        writeStatementLabel(statement);

        MethodVisitor mv = controller.getMethodVisitor();
        controller.getCompileStack().pushLoop(statement.getVariableScope(), statement.getStatementLabels());

        ClosureListExpression clExpr = (ClosureListExpression) statement.getCollectionExpression();
        controller.getCompileStack().pushVariableScope(clExpr.getVariableScope());

        List<Expression> expressions = clExpr.getExpressions();
        int size = expressions.size();

        // middle element is condition, lower half is init, higher half is increment
        int condIndex = (size - 1) / 2;

        // visit init
        for (int i = 0; i < condIndex; i += 1) {
            visitExpressionOfLoopStatement(expressions.get(i));
        }

        Label continueLabel = controller.getCompileStack().getContinueLabel();
        Label breakLabel = controller.getCompileStack().getBreakLabel();

        Label cond = new Label();
        mv.visitLabel(cond);
        // visit condition leave boolean on stack
        {
            int mark = controller.getOperandStack().getStackLength();
            Expression condExpr = expressions.get(condIndex);
            condExpr.visit(controller.getAcg());
            controller.getOperandStack().castToBool(mark, true);
        }
        // jump if we don't want to continue
        // note: ifeq tests for ==0, a boolean is 0 if it is false
        controller.getOperandStack().jump(IFEQ, breakLabel);

        // Generate the loop body
        statement.getLoopBlock().visit(controller.getAcg());

        // visit increment
        mv.visitLabel(continueLabel);
        // fix for being on the wrong line when debugging for loop
        controller.getAcg().onLineNumber(statement, "increment condition");
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

    private void visitExpressionOfLoopStatement(final Expression expression) {
        Consumer<Expression> visit = expr -> {
            if (expr instanceof EmptyExpression) return;
            int mark = controller.getOperandStack().getStackLength();
            expr.visit(controller.getAcg());
            controller.getOperandStack().popDownTo(mark);
        };

        if (expression instanceof ClosureListExpression) {
            ((ClosureListExpression) expression).getExpressions().forEach(visit);
        } else {
            visit.accept(expression);
        }
    }

    private void visitConditionOfLoopingStatement(final BooleanExpression expression, final Label breakLabel, final MethodVisitor mv) {
        Expression expr = expression;
        boolean reverse = false;
        do { // undo arbitrary nesting of (Boolean|Not)Expressions
            if (expr instanceof NotExpression) reverse = !reverse;
            expr = ((BooleanExpression) expr).getExpression();
        } while (expr instanceof BooleanExpression);

        // optimize constant boolean condition
        if (expr instanceof ConstantExpression && ((ConstantExpression) expr).getValue() instanceof Boolean) {
            if (((ConstantExpression) expr).isFalseExpression() && !reverse) {
                mv.visitJumpInsn(GOTO, breakLabel); // unconditional exit
                return;
            } else {
                // unconditional loop
                return;
            }
        }

        expression.visit(controller.getAcg());
        controller.getOperandStack().jump(IFEQ, breakLabel);
    }

    public void writeWhileLoop(final WhileStatement statement) {
        controller.getAcg().onLineNumber(statement, "visitWhileLoop");
        writeStatementLabel(statement);

        CompileStack compileStack = controller.getCompileStack();
        compileStack.pushLoop(statement.getStatementLabels());
        Label continueLabel = compileStack.getContinueLabel();
        Label breakLabel = compileStack.getBreakLabel();

        MethodVisitor mv = controller.getMethodVisitor();
        mv.visitLabel(continueLabel);

        visitConditionOfLoopingStatement(statement.getBooleanExpression(), breakLabel, mv);
        statement.getLoopBlock().visit(controller.getAcg());

        mv.visitJumpInsn(GOTO, continueLabel);
        mv.visitLabel(breakLabel);

        compileStack.pop();
    }

    public void writeDoWhileLoop(final DoWhileStatement statement) {
        writeStatementLabel(statement);

        CompileStack compileStack = controller.getCompileStack();
        compileStack.pushLoop(statement.getStatementLabels());
        Label continueLabel = compileStack.getContinueLabel();
        Label breakLabel = compileStack.getBreakLabel();
        Label blockLabel = new Label();

        MethodVisitor mv = controller.getMethodVisitor();
        mv.visitLabel(blockLabel);

        statement.getLoopBlock().visit(controller.getAcg());
        mv.visitLabel(continueLabel); // GROOVY-11739: continue jumps to condition
        visitConditionOfLoopingStatement(statement.getBooleanExpression(), breakLabel, mv);

        mv.visitJumpInsn(GOTO, blockLabel);
        mv.visitLabel(breakLabel);

        compileStack.pop();
    }

    public void writeIfElse(final IfStatement statement) {
        controller.getAcg().onLineNumber(statement, "visitIfElse");
        writeStatementLabel(statement);

        Label exitPath = controller.getCompileStack().pushBreakable(statement.getStatementLabels()); // GROOVY-7463
        statement.getBooleanExpression().visit(controller.getAcg());
        Label elsePath = controller.getOperandStack().jump(IFEQ);
        statement.getIfBlock().visit(controller.getAcg());
        controller.getCompileStack().pop();

        MethodVisitor mv = controller.getMethodVisitor();
        if (statement.getElseBlock().isEmpty()) {
            mv.visitLabel(elsePath);
        } else {
            mv.visitJumpInsn(GOTO, exitPath);
            mv.visitLabel(elsePath);
            statement.getElseBlock().visit(controller.getAcg());
        }
        mv.visitLabel(exitPath);
    }

    public void writeTryCatchFinally(final TryCatchStatement statement) {
        writeStatementLabel(statement);

        MethodVisitor mv = controller.getMethodVisitor();
        CompileStack compileStack = controller.getCompileStack();
        OperandStack operandStack = controller.getOperandStack();

        Statement tryStatement = statement.getTryStatement();
        Statement finallyStatement = statement.getFinallyStatement();
        BlockRecorder tryBlock = makeBlockRecorder(finallyStatement);

        startRange(tryBlock, mv);
        tryStatement.visit(controller.getAcg());

        // skip past catch block(s)
        Label finallyStart = new Label();
        boolean fallthroughFinally = false;
        if (maybeFallsThrough(tryStatement)) {
            mv.visitJumpInsn(GOTO, finallyStart);
            fallthroughFinally = true;
        }
        closeRange(tryBlock, mv);
        // pop for BlockRecorder
        compileStack.pop();

        BlockRecorder catches = makeBlockRecorder(finallyStatement);
        for (CatchStatement catchStatement : statement.getCatchStatements()) {
            Label catchBlock = startRange(catches, mv);

            // create variable for the exception
            compileStack.pushState();
            ClassNode type = catchStatement.getExceptionType();
            compileStack.defineVariable(catchStatement.getVariable(), type, true);
            // handle catch body
            catchStatement.visit(controller.getAcg());
            // placeholder to avoid problems with empty catch block
            mv.visitInsn(NOP);
            // pop for the variable
            compileStack.pop();

            // end of catch
            closeRange(catches, mv);
            if (maybeFallsThrough(catchStatement.getCode())) {
                mv.visitJumpInsn(GOTO, finallyStart);
                fallthroughFinally = true;
            }
            String typeName = BytecodeHelper.getClassInternalName(type);
            compileStack.writeExceptionTable(tryBlock, catchBlock, typeName);
        }

        // used to handle exceptions in catches and regularly visited finals
        Label catchAll = new Label(), afterCatchAll = new Label();

        // add "catch all" block to exception table for try part; we do this
        // after the exception blocks so they are not superseded by this one
        compileStack.writeExceptionTable(tryBlock, catchAll, null);
        // same for the catch parts
        compileStack.writeExceptionTable(catches , catchAll, null);

        // pop for BlockRecorder
        compileStack.pop();

        if (fallthroughFinally) {
            mv.visitLabel(finallyStart);
            finallyStatement.visit(controller.getAcg());

            // skip over the catch-finally-rethrow
            mv.visitJumpInsn(GOTO, afterCatchAll);
        }

        mv.visitLabel(catchAll);
        operandStack.push(ClassHelper.THROWABLE_TYPE);
        int anyThrowable = compileStack.defineTemporaryVariable("throwable", true);

        finallyStatement.visit(controller.getAcg());

        // load the throwable and rethrow it
        mv.visitVarInsn(ALOAD, anyThrowable);
        mv.visitInsn(ATHROW);

        if (fallthroughFinally)
            mv.visitLabel(afterCatchAll);
        compileStack.removeVar(anyThrowable);
    }

    private BlockRecorder makeBlockRecorder(final Statement finallyStatement) {
        BlockRecorder recorder = new BlockRecorder();
        final CompileStack compileStack = controller.getCompileStack();

        recorder.excludedStatement = () -> {
            if (finallyStatement == null || finallyStatement instanceof EmptyStatement) return;

            final VariableScope originalScope = compileStack.getScope();
            compileStack.pop();
            compileStack.pushBlockRecorderVisit(recorder);
            finallyStatement.visit(controller.getAcg());
            compileStack.popBlockRecorderVisit(recorder);
            compileStack.pushVariableScope(originalScope);
        };

        compileStack.pushBlockRecorder(recorder);
        return recorder;
    }

    private static Label startRange(final BlockRecorder br, final MethodVisitor mv) {
        Label label = new Label();
        mv.visitLabel(label);
        br.startRange(label);
        return label;
    }

    private static void  closeRange(final BlockRecorder br, final MethodVisitor mv) {
        Label label = new Label();
        mv.visitLabel(label);
        br.closeRange(label);
    }

    public void writeSwitch(final SwitchStatement statement) {
        controller.getAcg().onLineNumber(statement, "visitSwitch");
        writeStatementLabel(statement);

        statement.getExpression().visit(controller.getAcg());

        // switch does not have a continue label; use enclosing continue label
        CompileStack compileStack = controller.getCompileStack();
        Label breakLabel = compileStack.pushSwitch();

        int switchVariableIndex = compileStack.defineTemporaryVariable("switch", true);

        List<CaseStatement> caseStatements = statement.getCaseStatements();
        int caseCount = caseStatements.size();
        Label[] labels = new Label[caseCount + 1];
        for (int i = 0; i < caseCount; i += 1) {
            labels[i] = new Label();
        }

        int i = 0;
        for (Iterator<CaseStatement> iter = caseStatements.iterator(); iter.hasNext(); i += 1) {
            writeCaseStatement(iter.next(), switchVariableIndex, labels[i], labels[i + 1]);
        }

        statement.getDefaultStatement().visit(controller.getAcg());

        controller.getMethodVisitor().visitLabel(breakLabel);
        compileStack.removeVar(switchVariableIndex);
        compileStack.pop();
    }

    private void writeCaseStatement(final CaseStatement statement, final int switchVariableIndex, final Label thisLabel, final Label nextLabel) {
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

        // now if we don't finish with a break we need to jump past the next comparison
        if (nextLabel != null) {
            mv.visitJumpInsn(GOTO, nextLabel);
        }

        mv.visitLabel(l0);
    }

    public void writeBreak(final BreakStatement statement) {
        controller.getAcg().onLineNumber(statement, "visitBreakStatement");
        writeStatementLabel(statement);

        Label label = controller.getCompileStack().getNamedBreakLabel(statement.getLabel());
        controller.getCompileStack().applyFinallyBlocks(label, true);
        controller.getMethodVisitor().visitJumpInsn(GOTO, label);
    }

    public void writeContinue(final ContinueStatement statement) {
        controller.getAcg().onLineNumber(statement, "visitContinueStatement");
        writeStatementLabel(statement);

        Label label = controller.getCompileStack().getNamedContinueLabel(statement.getLabel());
        controller.getCompileStack().applyFinallyBlocks(label, false);
        controller.getMethodVisitor().visitJumpInsn(GOTO, label);
    }

    public void writeSynchronized(final SynchronizedStatement statement) {
        controller.getAcg().onLineNumber(statement, "visitSynchronizedStatement");
        writeStatementLabel(statement);
        MethodVisitor mv = controller.getMethodVisitor();
        CompileStack compileStack = controller.getCompileStack();

        statement.getExpression().visit(controller.getAcg());
        controller.getOperandStack().box();
        int index = compileStack.defineTemporaryVariable("synchronized", ClassHelper.OBJECT_TYPE, true);

        Label synchronizedStart = new Label();
        Label synchronizedEnd = new Label();
        Label catchAll = new Label();

        mv.visitVarInsn(ALOAD, index);
        mv.visitInsn(MONITORENTER);
        mv.visitLabel(synchronizedStart);
        // placeholder for "empty" synchronized blocks, for example
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

    public void writeAssert(final AssertStatement statement) {
        controller.getAcg().onLineNumber(statement, "visitAssertStatement");
        writeStatementLabel(statement);
        controller.getAssertionWriter().writeAssertStatement(statement);
    }

    public void writeThrow(final ThrowStatement statement) {
        controller.getAcg().onLineNumber(statement, "visitThrowStatement");
        writeStatementLabel(statement);
        MethodVisitor mv = controller.getMethodVisitor();

        statement.getExpression().visit(controller.getAcg());

        // we should infer the type of the exception from the expression
        mv.visitTypeInsn(CHECKCAST, "java/lang/Throwable");
        mv.visitInsn(ATHROW);

        controller.getOperandStack().remove(1);
    }

    public void writeReturn(final ReturnStatement statement) {
        controller.getAcg().onLineNumber(statement, "visitReturnStatement");
        writeStatementLabel(statement);

        var cs = controller.getCompileStack();
        var os = controller.getOperandStack();
        var mv = controller.getMethodVisitor();

        var returnType = controller.getReturnType();
        if (ClassHelper.isPrimitiveVoid(returnType)) {
            if (!statement.isReturningNullOrVoid()) { // TODO: move to Verifier
                controller.getAcg().throwException("Cannot use return statement with an expression on a method that returns void");
            }
            cs.applyBlockRecorder();
            mv.visitInsn(RETURN);
        } else { // return value
            Expression expression = statement.getExpression();
            expression.visit(controller.getAcg());

            if (!isNullConstant(expression) || ClassHelper.isPrimitiveType(returnType)) {
                os.doGroovyCast(returnType);
            } else { // GROOVY-10617
                os.replace(returnType);
            }

            if (cs.hasBlockRecorder()) {
                int rv = cs.defineTemporaryVariable("returnValue", returnType, true);
                cs.applyBlockRecorder(); // handle finally block
                BytecodeHelper.load(mv, returnType, rv);
                BytecodeHelper.doReturn(mv, returnType);
                cs.removeVar(rv);
            } else {
                BytecodeHelper.doReturn(mv, returnType);
                os.remove(1);
            }
        }
    }

    public void writeExpressionStatement(final ExpressionStatement statement) {
        Expression expression = statement.getExpression();

        controller.getAcg().onLineNumber(statement, "visitExpressionStatement: " + expression.getClass().getName());
        writeStatementLabel(statement);

        if (expression instanceof MethodCall || expression instanceof BinaryExpression)
            expression.putNodeMetaData(AsmClassGenerator.ELIDE_EXPRESSION_VALUE, Boolean.TRUE);

        var operandStack = controller.getOperandStack();
        int mark = operandStack.getStackLength();
        expression.visit(controller.getAcg());
        operandStack.popDownTo(mark);
    }
}
