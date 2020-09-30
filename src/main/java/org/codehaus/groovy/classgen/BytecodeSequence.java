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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * This class represents a sequence of {@link BytecodeInstruction}s
 * or {@link ASTNode}s. The evaluation is depending on the type of
 * the visitor.
 */
public class BytecodeSequence extends Statement {

    public BytecodeSequence(final BytecodeInstruction instruction) {
        this.instructions = Collections.singletonList(instruction);
    }

    public BytecodeSequence(final List<?> instructions) {
        this.instructions = Objects.requireNonNull(instructions);
    }

    public List<?> getInstructions() {
        return Collections.unmodifiableList(instructions);
    }

    private final List<?> instructions;

    /**
     * Returns the singular BytecodeInstruction.
     *
     * @return {@code null} if instruction(s) is not a BytecodeInstruction
     */
    public BytecodeInstruction getBytecodeInstruction() {
        if (instructions.size() == 1) {
            Object instruction = instructions.get(0);
            if (instruction instanceof BytecodeInstruction) {
                return (BytecodeInstruction) instruction;
            }
        }
        return null;
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
     * @see ClassGenerator
     */
    @Override
    public void visit(final GroovyCodeVisitor visitor) {
        if (visitor instanceof ClassGenerator) {
            ((ClassGenerator) visitor).visitBytecodeSequence(this);
        } else {
            for (Object instruction : instructions) {
                if (instruction instanceof ASTNode) {
                    ((ASTNode) instruction).visit(visitor);
                }
            }
        }
    }
}
