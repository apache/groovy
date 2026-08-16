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
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.SwitchExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.CaseStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.YieldStatement;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.List;

import static org.codehaus.groovy.ast.tools.GeneralUtils.maybeFallsThrough;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;

/**
 * Emits JVM bytecode for a {@link SwitchExpression}. Completing arms leave the
 * result on the operand stack and jump to a shared join point — the same shape
 * javac uses for JEP 361 switch expressions.
 * <p>
 * This writer implements sequential {@code isCase} matching so Class, regex,
 * Collection and Closure cases keep working. Tableswitch / lookupswitch
 * specializations live on the static-compilation subclass.
 *
 * @since 6.0.0
 */
public class SwitchExpressionWriter {

    private static final String ISE_INTERNAL_NAME = "java/lang/IllegalStateException";
    private static final String ICCE_INTERNAL_NAME = "java/lang/IncompatibleClassChangeError";
    private static final String STRING_BUILDER_INTERNAL_NAME = "java/lang/StringBuilder";

    /** The controller coordinating all bytecode writers for the current class. */
    protected final WriterController controller;

    /**
     * Creates a switch-expression writer with the given controller.
     *
     * @param controller the writer controller
     */
    public SwitchExpressionWriter(final WriterController controller) {
        this.controller = controller;
    }

    /**
     * Generates bytecode for a switch expression. The result is left on the
     * operand stack with the expression's resolved type.
     *
     * @param expression the switch expression to compile
     */
    public void writeSwitchExpression(final SwitchExpression expression) {
        AsmClassGenerator acg = controller.getAcg();
        acg.onLineNumber(expression, "visitSwitchExpression");

        CompileStack compileStack = controller.getCompileStack();
        OperandStack operandStack = controller.getOperandStack();
        MethodVisitor mv = controller.getMethodVisitor();

        ClassNode resultType = resolveResultType(expression);
        Label endLabel = compileStack.pushSwitchExpression(resultType);

        expression.getExpression().visit(acg);
        ClassNode selectorType = prepareSelectorType(operandStack);
        int selectorIndex = compileStack.defineTemporaryVariable("switch", selectorType, true);

        if (!writeOptimizedSwitch(expression, selectorIndex, selectorType)) {
            writeIsCaseSwitch(expression, selectorIndex, selectorType);
        }

        mv.visitLabel(endLabel);
        operandStack.push(resultType);

        compileStack.removeVar(selectorIndex);
        compileStack.popSwitchExpression();
    }

    /**
     * Generates bytecode for a {@code yield} statement: evaluate the operand,
     * cast it to the enclosing switch-expression result type, apply intervening
     * finally blocks, and jump to the expression join point.
     *
     * @param statement the yield statement to compile
     */
    public void writeYield(final YieldStatement statement) {
        CompileStack compileStack = controller.getCompileStack();
        CompileStack.SwitchExpressionContext context = compileStack.requireSwitchExpressionContext();

        controller.getAcg().onLineNumber(statement, "visitYieldStatement");
        OperandStack operandStack = controller.getOperandStack();
        MethodVisitor mv = controller.getMethodVisitor();
        statement.getExpression().visit(controller.getAcg());
        operandStack.doGroovyCast(context.resultType);
        if (compileStack.hasBlockRecorder()) {
            // stash the result so intervening finally / synchronized can run
            // without seeing it on the operand stack (same shape as return)
            int rv = compileStack.defineTemporaryVariable("$yield", context.resultType, true);
            compileStack.applyFinallyBlocks(context.endLabel, true);
            operandStack.load(context.resultType, rv);
            operandStack.remove(1);
            compileStack.removeVar(rv);
        } else {
            operandStack.remove(1);
        }
        mv.visitJumpInsn(GOTO, context.endLabel);
    }

    /**
     * Returns the type to store the selector under. Dynamic compilation boxes
     * so the subsequent {@code isCase} tests see a reference.
     */
    protected ClassNode prepareSelectorType(final OperandStack operandStack) {
        return operandStack.box();
    }

