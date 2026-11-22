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
 * GROOVY-6865: joint-compilation stubs for a Groovy class with
 * {@code @Delegate} used to omit delegated methods such as
 * {@code getValue()}, so a Java caller failed to compile.
 */
final class Groovy6865 {

    @Test
    void testDelegatedGetterAppearsInJointCompilationStub() {
        def stubDir = File.createTempDir()
        def config = new CompilerConfiguration(
            targetDirectory: File.createTempDir(),
            jointCompilationOptions: [stubDir: stubDir, keepStubs: true]
        )
        def parentDir = File.createTempDir()
        try {
            new File(parentDir, 'SomeDelegate.groovy').write '''
                class SomeDelegate {
                    String value
                    SomeDelegate(String value) {
                        this.value = value
                    }
                }
            '''
            new File(parentDir, 'SomeGroovyClass.groovy').write '''
                class SomeGroovyClass {
                    @Delegate
                    SomeDelegate delegate

                    SomeGroovyClass(SomeDelegate delegate) {
                        this.delegate = delegate
                    }
                }
            '''
            new File(parentDir, 'Client.java').write '''
                public class Client {
                    public static String run() {
                        SomeDelegate delegate = new SomeDelegate("val1");
                        SomeGroovyClass groovyClass = new SomeGroovyClass(delegate);
                        return delegate.getValue() + "," + groovyClass.getValue();
                    }
                }
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(parentDir.listFiles())
            cu.compile()

            def stub = new File(stubDir, 'SomeGroovyClass.java').text
            assert stub =~ /public\s+java\.lang\.String\s+getValue\s*\(\s*\)/

            def result = loader.loadClass('Client').getMethod('run').invoke(null)
            assert result == 'val1,val1'
        } finally {
            config.targetDirectory.deleteDir()
            parentDir.deleteDir()
            stubDir.deleteDir()
        }
    }
}
