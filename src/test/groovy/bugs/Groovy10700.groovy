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

import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

final class Groovy10700 {
    @Test
    void testDelegateAndInterface() {
        def config = new CompilerConfiguration(targetDirectory: File.createTempDir())

        def parentDir = File.createTempDir()
        try {
            new File(parentDir, 'p').mkdir()

            def a = new File(parentDir, 'p/ABC.groovy')
            a.write '''
                package p

                interface A {
                    String decode(String s)
                }

                class B implements A {
                    String decode(String s) { s }
                }

                class C implements A {
                    @Delegate private final B b = new B()
                }
            '''
            def b = new File(parentDir, 'p/D.groovy')
            b.write '''
                package p

                class D extends C { // implements A
                    @groovy.transform.TypeChecked
                    void test() {
                        def x = decode('string')
                        assert x == 'string'
                    }
                }
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new CompilationUnit(config, null, loader)
            cu.addSources(a, b)
            cu.compile()

            def basePath = config.targetDirectory.absolutePath.replace('\\','/')
            assertScript """
                def loader = new GroovyClassLoader(this.class.classLoader)
                loader.addClasspath('$basePath')
                def d = loader.loadClass('p.D')
                d.newInstance().test()
            """
        } finally {
            config.targetDirectory.deleteDir()
            parentDir.deleteDir()
        }
    }
}
