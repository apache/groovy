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
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.CaseStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.ast.tools.GeneralUtils;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.classgen.asm.BytecodeVariable;
import org.codehaus.groovy.classgen.asm.CompileStack;
import org.codehaus.groovy.classgen.asm.MethodCaller;
import org.codehaus.groovy.classgen.asm.OperandStack;
import org.codehaus.groovy.classgen.asm.StatementWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

import static org.apache.groovy.ast.tools.SwitchExpressionUtils.enumConstantName;
import static org.apache.groovy.ast.tools.SwitchExpressionUtils.intConstant;
import static org.apache.groovy.ast.tools.SwitchExpressionUtils.isIntegralType;
import static org.apache.groovy.ast.tools.SwitchExpressionUtils.isOptimizedIntSwitch;
import static org.apache.groovy.ast.tools.SwitchExpressionUtils.isOptimizedStringSwitch;
import static org.apache.groovy.ast.tools.SwitchExpressionUtils.stringConstant;
import static org.apache.groovy.ast.tools.SwitchExpressionUtils.unwrapEnumType;
import static org.codehaus.groovy.ast.tools.GeneralUtils.maybeFallsThrough;
import static org.objectweb.asm.Opcodes.AALOAD;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARRAYLENGTH;
import static org.objectweb.asm.Opcodes.BALOAD;
import static org.objectweb.asm.Opcodes.CALOAD;
import static org.objectweb.asm.Opcodes.DALOAD;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.FALOAD;
import static org.objectweb.asm.Opcodes.IALOAD;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.IF_ICMPGE;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.LALOAD;
import static org.objectweb.asm.Opcodes.SALOAD;

/**
 * A class to write out the optimized statements.
 */
public class StaticTypesStatementWriter extends StatementWriter {

    private static final ClassNode ENUMERATION_CLASSNODE = ClassHelper.make(Enumeration.class);
    private static final MethodCaller ENUMERATION_NEXT_METHOD = MethodCaller.newInterface(Enumeration.class, "nextElement");
    private static final MethodCaller ENUMERATION_HASMORE_METHOD = MethodCaller.newInterface(Enumeration.class, "hasMoreElements");

    /**
     * Creates a statement writer that favors statically typed loop and block generation.
     */
    public StaticTypesStatementWriter(final StaticTypesWriterController controller) {
        super(controller);
    }

    /** {@inheritDoc} */
    @Override
    public void writeBlockStatement(final BlockStatement statement) {
        controller.switchToFastPath();
        super.writeBlockStatement(statement);
        controller.switchToSlowPath();
    }

    /**
     * Emits a jump table when the selector and labels are constants of a type
     * {@code javac} would switch on, matching what a statically compiled switch
     * expression already gets (GROOVY-12405). Anything else, including repeated
     * labels, falls back to the sequential {@code isCase} chain.
     */
    @Override
    public void writeSwitch(final SwitchStatement statement) {
        List<CaseStatement> caseStatements = statement.getCaseStatements();
        ClassNode selectorType = controller.getTypeChooser()
                .resolveType(statement.getExpression(), controller.getClassNode());
        if (!SwitchDispatchWriter.isDispatchable(selectorType, caseStatements)) {
            super.writeSwitch(statement);
            return;
        }
        writeDispatchSwitch(statement, caseStatements, selectorType);
    }

