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
import org.codehaus.groovy.runtime.BytecodeInterface8;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static org.codehaus.groovy.syntax.Types.COMPARE_NOT_EQUAL;
import static org.codehaus.groovy.syntax.Types.COMPARE_TO;
import static org.objectweb.asm.Opcodes.DUP2;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IADD;
import static org.objectweb.asm.Opcodes.IAND;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.ICONST_M1;
import static org.objectweb.asm.Opcodes.IDIV;
import static org.objectweb.asm.Opcodes.IF_ICMPEQ;
import static org.objectweb.asm.Opcodes.IF_ICMPGE;
import static org.objectweb.asm.Opcodes.IF_ICMPGT;
import static org.objectweb.asm.Opcodes.IF_ICMPLE;
import static org.objectweb.asm.Opcodes.IF_ICMPLT;
import static org.objectweb.asm.Opcodes.IF_ICMPNE;
import static org.objectweb.asm.Opcodes.IMUL;
import static org.objectweb.asm.Opcodes.IOR;
import static org.objectweb.asm.Opcodes.IREM;
import static org.objectweb.asm.Opcodes.ISHL;
import static org.objectweb.asm.Opcodes.ISHR;
import static org.objectweb.asm.Opcodes.ISUB;
import static org.objectweb.asm.Opcodes.IUSHR;
import static org.objectweb.asm.Opcodes.IXOR;
import static org.objectweb.asm.Opcodes.POP2;

public class BinaryIntExpressionHelper extends BinaryExpressionWriter {
    
    private static final MethodCaller intArrayGet = MethodCaller.newStatic(BytecodeInterface8.class, "intArrayGet");
    private static final MethodCaller intArraySet = MethodCaller.newStatic(BytecodeInterface8.class, "intArraySet");

    private static final int[] stdCompareCodes = {
        IF_ICMPEQ,      // COMPARE_NOT_EQUAL            120
        IF_ICMPNE,      // COMPARE_IDENTICAL            121 
        IF_ICMPEQ,      // COMPARE_NOT_IDENTICAL        122
        IF_ICMPNE,      // COMPARE_EQUAL                123
        IF_ICMPGE,      // COMPARE_LESS_THAN            124
        IF_ICMPGT,      // COMPARE_LESS_THAN_EQUAL      125
        IF_ICMPLE,      // COMPARE_GREATER_THAN         126
        IF_ICMPLT,      // COMPARE_GREATER_THAN_EQUAL   127
    };
    
    private static final int[] stdOperations = {
        IADD,           //  PLUS        200
        ISUB,           //  MINUS       201
        IMUL,           //  MULTIPLY    202
        IDIV,           //  DIV         203
        IDIV,           //  INTDIV      204
        IREM,           //  MOD         203
    };
    
    private static final int[] bitOp = {
        IOR,            //  BITWISE_OR / PIPE   340
        IAND,           //  BITWISE_AND         341
        IXOR,           //  BIWISE_XOR          342
    };    
    
    /* unhandled types from from org.codehaus.groovy.syntax.Types
    public static final int LOGICAL_OR                  = 162;   // ||
    public static final int LOGICAL_AND                 = 164;   // &&

    public static final int DIVIDE                      = 203;   // /
    public static final int STAR_STAR                   = 206;   // **
    public static final int POWER                       = STAR_STAR;   //
    
    public static final int PLUS_EQUAL                  = 210;   // +=
    public static final int MINUS_EQUAL                 = 211;   // -=
    public static final int MULTIPLY_EQUAL              = 212;   // *=
    public static final int DIVIDE_EQUAL                = 213;   // /=
    public static final int INTDIV_EQUAL                = 214;   // \=
    public static final int MOD_EQUAL                   = 215;   // %=
    public static final int POWER_EQUAL                 = 216;   // **=

    public static final int PLUS_PLUS                   = 250;   // ++
    public static final int PREFIX_PLUS_PLUS            = 251;   // ++
    public static final int POSTFIX_PLUS_PLUS           = 252;   // ++
    public static final int PREFIX_PLUS                 = 253;   // +

    public static final int MINUS_MINUS                 = 260;   // --
    public static final int PREFIX_MINUS_MINUS          = 261;   // --
    public static final int POSTFIX_MINUS_MINUS         = 262;   // --
    public static final int PREFIX_MINUS                = 263;   // - (negation)
*/
    private static final int[] shiftOp = {
        ISHL,           // LEFT_SHIFT               280
        ISHR,           // RIGHT_SHIFT              281
        IUSHR           // RIGHT_SHIFT_UNSIGNED     282
    };

/*
    public static final int LEFT_SHIFT_EQUAL            = 285;   // <<=
    public static final int RIGHT_SHIFT_EQUAL           = 286;   // >>=
    public static final int RIGHT_SHIFT_UNSIGNED_EQUAL  = 287;   // >>>=

    public static final int BITWISE_OR_EQUAL            = 350;   // |=
    public static final int BITWISE_AND_EQUAL           = 351;   // &=
    public static final int BITWISE_XOR_EQUAL           = 352;   // ^=
    public static final int BITWISE_NEGATION            = REGEX_PATTERN;    // ~
    */
    
