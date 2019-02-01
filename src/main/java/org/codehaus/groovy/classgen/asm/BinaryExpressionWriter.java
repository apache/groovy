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
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static org.codehaus.groovy.syntax.Types.BITWISE_OR;
import static org.codehaus.groovy.syntax.Types.COMPARE_NOT_EQUAL;
import static org.codehaus.groovy.syntax.Types.COMPARE_TO;
import static org.codehaus.groovy.syntax.Types.LEFT_SHIFT;
import static org.codehaus.groovy.syntax.Types.LEFT_SQUARE_BRACKET;
import static org.codehaus.groovy.syntax.Types.MINUS_MINUS;
import static org.codehaus.groovy.syntax.Types.PLUS;
import static org.codehaus.groovy.syntax.Types.PLUS_PLUS;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.ICONST_M1;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFGE;
import static org.objectweb.asm.Opcodes.IFGT;
import static org.objectweb.asm.Opcodes.IFLE;
import static org.objectweb.asm.Opcodes.IFLT;
import static org.objectweb.asm.Opcodes.IFNE;

/**
 * Base class for writing primitive typed operations
 */
public abstract class BinaryExpressionWriter {
    
    private final WriterController controller;
    private MethodCaller arraySet, arrayGet;

    public BinaryExpressionWriter(WriterController controller, MethodCaller arraySet, MethodCaller arrayGet) {
        this.controller = controller;
        this.arraySet = arraySet;
        this.arrayGet = arrayGet;
    }

    /**
     * return writer controller
     * @since 2.5.0
     */
    public WriterController getController() {
        return controller;
    }
    
    protected static final int[] stdCompareCodes = {
        IFEQ,      // COMPARE_NOT_EQUAL            120
        IFNE,      // COMPARE_IDENTICAL            121 
        IFEQ,      // COMPARE_NOT_IDENTICAL        122
        IFNE,      // COMPARE_EQUAL                123
        IFGE,      // COMPARE_LESS_THAN            124
        IFGT,      // COMPARE_LESS_THAN_EQUAL      125
        IFLE,      // COMPARE_GREATER_THAN         126
        IFLT,      // COMPARE_GREATER_THAN_EQUAL   127
    };
    
    protected abstract int getCompareCode();

    /**
     * writes some int standard operations for compares
     * @param type the token type
     * @return true if a successful std operator write
     */
    protected boolean writeStdCompare(int type, boolean simulate) {
        type = type-COMPARE_NOT_EQUAL;
        // look if really compare
        if (type<0||type>7) return false;

        if (!simulate) {
            MethodVisitor mv = controller.getMethodVisitor();
            OperandStack operandStack = controller.getOperandStack();
            // operands are on the stack already
            int bytecode = stdCompareCodes[type];
            mv.visitInsn(getCompareCode());
            Label l1 = new Label();
            mv.visitJumpInsn(bytecode,l1);
            mv.visitInsn(ICONST_1);
            Label l2 = new Label();
            mv.visitJumpInsn(GOTO, l2);
            mv.visitLabel(l1);
            mv.visitInsn(ICONST_0);
            mv.visitLabel(l2);
            operandStack.replace(ClassHelper.boolean_TYPE, 2);
        }
        return true;
    }
    
    protected abstract void doubleTwoOperands(MethodVisitor mv);
    protected abstract void removeTwoOperands(MethodVisitor mv);
    
    protected boolean writeSpaceship(int type, boolean simulate) {
        if (type != COMPARE_TO) return false;
        /*  
           we will actually do
         
          (x < y) ? -1 : ((x == y) ? 0 : 1)
          which is the essence of what the call with Number would do
          this compiles to something along
          
              <x>
              <y>
              LCMP
              IFGE L1
              ICONST_M1
              GOTO L2
          L1
              <x>
              <y>
              LCMP
              IFNE L3
              ICONST_0
              GOTO L2
          L3
              ICONST_1
          L2
          
          since the operators are already on the stack and we don't want
          to load them again, we will instead duplicate them. This will
          require some pop actions in the branches!
          
              DUP4          (operands: L1L2L1L2)
              LCMP          
              IFGE L1       (operands: L1L2)
              ICONST_M1     (operands: L1L2I)
              GOTO L2
          L1
              -----         (operands: L1L2)
              LCMP
              IFNE L3       (operands: -)
              ICONST_0      (operands: I)
              GOTO L2
          L3
              - jump from L1 branch to here (operands: -)
              ICONST_1      (operands: I)
          L2  
          - if jump from GOTO L2 we have LLI, but need only I
          - if from L3 branch we get only I
          
          this means we have to pop of LL before loading -1
          
          since there is no DUP4 we have to do this:
            DUP2_X1
            POP2
            DUP2_X1
            DUP2_X1
            POP2
            DUP2_X1          
        */
        if (!simulate) {
            MethodVisitor mv = controller.getMethodVisitor();
            // duplicate arguments
            doubleTwoOperands(mv);
            
            Label l1 = new Label();
            mv.visitInsn(getCompareCode());
            mv.visitJumpInsn(IFGE,l1);
            // no jump, so -1, need to pop off surplus LL
            removeTwoOperands(mv);
            mv.visitInsn(ICONST_M1);
            Label l2 = new Label();
            mv.visitJumpInsn(GOTO, l2);
            
            mv.visitLabel(l1);
            Label l3 = new Label();
            mv.visitInsn(getCompareCode());
            mv.visitJumpInsn(IFNE,l3);
            mv.visitInsn(ICONST_0);
            mv.visitJumpInsn(GOTO,l2);
            
            mv.visitLabel(l3);
            mv.visitInsn(ICONST_1);
            
            controller.getOperandStack().replace(ClassHelper.int_TYPE, 2);
        }
        return true;
    }

