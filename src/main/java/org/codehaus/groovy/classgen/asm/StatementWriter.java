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
import org.codehaus.groovy.ast.expr.ClosureListExpression;
import org.codehaus.groovy.ast.expr.EmptyExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCall;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.stmt.AssertStatement;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.BreakStatement;
import org.codehaus.groovy.ast.stmt.CaseStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ContinueStatement;
import org.codehaus.groovy.ast.stmt.DoWhileStatement;
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
import static org.codehaus.groovy.ast.tools.GeneralUtils.ConditionValue;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.castX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constantBooleanValue;
import static org.codehaus.groovy.ast.tools.GeneralUtils.isEmptyStatement;
import static org.codehaus.groovy.ast.tools.GeneralUtils.mayReachLoopCondition;
import static org.codehaus.groovy.ast.tools.GeneralUtils.maybeFallsThrough;
import static org.codehaus.groovy.ast.tools.GeneralUtils.maybeFallsThroughToNextSwitchCase;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IADD;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.ICONST_M1;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.MONITORENTER;
import static org.objectweb.asm.Opcodes.MONITOREXIT;
import static org.objectweb.asm.Opcodes.NOP;
import static org.objectweb.asm.Opcodes.RETURN;

/**
 * Generates bytecode for Groovy statements by visiting AST statement nodes
 * and emitting corresponding JVM instructions via the {@link WriterController}.
 * Handles control flow (loops, branches, try/catch), synchronization, assertions,
 * and expression statements.
 */
public class StatementWriter {

    private static final MethodCaller iteratorHasNextMethod = MethodCaller.newInterface(Iterator.class, "hasNext");
    private static final MethodCaller iteratorNextMethod = MethodCaller.newInterface(Iterator.class, "next");

    /** The controller coordinating all bytecode writers for the current class. */
    protected final WriterController controller;

    /**
     * Creates a statement writer backed by the given controller.
     *
     * @param controller the writer controller for the current compilation
     */
    public StatementWriter(final WriterController controller) {
        this.controller = controller;
    }

    /**
     * Emits bytecode labels for any statement labels attached to {@code statement}.
     * Called before emitting the body of every statement so that named labels
     * ({@code break foo} / {@code continue foo}) resolve correctly.
     *
     * @param statement the statement whose labels should be emitted
     */
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

    /**
     * Generates bytecode for a block statement by visiting each contained statement.
     * Pushes the block's variable scope, emits the statements, and pops afterward.
     * Named labels on the block create a breakable region so that {@code break label}
     * within the block jumps to the end of it.
     *
     * @param block the block statement to compile
     */
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

    /**
     * Generates bytecode for a for statement.
     * Delegates to {@link #writeForLoopWithClosureList} for C-style loops (using a
     * {@link ClosureListExpression}), or to {@link #writeForInLoop} for for-in loops.
     *
     * @param statement the for statement to compile
     */
    public void writeForStatement(final ForStatement statement) {
        if (statement.getCollectionExpression() instanceof ClosureListExpression) {
            writeForLoopWithClosureList(statement);
        } else {
            writeForInLoop(statement);
        }
    }

    /**
     * Generates bytecode for a for-in loop by calling {@code iterator()} on the
     * collection expression and delegating loop control to
     * {@link #writeForInLoopControlAndBlock}.
     *
     * @param statement the for-in statement to compile
     */
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

