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
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Phases
import org.junit.jupiter.api.Test
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

final class SyntheticClassLiteralFieldOrderTest {

    /**
     * A class literal naming a non-public type from another compilation unit cannot be
     * emitted as an ldc (see BytecodeHelper#isClassLiteralPossible), so AsmClassGenerator
     * emits a synthetic {@code $class$...} field plus a {@code $get$$class$...} accessor
     * for it. Those must be emitted in the order the literals were encountered, not in the
     * hash order of the map collecting them, so that identical sources produce identical
     * class files.
     */
    @Test
    void testSyntheticClassLiteralMembersAreEmittedInEncounterOrder() {
        File dir = File.createTempDir()
        try {
            // compilation unit 1: the non-public types
            def config = new CompilerConfiguration(targetDirectory: dir)
            def cu1 = new CompilationUnit(config)
            cu1.addSource('Hidden.groovy', '''
                package p
                import groovy.transform.PackageScope
                @PackageScope class Ha {}
                @PackageScope class Hb {}
                @PackageScope class Hc {}
                @PackageScope class Hd {}
                @PackageScope class He {}
            ''')
            cu1.compile()

            // compilation unit 2: references their class literals, in order
            def loader = new GroovyClassLoader(getClass().classLoader)
            loader.addClasspath(dir.absolutePath)
            def config2 = new CompilerConfiguration()
            config2.setClasspath(dir.absolutePath)
            def cu2 = new CompilationUnit(config2, null, loader)
            cu2.addSource('User.groovy', '''
                class User {
                    def f() { [p.Ha, p.Hb, p.Hc, p.Hd, p.He] }
                }
            ''')
            cu2.compile(Phases.CLASS_GENERATION)

            byte[] bytes = cu2.classes.find { it.name == 'User' }.bytes

            def fields = [], accessors = []
            new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                    if (name.startsWith('$class$')) fields << name - '$class$p$'
                    return null
                }
                @Override
                MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                    if (name.startsWith('$get$$class$')) accessors << name - '$get$$class$p$'
                    return null
                }
            }, 0)

            assert fields == ['Ha', 'Hb', 'Hc', 'Hd', 'He']
            assert accessors == ['Ha', 'Hb', 'Hc', 'Hd', 'He']
        } finally {
            dir.deleteDir()
        }
    }
}
