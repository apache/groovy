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
import static groovy.test.GroovyAssert.shouldFail

final class Groovy10381 {
    @Test
    void testDuplicateDefaultMethodsFromGroovyClasses_implements0() {
        assertScript '''
            interface A {
              default String m(String n) { n.toLowerCase() }
            }

            interface B {
              default String m(String n) { n.toUpperCase() }
            }

            class C1 implements A, B {
              @Override
              String m(String n) { A.super.m(n) }
            }
            assert new C1().m('Hi') == 'hi'

            class C2 implements A, B {
              @Override
              String m(String n) { B.super.m(n) }
            }
            assert new C2().m('Hi') == 'HI'

            class C3 implements A, B {
              @Override
              String m(String n) { 'overridden' }
            }
            assert new C3().m('Hi') == 'overridden'
        '''
    }

    @Test
    void testDuplicateDefaultMethodsFromGroovyClasses_implements1() {
        def err = shouldFail '''
            interface A {
              default void m(String n) {}
            }
            interface B {
              default void m(String n) {}
            }
            class C implements A, B {
              void test() {
                m('Hi')
              }
            }
        '''
        assert err =~ /class C inherits unrelated defaults for void m\(java.lang.String\) from types A and B/
    }

    @Test
    void testDuplicateDefaultMethodsFromGroovyClasses_implements2() {
        def err = shouldFail '''
            interface BaseA {
              default void m(String n) {}
            }
            interface A extends BaseA {
            }
            interface BaseB {
              default void m(String n) {}
            }
            interface B extends BaseB {
            }
            class C implements A, B {
              void test() {
                m('Hi')
              }
            }
        '''
        assert err =~ /class C inherits unrelated defaults for void m\(java.lang.String\) from types BaseA and BaseB/
    }

    @Test
    void testDuplicateDefaultMethodsFromGroovyClasses_override() {
        def err = shouldFail '''
            interface BaseA {
              default void m(String n) {}
            }
            interface A extends BaseA {
              @Override
              default void m(String n) {}
            }
            interface BaseB {
              default void m(String n) {}
            }
            interface B extends BaseB {
              @Override
              default void m(String n) {}
            }
            class C implements A, B {
              void test() {
                m('Hi')
              }
            }
        '''
        assert err =~ /class C inherits unrelated defaults for void m\(java.lang.String\) from types A and B/
    }

    @Test
    void testDuplicateDefaultMethodsFromGroovyClasses_extends() {
        def err = shouldFail '''
            public interface A extends List {
              default void m(String n) {}
            }
            public interface B {
              default void m(String n) {}
            }
            interface C extends A, B {
              default void test() {
                m('Hi')
              }
            }
        '''
        assert err =~ /interface C inherits unrelated defaults for void m\(java.lang.String\) from types A and B/
    }

    @Test
    void testDuplicateDefaultMethodsFromJavaClasses_implements() {
        def config = new CompilerConfiguration().tap {
            jointCompilationOptions = [memStub: true]
            targetDirectory = File.createTempDir()
        }
        File parentDir = File.createTempDir()
        try {
            def a = new File(parentDir, 'A.java')
            a.write '''
                public interface A {
                    default void m(String n) {}
                }
            '''

            def b = new File(parentDir, 'B.java')
            b.write '''
                public interface B {
                    default void m(String n) {}
                }
            '''

            def c = new File(parentDir, 'C.groovy')
            c.write '''
                class C implements A, B {
                    void test() {
                        m("test")
                    }
                }
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(a, b, c)
            cu.compile()
            assert false
        } catch (Exception e) {
            assert e =~ /class C inherits unrelated defaults for m\(String\) from types A and B/
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
        }
    }

    @Test
    void testDuplicateDefaultMethodsFromJavaClasses_extends() {
        def config = new CompilerConfiguration().tap {
            jointCompilationOptions = [memStub: true]
            targetDirectory = File.createTempDir()
        }
        File parentDir = File.createTempDir()
        try {
            def a = new File(parentDir, 'A.java')
            a.write '''
                public interface A extends java.util.List {
                    default void m(String n) {}
                }
            '''

            def b = new File(parentDir, 'B.java')
            b.write '''
                public interface B {
                    default void m(String n) {}
                }
            '''

            def c = new File(parentDir, 'C.groovy')
            c.write '''
                interface C extends A, B {
                    default void test() {
                        m("test")
                    }
                }
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(a, b, c)
            cu.compile()
            assert false
        } catch (Exception e) {
            assert e =~ /interface C inherits unrelated defaults for m\(String\) from types A and B/
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
        }
    }
}
