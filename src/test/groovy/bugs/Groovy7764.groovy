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

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit
import org.junit.jupiter.api.Test

/**
 * GROOVY-7764: joint compilation stubs for @Delegate (and similar
 * transforms such as @InheritConstructors) must include the generated
 * members, otherwise javac rejects a Java caller that uses them.
 */
final class Groovy7764 {

    @Test
    void testDelegateCallMethodIsInJointCompilationStub() {
        def config = new CompilerConfiguration(
            targetDirectory: File.createTempDir(),
            jointCompilationOptions: [
                stubDir: File.createTempDir(),
                keepStubs: true
            ]
        )

        def parentDir = File.createTempDir()
        try {
            def groovy = new File(parentDir, 'GroovyTest.groovy')
            groovy.write '''
                import groovy.transform.CompileStatic

                import java.util.concurrent.Callable

                @CompileStatic
                class GroovyTest implements Callable {
                    @Delegate
                    private final Callable c = { println "Hello World" }
                }
            '''
            def java = new File(parentDir, 'JavaTest.java')
            java.write '''
                public class JavaTest {
                    public static void main(String[] args) throws Exception {
                        new GroovyTest().call();
                    }
                }
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(groovy, java)
            cu.compile()

            def stub = new File(config.jointCompilationOptions.stubDir, 'GroovyTest.java')
            assert stub.exists()
            assert stub.text.contains('java.lang.Object call()')

            def javaTest = loader.loadClass('JavaTest')
            def baos = new ByteArrayOutputStream()
            def original = System.out
            System.out = new PrintStream(baos)
            try {
                javaTest.getMethod('main', String[]).invoke(null, [new String[0]] as Object[])
            } finally {
                System.out = original
            }
            assert baos.toString().contains('Hello World')
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
            config.jointCompilationOptions.stubDir.deleteDir()
        }
    }
}
