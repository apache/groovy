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
package org.codehaus.groovy.transform.traitx

import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Phases
import org.junit.jupiter.api.Test
import org.objectweb.asm.ClassReader
import org.objectweb.asm.util.CheckClassAdapter

import static groovy.test.GroovyAssert.assertScript

final class Groovy_7926 {

    @Test
    void testThatVoidTypesFromTraitsWithGenericsWork() {
        assertScript '''
            trait T<X> {
                void proc() {
                    println 'works'
                }
            }
            class C implements T<C> {
            }

            new C().proc()
        '''
    }

    @Test
    void testThatVoidTypesAreNotUsedForVariableNamesInByteCode() {
        def config = new CompilerConfiguration().tap {
            targetDirectory = File.createTempDir()
        }
        File parentDir = File.createTempDir()
        try {
            def loader = new GroovyClassLoader(this.class.classLoader)
            def unit = new CompilationUnit(config, null, loader)
            unit.addSources(new File(parentDir, 'T.groovy').leftShift(
                '''
                trait T<X> {
                    void proc() {
                        println 'works'
                    }
                }
                class C implements T<C> {
                }
                '''
            ))
            unit.compile(Phases.CLASS_GENERATION)

            def writer = new StringWriter()
            def reader = new ClassReader(unit.classes.find{ it.name == 'C' }.bytes)
            CheckClassAdapter.verify(reader, loader, true, new PrintWriter(writer))

            def string = writer.toString().with {
                int start = indexOf('proc()V')
                int until = indexOf('RETURN', start) + 7
                substring(start, until) // proc bytecode
            }
            assert !string.contains('CHECKCAST void') //
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
        }
    }
}
