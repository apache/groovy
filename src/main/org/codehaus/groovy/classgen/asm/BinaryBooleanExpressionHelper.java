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

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.runtime.BytecodeInterface8;
import org.objectweb.asm.MethodVisitor;

/**
 * @author <a href="mailto:blackdrag@gmx.org">Jochen "blackdrag" Theodorou</a>
 */
public class BinaryBooleanExpressionHelper extends BinaryIntExpressionHelper {

    public BinaryBooleanExpressionHelper(WriterController wc) {
        super(wc);
    }
    
    private static final MethodCaller 
        boolArrayGet = MethodCaller.newStatic(BytecodeInterface8.class, "zArrayGet"),
        boolArraySet = MethodCaller.newStatic(BytecodeInterface8.class, "zArraySet");

    @Override
    protected MethodCaller getArrayGetCaller() {
        return boolArrayGet;
    }
    
    @Override
    protected MethodCaller getArraySetCaller() {
        return boolArraySet;
    }
    
    @Override
    protected ClassNode getArrayGetResultType() {
        return ClassHelper.boolean_TYPE;
    }
    
    public boolean writePostOrPrefixMethod(int operation, boolean simulate) {
        if (simulate) return false;
        throw new GroovyBugError("should not reach here");
    }
    
    @Override
    protected boolean writeStdOperators(int type, boolean simulate) {
        if (simulate) return false;
        throw new GroovyBugError("should not reach here");
    }
    
    protected boolean writeDivision(boolean simulate) {
        if (simulate) return false;
        throw new GroovyBugError("should not reach here");
    }

    protected int getBitwiseOperationBytecode(int type) {
        return -1;
    }
    
    protected ClassNode getNormalOpResultType() {
        return ClassHelper.boolean_TYPE;
    }
    
    protected ClassNode getDevisionOpResultType() {
        return ClassHelper.boolean_TYPE;
    }
    
    protected int getShiftOperationBytecode(int type) {
        return -1;
    }
    
    protected int getStandardOperationBytecode(int type) {
        return -1;
    }
    
    protected void removeTwoOperands(MethodVisitor mv) {
        throw new GroovyBugError("should not reach here");
    }
    protected void writePlusPlus(MethodVisitor mv) {
        throw new GroovyBugError("should not reach here");
    }
    protected void writeMinusMinus(MethodVisitor mv) {
        throw new GroovyBugError("should not reach here");
    }
    protected void doubleTwoOperands(MethodVisitor mv) {
        throw new GroovyBugError("should not reach here");
    }
    
}
