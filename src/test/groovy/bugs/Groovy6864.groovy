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
 * GROOVY-6864: joint-compilation stubs for a Groovy class annotated
 * with {@code @TupleConstructor} used to omit the generated tuple
 * constructor, so a Java caller {@code new SomeGroovyClass("val")}
 * failed to compile.
 */
final class Groovy6864 {

    @Test
    void testTupleConstructorAppearsInJointCompilationStub() {
        def stubDir = File.createTempDir()
        def config = new CompilerConfiguration(
            targetDirectory: File.createTempDir(),
            jointCompilationOptions: [stubDir: stubDir, keepStubs: true]
        )
        def parentDir = File.createTempDir()
        try {
            new File(parentDir, 'SomeGroovyClass.groovy').write '''
                @groovy.transform.TupleConstructor
                class SomeGroovyClass {
                    String someValue
                }
            '''
            new File(parentDir, 'Client.java').write '''
                public class Client {
                    public static void main(String[] args) {
                        SomeGroovyClass groovyClass = new SomeGroovyClass("val");
                        if (!"val".equals(groovyClass.getSomeValue())) {
                            throw new AssertionError(groovyClass.getSomeValue());
                        }
                    }
                }
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(parentDir.listFiles())
            cu.compile()

            def stub = new File(stubDir, 'SomeGroovyClass.java').text
            assert stub =~ /public\s+SomeGroovyClass\s*\(\s*java\.lang\.String\s+\w+\s*\)/

            loader.loadClass('Client').getMethod('main', String[]).invoke(null, [new String[0]] as Object[])
        } finally {
            config.targetDirectory.deleteDir()
            parentDir.deleteDir()
            stubDir.deleteDir()
        }
    }
}
