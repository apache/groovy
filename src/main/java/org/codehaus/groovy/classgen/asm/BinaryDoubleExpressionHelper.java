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

import static org.objectweb.asm.Opcodes.DADD;
import static org.objectweb.asm.Opcodes.DCMPG;
import static org.objectweb.asm.Opcodes.DCONST_1;
import static org.objectweb.asm.Opcodes.DDIV;
import static org.objectweb.asm.Opcodes.DMUL;
import static org.objectweb.asm.Opcodes.DREM;
import static org.objectweb.asm.Opcodes.DSUB;

public class BinaryDoubleExpressionHelper extends BinaryLongExpressionHelper {


    public BinaryDoubleExpressionHelper(WriterController controller) {
        super(controller, doubleArraySet, doubleArrayGet);
    }

    private static final MethodCaller 
        doubleArrayGet = MethodCaller.newStatic(BytecodeInterface8.class, "dArrayGet"),
        doubleArraySet = MethodCaller.newStatic(BytecodeInterface8.class, "dArraySet");

    @Override
    protected boolean writeBitwiseOp(int op, boolean simulate) {
        if (!simulate) throw new GroovyBugError("should not reach here");
        return false;   
    }
    
    @Override
    protected int getBitwiseOperationBytecode(int op) {
        return -1;
    }

    @Override
    protected int getCompareCode() {
        return DCMPG;
    }

    @Override
    protected ClassNode getNormalOpResultType() {
        return ClassHelper.double_TYPE;
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
        DADD,           //  PLUS        200
        DSUB,           //  MINUS       201
        DMUL,           //  MULTIPLY    202
        DDIV,           //  DIV         203
        DDIV,           //  INTDIV      204
        DREM,           //  MOD         203
    };
    
    @Override
    protected int getStandardOperationBytecode(int type) {
        return stdOperations[type];
    }
    
    @Override
    protected void writeMinusMinus(MethodVisitor mv) {
        mv.visitInsn(DCONST_1);
        mv.visitInsn(DSUB);
    }
    
    @Override
    protected void writePlusPlus(MethodVisitor mv) {
        mv.visitInsn(DCONST_1);
        mv.visitInsn(DADD);
    }
    
    @Override
    protected ClassNode getDevisionOpResultType() {
        return ClassHelper.double_TYPE;
    }
    
    @Override
    protected boolean supportsDivision() {
        return true;
    }
}
