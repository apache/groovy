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

import static groovy.test.GroovyAssert.assertScript

final class Groovy8964 {

    @Test
    void testInstanceVarargMethodNotMaskedByStaticMethodWithSameNumberOfArgs() {
        assertScript '''
            class C {
                def m(String... args) {
                    'vararg'
                }
                static m(List<String> args, File workDirectory, Appendable out, Appendable err) {
                    'multi'
                }
                def test() {
                    m('a', 'b', 'c', 'd')
                }
            }

            assert new C().test() == 'vararg'
        '''
    }

    @Test // GROOVY-9737
    void testInstanceMethodNotMaskedByStaticMethodWithSameNumberOfArgs1() {
        assertScript '''
            abstract class A {
                static void m(Integer i) {}
                protected void m(String s) {}
            }

            @groovy.transform.CompileStatic
            class C extends A {
                void test() {
                    m('') // ClassCastException: class java.lang.Class cannot be cast to class A
                }
            }

            new C().test()
        '''
    }

    @Test // GROOVY-9737
    void testInstanceMethodNotMaskedByStaticMethodWithSameNumberOfArgs2() {
        assertScript '''
            import groovy.bugs.Groovy8964.A
            @groovy.transform.CompileStatic
            class C extends A {
                void test() {
                    m('') // VerifyError: Bad access to protected data in invokevirtual
                }
            }

            new C().test()
        '''
    }

    @Test // GROOVY-10379
    void testInstanceMethodNotMaskedByStaticMethodWithSameNumberOfArgs3() {
        def config = new CompilerConfiguration(
            targetDirectory: File.createTempDir(),
            jointCompilationOptions: [memStub: true]
        )

        def parentDir = File.createTempDir()
        try {
            new File(parentDir, 'p').mkdir()

            def a = new File(parentDir, 'p/A.java')
            a.write '''
                package p;
                public abstract class A implements I {
                    public static String m(Number n) { return "number"; }
                }
            '''
            def b = new File(parentDir, 'p/I.java')
            b.write '''
                package p;
                public interface I {
                    default String m(String s) { return "string"; }
                }
            '''
            def c = new File(parentDir, 'Main.groovy')
            c.write '''
                @groovy.transform.CompileStatic
                class C extends p.A {
                    void test() {
                        String result = m('') // GroovyCastException: Cannot cast object 'class C' with class 'java.lang.Class' to class 'p.I'
                        assert result == 'string'
                    }
                }
                new C().test()
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(a, b, c)
            cu.compile()

            loader.loadClass('Main').getDeclaredConstructor().newInstance().run()
        } finally {
            config.targetDirectory.deleteDir()
            parentDir.deleteDir()
        }
    }

    static abstract class A {
        static void m(Integer i) {}
        protected void m(String s) {}
    }
}
