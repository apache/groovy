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

final class Groovy4349 {
    @Test
    void testStaticImport() {
        def config = new CompilerConfiguration(
            targetDirectory: File.createTempDir(),
            jointCompilationOptions: [memStub: true]
        )
        def parentDir = File.createTempDir()
        try {
            new File(parentDir, 'p').mkdir()

            def a = new File(parentDir, 'p/Types.groovy')
            a.write '''
                package p
                class C {
                    static final List<String> calls = []
                    def prop
                }
                class Utility {
                    static C method1(int i) {
                        C.calls << 'Utility.method1'
                        return new C()
                    }
                    static C method2(C c) {
                        C.calls << 'Utility.method2'
                        return c
                    }
                }
            '''
            def b = new File(parentDir, 'p/Main.groovy')
            b.write '''
                package p
                import static Utility.*
                class Main {
                    def method1() {
                        C.calls << 'Harness.method1'
                        new C()
                    }
                    void test() {
                        method2(method1()).prop = 8
                        assert C.calls == ['Harness.method1', 'Utility.method2']
                    }
                }
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(a, b)
            cu.compile()

            loader.loadClass('p.Main').newInstance().test()
        } finally {
            config.targetDirectory.deleteDir()
            parentDir.deleteDir()
        }
    }
}
