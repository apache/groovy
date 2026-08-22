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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;

import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

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
    }

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
        ArmGroup<Integer> group = groupArms(expression.getCaseStatements(),
                cs -> intConstant(cs.getExpression()), defaultTarget);
        if (group.keys == null) return false;

        OperandStack operandStack = controller.getOperandStack();
        CompileStack compileStack = controller.getCompileStack();

        int intSelector = selectorIndex;
        if (!primitive) {
            jumpIfNull(selectorIndex, selectorType, defaultTarget);
            operandStack.load(selectorType, selectorIndex);
            operandStack.doGroovyCast(ClassHelper.int_TYPE);
            intSelector = compileStack.defineTemporaryVariable("$switchInt", ClassHelper.int_TYPE, true);
        }

        emitIntSwitch(group.keys, group.targets, defaultTarget, intSelector);
        finishArms(expression, group.targets, defaultTarget, selectorIndex, selectorType, false);

        if (!primitive) {
            compileStack.removeVar(intSelector);
        }
        return true;
    }

    private void emitIntSwitch(final List<Integer> keys, final List<Label> targets,
            final Label defaultTarget, final int intSelector) {
        TreeMap<Integer, Label> sorted = new TreeMap<>();
        for (int i = 0; i < keys.size(); i += 1) {
            sorted.put(keys.get(i), targets.get(i));
        }
        int n = sorted.size();
        int[] keyArray = new int[n];
        Label[] targetArray = new Label[n];
        int i = 0;
        for (var entry : sorted.entrySet()) {
            keyArray[i] = entry.getKey();
            targetArray[i] = entry.getValue();
            i += 1;
        }

        OperandStack operandStack = controller.getOperandStack();
        operandStack.load(ClassHelper.int_TYPE, intSelector);
        operandStack.remove(1);

        int min = keyArray[0];
        int max = keyArray[n - 1];
        long span = (long) max - (long) min + 1L;
        // classfile payload, padding omitted (JVMS §6.5): tableswitch ≈ 12+4*span,
        // lookupswitch ≈ 8+8*n. Prefer tableswitch when it is no larger.
        long tableSize = 12L + 4L * span;
        long lookupSize = 8L + 8L * n;
        MethodVisitor mv = controller.getMethodVisitor();
        if (span > 0 && span <= Integer.MAX_VALUE && tableSize <= lookupSize) {
            Label[] table = new Label[(int) span];
            Arrays.fill(table, defaultTarget);
            for (int k = 0; k < n; k += 1) {
                table[(int) ((long) keyArray[k] - min)] = targetArray[k];
            }
            mv.visitTableSwitchInsn(min, max, defaultTarget, table);
        } else {
            mv.visitLookupSwitchInsn(defaultTarget, keyArray, targetArray);
        }
    }

    private boolean writeStringSwitch(final SwitchExpression expression,
            final int selectorIndex, final ClassNode selectorType) {
        if (!ClassHelper.isStringType(selectorType)) {
            return false;
        }

        Label defaultTarget = new Label();
        ArmGroup<String> group = groupArms(expression.getCaseStatements(),
                cs -> stringConstant(cs.getExpression()), defaultTarget);
        if (group.keys == null) return false;

        jumpIfNull(selectorIndex, selectorType, defaultTarget);
        emitStringHashDispatch(selectorIndex, group.keys, group.targets, defaultTarget);
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
        ArmGroup<String> group = groupArms(expression.getCaseStatements(),
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
        jumpIfNull(selectorIndex, selectorType, nullTarget);

        operandStack.load(selectorType, selectorIndex);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Enum", "name", "()Ljava/lang/String;", false);
        operandStack.replace(ClassHelper.STRING_TYPE);
        int nameLocal = compileStack.defineTemporaryVariable("$switchEnumName", ClassHelper.STRING_TYPE, true);

        emitStringHashDispatch(nameLocal, group.keys, group.targets, defaultTarget);
        finishArms(expression, group.targets, defaultTarget, selectorIndex, selectorType, complete);
        if (nullTarget != defaultTarget) {
            mv.visitLabel(nullTarget);
            throwUnmatchedSelector(selectorIndex, selectorType);
        }

        compileStack.removeVar(nameLocal);
        return true;
    }

    /**
     * Emits {@code lookupswitch} on {@code hashCode()} plus {@code equals},
     * jumping straight to the shared arm label. Fall-through keys already
     * share that label, so a second index tableswitch is unnecessary.
     */
    private void emitStringHashDispatch(final int stringLocal, final List<String> keys,
            final List<Label> targets, final Label defaultTarget) {
        Map<Integer, List<Integer>> hashToIndexes = new TreeMap<>();
        for (int i = 0; i < keys.size(); i += 1) {
            hashToIndexes.computeIfAbsent(keys.get(i).hashCode(), h -> new ArrayList<>()).add(i);
        }
        MethodVisitor mv = controller.getMethodVisitor();
        OperandStack operandStack = controller.getOperandStack();

        operandStack.load(ClassHelper.STRING_TYPE, stringLocal);
        operandStack.doGroovyCast(ClassHelper.STRING_TYPE);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "hashCode", "()I", false);
        operandStack.replace(ClassHelper.int_TYPE);
        operandStack.remove(1);

        int[] hashes = hashToIndexes.keySet().stream().mapToInt(Integer::intValue).toArray();
        Label[] hashTargets = new Label[hashes.length];
        for (int i = 0; i < hashes.length; i += 1) {
            hashTargets[i] = new Label();
        }
        mv.visitLookupSwitchInsn(defaultTarget, hashes, hashTargets);

        for (int i = 0; i < hashes.length; i += 1) {
            mv.visitLabel(hashTargets[i]);
            for (int index : hashToIndexes.get(hashes[i])) {
                operandStack.load(ClassHelper.STRING_TYPE, stringLocal);
                mv.visitLdcInsn(keys.get(index));
                operandStack.push(ClassHelper.STRING_TYPE);
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
                operandStack.replace(ClassHelper.boolean_TYPE, 2);
                Label next = operandStack.jump(IFEQ);
                mv.visitJumpInsn(GOTO, targets.get(index));
                mv.visitLabel(next);
            }
            mv.visitJumpInsn(GOTO, defaultTarget);
        }
    }

    /**
     * Forward pass extracts every constant key (or skips the optimizer).
     * Backward pass gives empty colon prefixes the following body's label,
     * or {@code defaultTarget} when the empty suffix falls into default.
     * Duplicate keys also skip the optimizer: the type checker reports them
     * as an error (GROOVY-12289), so reaching here with one means checking
     * was bypassed ({@code TypeCheckingMode.SKIP} or an extension), where
     * sequential first-match-wins dispatch preserves dynamic semantics.
     */
    private <K> ArmGroup<K> groupArms(final List<CaseStatement> caseStatements,
            final Function<CaseStatement, K> keyFn, final Label defaultTarget) {
        int n = caseStatements.size();
        if (n == 0) return ArmGroup.skip();

        List<K> keys = new ArrayList<>(n);
        Set<K> seen = new HashSet<>();
        for (CaseStatement caseStatement : caseStatements) {
            K key = keyFn.apply(caseStatement);
            if (key == null || !seen.add(key)) return ArmGroup.skip();
            keys.add(key);
        }

        Label[] targets = new Label[n];
        Label current = defaultTarget;
        for (int i = n - 1; i >= 0; i -= 1) {
            if (!caseStatements.get(i).getCode().isEmpty()) {
                current = new Label();
            }
            targets[i] = current;
        }
        return ArmGroup.of(keys, Arrays.asList(targets));
    }

    private void emitArmCode(final List<CaseStatement> caseStatements, final List<Label> targets) {
        AsmClassGenerator acg = controller.getAcg();
        MethodVisitor mv = controller.getMethodVisitor();
        for (int i = 0; i < caseStatements.size(); i += 1) {
            CaseStatement caseStatement = caseStatements.get(i);
            if (caseStatement.getCode().isEmpty()) {
                continue;
            }
            mv.visitLabel(targets.get(i));
            caseStatement.getCode().visit(acg);
        }
    }

    private void finishArms(final SwitchExpression expression, final List<Label> targets,
            final Label defaultTarget, final int selectorIndex, final ClassNode selectorType,
            final boolean completeEnum) {
        emitArmCode(expression.getCaseStatements(), targets);
        controller.getMethodVisitor().visitLabel(defaultTarget);
        writeDefaultOrThrow(expression, selectorIndex, selectorType, completeEnum);
    }

    private void jumpIfNull(final int selectorIndex, final ClassNode selectorType, final Label target) {
        OperandStack operandStack = controller.getOperandStack();
        operandStack.load(selectorType, selectorIndex);
        operandStack.remove(1);
        controller.getMethodVisitor().visitJumpInsn(IFNULL, target);
    }

    /**
     * {@code keys == null} means "not an optimizable constant switch, try the
     * next optimizer (or fall back to sequential dispatch)".
     */
    private static final class ArmGroup<K> {
        final List<K> keys;
        final List<Label> targets;

        private ArmGroup(final List<K> keys, final List<Label> targets) {
            this.keys = keys;
            this.targets = targets;
        }

        static <K> ArmGroup<K> skip() {
            return new ArmGroup<>(null, null);
        }

        static <K> ArmGroup<K> of(final List<K> keys, final List<Label> targets) {
            return new ArmGroup<>(keys, targets);
        }
    }
}
