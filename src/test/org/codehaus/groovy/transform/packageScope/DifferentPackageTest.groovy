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
package org.codehaus.groovy.transform.packageScope

import org.codehaus.groovy.control.*
import org.codehaus.groovy.tools.GroovyClass
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit
import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail

final class DifferentPackageTest {

    /** Class in package {@code p} with package-private fields {@code value} and {@code CONST}. */
    private static final String P_DOT_ONE = '''
        package p

        @groovy.transform.CompileStatic
        class One {
            @groovy.transform.PackageScope
            String value = 'value'
            @groovy.transform.PackageScope
            String getThing() { 'string' }
            @groovy.transform.PackageScope
            static final int CONST = 42
            @groovy.transform.PackageScope
            static int getAnswer() { 42 }
        }
    '''

    private ClassLoader addSources(Map<String, String> sources) {
        new CompilationUnit().with {
            sources.each { name, text -> addSource(name + '.groovy', text) }
            compile(Phases.CLASS_GENERATION)

            classes.each { GroovyClass groovyClass ->
                classLoader.defineClass(groovyClass.name, groovyClass.bytes)
            }
            return classLoader
        }
    }

    //--------------------------------------------------------------------------

    @Test
    void testSamePackageShouldSeeInstanceMethod() {
        def loader = addSources(
            One: P_DOT_ONE,
            Two: '''
                package p

                @groovy.transform.CompileStatic
                class Two extends One {
                    int size() {
                        getThing().size()
                    }
                }
            '''
        )
        assert loader.loadClass('p.Two').newInstance().size() == 6
    }

    @Test
    void testSamePackageShouldSeeInstanceProps1() {
        def loader = addSources(
            One: P_DOT_ONE,
            Two: '''
                package p

                @groovy.transform.CompileStatic
                class Two extends One {
                    int size() {
                        value.size()
                    }
                }
            '''
        )
        assert loader.loadClass('p.Two').newInstance().size() == 5
    }

    @Test
    void testSamePackageShouldSeeInstanceProps2() {
        def loader = addSources(
            One: P_DOT_ONE,
            Peer: '''
                package p

                @groovy.transform.CompileStatic
                class Peer {
                    int size() {
                        def one = new One()
                        one.value.size()
                    }
                }
            '''
        )
        assert loader.loadClass('p.Peer').newInstance().size() == 5
    }

    @Test
    void testSamePackageShouldSeeStaticProps1() {
        def loader = addSources(
            One: P_DOT_ONE,
            Two: '''
                package p

                @groovy.transform.CompileStatic
                class Two extends One {
                    static half() {
                        CONST / 2
                    }
                }
            '''
        )
        assert loader.loadClass('p.Two').half() == 21
    }

    @Test
    void testSamePackageShouldSeeStaticProps2() {
        def loader = addSources(
            One: P_DOT_ONE,
            Two: '''
                package p

                @groovy.transform.CompileStatic
                class Two extends One {
                    def half() {
                        CONST / 2
                    }
                }
            '''
        )
        assert loader.loadClass('p.Two').newInstance().half() == 21
    }

    @Test
    void testSamePackageShouldSeeStaticProps3() {
        def loader = addSources(
            One: P_DOT_ONE,
            Peer: '''
                package p

                @groovy.transform.CompileStatic
                class Peer {
                    static half() {
                        One.CONST / 2
                    }
                }
            '''
        )
        assert loader.loadClass('p.Peer').half() == 21
    }

    @Test
    void testSamePackageShouldSeeStaticProps4() {
        def loader = addSources(
            One: P_DOT_ONE,
            Peer: '''
                package p

                @groovy.transform.CompileStatic
                class Peer {
                    def half() {
                        One.CONST / 2
                    }
                }
            '''
        )
        assert loader.loadClass('p.Peer').newInstance().half() == 21
    }

    // GROOVY-9106
    @Test
    void testSamePackageShouldSeeStaticProps5() {
        def loader = addSources(
            One: P_DOT_ONE,
            Two: '''
                package q

                @groovy.transform.CompileStatic
                class Two extends p.One {
                }
            ''',
            Peer: '''
                package p

                @groovy.transform.CompileStatic
                class Peer {
                    static half() {
                        (q.Two.CONST / 2) // indirect access
                    }
                }
            '''
        )
        assert loader.loadClass('p.Peer').half() == 21
    }

