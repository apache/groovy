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
package org.codehaus.groovy.classgen;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.stmt.Statement;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a sequence of BytecodeInstructions
 * or ASTNodes. The evaluation is depending on the type of 
 * the visitor.
 * 
 * @see BytecodeInstruction
 * @see ASTNode
 */

public class BytecodeSequence extends Statement {
    private final List<BytecodeInstruction> instructions;

    public BytecodeSequence(List instructions) {
        this.instructions = instructions;
    }
    
    public BytecodeSequence(BytecodeInstruction instruction) {
        this.instructions = new ArrayList(1);
        this.instructions.add(instruction);
    }

    /**
     * Delegates to the visit method used for this class.
     * If the visitor is a ClassGenerator, then 
     * {@link ClassGenerator#visitBytecodeSequence(BytecodeSequence)}
     * is called with this instance. If the visitor is no 
     * ClassGenerator, then this method will call visit on
     * each ASTNode element sorted by this class. If one
     * element is a BytecodeInstruction, then it will be skipped
     * as it is no ASTNode. 
     * 
     * @param visitor the visitor
     * @see ClassGenerator
     */
    public void visit(GroovyCodeVisitor visitor) {
        if (visitor instanceof ClassGenerator) {
            ClassGenerator gen = (ClassGenerator) visitor;
            gen.visitBytecodeSequence(this);
            return;
        }
        for (BytecodeInstruction instruction : instructions) {
            Object part = instruction;
            if (part instanceof ASTNode) {
                ((ASTNode) part).visit(visitor);
            }
        }
    }

    public List getInstructions() {
        return instructions;
    }

}
