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
package org.codehaus.groovy.classgen

import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.Phases
import org.junit.jupiter.api.Test
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

final class CovariantBridgeMethodOrderTest {

    /**
     * The covariant bridge methods Verifier adds must be emitted in the order they are
     * found, not in the hash order of the map that collects them, so that identical
     * sources produce identical class files.
     */
    @Test
    void testBridgeMethodsAreEmittedInDeclarationOrder() {
        def cu = new CompilationUnit()
        cu.addSource('Sample.groovy', '''
            interface Alpha   { Object alpha()   }
            interface Beta    { Object beta()    }
            interface Gamma   { Object gamma()   }
            interface Delta   { Object delta()   }
            interface Epsilon { Object epsilon() }

            class Sample implements Alpha, Beta, Gamma, Delta, Epsilon {
                String  alpha()   { null }
                Integer beta()    { null }
                Long    gamma()   { null }
                Double  delta()   { null }
                Short   epsilon() { null }
            }
        ''')
        cu.compile(Phases.CLASS_GENERATION)

        byte[] bytes = cu.classes.find { it.name == 'Sample' }.bytes

        def bridges = []
        new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                if (descriptor == '()Ljava/lang/Object;') bridges << name
                return null
            }
        }, 0)

        assert bridges == ['alpha', 'beta', 'gamma', 'delta', 'epsilon']
    }
}
