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

import static org.objectweb.asm.Opcodes.*;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.runtime.BytecodeInterface8;
import org.objectweb.asm.MethodVisitor;

/**
 * @author <a href="mailto:blackdrag@gmx.org">Jochen "blackdrag" Theodorou</a>
 */
public class BinaryLongExpressionHelper extends BinaryExpressionWriter {

    public BinaryLongExpressionHelper(WriterController controller) {
        super(controller);
    }

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

    protected void removeTwoOperands(MethodVisitor mv) {
        mv.visitInsn(POP2);
        mv.visitInsn(POP2);
    }

    private static final MethodCaller 
        longArrayGet = MethodCaller.newStatic(BytecodeInterface8.class, "lArrayGet"),
        longArraySet = MethodCaller.newStatic(BytecodeInterface8.class, "lArraySet");

    protected MethodCaller getArrayGetCaller() {
        return longArrayGet;
    }

    protected MethodCaller getArraySetCaller() {
        return longArraySet;
    }
    
    private static final int[] bitOp = {
        LOR,            //  BITWISE_OR / PIPE   340
        LAND,           //  BITWISE_AND         341
        LXOR,           //  BIWISE_XOR          342
    };

    protected int getBitwiseOperationBytecode(int type) {
        return bitOp[type];
    }

    protected int getCompareCode() {
        return LCMP;
    }

    protected ClassNode getNormalOpResultType() {
        return ClassHelper.long_TYPE;
    }

    private static final int[] shiftOp = {
        LSHL,           // LEFT_SHIFT               280
        LSHR,           // RIGHT_SHIFT              281
        LUSHR           // RIGHT_SHIFT_UNSIGNED     282
    };
    
    protected int getShiftOperationBytecode(int type) {
        return shiftOp[type];
    }

    private static final int[] stdOperations = {
        LADD,           //  PLUS        200
        LSUB,           //  MINUS       201
        LMUL,           //  MULTIPLY    202
        0,              //  DIV, (203) but we don't want that one
        LDIV,           //  INTDIV      204
        LREM,           //  MOD         203
    };
    
    protected int getStandardOperationBytecode(int type) {
        return stdOperations[type];
    }

    protected void writeMinusMinus(MethodVisitor mv) {
        mv.visitInsn(LCONST_1);
        mv.visitInsn(LSUB);
    }

    protected void writePlusPlus(MethodVisitor mv) {
        mv.visitInsn(LCONST_1);
        mv.visitInsn(LADD);
    }
    
    protected ClassNode getDevisionOpResultType() {
        return ClassHelper.BigDecimal_TYPE;
    }
}