    public BinaryIntExpressionHelper(WriterController wc) {
        this(wc, intArraySet, intArrayGet);
    }

    /**
     * @since 2.5.0
     */
    public BinaryIntExpressionHelper(WriterController wc, MethodCaller arraySet, MethodCaller arrayGet) {
        super(wc, arraySet, arrayGet);
    }

    
    /**
     * writes a std compare. This involves the tokens IF_ICMPEQ, IF_ICMPNE, 
     * IF_ICMPEQ, IF_ICMPNE, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE and IF_ICMPLT
     * @param type the token type
     * @return true if a successful std compare write
     */
    @Override
    protected boolean writeStdCompare(int type, boolean simulate) {
        type = type-COMPARE_NOT_EQUAL;
        // look if really compare
        if (type<0||type>7) return false;

        if (!simulate) {
            MethodVisitor mv = getController().getMethodVisitor();
            OperandStack operandStack = getController().getOperandStack();
            // operands are on the stack already
            int bytecode = stdCompareCodes[type];
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
    
    /**
     * writes the spaceship operator, type should be COMPARE_TO
     * @param type the token type
     * @return true if a successful spaceship operator write
     */
    @Override
    protected boolean writeSpaceship(int type, boolean simulate) {
        if (type != COMPARE_TO) return false;
        /*  
           we will actually do
         
          (x < y) ? -1 : ((x == y) ? 0 : 1)
          which is the essence of what the call with Integers would do
          this compiles to something along
          
              <x>
              <y>
              IF_ICMPGE L1
              ICONST_M1
              GOTO L2
          L1
              <x>
              <y>
              IF_ICMPNE L3
              ICONST_0
              GOTO L2
          L3
              ICONST_1
          L2
          
          since the operators are already on the stack and we don't want
          to load them again, we will instead duplicate them. This will
          require some pop actions in the branches!
          
              DUP2          (operands: IIII) 
              IF_ICMPGE L1  (operands: II)
              ICONST_M1     (operands: III)
              GOTO L2
          L1
              -----         (operands: II)
              IF_ICMPNE L3  (operands: -)
              ICONST_0      (operands: I)
              GOTO L2
          L3
              - jump from L1 branch to here (operands: -)
              ICONST_1      (operands: I)
          L2  
          - if jump from GOTO L2 we have III, but need only I
          - if from L3 branch we get only I
          
          this means we have to pop of II before loading -1
          
        */
        if (!simulate) {
            MethodVisitor mv = getController().getMethodVisitor();
            // duplicate int arguments
            mv.visitInsn(DUP2);
            
            Label l1 = new Label();
            mv.visitJumpInsn(IF_ICMPGE,l1);
            // no jump, so -1, need to pop off surplus II
            mv.visitInsn(POP2);
            mv.visitInsn(ICONST_M1);
            Label l2 = new Label();
            mv.visitJumpInsn(GOTO, l2);
            
            mv.visitLabel(l1);
            Label l3 = new Label();
            mv.visitJumpInsn(IF_ICMPNE,l3);
            mv.visitInsn(ICONST_0);
            mv.visitJumpInsn(GOTO,l2);
            
            mv.visitLabel(l3);
            mv.visitInsn(ICONST_1);

            getController().getOperandStack().replace(ClassHelper.int_TYPE, 2);
        }
        return true;
    }

    @Override
    protected void doubleTwoOperands(MethodVisitor mv) {
        mv.visitInsn(DUP2);
    }

    @Override
    protected int getBitwiseOperationBytecode(int type) {
        return bitOp[type];
    }

    @Override
    protected int getCompareCode() {
        return -1;
    }

    @Override
    protected ClassNode getNormalOpResultType() {
        return ClassHelper.int_TYPE;
    }

    @Override
    protected int getShiftOperationBytecode(int type) {
        return shiftOp[type];
    }

    @Override
    protected int getStandardOperationBytecode(int type) {
        return stdOperations[type];
    }

    @Override
    protected void removeTwoOperands(MethodVisitor mv) {
        mv.visitInsn(POP2);
    }

    @Override
    protected void writeMinusMinus(MethodVisitor mv) {
        mv.visitInsn(ICONST_1);
        mv.visitInsn(ISUB);
    }

    @Override
    protected void writePlusPlus(MethodVisitor mv) {
        mv.visitInsn(ICONST_1);
        mv.visitInsn(IADD);
    }

    @Override
    protected ClassNode getDevisionOpResultType() {
        return ClassHelper.int_TYPE;
    }

    @Override
    protected boolean supportsDivision() {
        return true;
    }
}
