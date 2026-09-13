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
package org.codehaus.groovy.classgen.asm.sc;

import org.apache.groovy.ast.tools.ExpressionUtils;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.SwitchExpression;
import org.codehaus.groovy.ast.stmt.CaseStatement;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.classgen.asm.CompileStack;
import org.codehaus.groovy.classgen.asm.OperandStack;
import org.codehaus.groovy.classgen.asm.SwitchExpressionWriter;
import org.codehaus.groovy.classgen.asm.VariableSlotLoader;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.List;

import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFNULL;

/**
 * Static-compilation writer for {@link SwitchExpression}. Emits
 * {@code tableswitch} / {@code lookupswitch} when the selector and labels are
 * constants of a type {@code javac} would switch on, and otherwise the
 * {@code isCase} method selected by the type checker as a direct call.
 *
 * @since 6.0.0
 */
public class StaticTypesSwitchExpressionWriter extends SwitchExpressionWriter {

    /**
     * Creates a switch-expression writer for statically compiled methods.
     *
     * @param controller the static types writer controller
     */
    public StaticTypesSwitchExpressionWriter(final StaticTypesWriterController controller) {
        super(controller);
        this.dispatch = new SwitchDispatchWriter(controller);
    }

    private final SwitchDispatchWriter dispatch;

    /**
     * Keeps the selector as visited — boxing is deferred until a path actually
     * needs a reference (isCase or a null check on a wrapper).
     */
    @Override
    protected ClassNode prepareSelectorType(final OperandStack operandStack) {
        return operandStack.getTopOperand();
    }

    @Override
    protected boolean writeOptimizedSwitch(final SwitchExpression expression,
            final int selectorIndex, final ClassNode selectorType) {
        return writeIntSwitch(expression, selectorIndex, selectorType)
                || writeStringSwitch(expression, selectorIndex, selectorType)
                || writeEnumSwitch(expression, selectorIndex, selectorType);
    }