    // GROOVY-11357
    @Test
    void testDiffPackageShouldNotSeeInstanceMethod() {
        def err = shouldFail CompilationFailedException, {
            addSources(
                One: P_DOT_ONE,
                Two: '''
                    package q

                    @groovy.transform.CompileStatic
                    class Two extends p.One {
                        int size() {
                            getThing().size() // not visible
                        }
                    }
                '''
            )
        }
        assert err =~ /Cannot find matching method q.Two#getThing()/

        def loader = addSources(
            One: P_DOT_ONE,
            Two: '''
                package q

                class Two extends p.One {
                    int size() {
                        getThing().size() // not visible
                    }
                }
            '''
        )
        def two = loader.loadClass('q.Two').newInstance()
        shouldFail MissingMethodException, {
            two.size()
        }

        loader = addSources(
            One: P_DOT_ONE,
            Two: '''
                package q

                class Two extends p.One {
                    // getThing shouldn't be indexed for Two
                }
            ''',
            Three: '''
                package r

                class Three {
                    int size() {
                        def two = new q.Two()
                        two.getThing().size() // not visible
                    }
                }
            '''
        )
        def three = loader.loadClass('r.Three').newInstance()
        shouldFail MissingMethodException, {
            three.size()
        }

        //

        def config = new CompilerConfiguration(
            targetDirectory: File.createTempDir(),
            jointCompilationOptions: [memStub: true]
        )
        def parentDir = File.createTempDir()
        try {
            new File(parentDir, 'p').mkdir()
            new File(parentDir, 'q').mkdir()
            new File(parentDir, 'r').mkdir()

            def a = new File(parentDir, 'p/A.java')
            a.write '''
                package p;
                public class A {
                    void packagePrivate() {}
                }
            '''
            def b = new File(parentDir, 'q/B.java')
            b.write '''
                package q;
                public class B extends p.A {
                }
            '''
            def c = new File(parentDir, 'r/C.groovy')
            c.write '''
                package r
                class C {
                    void test() {
                        def q_b = new q.B()
                        q_b.packagePrivate() // indirect reference
                    }
                }
            '''

            loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(a, b, c)
            cu.compile()

            def pogo = loader.loadClass('r.C').newInstance()
            shouldFail MissingMethodException, {
                pogo.test()
            }
        } finally {
            config.targetDirectory.deleteDir()
            parentDir.deleteDir()
        }
    }

    // GROOVY-9093
    @Test
    void testDiffPackageShouldNotSeeInstanceProps1() {
        def err = shouldFail CompilationFailedException, {
            addSources(
                One: P_DOT_ONE,
                Two: '''
                    package q

                    @groovy.transform.CompileStatic
                    class Two extends p.One {
                        int size() {
                            value.size() // not visible
                        }
                    }
                '''
            )
        }
        assert err =~ /No such property: value for class: q.Two/
    }

    @Test
    void testDiffPackageShouldNotSeeInstanceProps2() {
        def err = shouldFail CompilationFailedException, {
            addSources(
                One: P_DOT_ONE,
                Two: '''
                    package q

                    @groovy.transform.CompileStatic
                    class Two {
                        int size() {
                            def one = new p.One()
                            one.value.size() // not visible
                        }
                    }
                '''
            )
        }
        assert err =~ /Access to p.One#value is forbidden/
    }

    // GROOVY-9093
    @Test
    void testDiffPackageShouldNotSeeStaticProps1() {
        def err = shouldFail CompilationFailedException, {
            addSources(
                One: P_DOT_ONE,
                Two: '''
                    package q

                    @groovy.transform.CompileStatic
                    class Two extends p.One {
                        static half() {
                            (CONST / 2) // not visible
                        }
                    }
                '''
            )
        }
        assert err =~ /No such property: CONST for class: q.Two/
    }

    // GROOVY-11356
    @Test
    void testDiffPackageShouldNotSeeStaticProps2() {
        def err = shouldFail CompilationFailedException, {
            addSources(
                One: P_DOT_ONE,
                Two: '''
                    package q

                    @groovy.transform.CompileStatic
                    class Two extends p.One {
                        static half() {
                            (answer / 2) // not visible
                        }
                    }
                '''
            )
        }
        assert err =~ /No such property: answer for class: q.Two/
    }

    @Test
    void testDiffPackageShouldNotSeeStaticProps3() {
        def err = shouldFail CompilationFailedException, {
            addSources(
                One: P_DOT_ONE,
                Other: '''
                    package q

                    import p.One

                    @groovy.transform.CompileStatic
                    class Other {
                        static half() {
                            (One.CONST / 2) // not visible
                        }
                    }
                '''
            )
        }
        assert err =~ /Access to p.One#CONST is forbidden/
    }

    @Test
    void testDiffPackageShouldNotSeeStaticProps4() {
        def err = shouldFail CompilationFailedException, {
            addSources(
                One: P_DOT_ONE,
                Other: '''
                    package q

                    @groovy.transform.CompileStatic
                    class Other {
                        static half() {
                            (p.One.answer / 2) // not visible
                        }
                    }
                '''
            )
        }
        assert err =~ /No such property: answer for class: p.One/ // TODO: Cannot access p.One#getAnswer?
    }
}
