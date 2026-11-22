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
 * GROOVY-8044: a Java-style local variable whose type is a fully
 * qualified lowercase class name, with an initializer, was compiled
 * as a property access on the first package segment and failed at
 * runtime with MissingPropertyException.
 */
final class Groovy8044 {

    @Test
    void testJavaStyleLocalVariableWithLowercaseClassName() {
        def config = new CompilerConfiguration(
            targetDirectory: File.createTempDir(),
            jointCompilationOptions: [
                stubDir: File.createTempDir(),
                keepStubs: true
            ]
        )
        def parentDir = File.createTempDir()
        try {
            def pkg = new File(parentDir, 'de/example/lib')
            pkg.mkdirs()
            new File(pkg, 'myJavaFile.java').write '''
                package de.example.lib;
                public class myJavaFile {
                    public String name() { return "ok"; }
                }
            '''
            def groovy = new File(parentDir, 'de/example/Script2.groovy')
            groovy.parentFile.mkdirs()
            groovy.write '''
                package de.example
                class Script2 {
                    def getInterfaceVersion() {
                        de.example.lib.myJavaFile theJavaFile = new de.example.lib.myJavaFile()
                        return theJavaFile.name()
                    }
                }
            '''
            def aliased = new File(parentDir, 'Aliased.groovy')
            aliased.write '''
                import de.example.lib.myJavaFile as JavaFile
                class Aliased {
                    def getInterfaceVersion() {
                        JavaFile theJavaFile = new JavaFile()
                        return theJavaFile.name()
                    }
                }
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(new File(pkg, 'myJavaFile.java'), groovy, aliased)
            cu.compile()

            def script2 = loader.loadClass('de.example.Script2').getDeclaredConstructor().newInstance()
            assert script2.getInterfaceVersion() == 'ok'

            def alias = loader.loadClass('Aliased').getDeclaredConstructor().newInstance()
            assert alias.getInterfaceVersion() == 'ok'
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
            config.jointCompilationOptions.stubDir.deleteDir()
        }
    }
}
