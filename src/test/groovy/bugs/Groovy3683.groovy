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
 * GROOVY-3683: joint compilation of a Java class that calls a
 * delegated getter from a Groovy class with {@code @Delegate}
 * used to fail. The original report is Test.groovy + TestRun.java
 * with {@code new Test().getName()}.
 *
 * Related coverage also exists in
 * {@code org.codehaus.groovy.tools.stubgenerator.DelegateJointCompilationStubTest}
 * (same-unit Groovy delegate, Java caller). This class keeps the
 * ticket's exact sources as a named regression.
 */
final class Groovy3683 {

    @Test
    void testJointCompileJavaCallerOfDelegatedProperty() {
        def config = new CompilerConfiguration(
            targetDirectory: File.createTempDir(),
            jointCompilationOptions: [memStub: true]
        )
        def parentDir = File.createTempDir()
        try {
            new File(parentDir, 'Test.groovy').write '''
                class Test {
                  @Delegate TestDelegate test = new TestDelegate(name:"Scott")
                }

                class TestDelegate { String name }
            '''
            new File(parentDir, 'TestRun.java').write '''
                public class TestRun {
                  public static void main(String args[]) {
                    System.out.println(new Test().getName());
                  }
                }
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(parentDir.listFiles())
            cu.compile()

            def baos = new ByteArrayOutputStream()
            def orig = System.out
            System.setOut(new PrintStream(baos, true))
            try {
                loader.loadClass('TestRun').getMethod('main', String[]).invoke(null, [new String[0]] as Object[])
            } finally {
                System.setOut(orig)
            }
            assert baos.toString().trim() == 'Scott'
        } finally {
            config.targetDirectory.deleteDir()
            parentDir.deleteDir()
        }
    }
}
