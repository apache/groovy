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
package org.codehaus.groovy.control

import org.codehaus.groovy.tools.GroovyClass
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * Tests for {@link CompilationUnit}'s ClassWriter and getCommonSuperClass lookups (GROOVY-12288).
 */
class ClassWriterCommonSuperClassTest {

    @Test
    void testBasicClassHierarchyMergeAndExecution() {
        GroovyClassLoader gcl = new GroovyClassLoader()
        String script = '''
            package test.pkg
            
            class Base {}
            class ChildA extends Base {}
            class ChildB extends Base {}
            
            class Tester {
                static Base choose(boolean flag) {
                    return flag ? new ChildA() : new ChildB()
                }
            }
        '''
        CompilationUnit cu = new CompilationUnit(CompilerConfiguration.DEFAULT, null, gcl)
        cu.addSource("test/pkg/Test.groovy", script)
        cu.compile(Phases.CLASS_GENERATION)

        List<GroovyClass> classes = cu.getClasses()
        assertNotNull(classes)
        assertTrue(classes.size() >= 4)

        for (GroovyClass gc : classes) {
            gcl.defineClass(gc.getName(), gc.getBytes())
        }

        Class<?> testerClass = gcl.loadClass('test.pkg.Tester')
        def resA = testerClass.getMethod('choose', boolean.class).invoke(null, true)
        def resB = testerClass.getMethod('choose', boolean.class).invoke(null, false)
        assertEquals('test.pkg.ChildA', resA.class.name)
        assertEquals('test.pkg.ChildB', resB.class.name)
    }

    @Test
    void testDeepHierarchyAndMultiCatchExecution() {
        GroovyClassLoader gcl = new GroovyClassLoader()
        String script = '''
            package test.deep
            
            class L0 {}
            class L1 extends L0 {}
            class L2 extends L1 {}
            class L3 extends L2 {}
            class Leaf1 extends L3 {}
            class Leaf2 extends L3 {}
            
            class DeepTester {
                static Object process(int mode, boolean flag) {
                    def val = null
                    try {
                        if (mode == 1) throw new java.io.FileNotFoundException("fnf")
                        if (mode == 2) throw new java.io.EOFException("eof")
                        val = flag ? new Leaf1() : new Leaf2()
                    } catch (java.io.FileNotFoundException | java.io.EOFException e) {
                        val = flag ? new java.util.ArrayList() : new java.util.LinkedList()
                    } catch (Exception e) {
                        val = flag ? new java.util.HashMap() : new java.util.TreeMap()
                    }
                    return val
                }
            }
        '''
        CompilationUnit cu = new CompilationUnit(CompilerConfiguration.DEFAULT, null, gcl)
        cu.addSource("test/deep/DeepTest.groovy", script)
        cu.compile(Phases.CLASS_GENERATION)

        for (GroovyClass gc : cu.getClasses()) {
            gcl.defineClass(gc.getName(), gc.getBytes())
        }

        Class<?> testerClass = gcl.loadClass('test.deep.DeepTester')
        def res0 = testerClass.getMethod('process', int.class, boolean.class).invoke(null, 0, true)
        def res1 = testerClass.getMethod('process', int.class, boolean.class).invoke(null, 1, false)
        def res2 = testerClass.getMethod('process', int.class, boolean.class).invoke(null, 2, true)

        assertEquals('test.deep.Leaf1', res0.class.name)
        assertEquals(java.util.LinkedList.class, res1.class)
        assertEquals(java.util.ArrayList.class, res2.class)
    }

    @Test
    void testStaticCompileWithComplexGenericsAndLoops() {
        GroovyClassLoader gcl = new GroovyClassLoader()
        String script = '''
            package test.sc
            import groovy.transform.CompileStatic
            
            @CompileStatic
            class StaticTester {
                static Object mergeComplex(int count, boolean flag) {
                    Object result = flag ? new java.util.ArrayList<String>() : new java.util.LinkedList<String>()
                    for (int i = 0; i < count; i++) {
                        Object inner = (i % 2 == 0) ? (flag ? new java.util.HashSet<Integer>() : new java.util.TreeSet<Integer>())
                                                    : (flag ? new StringBuilder() : new StringBuffer())
                        if (i == count - 1) {
                            result = inner
                        }
                    }
                    return result
                }
            }
        '''
        CompilationUnit cu = new CompilationUnit(CompilerConfiguration.DEFAULT, null, gcl)
        cu.addSource("test/sc/StaticTester.groovy", script)
        cu.compile(Phases.CLASS_GENERATION)

        for (GroovyClass gc : cu.getClasses()) {
            gcl.defineClass(gc.getName(), gc.getBytes())
        }

        Class<?> testerClass = gcl.loadClass('test.sc.StaticTester')
        def res = testerClass.getMethod('mergeComplex', int.class, boolean.class).invoke(null, 4, true)
        assertEquals(StringBuilder.class, res.class)
    }

    @Test
    void testInterfaceAndNestedClosureCompilation() {
        GroovyClassLoader gcl = new GroovyClassLoader()
        String script = '''
            package test.closure
            
            interface IntfA {}
            interface IntfB {}
            class ImplA implements IntfA {}
            class ImplB implements IntfB {}
            
            class ClosureTester {
                static List evaluate() {
                    def list = [1, 2, 3, 4]
                    def c1 = { int x -> (x % 2 == 0) ? new ImplA() : new ImplB() }
                    def c2 = { int x -> (x % 2 == 0) ? new java.util.ArrayList() : new java.util.HashSet() }
                    return list.collect(c1) + list.collect(c2)
                }
            }
        '''
        CompilationUnit cu = new CompilationUnit(CompilerConfiguration.DEFAULT, null, gcl)
        cu.addSource("test/closure/ClosureTester.groovy", script)
        cu.compile(Phases.CLASS_GENERATION)

        for (GroovyClass gc : cu.getClasses()) {
            gcl.defineClass(gc.getName(), gc.getBytes())
        }

        Class<?> testerClass = gcl.loadClass('test.closure.ClosureTester')
        List res = (List) testerClass.getMethod('evaluate').invoke(null)
        assertEquals(8, res.size())
    }
}
