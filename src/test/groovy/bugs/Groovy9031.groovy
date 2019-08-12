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

final class Groovy9031 {

    @Test
    void testGenerics() {
        def config = new CompilerConfiguration(
            targetDirectory: File.createTempDir(),
            jointCompilationOptions: [stubDir: File.createTempDir(), keepStubs: true]
        )

        def parentDir = File.createTempDir()
        try {
            def a = new File(parentDir, 'Trait.groovy')
            a.write '''
                trait Trait<V> {
                    V value
                }
            '''
            def b = new File(parentDir, 'TraitImpl.groovy')
            b.write '''
                class TraitImpl implements Trait<String> {
                }
            '''
            def c = new File(parentDir, 'Whatever.java')
            c.write '''
                class Whatever {
                    void meth() {
                        new TraitImpl().getValue();
                    }
                }
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(a, b, c)
            cu.compile()

            def stub = new File(config.jointCompilationOptions.stubDir, 'TraitImpl.java')
            def text = stub.text

            assert text.contains('java.lang.String getValue()')
            assert text.contains('void setValue(java.lang.String value)')
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
            config.jointCompilationOptions.stubDir.deleteDir()
        }
    }
}
