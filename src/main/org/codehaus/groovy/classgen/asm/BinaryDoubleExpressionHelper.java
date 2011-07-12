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

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.runtime.BytecodeInterface8;
import org.objectweb.asm.MethodVisitor;

/**
 * @author <a href="mailto:blackdrag@gmx.org">Jochen "blackdrag" Theodorou</a>
 */
public class BinaryDoubleExpressionHelper extends BinaryLongExpressionHelper {


    public BinaryDoubleExpressionHelper(WriterController controller) {
        super(controller);
    }

    private static final MethodCaller 
        doubleArrayGet = MethodCaller.newStatic(BytecodeInterface8.class, "dArrayGet"),
        doubleArraySet = MethodCaller.newStatic(BytecodeInterface8.class, "dArraySet");

    @Override
    protected MethodCaller getArrayGetCaller() {
        return doubleArrayGet;
    }

    @Override
    protected MethodCaller getArraySetCaller() {
        return doubleArraySet;
    }
    
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
        return ClassHelper.long_TYPE;
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
        0,              //  DIV, (203) but we don't want that one
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
}
