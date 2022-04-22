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

final class Groovy8579 {

    @Test
    void testCallToStaticInterfaceMethod1() {
        ['CompileDynamic', 'CompileStatic', 'TypeChecked'].each { mode ->
            assertScript """
                @groovy.transform.${mode}
                def test() {
                    Map.Entry.comparingByKey()
                }

                assert test() instanceof Comparator
            """
        }
    }

    @Test
    void testCallToStaticInterfaceMethod2() {
        ['CompileDynamic', 'CompileStatic', 'TypeChecked'].each { mode ->
            assertScript """
                import static java.util.Map.Entry.comparingByKey

                @groovy.transform.${mode}
                def test() {
                    comparingByKey()
                }

                assert test() instanceof Comparator
            """
        }
    }

    @Test // GROOVY-10592
    void testCallToStaticInterfaceMethod3() {
        ['CompileDynamic', 'CompileStatic', 'TypeChecked'].each { mode ->
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
                        static void setValue(String value) {
                            if (!"value".equals(value))
                                throw new AssertionError();
                        }
                    }
                '''
                def b = new File(sourceDir, 'Main.groovy')
                b.write """
                    @groovy.transform.${mode}
                    void test() {
                        assert Face.value == 'value'
                        Face.value = 'value'
                    }
                    test()
                """

                def loader = new GroovyClassLoader(this.class.classLoader)
                def cu = new JavaAwareCompilationUnit(config, loader)
                cu.addSources(a, b)
                cu.compile()

                loader.loadClass('Main').main()
            } finally {
                sourceDir.deleteDir()
                config.targetDirectory.deleteDir()
            }
        }
    }
}
