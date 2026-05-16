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

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.BytecodeExpression;
import org.objectweb.asm.MethodVisitor;

/**
 * A bytecode expression that loads a variable from a specific slot index.
 */
public class VariableSlotLoader extends BytecodeExpression {

    private final int idx;
    private final OperandStack operandStack;

    /**
     * Creates a variable slot loader with a specified type and index.
     *
     * @param type the type of the variable
     * @param index the slot index of the variable
     * @param os the operand stack
     */
    public VariableSlotLoader(ClassNode type, int index, OperandStack os) {
        super(type);
        this.idx = index;
        this.operandStack = os;
    }

    /**
     * Creates a variable slot loader with a specified index.
     *
     * @param index the slot index of the variable
     * @param os the operand stack
     */
    public VariableSlotLoader(int index, OperandStack os) {
        this.idx = index;
        this.operandStack = os;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(MethodVisitor mv) {
        operandStack.load(this.getType(), idx);
        operandStack.remove(1);
    }

    /**
     * Returns the slot index of the variable.
     *
     * @return the variable slot index
     */
    public int getIndex(){
        return idx;
    }
}