    /**
     * Attempts a specialized dispatch (tableswitch / lookupswitch). The base
     * writer has none; static compilation overrides this.
     *
     * @return {@code true} if specialized bytecode was emitted
     */
    protected boolean writeOptimizedSwitch(final SwitchExpression expression,
            final int selectorIndex, final ClassNode selectorType) {
        return false;
    }

    /**
     * Emits one {@code isCase} test. Shared with {@link StatementWriter} via
     * {@link BinaryExpressionHelper#writeIsCase}.
     */
    protected void writeIsCaseComparison(final Expression caseValue,
            final int selectorIndex, final ClassNode selectorType) {
        controller.getBinaryExpressionHelper().writeIsCase(selectorIndex, selectorType, caseValue);
    }

    //--------------------------------------------------------------------------

    private void writeIsCaseSwitch(final SwitchExpression expression,
            final int selectorIndex, final ClassNode selectorType) {
        List<CaseStatement> caseStatements = expression.getCaseStatements();
        int caseCount = caseStatements.size();
        Label[] nextTargets = new Label[caseCount + 1];
        for (int i = 0; i < caseCount; i += 1) {
            nextTargets[i] = new Label();
        }

        for (int i = 0; i < caseCount; i += 1) {
            writeIsCaseArm(caseStatements.get(i), selectorIndex, selectorType, nextTargets[i], nextTargets[i + 1]);
        }

        writeDefaultOrThrow(expression, selectorIndex, selectorType, false);
    }

    private void writeIsCaseArm(final CaseStatement caseStatement, final int selectorIndex,
            final ClassNode selectorType, final Label thisTarget, final Label nextTarget) {
        AsmClassGenerator acg = controller.getAcg();
        MethodVisitor mv = controller.getMethodVisitor();

        acg.onLineNumber(caseStatement, "visitCaseStatement");

        writeIsCaseComparison(caseStatement.getExpression(), selectorIndex, selectorType);

        Label miss = controller.getOperandStack().jump(IFEQ);

        mv.visitLabel(thisTarget);
        caseStatement.getCode().visit(acg);

        if (nextTarget != null && maybeFallsThrough(caseStatement.getCode())) {
            mv.visitJumpInsn(GOTO, nextTarget);
        }

        mv.visitLabel(miss);
    }

    /**
     * Emits the default arm, or an unmatched-selector throw.
     *
     * @param completeEnum {@code true} when a complete enum switch should throw
     *                     {@link IncompatibleClassChangeError} instead of
     *                     {@link IllegalStateException}
     */
    protected void writeDefaultOrThrow(final SwitchExpression expression, final int selectorIndex,
            final ClassNode selectorType, final boolean completeEnum) {
        Statement defaultStatement = expression.getDefaultStatement();
        if (defaultStatement != null && !defaultStatement.isEmpty()) {
            defaultStatement.visit(controller.getAcg());
            return;
        }
        if (completeEnum) {
            throwIncompatibleClassChangeError();
        } else {
            throwUnmatchedSelector(selectorIndex, selectorType);
        }
    }

