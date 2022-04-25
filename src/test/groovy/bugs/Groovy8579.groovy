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
import static groovy.test.GroovyAssert.shouldFail

final class Groovy8579 {

    @Test
    void testCallToStaticInterfaceMethod1() {
        assertScript '''
            @groovy.transform.CompileStatic
            Comparator test() {
                Map.Entry.comparingByKey()
            }

            assert test() instanceof Comparator
        '''
    }

    @Test
    void testCallToStaticInterfaceMethod2() {
        assertScript '''
            import static java.util.Map.Entry.comparingByKey

            @groovy.transform.CompileStatic
            Comparator test() {
                comparingByKey()
            }

            assert test() instanceof Comparator
        '''
    }

    @Test // GROOVY-10592
    void testCallToStaticInterfaceMethod4() {
        ['CompileStatic', 'TypeChecked'].each { mode ->
            def sourceDir = File.createTempDir()
            def config = new CompilerConfiguration(
                targetDirectory: File.createTempDir(),
                jointCompilationOptions: [memStub: true]
            )
            try {
                def a = new File(sourceDir, 'Face.java')
                a.write '''
                    interface Face {
                        static String getValue() {
                            return "value";
                        }
                    }
                '''
                def b = new File(sourceDir, 'Main.groovy')
                b.write """
                    @groovy.transform.${mode}
                    void test(Face face) {
                        face.value
                    }
                """

                def loader = new GroovyClassLoader(this.class.classLoader)
                def cu = new JavaAwareCompilationUnit(config, loader)
                cu.addSources(a, b)
                def err = shouldFail {
                    cu.compile()
                }
                assert err =~ /static method of interface Face can only be accessed /
            } finally {
                sourceDir.deleteDir()
                config.targetDirectory.deleteDir()
            }
        }
    }
}
