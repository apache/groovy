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
 * GROOVY-4744: extending a Java class whose {@code getProperty(String)}
 * is {@code public final} and returns {@code String} used to fail
 * compilation ({@code You are not allowed to override the final method
 * getProperty(java.lang.String)}). The {@code Object}-returning variant
 * (GROOVY-2837) already compiled. The original report is {@code Java2}
 * plus empty {@code class Groovy2 extends Java2}.
 */
final class Groovy4744 {

    @Test
    void testExtendJavaClassWithFinalObjectGetProperty() {
        compileAndLoad('Java1', 'Object', 'Groovy1')
    }

    @Test
    void testExtendJavaClassWithFinalStringGetProperty() {
        def loaded = compileAndLoad('Java2', 'String', 'Groovy2')
        def instance = loaded.getDeclaredConstructor().newInstance()
        def method = loaded.getMethod('getProperty', String)
        assert method.declaringClass.name == 'Java2'
        assert method.returnType == String
        assert method.invoke(instance, 'x') == null
    }

    private static Class compileAndLoad(String javaName, String returnType, String groovyName) {
        def config = new CompilerConfiguration(
            targetDirectory: File.createTempDir(),
            jointCompilationOptions: [memStub: true]
        )
        def parentDir = File.createTempDir()
        try {
            new File(parentDir, "${javaName}.java").write """
                public class ${javaName} {
                  public final ${returnType} getProperty(String s) { return null; }
                }
            """
            new File(parentDir, "${groovyName}.groovy").write """
                class ${groovyName} extends ${javaName} { }
            """

            def loader = new GroovyClassLoader(Groovy4744.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(parentDir.listFiles())
            cu.compile()
            return loader.loadClass(groovyName)
        } finally {
            config.targetDirectory.deleteDir()
            parentDir.deleteDir()
        }
    }
}