    /**
     * Throws {@code IllegalStateException} naming the unmatched selector.
     */
    protected void throwUnmatchedSelector(final int selectorIndex, final ClassNode selectorType) {
        MethodVisitor mv = controller.getMethodVisitor();
        OperandStack operandStack = controller.getOperandStack();
        mv.visitTypeInsn(NEW, ISE_INTERNAL_NAME);
        mv.visitInsn(DUP);
        mv.visitTypeInsn(NEW, STRING_BUILDER_INTERNAL_NAME);
        mv.visitInsn(DUP);
        mv.visitLdcInsn("the switch expression does not cover the value ");
        mv.visitMethodInsn(INVOKESPECIAL, STRING_BUILDER_INTERNAL_NAME, "<init>", "(Ljava/lang/String;)V", false);
        operandStack.load(selectorType, selectorIndex);
        operandStack.box();
        mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER_INTERNAL_NAME, "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;", false);
        operandStack.remove(1);
        mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER_INTERNAL_NAME, "toString", "()Ljava/lang/String;", false);
        mv.visitMethodInsn(INVOKESPECIAL, ISE_INTERNAL_NAME, "<init>", "(Ljava/lang/String;)V", false);
        mv.visitInsn(ATHROW);
    }

    /**
     * Throws {@code IncompatibleClassChangeError} for an enum constant added
     * after an exhaustive switch expression was compiled.
     */
    protected void throwIncompatibleClassChangeError() {
        MethodVisitor mv = controller.getMethodVisitor();
        mv.visitTypeInsn(NEW, ICCE_INTERNAL_NAME);
        mv.visitInsn(DUP);
        mv.visitLdcInsn("enum constant added after this switch expression was compiled");
        mv.visitMethodInsn(INVOKESPECIAL, ICCE_INTERNAL_NAME, "<init>", "(Ljava/lang/String;)V", false);
        mv.visitInsn(ATHROW);
    }

    /**
     * Resolves the type left on the operand stack at the switch-expression join.
     */
    protected ClassNode resolveResultType(final SwitchExpression expression) {
        ClassNode type = controller.getTypeChooser().resolveType(expression, controller.getClassNode());
        if (type == null || ClassHelper.isDynamicTyped(type) || ClassHelper.isPrimitiveVoid(type)) {
            type = ClassHelper.OBJECT_TYPE;
        }
        return type;
    }

    protected static boolean isIntegralType(final ClassNode type) {
        return ClassHelper.isPrimitiveInt(type) || ClassHelper.isPrimitiveByte(type)
                || ClassHelper.isPrimitiveShort(type) || ClassHelper.isPrimitiveChar(type);
    }

    protected static boolean isIntegralWrapper(final ClassNode type) {
        return ClassHelper.isWrapperInteger(type) || ClassHelper.isWrapperByte(type)
                || ClassHelper.isWrapperShort(type) || ClassHelper.isWrapperCharacter(type);
    }

    protected static Integer intConstant(final Expression expression) {
        if (!(expression instanceof ConstantExpression constant)) return null;
        Object value = constant.getValue();
        if (value instanceof Integer || value instanceof Byte || value instanceof Short) {
            return ((Number) value).intValue();
        }
        if (value instanceof Character) {
            return (int) (Character) value;
        }
        return null;
    }

    protected static String stringConstant(final Expression expression) {
        if (expression instanceof ConstantExpression constant && constant.getValue() instanceof String s) {
            return s;
        }
        return null;
    }

    protected static ClassNode unwrapEnumType(final ClassNode type) {
        if (type == null) return null;
        if (type.isEnum()) return type;
        return type.redirect().isEnum() ? type.redirect() : null;
    }

    protected static int enumConstantCount(final ClassNode enumType) {
        int count = 0;
        for (FieldNode field : enumType.redirect().getFields()) {
            if (field.isEnum()) count += 1;
        }
        if (count == 0 && enumType.isResolved()) {
            Object[] constants = enumType.getTypeClass().getEnumConstants();
            count = constants == null ? 0 : constants.length;
        }
        return count;
    }

    protected static String enumConstantName(final Expression expression, final ClassNode enumType) {
        if (expression instanceof PropertyExpression property
                && property.getObjectExpression() instanceof ClassExpression classExpression
                && classExpression.getType().equals(enumType)
                && property.getProperty() instanceof ConstantExpression name) {
            return name.getText();
        }
        if (expression instanceof VariableExpression variable
                && (variable.getAccessedVariable() == null || variable.getAccessedVariable() instanceof DynamicVariable)) {
            // a real local or parameter named like a constant is not a constant label
            FieldNode field = enumType.getField(variable.getName());
            if (field != null && field.isEnum()) {
                return variable.getName();
            }
        }
        if (expression instanceof ConstantExpression constant && enumType.isResolved()
                && constant.getValue() instanceof Enum<?> e) {
            Class<?> declaring = e.getDeclaringClass();
            Class<?> enumClass = enumType.getTypeClass();
            if (declaring == enumClass || enumClass.isAssignableFrom(declaring)) {
                return e.name();
            }
        }
        return null;
    }
}