    /**
     * Emits the loop-control structure and body for a for-in loop.
     * Assumes the iterator object is already on the operand stack.
     * Declares loop variables, emits the {@code hasNext}/{@code next} check-and-advance,
     * generates the loop body, and handles index-variable increment when present.
     *
     * @param statement the for-in statement whose control and body should be emitted
     */
    protected void writeForInLoopControlAndBlock(ForStatement statement) {
        CompileStack compileStack = controller.getCompileStack();
        MethodVisitor mv = controller.getMethodVisitor();
        OperandStack operandStack = controller.getOperandStack();

        // declare the loop index and value variables
        BytecodeVariable indexVariable = defineLoopIndexVariable(statement);
        BytecodeVariable valueVariable = compileStack.defineVariable(statement.getValueVariable(), false);

        // get the iterator and generate the loop control
        int iterator = compileStack.defineTemporaryVariable("iterator", ClassHelper.Iterator_TYPE, true);
        Label breakLabel = compileStack.getBreakLabel(), continueLabel = compileStack.getContinueLabel();
        boolean bodyMayReachContinue = mayReachLoopCondition(statement);

        mv.visitVarInsn(ALOAD, iterator);
        mv.visitJumpInsn(IFNULL, breakLabel);

        mv.visitLabel(continueLabel);

        mv.visitVarInsn(ALOAD, iterator);
        writeIteratorHasNext(mv);
        mv.visitJumpInsn(IFEQ, breakLabel); // jump if zero (aka false)

        mv.visitVarInsn(ALOAD, iterator);
        writeIteratorNext(mv);
        operandStack.push(ClassHelper.OBJECT_TYPE);
        storeForLoopVariable(valueVariable);
        if (indexVariable != null) {
            incrementForLoopIndexVariable(indexVariable);
        }

        // generate the loop body
        statement.getLoopBlock().visit(controller.getAcg());
        writeLoopBackEdge(continueLabel, bodyMayReachContinue);

        mv.visitLabel(breakLabel);
        compileStack.removeVar(iterator);
    }

    /**
     * Stores the top-of-stack value into a for-in loop variable slot.
     * <p>
     * Applies the for-in per-iteration capture policy
     * ({@link WriterController#isForLoopCaptureEnabled()}, GROOVY-11792): when
     * enabled, a shared (holder) loop variable receives a fresh
     * {@link groovy.lang.Reference} so deferred closures, lambdas, and
     * anonymous inner classes observe this iteration's value. When disabled,
     * the historical in-place {@code Reference#set} path is used. Non-holder
     * variables always take the plain store path.
     * <p>
     * Bytecode emission is delegated to
     * {@link CompileStack#storeVar(BytecodeVariable, boolean)}.
     *
     * @param variable the for-in value (or similarly stored) loop variable
     */
    protected final void storeForLoopVariable(final BytecodeVariable variable) {
        controller.getCompileStack().storeVar(variable, controller.isForLoopCaptureEnabled());
    }

    /**
     * Increments the for-in index variable by one.
     * <p>
     * Plain (non-shared) indexes use {@code IINC}. Shared indexes are loaded
     * from their {@link groovy.lang.Reference}, unboxed, incremented, then
     * stored via {@link #storeForLoopVariable(BytecodeVariable)} so the same
     * per-iteration capture policy applies as for value variables
     * (GROOVY-11751, GROOVY-11792).
     *
     * @param indexVariable the for-in index variable; must not be {@code null}
     */
    protected final void incrementForLoopIndexVariable(final BytecodeVariable indexVariable) {
        MethodVisitor mv = controller.getMethodVisitor();
        if (!indexVariable.isHolder()) {
            mv.visitIincInsn(indexVariable.getIndex(), 1);
            return;
        }
        // GROOVY-11751: shared Reference — load, unbox, add 1, then store with
        // the for-in recapture policy (storeForLoopVariable / GROOVY-11792).
        mv.visitVarInsn(ALOAD, indexVariable.getIndex());
        mv.visitMethodInsn(INVOKEVIRTUAL, "groovy/lang/Reference", "get", "()Ljava/lang/Object;", false);
        mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
        mv.visitInsn(ICONST_1);
        mv.visitInsn(IADD);
        controller.getOperandStack().push(ClassHelper.int_TYPE);
        storeForLoopVariable(indexVariable);
    }

    protected final BytecodeVariable defineLoopIndexVariable(final ForStatement statement) {
        CompileStack compileStack = controller.getCompileStack();
        return Optional.ofNullable(statement.getIndexVariable()).map(iv -> {
            controller.getMethodVisitor().visitInsn(ICONST_M1); // initialize to -1 so increment can pair with next()
            return compileStack.defineVariable(iv, true);
        }).orElse(null);
    }

