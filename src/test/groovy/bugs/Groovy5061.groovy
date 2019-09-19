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

import groovy.test.GroovyTestCase
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit

class Groovy5061 extends GroovyTestCase {
    void testShouldCompileProperly() {
        def config = new CompilerConfiguration()
        config.with {
            targetDirectory = createTempDir()
            jointCompilationOptions = [stubDir: createTempDir()]
        }

        File parentDir = createTempDir()
        try {
            def a = new File(parentDir, 'A.groovy')
            a.write '''
            class A {
                Map<String, Map<String, Integer[]>> columnsMap = [:]
            }
            '''

            def b = new File(parentDir, 'B.java')
            b.write '''
            public class B {
                public void f(A a) {
                    System.out.println(a.getColumnsMap());
                }
            }
        '''
            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources([a, b] as File[])
            cu.compile()
        } finally {
            parentDir.deleteDir()
            config.targetDirectory?.deleteDir()
            config.jointCompilationOptions.stubDir?.deleteDir()
        }

    }

    void testShouldCompileProperly2() {
        def config = new CompilerConfiguration()
        config.with {
            targetDirectory = createTempDir()
            jointCompilationOptions = [stubDir: createTempDir()]
        }

        File parentDir = createTempDir()
        try {
            def a = new File(parentDir, 'A.groovy')
            a.write '''
            class A {
                Map<String, Map<String, List<Integer[]>[]>> columnsMap = [:]
            }
            '''

            def b = new File(parentDir, 'B.java')
            b.write '''
            public class B {
                public void f(A a) {
                    System.out.println(a.getColumnsMap());
                }
            }
        '''
            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources([a, b] as File[])
            cu.compile()
        } finally {
            parentDir.deleteDir()
            config.targetDirectory?.deleteDir()
            config.jointCompilationOptions.stubDir?.deleteDir()
        }

    }

    private static File createTempDir() {
        File.createTempDir("groovyTest${System.currentTimeMillis()}", "")
    }
}
