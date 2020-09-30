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
import org.codehaus.groovy.runtime.BytecodeInterface8;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.DUP2;
import static org.objectweb.asm.Opcodes.FADD;
import static org.objectweb.asm.Opcodes.FCMPG;
import static org.objectweb.asm.Opcodes.FCONST_1;
import static org.objectweb.asm.Opcodes.FDIV;
import static org.objectweb.asm.Opcodes.FMUL;
import static org.objectweb.asm.Opcodes.FREM;
import static org.objectweb.asm.Opcodes.FSUB;
import static org.objectweb.asm.Opcodes.POP2;

public class BinaryFloatExpressionHelper extends BinaryExpressionWriter {

    public BinaryFloatExpressionHelper(WriterController controller) {
        super(controller, floatArraySet, floatArrayGet);
    }
    
    @Override
    protected void doubleTwoOperands(MethodVisitor mv) {
        mv.visitInsn(DUP2);
    }

    private static final MethodCaller 
        floatArrayGet = MethodCaller.newStatic(BytecodeInterface8.class, "fArrayGet"),
        floatArraySet = MethodCaller.newStatic(BytecodeInterface8.class, "fArraySet");

    @Override
    protected boolean writeBitwiseOp(int type, boolean simulate) {
        if (!simulate) throw new GroovyBugError("should not reach here");
        return false;   
    }    
    
    @Override
    protected int getBitwiseOperationBytecode(int type) {
        return -1;
    }
    
    @Override
    protected int getCompareCode() {
        return FCMPG;
    }
    
    @Override
    protected ClassNode getNormalOpResultType() {
        return ClassHelper.float_TYPE;
    }
    
    @Override
    protected boolean writeShiftOp(int type, boolean simulate) {
        if (!simulate) throw new GroovyBugError("should not reach here");
        return false;   
    }    
    
    @Override
    protected int getShiftOperationBytecode(int type) {
        return -1;
    }

    private static final int[] stdOperations = {
        FADD,           //  PLUS        200
        FSUB,           //  MINUS       201
        FMUL,           //  MULTIPLY    202
        0,              //  DIV, (203) but we don't want that one
        FDIV,           //  INTDIV      204
        FREM,           //  MOD         203
    };    
    
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
        mv.visitInsn(FCONST_1);
        mv.visitInsn(FSUB);
    }
    
    @Override
    protected void writePlusPlus(MethodVisitor mv) {
        mv.visitInsn(FCONST_1);
        mv.visitInsn(FADD);
    }

    @Override
    protected ClassNode getDevisionOpResultType() {
        return ClassHelper.BigDecimal_TYPE;
    }

}
