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

import groovy.test.NotYetImplemented
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit
import org.junit.Test

final class Groovy9204 {

    @Test @NotYetImplemented
    void testGenerics() {
        def config = new CompilerConfiguration(
            targetDirectory: File.createTempDir(),
            jointCompilationOptions: [memStub: true]
        )

        def parentDir = File.createTempDir()
        try {
            def a = new File(parentDir, 'A.java')
            a.write '''
                public class A {
                    public String meth() {
                        return "hello";
                    }
                }

                abstract class One<T extends A> {
                    protected T field;
                }

                abstract class Two<T extends A> extends One<T> {
                }

                abstract class Three extends Two<A> {
                }
            '''
            def b = new File(parentDir, 'B.groovy')
            b.write '''
                @groovy.transform.CompileStatic
                class B extends Three {
                    def test() {
                        field.meth() // typeof(field) should be A
                        //    ^^^^ Cannot find matching method java.lang.Object#meth()
                    }
                }
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(a, b)
            cu.compile()

            Class clazz = loader.loadClass('B')
            assert clazz.newInstance().test() == 'hello'
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
        }
    }
}
