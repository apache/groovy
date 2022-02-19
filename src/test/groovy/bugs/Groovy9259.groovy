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

import static groovy.test.GroovyAssert.assertScript

final class Groovy9259 {

    @Test
    void testInterfaceAndTrait() {
        def config = new CompilerConfiguration(
            targetDirectory: File.createTempDir(),
            jointCompilationOptions: [memStub: true]
        )

        def parentDir = File.createTempDir()
        try {
            def a = new File(parentDir, 'J.java')
            a.write '''
                public interface J {
                    String m();
                }
            '''
            def b = new File(parentDir, 'G.groovy')
            b.write '''
                interface G extends J {
                    default String m() {
                        'G'
                    }
                }
            '''
            def c = new File(parentDir, 'Main.groovy')
            c.write '''
                class C implements G {
                    @Override String m() {
                        'C'
                    }
                }

                @groovy.transform.CompileStatic
                void test() {
                    J obj = new C()
                    def x = obj.m()
                    assert x == 'C'
                }
                test()
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(a, b, c)
            cu.compile()

            loader.loadClass('Main').main()
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
        }
    }

    @Test
    void testInterfacesAndTrait() {
        assertScript '''
            interface A {
                String m()
            }
            interface B {
                String m()
            }
            interface C extends A, B {
                default String m() {
                    'C'
                }
            }

            @groovy.transform.CompileStatic
            void test() {
                A one = new C() {}
                def x = one.m()
                assert x == 'C'

                B two = new C() {}
                def y = two.m()
                assert y == 'C'
            }
            test()
        '''
    }
}
