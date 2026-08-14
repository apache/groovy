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

import org.codehaus.groovy.GroovyBugError;
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
import org.codehaus.groovy.classgen.asm.sc.StaticTypesTypeChooser;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static org.codehaus.groovy.ast.tools.GeneralUtils.maybeFallsThrough;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;

/**
 * Emits JVM bytecode for a {@link SwitchExpression}. Completing arms leave the
 * result on the operand stack and jump to a shared join point — the same shape
 * javac uses for JEP 361 switch expressions.
 * <p>
 * When the selector type and case labels permit it, the writer emits
 * {@code tableswitch} / {@code lookupswitch} (with the Java 7 two-switch form
 * for String selectors, reused over {@code Enum.name()} for enum selectors so
 * separately recompiled enums cannot retarget arms). A null selector matches
 * no constant label and takes the default path. Otherwise it falls back to
 * Groovy's sequential {@code isCase} tests so Class, regex, Collection and
 * Closure cases keep working.
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

        WriterController effective = effectiveController();
        CompileStack compileStack = effective.getCompileStack();
        OperandStack operandStack = effective.getOperandStack();
        MethodVisitor mv = effective.getMethodVisitor();

        ClassNode resultType = resolveResultType(expression, effective);
        Label endLabel = compileStack.pushSwitchExpression(resultType);

        expression.getExpression().visit(acg);
        ClassNode selectorType = operandStack.getTopOperand();
        ClassNode storedSelectorType = ClassHelper.isPrimitiveType(selectorType)
                ? ClassHelper.getWrapper(selectorType)
                : selectorType;
        operandStack.box();
        int selectorIndex = compileStack.defineTemporaryVariable("switch", storedSelectorType, true);

        boolean emitted = writeIntSwitch(expression, selectorIndex, storedSelectorType, resultType, endLabel)
                || writeStringSwitch(expression, selectorIndex, storedSelectorType, resultType, endLabel)
                || writeEnumSwitch(expression, selectorIndex, storedSelectorType, resultType, endLabel);
        if (!emitted) {
            writeIsCaseSwitch(expression, selectorIndex, endLabel);
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
        WriterController effective = effectiveController();
        CompileStack compileStack = effective.getCompileStack();
        CompileStack.SwitchExpressionContext context = compileStack.getSwitchExpressionContext();
        if (context == null) {
            throw new GroovyBugError("yield outside of a switch expression");
        }

        effective.getAcg().onLineNumber(statement, "visitYieldStatement");
        OperandStack operandStack = effective.getOperandStack();
        MethodVisitor mv = effective.getMethodVisitor();
        statement.getExpression().visit(effective.getAcg());
        operandStack.doGroovyCast(context.resultType);
        if (compileStack.hasBlockRecorder()) {
            // stash the result so intervening finally / synchronized can run
            // without seeing it on the operand stack (same shape as return)
            int rv = compileStack.defineTemporaryVariable("$yield", context.resultType, true);
            compileStack.applyFinallyBlocks(context.endLabel, true);
            BytecodeHelper.load(mv, context.resultType, rv);
            compileStack.removeVar(rv);
        } else {
            operandStack.remove(1);
        }
        mv.visitJumpInsn(GOTO, context.endLabel);
    }

    //--------------------------------------------------------------------------

    private void writeIsCaseSwitch(final SwitchExpression expression, final int selectorIndex, final Label endLabel) {
        WriterController effective = effectiveController();

        List<CaseStatement> caseStatements = expression.getCaseStatements();
        int caseCount = caseStatements.size();
        Label[] bodyLabels = new Label[caseCount + 1];
        for (int i = 0; i < caseCount; i += 1) {
            bodyLabels[i] = new Label();
        }

        for (int i = 0; i < caseCount; i += 1) {
            writeIsCaseArm(caseStatements.get(i), selectorIndex, bodyLabels[i], bodyLabels[i + 1]);
        }

        writeDefaultOrThrow(expression, selectorIndex, false);
    }

    private void writeIsCaseArm(final CaseStatement caseStatement, final int selectorIndex,
            final Label thisLabel, final Label nextLabel) {
        WriterController effective = effectiveController();
        MethodVisitor mv = effective.getMethodVisitor();
        OperandStack operandStack = effective.getOperandStack();
        AsmClassGenerator acg = effective.getAcg();

        acg.onLineNumber(caseStatement, "visitCaseStatement");

        mv.visitVarInsn(ALOAD, selectorIndex);
        caseStatement.getExpression().visit(acg);
        operandStack.box();
        effective.getBinaryExpressionHelper().getIsCaseMethod().call(mv);
        operandStack.replace(ClassHelper.boolean_TYPE);

        Label miss = operandStack.jump(IFEQ);

        mv.visitLabel(thisLabel);
        caseStatement.getCode().visit(acg);

        if (nextLabel != null && maybeFallsThrough(caseStatement.getCode())) {
            mv.visitJumpInsn(GOTO, nextLabel);
        }

        mv.visitLabel(miss);
    }

    private void writeDefaultOrThrow(final SwitchExpression expression, final int selectorIndex, final boolean completeEnum) {
        WriterController effective = effectiveController();
        Statement defaultStatement = expression.getDefaultStatement();
        if (defaultStatement != null && !defaultStatement.isEmpty()) {
            defaultStatement.visit(effective.getAcg());
            return;
        }
        if (completeEnum) {
            throwIncompatibleClassChangeError(effective.getMethodVisitor());
        } else {
            throwIllegalState(effective.getMethodVisitor(), selectorIndex);
        }
    }

    private boolean writeIntSwitch(final SwitchExpression expression, final int selectorIndex,
            final ClassNode storedSelectorType, final ClassNode resultType, final Label endLabel) {
        if (!isStaticCompilation()) return false;
        if (!isIntegralType(storedSelectorType) && !isIntegralWrapper(storedSelectorType)) return false;

        List<CaseStatement> caseStatements = expression.getCaseStatements();
        Map<Integer, Label> keyToBody = new TreeMap<>();
        Label currentGroupBody = null;
        for (CaseStatement caseStatement : caseStatements) {
            Integer key = intConstant(caseStatement.getExpression());
            if (key == null) return false;
            if (!caseStatement.getCode().isEmpty() || caseStatement.isArrow()) {
                currentGroupBody = new Label();
            }
            if (currentGroupBody == null) {
                currentGroupBody = new Label();
            }
            if (keyToBody.put(key, currentGroupBody) != null) {
                return false; // duplicate case value
            }
        }
        if (keyToBody.isEmpty()) return false;

        WriterController effective = effectiveController();
        MethodVisitor mv = effective.getMethodVisitor();
        OperandStack operandStack = effective.getOperandStack();
        AsmClassGenerator acg = effective.getAcg();

        Label defaultLabel = new Label();
        // a null selector matches no constant label; it selects the default
        // arm (or the unmatched-selector throw), as in 4.x/5.x and dynamic mode
        mv.visitVarInsn(ALOAD, selectorIndex);
        mv.visitJumpInsn(IFNULL, defaultLabel);

        mv.visitVarInsn(ALOAD, selectorIndex);
        operandStack.push(storedSelectorType);
        operandStack.doGroovyCast(ClassHelper.int_TYPE);
        int intSelector = effective.getCompileStack().defineTemporaryVariable("$switchInt", ClassHelper.int_TYPE, true);

        emitIntSwitch(mv, keyToBody, defaultLabel, intSelector);

        // emit case bodies in source order; several keys may share a label
        Set<Label> emitted = new HashSet<>();
        for (CaseStatement caseStatement : caseStatements) {
            Integer key = intConstant(caseStatement.getExpression());
            Label body = keyToBody.get(key);
            if (emitted.add(body)) {
                mv.visitLabel(body);
            }
            if (!caseStatement.getCode().isEmpty()) {
                caseStatement.getCode().visit(acg);
            }
        }

        mv.visitLabel(defaultLabel);
        writeDefaultOrThrow(expression, selectorIndex, false);

        effective.getCompileStack().removeVar(intSelector);
        return true;
    }

    private static void emitIntSwitch(final MethodVisitor mv, final Map<Integer, Label> keyToBody,
            final Label defaultLabel, final int intSelector) {
        mv.visitVarInsn(ILOAD, intSelector);
        int[] keys = keyToBody.keySet().stream().mapToInt(Integer::intValue).toArray();
        Label[] labels = keyToBody.values().toArray(Label[]::new);
        int min = keys[0];
        int max = keys[keys.length - 1];
        long span = (long) max - (long) min + 1L;
        // same size heuristic javac uses: tableswitch if it is no larger than lookupswitch
        long tableSize = 12L + 4L * span;
        long lookupSize = 8L + 8L * keys.length;
        if (tableSize <= lookupSize) {
            Label[] table = new Label[(int) span];
            java.util.Arrays.fill(table, defaultLabel);
            for (int i = 0; i < keys.length; i += 1) {
                table[keys[i] - min] = labels[i];
            }
            mv.visitTableSwitchInsn(min, max, defaultLabel, table);
        } else {
            mv.visitLookupSwitchInsn(defaultLabel, keys, labels);
        }
    }

    private boolean writeStringSwitch(final SwitchExpression expression, final int selectorIndex,
            final ClassNode storedSelectorType, final ClassNode resultType, final Label endLabel) {
        if (!isStaticCompilation()) return false;
        if (!ClassHelper.isStringType(storedSelectorType)) {
            return false;
        }

        List<CaseStatement> caseStatements = expression.getCaseStatements();
        Map<String, Label> stringToBody = new LinkedHashMap<>();
        Label currentGroupBody = null;
        for (CaseStatement caseStatement : caseStatements) {
            String key = stringConstant(caseStatement.getExpression());
            if (key == null) return false;
            if (!caseStatement.getCode().isEmpty() || caseStatement.isArrow()) {
                currentGroupBody = new Label();
            }
            if (currentGroupBody == null) {
                currentGroupBody = new Label();
            }
            if (stringToBody.put(key, currentGroupBody) != null) {
                return false;
            }
        }
        if (stringToBody.isEmpty()) return false;

        WriterController effective = effectiveController();
        MethodVisitor mv = effective.getMethodVisitor();
        AsmClassGenerator acg = effective.getAcg();

        Label defaultLabel = new Label();
        int caseIndexLocal = effective.getCompileStack().defineTemporaryVariable("$switchCase", ClassHelper.int_TYPE, false);

        // a null selector matches no constant label; it selects the default
        // arm (or the unmatched-selector throw), as in 4.x/5.x and dynamic mode
        mv.visitVarInsn(ALOAD, selectorIndex);
        mv.visitJumpInsn(IFNULL, defaultLabel);

        List<String> ordered = new ArrayList<>(stringToBody.keySet());
        emitStringIndexDispatch(mv, selectorIndex, caseIndexLocal, ordered, stringToBody, defaultLabel);

        Set<Label> emitted = new HashSet<>();
        for (CaseStatement caseStatement : caseStatements) {
            String key = stringConstant(caseStatement.getExpression());
            Label body = stringToBody.get(key);
            if (emitted.add(body)) {
                mv.visitLabel(body);
            }
            if (!caseStatement.getCode().isEmpty()) {
                caseStatement.getCode().visit(acg);
            }
        }

        mv.visitLabel(defaultLabel);
        writeDefaultOrThrow(expression, selectorIndex, false);

        effective.getCompileStack().removeVar(caseIndexLocal);
        return true;
    }

    /**
     * Switches on {@code Enum.name()} through the string-switch machinery.
     * Constant names are stable when a separately compiled enum adds or
     * reorders constants, so arms are never silently retargeted — the same
     * tolerance javac gets from its {@code $SwitchMap} indirection. A constant
     * added after an exhaustive switch was compiled reaches the implicit
     * default and throws {@code IncompatibleClassChangeError}, as in Java.
     */
    private boolean writeEnumSwitch(final SwitchExpression expression, final int selectorIndex,
            final ClassNode storedSelectorType, final ClassNode resultType, final Label endLabel) {
        if (!isStaticCompilation()) return false;
        ClassNode enumType = unwrapEnumType(storedSelectorType);
        if (enumType == null || !enumType.isEnum()) return false;

        List<CaseStatement> caseStatements = expression.getCaseStatements();
        Map<String, Label> nameToBody = new LinkedHashMap<>();
        Label currentGroupBody = null;
        for (CaseStatement caseStatement : caseStatements) {
            String name = enumConstantName(caseStatement.getExpression(), enumType);
            if (name == null) return false;
            if (!caseStatement.getCode().isEmpty() || caseStatement.isArrow()) {
                currentGroupBody = new Label();
            }
            if (currentGroupBody == null) {
                currentGroupBody = new Label();
            }
            if (nameToBody.put(name, currentGroupBody) != null) {
                return false;
            }
        }
        if (nameToBody.isEmpty()) return false;

        WriterController effective = effectiveController();
        MethodVisitor mv = effective.getMethodVisitor();
        AsmClassGenerator acg = effective.getAcg();
        CompileStack compileStack = effective.getCompileStack();

        boolean hasDefault = expression.getDefaultStatement() != null && !expression.getDefaultStatement().isEmpty();
        boolean complete = !hasDefault && nameToBody.size() == enumConstantCount(enumType);

        Label defaultLabel = new Label();
        // a null selector matches no constant label (as in 4.x/5.x and dynamic
        // mode); with no default it must throw ISE, never the complete-enum ICCE
        Label nullLabel = complete ? new Label() : defaultLabel;
        mv.visitVarInsn(ALOAD, selectorIndex);
        mv.visitJumpInsn(IFNULL, nullLabel);

        mv.visitVarInsn(ALOAD, selectorIndex);
        mv.visitTypeInsn(CHECKCAST, "java/lang/Enum");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Enum", "name", "()Ljava/lang/String;", false);
        effective.getOperandStack().push(ClassHelper.STRING_TYPE);
        int nameLocal = compileStack.defineTemporaryVariable("$switchEnumName", ClassHelper.STRING_TYPE, true);
        int caseIndexLocal = compileStack.defineTemporaryVariable("$switchCase", ClassHelper.int_TYPE, false);

        List<String> ordered = new ArrayList<>(nameToBody.keySet());
        emitStringIndexDispatch(mv, nameLocal, caseIndexLocal, ordered, nameToBody, defaultLabel);

        Set<Label> emitted = new HashSet<>();
        for (CaseStatement caseStatement : caseStatements) {
            String name = enumConstantName(caseStatement.getExpression(), enumType);
            Label body = nameToBody.get(name);
            if (emitted.add(body)) {
                mv.visitLabel(body);
            }
            if (!caseStatement.getCode().isEmpty()) {
                caseStatement.getCode().visit(acg);
            }
        }

        mv.visitLabel(defaultLabel);
        writeDefaultOrThrow(expression, selectorIndex, complete);
        if (nullLabel != defaultLabel) {
            mv.visitLabel(nullLabel);
            throwIllegalState(mv, selectorIndex);
        }

        compileStack.removeVar(caseIndexLocal);
        compileStack.removeVar(nameLocal);
        return true;
    }

    /**
     * Emits the Java 7 two-switch string dispatch: {@code lookupswitch} on
     * {@code hashCode()} plus {@code equals} to pick a stable case index, then
     * {@code tableswitch} on that index onto the body labels.
     */
    private static void emitStringIndexDispatch(final MethodVisitor mv, final int stringLocal, final int caseIndexLocal,
            final List<String> ordered, final Map<String, Label> stringToBody, final Label defaultLabel) {
        Map<Integer, List<String>> byHash = new TreeMap<>();
        for (String s : ordered) {
            byHash.computeIfAbsent(s.hashCode(), h -> new ArrayList<>()).add(s);
        }
        Label secondSwitch = new Label();

        mv.visitVarInsn(ALOAD, stringLocal);
        mv.visitTypeInsn(CHECKCAST, "java/lang/String");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "hashCode", "()I", false);

        int[] hashes = byHash.keySet().stream().mapToInt(Integer::intValue).toArray();
        Label[] hashLabels = new Label[hashes.length];
        for (int i = 0; i < hashes.length; i += 1) {
            hashLabels[i] = new Label();
        }
        mv.visitLookupSwitchInsn(defaultLabel, hashes, hashLabels);

        for (int i = 0; i < hashes.length; i += 1) {
            mv.visitLabel(hashLabels[i]);
            List<String> group = byHash.get(hashes[i]);
            for (String s : group) {
                mv.visitVarInsn(ALOAD, stringLocal);
                mv.visitLdcInsn(s);
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
                Label next = new Label();
                mv.visitJumpInsn(IFEQ, next);
                mv.visitLdcInsn(ordered.indexOf(s));
                mv.visitVarInsn(ISTORE, caseIndexLocal);
                mv.visitJumpInsn(GOTO, secondSwitch);
                mv.visitLabel(next);
            }
            mv.visitJumpInsn(GOTO, defaultLabel);
        }

        mv.visitLabel(secondSwitch);
        Label[] bodyByIndex = new Label[ordered.size()];
        for (int i = 0; i < ordered.size(); i += 1) {
            bodyByIndex[i] = stringToBody.get(ordered.get(i));
        }
        mv.visitVarInsn(ILOAD, caseIndexLocal);
        mv.visitTableSwitchInsn(0, ordered.size() - 1, defaultLabel, bodyByIndex);
    }

    private static void throwIllegalState(final MethodVisitor mv, final int selectorIndex) {
        mv.visitTypeInsn(NEW, ISE_INTERNAL_NAME);
        mv.visitInsn(DUP);
        mv.visitTypeInsn(NEW, STRING_BUILDER_INTERNAL_NAME);
        mv.visitInsn(DUP);
        mv.visitLdcInsn("the switch expression does not cover the value ");
        mv.visitMethodInsn(INVOKESPECIAL, STRING_BUILDER_INTERNAL_NAME, "<init>", "(Ljava/lang/String;)V", false);
        mv.visitVarInsn(ALOAD, selectorIndex);
        mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER_INTERNAL_NAME, "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER_INTERNAL_NAME, "toString", "()Ljava/lang/String;", false);
        mv.visitMethodInsn(INVOKESPECIAL, ISE_INTERNAL_NAME, "<init>", "(Ljava/lang/String;)V", false);
        mv.visitInsn(ATHROW);
    }

    private static void throwIncompatibleClassChangeError(final MethodVisitor mv) {
        mv.visitTypeInsn(NEW, ICCE_INTERNAL_NAME);
        mv.visitInsn(DUP);
        mv.visitLdcInsn("enum constant added after this switch expression was compiled");
        mv.visitMethodInsn(INVOKESPECIAL, ICCE_INTERNAL_NAME, "<init>", "(Ljava/lang/String;)V", false);
        mv.visitInsn(ATHROW);
    }

    private ClassNode resolveResultType(final SwitchExpression expression, final WriterController effective) {
        ClassNode type = effective.getTypeChooser().resolveType(expression, effective.getClassNode());
        if (type == null || ClassHelper.isDynamicTyped(type) || ClassHelper.isPrimitiveVoid(type)) {
            type = ClassHelper.OBJECT_TYPE;
        }
        return type;
    }

    private WriterController effectiveController() {
        WriterController fromAcg = controller.getAcg() != null ? controller.getAcg().getController() : null;
        return fromAcg != null ? fromAcg : controller;
    }

    private boolean isStaticCompilation() {
        return effectiveController().getTypeChooser() instanceof StaticTypesTypeChooser;
    }

    private static boolean isIntegralType(final ClassNode type) {
        return ClassHelper.isPrimitiveInt(type) || ClassHelper.isPrimitiveByte(type)
                || ClassHelper.isPrimitiveShort(type) || ClassHelper.isPrimitiveChar(type);
    }

    private static boolean isIntegralWrapper(final ClassNode type) {
        return ClassHelper.isWrapperInteger(type) || ClassHelper.isWrapperByte(type)
                || ClassHelper.isWrapperShort(type) || ClassHelper.isWrapperCharacter(type);
    }

    private static Integer intConstant(final Expression expression) {
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

    private static String stringConstant(final Expression expression) {
        if (expression instanceof ConstantExpression constant && constant.getValue() instanceof String s) {
            return s;
        }
        return null;
    }

    private static ClassNode unwrapEnumType(final ClassNode type) {
        if (type == null) return null;
        if (type.isEnum()) return type;
        return type.redirect().isEnum() ? type.redirect() : null;
    }

    private static int enumConstantCount(final ClassNode enumType) {
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

    private static String enumConstantName(final Expression expression, final ClassNode enumType) {
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
                && constant.getValue() instanceof Enum<?> e
                && e.getDeclaringClass() == enumType.getTypeClass()) {
            return e.name();
        }
        return null;
    }
}
