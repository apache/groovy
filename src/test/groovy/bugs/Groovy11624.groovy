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
package bugs

import groovy.test.NotYetImplemented
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

import static groovy.test.GroovyAssert.assertScript

final class Groovy11624 {

    @ParameterizedTest
    @ValueSource(strings=['CompileDynamic','TypeChecked','CompileStatic'])
    void testConstantInlining1(String mode) {
        assertScript """import static bugs.Groovy11624Support.CONST
            @groovy.transform.${mode}
            interface I {
                String SINCE = CONST + '12345'
                @Deprecated(since = SINCE)
                interface J {
                }
            }

            assert I.J.getAnnotation(Deprecated).since() == 'value12345'
        """
    }

    @NotYetImplemented @Test
    void testConstantInlining2() {
        def config = new CompilerConfiguration().tap {
            jointCompilationOptions = [memStub: true]
            targetDirectory = File.createTempDir()
        }
        File parentDir = File.createTempDir()
        try {
            new File(parentDir, 'p').mkdir()

            def a = new File(parentDir, 'p/A.java')
            a.write '''package p;
                public class A {
                    public static final String CONST = "value";
                }
            '''

            def b = new File(parentDir, 'B.groovy')
            b.write '''
                interface I {
                    String SINCE = p.A.CONST + '12345'
                    @Deprecated(since = SINCE)
                    interface J {
                    }
                }

                assert I.J.getAnnotation(Deprecated).since() == 'value12345'
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(a, b)
            cu.compile()

            loader.loadClass('B').main()
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
        }
    }
}
