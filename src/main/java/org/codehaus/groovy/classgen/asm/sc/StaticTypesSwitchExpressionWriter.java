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
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;

import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.chooseBestMethod;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.findDGMMethodsByNameAndArguments;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.ISTORE;

/**
 * Static-compilation writer for {@link SwitchExpression}. Emits
 * {@code tableswitch} / {@code lookupswitch} when the selector and labels are
 * constants of a type {@code javac} would switch on, and otherwise a resolved
 * {@code isCase} call rather than a forced dynamic adapter invocation.
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
     * Prefers a statically resolved {@code isCase} (DGM overload or instance
     * method) so Class / Collection / Pattern / Closure labels stay correct
     * without going through {@code ScriptBytecodeAdapter}.
     */
    @Override
    protected void writeIsCaseComparison(final Expression caseValue,
            final int selectorIndex, final ClassNode selectorType) {
        MethodNode target = resolveIsCaseTarget(caseValue, selectorType);
        if (target == null) {
            super.writeIsCaseComparison(caseValue, selectorIndex, selectorType);
            return;
        }
        OperandStack operandStack = controller.getOperandStack();
        VariableSlotLoader selector = new VariableSlotLoader(selectorType, selectorIndex, operandStack);
        MethodCallExpression call = callX(caseValue, "isCase", args(selector));
        call.setImplicitThis(false);
        call.setMethodTarget(target);
        call.putNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET, target);
        call.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, ClassHelper.boolean_TYPE);
        call.setSourcePosition(caseValue);
        call.visit(controller.getAcg());
        operandStack.doGroovyCast(ClassHelper.boolean_TYPE);
    }

    private MethodNode resolveIsCaseTarget(final Expression caseValue, final ClassNode selectorType) {
        ClassNode caseType = controller.getTypeChooser().resolveType(caseValue, controller.getClassNode());
        ClassNode switchArg = ClassHelper.isPrimitiveType(selectorType)
                ? ClassHelper.getWrapper(selectorType) : selectorType;
        MethodNode instance = chooseInstanceIsCase(caseType, switchArg);
        if (instance != null) {
            return instance;
        }
        ClassLoader loader = controller.getSourceUnit().getClassLoader();
        List<MethodNode> methods = findDGMMethodsByNameAndArguments(
                loader, caseType, "isCase", new ClassNode[]{switchArg});
        if (methods.size() != 1) {
            return null;
        }
        MethodNode dgm = methods.get(0);
        // Object.equals-style isCase is not a substitute for runtime dispatch
        if (isObjectObjectIsCase(dgm)) {
            return null;
        }
        return dgm;
    }

    private static MethodNode chooseInstanceIsCase(final ClassNode caseType, final ClassNode switchArg) {
        if (caseType == null) return null;
        List<MethodNode> candidates = new ArrayList<>();
        for (MethodNode method : caseType.getMethods("isCase")) {
            if (!method.isStatic() && method.getParameters().length == 1
                    && !ClassHelper.isObjectType(method.getDeclaringClass())) {
                candidates.add(method);
            }
        }
        List<MethodNode> best = chooseBestMethod(caseType, candidates, switchArg);
        return best.size() == 1 ? best.get(0) : null;
    }

    private static boolean isObjectObjectIsCase(final MethodNode method) {
        return ClassHelper.isObjectType(method.getDeclaringClass());
    }

    //--------------------------------------------------------------------------
    // tableswitch / lookupswitch

    private boolean writeIntSwitch(final SwitchExpression expression,
            final int selectorIndex, final ClassNode selectorType) {
        boolean primitive = isIntegralType(selectorType);
        if (!primitive && !isIntegralWrapper(selectorType)) return false;

        ArmGroup<Integer> group = groupArms(expression.getCaseStatements(),
                cs -> intConstant(cs.getExpression()));
        if (group.error) return true;
        if (group.dispatch == null) return false;
        ArmDispatch<Integer> dispatch = group.dispatch;

        MethodVisitor mv = controller.getMethodVisitor();
        OperandStack operandStack = controller.getOperandStack();
        CompileStack compileStack = controller.getCompileStack();
        Label defaultTarget = new Label();

        int intSelector = selectorIndex;
        if (!primitive) {
            operandStack.load(selectorType, selectorIndex);
            operandStack.remove(1);
            mv.visitJumpInsn(IFNULL, defaultTarget);
            operandStack.load(selectorType, selectorIndex);
            operandStack.doGroovyCast(ClassHelper.int_TYPE);
            intSelector = compileStack.defineTemporaryVariable("$switchInt", ClassHelper.int_TYPE, true);
        }

        emitIntSwitch(mv, dispatch.sortedIntKeys(), dispatch.sortedIntTargets(), defaultTarget, intSelector);
        emitArmCode(expression.getCaseStatements(), dispatch.targetsByArm);
        mv.visitLabel(defaultTarget);
        writeDefaultOrThrow(expression, selectorIndex, selectorType, false);

        if (!primitive) {
            compileStack.removeVar(intSelector);
        }
        return true;
    }

    private static void emitIntSwitch(final MethodVisitor mv, final int[] keys,
            final Label[] targets, final Label defaultTarget, final int intSelector) {
        mv.visitVarInsn(ILOAD, intSelector);
        int min = keys[0];
        int max = keys[keys.length - 1];
        long span = (long) max - (long) min + 1L;
        // same size heuristic javac uses: tableswitch if it is no larger than lookupswitch
        long tableSize = 12L + 4L * span;
        long lookupSize = 8L + 8L * keys.length;
        if (tableSize <= lookupSize) {
            Label[] table = new Label[(int) span];
            java.util.Arrays.fill(table, defaultTarget);
            for (int i = 0; i < keys.length; i += 1) {
                table[keys[i] - min] = targets[i];
            }
            mv.visitTableSwitchInsn(min, max, defaultTarget, table);
        } else {
            mv.visitLookupSwitchInsn(defaultTarget, keys, targets);
        }
    }

    private boolean writeStringSwitch(final SwitchExpression expression,
            final int selectorIndex, final ClassNode selectorType) {
        if (!ClassHelper.isStringType(selectorType)) {
            return false;
        }

        ArmGroup<String> group = groupArms(expression.getCaseStatements(),
                cs -> stringConstant(cs.getExpression()));
        if (group.error) return true;
        if (group.dispatch == null) return false;
        ArmDispatch<String> dispatch = group.dispatch;

        MethodVisitor mv = controller.getMethodVisitor();
        CompileStack compileStack = controller.getCompileStack();
        Label defaultTarget = new Label();
        int caseIndexLocal = compileStack.defineTemporaryVariable("$switchCase", ClassHelper.int_TYPE, false);

        OperandStack operandStack = controller.getOperandStack();
        operandStack.load(selectorType, selectorIndex);
        operandStack.remove(1);
        mv.visitJumpInsn(IFNULL, defaultTarget);

        emitStringIndexDispatch(selectorIndex, caseIndexLocal, dispatch.keys, dispatch.targets, defaultTarget);
        emitArmCode(expression.getCaseStatements(), dispatch.targetsByArm);
        mv.visitLabel(defaultTarget);
        writeDefaultOrThrow(expression, selectorIndex, selectorType, false);

        compileStack.removeVar(caseIndexLocal);
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

        ArmGroup<String> group = groupArms(expression.getCaseStatements(),
                cs -> enumConstantName(cs.getExpression(), enumType));
        if (group.error) return true;
        if (group.dispatch == null) return false;
        ArmDispatch<String> dispatch = group.dispatch;

        MethodVisitor mv = controller.getMethodVisitor();
        OperandStack operandStack = controller.getOperandStack();
        CompileStack compileStack = controller.getCompileStack();

        boolean hasDefault = expression.getDefaultStatement() != null && !expression.getDefaultStatement().isEmpty();
        boolean complete = !hasDefault && dispatch.keys.size() == enumConstantCount(enumType);

        Label defaultTarget = new Label();
        // a null selector matches no constant label; with no default it must
        // throw ISE, never the complete-enum ICCE
        Label nullTarget = complete ? new Label() : defaultTarget;
        operandStack.load(selectorType, selectorIndex);
        operandStack.remove(1);
        mv.visitJumpInsn(IFNULL, nullTarget);

        operandStack.load(selectorType, selectorIndex);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Enum", "name", "()Ljava/lang/String;", false);
        operandStack.replace(ClassHelper.STRING_TYPE);
        int nameLocal = compileStack.defineTemporaryVariable("$switchEnumName", ClassHelper.STRING_TYPE, true);
        int caseIndexLocal = compileStack.defineTemporaryVariable("$switchCase", ClassHelper.int_TYPE, false);

        emitStringIndexDispatch(nameLocal, caseIndexLocal, dispatch.keys, dispatch.targets, defaultTarget);
        emitArmCode(expression.getCaseStatements(), dispatch.targetsByArm);

        mv.visitLabel(defaultTarget);
        writeDefaultOrThrow(expression, selectorIndex, selectorType, complete);
        if (nullTarget != defaultTarget) {
            mv.visitLabel(nullTarget);
            throwUnmatchedSelector(selectorIndex, selectorType);
        }

        compileStack.removeVar(caseIndexLocal);
        compileStack.removeVar(nameLocal);
        return true;
    }

    /**
     * Emits the Java 7 two-switch string dispatch: {@code lookupswitch} on
     * {@code hashCode()} plus {@code equals} to pick a stable case index, then
     * {@code tableswitch} on that index onto the arm targets.
     */
    private void emitStringIndexDispatch(final int stringLocal, final int caseIndexLocal,
            final List<String> ordered, final List<Label> targets, final Label defaultTarget) {
        Map<Integer, List<Integer>> hashToIndexes = new TreeMap<>();
        for (int i = 0; i < ordered.size(); i += 1) {
            hashToIndexes.computeIfAbsent(ordered.get(i).hashCode(), h -> new ArrayList<>()).add(i);
        }
        MethodVisitor mv = controller.getMethodVisitor();
        OperandStack operandStack = controller.getOperandStack();
        Label secondSwitch = new Label();

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
                mv.visitLdcInsn(ordered.get(index));
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
                operandStack.replace(ClassHelper.boolean_TYPE);
                Label next = operandStack.jump(IFEQ);
                mv.visitLdcInsn(index);
                mv.visitVarInsn(ISTORE, caseIndexLocal);
                mv.visitJumpInsn(GOTO, secondSwitch);
                mv.visitLabel(next);
            }
            mv.visitJumpInsn(GOTO, defaultTarget);
        }

        mv.visitLabel(secondSwitch);
        mv.visitVarInsn(ILOAD, caseIndexLocal);
        mv.visitTableSwitchInsn(0, ordered.size() - 1, defaultTarget, targets.toArray(Label[]::new));
    }

    /**
     * Walks arms from the last completing one backward so empty colon cases
     * share the following arm's jump target. A trailing empty group or a
     * duplicate constant is a compilation error, not a silent isCase fallback.
     */
    private <K> ArmGroup<K> groupArms(final List<CaseStatement> caseStatements,
            final Function<CaseStatement, K> keyFn) {
        int n = caseStatements.size();
        if (n == 0) return ArmGroup.skip();

        List<K> rawKeys = new ArrayList<>(n);
        for (CaseStatement caseStatement : caseStatements) {
            K key = keyFn.apply(caseStatement);
            if (key == null) return ArmGroup.skip();
            rawKeys.add(key);
        }

        Label[] targetsByArm = new Label[n];
        Label current = null;
        for (int i = n - 1; i >= 0; i -= 1) {
            CaseStatement caseStatement = caseStatements.get(i);
            if (!caseStatement.getCode().isEmpty() || caseStatement.isArrow()) {
                current = new Label();
            }
            if (current == null) {
                addError("the switch expression case does not complete with yield or throw", caseStatement);
                return ArmGroup.error();
            }
            targetsByArm[i] = current;
        }

        List<K> uniqueKeys = new ArrayList<>(n);
        List<Label> uniqueTargets = new ArrayList<>(n);
        Map<K, Label> keyToTarget = new LinkedHashMap<>();
        for (int i = 0; i < n; i += 1) {
            K key = rawKeys.get(i);
            if (keyToTarget.put(key, targetsByArm[i]) != null) {
                addError("Duplicate case label: " + key, caseStatements.get(i));
                return ArmGroup.error();
            }
            uniqueKeys.add(key);
            uniqueTargets.add(targetsByArm[i]);
        }
        return ArmGroup.of(new ArmDispatch<>(uniqueKeys, uniqueTargets, targetsByArm, keyToTarget));
    }

    private void emitArmCode(final List<CaseStatement> caseStatements, final Label[] targetsByArm) {
        AsmClassGenerator acg = controller.getAcg();
        MethodVisitor mv = controller.getMethodVisitor();
        Set<Label> emitted = new HashSet<>();
        for (int i = 0; i < caseStatements.size(); i += 1) {
            CaseStatement caseStatement = caseStatements.get(i);
            if (caseStatement.getCode().isEmpty() && !caseStatement.isArrow()) {
                continue;
            }
            Label target = targetsByArm[i];
            if (emitted.add(target)) {
                mv.visitLabel(target);
            }
            caseStatement.getCode().visit(acg);
        }
    }

    private void addError(final String message, final CaseStatement caseStatement) {
        controller.getSourceUnit().addError(new SyntaxException(message, caseStatement));
    }

    private static final class ArmGroup<K> {
        final ArmDispatch<K> dispatch;
        final boolean error;

        private ArmGroup(final ArmDispatch<K> dispatch, final boolean error) {
            this.dispatch = dispatch;
            this.error = error;
        }

        static <K> ArmGroup<K> skip() {
            return new ArmGroup<>(null, false);
        }

        static <K> ArmGroup<K> error() {
            return new ArmGroup<>(null, true);
        }

        static <K> ArmGroup<K> of(final ArmDispatch<K> dispatch) {
            return new ArmGroup<>(dispatch, false);
        }
    }

    private record ArmDispatch<K>(List<K> keys, List<Label> targets, Label[] targetsByArm, Map<K, Label> keyToTarget) {
        int[] sortedIntKeys() {
            return keyToTarget.keySet().stream().mapToInt(k -> (Integer) k).sorted().toArray();
        }

        Label[] sortedIntTargets() {
            TreeMap<Integer, Label> sorted = new TreeMap<>();
            for (Map.Entry<K, Label> entry : keyToTarget.entrySet()) {
                sorted.put((Integer) entry.getKey(), entry.getValue());
            }
            return sorted.values().toArray(Label[]::new);
        }
    }
}
