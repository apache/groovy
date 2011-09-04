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
public class BinaryFloatExpressionHelper extends BinaryExpressionWriter {

    public BinaryFloatExpressionHelper(WriterController controller) {
        super(controller);
    }
    
    protected void doubleTwoOperands(MethodVisitor mv) {
        mv.visitInsn(DUP2);
    }

    private static final MethodCaller 
        floatArrayGet = MethodCaller.newStatic(BytecodeInterface8.class, "fArrayGet"),
        floatArraySet = MethodCaller.newStatic(BytecodeInterface8.class, "fArraySet");

    
    protected MethodCaller getArrayGetCaller() {
        return floatArrayGet;
    }
    
    protected MethodCaller getArraySetCaller() {
        return floatArraySet;
    }
    
    protected boolean writeBitwiseOp(int type, boolean simulate) {
        if (!simulate) throw new GroovyBugError("should not reach here");
        return false;   
    }    
    
    protected int getBitwiseOperationBytecode(int type) {
        return -1;
    }
    
    protected int getCompareCode() {
        return FCMPG;
    }
    
    protected ClassNode getNormalOpResultType() {
        return ClassHelper.float_TYPE;
    }
    
    protected boolean writeShiftOp(int type, boolean simulate) {
        if (!simulate) throw new GroovyBugError("should not reach here");
        return false;   
    }    
    
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
    
    protected int getStandardOperationBytecode(int type) {
        return stdOperations[type];
    }
    
    protected void removeTwoOperands(MethodVisitor mv) {
        mv.visitInsn(POP2);
    }
    
    protected void writeMinusMinus(MethodVisitor mv) {
        mv.visitInsn(FCONST_1);
        mv.visitInsn(FSUB);
    }
    
    protected void writePlusPlus(MethodVisitor mv) {
        mv.visitInsn(FCONST_1);
        mv.visitInsn(FADD);
    }

    protected ClassNode getDevisionOpResultType() {
        return ClassHelper.BigDecimal_TYPE;
    }

}
