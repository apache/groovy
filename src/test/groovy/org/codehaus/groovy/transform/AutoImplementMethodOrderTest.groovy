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
package org.codehaus.groovy.transform

import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.Phases
import org.junit.jupiter.api.Test
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

final class AutoImplementMethodOrderTest {

    /**
     * The methods {@code @AutoImplement} generates must be emitted in the order they are
     * found, not in the hash order of the map that collects them, so that identical
     * sources produce identical class files.
     */
    @Test
    void testGeneratedMethodsAreEmittedInDeclarationOrder() {
        def cu = new CompilationUnit()
        cu.addSource('Probe.groovy', '''
            interface Face {
                void alpha()
                void beta()
                void gamma()
                void delta()
                void epsilon()
                void zeta()
                void eta()
                void theta()
            }

            @groovy.transform.AutoImplement
            class Impl implements Face {}
        ''')
        cu.compile(Phases.CLASS_GENERATION)

        byte[] bytes = cu.classes.find { it.name == 'Impl' }.bytes

        def declared = ['alpha', 'beta', 'gamma', 'delta', 'epsilon', 'zeta', 'eta', 'theta']
        def generated = []
        new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                if (name in declared) generated << name
                return null
            }
        }, 0)

        assert generated == declared
    }
}
