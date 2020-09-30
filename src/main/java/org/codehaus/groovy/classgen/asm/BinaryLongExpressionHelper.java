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
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.DUP2_X1;
import static org.objectweb.asm.Opcodes.LADD;
import static org.objectweb.asm.Opcodes.LAND;
import static org.objectweb.asm.Opcodes.LCMP;
import static org.objectweb.asm.Opcodes.LCONST_1;
import static org.objectweb.asm.Opcodes.LDIV;
import static org.objectweb.asm.Opcodes.LMUL;
import static org.objectweb.asm.Opcodes.LOR;
import static org.objectweb.asm.Opcodes.LREM;
import static org.objectweb.asm.Opcodes.LSHL;
import static org.objectweb.asm.Opcodes.LSHR;
import static org.objectweb.asm.Opcodes.LSUB;
import static org.objectweb.asm.Opcodes.LUSHR;
import static org.objectweb.asm.Opcodes.LXOR;
import static org.objectweb.asm.Opcodes.POP2;

public class BinaryLongExpressionHelper extends BinaryExpressionWriter {

    /**
     * @since 2.5.0
     */
    public BinaryLongExpressionHelper(WriterController controller, MethodCaller arraySet, MethodCaller arrayGet) {
        super(controller, arraySet, arrayGet);
    }

    public BinaryLongExpressionHelper(WriterController controller) {
        this(controller, longArraySet, longArrayGet);
    }

    @Override
    protected void doubleTwoOperands(MethodVisitor mv) {
        /*
            since there is no DUP4 we have to do this:
            DUP2_X1
            POP2
            DUP2_X1
            DUP2_X1
            POP2
            DUP2_X1          
         */
        mv.visitInsn(DUP2_X1);
        mv.visitInsn(POP2);
        mv.visitInsn(DUP2_X1);
        mv.visitInsn(DUP2_X1);
        mv.visitInsn(POP2);
        mv.visitInsn(DUP2_X1);
    }

    @Override
    protected void removeTwoOperands(MethodVisitor mv) {
        mv.visitInsn(POP2);
        mv.visitInsn(POP2);
    }

    private static final MethodCaller 
        longArrayGet = MethodCaller.newStatic(BytecodeInterface8.class, "lArrayGet"),
        longArraySet = MethodCaller.newStatic(BytecodeInterface8.class, "lArraySet");

    private static final int[] bitOp = {
        LOR,            //  BITWISE_OR / PIPE   340
        LAND,           //  BITWISE_AND         341
        LXOR,           //  BIWISE_XOR          342
    };

    @Override
    protected int getBitwiseOperationBytecode(int type) {
        return bitOp[type];
    }

    @Override
    protected int getCompareCode() {
        return LCMP;
    }

    @Override
    protected ClassNode getNormalOpResultType() {
        return ClassHelper.long_TYPE;
    }

    private static final int[] shiftOp = {
        LSHL,           // LEFT_SHIFT               280
        LSHR,           // RIGHT_SHIFT              281
        LUSHR           // RIGHT_SHIFT_UNSIGNED     282
    };
    
    @Override
    protected int getShiftOperationBytecode(int type) {
        return shiftOp[type];
    }

    private static final int[] stdOperations = {
        LADD,           //  PLUS        200
        LSUB,           //  MINUS       201
        LMUL,           //  MULTIPLY    202
        LDIV,           //  DIV         203
        LDIV,           //  INTDIV      204
        LREM,           //  MOD         203
    };
    
    @Override
    protected int getStandardOperationBytecode(int type) {
        return stdOperations[type];
    }

    @Override
    protected void writeMinusMinus(MethodVisitor mv) {
        mv.visitInsn(LCONST_1);
        mv.visitInsn(LSUB);
    }

    @Override
    protected void writePlusPlus(MethodVisitor mv) {
        mv.visitInsn(LCONST_1);
        mv.visitInsn(LADD);
    }

    @Override
    protected ClassNode getDevisionOpResultType() {
        return ClassHelper.long_TYPE;
    }

    @Override
    protected boolean supportsDivision() {
        return true;
    }
}
