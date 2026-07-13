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
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Phases
import org.junit.jupiter.api.Test
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes

final class AnnotationCollectorMemberOrderTest {

    /**
     * When a collector annotation is precompiled, its members are read back from the
     * generated CollectorHelper and copied onto the annotations it expands to. They must
     * be copied in the order they were declared on the collector, not in hash order, since
     * that order is written into the class file's annotation attribute.
     * <p>
     * A collector declared in the same compilation unit takes an order-preserving AST path,
     * so this only shows up when the collector comes from the classpath.
     */
    @Test
    void testCollectedMembersAreEmittedInDeclarationOrder() {
        File dir = File.createTempDir()
        try {
            // compilation unit 1: the annotation and the collector that presets its members
            def config = new CompilerConfiguration(targetDirectory: dir)
            def cu1 = new CompilationUnit(config)
            cu1.addSource('Coll.groovy', '''
                import groovy.transform.AnnotationCollector
                import java.lang.annotation.*

                @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE)
                @interface Marker {
                    String alpha() default ''
                    String beta() default ''
                    String gamma() default ''
                    String delta() default ''
                    String epsilon() default ''
                }

                @Marker(alpha='a', beta='b', gamma='c', delta='d', epsilon='e')
                @AnnotationCollector
                @interface Coll {}
            ''')
            cu1.compile()

            // compilation unit 2: applies the precompiled collector
            def loader = new GroovyClassLoader(getClass().classLoader)
            loader.addClasspath(dir.absolutePath)
            def config2 = new CompilerConfiguration()
            config2.setClasspath(dir.absolutePath)
            def cu2 = new CompilationUnit(config2, null, loader)
            cu2.addSource('Holder.groovy', '''
                @Coll
                class Holder {}
            ''')
            cu2.compile(Phases.CLASS_GENERATION)

            byte[] bytes = cu2.classes.find { it.name == 'Holder' }.bytes

            def members = []
            new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                    if (descriptor != 'LMarker;') return null
                    return new AnnotationVisitor(Opcodes.ASM9) {
                        @Override void visit(String name, Object value) { members << name }
                    }
                }
            }, 0)

            assert members == ['alpha', 'beta', 'gamma', 'delta', 'epsilon']
        } finally {
            dir.deleteDir()
        }
    }
}
