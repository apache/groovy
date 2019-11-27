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
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit
import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail

final class Groovy8361 {

    @Test
    void testAliasedImportNotUsed1() {
        def config = new CompilerConfiguration(
            targetDirectory: File.createTempDir(),
            jointCompilationOptions: [memStub: true]
        )

        def parentDir = File.createTempDir()
        new File(parentDir, 'p').mkdir()
        new File(parentDir, 'q').mkdir()
        try {
            def a = new File(parentDir, 'p/A.java')
            a.write '''
                package p;

                public class A {
                    public static class AA {
                    }
                }
            '''
            def b = new File(parentDir, 'q/B.groovy')
            b.write '''
                package q

                import p.A.AA as AAA

                println new AA() // should be unresolved
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(a, b)

            def err = shouldFail {
                cu.compile()
            }
            assert err =~ /unable to resolve class AA/
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
        }
    }

    @Test
    void testAliasedImportNotUsed2() {
        def config = new CompilerConfiguration(
            targetDirectory: File.createTempDir(),
            jointCompilationOptions: [memStub: true]
        )

        def parentDir = File.createTempDir()
        new File(parentDir, 'p').mkdir()
        new File(parentDir, 'q').mkdir()
        try {
            def a = new File(parentDir, 'p/A.groovy')
            a.write '''
                package p

                class A {
                    static class AA {
                    }
                }
            '''
            def b = new File(parentDir, 'q/B.groovy')
            b.write '''
                package q

                import p.A.AA as AAA

                println new AA() // should be unresolved
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new CompilationUnit(config, null, loader)
            cu.addSources(a, b)

            def err = shouldFail {
                cu.compile()
            }
            assert err =~ /unable to resolve class AA/
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
        }
    }
}
