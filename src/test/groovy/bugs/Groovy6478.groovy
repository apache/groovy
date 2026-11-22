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

import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit
import org.junit.jupiter.api.Test

/**
 * GROOVY-6478: {@code import static foo.Bar.xyz} was resolved to an inner
 * class {@code foo.Bar} when the first-level {@code foo.Bar} was a Java
 * class, but to the first-level method when {@code foo.Bar} was Groovy.
 * Both cases must resolve to the first-level class method.
 */
final class Groovy6478 {

    @Test
    void testStaticImportResolvesToJavaFirstLevelClassNotInnerClass() {
        def config = new CompilerConfiguration(
            targetDirectory: File.createTempDir(),
            jointCompilationOptions: [memStub: true]
        )
        def parentDir = File.createTempDir()
        try {
            def pkg = new File(parentDir, 'foo')
            pkg.mkdirs()
            new File(pkg, 'Bar.java').write '''
                package foo;
                public class Bar {
                    public static String xyz() { return "JAVA_TOPLEVEL"; }
                }
            '''
            new File(parentDir, 'fooHolder.groovy').write '''
                import static foo.Bar.xyz

                class foo {
                    static class Bar {
                        static xyz() { return "INNER" }
                    }
                    static which() { xyz() }
                }
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(new File(pkg, 'Bar.java'), new File(parentDir, 'fooHolder.groovy'))
            cu.compile()

            loader.addClasspath(config.targetDirectory.absolutePath)
            assert loader.loadClass('foo').which() == 'JAVA_TOPLEVEL'
        } finally {
            config.targetDirectory.deleteDir()
            parentDir.deleteDir()
        }
    }

    @Test
    void testStaticImportResolvesToGroovyFirstLevelClassNotInnerClass() {
        def config = new CompilerConfiguration(targetDirectory: File.createTempDir())
        def parentDir = File.createTempDir()
        try {
            def pkg = new File(parentDir, 'foo')
            pkg.mkdirs()
            new File(pkg, 'Bar.groovy').write '''
                package foo
                class Bar {
                    static String xyz() { return "GROOVY_TOPLEVEL" }
                }
            '''
            new File(parentDir, 'fooHolder.groovy').write '''
                import static foo.Bar.xyz

                class foo {
                    static class Bar {
                        static xyz() { return "INNER" }
                    }
                    static which() { xyz() }
                }
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new CompilationUnit(config, null, loader)
            cu.addSources(new File(pkg, 'Bar.groovy'), new File(parentDir, 'fooHolder.groovy'))
            cu.compile()

            loader.addClasspath(config.targetDirectory.absolutePath)
            assert loader.loadClass('foo').which() == 'GROOVY_TOPLEVEL'
        } finally {
            config.targetDirectory.deleteDir()
            parentDir.deleteDir()
        }
    }
}
