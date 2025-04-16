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
    void testSamePackageShouldSeeInstanceProps1() {
        def loader = addSources(
            One: P_DOT_ONE,
            Two: '''
                package p

                @groovy.transform.CompileStatic
                class Two extends One {
                    int valueSize() {
                        value.size()
                    }
                }
            '''
        )
        assert loader.loadClass('p.Two').newInstance().valueSize() == 5
    }

    @Test
    void testSamePackageShouldSeeInstanceProps2() {
        def loader = addSources(
            One: P_DOT_ONE,
            Peer: '''
                package p

                @groovy.transform.CompileStatic
                class Peer {
                    int valueSize() {
                        new One().value.size()
                    }
                }
            '''
        )
        assert loader.loadClass('p.Peer').newInstance().valueSize() == 5
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

    // GROOVY-9093
    @Test
    void testDifferentPackageShouldNotSeeInstanceProps() {
        def err = shouldFail CompilationFailedException, {
            addSources(
                One: P_DOT_ONE,
                Two: '''
                    package q

                    @groovy.transform.CompileStatic
                    class Two extends p.One {
                        int valueSize() {
                            value.size() // not visible
                        }
                    }
                '''
            )
        }
        assert err =~ /No such property: value for class: q.Two/
    }

    // GROOVY-9093
    @Test
    void testDifferentPackageShouldNotSeeStaticProps1() {
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
    void testDifferentPackageShouldNotSeeStaticProps2() {
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
    void testDifferentPackageShouldNotSeeStaticProps3() {
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
    void testDifferentPackageShouldNotSeeStaticProps4() {
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