    /**
     * Emits the {@code isCase} call selected by the type checker as a direct
     * method call via {@link AsmClassGenerator#visitMethodCallExpression},
     * which reaches {@code StaticInvocationWriter.writeDirectMethodCall}.
     * A literal {@code case null} is identity, not {@code DGM.isCase(Object,Object)}
     * (which NPEs). Any other arm must already carry
     * {@link StaticTypesMarker#DIRECT_METHOD_CALL_TARGET} on the
     * {@link CaseStatement}; missing targets are a type-checking error except
     * under {@code TypeCheckingMode.SKIP}.
     */
    @Override
    protected void writeIsCaseComparison(final CaseStatement caseStatement,
            final int selectorIndex, final ClassNode selectorType) {
        Expression caseValue = caseStatement.getExpression();
        if (ExpressionUtils.isNullConstant(caseValue)) {
            writeNullIdentity(selectorIndex, selectorType);
            return;
        }
        MethodNode target = caseStatement.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET);
        if (target == null) {
            // TypeCheckingMode.SKIP (and any residual miss): SBA keeps a boolean
            // on the operand stack for the following IFEQ.
            super.writeIsCaseComparison(caseStatement, selectorIndex, selectorType);
            return;
        }
        OperandStack operandStack = controller.getOperandStack();
        VariableSlotLoader selector = new VariableSlotLoader(selectorType, selectorIndex, operandStack);
        MethodCallExpression call = callX(caseValue, "isCase", args(selector));
        call.setImplicitThis(false);
        call.setMethodTarget(target);
        call.putNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET, target);
        call.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, ClassHelper.boolean_TYPE);
        if (caseStatement.getNodeMetaData(StaticTypesMarker.PV_METHODS_ACCESS) != null) {
            call.putNodeMetaData(StaticTypesMarker.PV_METHODS_ACCESS,
                    caseStatement.getNodeMetaData(StaticTypesMarker.PV_METHODS_ACCESS));
        }
        call.setSourcePosition(caseValue);
        call.visit(controller.getAcg());
        operandStack.doGroovyCast(ClassHelper.boolean_TYPE);
    }

    /**
     * {@code case null} is {@code selector == null}. A primitive selector
     * can never be null, so the arm is a compile-time miss.
     */
    private void writeNullIdentity(final int selectorIndex, final ClassNode selectorType) {
        OperandStack operandStack = controller.getOperandStack();
        MethodVisitor mv = controller.getMethodVisitor();
        if (ClassHelper.isPrimitiveType(selectorType)) {
            mv.visitInsn(ICONST_0);
            operandStack.push(ClassHelper.boolean_TYPE);
            return;
        }
        operandStack.load(selectorType, selectorIndex);
        operandStack.remove(1);
        Label isNull = new Label();
        Label end = new Label();
        mv.visitJumpInsn(IFNULL, isNull);
        mv.visitInsn(ICONST_0);
        mv.visitJumpInsn(GOTO, end);
        mv.visitLabel(isNull);
        mv.visitInsn(ICONST_1);
        mv.visitLabel(end);
        operandStack.push(ClassHelper.boolean_TYPE);
    }

    //--------------------------------------------------------------------------
    // tableswitch / lookupswitch

    private boolean writeIntSwitch(final SwitchExpression expression,
            final int selectorIndex, final ClassNode selectorType) {
        boolean primitive = isIntegralType(selectorType);
        if (!primitive && !isIntegralWrapper(selectorType)) return false;

        Label defaultTarget = new Label();
        SwitchDispatchWriter.ArmGroup<Integer> group = dispatch.groupArms(expression.getCaseStatements(),
                cs -> intConstant(cs.getExpression()), defaultTarget);
        if (group.keys == null) return false;

        OperandStack operandStack = controller.getOperandStack();
        CompileStack compileStack = controller.getCompileStack();

        int intSelector = selectorIndex;
        if (!primitive) {
            dispatch.jumpIfNull(selectorIndex, selectorType, defaultTarget);
            operandStack.load(selectorType, selectorIndex);
            operandStack.doGroovyCast(ClassHelper.int_TYPE);
            intSelector = compileStack.defineTemporaryVariable("$switchInt", ClassHelper.int_TYPE, true);
        }

        dispatch.emitIntSwitch(group.keys, group.targets, defaultTarget, intSelector);
        finishArms(expression, group.targets, defaultTarget, selectorIndex, selectorType, false);

        if (!primitive) {
            compileStack.removeVar(intSelector);
        }
        return true;
    }

    private boolean writeStringSwitch(final SwitchExpression expression,
            final int selectorIndex, final ClassNode selectorType) {
        if (!ClassHelper.isStringType(selectorType)) {
            return false;
        }

        Label defaultTarget = new Label();
        SwitchDispatchWriter.ArmGroup<String> group = dispatch.groupArms(expression.getCaseStatements(),
                cs -> stringConstant(cs.getExpression()), defaultTarget);
        if (group.keys == null) return false;

        dispatch.jumpIfNull(selectorIndex, selectorType, defaultTarget);
        dispatch.emitStringHashDispatch(selectorIndex, group.keys, group.targets, defaultTarget);
        finishArms(expression, group.targets, defaultTarget, selectorIndex, selectorType, false);
        return true;
    }

    /**
     * Switches on {@code Enum.name()} through the string-switch machinery.
     * Constant names are stable when a separately compiled enum adds or
     * reorders constants, so arms are never silently retargeted.
     */
    private boolean writeEnumSwitch(final SwitchExpression expression,
            final int selectorIndex, final ClassNode selectorType) {
        ClassNode enumType = unwrapEnumType(selectorType);
        if (enumType == null || !enumType.isEnum()) return false;

        Label defaultTarget = new Label();
        SwitchDispatchWriter.ArmGroup<String> group = dispatch.groupArms(expression.getCaseStatements(),
                cs -> enumConstantName(cs.getExpression(), enumType), defaultTarget);
        if (group.keys == null) return false;

        MethodVisitor mv = controller.getMethodVisitor();
        OperandStack operandStack = controller.getOperandStack();
        CompileStack compileStack = controller.getCompileStack();

        boolean hasDefault = expression.getDefaultStatement() != null && !expression.getDefaultStatement().isEmpty();
        boolean complete = !hasDefault && group.keys.size() == enumConstantCount(enumType);

        // a null selector matches no constant label; with no default it must
        // throw ISE, never the complete-enum ICCE
        Label nullTarget = complete ? new Label() : defaultTarget;
        dispatch.jumpIfNull(selectorIndex, selectorType, nullTarget);

        int nameLocal = dispatch.loadEnumName(selectorIndex, selectorType);

        dispatch.emitStringHashDispatch(nameLocal, group.keys, group.targets, defaultTarget);
        finishArms(expression, group.targets, defaultTarget, selectorIndex, selectorType, complete);
        if (nullTarget != defaultTarget) {
            mv.visitLabel(nullTarget);
            throwUnmatchedSelector(selectorIndex, selectorType);
        }

        compileStack.removeVar(nameLocal);
        return true;
    }

    private void finishArms(final SwitchExpression expression, final List<Label> targets,
            final Label defaultTarget, final int selectorIndex, final ClassNode selectorType,
            final boolean completeEnum) {
        dispatch.emitArmCode(expression.getCaseStatements(), targets);
        controller.getMethodVisitor().visitLabel(defaultTarget);
        writeDefaultOrThrow(expression, selectorIndex, selectorType, completeEnum);
    }

}
