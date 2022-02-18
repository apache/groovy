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

final class Groovy9678 {

    @Test
    void testTraitStaticProperty() {
        def config = new CompilerConfiguration(
            targetDirectory: File.createTempDir(),
            jointCompilationOptions: [stubDir: File.createTempDir()]
        )

        def parentDir = File.createTempDir()
        try {
            def a = new File(parentDir, 'T.groovy')
            a.write '''
                trait T {
                    static p = 1
                }
            '''
            def b = new File(parentDir, 'C.groovy')
            b.write '''
                class C implements T {
                    static m() {
                        p = 2
                        p += 1
                        return p
                    }
                }
            '''
            def c = new File(parentDir, 'Main.java')
            c.write '''
                public class Main {
                    public static void main(String[] args) {
                        if (!C.m().equals(3)) throw new AssertionError();
                    }
                }
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(a, b, c)
            cu.compile()

            loader.loadClass('Main').main()
        } finally {
            config.jointCompilationOptions.stubDir.deleteDir()
            config.targetDirectory.deleteDir()
            parentDir.deleteDir()
        }
    }
}
