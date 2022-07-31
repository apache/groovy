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
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
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

import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.implementsInterfaceOrIsSubclassOf;
import static org.objectweb.asm.Opcodes.AALOAD;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARRAYLENGTH;
import static org.objectweb.asm.Opcodes.BALOAD;
import static org.objectweb.asm.Opcodes.CALOAD;
import static org.objectweb.asm.Opcodes.DALOAD;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.FALOAD;
import static org.objectweb.asm.Opcodes.GOTO;
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

    private static final ClassNode ITERABLE_CLASSNODE = ClassHelper.make(Iterable.class);
    private static final ClassNode ENUMERATION_CLASSNODE = ClassHelper.make(Enumeration.class);
    private static final MethodCaller ENUMERATION_NEXT_METHOD = MethodCaller.newInterface(Enumeration.class, "nextElement");
    private static final MethodCaller ENUMERATION_HASMORE_METHOD = MethodCaller.newInterface(Enumeration.class, "hasMoreElements");

    public StaticTypesStatementWriter(final StaticTypesWriterController controller) {
        super(controller);
    }

    @Override
    public void writeBlockStatement(final BlockStatement statement) {
        controller.switchToFastPath();
        super.writeBlockStatement(statement);
        controller.switchToSlowPath();
    }

    //--------------------------------------------------------------------------

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
        Parameter loopVariable = loop.getVariable();
        if (collectionType.isArray() && loopVariable.getType().equals(collectionType.getComponentType())) {
            writeOptimizedForEachLoop(loop, loopVariable, collectionExpression, collectionType);
        } else if (GeneralUtils.isOrImplements(collectionType, ENUMERATION_CLASSNODE)) {
            writeEnumerationBasedForEachLoop(loop, collectionExpression, collectionType);
        } else {
            writeIteratorBasedForEachLoop(loop, collectionExpression, collectionType);
        }
        operandStack.popDownTo(mark);
        compileStack.pop();
    }

    private void writeOptimizedForEachLoop(final ForStatement loop, final Parameter loopVariable, final Expression arrayExpression, final ClassNode arrayType) {
        CompileStack compileStack = controller.getCompileStack();
        OperandStack operandStack = controller.getOperandStack();
        MethodVisitor mv = controller.getMethodVisitor();
        AsmClassGenerator acg = controller.getAcg();

        BytecodeVariable variable = compileStack.defineVariable(loopVariable, arrayType.getComponentType(), false);
        Label continueLabel = compileStack.getContinueLabel();
        Label breakLabel = compileStack.getBreakLabel();

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
        loadFromArray(mv, operandStack, variable, array, loopIdx);

        // $idx += 1
        mv.visitIincInsn(loopIdx, 1);

        // loop body
        loop.getLoopBlock().visit(acg);

        mv.visitJumpInsn(GOTO, continueLabel);

        mv.visitLabel(breakLabel);

        compileStack.removeVar(loopIdx);
        compileStack.removeVar(arrayLen);
        compileStack.removeVar(array);
    }

    private static void loadFromArray(final MethodVisitor mv, final OperandStack os, final BytecodeVariable variable, final int array, final int index) {
        mv.visitVarInsn(ALOAD, array);
        mv.visitVarInsn(ILOAD, index);
        ClassNode varType = variable.getType();
        if (ClassHelper.isPrimitiveType(varType)) {
            if (varType.equals(ClassHelper.int_TYPE)) {
                mv.visitInsn(IALOAD);
            } else if (varType.equals(ClassHelper.long_TYPE)) {
                mv.visitInsn(LALOAD);
            } else if (varType.equals(ClassHelper.byte_TYPE) || varType.equals(ClassHelper.boolean_TYPE)) {
                mv.visitInsn(BALOAD);
            } else if (varType.equals(ClassHelper.char_TYPE)) {
                mv.visitInsn(CALOAD);
            } else if (varType.equals(ClassHelper.short_TYPE)) {
                mv.visitInsn(SALOAD);
            } else if (varType.equals(ClassHelper.float_TYPE)) {
                mv.visitInsn(FALOAD);
            } else if (varType.equals(ClassHelper.double_TYPE)) {
                mv.visitInsn(DALOAD);
            }
        } else {
            mv.visitInsn(AALOAD);
        }
        os.push(varType);
        os.storeVar(variable);
    }

    private void writeEnumerationBasedForEachLoop(final ForStatement loop, final Expression collectionExpression, final ClassNode collectionType) {
        CompileStack compileStack = controller.getCompileStack();
        OperandStack operandStack = controller.getOperandStack();
        MethodVisitor mv = controller.getMethodVisitor();

        BytecodeVariable variable = compileStack.defineVariable(loop.getVariable(), false);
        Label continueLabel = compileStack.getContinueLabel();
        Label breakLabel = compileStack.getBreakLabel();

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
        operandStack.storeVar(variable);

        loop.getLoopBlock().visit(controller.getAcg());
        mv.visitJumpInsn(GOTO, continueLabel);

        mv.visitLabel(breakLabel);
    }

    private void writeIteratorBasedForEachLoop(final ForStatement loop, final Expression collectionExpression, final ClassNode collectionType) {
        if (implementsInterfaceOrIsSubclassOf(collectionType, ITERABLE_CLASSNODE)) {
            MethodCallExpression call = GeneralUtils.callX(collectionExpression, "iterator");
            call.setMethodTarget(collectionType.getMethod("iterator",Parameter.EMPTY_ARRAY));
            call.setImplicitThis(false);
            call.setSafe(true);//GROOVY-8643
            call.visit(controller.getAcg());
        } else {
            collectionExpression.visit(controller.getAcg());
            controller.getMethodVisitor().visitMethodInsn(INVOKESTATIC, "org/codehaus/groovy/runtime/DefaultGroovyMethods", "iterator", "(Ljava/lang/Object;)Ljava/util/Iterator;", false);
            controller.getOperandStack().replace(ClassHelper.Iterator_TYPE);
        }
        writeForInLoopControlAndBlock(loop);
    }
}
