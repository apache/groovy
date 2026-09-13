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

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.stmt.CaseStatement;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.classgen.asm.OperandStack;
import org.codehaus.groovy.classgen.asm.WriterController;
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

import static org.apache.groovy.ast.tools.SwitchExpressionUtils.enumConstantName;
import static org.apache.groovy.ast.tools.SwitchExpressionUtils.intConstant;
import static org.apache.groovy.ast.tools.SwitchExpressionUtils.isOptimizedEnumSwitch;
import static org.apache.groovy.ast.tools.SwitchExpressionUtils.isOptimizedIntSwitch;
import static org.apache.groovy.ast.tools.SwitchExpressionUtils.isOptimizedStringSwitch;
import static org.apache.groovy.ast.tools.SwitchExpressionUtils.stringConstant;
import static org.apache.groovy.ast.tools.SwitchExpressionUtils.unwrapEnumType;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

/**
 * Emits the JVM dispatch shared by statically compiled switch statements and
 * switch expressions: {@code tableswitch} or {@code lookupswitch} for int-family
 * labels, {@code lookupswitch} on {@code hashCode} plus {@code equals} for
 * strings, and the latter over {@code Enum.name()} for enums.
 * <p>
 * Callers own everything around the dispatch: evaluating the selector into a
 * local, laying out the arm bodies with {@link #emitArmCode}, and deciding what
 * happens when nothing matches, which differs between a statement and an
 * expression.
 *
 * @since 7.0.0
 */
class SwitchDispatchWriter {

    private final WriterController controller;

    SwitchDispatchWriter(final WriterController controller) {
        this.controller = controller;
    }

    /**
     * Whether the selector and labels support a jump table: every label is a
     * constant of a type {@code javac} would switch on, and no key repeats.
     * <p>
     * Duplicate keys cannot be expressed in a jump table, so they fall back to
     * sequential first-match-wins dispatch, which preserves the behaviour of a
     * switch the type checker did not reject (GROOVY-12289).
     */
    static boolean isDispatchable(final ClassNode selectorType, final List<CaseStatement> caseStatements) {
        if (isOptimizedIntSwitch(selectorType, caseStatements)) {
            return hasDistinctKeys(caseStatements, cs -> intConstant(cs.getExpression()));
        }
        if (isOptimizedStringSwitch(selectorType, caseStatements)) {
            return hasDistinctKeys(caseStatements, cs -> stringConstant(cs.getExpression()));
        }
        if (isOptimizedEnumSwitch(selectorType, caseStatements)) {
            ClassNode enumType = unwrapEnumType(selectorType);
            return hasDistinctKeys(caseStatements, cs -> enumConstantName(cs.getExpression(), enumType));
        }
        return false;
    }

    private static <K> boolean hasDistinctKeys(final List<CaseStatement> caseStatements,
            final Function<CaseStatement, K> keyFn) {
        Set<K> seen = new HashSet<>();
        for (CaseStatement caseStatement : caseStatements) {
            if (!seen.add(keyFn.apply(caseStatement))) return false;
        }
        return true;
    }

    /**
     * Forward pass extracts every constant key (or skips the optimizer).
     * Backward pass gives empty colon prefixes the following body's label,
     * or {@code defaultTarget} when the empty suffix falls into default.
     * Duplicate keys also skip the optimizer: the type checker reports them
     * as an error for a switch expression (GROOVY-12289), so reaching here
     * with one means checking was bypassed ({@code TypeCheckingMode.SKIP} or
     * an extension), where sequential dispatch preserves dynamic semantics.
     */
    <K> ArmGroup<K> groupArms(final List<CaseStatement> caseStatements,
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

    /**
     * Emits {@code tableswitch} or {@code lookupswitch}, whichever has the
     * smaller classfile payload, over an int-typed local.
     */
    void emitIntSwitch(final List<Integer> keys, final List<Label> targets,
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

    /**
     * Emits {@code lookupswitch} on {@code hashCode()} plus {@code equals},
     * jumping straight to the shared arm label. Fall-through keys already
     * share that label, so a second index tableswitch is unnecessary.
     */
    void emitStringHashDispatch(final int stringLocal, final List<String> keys,
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
     * Replaces the enum selector in {@code selectorIndex} with its
     * {@code name()} in a fresh local, and returns that local. Constant names
     * are stable when a separately compiled enum adds or reorders constants,
     * so arms are never silently retargeted.
     */
    int loadEnumName(final int selectorIndex, final ClassNode selectorType) {
        OperandStack operandStack = controller.getOperandStack();
        operandStack.load(selectorType, selectorIndex);
        controller.getMethodVisitor().visitMethodInsn(
                INVOKEVIRTUAL, "java/lang/Enum", "name", "()Ljava/lang/String;", false);
        operandStack.replace(ClassHelper.STRING_TYPE);
        return controller.getCompileStack().defineTemporaryVariable(
                "$switchEnumName", ClassHelper.STRING_TYPE, true);
    }

    /** Lays out the arm bodies in source order, so an arm without a jump falls into the next. */
    void emitArmCode(final List<CaseStatement> caseStatements, final List<Label> targets) {
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

    /** Branches to {@code target} when the selector is null; a primitive selector never is. */
    void jumpIfNull(final int selectorIndex, final ClassNode selectorType, final Label target) {
        if (ClassHelper.isPrimitiveType(selectorType)) {
            return;
        }
        OperandStack operandStack = controller.getOperandStack();
        operandStack.load(selectorType, selectorIndex);
        operandStack.remove(1);
        controller.getMethodVisitor().visitJumpInsn(IFNULL, target);
    }

    /**
     * {@code keys == null} means "not an optimizable constant switch, try the
     * next optimizer (or fall back to sequential dispatch)".
     */
    static final class ArmGroup<K> {
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
