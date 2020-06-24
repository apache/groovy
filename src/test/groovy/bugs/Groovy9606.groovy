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

final class Groovy9606 {

    @Test
    void testGenerics() {
        def config = new CompilerConfiguration(
            targetDirectory: File.createTempDir(),
            jointCompilationOptions: [
                    stubDir: File.createTempDir(),
                    keepStubs: true   // keep stubs to read the stubs later in this test
            ]
        )

        def parentDir = File.createTempDir()
        try {
            def a = new File(parentDir, 'SomeGroovyTrait.groovy')
            a.write '''
                trait SomeGroovyTrait<D> {
                    List<D> list(List<D> front, D last) {
                        front + [last]
                    }
                }
            '''
            def b = new File(parentDir, 'SomeGroovyClass.groovy')
            b.write '''
                class SomeGroovyClass implements SomeGroovyTrait<String> { }
            '''
            def c = new File(parentDir, 'SomeJavaClass.java')
            c.write '''
                public class SomeJavaClass {
                    SomeGroovyClass sgc;
                }
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(a, b, c)
            cu.compile()

            def stub = new File(config.jointCompilationOptions.stubDir, 'SomeGroovyClass.java')
            def text = stub.text

            assert text.contains('java.util.List<java.lang.String> list')
            assert text.contains('java.util.List<java.lang.String> front, java.lang.String last')
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
            config.jointCompilationOptions.stubDir.deleteDir()
        }
    }
}
