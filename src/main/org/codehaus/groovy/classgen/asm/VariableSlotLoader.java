/*
 * Copyright 2003-2009 the original author or authors.
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

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.BytecodeExpression;
import org.objectweb.asm.MethodVisitor;

public class VariableSlotLoader extends BytecodeExpression {

    private int idx;
    private OperandStack operandStack;
    
    public VariableSlotLoader(ClassNode type, int index, OperandStack os) {
        super(type);
        this.idx = index;
        this.operandStack = os;
    }
    
    public VariableSlotLoader(int index, OperandStack os) {
        this.idx = index;
        this.operandStack = os;
    }
    
    @Override
    public void visit(MethodVisitor mv) {
        operandStack.load(this.getType(), idx);
        operandStack.remove(1);
    }

    public int getIndex(){
        return idx;
    }
}
