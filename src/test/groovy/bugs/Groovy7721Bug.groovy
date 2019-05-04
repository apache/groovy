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

class Groovy7721Bug extends GroovyTestCase {
    void testCovariantArrayAtOverriding() {
        def config = new CompilerConfiguration()
        config.with {
            targetDirectory = createTempDir()
            jointCompilationOptions = [stubDir: createTempDir()]
        }

        File parentDir = createTempDir()
        try {
            def a = new File(parentDir, 'A.java')
            a.write '''
                    package pack;
                    interface A {
                        Object[] bar();
                    }

                '''

            //    We should declare interface B as public, or B can not be accessed from outside package(Note: C is in the default package)
            //
            //    See the error message:
            //    /tmp/groovyTest15544748311926307266330586729753/C.java:17: error: B is not public in pack; cannot be accessed from outside package
            //    public static  java.lang.Object bar(pack.B b) { return null;}
            def b = new File(parentDir, 'B.java')
            b.write '''
                    package pack;
                    public interface B extends A {
                        @Override
                        String[] bar();
                    }
                '''

            def c = new File(parentDir, 'C.groovy')
            c.write '''
            import groovy.transform.CompileStatic

            @CompileStatic
            class C {
                static def bar(pack.B b) {
                    b.bar()
                }
            }
            '''
            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources([a, b, c] as File[])
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
