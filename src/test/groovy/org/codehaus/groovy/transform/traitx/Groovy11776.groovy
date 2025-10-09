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
import org.junit.Test
import org.objectweb.asm.ClassReader
import org.objectweb.asm.util.CheckClassAdapter

final class Groovy11776 {

    @Test
    void testTraitMethodOverloads() {
        File sourceDir = File.createTempDir()
        File targetDir = File.createTempDir()
        try {
            def a = new File(sourceDir, 'A.groovy')
            a.write '''
                trait A {
                    def foo(Object o) {
                        return 'foo(o)'
                    }
                    def foo(Map<String,Object> m) {
                        return 'foo(m)'
                    }
                }
            '''
            def b = new File(sourceDir, 'B.groovy')
            b.write '''
                class B implements A {
                    def bar(Object o) {
                        return 'bar(o)'
                    }
                    def bar(Map<String,Object> m) {
                        return 'bar(m)'
                    }
                }
            '''
            def c = new File(sourceDir, 'C.groovy')
            c.write '''
                new B().with {
                    assert bar( (Object) null) == 'bar(o)'
                    assert bar(null as Object) == 'bar(o)'
                    assert foo( (Object) null) == 'foo(o)'
                    assert foo(null as Object) == 'foo(o)'
                }
                (new Object() as A).with {
                    assert foo( (Object) null) == 'foo(o)'
                    assert foo(null as Object) == 'foo(o)'
                }
            '''

            def config = new CompilerConfiguration(targetDirectory: targetDir)
            def loader = new GroovyClassLoader(this.class.classLoader)
            def unit = new CompilationUnit(config, null, loader)
            unit.addSources(a, b, c)
            unit.compile()

            loader.addClasspath(targetDir.absolutePath)
            loader.loadClass('C', true).main()

            // produce bytecode for class B
            def writer = new StringWriter()
            def reader = new ClassReader(unit.classes.find{ it.name == 'B' }.bytes)
            CheckClassAdapter.verify(reader, loader, true, new PrintWriter(writer))

            def string = writer.toString().with {
                int start = indexOf('foo(Ljava/lang/Object;)')
                int until = indexOf('ARETURN', start) + 8
                substring(start, until)
            }
            assert !string.contains('INVOKEDYNAMIC invoke(Ljava/lang/Class;')
            assert  string.contains('INVOKESTATIC A$Trait$Helper.foo')
        } finally {
            sourceDir.deleteDir()
            targetDir.deleteDir()
        }
    }
}
