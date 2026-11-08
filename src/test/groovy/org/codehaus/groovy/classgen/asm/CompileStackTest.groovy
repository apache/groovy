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
package org.codehaus.groovy.classgen.asm

import org.codehaus.groovy.ast.ClassHelper
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertSame

/**
 * Direct coverage of {@link CompileStack} pattern-variable registration.
 */
final class CompileStackTest {

    @Test
    void testPredeclarePatternVariableIgnoresNull() {
        CompileStack stack = new CompileStack(null)
        BytecodeVariable variable = new BytecodeVariable(2, ClassHelper.STRING_TYPE, 's', 1)
        stack.predeclarePatternVariable(variable)

        stack.predeclarePatternVariable(null)

        assertSame(variable, stack.takePredeclaredPatternVariable('s'))
        assertNull(stack.takePredeclaredPatternVariable('s'))
    }

    @Test
    void testPredeclarePatternVariableRegistersSlot() {
        CompileStack stack = new CompileStack(null)
        BytecodeVariable variable = new BytecodeVariable(2, ClassHelper.STRING_TYPE, 's', 1)

        stack.predeclarePatternVariable(variable)

        assertSame(variable, stack.takePredeclaredPatternVariable('s'))
        assertNull(stack.takePredeclaredPatternVariable(null))
    }
}