    /**
     * The kind of dispatch is chosen once, from the type the type checker
     * inferred, because that is what {@code isDispatchable} agreed to. Code
     * generation can leave something wider on the operand stack, so a
     * reference selector is coerced to the inferred type before it is stored.
     */
    private void writeDispatchSwitch(final SwitchStatement statement, final List<CaseStatement> caseStatements,
            final ClassNode inferredType) {
        AsmClassGenerator acg = controller.getAcg();
        acg.onLineNumber(statement, "visitSwitch");
        writeStatementLabel(statement);

        statement.getExpression().visit(acg);
        OperandStack operandStack = controller.getOperandStack();
        boolean intSwitch = isOptimizedIntSwitch(inferredType, caseStatements);
        ClassNode selectorType;
        if (intSwitch) {
            // unlike the sequential path the selector is not boxed: the jump
            // table wants the primitive, and a wrapper is unboxed below
            selectorType = operandStack.getTopOperand();
        } else {
            operandStack.doGroovyCast(inferredType);
            selectorType = inferredType;
        }

        CompileStack compileStack = controller.getCompileStack();
        // switch does not have a continue label; use enclosing continue label
        Label breakLabel = compileStack.pushSwitch(statement.getStatementLabels());
        int selectorIndex = compileStack.defineTemporaryVariable("switch", selectorType, true);

        SwitchDispatchWriter dispatch = new SwitchDispatchWriter(controller);
        Label defaultTarget = new Label();
        ClassNode enumType = unwrapEnumType(inferredType);
        int scratch = -1;
        List<Label> targets;

        if (intSwitch) {
            SwitchDispatchWriter.ArmGroup<Integer> group = dispatch.groupArms(
                    caseStatements, cs -> intConstant(cs.getExpression()), defaultTarget);
            targets = group.targets;
            int intSelector = selectorIndex;
            if (!isIntegralType(selectorType)) {
                dispatch.jumpIfNull(selectorIndex, selectorType, defaultTarget);
                operandStack.load(selectorType, selectorIndex);
                operandStack.doGroovyCast(ClassHelper.int_TYPE);
                scratch = intSelector = compileStack.defineTemporaryVariable("$switchInt", ClassHelper.int_TYPE, true);
            }
            dispatch.emitIntSwitch(group.keys, targets, defaultTarget, intSelector);
        } else if (isOptimizedStringSwitch(inferredType, caseStatements)) {
            SwitchDispatchWriter.ArmGroup<String> group = dispatch.groupArms(
                    caseStatements, cs -> stringConstant(cs.getExpression()), defaultTarget);
            targets = group.targets;
            dispatch.jumpIfNull(selectorIndex, selectorType, defaultTarget);
            dispatch.emitStringHashDispatch(selectorIndex, group.keys, targets, defaultTarget);
        } else {
            SwitchDispatchWriter.ArmGroup<String> group = dispatch.groupArms(
                    caseStatements, cs -> enumConstantName(cs.getExpression(), enumType), defaultTarget);
            targets = group.targets;
            // a null selector matches no constant label, so it takes the default
            dispatch.jumpIfNull(selectorIndex, selectorType, defaultTarget);
            scratch = dispatch.loadEnumName(selectorIndex, selectorType);
            dispatch.emitStringHashDispatch(scratch, group.keys, targets, defaultTarget);
        }

        // arms are laid out in source order, so one without a jump falls into
        // the next and then into the default, as the sequential path does
        dispatch.emitArmCode(caseStatements, targets);
        controller.getMethodVisitor().visitLabel(defaultTarget);
        statement.getDefaultStatement().visit(acg);

        if (maybeFallsThrough(statement) || statement.getStatementLabels() != null) {
            controller.getMethodVisitor().visitLabel(breakLabel);
        }
        if (scratch != -1) {
            compileStack.removeVar(scratch);
        }
        compileStack.removeVar(selectorIndex);
        compileStack.pop();
    }

    //--------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    protected void writeForInLoop(final ForStatement loop) {
        controller.getAcg().onLineNumber(loop, "visitForLoop");
        writeStatementLabel(loop);

        CompileStack compileStack = controller.getCompileStack();
        OperandStack operandStack = controller.getOperandStack();

        compileStack.pushLoop(loop.getVariableScope(), loop.getStatementLabels());

        // identify type of collection
        Expression collectionExpression = loop.getCollectionExpression();
        ClassNode collectionType = controller.getTypeChooser().resolveType(collectionExpression, controller.getClassNode());

