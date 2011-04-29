/*
 * Copyright 2003-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.classgen.asm;

import static org.codehaus.groovy.syntax.Types.*;
import static org.objectweb.asm.Opcodes.*;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.runtime.BytecodeInterface8;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

/**
 * @author <a href="mailto:blackdrag@gmx.org">Jochen "blackdrag" Theodorou</a>
 */
public class BinaryLongExpressionHelper implements BinaryExpressionWriter {

    private WriterController controller;
    public BinaryLongExpressionHelper(WriterController controller) {
        this.controller = controller;
    }
    
    private static final int[] stdCompareCodes = {
        IFEQ,      // COMPARE_NOT_EQUAL            120
        IFNE,      // COMPARE_IDENTICAL            121 
        IFEQ,      // COMPARE_NOT_IDENTICAL        122
        IFNE,      // COMPARE_EQUAL                123
        IFGE,      // COMPARE_LESS_THAN            124
        IFGT,      // COMPARE_LESS_THAN_EQUAL      125
        IFLE,      // COMPARE_GREATER_THAN         126
        IFLT,      // COMPARE_GREATER_THAN_EQUAL   127
    };
    
    private boolean writeStdCompare(int type, boolean simulate) {
        type = type-COMPARE_NOT_EQUAL;
        // look if really compare
        if (type<0||type>7) return false;

        if (!simulate) {
            MethodVisitor mv = controller.getMethodVisitor();
            OperandStack operandStack = controller.getOperandStack();
            // operands are on the stack already
            int bytecode = stdCompareCodes[type];
            mv.visitInsn(LCMP);
            Label l1 = new Label();
            mv.visitJumpInsn(bytecode,l1);
            mv.visitInsn(ICONST_1);
            Label l2 = new Label();;
            mv.visitJumpInsn(GOTO, l2);
            mv.visitLabel(l1);
            mv.visitInsn(ICONST_0);
            mv.visitLabel(l2);
            operandStack.replace(ClassHelper.boolean_TYPE, 2);
        }
        return true;
    }
    
    private boolean writeSpaceship(int type, boolean simulate) {
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
            // duplicate long arguments
            mv.visitInsn(DUP2_X1);
            mv.visitInsn(POP2);
            mv.visitInsn(DUP2_X1);
            mv.visitInsn(DUP2_X1);
            mv.visitInsn(POP2);
            mv.visitInsn(DUP2_X1);
            
            Label l1 = new Label();
            mv.visitInsn(LCMP);
            mv.visitJumpInsn(IFGE,l1);
            // no jump, so -1, need to pop off surplus LL
            mv.visitInsn(POP2);
            mv.visitInsn(POP2);
            mv.visitInsn(ICONST_M1);
            Label l2 = new Label();;
            mv.visitJumpInsn(GOTO, l2);
            
            mv.visitLabel(l1);
            Label l3 = new Label();
            mv.visitInsn(LCMP);
            mv.visitJumpInsn(IFNE,l3);
            mv.visitInsn(ICONST_0);
            mv.visitJumpInsn(GOTO,l2);
            
            mv.visitLabel(l3);
            mv.visitInsn(ICONST_1);
            
            controller.getOperandStack().replace(ClassHelper.int_TYPE, 2);
        }
        return true;
    }
    
    private static final int[] stdOperations = {
        LADD,           //  PLUS        200
        LSUB,           //  MINUS       201
        LMUL,           //  MULTIPLY    202
        0,              //  DIV, (203) but we don't want that one
        LDIV,           //  INTDIV      204
        LREM,           //  MOD         203
    };
    
    private boolean writeStdOperators(int type, boolean simulate) {
        type = type-PLUS;
        if (type<0 || type>5 || type == 3 /*DIV*/) return false;
        
        if (!simulate) {
            int bytecode = stdOperations[type];
            controller.getMethodVisitor().visitInsn(bytecode);
            controller.getOperandStack().replace(ClassHelper.long_TYPE, 2);
        }
        return true;
    }
    
    private static final int[] bitOp = {
        LOR,            //  BITWISE_OR / PIPE   340
        LAND,           //  BITWISE_AND         341
        LXOR,           //  BIWISE_XOR          342
    };
    
    private boolean writeBitwiseOp(int type, boolean simulate) {
        type = type-BITWISE_OR;
        if (type<0 || type>2) return false;

        if (!simulate) {
            int bytecode = bitOp[type];
            controller.getMethodVisitor().visitInsn(bytecode);
            controller.getOperandStack().replace(ClassHelper.long_TYPE, 2);
        }
        return true;
    }
    
    private static final int[] shiftOp = {
        LSHL,           // LEFT_SHIFT               280
        LSHR,           // RIGHT_SHIFT              281
        LUSHR           // RIGHT_SHIFT_UNSIGNED     282
    };
    
    private boolean writeShiftOp(int type, boolean simulate) {
        type = type - LEFT_SHIFT;
        if (type < 0 || type > 2) return false;

        if (!simulate) {
            int bytecode = shiftOp[type];
            controller.getMethodVisitor().visitInsn(bytecode);
            controller.getOperandStack().replace(ClassHelper.long_TYPE, 2);
        }
        return true;
    }
    
    @Override
    public boolean write(int operation, boolean simulate) {
        return  writeStdCompare(operation, simulate)         ||
                writeSpaceship(operation, simulate)          ||
                writeStdOperators(operation, simulate)       ||
                writeBitwiseOp(operation, simulate)          ||
                writeShiftOp(operation, simulate);
    }

    private static final MethodCaller 
        longArrayGet = MethodCaller.newStatic(BytecodeInterface8.class, "lArrayGet"),
        longArraySet = MethodCaller.newStatic(BytecodeInterface8.class, "lArraySet");
    
    @Override
    public boolean arrayGet(int operation, boolean simulate) {
        if (operation!=LEFT_SQUARE_BRACKET) return false;
        
        if (!simulate) {
            longArrayGet.call(controller.getMethodVisitor());
            controller.getOperandStack().replace(ClassHelper.int_TYPE,2);
        }
        return true;
    }

    @Override
    public boolean arraySet(boolean simulate) {        
        if (!simulate) {
            longArraySet.call(controller.getMethodVisitor());
        }
        return true;
    }

}