    protected abstract ClassNode getNormalOpResultType();
    protected abstract int getStandardOperationBytecode(int type);
    
    protected boolean writeStdOperators(int type, boolean simulate) {
        type = type-PLUS;
        if (type<0 || type>5 || type == 3 /*DIV*/) return false;
        
        if (!simulate) {
            int bytecode = getStandardOperationBytecode(type);
            controller.getMethodVisitor().visitInsn(bytecode);
            controller.getOperandStack().replace(getNormalOpResultType(), 2);
        }
        return true;
    }
    
    protected boolean writeDivision(boolean simulate) {
        if (!supportsDivision()) return false;
        if (!simulate) {
            int bytecode = getStandardOperationBytecode(3 /*DIV*/);
            controller.getMethodVisitor().visitInsn(bytecode);
            controller.getOperandStack().replace(getDevisionOpResultType(), 2);
        }
        return true;
    }
    
    protected boolean supportsDivision() {
        return false;
    }

    protected abstract ClassNode getDevisionOpResultType();

    protected abstract int getBitwiseOperationBytecode(int type);
    
    /**
     * writes some the bitwise operations. type is one of BITWISE_OR, 
     * BITWISE_AND, BITWISE_XOR
     * @param type the token type
     * @return true if a successful bitwise operation write
     */
    protected boolean writeBitwiseOp(int type, boolean simulate) {
        type = type-BITWISE_OR;
        if (type<0 || type>2) return false;

        if (!simulate) {
            int bytecode = getBitwiseOperationBytecode(type);
            controller.getMethodVisitor().visitInsn(bytecode);
            controller.getOperandStack().replace(getNormalOpResultType(), 2);
        }
        return true;
    }
    
    protected abstract int getShiftOperationBytecode(int type);
    
    /**
     * Write shifting operations.
     * Type is one of LEFT_SHIFT, RIGHT_SHIFT, or RIGHT_SHIFT_UNSIGNED
     *
     * @param type the token type
     * @return true on a successful shift operation write
     */
    protected boolean writeShiftOp(int type, boolean simulate) {
        type = type - LEFT_SHIFT;
        if (type < 0 || type > 2) return false;

        if (!simulate) {
            int bytecode = getShiftOperationBytecode(type);
            controller.getMethodVisitor().visitInsn(bytecode);
            controller.getOperandStack().replace(getNormalOpResultType(), 2);
        }
        return true;
    }

    public boolean write(int operation, boolean simulate) {
        return  writeStdCompare(operation, simulate)         ||
                writeSpaceship(operation, simulate)          ||
                writeStdOperators(operation, simulate)       ||
                writeBitwiseOp(operation, simulate)          ||
                writeShiftOp(operation, simulate);
    }
    
    protected MethodCaller getArrayGetCaller() {
        return arrayGet;
    }

    protected ClassNode getArrayGetResultType(){
        return getNormalOpResultType();
    }
    
    protected MethodCaller getArraySetCaller() {
        return arraySet;
    }

    public void setArraySetAndGet(MethodCaller arraySet, MethodCaller arrayGet) {
        this.arraySet = arraySet;
        this.arrayGet = arrayGet;
    }
    
    public boolean arrayGet(int operation, boolean simulate) {
        if (operation!=LEFT_SQUARE_BRACKET) return false;
        
        if (!simulate) {
            getArrayGetCaller().call(controller.getMethodVisitor());
        }
        return true;
    }

    public boolean arraySet(boolean simulate) {        
        if (!simulate) {
            getArraySetCaller().call(controller.getMethodVisitor());
        }
        return true;
    }

    public boolean writePostOrPrefixMethod(int operation, boolean simulate) {
        if (operation!=PLUS_PLUS && operation!=MINUS_MINUS) return false;
        if (!simulate) {
            MethodVisitor mv = controller.getMethodVisitor();
            if (operation==PLUS_PLUS) {
                writePlusPlus(mv);
            } else {
                writeMinusMinus(mv);
            }
        }
        return true;
    }

    protected abstract void writePlusPlus(MethodVisitor mv);
    protected abstract void writeMinusMinus(MethodVisitor mv);
}