    protected final void writeLoopBackEdge(final Label continueLabel, final boolean bodyMayReachContinue) {
        if (bodyMayReachContinue) {
            controller.getMethodVisitor().visitJumpInsn(GOTO, continueLabel);
        }
    }

    /**
     * Emits the {@link java.util.Iterator#hasNext()} call via the given visitor.
     * Overrideable so subclasses can substitute a specialized or inlined variant.
     *
     * @param mv the method visitor to write to
     */
    protected void writeIteratorHasNext(final MethodVisitor mv) {
        iteratorHasNextMethod.call(mv);
    }

    /**
     * Emits the {@link java.util.Iterator#next()} call via the given visitor.
     * Overrideable so subclasses can substitute a specialized or inlined variant.
     *
     * @param mv the method visitor to write to
     */
    protected void writeIteratorNext(final MethodVisitor mv) {
        iteratorNextMethod.call(mv);
    }

    /**
     * Generates bytecode for a C-style {@code for(init; cond; incr)} loop.
     * The collection expression is a {@link ClosureListExpression} whose middle
     * element is the boolean condition, lower elements are initializers, and
     * upper elements are incrementors.
     *
     * @param statement the for statement with a {@link ClosureListExpression} collection
     */
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
        ConditionValue conditionValue = visitConditionOfLoopStatement(expressions.get(condIndex), breakLabel, mv);
        boolean bodyMayReachContinue = mayReachLoopCondition(statement);
        if (conditionValue != ConditionValue.ALWAYS_FALSE) {
            // Generate the loop body
            statement.getLoopBlock().visit(controller.getAcg());

            if (bodyMayReachContinue) {
                // visit increment
                mv.visitLabel(continueLabel);
                // fix for being on the wrong line when debugging for loop
                controller.getAcg().onLineNumber(statement, "increment condition");
                for (int i = condIndex + 1; i < size; i += 1) {
                    visitExpressionOfLoopStatement(expressions.get(i));
                }

                // jump to test the condition again
                writeLoopBackEdge(cond, true);
            }
        }

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

    private ConditionValue visitConditionOfLoopStatement(final Expression expression, final Label breakLabel, final MethodVisitor mv) {
        ConditionValue conditionValue = constantBooleanValue(expression);
        if (conditionValue == ConditionValue.ALWAYS_FALSE) {
            mv.visitJumpInsn(GOTO, breakLabel); // unconditional exit
            return conditionValue;
        }
        if (conditionValue == ConditionValue.ALWAYS_TRUE) {
            return conditionValue; // unconditional loop
        }

        int mark = controller.getOperandStack().getStackLength();
        expression.visit(controller.getAcg());
        controller.getOperandStack().castToBool(mark, true);
        controller.getOperandStack().jump(IFEQ, breakLabel);
        return ConditionValue.UNKNOWN;
    }

    /**
     * Generates bytecode for a while loop.
     *
     * @param statement the while statement to compile
     */
    public void writeWhileLoop(final WhileStatement statement) {
        controller.getAcg().onLineNumber(statement, "visitWhileLoop");
        writeStatementLabel(statement);

        CompileStack compileStack = controller.getCompileStack();
        compileStack.pushLoop(statement.getStatementLabels());
        Label continueLabel = compileStack.getContinueLabel();
        Label breakLabel = compileStack.getBreakLabel();
        boolean bodyMayReachContinue = mayReachLoopCondition(statement);

        MethodVisitor mv = controller.getMethodVisitor();
        mv.visitLabel(continueLabel);

        if (visitConditionOfLoopStatement(statement.getBooleanExpression(), breakLabel, mv) != ConditionValue.ALWAYS_FALSE) {
            statement.getLoopBlock().visit(controller.getAcg());
            writeLoopBackEdge(continueLabel, bodyMayReachContinue);
        }
        mv.visitLabel(breakLabel);

        compileStack.pop();
    }

