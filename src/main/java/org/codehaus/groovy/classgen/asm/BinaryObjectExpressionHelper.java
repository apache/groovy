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

public class BinaryObjectExpressionHelper extends BinaryExpressionWriter {
    private static final MethodCaller arrayGet = MethodCaller.newStatic(BytecodeInterface8.class, "objectArrayGet");
    private static final MethodCaller arraySet = MethodCaller.newStatic(BytecodeInterface8.class, "objectArraySet");

    public BinaryObjectExpressionHelper(WriterController controller) {
        super(controller, arraySet, arrayGet);
    }
    
    // dummy methods
    @Override
    public boolean writePostOrPrefixMethod(int operation, boolean simulate) {
        if (simulate) return false;
        throw new GroovyBugError("should not reach here");
    }
    
    @Override
    public boolean write(int operation, boolean simulate) {
        if (simulate) return false;
        throw new GroovyBugError("should not reach here");
    }
    
    @Override
    protected boolean writeDivision(boolean simulate) {
        if (simulate) return false;
        throw new GroovyBugError("should not reach here");
    }

    @Override
    protected int getBitwiseOperationBytecode(int type) {
        return -1;
    }
    
    @Override
    protected int getCompareCode() {
        return -1;
    }
    
    @Override
    protected ClassNode getNormalOpResultType() {
        return null;
    }
    
    @Override
    protected ClassNode getDevisionOpResultType() {
        return null;
    }
    
    @Override
    protected int getShiftOperationBytecode(int type) {
        return -1;
    }
    
    @Override
    protected int getStandardOperationBytecode(int type) {
        return -1;
    }
    
    @Override
    protected void removeTwoOperands(MethodVisitor mv) {}
    @Override
    protected void writePlusPlus(MethodVisitor mv) {}
    @Override
    protected void writeMinusMinus(MethodVisitor mv) {}
    @Override
    protected void doubleTwoOperands(MethodVisitor mv) {}
    
    @Override
    protected ClassNode getArrayGetResultType() {
    	return ClassHelper.OBJECT_TYPE;
    }
}
