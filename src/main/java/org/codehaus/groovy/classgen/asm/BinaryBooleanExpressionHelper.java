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

import static org.codehaus.groovy.syntax.Types.PLUS;

/**
 * Binary write operations specialised for Booleans
 */
public class BinaryBooleanExpressionHelper extends BinaryIntExpressionHelper {

    public BinaryBooleanExpressionHelper(WriterController wc) {
        super(wc, boolArraySet, boolArrayGet);
    }
    
    private static final MethodCaller 
        boolArrayGet = MethodCaller.newStatic(BytecodeInterface8.class, "zArrayGet"),
        boolArraySet = MethodCaller.newStatic(BytecodeInterface8.class, "zArraySet");

    @Override
    protected ClassNode getArrayGetResultType() {
        return ClassHelper.boolean_TYPE;
    }
    
    @Override
    public boolean writePostOrPrefixMethod(int operation, boolean simulate) {
        if (simulate) return false;
        throw new GroovyBugError("should not reach here");
    }
    
    @Override
    protected boolean writeStdOperators(int type, boolean simulate) {
        type = type - PLUS;
        if (type < 0 || type > 5 || type == 3 /*DIV*/) return false;
        if (simulate) return false;
        throw new GroovyBugError("should not reach here");
    }
    
    @Override
    protected boolean writeDivision(boolean simulate) {
        if (simulate) return false;
        throw new GroovyBugError("should not reach here");
    }

    @Override
    protected ClassNode getNormalOpResultType() {
        return ClassHelper.boolean_TYPE;
    }
    
    @Override
    protected ClassNode getDevisionOpResultType() {
        return ClassHelper.boolean_TYPE;
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
    protected void removeTwoOperands(MethodVisitor mv) {
        throw new GroovyBugError("should not reach here");
    }
    @Override
    protected void writePlusPlus(MethodVisitor mv) {
        throw new GroovyBugError("should not reach here");
    }
    @Override
    protected void writeMinusMinus(MethodVisitor mv) {
        throw new GroovyBugError("should not reach here");
    }
    @Override
    protected void doubleTwoOperands(MethodVisitor mv) {
        throw new GroovyBugError("should not reach here");
    }
    
}