        int mark = operandStack.getStackLength();
        if (collectionType.isArray() && collectionType.getComponentType().equals(loop.getValueVariable().getType())) {
            writeOptimizedForEachLoop(loop, collectionExpression, collectionType);
        } else if (GeneralUtils.isOrImplements(collectionType, ENUMERATION_CLASSNODE)) {
            writeEnumerationBasedForEachLoop(loop, collectionExpression, collectionType);
        } else {
            writeIteratorBasedForEachLoop(loop, collectionExpression, collectionType);
        }
        operandStack.popDownTo(mark);
        compileStack.pop();
    }

    private void writeOptimizedForEachLoop(final ForStatement loop, final Expression arrayExpression, final ClassNode arrayType) {
        CompileStack compileStack = controller.getCompileStack();
        OperandStack operandStack = controller.getOperandStack();
        MethodVisitor mv = controller.getMethodVisitor();
        AsmClassGenerator acg = controller.getAcg();

        BytecodeVariable indexVariable = defineLoopIndexVariable(loop);
        BytecodeVariable valueVariable = compileStack.defineVariable(loop.getValueVariable(), arrayType.getComponentType(), false);
        Label continueLabel = compileStack.getContinueLabel();
        Label breakLabel = compileStack.getBreakLabel();
        boolean bodyMayReachContinue = GeneralUtils.mayReachLoopCondition(loop);

        // load array on stack
        arrayExpression.visit(acg);
        mv.visitInsn(DUP);
        int array = compileStack.defineTemporaryVariable("$arr", arrayType, true);
        mv.visitJumpInsn(IFNULL, breakLabel);

        // $len = array.length
        mv.visitVarInsn(ALOAD, array);
        mv.visitInsn(ARRAYLENGTH);
        operandStack.push(ClassHelper.int_TYPE);
        int arrayLen = compileStack.defineTemporaryVariable("$len", ClassHelper.int_TYPE, true);

        // $idx = 0
        mv.visitInsn(ICONST_0);
        operandStack.push(ClassHelper.int_TYPE);
        int loopIdx = compileStack.defineTemporaryVariable("$idx", ClassHelper.int_TYPE, true);

        mv.visitLabel(continueLabel);
        // $idx<$len?
        mv.visitVarInsn(ILOAD, loopIdx);
        mv.visitVarInsn(ILOAD, arrayLen);
        mv.visitJumpInsn(IF_ICMPGE, breakLabel);

        // get array element
        loadFromArray(mv, operandStack, valueVariable, array, loopIdx);

        // $idx += 1
        mv.visitIincInsn(loopIdx, 1);
        if (indexVariable != null) {
            incrementForLoopIndexVariable(indexVariable);
        }

        // loop body
        loop.getLoopBlock().visit(acg);

        writeLoopBackEdge(continueLabel, bodyMayReachContinue);

        mv.visitLabel(breakLabel);

        compileStack.removeVar(loopIdx);
        compileStack.removeVar(arrayLen);
        compileStack.removeVar(array);
    }

    private void loadFromArray(final MethodVisitor mv, final OperandStack os, final BytecodeVariable variable, final int array, final int index) {
        mv.visitVarInsn(ALOAD, array);
        mv.visitVarInsn(ILOAD, index);
        ClassNode varType = variable.getType();
        if (ClassHelper.isPrimitiveType(varType)) {
            if (ClassHelper.isPrimitiveInt(varType)) {
                mv.visitInsn(IALOAD);
            } else if (ClassHelper.isPrimitiveLong(varType)) {
                mv.visitInsn(LALOAD);
            } else if (ClassHelper.isPrimitiveByte(varType) || ClassHelper.isPrimitiveBoolean(varType)) {
                mv.visitInsn(BALOAD);
            } else if (ClassHelper.isPrimitiveChar(varType)) {
                mv.visitInsn(CALOAD);
            } else if (ClassHelper.isPrimitiveShort(varType)) {
                mv.visitInsn(SALOAD);
            } else if (ClassHelper.isPrimitiveFloat(varType)) {
                mv.visitInsn(FALOAD);
            } else if (ClassHelper.isPrimitiveDouble(varType)) {
                mv.visitInsn(DALOAD);
            }
        } else {
            mv.visitInsn(AALOAD);
        }
        os.push(varType);
        storeForLoopVariable(variable);
    }

    private void writeEnumerationBasedForEachLoop(final ForStatement loop, final Expression collectionExpression, final ClassNode collectionType) {
        CompileStack compileStack = controller.getCompileStack();
        OperandStack operandStack = controller.getOperandStack();
        MethodVisitor mv = controller.getMethodVisitor();

        BytecodeVariable indexVariable = defineLoopIndexVariable(loop);
        BytecodeVariable valueVariable = compileStack.defineVariable(loop.getValueVariable(), false);
        Label continueLabel = compileStack.getContinueLabel();
        Label breakLabel = compileStack.getBreakLabel();
        boolean bodyMayReachContinue = GeneralUtils.mayReachLoopCondition(loop);

        collectionExpression.visit(controller.getAcg());

        int enumeration = compileStack.defineTemporaryVariable("$enum", ENUMERATION_CLASSNODE, true);

        mv.visitVarInsn(ALOAD, enumeration);
        mv.visitJumpInsn(IFNULL, breakLabel);

        mv.visitLabel(continueLabel);

        mv.visitVarInsn(ALOAD, enumeration);
        ENUMERATION_HASMORE_METHOD.call(mv);
        mv.visitJumpInsn(IFEQ, breakLabel); // jump if zero (aka false)

        mv.visitVarInsn(ALOAD, enumeration);
        ENUMERATION_NEXT_METHOD.call(mv);
        operandStack.push(ClassHelper.OBJECT_TYPE);
        storeForLoopVariable(valueVariable);
        if (indexVariable != null) {
            incrementForLoopIndexVariable(indexVariable);
        }

        loop.getLoopBlock().visit(controller.getAcg());
        writeLoopBackEdge(continueLabel, bodyMayReachContinue);

        mv.visitLabel(breakLabel);
    }

    private void writeIteratorBasedForEachLoop(final ForStatement loop, final Expression collectionExpression, final ClassNode collectionType) {
        if (GeneralUtils.isOrImplements(collectionType, ClassHelper.Iterator_TYPE)) {
            collectionExpression.visit(controller.getAcg()); // GROOVY-8487: iterator supplied
        } else {
            // GROOVY-10476: BaseStream provides an iterator() but does not implement Iterable
            MethodNode iterator = collectionType.getMethod("iterator", Parameter.EMPTY_ARRAY);
            if (iterator == null) {
                iterator = GeneralUtils.getInterfacesAndSuperInterfaces(collectionType).stream()
                        .map(in -> in.getMethod("iterator", Parameter.EMPTY_ARRAY))
                        .filter(Objects::nonNull).findFirst().orElse(null);
            }
            if (iterator != null && GeneralUtils.isOrImplements(iterator.getReturnType(), ClassHelper.Iterator_TYPE)) {
                MethodCallExpression call = GeneralUtils.callX(collectionExpression, "iterator");
                call.setImplicitThis(false);
                call.setMethodTarget(iterator);
                call.setSafe(true);//GROOVY-8643
                call.visit(controller.getAcg());
            } else {
                collectionExpression.visit(controller.getAcg());
                controller.getMethodVisitor().visitMethodInsn(INVOKESTATIC, "org/codehaus/groovy/runtime/DefaultGroovyMethods", "iterator", "(Ljava/lang/Object;)Ljava/util/Iterator;", false);
                controller.getOperandStack().replace(ClassHelper.Iterator_TYPE);
            }
        }
        writeForInLoopControlAndBlock(loop);
    }
}
