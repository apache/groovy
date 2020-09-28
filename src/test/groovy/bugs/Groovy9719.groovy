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
package groovy.bugs

import org.codehaus.groovy.control.*
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

final class Groovy9719 {
    @Test
    void testInnerClassRef() {
        def config = new CompilerConfiguration()
        config.with {
            targetDirectory = File.createTempDir()
            jointCompilationOptions = [stubDir: File.createTempDir()]
        }
        def parentDir = File.createTempDir()
        try {
            new File(parentDir, 'p').mkdir()
            new File(parentDir, 'q').mkdir()

            def a = new File(parentDir, 'p/A.groovy')
            a.write '''\
                package p
                import q.B
                class A {
                    @SuppressWarnings([B.C.prop])
                    A() {
                        println(B.C.name)
                        println(B.C.prop)
                    }
                }
            '''.stripIndent()
            def b = new File(parentDir, 'q/B.groovy')
            b.write '''\
                package q
                class B {
                    interface C {
                        String prop = 'rawtypes'
                    }
                }
            '''.stripIndent()

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new CompilationUnit(config, null, loader)
            cu.addSources(a, b)
            cu.compile()

            def basePath = config.targetDirectory.absolutePath.replace('\\', '/')
            assertScript """
                def loader = new GroovyClassLoader(this.class.classLoader)
                loader.addClasspath('$basePath')
                def a = loader.loadClass('p.A')
                a.newInstance()
            """
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
            config.jointCompilationOptions.stubDir.deleteDir()
        }
    }
}