    /**
     * Generates bytecode for a do-while loop.
     *
     * @param statement the do-while statement to compile
     */
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
        if (mayReachLoopCondition(statement)) {
            mv.visitLabel(continueLabel); // GROOVY-11739: continue jumps to condition
            if (visitConditionOfLoopStatement(statement.getBooleanExpression(), breakLabel, mv) != ConditionValue.ALWAYS_FALSE) {
                mv.visitJumpInsn(GOTO, blockLabel);
            }
        }
        mv.visitLabel(breakLabel);

        compileStack.pop();
    }

    /**
     * Generates bytecode for an if/else statement.
     *
     * @param statement the if statement to compile
     */
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
            if (maybeFallsThrough(statement.getIfBlock())) {
                mv.visitJumpInsn(GOTO, exitPath);
            }
            mv.visitLabel(elsePath);
            statement.getElseBlock().visit(controller.getAcg());
        }
        mv.visitLabel(exitPath);
    }

    /**
     * Generates bytecode for a try/catch/finally statement.
     * <p>
     * A {@link BlockRecorder} is always registered for the try (and catch)
     * regions so that:
     * <ul>
     *   <li>a non-empty finally is inlined on every abrupt exit
     *       ({@code return}/{@code break}/{@code continue});</li>
     *   <li>exception-table ranges are closed before any enclosing finally is
     *       inlined (required when an inner try/catch without its own finally
     *       sits inside an outer try/finally — see GROOVY-8229);</li>
     *   <li>GROOVY-9805 stack-map casts remain active for assignments in the
     *       region ({@link CompileStack#hasBlockRecorder()}).</li>
     * </ul>
     * When the finally clause is empty (plain {@code try}/{@code catch}), the
     * catch-all identity rethrow and the empty shared-finally block are omitted
     * so the shape matches javac more closely and stays more JIT-friendly.
     *
     * @param statement the try/catch/finally statement to compile
     */
    public void writeTryCatchFinally(final TryCatchStatement statement) {
        writeStatementLabel(statement);

        MethodVisitor mv = controller.getMethodVisitor();
        CompileStack compileStack = controller.getCompileStack();
        OperandStack operandStack = controller.getOperandStack();

        Statement tryStatement = statement.getTryStatement();
        Statement finallyStatement = statement.getFinallyStatement();
        boolean hasFinally = !isEmptyStatement(finallyStatement);
        List<CatchStatement> catchStatements = statement.getCatchStatements();

        BlockRecorder tryBlock = makeBlockRecorder(finallyStatement);

        startRange(tryBlock, mv);
        tryStatement.visit(controller.getAcg());

        // Destination for normal completion: shared finally when present, else
        // the join point after all catch handlers.
        Label afterHandlers = new Label();
        boolean fallthrough = false;
        if (maybeFallsThrough(tryStatement)) {
            // Keeps an otherwise-empty try range non-empty and skips handlers.
            mv.visitJumpInsn(GOTO, afterHandlers);
            fallthrough = true;
        }
        closeRange(tryBlock, mv);
        // pop for try BlockRecorder
        compileStack.pop();

        BlockRecorder catches = null;
        if (!catchStatements.isEmpty()) {
            catches = makeBlockRecorder(finallyStatement);
            for (CatchStatement catchStatement : catchStatements) {
                Label catchBlock = startRange(catches, mv);

                compileStack.pushState();
                ClassNode type = catchStatement.getExceptionType();
                compileStack.defineVariable(catchStatement.getVariable(), type, true);
                catchStatement.visit(controller.getAcg());
                // Non-empty catch range / LVT span for empty catch bodies
                // (defineVariable starts the LVT after the store).
                mv.visitInsn(NOP);
                compileStack.pop();

                closeRange(catches, mv);
                if (maybeFallsThrough(catchStatement.getCode())) {
                    mv.visitJumpInsn(GOTO, afterHandlers);
                    fallthrough = true;
                }
                compileStack.writeExceptionTable(tryBlock, catchBlock, BytecodeHelper.getClassInternalName(type));
            }
        }

        if (hasFinally) {
            // Catch-all after typed handlers so it does not supersede them.
            Label catchAll = new Label(), afterCatchAll = new Label();
            compileStack.writeExceptionTable(tryBlock, catchAll, null);
            if (catches != null) {
                compileStack.writeExceptionTable(catches, catchAll, null);
                compileStack.pop(); // catches BlockRecorder
            }

            if (fallthrough) {
                mv.visitLabel(afterHandlers);
                finallyStatement.visit(controller.getAcg());
                // Skip over the catch-all finally/rethrow path.
                mv.visitJumpInsn(GOTO, afterCatchAll);
            }

            mv.visitLabel(catchAll);
            operandStack.push(ClassHelper.THROWABLE_TYPE);
            int anyThrowable = compileStack.defineTemporaryVariable("throwable", ClassHelper.THROWABLE_TYPE, true);

            finallyStatement.visit(controller.getAcg());

            mv.visitVarInsn(ALOAD, anyThrowable);
            mv.visitInsn(ATHROW);

            if (fallthrough) {
                mv.visitLabel(afterCatchAll);
            }
            compileStack.removeVar(anyThrowable);
        } else {
            // No catch-all identity rethrow — uncaught exceptions propagate.
            if (catches != null) {
                compileStack.pop(); // catches BlockRecorder
            }
            if (fallthrough) {
                mv.visitLabel(afterHandlers);
            }
        }
    }

    /**
     * Pushes a {@link BlockRecorder} that inlines {@code finallyStatement} on
     * abrupt exits from the protected region (return/break/continue), while
     * excluding that inlined body from the exception table so finally is not
     * applied twice. When the finally is empty the excluded statement is a
     * no-op, but the recorder still splits exception-table ranges around
     * enclosing finally inlines.
     */
    private BlockRecorder makeBlockRecorder(final Statement finallyStatement) {
        BlockRecorder recorder = new BlockRecorder();
        final CompileStack compileStack = controller.getCompileStack();

        recorder.excludedStatement = () -> {
            if (isEmptyStatement(finallyStatement)) return;
            // GROOVY-4721, GROOVY-12062: emit the finally with the try-block locals
            // hidden, then restore the try scope (and its variables) afterwards.
            compileStack.visitExcludedFinally(recorder, () -> finallyStatement.visit(controller.getAcg()));
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

    private static void closeRange(final BlockRecorder br, final MethodVisitor mv) {
        Label label = new Label();
        mv.visitLabel(label);
        br.closeRange(label);
    }

    /**
     * Generates bytecode for a switch statement.
     * Each {@code case} expression is compared using Groovy's {@code isCase} operator,
     * so non-integer switch expressions are supported.
     *
     * @param statement the switch statement to compile
     */
    public void writeSwitch(final SwitchStatement statement) {
        controller.getAcg().onLineNumber(statement, "visitSwitch");
        writeStatementLabel(statement);

        statement.getExpression().visit(controller.getAcg());
        ClassNode exprType = controller.getOperandStack().getTopOperand();
        if (ClassHelper.isPrimitiveType(exprType)) exprType = ClassHelper.getWrapper(exprType);

        // switch does not have a continue label; use enclosing continue label
        CompileStack compileStack = controller.getCompileStack();
        Label breakLabel = compileStack.pushSwitch(statement.getStatementLabels());

        int switchVariableIndex = compileStack.defineTemporaryVariable("switch", exprType, true);

        List<CaseStatement> caseStatements = statement.getCaseStatements();
        int caseCount = caseStatements.size();
        Label[] labels = new Label[caseCount + 1];
        for (int i = 0; i < caseCount; i += 1) {
            labels[i] = new Label();
        }

        int i = 0;
        for (Iterator<CaseStatement> iter = caseStatements.iterator(); iter.hasNext(); i += 1) {
            writeCaseStatement(iter.next(), statement, switchVariableIndex, labels[i], labels[i + 1]);
        }

        statement.getDefaultStatement().visit(controller.getAcg());

        if (maybeFallsThrough(statement) || statement.getStatementLabels() != null) {
            controller.getMethodVisitor().visitLabel(breakLabel);
        }
        compileStack.removeVar(switchVariableIndex);
        compileStack.pop();
    }

    private void writeCaseStatement(final CaseStatement caseStatement, final SwitchStatement switchStatement, final int switchVariableIndex, final Label thisLabel, final Label nextLabel) {
        controller.getAcg().onLineNumber(caseStatement, "visitCaseStatement");
        MethodVisitor mv = controller.getMethodVisitor();
        OperandStack operandStack = controller.getOperandStack();

        mv.visitVarInsn(ALOAD, switchVariableIndex);

        caseStatement.getExpression().visit(controller.getAcg());
        operandStack.box();
        controller.getBinaryExpressionHelper().getIsCaseMethod().call(mv);
        operandStack.replace(ClassHelper.boolean_TYPE);

        Label l0 = controller.getOperandStack().jump(IFEQ);

        mv.visitLabel(thisLabel);

        caseStatement.getCode().visit(controller.getAcg());

        // now if we don't finish with a break we need to jump past the next comparison
        if (nextLabel != null && maybeFallsThroughToNextSwitchCase(caseStatement.getCode(), switchStatement)) {
            mv.visitJumpInsn(GOTO, nextLabel);
        }

        mv.visitLabel(l0);
    }

    /**
     * Generates bytecode for a break statement, applying any intervening
     * finally blocks before the jump.
     *
     * @param statement the break statement to compile
     */
    public void writeBreak(final BreakStatement statement) {
        controller.getAcg().onLineNumber(statement, "visitBreakStatement");
        writeStatementLabel(statement);

        Label label = controller.getCompileStack().getNamedBreakLabel(statement.getLabel());
        controller.getCompileStack().applyFinallyBlocks(label, true);
        controller.getMethodVisitor().visitJumpInsn(GOTO, label);
    }

    /**
     * Generates bytecode for a continue statement, applying any intervening
     * finally blocks before the jump.
     *
     * @param statement the continue statement to compile
     */
    public void writeContinue(final ContinueStatement statement) {
        controller.getAcg().onLineNumber(statement, "visitContinueStatement");
        writeStatementLabel(statement);

        Label label = controller.getCompileStack().getNamedContinueLabel(statement.getLabel());
        controller.getCompileStack().applyFinallyBlocks(label, false);
        controller.getMethodVisitor().visitJumpInsn(GOTO, label);
    }

    /**
     * Generates bytecode for a synchronized statement.
     * Stores the monitor object in a local variable, emits
     * {@code MONITORENTER}/{@code MONITOREXIT} guards, and registers
     * a catch-all exception handler that exits the monitor before rethrowing.
     *
     * @param statement the synchronized statement to compile
     */
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

        boolean fallthrough = maybeFallsThrough(statement.getCode());
        if (fallthrough) {
            finallyPart.run();
            mv.visitJumpInsn(GOTO, synchronizedEnd);
        }
        mv.visitLabel(catchAll);
        finallyPart.run();
        mv.visitInsn(ATHROW);

        if (fallthrough) {
            mv.visitLabel(synchronizedEnd);
        }
        compileStack.removeVar(index);
    }

    /**
     * Generates bytecode for an assert statement.
     *
     * @param statement the assert statement to compile
     */
    public void writeAssert(final AssertStatement statement) {
        controller.getAcg().onLineNumber(statement, "visitAssertStatement");
        writeStatementLabel(statement);
        controller.getAssertionWriter().writeAssertStatement(statement);
    }

    /**
     * Generates bytecode for a throw statement.
     * Casts the expression to {@code Throwable} and emits {@code ATHROW}.
     *
     * @param statement the throw statement to compile
     */
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

    /**
     * Generates bytecode for a return statement.
     * For void methods emits {@code RETURN} after applying any finally blocks.
     * For value-returning methods evaluates the expression, casts it to the
     * declared return type, and emits the appropriate typed return instruction.
     *
     * @param statement the return statement to compile
     */
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

    /**
     * Generates bytecode for an expression statement.
     * Evaluates the expression and discards any value left on the operand stack.
     * Marks method-call and binary expressions so that unused return values
     * are elided rather than boxed.
     *
     * @param statement the expression statement to compile
     */
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
