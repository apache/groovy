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

import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.junit.Test

final class Groovy10143 {
    @Test
    void testTraitsAndTypes() {
        def config = new CompilerConfiguration(targetDirectory: File.createTempDir())
        config.addCompilationCustomizers(new ASTTransformationCustomizer(CompileStatic))

        def parentDir = File.createTempDir()
        try {
            new File(parentDir, 'p').mkdir()

            def a = new File(parentDir, 'p/A.groovy')
            a.write '''
                package p

                abstract class A {
                }
            '''
            def b = new File(parentDir, 'p/B.groovy')
            b.write '''
                package p

                class B extends A {
                    public final T t
                    B(T t) { this.t = t }
                }
            '''
            def c = new File(parentDir, 'p/C.groovy')
            c.write '''
                package p

                @groovy.transform.InheritConstructors
                class C extends B {
                    String getString() { 'string' }
                }
            '''
            def d = new File(parentDir, 'p/T.groovy')
            d.write '''
                package p

                @groovy.transform.SelfType(A)
                trait T {
                    abstract boolean booleanMethod1()
                    abstract boolean booleanMethod2()
                    abstract boolean booleanMethod3()

                    B manager

                    def getManager() {
                        if (manager == null) {
                            manager = new B(this)
                        }
                        manager
                    }
                }
            '''
            def e = new File(parentDir, 'p/U.groovy')
            e.write '''
                package p

                //@groovy.transform.SelfType(A)
                trait U extends T {
                    boolean booleanMethod1() { true }
                    boolean booleanMethod2() { true }
                    boolean booleanMethod3() { true }

                    @Override @groovy.transform.CompileDynamic
                    def getManager() {
                        if (manager == null) {
                            manager = new C(this)
                        }
                        manager
                    }
                }
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new CompilationUnit(config, null, loader)
            cu.addSources(a, b, c, d, e)
            cu.compile()
        } finally {
            config.targetDirectory.deleteDir()
            parentDir.deleteDir()
        }
    }
}
