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

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit
import org.junit.Test

final class Groovy9204 {

    @Test
    void testGenerics() {
        def config = new CompilerConfiguration(
            targetDirectory: File.createTempDir(),
            jointCompilationOptions: [memStub: true]
        )

        def parentDir = File.createTempDir()
        try {
            def a = new File(parentDir, 'A.java')
            a.write '''
                class One<T extends java.util.List> {
                    protected T field;
                }

                class Two<T extends java.util.List> extends One<T> {
                }

                class Three extends Two<java.util.List> {
                }
                
                class Four extends Two<java.util.LinkedList> {
                }
                
            '''
            def b = new File(parentDir, 'B.groovy')
            b.write '''
                @groovy.transform.CompileStatic
                class ArrayListTest extends Three {
                    def test() {
                        field = new ArrayList()
                        field.add("hello")
                        field[0]
                    }
                }

                @groovy.transform.CompileStatic
                class LinkedListTest extends Four {
                    def test() {
                        field = new LinkedList()
                        field.addFirst("hello")
                        field[0]
                    }
                }
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(a, b)
            cu.compile()

            assert loader.loadClass('LinkedListTest').getConstructor().newInstance().test() == 'hello'
            assert loader.loadClass('ArrayListTest').getConstructor().newInstance().test() == 'hello'
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
        }
    }
}
